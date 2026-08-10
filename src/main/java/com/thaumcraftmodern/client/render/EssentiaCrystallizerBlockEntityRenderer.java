package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.world.block.EssentiaCrystallizerBlock;
import com.thaumcraftmodern.world.block.entity.EssentiaCrystallizerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class EssentiaCrystallizerBlockEntityRenderer
        implements BlockEntityRenderer<EssentiaCrystallizerBlockEntity> {
    public static final ResourceLocation CRYSTAL_MODEL =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "block/crystallizer_crystal"
            );

    public EssentiaCrystallizerBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(EssentiaCrystallizerBlockEntity machine, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (machine.aspect() == null) return;
        Direction input = machine.getBlockState().getValue(EssentiaCrystallizerBlock.FACING);
        int color = AspectRegistryRuntime.find(machine.aspect())
                .map(AspectDefinition::color).orElse(0xFFFFFF);
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        var minecraft = Minecraft.getInstance();
        var crystal = minecraft.getModelManager().getModel(CRYSTAL_MODEL);
        VertexConsumer out = buffers.getBuffer(RenderType.translucent());
        pose.pushPose();
        pose.translate(.5, .5, .5);
        orient(pose, input);
        // Exact final transform in TC4's orientByFace: the raw OBJ axis starts
        // at the input face and the animated crystals sit beyond the output.
        pose.translate(0.0D, 0.0D, -0.5D);
        for (int index = 0; index < 4; index++) {
            pose.pushPose();
            pose.scale(.75F, .75F, .75F);
            pose.mulPose(Axis.ZP.rotationDegrees(index * 90.0F));
            pose.translate(.34F, 0.0F, 1.2125F);
            pose.mulPose(Axis.ZP.rotationDegrees(machine.spin(partialTick)));
            minecraft.getBlockRenderer().getModelRenderer().renderModel(
                    pose.last(), out, null, crystal,
                    red, green, blue, light, overlay);
            pose.popPose();
        }
        pose.popPose();
    }
    private static void orient(PoseStack pose, Direction input) {
        switch (input) {
            case DOWN -> pose.mulPose(Axis.XP.rotationDegrees(-90));
            case UP -> pose.mulPose(Axis.XP.rotationDegrees(90));
            case SOUTH -> pose.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(-90));
            default -> { }
        }
    }
}
