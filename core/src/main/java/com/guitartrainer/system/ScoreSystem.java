package com.guitartrainer.system;

public class ScoreSystem {

    public enum HitGrade {
        PERFECT(100),
        OK(50),
        MISS(0);

        private final int basePoints;

        HitGrade(int basePoints) {
            this.basePoints = basePoints;
        }

        public int getBasePoints() {
            return basePoints;
        }
    }

    private int score;
    private int combo;
    private int maxCombo;

    // 🔥 opcional: último resultado (pra HUD / feedback)
    private HitGrade lastHit;

    public void register(HitGrade hitGrade) {

        lastHit = hitGrade;

        switch (hitGrade) {

            case PERFECT:
            case OK:
                combo++;
                maxCombo = Math.max(maxCombo, combo);

                int multiplier = scoreMultiplierForCombo(combo);
                int pointsGained = hitGrade.getBasePoints() * multiplier;

                score += pointsGained;

                logHit(hitGrade, multiplier, pointsGained);
                break;

            case MISS:
                combo = 0;
                logMiss();
                break;
        }
    }

    private int scoreMultiplierForCombo(int currentCombo) {
        if (currentCombo >= 10) return 3;
        if (currentCombo >= 5) return 2;
        return 1;
    }

    private void logHit(HitGrade grade, int multiplier, int points) {
        System.out.println(
                "[HIT] " + grade +
                        " | +" + points +
                        " | x" + multiplier +
                        " | combo=" + combo
        );
    }

    private void logMiss() {
        System.out.println("[MISS] combo reset");
    }

    // =========================
    // 📊 GETTERS
    // =========================

    public int getScore() {
        return score;
    }

    public int getCombo() {
        return combo;
    }

    public int getMaxCombo() {
        return maxCombo;
    }

    public HitGrade getLastHit() {
        return lastHit;
    }
}