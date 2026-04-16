package com.guitartrainer.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.guitartrainer.config.GameConfig;
import com.guitartrainer.input.LaneKey;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class Player extends GameObject {

    private static final float FLASH_DURATION = 0.12f;

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
                if (current < 0f) current = 0f;
                laneFlashTimers.put(laneKey, current);
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

            batch.setColor(getLaneColor(laneKey, flashTimer));

            batch.draw(
                    pixelTexture,
                    laneX,
                    GameConfig.HIT_LINE_Y - 6f,
                    GameConfig.LANE_WIDTH,
                    14f
            );
        }

        batch.setColor(previous);
    }

    private Color getLaneColor(LaneKey laneKey, float flashTimer) {

        if (flashTimer > 0f) {
            return Color.GOLD;
        }

        switch (laneKey) {
            case A:
                return new Color(0.3f, 0.5f, 0.8f, 1f);
            case S:
                return new Color(0.3f, 0.8f, 0.3f, 1f);
            case D:
                return new Color(0.9f, 0.5f, 0.2f, 1f);
            case F:
                return new Color(0.9f, 0.3f, 0.3f, 1f);
            default:
                return Color.DARK_GRAY;
        }
    }

    public float laneToX(LaneKey laneKey) {
        return GameConfig.LANES_START_X +
                laneKey.ordinal() * (GameConfig.LANE_WIDTH + GameConfig.LANE_GAP);
    }
}