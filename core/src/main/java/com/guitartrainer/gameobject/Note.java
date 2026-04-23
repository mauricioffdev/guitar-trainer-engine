package com.guitartrainer.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.guitartrainer.input.LaneKey;

public class Note extends GameObject {

    private static final BitmapFont LABEL_FONT = new BitmapFont();

    static {
        LABEL_FONT.getData().setScale(0.85f);
    }

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

        Color laneColor = getLaneColor(laneKey);
        Color previousColor = batch.getColor();

        // body
        batch.setColor(laneColor);
        batch.draw(pixelTexture, x, y, width, height);

        // bright top edge
        batch.setColor(laneColor.r + 0.3f, laneColor.g + 0.3f, laneColor.b + 0.3f, 1f);
        batch.draw(pixelTexture, x, y + height - 3f, width, 3f);

        // string label centered on note
        LABEL_FONT.setColor(Color.WHITE);
        String label = laneToStringName(laneKey);
        LABEL_FONT.draw(batch, label, x + width * 0.5f - 10f, y + height * 0.5f + 6f);

        batch.setColor(previousColor);
    }

    public LaneKey getLaneKey() { return laneKey; }
    public NoteState getState() { return state; }
    public void setState(NoteState state) { this.state = state; }
    public boolean isActive() { return state == NoteState.ACTIVE; }
    public float getCenterY() { return y + height / 2f; }
    public float getTargetTime() { return targetTime; }
    public void setTargetTime(float targetTime) { this.targetTime = targetTime; }
    public float getSpawnTime() { return targetTime; }
    public void setSpawnTime(float spawnTime) { this.targetTime = spawnTime; }

    private String laneToStringName(LaneKey key) {
        switch (key) {
            case A: return "E2";
            case S: return "A2";
            case D: return "D3";
            case F: return "G3";
            default: return "?";
        }
    }

    private Color getLaneColor(LaneKey key) {
        switch (key) {
            case A: return new Color(0.2f, 0.6f, 1.0f, 1f);
            case S: return new Color(0.2f, 0.85f, 0.3f, 1f);
            case D: return new Color(1.0f, 0.55f, 0.1f, 1f);
            case F: return new Color(1.0f, 0.25f, 0.25f, 1f);
            default: return Color.WHITE;
        }
    }
}
