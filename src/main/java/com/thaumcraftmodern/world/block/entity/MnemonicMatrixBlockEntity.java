package com.thaumcraftmodern.world.block.entity;

import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.MnemonicMatrixBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Exposes the brainbox's selected socket to tube render/connection discovery.
 * The mnemonic matrix is not an essentia inventory and never accepts or emits
 * essentia; this endpoint exists solely for the classic physical connection.
 */
public final class MnemonicMatrixBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public MnemonicMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MNEMONIC_MATRIX.get(), pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(MnemonicMatrixBlock.FACING);
    }

    @Override public boolean isConnectable(Direction side) {
        return side == facing();
    }
    @Override public boolean canInputFrom(Direction side) { return false; }
    @Override public boolean canOutputTo(Direction side) { return false; }
    @Override public void setSuction(@Nullable String aspect, int amount) { }
    @Override public @Nullable String suctionType(Direction side) { return null; }
    @Override public int suctionAmount(Direction side) { return 0; }
    @Override public @Nullable String essentiaType(Direction side) { return null; }
    @Override public int essentiaAmount(Direction side) { return 0; }
    @Override public int minimumSuction() { return 0; }
    @Override public int takeEssentia(String aspect, int amount, Direction side) {
        return 0;
    }
    @Override public int addEssentia(String aspect, int amount, Direction side) {
        return 0;
    }
}
