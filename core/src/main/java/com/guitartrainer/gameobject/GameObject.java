package com.guitartrainer.gameobject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float velocityX;
    protected float velocityY;

    protected GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void update(float deltaTime) {
        x += velocityX * deltaTime;
        y += velocityY * deltaTime;
    }

    public abstract void render(SpriteBatch batch, Texture pixelTexture);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public float getCenterY() {
        return y + (height * 0.5f);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
