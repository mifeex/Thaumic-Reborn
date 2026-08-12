package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class ThaumatoriumBlockEntityRenderer
        implements BlockEntityRenderer<ThaumatoriumBlockEntity> {
    public ThaumatoriumBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void render(ThaumatoriumBlockEntity machine, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (machine.getBlockState().getValue(ThaumatoriumBlock.HALF)
                == DoubleBlockHalf.UPPER) return;
        if (machine.getLevel() == null) return;
        if (machine.catalyst().isEmpty()) return;
        var formulae = machine.formulaeForRender();
        if (formulae.isEmpty()) return;
        int recipeIndex = (int) (machine.getLevel().getGameTime()
                / 40L % formulae.size());
        var displayedRecipe = formulae.get(recipeIndex);
        var recipe = machine.recipeForRender(displayedRecipe);
        if (recipe == null) return;
        Direction facing = machine.getBlockState().getValue(ThaumatoriumBlock.FACING);
        pose.pushPose();
        pose.translate(.5 + facing.getStepX() / 1.99,
                1.42, .5 + facing.getStepZ() / 1.99);
        switch (facing) {
            case EAST -> pose.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> pose.mulPose(Axis.YP.rotationDegrees(270));
            case NORTH -> pose.mulPose(Axis.YP.rotationDegrees(180));
            default -> { }
        }
        pose.scale(.75F, .75F, .75F);
        Minecraft.getInstance().getItemRenderer().renderStatic(recipe.output(),
                ItemDisplayContext.GROUND, light, overlay, pose, buffers,
                machine.getLevel(), 0);
        pose.popPose();
    }
}
