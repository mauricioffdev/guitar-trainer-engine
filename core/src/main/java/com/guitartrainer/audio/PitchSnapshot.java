package com.guitartrainer.audio;

public record PitchSnapshot(
        float frequencyHz,
        float confidence,
        float signalLevel,
        boolean hasSignal,
        boolean hasReliablePitch,
        String statusMessage
) {
    public static PitchSnapshot idle(String statusMessage) {
        return new PitchSnapshot(0f, 0f, 0f, false, false, statusMessage);
    }
}
