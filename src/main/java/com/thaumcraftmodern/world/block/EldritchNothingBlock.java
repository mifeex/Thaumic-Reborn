package com.thaumcraftmodern.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** TC4's invisible, non-colliding buffer between the maze and its bedrock shell. */
public final class EldritchNothingBlock extends Block {
    public EldritchNothingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
