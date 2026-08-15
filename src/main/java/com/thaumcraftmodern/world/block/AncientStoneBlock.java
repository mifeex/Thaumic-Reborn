package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Ancient masonry with the eight original TC4 texture variants. */
public final class AncientStoneBlock extends Block {
    public static final IntegerProperty VARIANT =
            IntegerProperty.create("variant", 0, 7);

    public AncientStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    public BlockState stateFor(BlockPos position, long salt) {
        long mixed = position.asLong() ^ salt;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdl;
        return defaultBlockState().setValue(
                VARIANT,
                Math.floorMod((int) (mixed ^ mixed >>> 32), 8)
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(VARIANT);
    }
}
