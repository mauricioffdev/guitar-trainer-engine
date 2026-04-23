package com.guitartrainer.audio;

import com.guitartrainer.input.LaneKey;

import java.util.List;

public interface GuitarInputService {

    record LaneEvent(LaneKey laneKey, long captureTimeNanos) {
    }

    void start();

    void stop();

    PitchSnapshot getLatestSnapshot();

    /** Returns all lane events detected since the last call, then clears the queue. */
    List<LaneEvent> drainLaneEvents();

    void dispose();
}
