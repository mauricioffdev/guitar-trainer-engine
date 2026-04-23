package com.guitartrainer.input;

/**
 * Maps a detected frequency to a LaneKey using E standard open strings.
 *
 * Lane mapping:
 *   A → E2  (~82.4 Hz)
 *   S → A2  (~110.0 Hz)
 *   D → D3  (~146.8 Hz)
 *   F → G3  (~196.0 Hz)
 *
 * Fires a lane only on the leading edge of a new note detection
 * (i.e. when the detected lane changes), to avoid holding a lane
 * pressed every frame.
 */
public class GuitarNoteMapper {

    private static final float A4_HZ = 440f;

    private static final int MIDI_E2 = 40;
    private static final int MIDI_A2 = 45;
    private static final int MIDI_D3 = 50;
    private static final int MIDI_G3 = 55;

    /** Tolerance: ±50 cents (half a semitone). */
    private static final float CENTS_TOLERANCE = 50f;

    /** Frames of silence required before the edge resets (prevents flicker re-fires). */
    private static final int SILENCE_FRAMES_REQUIRED = 3;

    private LaneKey lastLane = null;
    private int silenceFrames = 0;

    /**
     * Returns the mapped LaneKey only on the leading edge of a new note detection.
     * Requires SILENCE_FRAMES_REQUIRED consecutive null frames before the edge resets,
     * so brief pitch flicker doesn't cause double-fires.
     */
    public LaneKey poll(float frequencyHz) {
        LaneKey current = map(frequencyHz);

        if (current != null) {
            silenceFrames = 0;
            if (current != lastLane) {
                lastLane = current;
                return current;
            }
            return null;
        }

        // null frame — only reset after sustained silence
        silenceFrames++;
        if (silenceFrames >= SILENCE_FRAMES_REQUIRED) {
            lastLane = null;
        }
        return null;
    }

    private LaneKey map(float frequencyHz) {
        if (frequencyHz <= 0f) return null;

        int midi = frequencyToMidi(frequencyHz);
        float cents = centsDiff(frequencyHz, midiToFrequency(midi));

        if (Math.abs(cents) > CENTS_TOLERANCE) return null;

        return switch (midi) {
            case MIDI_E2 -> LaneKey.A;
            case MIDI_A2 -> LaneKey.S;
            case MIDI_D3 -> LaneKey.D;
            case MIDI_G3 -> LaneKey.F;
            default -> null;
        };
    }

    private int frequencyToMidi(float hz) {
        return Math.round(69f + 12f * (float) (Math.log(hz / A4_HZ) / Math.log(2.0)));
    }

    private float midiToFrequency(int midi) {
        return (float) (A4_HZ * Math.pow(2.0, (midi - 69) / 12.0));
    }

    private float centsDiff(float hz, float ref) {
        return (float) (1200.0 * Math.log(hz / ref) / Math.log(2.0));
    }
}
