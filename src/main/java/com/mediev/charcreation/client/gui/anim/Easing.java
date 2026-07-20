package com.mediev.charcreation.client.gui.anim;

/**
 * Tiny, dependency-free animation helpers shared across the wizard shell and
 * its widgets, so every hover/press/fade/page-transition uses the same math
 * instead of each widget rolling its own.
 */
public final class Easing {
    private Easing() {
    }

    public static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    public static float lerp(float from, float to, float progress) {
        return from + (to - from) * clamp01(progress);
    }

    public static int lerpColor(int from, int to, float progress) {
        float p = clamp01(progress);
        int fromA = (from >> 24) & 0xFF, fromR = (from >> 16) & 0xFF, fromG = (from >> 8) & 0xFF, fromB = from & 0xFF;
        int toA = (to >> 24) & 0xFF, toR = (to >> 16) & 0xFF, toG = (to >> 8) & 0xFF, toB = to & 0xFF;
        int a = (int) lerp(fromA, toA, p);
        int r = (int) lerp(fromR, toR, p);
        int g = (int) lerp(fromG, toG, p);
        int b = (int) lerp(fromB, toB, p);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Smooth ease-out cubic, useful for progress values driven by a fixed-duration timer. */
    public static float easeOutCubic(float progress) {
        float p = clamp01(progress);
        float inv = 1f - p;
        return 1f - inv * inv * inv;
    }

    /**
     * Frame-rate independent approach of `current` towards `target`.
     * `speed` is roughly "how much of the remaining distance to close per tick at 20 TPS".
     */
    public static float approach(float current, float target, float speed, float delta) {
        return lerp(current, target, clamp01(speed * (delta / 20f)));
    }
}