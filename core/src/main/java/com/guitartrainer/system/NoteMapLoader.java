package com.guitartrainer.system;

import com.guitartrainer.input.LaneKey;

import java.util.ArrayList;
import java.util.List;

public class NoteMapLoader {

    /**
     * E standard open strings mapped to lanes:
     *   A = E2, S = A2, D = D3, F = G3
     *
     * Pattern gives the player time to practice each string
     * individually, then in combinations.
     */
    public List<NoteSpawner.NoteEvent> loadTestMap() {
        List<NoteSpawner.NoteEvent> events = new ArrayList<>();

        // Warm-up: one of each string
        events.add(new NoteSpawner.NoteEvent(1.5f,  LaneKey.A)); // E2
        events.add(new NoteSpawner.NoteEvent(2.5f,  LaneKey.S)); // A2
        events.add(new NoteSpawner.NoteEvent(3.5f,  LaneKey.D)); // D3
        events.add(new NoteSpawner.NoteEvent(4.5f,  LaneKey.F)); // G3

        // Ascending run
        events.add(new NoteSpawner.NoteEvent(6.0f,  LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(6.6f,  LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(7.2f,  LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(7.8f,  LaneKey.F));

        // Descending run
        events.add(new NoteSpawner.NoteEvent(9.0f,  LaneKey.F));
        events.add(new NoteSpawner.NoteEvent(9.6f,  LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(10.2f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(10.8f, LaneKey.A));

        // Alternating pairs
        events.add(new NoteSpawner.NoteEvent(12.0f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(12.5f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(13.0f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(13.5f, LaneKey.F));
        events.add(new NoteSpawner.NoteEvent(14.0f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(14.5f, LaneKey.D));

        // Fast run
        events.add(new NoteSpawner.NoteEvent(16.0f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(16.4f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(16.8f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(17.2f, LaneKey.F));
        events.add(new NoteSpawner.NoteEvent(17.6f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(18.0f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(18.4f, LaneKey.A));

        return events;
    }
}
