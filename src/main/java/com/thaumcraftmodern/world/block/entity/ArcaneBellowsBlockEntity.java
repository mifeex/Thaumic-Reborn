package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Render state for TC4's continuously pumping Arcane Bellows. */
public final class ArcaneBellowsBlockEntity extends BlockEntity {
    private float previousInflation = 1.0F;
    private float inflation = 1.0F;
    private boolean filling;
    private boolean firstRun = true;

    public ArcaneBellowsBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ARCANE_BELLOWS.get(), position, state);
    }

    /** Exact TC4 world animation: fast compression and slow refill. */
    public static void clientTick(Level level, BlockPos position, BlockState state,
            ArcaneBellowsBlockEntity bellows) {
        bellows.previousInflation = bellows.inflation;
        if (level.hasNeighborSignal(position)) return;
        if (bellows.firstRun) {
            bellows.inflation = 0.35F + level.random.nextFloat() * 0.55F;
            bellows.previousInflation = bellows.inflation;
            bellows.firstRun = false;
        }
        if (bellows.inflation > 0.35F && !bellows.filling)
            bellows.inflation -= 0.075F;
        if (bellows.inflation <= 0.35F && !bellows.filling)
            bellows.filling = true;
        if (bellows.inflation < 1.0F && bellows.filling)
            bellows.inflation += 0.025F;
        if (bellows.inflation >= 1.0F && bellows.filling) {
            bellows.inflation = 1.0F;
            bellows.filling = false;
            level.playLocalSound(position.getX() + 0.5D,
                    position.getY() + 0.5D, position.getZ() + 0.5D,
                    SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 0.01F,
                    0.5F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F,
                    false);
        }
    }

    /** The item renderer retains TC4's independent sinusoidal fallback. */
    public float inflation(float partialTick) {
        if (level != null) return Mth.lerp(partialTick, previousInflation, inflation);
        return Mth.sin(partialTick / 8.0F) * 0.3F + 0.7F;
    }
}
