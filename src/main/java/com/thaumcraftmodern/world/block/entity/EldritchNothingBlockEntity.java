package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render anchor created only for an exposed face of the Outer Lands void. */
public final class EldritchNothingBlockEntity extends BlockEntity {
    public EldritchNothingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELDRITCH_NOTHING.get(), pos, state);
    }
}
