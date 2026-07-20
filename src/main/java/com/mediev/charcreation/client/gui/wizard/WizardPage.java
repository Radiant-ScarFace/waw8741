package com.mediev.charcreation.client.gui.wizard;

import com.mediev.charcreation.client.gui.layout.ResponsiveLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;

/**
 * One step of the character-creation wizard. The wizard shell
 * (CharacterCreationScreen) only ever talks to pages through this contract,
 * so adding, removing, or reordering pages never requires touching the
 * shell's navigation, layout, or animation logic.
 */
public interface WizardPage {

    String getTitle();

    /**
     * Called once when the page is first needed. Widgets should be created
     * here and registered through {@code host}; the page positions them
     * itself using the given layout.
     */
    void init(WizardHost host, ResponsiveLayout layout);

    /** Called whenever the screen is resized so the page can reposition its own widgets. */
    void updateLayout(ResponsiveLayout layout);

    /** Called every client tick while this page is the active page. */
    default void tick() {
    }

    /** Renders anything the page needs beyond its registered widgets (labels, panel chrome, errors). */
    void renderContent(DrawContext context, int mouseX, int mouseY, float delta, ResponsiveLayout layout);

    /** Whether every required field on this page currently holds a valid value. */
    boolean isValid();

    /** Called each time the wizard navigates onto this page. */
    default void onShown() {
    }

    /** Called each time the wizard navigates away from this page. */
    default void onHidden() {
    }

    /**
     * The minimal surface a page needs from the wizard shell: a way to
     * register its widgets so they receive input and get rendered.
     */
    interface WizardHost {
        <T extends ClickableWidget> T addWidget(T widget);
    }
}