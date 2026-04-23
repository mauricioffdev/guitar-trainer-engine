package com.guitartrainer.system;

import com.guitartrainer.config.GameConfig;
import com.guitartrainer.gameobject.Note;
import com.guitartrainer.gameobject.NoteState;
import com.guitartrainer.input.LaneKey;

import java.util.List;

public class CollisionSystem {

    private static final float PERFECT_WINDOW_SECONDS = 0.07f;
    private static final float OK_WINDOW_SECONDS = 0.14f;
    private static final float GUITAR_WINDOW_BONUS_SECONDS = 0.08f;
    private static final float MAX_INPUT_OK_WINDOW_SECONDS = OK_WINDOW_SECONDS + GUITAR_WINDOW_BONUS_SECONDS;

    public interface HitListener {
        void onHit(Note note, HitResult result);
    }

    public enum HitResult {
        PERFECT,
        OK,
        MISS
    }

    public void processInputEvent(
            List<Note> notes,
            LaneKey lane,
            float eventTimeSeconds,
            boolean fromGuitar,
            HitListener listener
    ) {
        if (lane == null) {
            return;
        }

        float adjustedTime = fromGuitar
                ? eventTimeSeconds - GameConfig.GUITAR_INPUT_OFFSET_SECONDS
                : eventTimeSeconds;

        float perfectWindow = fromGuitar
                ? PERFECT_WINDOW_SECONDS + (GUITAR_WINDOW_BONUS_SECONDS * 0.5f)
                : PERFECT_WINDOW_SECONDS;
        float okWindow = fromGuitar
                ? OK_WINDOW_SECONDS + GUITAR_WINDOW_BONUS_SECONDS
                : OK_WINDOW_SECONDS;

        Note candidate = findClosestMatchingNote(notes, lane, adjustedTime, okWindow);

        if (candidate == null) {
            if (!fromGuitar) {
                listener.onHit(null, HitResult.MISS);
            }
            return;
        }

        if (candidate.getState() != NoteState.ACTIVE) {
            return;
        }

        float timeDelta = Math.abs(candidate.getTargetTime() - adjustedTime);

        if (timeDelta <= perfectWindow) {
            candidate.setState(NoteState.HIT);
            listener.onHit(candidate, HitResult.PERFECT);
        } else if (timeDelta <= okWindow) {
            candidate.setState(NoteState.HIT);
            listener.onHit(candidate, HitResult.OK);
        } else if (!fromGuitar) {
            listener.onHit(null, HitResult.MISS);
        }
    }

    public void processAutoMisses(
            List<Note> notes,
            float elapsedTimeSeconds,
            HitListener listener
    ) {
        // =========================
        // ❌ MISS AUTOMÁTICO
        // =========================
        for (Note note : notes) {

            if (note.getState() != NoteState.ACTIVE) continue;

            if (elapsedTimeSeconds - note.getTargetTime() > MAX_INPUT_OK_WINDOW_SECONDS) {
                note.setState(NoteState.MISSED);
                listener.onHit(note, HitResult.MISS);
            }
        }
    }

    private Note findClosestMatchingNote(
            List<Note> notes,
            LaneKey lane,
            float adjustedTime,
            float hitWindowSeconds
    ) {

        Note closest = null;
        float closestDistance = Float.MAX_VALUE;

        for (Note note : notes) {

            if (note.getState() != NoteState.ACTIVE ||
                    note.getLaneKey() != lane) {
                continue;
            }

            float distance = Math.abs(note.getTargetTime() - adjustedTime);

            if (distance > hitWindowSeconds) {
                continue;
            }

            if (distance < closestDistance) {
                closest = note;
                closestDistance = distance;
            }
        }

        return closest;
    }
}
