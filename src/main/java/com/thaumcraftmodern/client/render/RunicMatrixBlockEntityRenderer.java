package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.particle.InfusionMatrixVisualEffects;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.entity.RunicMatrixBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

/** TC4 eight-cube matrix, glow mask, startup shake and crafting halo. */
public final class RunicMatrixBlockEntityRenderer
        implements BlockEntityRenderer<RunicMatrixBlockEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/block/infuser.png");
    private static final Random HALO_RANDOM = new Random(245L);
    private final RunicMatrixCubeModel model;

    public RunicMatrixBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new RunicMatrixCubeModel(
                context.bakeLayer(RunicMatrixCubeModel.LAYER));
    }

    @Override
    public void render(RunicMatrixBlockEntity matrix, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        float ticks = matrix.getLevel() == null ? partialTick
                : matrix.getLevel().getGameTime() + partialTick;
        boolean active = matrix.getBlockState().getValue(RunicMatrixBlock.ACTIVE);
        float startUp = matrix.clientStartUp();
        float craftFactor = Math.min(matrix.clientCraftTicks(), 50) / 50.0F;
        float instability = Math.min(6.0F, 1.0F + matrix.instability() * 0.66F
                * craftFactor);
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees((ticks % 360.0F) * startUp));
        pose.mulPose(Axis.XP.rotationDegrees(35.0F * startUp));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0F * startUp));
        renderCubeCluster(pose,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                light, overlay, ticks, instability, startUp);
        if (active) {
            renderCubeGlow(pose,
                    buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE)),
                    LightTexture.FULL_BRIGHT, overlay, ticks, instability, startUp);
        }
        pose.popPose();
        if (matrix.clientCraftTicks() > 0) {
            renderHalo(pose, buffers.getBuffer(RenderType.lightning()),
                    matrix.clientCraftTicks());
        }
        InfusionMatrixVisualEffects.tick(matrix);
    }

    private void renderCubeCluster(PoseStack pose, VertexConsumer vertices,
            int light, int packedOverlay, float ticks, float instability,
            float startUp) {
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float ox = net.minecraft.util.Mth.sin((ticks + a * 10.0F)
                            / (15.0F - instability / 2.0F))
                            * 0.01F * startUp * instability;
                    float oy = net.minecraft.util.Mth.sin((ticks + b * 10.0F)
                            / (14.0F - instability / 2.0F))
                            * 0.01F * startUp * instability;
                    float oz = net.minecraft.util.Mth.sin((ticks + c * 10.0F)
                            / (13.0F - instability / 2.0F))
                            * 0.01F * startUp * instability;
                    pose.pushPose();
                    pose.translate(ox + (a == 0 ? -0.25F : 0.25F),
                            oy + (b == 0 ? -0.25F : 0.25F),
                            oz + (c == 0 ? -0.25F : 0.25F));
                    if (a > 0) pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                    if (b > 0) pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                    if (c > 0) pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    pose.scale(0.45F, 0.45F, 0.45F);
                    model.renderSolid(pose, vertices, light, packedOverlay);
                    pose.popPose();
                }
            }
        }
    }

    private void renderCubeGlow(PoseStack pose, VertexConsumer vertices,
            int light, int packedOverlay, float ticks, float instability,
            float startUp) {
        for (int a = 0; a < 2; a++) {
            for (int b = 0; b < 2; b++) {
                for (int c = 0; c < 2; c++) {
                    float ox = net.minecraft.util.Mth.sin((ticks + a * 10.0F)
                            / (15.0F - instability / 2.0F))
                            * 0.01F * startUp * instability;
                    float oy = net.minecraft.util.Mth.sin((ticks + b * 10.0F)
                            / (14.0F - instability / 2.0F))
                            * 0.01F * startUp * instability;
                    float oz = net.minecraft.util.Mth.sin((ticks + c * 10.0F)
                            / (13.0F - instability / 2.0F))
                            * 0.01F * startUp * instability;
                    pose.pushPose();
                    pose.translate(ox + (a == 0 ? -0.25F : 0.25F),
                            oy + (b == 0 ? -0.25F : 0.25F),
                            oz + (c == 0 ? -0.25F : 0.25F));
                    if (a > 0) pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                    if (b > 0) pose.mulPose(Axis.YP.rotationDegrees(90.0F));
                    if (c > 0) pose.mulPose(Axis.ZP.rotationDegrees(90.0F));
                    pose.scale(0.45F, 0.45F, 0.45F);
                    float alpha = (net.minecraft.util.Mth.sin(
                            (ticks + a * 2.0F + b * 3.0F + c * 4.0F) / 4.0F)
                            * 0.1F + 0.2F) * startUp;
                    model.renderOverlay(pose, vertices, light, packedOverlay, alpha);
                    pose.popPose();
                }
            }
        }
    }

    private static void renderHalo(PoseStack pose, VertexConsumer vertices,
            int craftCount) {
        float craftScale = Math.min(craftCount, 50) / 50.0F;
        if (craftScale <= 0.0F) craftScale = 0.05F;
        float spin = craftCount / 500.0F;
        HALO_RANDOM.setSeed(245L);
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        int rays = Minecraft.useFancyGraphics() ? 20 : 10;
        for (int index = 0; index < rays; index++) {
            pose.mulPose(Axis.XP.rotationDegrees(HALO_RANDOM.nextFloat() * 360.0F));
            pose.mulPose(Axis.YP.rotationDegrees(HALO_RANDOM.nextFloat() * 360.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(HALO_RANDOM.nextFloat() * 360.0F));
            pose.mulPose(Axis.XP.rotationDegrees(HALO_RANDOM.nextFloat() * 360.0F));
            pose.mulPose(Axis.YP.rotationDegrees(HALO_RANDOM.nextFloat() * 360.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(
                    HALO_RANDOM.nextFloat() * 360.0F + spin * 360.0F));
            float length = (HALO_RANDOM.nextFloat() * 20.0F + 5.0F)
                    / (20.0F / craftScale);
            float width = (HALO_RANDOM.nextFloat() * 2.0F + 1.0F)
                    / (20.0F / craftScale);
            haloTriangle(vertices, pose, -0.866F * width, length,
                    -0.5F * width, 0.866F * width, length, -0.5F * width);
            haloTriangle(vertices, pose, 0.866F * width, length,
                    -0.5F * width, 0.0F, length, width);
            haloTriangle(vertices, pose, 0.0F, length, width,
                    -0.866F * width, length, -0.5F * width);
        }
        pose.popPose();
    }

    private static void haloTriangle(VertexConsumer vertices, PoseStack pose,
            float x1, float y1, float z1, float x2, float y2, float z2) {
        vertices.vertex(pose.last().pose(), 0, 0, 0)
                .color(255, 255, 255, 255).endVertex();
        vertices.vertex(pose.last().pose(), x1, y1, z1)
                .color(204, 0, 255, 0).endVertex();
        vertices.vertex(pose.last().pose(), x2, y2, z2)
                .color(204, 0, 255, 0).endVertex();
        vertices.vertex(pose.last().pose(), 0, 0, 0)
                .color(255, 255, 255, 255).endVertex();
    }

    @Override public boolean shouldRenderOffScreen(RunicMatrixBlockEntity matrix) {
        return true;
    }
    @Override public int getViewDistance() { return 64; }
}
