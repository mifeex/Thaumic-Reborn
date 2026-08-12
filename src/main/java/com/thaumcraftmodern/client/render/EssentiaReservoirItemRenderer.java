package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Modern equivalent of TC4 ItemEssentiaReservoirRenderer. */
final class EssentiaReservoirItemRenderer
        extends BlockEntityWithoutLevelRenderer {
    EssentiaReservoirItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context,
            PoseStack poses, MultiBufferSource buffers,
            int light, int overlay) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                ModBlocks.ESSENTIA_RESERVOIR.get().defaultBlockState(),
                poses, buffers, light, overlay);
    }
}
