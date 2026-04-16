package com.guitartrainer;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.guitartrainer.config.GameConfig;

public class DesktopLauncher {

    public static void main(String[] args) {

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("Guitar Trainer - MVP");
        config.setWindowedMode(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new GameMain(), config);
    }
}