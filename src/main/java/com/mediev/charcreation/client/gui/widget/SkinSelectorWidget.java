package com.mediev.charcreation.client.gui.widget;

import com.mediev.charcreation.client.gui.anim.Easing;
import com.mediev.charcreation.client.gui.skin.SkinEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Scrollable list of skins. Never contains hardcoded skin data - its content
 * is entirely whatever {@link #setEntries} is given, which the owning page
 * fills in from {@code SkinRegistry}. Adding/removing a skin file therefore
 * never requires touching this class.
 */
public final class SkinSelectorWidget extends ClickableWidget {
    private static final int ROW_HEIGHT = 22;
    private static final int ICON_SIZE = 16;
    private static final int SKIN_TEXTURE_SIZE = 64;
    private static final int FACE_U = 8;
    private static final int FACE_V = 8;
    private static final int FACE_SIZE = 8;

    private final Consumer<SkinEntry> onSelect;
    private final Map<SkinEntry, Float> hoverProgress = new HashMap<>();

    private List<SkinEntry> entries = List.of();
    private SkinEntry selected;
    private int scrollOffset;

    public SkinSelectorWidget(int x, int y, int width, int height, Consumer<SkinEntry> onSelect) {
        super(x, y, width, height, Text.literal("Skin Selector"));
        this.onSelect = onSelect;
    }

    /**
     * Replaces the visible skin list, e.g. after a gender change. If the
     * previous selection isn't in the new list, the first entry is chosen
     * automatically and the preview is notified.
     */
    public void setEntries(List<SkinEntry> newEntries) {
        this.entries = newEntries;
        this.scrollOffset = 0;
        if (selected == null || !newEntries.contains(selected)) {
            SkinEntry fallback = newEntries.isEmpty() ? null : newEntries.get(0);
            selected = fallback;
            if (fallback != null) {
                onSelect.accept(fallback);
            }
        }
    }

    public SkinEntry getSelected() {
        return selected;
    }

    private void selectFromClick(SkinEntry entry) {
        if (entry == null || entry.equals(selected)) {
            return;
        }
        selected = entry;
        onSelect.accept(entry);
    }

    private int contentHeight() {
        return entries.size() * ROW_HEIGHT;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - height);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, 0x55000000);
        context.enableScissor(getX(), getY(), getX() + width, getY() + height);

        int rowY = getY() + 2 - scrollOffset;
        for (SkinEntry entry : entries) {
            boolean rowVisible = rowY + ROW_HEIGHT >= getY() && rowY <= getY() + height;
            boolean hovered = isMouseOver(mouseX, mouseY) && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT;
            float previous = hoverProgress.getOrDefault(entry, 0f);
            float progress = Easing.clamp01(previous + (hovered ? 1f : -1f) * delta * 0.2f);
            hoverProgress.put(entry, progress);

            if (rowVisible) {
                boolean isSelected = entry.equals(selected);
                int bg = isSelected ? 0xFF4A3A1A : Easing.lerpColor(0x00000000, 0x33FFFFFF, progress);
                context.fill(getX(), rowY, getX() + width, rowY + ROW_HEIGHT, bg);
                if (isSelected) {
                    context.fill(getX(), rowY, getX() + 2, rowY + ROW_HEIGHT, 0xFFD4AF37);
                }
                int iconY = rowY + (ROW_HEIGHT - ICON_SIZE) / 2;
                context.drawTexture(entry.texture(), getX() + 5, iconY, ICON_SIZE, ICON_SIZE,
                        FACE_U, FACE_V, FACE_SIZE, FACE_SIZE, SKIN_TEXTURE_SIZE, SKIN_TEXTURE_SIZE);
                context.drawText(MinecraftClient.getInstance().textRenderer, entry.displayName(),
                        getX() + 5 + ICON_SIZE + 6, rowY + (ROW_HEIGHT - 8) / 2, 0xFFE8D9B0, false);
            }
            rowY += ROW_HEIGHT;
        }

        if (entries.isEmpty()) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    Text.literal("No skins found"), getX() + width / 2, getY() + height / 2, 0xFFA89878);
        }

        context.disableScissor();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOver(mouseX, mouseY) || button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int index = (int) ((mouseY - (getY() + 2 - scrollOffset)) / ROW_HEIGHT);
        if (index >= 0 && index < entries.size()) {
            selectFromClick(entries.get(index));
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        scrollOffset = (int) MathHelper.clamp(scrollOffset - amount * (ROW_HEIGHT / 1.5), 0, maxScroll());
        return true;
    }
}