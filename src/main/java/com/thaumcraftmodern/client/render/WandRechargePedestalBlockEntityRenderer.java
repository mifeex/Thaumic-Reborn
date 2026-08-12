package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.world.block.entity.WandRechargePedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** TC4 TileWandPedestalRenderer: floating wand plus one continuous wispy stream. */
public final class WandRechargePedestalBlockEntityRenderer
        implements BlockEntityRenderer<WandRechargePedestalBlockEntity> {
    private final ItemRenderer items;

    public WandRechargePedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(WandRechargePedestalBlockEntity pedestal, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = pedestal.item();
        if (stack.isEmpty()) return;
        float ticks = pedestal.getLevel() == null ? partialTick
                : pedestal.getLevel().getGameTime() + partialTick;
        float bob = net.minecraft.util.Mth.sin((ticks % 32767.0F) / 16.0F) * 0.05F;
        pose.pushPose();
        pose.translate(0.5D, 1.15D + bob, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
        items.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay,
                pose, buffers, pedestal.getLevel(), 0);
        pose.popPose();

        BlockPos node = pedestal.drainPosition();
        String aspect = pedestal.drainAspectId();
        if (!pedestal.isDraining() || node == null || aspect == null) return;
        Vec3 source = new Vec3(0.5D, 1.65D - bob * 2.0F, 0.5D);
        Vec3 endpoint = Vec3.atCenterOf(node)
                .subtract(Vec3.atLowerCornerOf(pedestal.getBlockPos()));
        pose.pushPose();
        pose.translate(source.x - 0.5D, source.y - 0.5D, source.z - 0.5D);
        ClassicNodeDrainRenderer.renderFloatyLineFromDelta(
                endpoint.subtract(source), Vec3.atCenterOf(node), pedestal.drainColor(),
                1.0F, pose, buffers);
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(WandRechargePedestalBlockEntity pedestal) {
        return true;
    }
}
