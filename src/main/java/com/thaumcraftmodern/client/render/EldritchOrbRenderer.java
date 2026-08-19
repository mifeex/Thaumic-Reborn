package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.EldritchOrbEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Modern billboard pass for TC4's animated black Eldritch Orb atlas row.
 */
public final class EldritchOrbRenderer
        extends EntityRenderer<EldritchOrbEntity> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/particles.png"
            );
    private static final int FULL_BRIGHT = 0xF000F0;

    public EldritchOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(
            EldritchOrbEntity orb,
            float yaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean crimson = orb.isCrimsonGolemOrb();
        float scale = crimson
                ? 1.2F + (float) Math.sin(orb.tickCount / 5.0F) * 0.2F
                : 1.0F;
        poseStack.scale(scale, scale, scale);

        float u0 = crimson
                ? (1.0F + Math.floorMod(orb.tickCount, 6)) / 8.0F
                : Math.floorMod(orb.tickCount, 13) / 16.0F;
        float tileSize = crimson ? 0.125F : 0.0624375F;
        float u1 = u0 + tileSize;
        float v0 = crimson ? 0.75F : 0.1875F;
        float v1 = v0 + tileSize;
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer consumer = buffers.getBuffer(
                crimson
                        ? EldritchOrbRenderType.additive(TEXTURE)
                        : RenderType.entityTranslucentEmissive(TEXTURE)
        );
        int alpha = crimson ? 204 : 255;
        vertex(consumer, pose, -0.5F, -0.5F, u0, v1, alpha);
        vertex(consumer, pose, 0.5F, -0.5F, u1, v1, alpha);
        vertex(consumer, pose, 0.5F, 0.5F, u1, v0, alpha);
        vertex(consumer, pose, -0.5F, 0.5F, u0, v0, alpha);
        poseStack.popPose();
        super.render(
                orb,
                yaw,
                partialTick,
                poseStack,
                buffers,
                packedLight
        );
    }

    private static void vertex(
            VertexConsumer consumer,
            PoseStack.Pose pose,
            float x,
            float y,
            float u,
            float v,
            int alpha
    ) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(position, x, y, 0.0F)
                .color(255, 255, 255, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EldritchOrbEntity orb) {
        return TEXTURE;
    }
}
