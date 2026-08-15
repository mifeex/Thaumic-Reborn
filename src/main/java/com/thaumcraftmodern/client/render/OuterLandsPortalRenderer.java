package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.world.block.entity.OuterLandsPortalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Faithful modern equivalent of TC4's TileEldritchPortalRenderer. */
public final class OuterLandsPortalRenderer
        implements BlockEntityRenderer<OuterLandsPortalBlockEntity> {
    private static final int ATLAS_FRAMES = 16;

    public OuterLandsPortalRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            OuterLandsPortalBlockEntity portal,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        float opening = portal.openCount(partialTick);
        float scale = Math.min(5.0F, opening) / 5.0F;
        float scaleY = Math.min(
                OuterLandsPortalBlockEntity.OPEN_TICKS,
                opening
        ) / OuterLandsPortalBlockEntity.OPEN_TICKS;
        if (scale <= 0.0F || scaleY <= 0.0F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        long gameTime = portal.getLevel() == null
                ? 0L
                : portal.getLevel().getGameTime();
        int frame = Math.floorMod((int) gameTime, ATLAS_FRAMES);
        float u0 = frame / (float) ATLAS_FRAMES;
        float u1 = (frame + 1.0F) / ATLAS_FRAMES;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(
                minecraft.gameRenderer.getMainCamera().rotation()
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vertices = buffers.getBuffer(
                OuterLandsPortalRenderType.get()
        );
        renderPortalQuad(vertices, pose, scale, scaleY, u0, u1);
        // The original asset contains soft partial alpha. A second identical
        // colour-only pass keeps that edge while making the gate materially
        // denser against bright skies and water.
        renderPortalQuad(vertices, pose, scale, scaleY, u0, u1);
        poseStack.popPose();
    }

    private static void renderPortalQuad(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float scale,
            float scaleY,
            float u0,
            float u1
    ) {
        vertex(vertices, pose.pose(), pose.normal(),
                -scale, -scaleY, u0, 1.0F);
        vertex(vertices, pose.pose(), pose.normal(),
                scale, -scaleY, u1, 1.0F);
        vertex(vertices, pose.pose(), pose.normal(),
                scale, scaleY, u1, 0.0F);
        vertex(vertices, pose.pose(), pose.normal(),
                -scale, scaleY, u0, 0.0F);
    }

    private static void vertex(
            VertexConsumer vertices,
            Matrix4f pose,
            Matrix3f normal,
            float x,
            float y,
            float u,
            float v
    ) {
        vertices.vertex(pose, x, y, 0.0F)
                .color(1.0F, 0.0F, 1.0F, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 0.0F, 1.0F)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(OuterLandsPortalBlockEntity portal) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }
}
