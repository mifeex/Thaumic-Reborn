package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.crystal.CrystalClusterVariant;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.world.block.EldritchCrystalBlock;
import com.thaumcraftmodern.world.block.entity.CrystalClusterBlockEntity;
import java.util.Random;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Deterministic six-spike port of TC4 TileCrystalRenderer. */
public final class CrystalClusterRenderer
        implements BlockEntityRenderer<CrystalClusterBlockEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/models/crystal.png"
    );
    private final CrystalClusterModel model;

    public CrystalClusterRenderer(BlockEntityRendererProvider.Context context) {
        model = new CrystalClusterModel(
                context.bakeLayer(CrystalClusterModel.LAYER)
        );
    }

    @Override
    public void render(
            CrystalClusterBlockEntity cluster,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (cluster.getBlockState().getBlock()
                instanceof EldritchCrystalBlock) {
            EldritchCrystalRenderer.renderBlock(
                    cluster,
                    partialTick,
                    poses,
                    buffers,
                    packedLight
            );
            return;
        }
        if (!(cluster.getBlockState().getBlock()
                instanceof CrystalClusterBlock block)) {
            return;
        }
        CrystalClusterVariant variant = block.variant();
        Random random = new Random(
                variant.legacyMetadata()
                        + cluster.getBlockPos().getX()
                        + cluster.getBlockPos().getY()
                        * cluster.getBlockPos().getZ()
        );
        poses.pushPose();
        // TC4 BlockCrystal has no orientation state: a solid neighbour only
        // keeps the block alive, while every cluster is rendered upright.
        poses.translate(0.5D, -0.3D, 0.5D);
        drawCrystal(
                poses,
                buffers,
                packedLight,
                random,
                variant.crystalColor(0),
                (random.nextFloat() - random.nextFloat()) * 5.0F,
                (random.nextFloat() - random.nextFloat()) * 5.0F,
                1.1F
        );
        for (int index = 1; index < 6; index++) {
            drawCrystal(
                    poses,
                    buffers,
                    packedLight,
                    random,
                    variant.crystalColor(index),
                    random.nextInt(36) + 72.0F * index,
                    15.0F + random.nextInt(15),
                    0.8F
            );
        }
        poses.popPose();
    }

    public static void renderItemCluster(
            CrystalClusterModel model,
            CrystalClusterVariant variant,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight
    ) {
        Random random = new Random(variant.legacyMetadata());
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(90.0F));
        poses.translate(0.0D, -0.8D, 0.0D);
        drawCrystal(
                model,
                poses,
                buffers,
                packedLight,
                random,
                variant.crystalColor(0),
                (random.nextFloat() - random.nextFloat()) * 5.0F,
                (random.nextFloat() - random.nextFloat()) * 5.0F,
                1.1F
        );
        for (int index = 1; index < 6; index++) {
            drawCrystal(
                    model,
                    poses,
                    buffers,
                    packedLight,
                    random,
                    variant.crystalColor(index),
                    random.nextInt(36) + 72.0F * index,
                    15.0F + random.nextInt(15),
                    0.8F
            );
        }
        poses.popPose();
    }

    private void drawCrystal(
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            Random random,
            int color,
            float yaw,
            float pitch,
            float size
    ) {
        drawCrystal(
                model,
                poses,
                buffers,
                packedLight,
                random,
                color,
                yaw,
                pitch,
                size
        );
    }

    private static void drawCrystal(
            CrystalClusterModel model,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            Random random,
            int color,
            float yaw,
            float pitch,
            float size
    ) {
        // TC4 consumed these two random values for the animated glow before
        // generating each spike's dimensions.
        random.nextInt(10);
        random.nextFloat();
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(yaw));
        poses.mulPose(Axis.XP.rotationDegrees(pitch));
        poses.scale(
                (0.15F + random.nextFloat() * 0.075F) * size,
                (0.5F + random.nextFloat() * 0.1F) * size,
                (0.15F + random.nextFloat() * 0.05F) * size
        );
        float red = legacyTint((color >> 16) & 0xFF);
        float green = legacyTint((color >> 8) & 0xFF);
        float blue = legacyTint(color & 0xFF);
        int glow = LightTexture.pack(
                Math.max(13, LightTexture.block(packedLight)),
                LightTexture.sky(packedLight)
        );
        model.render(
                poses,
                buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)),
                glow,
                OverlayTexture.NO_OVERLAY,
                red,
                green,
                blue
        );
        poses.popPose();
    }

    /**
     * TC4 passed component / 220 directly to glColor, whose fixed pipeline
     * clamped values above 1. Modern VertexConsumer converts an unclamped
     * float to an integer byte, so values such as 255 / 220 overflow and lose
     * their intended color unless the legacy GL clamp is made explicit.
     */
    static float legacyTint(int component) {
        return Math.min(1.0F, component / 220.0F);
    }

}
