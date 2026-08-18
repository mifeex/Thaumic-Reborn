package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.EldritchCrabVentBlock;
import com.thaumcraftmodern.world.block.entity.EldritchCrabVentBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/** Original crabvent.obj mounted flush to its single exposed wall face. */
public final class EldritchCrabVentRenderer
        implements BlockEntityRenderer<EldritchCrabVentBlockEntity> {
    private static final ResourceLocation MODEL = texture("crabvent.obj");
    private static final ResourceLocation TEXTURE = texture("crabvent.png");

    public EldritchCrabVentRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            EldritchCrabVentBlockEntity vent,
            float partialTick,
            PoseStack pose,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        Direction facing = vent.getBlockState().getValue(
                EldritchCrabVentBlock.FACING
        );
        pose.pushPose();
        pose.translate(0.5D, 0.5D, 0.5D);
        orient(pose, facing);
        LegacyObjMesh.get(MODEL).render(
                "Vent",
                pose,
                buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                LightTexture.FULL_BRIGHT,
                1.0F, 1.0F, 1.0F, 1.0F
        );
        pose.popPose();
    }

    private static void orient(PoseStack pose, Direction facing) {
        switch (facing) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(90.0F));
            case UP -> pose.mulPose(Axis.XN.rotationDegrees(90.0F));
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> pose.mulPose(Axis.YN.rotationDegrees(90.0F));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90.0F));
            default -> {
            }
        }
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(
                ThaumcraftModern.MOD_ID, "textures/models/" + name
        );
    }

    @Override
    public boolean shouldRenderOffScreen(EldritchCrabVentBlockEntity vent) {
        return true;
    }
}
