package com.thaumcraftmodern.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.knowledge.KnowledgeCapabilities;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * TC4 sanity meter plus warp-event vignette/mist feedback.
 */
public final class ClientWarpOverlay {
    private static final ResourceLocation HUD = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/gui/hud.png"
    );
    private static long vignetteUntil;
    private static long mistUntil;

    private ClientWarpOverlay() {
    }

    public static void accept(WarpFeedbackPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (!packet.messageKey().isBlank()) {
            ClientScanOverlay.showWarp(packet.messageKey());
        }
        if (packet.change() != 0) {
            String key = switch (packet.type()) {
                case WarpFeedbackPacket.PERMANENT -> "tc.addwarp";
                case WarpFeedbackPacket.NORMAL ->
                        packet.change() < 0
                                ? "tc.removewarpsticky"
                                : "tc.addwarpsticky";
                default -> "tc.addwarptemp";
            };
            minecraft.player.displayClientMessage(Component.translatable(key), true);
            if (packet.change() > 0
                    && packet.type() != WarpFeedbackPacket.TEMPORARY) {
                minecraft.level.playLocalSound(
                        minecraft.player.getX(),
                        minecraft.player.getY(),
                        minecraft.player.getZ(),
                        ModSounds.WHISPERS.get(),
                        SoundSource.PLAYERS,
                        0.5F,
                        1.0F,
                        false
                );
            }
        }
        long now = System.currentTimeMillis();
        if (packet.visual() == WarpFeedbackPacket.VISUAL_EVENT) {
            vignetteUntil = now + 5_000L;
            minecraft.level.playLocalSound(
                    minecraft.player.getX(),
                    minecraft.player.getY(),
                    minecraft.player.getZ(),
                    ModSounds.HEARTBEAT.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F,
                    false
            );
        } else if (packet.visual() == WarpFeedbackPacket.VISUAL_MIST) {
            mistUntil = Math.max(mistUntil, now + 120_000L);
        }
    }

    public static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        long now = System.currentTimeMillis();
        if (minecraft.player.hasEffect(ModEffects.BLURRED_VISION.get())) {
            int pulse = 20 + (int) (12.0D * (1.0D + Math.sin(now / 180.0D)));
            graphics.fill(0, 0, screenWidth, screenHeight, (pulse << 24) | 0x503060);
        }
        if (minecraft.player.hasEffect(ModEffects.UNNATURAL_HUNGER.get())) {
            graphics.fill(0, 0, screenWidth, screenHeight, 0x181E3010);
        }
        if (minecraft.player.hasEffect(ModEffects.SUN_SCORNED.get())) {
            graphics.fill(0, 0, screenWidth, screenHeight, 0x18FFF2B0);
        }
        if (minecraft.player.hasEffect(ModEffects.DEATH_GAZE.get())) {
            graphics.fill(0, 0, screenWidth, screenHeight, 0x30400040);
        }
        if (mistUntil > now) {
            graphics.fill(0, 0, screenWidth, screenHeight, 0x351F1633);
        }
        if (vignetteUntil > now) {
            float remaining = (vignetteUntil - now) / 5000.0F;
            int alpha = Math.min(120, 24 + (int) (remaining * 96.0F));
            graphics.fill(
                    0,
                    0,
                    screenWidth,
                    screenHeight,
                    (alpha << 24) | 0x430044
            );
        }
        if (!minecraft.player.getMainHandItem().is(ModItems.SANITY_CHECKER.get())) {
            return;
        }
        minecraft.player.getCapability(KnowledgeCapabilities.PLAYER)
                .ifPresent(knowledge -> renderMeter(
                        graphics,
                        knowledge.warp(WarpType.PERMANENT),
                        knowledge.warp(WarpType.NORMAL),
                        knowledge.warp(WarpType.TEMPORARY)
                ));
    }

    static void renderMeter(
            GuiGraphics graphics,
            int permanent,
            int normal,
            int temporary
    ) {
        float total = permanent + normal + temporary;
        float componentScale = total > 100.0F ? 100.0F / total : 1.0F;
        total = Math.min(100.0F, total);
        int empty = (int) ((100.0F - total) / 100.0F * 48.0F);
        int temporaryHeight =
                (int) (temporary / 100.0F * 48.0F * componentScale);
        int normalHeight = (int) (normal / 100.0F * 48.0F * componentScale);

        graphics.pose().pushPose();
        graphics.pose().scale(0.625F, 0.625F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(HUD, 1, 1, 152, 0, 20, 76, 256, 256);
        drawFill(graphics, empty, temporaryHeight, 1.0F, 0.5F, 1.0F);
        drawFill(
                graphics,
                empty + temporaryHeight,
                normalHeight,
                0.75F,
                0.0F,
                0.75F
        );
        int permanentHeight = Math.max(
                0,
                48 - empty - temporaryHeight - normalHeight
        );
        drawFill(
                graphics,
                empty + temporaryHeight + normalHeight,
                permanentHeight,
                0.5F,
                0.0F,
                0.5F
        );
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(HUD, 1, 1, 176, 0, 20, 76, 256, 256);
        if (total >= 100.0F) {
            graphics.blit(HUD, 1, 1, 216, 0, 20, 16, 256, 256);
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.pose().popPose();
    }

    private static void drawFill(
            GuiGraphics graphics,
            int offset,
            int height,
            float red,
            float green,
            float blue
    ) {
        if (height <= 0) {
            return;
        }
        graphics.flush();
        graphics.setColor(red, green, blue, 1.0F);
        graphics.blit(HUD, 7, 21 + offset, 200, offset, 8, height, 256, 256);
    }
}
