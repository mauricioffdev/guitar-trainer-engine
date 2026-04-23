package com.guitartrainer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.guitartrainer.audio.GuitarInputService;
import com.guitartrainer.audio.NoopGuitarInputService;
import com.guitartrainer.screen.MainGameScreen;

public class GameMain extends Game {

    private SpriteBatch batch;
    private final GuitarInputService guitarInputService;

    public GameMain() {
        this(new NoopGuitarInputService());
    }

    public GameMain(GuitarInputService guitarInputService) {
        this.guitarInputService = guitarInputService == null
                ? new NoopGuitarInputService()
                : guitarInputService;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        setScreen(new MainGameScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public GuitarInputService getGuitarInputService() {
        return guitarInputService;
    }

    @Override
    public void dispose() {
        guitarInputService.dispose();
        batch.dispose();
        super.dispose();
    }
}
