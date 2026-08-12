package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Exact TC4 pedestal bob, spin, and block-item scale contract. */
public final class ArcanePedestalBlockEntityRenderer
        implements BlockEntityRenderer<ArcanePedestalBlockEntity> {
    private final ItemRenderer items;

    public ArcanePedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(ArcanePedestalBlockEntity pedestal, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = pedestal.item();
        if (stack.isEmpty()) return;
        float ticks = pedestal.getLevel() == null ? partialTick
                : pedestal.getLevel().getGameTime() + partialTick;
        float bob = net.minecraft.util.Mth.sin((ticks % 32767.0F) / 16.0F) * 0.05F;
        float scale = 1.0F;
        pose.pushPose();
        pose.translate(0.5D, 1.15D + bob, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
        pose.scale(scale, scale, scale);
        items.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay,
                pose, buffers, pedestal.getLevel(), 0);
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(ArcanePedestalBlockEntity pedestal) {
        return true;
    }
}
