package com.guitartrainer.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.guitartrainer.config.GameConfig;
import com.guitartrainer.input.LaneKey;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class Player extends GameObject {

    private static final float FLASH_DURATION = 0.15f;
    private static final BitmapFont LABEL_FONT = new BitmapFont();

    static {
        LABEL_FONT.getData().setScale(0.9f);
    }

    private final Map<LaneKey, Float> laneFlashTimers;

    public Player() {
        super(0f, 0f, 0f, 0f);
        this.laneFlashTimers = new EnumMap<>(LaneKey.class);
        for (LaneKey laneKey : LaneKey.values()) {
            laneFlashTimers.put(laneKey, 0f);
        }
    }

    @Override
    public void update(float deltaTime) {
        for (LaneKey laneKey : LaneKey.values()) {
            float current = laneFlashTimers.get(laneKey);
            if (current > 0f) {
                current -= deltaTime;
                laneFlashTimers.put(laneKey, Math.max(0f, current));
            }
        }
    }

    public void onInput(Set<LaneKey> pressedLanes) {
        for (LaneKey laneKey : pressedLanes) {
            laneFlashTimers.put(laneKey, FLASH_DURATION);
        }
    }

    @Override
    public void render(SpriteBatch batch, Texture pixelTexture) {
        Color previous = batch.getColor();

        for (LaneKey laneKey : LaneKey.values()) {
            float laneX = laneToX(laneKey);
            float flashTimer = laneFlashTimers.get(laneKey);
            boolean flashing = flashTimer > 0f;

            // button body
            batch.setColor(getLaneColor(laneKey, flashing));
            batch.draw(pixelTexture, laneX, GameConfig.HIT_LINE_Y - 20f, GameConfig.LANE_WIDTH, 22f);

            // string name label
            LABEL_FONT.setColor(flashing ? Color.BLACK : Color.WHITE);
            String label = laneToStringName(laneKey);
            LABEL_FONT.draw(batch, label, laneX + GameConfig.LANE_WIDTH * 0.5f - 12f, GameConfig.HIT_LINE_Y - 4f);
        }

        batch.setColor(previous);
    }

    private String laneToStringName(LaneKey key) {
        switch (key) {
            case A: return "E2";
            case S: return "A2";
            case D: return "D3";
            case F: return "G3";
            default: return "?";
        }
    }

    private Color getLaneColor(LaneKey laneKey, boolean flashing) {
        if (flashing) return Color.GOLD;
        switch (laneKey) {
            case A: return new Color(0.2f, 0.5f, 0.85f, 1f);
            case S: return new Color(0.2f, 0.75f, 0.25f, 1f);
            case D: return new Color(0.9f, 0.5f, 0.15f, 1f);
            case F: return new Color(0.85f, 0.25f, 0.25f, 1f);
            default: return Color.DARK_GRAY;
        }
    }

    public float laneToX(LaneKey laneKey) {
        return GameConfig.LANES_START_X +
                laneKey.ordinal() * (GameConfig.LANE_WIDTH + GameConfig.LANE_GAP);
    }
}