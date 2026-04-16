package com.guitartrainer.input;

import com.badlogic.gdx.Input;

public enum LaneKey {
    A(Input.Keys.A),
    S(Input.Keys.S),
    D(Input.Keys.D),
    F(Input.Keys.F);

    private final int inputKeyCode;

    LaneKey(int inputKeyCode) {
        this.inputKeyCode = inputKeyCode;
    }

    public int getInputKeyCode() {
        return inputKeyCode;
    }

    public static LaneKey fromKeyCode(int keyCode) {
        for (LaneKey laneKey : values()) {
            if (laneKey.inputKeyCode == keyCode) {
                return laneKey;
            }
        }
        return null;
    }
}
