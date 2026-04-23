package com.guitartrainer.audio;

import com.guitartrainer.input.LaneKey;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DesktopGuitarInputService implements GuitarInputService {

    private static final float SAMPLE_RATE = 44_100f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    // 1024 samples @ 44100 Hz = ~23ms per buffer
    private static final int BUFFER_SAMPLES = 1_024;
    private static final int BUFFER_BYTES = BUFFER_SAMPLES * 2;

    private static final float MIN_FREQUENCY_HZ = 70f;
    private static final float MAX_FREQUENCY_HZ = 1_200f;
    private static final float MIN_RMS_LEVEL = 0.01f;
    private static final float MIN_CONFIDENCE = 0.75f;
    private static final float A4_HZ = 440f;
    private static final int SILENCE_FRAMES_REQUIRED = 3;

    private static final int MIDI_E2 = 40;
    private static final int MIDI_A2 = 45;
    private static final int MIDI_D3 = 50;
    private static final int MIDI_G3 = 55;

    private static final float E2_CENTS_TOLERANCE = 90f;
    private static final float A2_CENTS_TOLERANCE = 70f;
    private static final float D3_CENTS_TOLERANCE = 85f;
    private static final float G3_CENTS_TOLERANCE = 85f;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicReference<PitchSnapshot> latestSnapshot =
            new AtomicReference<>(PitchSnapshot.idle("Guitar input idle"));
    private final ConcurrentLinkedQueue<LaneEvent> laneEventQueue = new ConcurrentLinkedQueue<>();

    // edge-detection state — only touched by capture thread
    private LaneKey lastDetectedLane = null;
    private int silenceFrames = 0;

    private TargetDataLine line;
    private Thread captureThread;

    @Override
    public synchronized void start() {
        if (running.get()) return;

        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            latestSnapshot.set(PitchSnapshot.idle("No supported microphone line"));
            return;
        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format, BUFFER_BYTES * 2);
            line.start();
        } catch (LineUnavailableException exception) {
            latestSnapshot.set(PitchSnapshot.idle("Microphone unavailable"));
            line = null;
            return;
        }

        running.set(true);
        latestSnapshot.set(PitchSnapshot.idle("Listening for guitar..."));
        captureThread = new Thread(this::captureLoop, "guitar-capture-thread");
        captureThread.setDaemon(true);
        captureThread.start();
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        if (captureThread != null) {
            try { captureThread.join(150); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            captureThread = null;
        }
        if (line != null) {
            line.stop();
            line.close();
            line = null;
        }
        latestSnapshot.set(PitchSnapshot.idle("Guitar input stopped"));
    }

    @Override
    public PitchSnapshot getLatestSnapshot() {
        return latestSnapshot.get();
    }

    @Override
    public List<LaneEvent> drainLaneEvents() {
        if (laneEventQueue.isEmpty()) return List.of();
        List<LaneEvent> events = new ArrayList<>();
        LaneEvent laneEvent;
        while ((laneEvent = laneEventQueue.poll()) != null) {
            events.add(laneEvent);
        }
        return events;
    }

    @Override
    public void dispose() {
        stop();
    }

    private void captureLoop() {
        byte[] buffer = new byte[BUFFER_BYTES];
        float[] samples = new float[BUFFER_SAMPLES];
        float[] centered = new float[BUFFER_SAMPLES];
        float[] correlations = new float[BUFFER_SAMPLES];

        while (running.get() && line != null) {
            int bytesRead = line.read(buffer, 0, buffer.length);
            if (bytesRead <= 0) continue;

            int sampleCount = bytesRead / 2;
            long bufferDurationNanos = Math.round((sampleCount / SAMPLE_RATE) * 1_000_000_000d);
            long bufferCaptureTimeNanos = System.nanoTime() - (bufferDurationNanos / 2L);

            for (int i = 0; i < sampleCount; i++) {
                int low = buffer[i * 2] & 0xFF;
                int high = buffer[i * 2 + 1];
                short pcm = (short) ((high << 8) | low);
                samples[i] = pcm / 32768f;
            }

            PitchSnapshot snapshot = analyze(samples, centered, correlations, sampleCount);
            latestSnapshot.set(snapshot);

            // edge detection runs here, in the capture thread, at buffer rate (~23ms)
            pushLaneEvent(snapshot, bufferCaptureTimeNanos);
        }
    }

    private void pushLaneEvent(PitchSnapshot snapshot, long captureTimeNanos) {
        if (!snapshot.hasReliablePitch()) {
            silenceFrames++;
            if (silenceFrames >= SILENCE_FRAMES_REQUIRED) {
                lastDetectedLane = null;
            }
            return;
        }

        silenceFrames = 0;
        LaneKey lane = mapFrequencyToLane(snapshot.frequencyHz());

        if (lane != null && lane != lastDetectedLane) {
            lastDetectedLane = lane;
            laneEventQueue.offer(new LaneEvent(lane, captureTimeNanos));
        } else if (lane == null) {
            lastDetectedLane = null;
        }
    }

    private LaneKey mapFrequencyToLane(float hz) {
        if (hz <= 0f) return null;

        LaneKey bestLane = null;
        float bestCentsDistance = Float.MAX_VALUE;

        float e2Distance = minCentsDistanceAcrossOctaves(hz, MIDI_E2);
        if (e2Distance < bestCentsDistance) {
            bestCentsDistance = e2Distance;
            bestLane = LaneKey.A;
        }

        float a2Distance = minCentsDistanceAcrossOctaves(hz, MIDI_A2);
        if (a2Distance < bestCentsDistance) {
            bestCentsDistance = a2Distance;
            bestLane = LaneKey.S;
        }

        float d3Distance = minCentsDistanceAcrossOctaves(hz, MIDI_D3);
        if (d3Distance < bestCentsDistance) {
            bestCentsDistance = d3Distance;
            bestLane = LaneKey.D;
        }

        float g3Distance = minCentsDistanceAcrossOctaves(hz, MIDI_G3);
        if (g3Distance < bestCentsDistance) {
            bestCentsDistance = g3Distance;
            bestLane = LaneKey.F;
        }

        if (bestLane == null) {
            return null;
        }

        float laneTolerance = laneToleranceInCents(bestLane);
        return bestCentsDistance <= laneTolerance ? bestLane : null;
    }

    private float laneToleranceInCents(LaneKey lane) {
        return switch (lane) {
            case A -> E2_CENTS_TOLERANCE;
            case S -> A2_CENTS_TOLERANCE;
            case D -> D3_CENTS_TOLERANCE;
            case F -> G3_CENTS_TOLERANCE;
        };
    }

    private float minCentsDistanceAcrossOctaves(float hz, int baseMidi) {
        float minDistance = Float.MAX_VALUE;
        for (int octaveShift = -12; octaveShift <= 12; octaveShift += 12) {
            float distance = Math.abs(centsDistanceToMidi(hz, baseMidi + octaveShift));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    private float centsDistanceToMidi(float hz, int midi) {
        float ref = (float) (A4_HZ * Math.pow(2.0, (midi - 69) / 12.0));
        return (float) (1200.0 * Math.log(hz / ref) / Math.log(2.0));
    }

    private PitchSnapshot analyze(float[] input, float[] centered, float[] correlations, int sampleCount) {
        if (sampleCount < 256) return PitchSnapshot.idle("Waiting for audio buffer...");

        float mean = 0f;
        for (int i = 0; i < sampleCount; i++) mean += input[i];
        mean /= sampleCount;

        float energy = 0f;
        for (int i = 0; i < sampleCount; i++) {
            centered[i] = input[i] - mean;
            energy += centered[i] * centered[i];
        }

        float rms = (float) Math.sqrt(energy / sampleCount);
        if (rms < MIN_RMS_LEVEL) return new PitchSnapshot(0f, 0f, rms, false, false, "Signal too low");

        int minLag = Math.max(2, Math.round(SAMPLE_RATE / MAX_FREQUENCY_HZ));
        int maxLag = Math.min(sampleCount - 2, Math.round(SAMPLE_RATE / MIN_FREQUENCY_HZ));
        if (maxLag <= minLag) return new PitchSnapshot(0f, 0f, rms, true, false, "Not enough samples");

        int bestLag = -1;
        float bestCorrelation = -1f;

        for (int lag = minLag; lag <= maxLag; lag++) {
            float sumXY = 0f, sumXX = 0f, sumYY = 0f;
            int size = sampleCount - lag;
            for (int i = 0; i < size; i++) {
                float x = centered[i], y = centered[i + lag];
                sumXY += x * y;
                sumXX += x * x;
                sumYY += y * y;
            }
            if (sumXX <= 0f || sumYY <= 0f) { correlations[lag] = 0f; continue; }
            float correlation = (float) (sumXY / Math.sqrt(sumXX * sumYY));
            correlations[lag] = correlation;
            if (correlation > bestCorrelation) { bestCorrelation = correlation; bestLag = lag; }
        }

        if (bestLag < 0) return new PitchSnapshot(0f, 0f, rms, true, false, "Pitch not detected");

        float refinedLag = bestLag;
        if (bestLag > minLag && bestLag < maxLag) {
            float left = correlations[bestLag - 1];
            float center = correlations[bestLag];
            float right = correlations[bestLag + 1];
            float denom = left - (2f * center) + right;
            if (Math.abs(denom) > 1e-5f) {
                float shift = 0.5f * (left - right) / denom;
                refinedLag = bestLag + Math.max(-1f, Math.min(1f, shift));
            }
        }

        float frequency = SAMPLE_RATE / refinedLag;
        boolean reliable = bestCorrelation >= MIN_CONFIDENCE;
        return new PitchSnapshot(frequency, bestCorrelation, rms, true, reliable,
                reliable ? "Pitch detected" : "Signal detected, stabilizing pitch");
    }
}
