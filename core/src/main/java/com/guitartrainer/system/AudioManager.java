package com.guitartrainer.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.audio.Music;

public class AudioManager {

    private Music music;

    private boolean isPlaying;
    private float startPositionOffset;

    public void load(String path) {

        dispose();

        if (path == null || path.isBlank()) {
            return;
        }

        FileHandle audioFile = resolveAudioFile(path);
        if (audioFile == null) {
            return;
        }

        try {
            music = Gdx.audio.newMusic(audioFile);
            music.setOnCompletionListener(completedMusic -> isPlaying = false);
        } catch (RuntimeException exception) {
            music = null;
            return;
        }

        isPlaying = false;
        startPositionOffset = 0f;
    }

    private FileHandle resolveAudioFile(String path) {
        FileHandle internal = Gdx.files.internal(path);
        if (internal.exists()) {
            return internal;
        }

        FileHandle classpath = Gdx.files.classpath(path);
        if (classpath.exists()) {
            return classpath;
        }

        FileHandle local = Gdx.files.local(path);
        if (local.exists()) {
            return local;
        }

        FileHandle absolute = Gdx.files.absolute(path);
        if (absolute.exists()) {
            return absolute;
        }

        return null;
    }

    public void play() {

        if (music == null) return;

        music.stop(); // 🔥 garante reset limpo
        music.play();

        startPositionOffset = music.getPosition();
        isPlaying = true;
    }

    public void pause() {

        if (music == null) return;

        music.pause();
        isPlaying = false;
    }

    public void stop() {

        if (music == null) return;

        music.stop();
        isPlaying = false;
    }

    public void restart() {

        if (music == null) return;

        music.stop();
        music.setPosition(0f);
        music.play();

        startPositionOffset = music.getPosition();
        isPlaying = true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isLoaded() {
        return music != null;
    }

    public float getPosition() {

        if (music == null || !isPlaying) {
            return 0f;
        }

        // 🔥 tempo relativo ao início real
        return music.getPosition() - startPositionOffset;
    }

    public void setLooping(boolean looping) {
        if (music != null) {
            music.setLooping(looping);
        }
    }

    public void setVolume(float volume) {
        if (music != null) {
            music.setVolume(Math.max(0f, Math.min(1f, volume)));
        }
    }

    public void dispose() {

        if (music != null) {
            music.dispose();
            music = null;
        }

        isPlaying = false;
        startPositionOffset = 0f;
    }
}
