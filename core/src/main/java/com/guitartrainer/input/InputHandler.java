package com.guitartrainer.input;

import com.badlogic.gdx.InputAdapter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class InputHandler extends InputAdapter {
    private final Set<LaneKey> pressedLanes = EnumSet.noneOf(LaneKey.class);
    private boolean anyKeyPressed;

    @Override
    public boolean keyDown(int keycode) {
        anyKeyPressed = true;

        LaneKey laneKey = LaneKey.fromKeyCode(keycode);
        if (laneKey == null) {
            return false;
        }

        pressedLanes.add(laneKey);
        return true;
    }

    public Set<LaneKey> consumePressedLanes() {
        if (pressedLanes.isEmpty()) {
            return Collections.emptySet();
        }

        Set<LaneKey> snapshot = EnumSet.copyOf(pressedLanes);
        pressedLanes.clear();
        return Collections.unmodifiableSet(snapshot);
    }

    public boolean consumeAnyKeyPressed() {
        boolean wasPressed = anyKeyPressed;
        anyKeyPressed = false;
        return wasPressed;
    }
}
