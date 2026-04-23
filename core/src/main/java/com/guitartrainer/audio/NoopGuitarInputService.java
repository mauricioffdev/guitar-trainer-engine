package com.guitartrainer.audio;

import java.util.Collections;
import java.util.List;

public class NoopGuitarInputService implements GuitarInputService {

    private static final PitchSnapshot SNAPSHOT = PitchSnapshot.idle("No guitar input backend");

    @Override public void start() {}
    @Override public void stop() {}
    @Override public void dispose() {}

    @Override
    public PitchSnapshot getLatestSnapshot() {
        return SNAPSHOT;
    }

    @Override
    public List<LaneEvent> drainLaneEvents() {
        return Collections.emptyList();
    }
}
