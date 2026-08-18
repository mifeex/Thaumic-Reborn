package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBaseBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** TC4 TileArcaneBoreBaseRenderer transforms and original Bore.png atlas. */
public final class ArcaneBoreBaseBlockEntityRenderer
        implements BlockEntityRenderer<ArcaneBoreBaseBlockEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/bore.png");
    private final ArcaneBoreModel model;
    public ArcaneBoreBaseBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new ArcaneBoreModel(context.bakeLayer(ArcaneBoreModel.LAYER));
    }
    @Override public void render(ArcaneBoreBaseBlockEntity base, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        pose.pushPose(); pose.translate(0.5D, 0.0D, 0.5D);
        model.renderSupport(pose, vertices, light, overlay);
        pose.pushPose(); rotateNozzle(pose, base.output());
        model.renderSupportNozzle(pose, vertices, light, overlay);
        pose.popPose(); pose.popPose();
    }
    static void rotateNozzle(PoseStack pose, Direction facing) {
        switch (facing) {
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(270));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(180));
            case UP -> pose.mulPose(Axis.ZP.rotationDegrees(90));
            case DOWN -> pose.mulPose(Axis.ZP.rotationDegrees(-90));
            default -> { }
        }
    }
    @Override public boolean shouldRenderOffScreen(ArcaneBoreBaseBlockEntity base) { return true; }
}
