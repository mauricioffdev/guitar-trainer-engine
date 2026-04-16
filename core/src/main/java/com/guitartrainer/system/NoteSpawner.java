package com.guitartrainer.system;

import com.badlogic.gdx.math.MathUtils;
import com.guitartrainer.config.GameConfig;
import com.guitartrainer.gameobject.Note;
import com.guitartrainer.input.LaneKey;

import java.util.ArrayList;
import java.util.List;

public class NoteSpawner {

    private static final float BASE_SPAWN_INTERVAL = 0.95f;
    private static final float MIN_SPAWN_INTERVAL = 0.35f;

    private float spawnTimer;

    private final List<NoteEvent> noteEvents = new ArrayList<>();
    private int nextEventIndex = 0;

    private boolean useMap = false;

    public void update(float deltaTime, float elapsedTime, List<Note> notes) {

        if (useMap) {
            updateFromMap(elapsedTime, notes);
        } else {
            updateRandom(deltaTime, elapsedTime, notes);
        }
    }

    private void updateRandom(float deltaTime, float elapsedTime, List<Note> notes) {

        spawnTimer += deltaTime;

        float currentSpawnInterval = Math.max(
                MIN_SPAWN_INTERVAL,
                BASE_SPAWN_INTERVAL - elapsedTime * 0.02f
        );

        if (spawnTimer < currentSpawnInterval) {
            return;
        }

        spawnTimer = 0f;

        LaneKey randomLane = LaneKey.values()[
                MathUtils.random(0, LaneKey.values().length - 1)
                ];

        spawnRandomNote(notes, randomLane, elapsedTime);
    }

    private void updateFromMap(float elapsedTime, List<Note> notes) {

        while (nextEventIndex < noteEvents.size()) {

            NoteEvent event = noteEvents.get(nextEventIndex);

            float spawnAt = event.time - GameConfig.NOTE_TRAVEL_TIME_SECONDS;

            if (spawnAt > elapsedTime) {
                break;
            }

            spawnNote(notes, event.laneKey, event.time);
            nextEventIndex++;
        }
    }

    private void spawnRandomNote(List<Note> notes, LaneKey laneKey, float elapsedTime) {
        float targetTime = elapsedTime + GameConfig.NOTE_TRAVEL_TIME_SECONDS;
        spawnNote(notes, laneKey, targetTime);
    }

    private void spawnNote(List<Note> notes, LaneKey laneKey, float targetTime) {

        float laneX = GameConfig.LANES_START_X +
                laneKey.ordinal() * (GameConfig.LANE_WIDTH + GameConfig.LANE_GAP);

        float noteSpeed = calculateNoteSpeed();

        Note note = new Note(
                laneX,
                GameConfig.SCREEN_HEIGHT + GameConfig.NOTE_HEIGHT,
                GameConfig.LANE_WIDTH,
                GameConfig.NOTE_HEIGHT,
                noteSpeed,
                laneKey,
                targetTime
        );

        note.setTargetTime(targetTime);
        notes.add(note);
    }

    private float calculateNoteSpeed() {
        float spawnY = GameConfig.SCREEN_HEIGHT + GameConfig.NOTE_HEIGHT;
        float distanceToHitLine = spawnY - GameConfig.HIT_LINE_Y;
        return distanceToHitLine / GameConfig.NOTE_TRAVEL_TIME_SECONDS;
    }

    public void setNoteMap(List<NoteEvent> events) {
        noteEvents.clear();
        noteEvents.addAll(events);
        nextEventIndex = 0;
        useMap = true;
    }

    public void useRandomMode() {
        useMap = false;
        spawnTimer = 0f;
    }

    public boolean isMapFinished() {
        return useMap && nextEventIndex >= noteEvents.size();
    }

    public void resetMap() {
        nextEventIndex = 0;
        spawnTimer = 0f;
    }

    public static class NoteEvent {
        public final float time;
        public final LaneKey laneKey;

        public NoteEvent(float time, LaneKey laneKey) {
            this.time = time;
            this.laneKey = laneKey;
        }
    }
}
