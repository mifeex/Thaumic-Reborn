package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Exact TC4 pedestal bob, spin, and block-item scale contract. */
public final class ArcanePedestalBlockEntityRenderer
        implements BlockEntityRenderer<ArcanePedestalBlockEntity> {
    private static final ResourceLocation OUTER_CAP_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/models/obelisk_cap_2.png"
            );
    private static final LegacyEldritchCapModel OUTER_CAP_MODEL =
            new LegacyEldritchCapModel();
    private final ItemRenderer items;

    public ArcanePedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(ArcanePedestalBlockEntity pedestal, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = pedestal.item();
        boolean outerCap = pedestal.getBlockState().is(
                ModBlocks.ELDRITCH_CAPSTONE.get()
        );
        if (outerCap) {
            renderOuterCap(pose, buffers, light);
        }
        if (stack.isEmpty()) return;
        float ticks = pedestal.getLevel() == null ? partialTick
                : pedestal.getLevel().getGameTime() + partialTick;
        float bob = net.minecraft.util.Mth.sin((ticks % 32767.0F) / 16.0F) * 0.05F;
        float scale = 1.0F;
        double itemHeight = outerCap ? 1.5D + bob : 1.15D + bob;
        pose.pushPose();
        pose.translate(0.5D, itemHeight, 0.5D);
        pose.mulPose(Axis.YP.rotationDegrees(ticks % 360.0F));
        pose.scale(scale, scale, scale);
        items.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay,
                pose, buffers, pedestal.getLevel(), 0);
        pose.popPose();
    }

    /** Exact TC4 TileEldritchCapRenderer transform and Outer Lands texture. */
    private static void renderOuterCap(
            PoseStack pose,
            MultiBufferSource buffers,
            int light
    ) {
        pose.pushPose();
        pose.translate(0.5D, 0.0D, 0.5D);
        pose.mulPose(Axis.XN.rotationDegrees(90.0F));
        var vertices = buffers.getBuffer(
                EldritchRenderTypes.capTriangles(OUTER_CAP_TEXTURE)
        );
        /* Ancient pedestals use only the cap shell, never the dark Tip leg. */
        OUTER_CAP_MODEL.renderClosedShell(pose, vertices, light);
        pose.popPose();
    }

    @Override public boolean shouldRenderOffScreen(ArcanePedestalBlockEntity pedestal) {
        return true;
    }
}
