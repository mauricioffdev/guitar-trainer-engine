package com.guitartrainer.gameobject;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HitEffect {

    private static final float DURATION_SECONDS = 0.3f;
    private static final float START_SIZE = 32f;
    private static final float PEAK_SIZE = 80f;

    private final float x;
    private final float y;
    private final Color color;
    private float timer;

    public HitEffect(float x, float y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.timer = DURATION_SECONDS;
    }

    public boolean update(float deltaTime) {
        timer -= deltaTime;
        return timer <= 0f;
    }

    public void render(SpriteBatch batch, Texture texture) {

        if (timer <= 0f) return;

        float t = 1f - (timer / DURATION_SECONDS); // 0 → 1

        // 🔥 easing (ease-out)
        float eased = easeOutCubic(t);

        // 🔥 escala com "pop"
        float size = lerp(START_SIZE, PEAK_SIZE, eased);

        // 🔥 alpha com fade mais suave
        float alpha = 1f - (t * t);

        Color previous = batch.getColor();

        batch.setColor(color.r, color.g, color.b, alpha);

        batch.draw(
                texture,
                x - size / 2f,
                y - size / 2f,
                size,
                size
        );

        batch.setColor(previous);
    }

    // =========================
    // 🔧 UTILS
    // =========================

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float easeOutCubic(float t) {
        return 1f - (float) Math.pow(1f - t, 3);
    }
}