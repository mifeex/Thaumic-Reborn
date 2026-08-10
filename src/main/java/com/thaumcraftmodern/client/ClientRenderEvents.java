package com.thaumcraftmodern.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.ThaumometerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE,
        value = Dist.CLIENT
)
public final class ClientRenderEvents {
    private static final double THAUMOMETER_DEPTH_NEAR = 0.0D;
    private static final double THAUMOMETER_DEPTH_FAR = 0.05D;

    private ClientRenderEvents() {
    }

    @SubscribeEvent
    public static void renderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        boolean mainHandThaumometer =
                minecraft.player.getMainHandItem().getItem() instanceof ThaumometerItem;
        boolean offHandThaumometer =
                minecraft.player.getOffhandItem().getItem() instanceof ThaumometerItem;
        if (!mainHandThaumometer && !offHandThaumometer) {
            return;
        }

        /*
         * A Thaumometer owns the complete first-person hand presentation.
         * Suppress both ordinary item poses, then render the pair once from
         * whichever hand owns the instrument.
         */
        event.setCanceled(true);
        InteractionHand owner = mainHandThaumometer
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        if (event.getHand() != owner || minecraft.player.isInvisible()) {
            return;
        }

        EntityRenderer<?> entityRenderer =
                minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
        if (entityRenderer instanceof PlayerRenderer playerRenderer) {
            renderThaumometerHands(event, minecraft, playerRenderer, owner);
        }
    }

    private static void renderThaumometerHands(
            RenderHandEvent event,
            Minecraft minecraft,
            PlayerRenderer playerRenderer,
            InteractionHand owner
    ) {
        PoseStack poseStack = event.getPoseStack();
        ThaumometerSwingAnimation.Transform swing =
                ThaumometerSwingAnimation.sample(
                        event.getSwingProgress(),
                        ThaumometerSwingAnimation.sideFor(
                                minecraft.player.getMainArm(),
                                owner
                        ),
                        event.getEquipProgress()
        );
        ThaumometerFirstPersonPose.capture(owner, swing);
        ThaumometerHudLayout.Layout layout = ThaumometerHudLayout.current();

        poseStack.pushPose();
        poseStack.translate(swing.handOffsetX(), swing.handOffsetY(), 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(swing.handRotationDegrees()));
        poseStack.translate(
                0.0F,
                0.04F,
                -0.72F
        );
        /*
         * Treat the hands and instrument as two explicit screen-space layers.
         * The instrument is already positioned closer to the camera than the
         * hands, so the ordinary depth test keeps it in front. Never clear the
         * shared depth buffer here: OptiFine shader passes reuse it for
         * translucent world geometry, including water.
         */
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, layout.handsOffsetZ());
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderArm(
                poseStack,
                event,
                minecraft,
                playerRenderer,
                HumanoidArm.RIGHT
        );
        renderArm(
                poseStack,
                event,
                minecraft,
                playerRenderer,
                HumanoidArm.LEFT
        );
        poseStack.popPose();
        flushRenderLayer(event);

        renderThaumometerInForeground(
                poseStack,
                event,
                minecraft,
                owner,
                layout
        );
        poseStack.popPose();
    }

    private static void renderThaumometerInForeground(
            PoseStack poseStack,
            RenderHandEvent event,
            Minecraft minecraft,
            InteractionHand owner,
            ThaumometerHudLayout.Layout layout
    ) {
        /*
         * Depth-range remapping acts like a z-index without changing the
         * perspective transform: the instrument keeps the exact same screen
         * size and position, but all of its fragments occupy the nearest 5%
         * of the depth buffer and therefore cover the already-rendered arms.
         *
         * Do not clear or disable the shared depth buffer. The renderer writes
         * depth for the solid frame, then suppresses depth writes only for the
         * transparent lens. That keeps deferred water/cloud passes behind the
         * body while allowing them to remain visible through the glass.
         */
        GL11.glDepthRange(
                THAUMOMETER_DEPTH_NEAR,
                THAUMOMETER_DEPTH_FAR
        );
        try {
            renderThaumometer(
                    poseStack,
                    event,
                    minecraft,
                    owner,
                    layout
            );
            flushRenderLayer(event);
        } finally {
            GL11.glDepthRange(0.0D, 1.0D);
        }
    }

    private static void flushRenderLayer(RenderHandEvent event) {
        if (event.getMultiBufferSource() instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch();
        }
    }

    private static void renderThaumometer(
            PoseStack poseStack,
            RenderHandEvent event,
            Minecraft minecraft,
            InteractionHand owner,
            ThaumometerHudLayout.Layout layout
    ) {
        ItemStack thaumometer = owner == InteractionHand.MAIN_HAND
                ? minecraft.player.getMainHandItem()
                : minecraft.player.getOffhandItem();
        poseStack.pushPose();
        poseStack.translate(
                layout.modelOffsetX(),
                layout.modelOffsetY(),
                layout.modelOffsetZ()
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(layout.modelRotationXDegrees()));
        poseStack.mulPose(Axis.YP.rotationDegrees(layout.modelRotationYDegrees()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(layout.modelRotationZDegrees()));
        poseStack.scale(
                layout.modelScale(),
                layout.modelScale(),
                layout.modelScale()
        );
        minecraft.gameRenderer.itemInHandRenderer.renderItem(
                minecraft.player,
                thaumometer,
                ItemDisplayContext.NONE,
                owner == InteractionHand.OFF_HAND,
                poseStack,
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
        poseStack.popPose();
    }

    private static void renderArm(
            PoseStack poseStack,
            RenderHandEvent event,
            Minecraft minecraft,
            PlayerRenderer playerRenderer,
            HumanoidArm arm
    ) {
        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * -41.0F));
        poseStack.translate(side * 0.3F, -1.1F, 0.45F);
        if (arm == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    minecraft.player
            );
        } else {
            playerRenderer.renderLeftHand(
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight(),
                    minecraft.player
            );
        }
        poseStack.popPose();
    }
}
