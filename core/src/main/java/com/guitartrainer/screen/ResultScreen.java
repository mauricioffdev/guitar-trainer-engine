package com.guitartrainer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.guitartrainer.GameMain;
import com.guitartrainer.config.GameConfig;

public class ResultScreen extends ScreenAdapter {

    private final GameMain game;
    private final int score;
    private final int maxCombo;
    private final float runTimeSeconds;

    private SpriteBatch spriteBatch;
    private Texture pixelTexture;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    public ResultScreen(GameMain game, int score, int maxCombo, float runTimeSeconds) {
        this.game = game;
        this.score = score;
        this.maxCombo = maxCombo;
        this.runTimeSeconds = runTimeSeconds;
    }

    @Override
    public void show() {
        spriteBatch = game.getBatch();
        pixelTexture = createPixelTexture();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new MainGameScreen(game));
            dispose();
            return;
        }

        Gdx.gl.glClearColor(0.06f, 0.08f, 0.12f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();
        drawBackground();
        drawCenteredLine("RESULTADO", GameConfig.SCREEN_HEIGHT * 0.78f, 2.0f, Color.WHITE);
        drawCenteredLine("Score: " + score, GameConfig.SCREEN_HEIGHT * 0.60f, 1.4f, Color.GOLD);
        drawCenteredLine("Max Combo: " + maxCombo, GameConfig.SCREEN_HEIGHT * 0.52f, 1.2f, Color.SKY);
        drawCenteredLine("Tempo: " + (int) runTimeSeconds + "s", GameConfig.SCREEN_HEIGHT * 0.44f, 1.2f, Color.LIGHT_GRAY);
        drawCenteredLine("ENTER ou SPACE para jogar novamente", GameConfig.SCREEN_HEIGHT * 0.24f, 1.0f, Color.WHITE);
        spriteBatch.end();
    }

    private void drawBackground() {
        Color previous = spriteBatch.getColor();

        spriteBatch.setColor(0.10f, 0.13f, 0.18f, 1f);
        spriteBatch.draw(pixelTexture, 0f, 0f, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        spriteBatch.setColor(0.16f, 0.21f, 0.30f, 1f);
        spriteBatch.draw(pixelTexture, 0f, 0f, GameConfig.SCREEN_WIDTH, 12f);
        spriteBatch.draw(pixelTexture, 0f, GameConfig.SCREEN_HEIGHT - 12f, GameConfig.SCREEN_WIDTH, 12f);

        spriteBatch.setColor(previous);
    }

    private void drawCenteredLine(String text, float y, float scale, Color color) {
        font.getData().setScale(scale);
        font.setColor(color);
        glyphLayout.setText(font, text);
        float x = (GameConfig.SCREEN_WIDTH - glyphLayout.width) * 0.5f;
        font.draw(spriteBatch, text, x, y);
        font.getData().setScale(1f);
    }

    private Texture createPixelTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    @Override
    public void dispose() {
        pixelTexture.dispose();
        font.dispose();
    }
}
