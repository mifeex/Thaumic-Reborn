package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** TC4 ItemCrystalRenderer metadata-7 path. */
final class EldritchCrystalItemRenderer extends BlockEntityWithoutLevelRenderer {
    EldritchCrystalItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        poses.pushPose();
        EldritchCrystalRenderer.renderItem(poses, buffers, packedLight);
        poses.popPose();
    }
}
