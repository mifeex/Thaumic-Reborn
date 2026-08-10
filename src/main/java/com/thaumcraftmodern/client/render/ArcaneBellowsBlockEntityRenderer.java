package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.ArcaneBellowsBlock;
import com.thaumcraftmodern.world.block.entity.ArcaneBellowsBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Modern TESR counterpart of TC4 {@code TileBellowsRenderer}. */
public final class ArcaneBellowsBlockEntityRenderer
        implements BlockEntityRenderer<ArcaneBellowsBlockEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/bellows.png");
    private final ArcaneBellowsModel model;

    public ArcaneBellowsBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new ArcaneBellowsModel(context.bakeLayer(ArcaneBellowsModel.LAYER));
    }

    @Override
    public void render(ArcaneBellowsBlockEntity bellows, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light, int overlay) {
        poses.pushPose();
        poses.translate(0.5D, -0.5D, 0.5D);
        rotateFromOrientation(bellows.getBlockState().getValue(ArcaneBellowsBlock.FACING), poses);
        model.render(poses, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                light, overlay, bellows.inflation(partialTick));
        poses.popPose();
    }

    static void rotateFromOrientation(Direction orientation, PoseStack poses) {
        switch (orientation) {
            case DOWN -> { poses.translate(0, 1, -1); poses.mulPose(Axis.XP.rotationDegrees(90)); }
            case UP -> { poses.translate(0, 1, 1); poses.mulPose(Axis.XP.rotationDegrees(270)); }
            case NORTH -> poses.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poses.mulPose(Axis.YP.rotationDegrees(270));
            case EAST -> poses.mulPose(Axis.YP.rotationDegrees(90));
            default -> { }
        }
    }
}
