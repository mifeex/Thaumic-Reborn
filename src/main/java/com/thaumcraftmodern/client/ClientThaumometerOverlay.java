package com.thaumcraftmodern.client;

import com.thaumcraftmodern.item.ThaumometerItem;
import com.thaumcraftmodern.scan.ScanSessionManager;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * Renders the classic Thaumometer OBJ in first person, then places the dynamic
 * scan readout over its separately textured screen surface. The ordinary hand
 * model is suppressed separately so the instrument can own the complete
 * two-handed presentation.
 */
public final class ClientThaumometerOverlay {
    private static final float READOUT_Z = 250.0F;

    /*
     * Manual UI tuning block.
     *
     * Pixel offsets use Minecraft GUI pixels: +X moves right, +Y moves down.
     * Ratios are relative to the rendered golden frame, not the monitor.
     */
    private static final int PROGRESS_TEXT_OFFSET_Y = -5;
    private static final int PROGRESS_SIDE_INSET = 21;
    private static final int PROGRESS_MIN_WIDTH = 36;
    private static final int PROGRESS_BOTTOM_INSET = 21;
    private static final int PROGRESS_HEIGHT = 3;

    private ClientThaumometerOverlay() {
    }

    public static void render(
            ForgeGui gui,
            GuiGraphics graphics,
            float partialTick,
            int screenWidth,
            int screenHeight
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.screen != null
                || minecraft.options.hideGui
                || !minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }

        InteractionHand owner = heldThaumometerHand(minecraft);
        if (owner == null) {
            return;
        }
        float swingProgress = minecraft.player.swingingArm == owner
                ? minecraft.player.getAttackAnim(partialTick)
                : 0.0F;
        ThaumometerSwingAnimation.Transform fallbackSwing =
                ThaumometerSwingAnimation.sample(
                        swingProgress,
                        ThaumometerSwingAnimation.sideFor(
                                minecraft.player.getMainArm(),
                                owner
                        )
                );
        ThaumometerSwingAnimation.Transform swing =
                ThaumometerFirstPersonPose.currentOr(owner, fallbackSwing);

        ThaumometerReadoutLayout.Layout readoutLayout =
                ThaumometerReadoutLayout.calculate(screenWidth, screenHeight);
        int frameX = readoutLayout.frameX();
        int frameY = readoutLayout.frameY();
        int frameWidth = readoutLayout.frameWidth();
        int frameHeight = readoutLayout.frameHeight();
        int lensX = readoutLayout.readoutX();
        int lensY = readoutLayout.readoutY();
        int lensWidth = readoutLayout.readoutWidth();
        int lensHeight = readoutLayout.readoutHeight();
        graphics.pose().pushPose();
        graphics.pose().translate(
                screenWidth / 2.0F + swing.guiOffsetX(),
                screenHeight / 2.0F + swing.guiOffsetY(),
                0.0F
        );
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(swing.guiRotationDegrees()));
        graphics.pose().translate(-screenWidth / 2.0F, -screenHeight / 2.0F, 0.0F);
        try {
            ThaumometerHudLayout.Layout hudLayout = ThaumometerHudLayout.current();
            graphics.pose().pushPose();
            graphics.pose().translate(
                    frameX + frameWidth / 2.0F + hudLayout.offsetX(),
                    frameY + frameHeight / 2.0F + hudLayout.offsetY(),
                    0.0F
            );
            graphics.pose().scale(
                    hudLayout.mirrorX() ? -1.0F : 1.0F,
                    hudLayout.mirrorY() ? -1.0F : 1.0F,
                    1.0F
            );
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(hudLayout.rotationDegrees()));
            graphics.pose().translate(
                    -frameX - frameWidth / 2.0F,
                    -frameY - frameHeight / 2.0F,
                    READOUT_Z
            );
            ClientThaumometerTarget.TargetedReadout target =
                    ClientThaumometerTarget.findReadout(minecraft, partialTick).orElse(null);
            try {
                if (target != null) {
                    ClientScanOverlay.renderThaumometerTarget(
                            graphics,
                            minecraft,
                            target.displayName(),
                            target.nodeDescription(),
                            target.aspects(),
                            lensX,
                            lensY,
                            lensWidth,
                            lensHeight
                    );
                } else {
                    ClientScanOverlay.renderThaumometerReadout(
                            graphics,
                            minecraft,
                            lensX,
                            lensY,
                            lensWidth,
                            lensHeight
                    );
                }

                if (minecraft.player.isUsingItem()
                        && minecraft.player.getUseItem().getItem() instanceof ThaumometerItem) {
                    renderProgress(graphics, minecraft, lensX, lensY, lensWidth, lensHeight);
                }
            } finally {
                graphics.pose().popPose();
            }
        } finally {
            graphics.pose().popPose();
        }
    }

    private static InteractionHand heldThaumometerHand(Minecraft minecraft) {
        ItemStack mainHand = minecraft.player.getMainHandItem();
        if (mainHand.getItem() instanceof ThaumometerItem) {
            return InteractionHand.MAIN_HAND;
        }
        ItemStack offHand = minecraft.player.getOffhandItem();
        return offHand.getItem() instanceof ThaumometerItem
                ? InteractionHand.OFF_HAND
                : null;
    }

    private static void renderProgress(
            GuiGraphics graphics,
            Minecraft minecraft,
            int lensX,
            int lensY,
            int lensWidth,
            int lensHeight
    ) {
        int elapsed = ThaumometerItem.USE_DURATION - minecraft.player.getUseItemRemainingTicks();
        float progress = Math.min(1.0F, Math.max(
                0.0F,
                elapsed / (float) ScanSessionManager.REQUIRED_TICKS
        ));
        Component scanning = Component.translatable("screen.thaumic_reborn.thaumometer.scanning");
        graphics.drawCenteredString(
                minecraft.font,
                scanning,
                lensX + lensWidth / 2,
                lensY + lensHeight / 2 + PROGRESS_TEXT_OFFSET_Y,
                0xFFF1E6BA
        );

        int barWidth = Math.max(
                PROGRESS_MIN_WIDTH,
                lensWidth - PROGRESS_SIDE_INSET * 2
        );
        int barX = lensX + (lensWidth - barWidth) / 2;
        int barY = lensY + lensHeight - PROGRESS_BOTTOM_INSET;
        graphics.fill(
                barX,
                barY,
                barX + barWidth,
                barY + PROGRESS_HEIGHT,
                0xB0181632
        );
        graphics.fill(
                barX,
                barY,
                barX + Math.round(barWidth * progress),
                barY + PROGRESS_HEIGHT,
                0xFF75DDEB
        );
    }
}
