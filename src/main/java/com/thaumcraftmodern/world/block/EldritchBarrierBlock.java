package com.thaumcraftmodern.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Invisible, indestructible backing for TC4's star-field boss door. */
public final class EldritchBarrierBlock extends Block {
    public EldritchBarrierBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }
}
