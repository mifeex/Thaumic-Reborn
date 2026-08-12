package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import com.thaumcraftmodern.world.block.entity.InfusionPillarBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Direct TESR equivalent of TC4's TileInfusionPillarRenderer. */
public final class InfusionPillarBlockEntityRenderer
        implements BlockEntityRenderer<InfusionPillarBlockEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/pillar.obj");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/models/pillar.png");
    public InfusionPillarBlockEntityRenderer(
            BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InfusionPillarBlockEntity pillar, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (pillar.getBlockState().getValue(InfusionPillarBlock.CAP)) return;
        Direction facing = pillar.getBlockState().getValue(InfusionPillarBlock.FACING);
        LegacyObjMesh mesh = LegacyObjMesh.get(MODEL);
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        pose.mulPose(Axis.ZP.rotationDegrees(rotation(facing)));
        mesh.render("Box001", pose,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                light, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    private static float rotation(Direction facing) {
        return switch (facing) {
            // The OBJ's unrotated bend points east/north after TC4's -90 X
            // transform. Opposite altar corners therefore differ by 180°.
            case EAST -> 90.0F;
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            default -> 0.0F; // SOUTH
        };
    }

    @Override
    public boolean shouldRenderOffScreen(InfusionPillarBlockEntity pillar) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
