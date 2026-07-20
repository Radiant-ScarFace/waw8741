package com.mediev.charcreation.client.gui;

import com.mediev.charcreation.client.gui.anim.Easing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ThemedButtonWidget extends ButtonWidget {
    private float hoverProgress;
    private float pressPunch;

    protected ThemedButtonWidget(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    public static ThemedButtonWidget create(int x, int y, int width, int height, Text message, PressAction onPress) {
        return new ThemedButtonWidget(x, y, width, height, message, onPress);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        pressPunch = 1f;
        super.onClick(mouseX, mouseY);
    }

    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();
        hoverProgress += (hovered ? 1f : -1f) * delta * 0.15f;
        hoverProgress = Easing.clamp01(hoverProgress);
        pressPunch = Math.max(0f, pressPunch - delta * 0.12f);

        // Small inward "punch" on press that eases back out - purely additive to layout,
        // never changes the widget's actual x/y/width/height used for click detection.
        float punch = Easing.easeOutCubic(pressPunch) * 2f;
        int drawX = (int) (getX() + punch);
        int drawY = (int) (getY() + punch / 2f);
        int drawWidth = (int) (width - punch * 2f);
        int drawHeight = (int) (height - punch);

        int baseColor = this.active ? MedievalTheme.STONE_COLOR : 0xFF1A1A1A;
        int borderColor = this.active
                ? Easing.lerpColor(MedievalTheme.GOLD_ACCENT, MedievalTheme.GOLD_ACCENT_BRIGHT, hoverProgress)
                : 0xFF4A4A4A;

        context.fill(drawX, drawY, drawX + drawWidth, drawY + drawHeight, baseColor);
        context.fill(drawX, drawY, drawX + drawWidth, drawY + 1, borderColor);
        context.fill(drawX, drawY + drawHeight - 1, drawX + drawWidth, drawY + drawHeight, borderColor);
        context.fill(drawX, drawY, drawX + 1, drawY + drawHeight, borderColor);
        context.fill(drawX + drawWidth - 1, drawY, drawX + drawWidth, drawY + drawHeight, borderColor);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textColor = this.active ? MedievalTheme.TEXT_COLOR : MedievalTheme.TEXT_MUTED;
        int textX = drawX + (drawWidth - textRenderer.getWidth(getMessage())) / 2;
        int textY = drawY + (drawHeight - 8) / 2;
        context.drawText(textRenderer, getMessage(), textX, textY, textColor, false);
    }
}