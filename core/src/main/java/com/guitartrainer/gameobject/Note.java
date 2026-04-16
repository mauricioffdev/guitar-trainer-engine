package com.guitartrainer.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.guitartrainer.input.LaneKey;

public class Note extends GameObject {

    private final LaneKey laneKey;
    private NoteState state;
    private float targetTime;

    public Note(float x, float y, float width, float height, float speed, LaneKey laneKey) {
        this(x, y, width, height, speed, laneKey, 0f);
    }

    public Note(float x, float y, float width, float height, float speed, LaneKey laneKey, float targetTime) {
        super(x, y, width, height);
        this.laneKey = laneKey;
        this.velocityY = -speed;
        this.state = NoteState.ACTIVE;
        this.targetTime = targetTime;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void render(SpriteBatch batch, Texture pixelTexture) {
        if (state != NoteState.ACTIVE) return;

        Color previousColor = batch.getColor();

        batch.setColor(getLaneColor(laneKey));
        batch.draw(pixelTexture, x, y, width, height);

        batch.setColor(previousColor);
    }

    public LaneKey getLaneKey() {
        return laneKey;
    }

    public NoteState getState() {
        return state;
    }

    public void setState(NoteState state) {
        this.state = state;
    }

    public boolean isActive() {
        return state == NoteState.ACTIVE;
    }

    public float getCenterY() {
        return y + height / 2f;
    }

    public float getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(float targetTime) {
        this.targetTime = targetTime;
    }

    public float getSpawnTime() {
        return targetTime;
    }

    public void setSpawnTime(float spawnTime) {
        this.targetTime = spawnTime;
    }

    private Color getLaneColor(LaneKey key) {
        switch (key) {
            case A:
                return Color.SKY;
            case S:
                return Color.LIME;
            case D:
                return Color.ORANGE;
            case F:
                return Color.CORAL;
            default:
                return Color.WHITE;
        }
    }
}
