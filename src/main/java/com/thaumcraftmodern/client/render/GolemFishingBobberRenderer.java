package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemFishingBobberEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Billboard float plus the original curved black tether to the golem's right hand. */
public final class GolemFishingBobberRenderer extends EntityRenderer<GolemFishingBobberEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "minecraft", "textures/entity/fishing_hook.png");

    public GolemFishingBobberRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GolemFishingBobberEntity bobber, float yaw, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.scale(.5F, .5F, .5F);
        poses.mulPose(entityRenderDispatcher.cameraOrientation());
        poses.mulPose(Axis.YP.rotationDegrees(180F));
        PoseStack.Pose pose = poses.last();
        VertexConsumer sprite = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        quad(sprite, pose.pose(), pose.normal(), light);
        poses.popPose();

        ClassicGolemEntity fisher = bobber.fisher();
        if (fisher != null) renderLine(bobber, fisher, partialTick, poses, buffers);
        super.render(bobber, yaw, partialTick, poses, buffers, light);
    }

    private static void renderLine(GolemFishingBobberEntity bobber, ClassicGolemEntity fisher,
            float partialTick, PoseStack poses, MultiBufferSource buffers) {
        double fisherX = Mth.lerp(partialTick, fisher.xOld, fisher.getX());
        double fisherY = Mth.lerp(partialTick, fisher.yOld, fisher.getY()) + fisher.getBbHeight() * .62D;
        double fisherZ = Mth.lerp(partialTick, fisher.zOld, fisher.getZ());
        float bodyYaw = Mth.lerp(partialTick, fisher.yBodyRotO, fisher.yBodyRot) * Mth.DEG_TO_RAD;
        // The whole legacy golem rig is rendered at 0.4 scale. Applying TC4's
        // unscaled 0.7 side offset sends the tether past this model. Keep the
        // forward reach, but scale the sideways hand/rod offset with the rig.
        fisherX -= Mth.cos(bodyYaw) * .25D + Mth.sin(bodyYaw) * .18D;
        fisherZ -= Mth.sin(bodyYaw) * .25D - Mth.cos(bodyYaw) * .18D;
        double bobberX = Mth.lerp(partialTick, bobber.xOld, bobber.getX());
        double bobberY = Mth.lerp(partialTick, bobber.yOld, bobber.getY()) + .12D;
        double bobberZ = Mth.lerp(partialTick, bobber.zOld, bobber.getZ());
        float dx = (float) (fisherX - bobberX);
        float dy = (float) (fisherY - bobberY);
        float dz = (float) (fisherZ - bobberZ);
        VertexConsumer line = buffers.getBuffer(RenderType.lineStrip());
        Matrix4f matrix = poses.last().pose();
        Matrix3f normal = poses.last().normal();
        for (int index = 0; index <= 16; index++) {
            float part = index / 16F;
            float x = dx * part;
            float y = dy * (part * part + part) * .5F + .12F;
            float z = dz * part;
            line.vertex(matrix, x, y, z).color(0, 0, 0, 255).normal(normal, 0F, 1F, 0F).endVertex();
        }
    }

    private static void quad(VertexConsumer out, Matrix4f matrix, Matrix3f normal, int light) {
        vertex(out, matrix, normal, light, -.5F, -.5F, 0F, 1F);
        vertex(out, matrix, normal, light, .5F, -.5F, 1F, 1F);
        vertex(out, matrix, normal, light, .5F, .5F, 1F, 0F);
        vertex(out, matrix, normal, light, -.5F, .5F, 0F, 0F);
    }

    private static void vertex(VertexConsumer out, Matrix4f matrix, Matrix3f normal, int light,
            float x, float y, float u, float v) {
        out.vertex(matrix, x, y, 0F).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0F, 1F, 0F).endVertex();
    }

    @Override public ResourceLocation getTextureLocation(GolemFishingBobberEntity entity) {
        return TEXTURE;
    }
}
