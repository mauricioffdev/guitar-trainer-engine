package com.guitartrainer.system;

import com.guitartrainer.input.LaneKey;

import java.util.ArrayList;
import java.util.List;

public class NoteMapLoader {

    public List<NoteSpawner.NoteEvent> loadTestMap() {
        List<NoteSpawner.NoteEvent> events = new ArrayList<>();

        // Mapa hardcoded para testes locais.
        events.add(new NoteSpawner.NoteEvent(1.0f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(1.5f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(2.0f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(2.5f, LaneKey.F));

        events.add(new NoteSpawner.NoteEvent(3.2f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(3.6f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(4.0f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(4.4f, LaneKey.F));

        events.add(new NoteSpawner.NoteEvent(5.1f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(5.35f, LaneKey.S));
        events.add(new NoteSpawner.NoteEvent(5.6f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(5.85f, LaneKey.F));

        events.add(new NoteSpawner.NoteEvent(6.5f, LaneKey.D));
        events.add(new NoteSpawner.NoteEvent(6.9f, LaneKey.A));
        events.add(new NoteSpawner.NoteEvent(7.2f, LaneKey.F));
        events.add(new NoteSpawner.NoteEvent(7.6f, LaneKey.S));

        return events;
    }
}
