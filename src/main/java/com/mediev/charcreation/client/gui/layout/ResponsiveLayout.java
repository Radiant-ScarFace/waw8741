package com.mediev.charcreation.client.gui.layout;

/**
 * Resolves a screen's current width/height into the rectangles the wizard
 * shell and its pages need, expressed as percentages of the screen rather
 * than fixed pixel offsets. This is what keeps the GUI correct across every
 * Minecraft GUI Scale, window size, and aspect ratio (including ultrawide
 * and small resolutions) without any per-resolution special-casing.
 *
 * All rectangles are clamped to sane minimums so the layout degrades
 * gracefully instead of collapsing on very small windows.
 */
public final class ResponsiveLayout {
    private final int screenWidth;
    private final int screenHeight;

    public final int contentLeft;
    public final int contentTop;
    public final int contentWidth;
    public final int contentHeight;

    public final int leftPanelX;
    public final int leftPanelY;
    public final int leftPanelWidth;
    public final int leftPanelHeight;

    public final int rightPanelX;
    public final int rightPanelY;
    public final int rightPanelWidth;
    public final int rightPanelHeight;

    public final int navBarY;
    public final int navBarHeight;

    public ResponsiveLayout(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Overall content frame: a margin around the edges, scaled with the window
        // rather than fixed, so it never touches the edges on huge or tiny screens.
        int marginX = clampInt(round(screenWidth * 0.04f), 6, 60);
        int marginY = clampInt(round(screenHeight * 0.05f), 6, 40);

        this.contentLeft = marginX;
        this.contentTop = marginY;
        this.contentWidth = Math.max(160, screenWidth - marginX * 2);
        this.contentHeight = Math.max(120, screenHeight - marginY * 2);

        this.navBarHeight = clampInt(round(contentHeight * 0.12f), 22, 40);
        this.navBarY = contentTop + contentHeight - navBarHeight;

        int gap = clampInt(round(contentWidth * 0.02f), 4, 20);
        int usableHeight = contentHeight - navBarHeight - gap;

        // Left panel (preview + skin selector) takes ~42% of width on wide screens,
        // but is capped so it doesn't dominate ultrawide monitors.
        int leftWidth = clampInt(round(contentWidth * 0.42f), 160, 520);
        int rightWidth = contentWidth - leftWidth - gap;

        this.leftPanelX = contentLeft;
        this.leftPanelY = contentTop;
        this.leftPanelWidth = leftWidth;
        this.leftPanelHeight = usableHeight;

        this.rightPanelX = contentLeft + leftWidth + gap;
        this.rightPanelY = contentTop;
        this.rightPanelWidth = Math.max(140, rightWidth);
        this.rightPanelHeight = usableHeight;
    }

    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public static int percentWidth(int width, float percent) {
        return round(width * percent);
    }

    public static int percentHeight(int height, float percent) {
        return round(height * percent);
    }

    private static int round(float value) {
        return Math.round(value);
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}