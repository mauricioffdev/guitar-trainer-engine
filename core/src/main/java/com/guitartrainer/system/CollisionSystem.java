package com.guitartrainer.system;

import com.guitartrainer.gameobject.Note;
import com.guitartrainer.gameobject.NoteState;
import com.guitartrainer.input.LaneKey;

import java.util.List;
import java.util.Set;

public class CollisionSystem {

    private static final float PERFECT_WINDOW_SECONDS = 0.08f;
    private static final float OK_WINDOW_SECONDS = 0.18f;

    public interface HitListener {
        void onHit(Note note, HitResult result);
    }

    public enum HitResult {
        PERFECT,
        OK,
        MISS
    }

    public void processHits(
            List<Note> notes,
            Set<LaneKey> pressedLanes,
            float elapsedTime,
            HitListener listener
    ) {

        // =========================
        // 🎯 INPUT (PLAYER)
        // =========================
        for (LaneKey lane : pressedLanes) {

            Note candidate = findClosestMatchingNote(notes, lane, elapsedTime);

            if (candidate == null) {
                listener.onHit(null, HitResult.MISS);
                continue;
            }

            if (candidate.getState() != NoteState.ACTIVE) {
                continue;
            }

            float timeDelta = Math.abs(candidate.getTargetTime() - elapsedTime);

            if (timeDelta <= PERFECT_WINDOW_SECONDS) {
                candidate.setState(NoteState.HIT);
                listener.onHit(candidate, HitResult.PERFECT);

            } else if (timeDelta <= OK_WINDOW_SECONDS) {
                candidate.setState(NoteState.HIT);
                listener.onHit(candidate, HitResult.OK);

            } else {
                listener.onHit(null, HitResult.MISS);
            }
        }

        // =========================
        // ❌ MISS AUTOMÁTICO
        // =========================
        for (Note note : notes) {

            if (note.getState() != NoteState.ACTIVE) continue;

            if (elapsedTime - note.getTargetTime() > OK_WINDOW_SECONDS) {
                note.setState(NoteState.MISSED);
                listener.onHit(note, HitResult.MISS);
            }
        }
    }

    private Note findClosestMatchingNote(
            List<Note> notes,
            LaneKey lane,
            float elapsedTime
    ) {

        Note closest = null;
        float closestDistance = Float.MAX_VALUE;

        for (Note note : notes) {

            if (note.getState() != NoteState.ACTIVE ||
                    note.getLaneKey() != lane) {
                continue;
            }

            float distance = Math.abs(note.getTargetTime() - elapsedTime);

            if (distance > OK_WINDOW_SECONDS) {
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
