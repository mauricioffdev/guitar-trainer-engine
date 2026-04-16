package com.guitartrainer.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
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
import com.guitartrainer.gameobject.HitEffect;
import com.guitartrainer.gameobject.Note;
import com.guitartrainer.gameobject.Player;
import com.guitartrainer.input.InputHandler;
import com.guitartrainer.input.LaneKey;
import com.guitartrainer.system.AudioManager;
import com.guitartrainer.system.CollisionSystem;
import com.guitartrainer.system.NoteMapLoader;
import com.guitartrainer.system.NoteSpawner;
import com.guitartrainer.system.ScoreSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainGameScreen extends ScreenAdapter {

    private static final float HIT_FEEDBACK_DURATION = 0.5f;
    private static final float MAX_RUN_DURATION_SECONDS = 30f;
    private static final String AUDIO_DIR = "audio";

    private static final float SCORE_SCALE = 1.0f;
    private static final float COMBO_SCALE = 2.2f;
    private static final float FEEDBACK_BASE_SCALE = 1.4f;
    private static final float FEEDBACK_SCALE_FACTOR = 1.5f;

    private final GameMain game;
    private SpriteBatch spriteBatch;

    private Texture pixelTexture;
    private BitmapFont font;
    private GlyphLayout glyphLayout;

    private Player player;
    private InputHandler inputHandler;
    private NoteSpawner noteSpawner;
    private CollisionSystem collisionSystem;
    private ScoreSystem scoreSystem;
    private AudioManager audioManager;

    private List<Note> notes;
    private List<HitEffect> hitEffects;

    private float elapsedTime;
    private float fallbackElapsedTime;
    private float lastAudioPosition;
    private boolean useMusicClock;
    private float runDurationSeconds;
    private float mapStartTime;

    private GameState gameState;

    private String hitFeedbackText;
    private float hitFeedbackTimer;

    public MainGameScreen(GameMain game) {
        this.game = game;
    }

    @Override
    public void show() {

        spriteBatch = game.getBatch();

        pixelTexture = createPixelTexture();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();

        player = new Player();
        inputHandler = new InputHandler();
        noteSpawner = new NoteSpawner();
        collisionSystem = new CollisionSystem();
        scoreSystem = new ScoreSystem();

        audioManager = new AudioManager();
        audioManager.load(resolveTrackPath());

        NoteMapLoader loader = new NoteMapLoader();
        noteSpawner.setNoteMap(loader.loadTestMap());

        notes = new ArrayList<>();
        hitEffects = new ArrayList<>();

        elapsedTime = 0f;
        fallbackElapsedTime = 0f;
        lastAudioPosition = 0f;
        useMusicClock = false;
        runDurationSeconds = resolveRunDurationSeconds();
        mapStartTime = 0f;

        gameState = GameState.READY;

        hitFeedbackText = "";
        hitFeedbackTimer = 0f;

        Gdx.input.setInputProcessor(inputHandler);
    }

    @Override
    public void render(float delta) {

        update(delta);

        Gdx.gl.glClearColor(0.08f, 0.1f, 0.14f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.begin();

        drawBackground();
        drawHitLine();

        player.render(spriteBatch, pixelTexture);

        for (Note note : notes) {
            if (note.isActive()) {
                note.render(spriteBatch, pixelTexture);
            }
        }

        for (HitEffect effect : hitEffects) {
            effect.render(spriteBatch, pixelTexture);
        }

        drawHud();
        drawStateLabel();
        drawHitFeedback();

        spriteBatch.end();
    }

    private void update(float deltaTime) {

        handleGlobalControls();

        updateFeedbackTimer(deltaTime);

        // =========================
        // READY → START
        // =========================
        if (gameState == GameState.READY) {
            if (inputHandler.consumeAnyKeyPressed()) {
                gameState = GameState.PLAYING;
                startRun();
            }
            return;
        }

        // =========================
        // GAME FINISHED
        // =========================
        if (gameState == GameState.FINISHED) {
            return;
        }

        // =========================
        // GAME LOOP
        // =========================

        elapsedTime = resolveElapsedTime(deltaTime);

        if (elapsedTime >= runDurationSeconds) {
            finishRunAndShowResult();
            return;
        }

        float mapElapsedTime = Math.max(0f, elapsedTime - mapStartTime);
        noteSpawner.update(deltaTime, mapElapsedTime, notes);

        for (Note note : notes) {
            if (note.isActive()) {
                note.update(deltaTime);
            }
        }

        Set<LaneKey> pressedLanes = inputHandler.consumePressedLanes();

        player.onInput(pressedLanes);
        player.update(deltaTime);

        collisionSystem.processHits(
                notes,
                pressedLanes,
                mapElapsedTime,
                (note, result) -> {

                    switch (result) {
                        case PERFECT -> {
                            scoreSystem.register(ScoreSystem.HitGrade.PERFECT);
                            showHitFeedback("PERFECT");
                            spawnHitEffect(note);
                        }
                        case OK -> {
                            scoreSystem.register(ScoreSystem.HitGrade.OK);
                            showHitFeedback("OK");
                            spawnHitEffect(note);
                        }
                        case MISS -> {
                            scoreSystem.register(ScoreSystem.HitGrade.MISS);
                            showHitFeedback("MISS");
                        }
                    }
                }
        );

        removeResolvedNotes();
        updateHitEffects(deltaTime);

        if (noteSpawner.isMapFinished() && notes.isEmpty()) {
            noteSpawner.resetMap();
            mapStartTime = elapsedTime;
            hitFeedbackText = "";
            hitFeedbackTimer = 0f;
        }
    }

    private void startRun() {
        fallbackElapsedTime = 0f;
        lastAudioPosition = 0f;
        mapStartTime = 0f;

        useMusicClock = audioManager.isLoaded();
        if (useMusicClock) {
            audioManager.setLooping(false);
            audioManager.play();
        }
    }

    private void restartRun() {
        notes.clear();
        hitEffects.clear();
        noteSpawner.resetMap();
        scoreSystem = new ScoreSystem();

        hitFeedbackText = "";
        hitFeedbackTimer = 0f;
        elapsedTime = 0f;
        fallbackElapsedTime = 0f;
        lastAudioPosition = 0f;
        runDurationSeconds = resolveRunDurationSeconds();
        mapStartTime = 0f;

        if (audioManager.isLoaded()) {
            audioManager.restart();
            useMusicClock = true;
        } else {
            useMusicClock = false;
        }

        gameState = GameState.PLAYING;
    }

    private void finishRunAndShowResult() {
        gameState = GameState.FINISHED;
        audioManager.stop();

        game.setScreen(new ResultScreen(
                game,
                scoreSystem.getScore(),
                scoreSystem.getMaxCombo(),
                elapsedTime
        ));
        dispose();
    }

    private void handleGlobalControls() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartRun();
        }
    }

    private String resolveTrackPath() {
        String[] preferredNames = {
                "main.mp3",
                "main.ogg",
                "main.wav",
                "mozart.mp3",
                "mozart.ogg",
                "mozart.wav"
        };

        for (String fileName : preferredNames) {
            String candidate = AUDIO_DIR + "/" + fileName;
            if (fileExists(candidate)) {
                return candidate;
            }
        }

        FileHandle audioFolder = resolveFolderHandle(AUDIO_DIR);
        if (!audioFolder.exists() || !audioFolder.isDirectory()) {
            return null;
        }

        FileHandle[] files = audioFolder.list();

        String firstMp3 = null;
        String firstOgg = null;
        String firstWav = null;

        for (FileHandle file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String name = file.name().toLowerCase();

            if (firstMp3 == null && name.endsWith(".mp3")) {
                firstMp3 = AUDIO_DIR + "/" + file.name();
            } else if (firstOgg == null && name.endsWith(".ogg")) {
                firstOgg = AUDIO_DIR + "/" + file.name();
            } else if (firstWav == null && name.endsWith(".wav")) {
                firstWav = AUDIO_DIR + "/" + file.name();
            }
        }

        if (firstMp3 != null) {
            return firstMp3;
        }
        if (firstOgg != null) {
            return firstOgg;
        }
        return firstWav;
    }

    private boolean fileExists(String path) {
        return Gdx.files.internal(path).exists()
                || Gdx.files.classpath(path).exists()
                || Gdx.files.local(path).exists()
                || Gdx.files.absolute(path).exists();
    }

    private FileHandle resolveFolderHandle(String path) {
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

        return Gdx.files.internal(path);
    }

    private float resolveElapsedTime(float deltaTime) {

        if (useMusicClock && audioManager.isPlaying()) {
            float musicPosition = audioManager.getPosition();

            if (musicPosition > lastAudioPosition || musicPosition > 0f) {
                lastAudioPosition = musicPosition;
                fallbackElapsedTime = musicPosition;
                return musicPosition;
            }
        }

        fallbackElapsedTime += deltaTime;
        return fallbackElapsedTime;
    }

    private float resolveRunDurationSeconds() {
        return MAX_RUN_DURATION_SECONDS;
    }

    private void updateFeedbackTimer(float deltaTime) {

        if (hitFeedbackTimer <= 0f) return;

        hitFeedbackTimer -= deltaTime;

        if (hitFeedbackTimer <= 0f) {
            hitFeedbackTimer = 0f;
            hitFeedbackText = "";
        }
    }

    private void showHitFeedback(String text) {
        hitFeedbackText = text;
        hitFeedbackTimer = HIT_FEEDBACK_DURATION;
    }

    private void removeResolvedNotes() {

        Iterator<Note> iterator = notes.iterator();

        while (iterator.hasNext()) {
            Note note = iterator.next();

            if (!note.isActive()) {
                iterator.remove();
            }
        }
    }

    private void updateHitEffects(float deltaTime) {

        Iterator<HitEffect> iterator = hitEffects.iterator();

        while (iterator.hasNext()) {
            HitEffect effect = iterator.next();

            if (effect.update(deltaTime)) {
                iterator.remove();
            }
        }
    }

    private void spawnHitEffect(Note note) {
        if (note == null) return;

        hitEffects.add(new HitEffect(
                note.getX() + note.getWidth() * 0.5f,
                GameConfig.HIT_LINE_Y
        ));
    }

    private void drawBackground() {

        Color previous = spriteBatch.getColor();

        spriteBatch.setColor(0.12f, 0.15f, 0.2f, 1f);
        spriteBatch.draw(pixelTexture, 0f, 0f, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);

        spriteBatch.setColor(0.17f, 0.2f, 0.27f, 1f);

        for (int i = 0; i < LaneKey.values().length; i++) {
            float x = GameConfig.LANES_START_X +
                    i * (GameConfig.LANE_WIDTH + GameConfig.LANE_GAP);

            spriteBatch.draw(pixelTexture, x, 0f, GameConfig.LANE_WIDTH, GameConfig.SCREEN_HEIGHT);
        }

        spriteBatch.setColor(previous);
    }

    private void drawHitLine() {

        Color previous = spriteBatch.getColor();

        spriteBatch.setColor(Color.WHITE);

        spriteBatch.draw(
                pixelTexture,
                GameConfig.LANES_START_X,
                GameConfig.HIT_LINE_Y,
                4 * GameConfig.LANE_WIDTH + 3 * GameConfig.LANE_GAP,
                3f
        );

        spriteBatch.setColor(previous);
    }

    private void drawHud() {

        font.getData().setScale(SCORE_SCALE);
        font.setColor(Color.WHITE);

        font.draw(spriteBatch, "Score: " + scoreSystem.getScore(), 20f, GameConfig.SCREEN_HEIGHT - 20f);
        font.draw(spriteBatch, "Max Combo: " + scoreSystem.getMaxCombo(), 20f, GameConfig.SCREEN_HEIGHT - 48f);
        font.draw(spriteBatch, "Time: " + (int) elapsedTime + " / " + (int) runDurationSeconds + "s", 20f, GameConfig.SCREEN_HEIGHT - 76f);
        font.draw(spriteBatch, "Keys: A S D F | R: Restart", 20f, 36f);

        String comboText = "COMBO " + scoreSystem.getCombo();

        font.getData().setScale(COMBO_SCALE);
        font.setColor(scoreSystem.getCombo() > 0 ? Color.WHITE : Color.LIGHT_GRAY);

        drawCenteredText(comboText, GameConfig.SCREEN_HEIGHT * 0.78f);

        font.getData().setScale(1f);
    }

    private void drawStateLabel() {

        font.setColor(Color.LIGHT_GRAY);

        if (gameState == GameState.READY) {
            drawCenteredText("PRESS ANY KEY", GameConfig.SCREEN_HEIGHT - 40f);
        }

        if (gameState == GameState.FINISHED) {
            drawCenteredText("MAP COMPLETE", GameConfig.SCREEN_HEIGHT - 40f);
        }
    }

    private void drawHitFeedback() {

        if (hitFeedbackText.isEmpty()) return;

        float scale = FEEDBACK_BASE_SCALE + hitFeedbackTimer * FEEDBACK_SCALE_FACTOR;
        font.getData().setScale(scale);

        switch (hitFeedbackText) {
            case "PERFECT" -> font.setColor(Color.GOLD);
            case "OK" -> font.setColor(Color.SKY);
            case "MISS" -> font.setColor(Color.RED);
        }

        drawCenteredText(hitFeedbackText, GameConfig.HIT_LINE_Y + 96f);

        font.getData().setScale(1f);
    }

    private void drawCenteredText(String text, float y) {
        glyphLayout.setText(font, text);
        float x = (GameConfig.SCREEN_WIDTH - glyphLayout.width) * 0.5f;
        font.draw(spriteBatch, text, x, y);
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
        audioManager.dispose();
    }
}
