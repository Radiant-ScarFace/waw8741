package com.mediev.charcreation.client.gui.widget;

import com.mediev.charcreation.client.gui.anim.Easing;
import com.mediev.charcreation.client.gui.skin.SkinEntry;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.UUID;


public final class PlayerModelPreviewWidget extends ClickableWidget {
    private static final float MIN_ZOOM = 0.6f;
    private static final float MAX_ZOOM = 2.2f;
    private static final float DEFAULT_ZOOM = 1.0f;
    private static final float FADE_DURATION_TICKS = 6f;
    private static final String HINT_TEXT = "Drag to rotate \u00b7 Scroll to zoom";

    private PreviewPlayerEntity previewPlayer;
    private SkinEntry currentSkin;

    private float yaw = 25f;
    private float zoom = DEFAULT_ZOOM;
    private boolean dragging;
    private double lastDragX;
    private float fadeTimer = FADE_DURATION_TICKS;

    public PlayerModelPreviewWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Text.literal("Character Preview"));
    }

    /** Updates the rendered skin. Safe to call every time the selection changes; triggers the fade-in. */
    public void setSkin(SkinEntry entry) {
        if (entry == null || entry.equals(currentSkin)) {
            return;
        }
        currentSkin = entry;
        fadeTimer = 0f;
        PreviewPlayerEntity player = getOrCreatePreviewPlayer();
        if (player != null) {
            player.applySkin(entry.texture(), entry.isSlim());
        }
    }

    private PreviewPlayerEntity getOrCreatePreviewPlayer() {
        if (previewPlayer != null) {
            return previewPlayer;
        }
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) {
            return null;
        }
        previewPlayer = new PreviewPlayerEntity(world);
        if (currentSkin != null) {
            previewPlayer.applySkin(currentSkin.texture(), currentSkin.isSlim());
        }
        return previewPlayer;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(getX(), getY(), getX() + width, getY() + height, 0x66000000);

        PreviewPlayerEntity player = getOrCreatePreviewPlayer();
        fadeTimer = Math.min(FADE_DURATION_TICKS, fadeTimer + delta);
        float alpha = Easing.easeOutCubic(fadeTimer / FADE_DURATION_TICKS);

        if (player == null) {
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    Text.literal("Loading preview..."), getX() + width / 2, getY() + height / 2, 0xFFFFFFFF);
            return;
        }

        int centerX = getX() + width / 2;
        int feetY = getY() + (int) (height * 0.92f);
        int size = Math.max(8, (int) (Math.min(width, height) * 0.42f * zoom));

        player.setYaw(yaw);
        player.setBodyYaw(yaw);
        player.setHeadYaw(yaw);
        player.setPitch(0f);

        context.enableScissor(getX(), getY(), getX() + width, getY() + height);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        InventoryScreen.drawEntity(context, centerX, feetY, size, 0f, 0f, player);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.disableScissor();

        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer,
                Text.literal(HINT_TEXT), centerX, getY() + height - 12, 0xFFC8B48A);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && button == 0) {
            dragging = true;
            lastDragX = mouseX;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            yaw -= (float) (mouseX - lastDragX) * 1.2f;
            lastDragX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }
        zoom = MathHelper.clamp(zoom + (float) amount * 0.08f, MIN_ZOOM, MAX_ZOOM);
        return true;
    }

    /**
     * A minimal fake player whose only job is to hold a chosen skin texture
     * and model type so vanilla's own renderer can draw it for us.
     */
    private static final class PreviewPlayerEntity extends AbstractClientPlayerEntity {
        private Identifier skinTexture;
        private String modelName = "default";

        PreviewPlayerEntity(ClientWorld world) {
            super(world, new GameProfile(UUID.randomUUID(), "Preview"));
        }

        void applySkin(Identifier texture, boolean slim) {
            this.skinTexture = texture;
            this.modelName = slim ? "slim" : "default";
        }

        @Override
        public Identifier getSkinTexture() {
            return skinTexture != null ? skinTexture : super.getSkinTexture();
        }

        @Override
        public String getModel() {
            return modelName;
        }
    }
}