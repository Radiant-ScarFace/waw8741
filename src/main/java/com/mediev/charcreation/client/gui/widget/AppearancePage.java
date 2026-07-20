package com.mediev.charcreation.client.gui.wizard;

import com.mediev.charcreation.client.gui.MedievalTheme;
import com.mediev.charcreation.client.gui.layout.ResponsiveLayout;
import com.mediev.charcreation.client.gui.skin.SkinEntry;
import com.mediev.charcreation.client.gui.widget.SkinSelectorWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Page 2 - Appearance. Only skin selection is implemented in this version;
 * everything else (hair, beard, eyes, clothing, ...) is intentionally left
 * out. Adding those later just means adding more WizardPage sections or
 * more controls here - it does not require touching the wizard shell.
 *
 * The actual preview and skin list live on the wizard shell's persistent
 * left panel (so the player can see and change their skin from either page);
 * this page owns the right-side informational panel and read access to the
 * current selection for submission.
 */
public final class AppearancePage implements WizardPage {
    private final SkinSelectorWidget skinSelector;

    public AppearancePage(SkinSelectorWidget skinSelector) {
        this.skinSelector = skinSelector;
    }

    @Override
    public String getTitle() {
        return "Appearance";
    }

    @Override
    public void init(WizardHost host, ResponsiveLayout layout) {
        // No page-local widgets yet: skin selection lives on the shared left
        // panel. This method exists and is called so future options (hair,
        // beard, eyes, clothing, ...) have a natural place to register their
        // own widgets without changing the wizard shell.
    }

    @Override
    public void updateLayout(ResponsiveLayout layout) {
    }

    @Override
    public void renderContent(DrawContext context, int mouseX, int mouseY, float delta, ResponsiveLayout layout) {
        int left = layout.rightPanelX;
        int top = layout.rightPanelY;
        int width = layout.rightPanelWidth;

        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Appearance"),
                left, top, MedievalTheme.GOLD_ACCENT_BRIGHT, false);

        SkinEntry selected = skinSelector.getSelected();
        String selectedLabel = selected != null ? selected.displayName() : "None selected";
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Selected skin: " + selectedLabel),
                left, top + 16, MedievalTheme.TEXT_COLOR, false);

        String[] wrapNotice = {
                "Pick your skin from the list on the left.",
                "More appearance options (hair, beard, eyes,",
                "clothing) are coming in a future update."
        };
        int y = top + 34;
        for (String line : wrapNotice) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(line),
                    left, y, MedievalTheme.TEXT_MUTED, false);
            y += 11;
        }

        if (selected == null) {
            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal("Select a skin to continue."),
                    left, y + 6, MedievalTheme.ERROR_COLOR, false);
        }
    }

    @Override
    public boolean isValid() {
        return skinSelector.getSelected() != null;
    }

    public SkinEntry getSelectedSkin() {
        return skinSelector.getSelected();
    }
}