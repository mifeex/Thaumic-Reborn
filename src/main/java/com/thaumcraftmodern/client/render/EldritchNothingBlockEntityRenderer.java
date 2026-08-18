package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import com.thaumcraftmodern.world.block.entity.EldritchNothingBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** Draws TC4's layered tunnel and star field on exposed void faces. */
public final class EldritchNothingBlockEntityRenderer
        implements BlockEntityRenderer<EldritchNothingBlockEntity> {
    public EldritchNothingBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            EldritchNothingBlockEntity nothing,
            float partialTick,
            PoseStack poses,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (nothing.getLevel() == null) {
            return;
        }
        float time = nothing.getLevel().getGameTime() + partialTick;
        for (Direction face : Direction.values()) {
            BlockPos adjacentPos = nothing.getBlockPos().relative(face);
            BlockState adjacent = nothing.getLevel().getBlockState(adjacentPos);
            if (EldritchNothingBlock.isNothing(adjacent)
                    || adjacent.isSolidRender(nothing.getLevel(), adjacentPos)) {
                continue;
            }
            TemporaryHoleBlockEntityRenderer.drawFieldFace(
                    poses,
                    buffers,
                    face,
                    time,
                    nothing.getBlockPos()
            );
        }
    }
}
