package com.mediev.charcreation.client.gui;

import com.mediev.charcreation.client.gui.anim.Easing;
import com.mediev.charcreation.client.gui.layout.ResponsiveLayout;
import com.mediev.charcreation.client.gui.skin.SkinEntry;
import com.mediev.charcreation.client.gui.skin.SkinRegistry;
import com.mediev.charcreation.client.gui.widget.PlayerModelPreviewWidget;
import com.mediev.charcreation.client.gui.widget.SkinSelectorWidget;
import com.mediev.charcreation.client.gui.wizard.AppearancePage;
import com.mediev.charcreation.client.gui.wizard.IdentityPage;
import com.mediev.charcreation.client.gui.wizard.WizardPage;
import com.mediev.charcreation.data.Gender;
import com.mediev.charcreation.network.NetworkingConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-page character creation wizard shell.
 *
 * This class only handles what's common to every page: responsive two-pane
 * layout, the persistent left-side preview/skin-selector, navigation
 * (Previous/Next/Finish/Cancel), and page-switch animation. All page-specific
 * fields and validation live in {@link WizardPage} implementations under
 * {@code client.gui.wizard} - adding a page 3 never requires changes here
 * beyond adding it to the {@code pages} list built in {@link #init()}.
 */
public final class CharacterCreationScreen extends Screen {
    /**
     * The Cancel button is for development/testing ONLY - it lets a
     * developer close the (otherwise inescapable) creator without finishing
     * it. Flip this to false (or delete the button wiring in initNavBar())
     * to remove it before shipping a build to players.
     */
    private static final boolean ENABLE_DEV_CANCEL_BUTTON = true;

    private static final int PAGE_TRANSITION_TICKS = 8;

    private ResponsiveLayout layout;

    private PlayerModelPreviewWidget preview;
    private SkinSelectorWidget skinSelector;

    private IdentityPage identityPage;
    private AppearancePage appearancePage;
    private List<WizardPage> pages;
    private int currentPageIndex;
    private final Map<WizardPage, List<ClickableWidget>> pageWidgets = new HashMap<>();
    private float pageTransitionTimer;

    private ThemedButtonWidget previousButton;
    private ThemedButtonWidget nextOrFinishButton;
    private ThemedButtonWidget cancelButton;

    private String serverError = "";

    public CharacterCreationScreen() {
        super(Text.literal("Character Creation"));
    }

    @Override
    protected void init() {
        layout = new ResponsiveLayout(width, height);
        pageWidgets.clear();
        currentPageIndex = 0;
        pageTransitionTimer = PAGE_TRANSITION_TICKS;

        initLeftPanel();
        identityPage = new IdentityPage(this::onGenderChanged);
        appearancePage = new AppearancePage(skinSelector);
        pages = List.of(identityPage, appearancePage);
        skinSelector.setEntries(SkinRegistry.getSkins(identityPage.getGender()));

        initNavBar();
        showPage(0);
    }

    private void initLeftPanel() {
        int gap = Math.max(4, layout.leftPanelHeight / 40);
        int previewHeight = (int) (layout.leftPanelHeight * 0.68f);
        int selectorHeight = layout.leftPanelHeight - previewHeight - gap;

        preview = new PlayerModelPreviewWidget(layout.leftPanelX, layout.leftPanelY, layout.leftPanelWidth, previewHeight);
        skinSelector = new SkinSelectorWidget(layout.leftPanelX, layout.leftPanelY + previewHeight + gap,
                layout.leftPanelWidth, Math.max(20, selectorHeight), this::onSkinSelected);

        addDrawableChild(preview);
        addDrawableChild(skinSelector);
    }

    private void initNavBar() {
        int buttonHeight = layout.navBarHeight - 4;
        int buttonWidth = Math.max(70, layout.rightPanelWidth / 3 - 8);

        previousButton = ThemedButtonWidget.create(layout.rightPanelX, layout.navBarY + 2, buttonWidth, buttonHeight,
                Text.literal("Previous"), btn -> goToPage(currentPageIndex - 1));

        nextOrFinishButton = ThemedButtonWidget.create(
                layout.rightPanelX + layout.rightPanelWidth - buttonWidth, layout.navBarY + 2, buttonWidth, buttonHeight,
                Text.literal("Next"), btn -> onNextOrFinishPressed());

        addDrawableChild(previousButton);
        addDrawableChild(nextOrFinishButton);

        if (ENABLE_DEV_CANCEL_BUTTON) {
            int cancelWidth = 70;
            cancelButton = ThemedButtonWidget.create(width - cancelWidth - 6, 6, cancelWidth, 16,
                    Text.literal("[DEV] Cancel"), btn -> close0());
            addDrawableChild(cancelButton);
        }
    }

    private WizardPage currentPage() {
        return pages.get(currentPageIndex);
    }

    private void goToPage(int index) {
        if (index < 0 || index >= pages.size() || index == currentPageIndex) {
            return;
        }
        showPage(index);
    }

    private void showPage(int index) {
        if (!pages.isEmpty() && currentPageIndex >= 0 && currentPageIndex < pages.size() && pageWidgets.containsKey(currentPage())) {
            WizardPage oldPage = currentPage();
            for (ClickableWidget widget : pageWidgets.get(oldPage)) {
                remove(widget);
            }
            oldPage.onHidden();
        }

        currentPageIndex = index;
        pageTransitionTimer = 0f;
        WizardPage newPage = currentPage();

        if (!pageWidgets.containsKey(newPage)) {
            List<ClickableWidget> created = new ArrayList<>();
            pageWidgets.put(newPage, created);
            WizardPage.WizardHost host = new WizardPage.WizardHost() {
                @Override
                public <T extends ClickableWidget> T addWidget(T widget) {
                    addDrawableChild(widget);
                    created.add(widget);
                    return widget;
                }
            };
            newPage.init(host, layout);
        } else {
            for (ClickableWidget widget : pageWidgets.get(newPage)) {
                addDrawableChild(widget);
            }
            newPage.updateLayout(layout);
        }
        newPage.onShown();
        updateNavButtonState();
    }

    private void onNextOrFinishPressed() {
        if (currentPageIndex < pages.size() - 1) {
            goToPage(currentPageIndex + 1);
        } else {
            submit();
        }
    }

    private void onGenderChanged(Gender gender) {
        skinSelector.setEntries(SkinRegistry.getSkins(gender));
    }

    private void onSkinSelected(SkinEntry entry) {
        preview.setSkin(entry);
    }

    private boolean allPagesValid() {
        for (WizardPage page : pages) {
            if (!page.isValid()) {
                return false;
            }
        }
        return true;
    }

    private void updateNavButtonState() {
        boolean isLastPage = currentPageIndex == pages.size() - 1;
        previousButton.active = currentPageIndex > 0;
        nextOrFinishButton.setMessage(Text.literal(isLastPage ? "Finish" : "Next"));
        nextOrFinishButton.active = isLastPage ? allPagesValid() : currentPage().isValid();
    }

    @Override
    public void tick() {
        for (WizardPage page : pages) {
            page.tick();
        }
        updateNavButtonState();
    }

    private void submit() {
        if (!allPagesValid()) {
            return;
        }
        SkinEntry skin = appearancePage.getSelectedSkin();
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(identityPage.getFirstName());
        buf.writeString(identityPage.getLastName());
        buf.writeEnumConstant(identityPage.getNationality());
        buf.writeEnumConstant(identityPage.getGender());
        buf.writeInt(identityPage.getBirthDay());
        buf.writeInt(identityPage.getBirthMonth());
        buf.writeInt(identityPage.getBirthYear());
        buf.writeEnumConstant(identityPage.getBackground());
        buf.writeString(skin.id());
        ClientPlayNetworking.send(NetworkingConstants.SUBMIT_CHARACTER, buf);
    }

    public void showServerError(String message) {
        serverError = message;
    }

    /** Dev-only: closes the screen locally without telling the server. See {@link #ENABLE_DEV_CANCEL_BUTTON}. */
    private void close0() {
        if (client != null) {
            client.setScreen(null);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, MedievalTheme.BACKGROUND_COLOR);

        drawPanelChrome(context, layout.leftPanelX, layout.leftPanelY, layout.leftPanelWidth, layout.leftPanelHeight);
        drawPanelChrome(context, layout.rightPanelX, layout.rightPanelY, layout.rightPanelWidth, layout.rightPanelHeight);

        int centerX = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Forge Your Legend"), centerX, layout.contentTop / 2,
                MedievalTheme.GOLD_ACCENT_BRIGHT);

        String stepLabel = "Step " + (currentPageIndex + 1) + " of " + pages.size() + " - " + currentPage().getTitle();
        context.drawText(textRenderer, Text.literal(stepLabel), layout.rightPanelX, layout.rightPanelY - 12,
                MedievalTheme.TEXT_MUTED, false);

        pageTransitionTimer = Math.min(PAGE_TRANSITION_TICKS, pageTransitionTimer + delta);
        float transitionProgress = Easing.easeOutCubic(pageTransitionTimer / PAGE_TRANSITION_TICKS);
        int slide = (int) ((1f - transitionProgress) * 10f);

        context.getMatrices().push();
        context.getMatrices().translate(0, slide, 0);
        currentPage().renderContent(context, mouseX, mouseY, delta, layout);
        context.getMatrices().pop();

        super.render(context, mouseX, mouseY, delta);

        if (!serverError.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(serverError), centerX,
                    layout.navBarY + layout.navBarHeight + 4, MedievalTheme.ERROR_COLOR);
        }
    }

    private void drawPanelChrome(DrawContext context, int x, int y, int w, int h) {
        context.fill(x, y, x + w, y + h, MedievalTheme.PANEL_COLOR);
        context.fill(x, y, x + w, y + 2, MedievalTheme.GOLD_ACCENT);
        context.fill(x, y + h - 2, x + w, y + h, MedievalTheme.GOLD_ACCENT);
        context.fill(x, y, x + 2, y + h, MedievalTheme.GOLD_ACCENT);
        context.fill(x + w - 2, y, x + w, y + h, MedievalTheme.GOLD_ACCENT);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
