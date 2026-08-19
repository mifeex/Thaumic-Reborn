package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.particle.TubeVentParticleOptions;
import com.thaumcraftmodern.world.block.EldritchCrabVentBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** TC4 crab vent: warns, then throws an unhelmeted crab into the corridor. */
public final class EldritchCrabVentBlockEntity extends BlockEntity {
    private static final int VENT_COLOR = 10061994;
    private static final float VENT_SCALE = 2.0F;
    private int countdown = 150;
    private int venting;

    public EldritchCrabVentBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ELDRITCH_CRAB_VENT.get(), position, state);
    }

    public static void serverTick(
            Level ignored, BlockPos position, BlockState state,
            EldritchCrabVentBlockEntity vent
    ) {
        if (!(vent.level instanceof ServerLevel level)
                || level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        vent.countdown--;
        if (vent.countdown < 0) {
            vent.countdown = 50 + level.random.nextInt(50);
            return;
        }
        boolean active = level.getNearestPlayer(
                position.getX() + 0.5D,
                position.getY() + 0.5D,
                position.getZ() + 0.5D,
                16.0D,
                false
        ) != null;
        boolean full = vent.maxEntitiesReached(level);
        if (vent.countdown == 15 && active && !full) {
            level.blockEvent(position, state.getBlock(), 1, 0);
            level.playSound(null, position, SoundEvents.LAVA_EXTINGUISH,
                    SoundSource.BLOCKS, 0.5F, 1.0F);
        }
        if (vent.countdown <= 0 && active && !full) {
            vent.countdown = 150 + level.random.nextInt(100);
            vent.releaseCrab(level, state.getValue(EldritchCrabVentBlock.FACING));
            level.playSound(null, position, ModSounds.GORE.get(),
                    SoundSource.BLOCKS, 0.5F, 1.0F);
        }
    }

    public static void clientTick(
            Level level, BlockPos position, BlockState state,
            EldritchCrabVentBlockEntity vent
    ) {
        if (vent.venting > 0) {
            vent.venting--;
            for (int i = 0; i < 3; i++) {
                vent.drawVentParticle(level, position, state);
            }
        } else if (level.random.nextInt(20) == 0) {
            vent.drawVentParticle(level, position, state);
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {
            venting = 20;
            return true;
        }
        return super.triggerEvent(id, type);
    }

    private boolean maxEntitiesReached(ServerLevel level) {
        return level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                new AABB(worldPosition).inflate(32.0D),
                mob -> mob.kind() == LegacyMobKind.ELDRITCH_CRAB
        ).size() > 5;
    }

    /** Shared with the server GameTest that proves the vent creates a crab. */
    public boolean releaseCrab(ServerLevel level, Direction direction) {
        BlockPos spawnPosition = worldPosition.relative(direction);
        LegacyThaumcraftMob crab = ModEntities.ELDRITCH_CRAB.get().create(level);
        if (crab == null) {
            return false;
        }
        crab.moveTo(
                spawnPosition.getX() + 0.5D,
                spawnPosition.getY() + 0.5D,
                spawnPosition.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        crab.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPosition),
                MobSpawnType.STRUCTURE, null, null);
        crab.setDeltaMovement(
                direction.getStepX() * 0.2D,
                direction.getStepY() * 0.2D,
                direction.getStepZ() * 0.2D
        );
        return level.addFreshEntity(crab);
    }

    private void drawVentParticle(
            Level level, BlockPos position, BlockState state
    ) {
        Direction direction = state.getValue(EldritchCrabVentBlock.FACING);
        double x = position.getX() + 0.5D
                + direction.getStepX() / 2.1D
                + (level.random.nextDouble() - 0.5D) * 0.3D;
        double y = position.getY() + 0.5D
                + direction.getStepY() / 2.1D
                + (level.random.nextDouble() - 0.5D) * 0.3D;
        double z = position.getZ() + 0.5D
                + direction.getStepZ() / 2.1D
                + (level.random.nextDouble() - 0.5D) * 0.3D;
        double xMotion = 0.1D - level.random.nextDouble() * 0.2D;
        double yMotion = 0.1D - level.random.nextDouble() * 0.2D;
        double zMotion = 0.1D - level.random.nextDouble() * 0.2D;
        level.addParticle(
                new TubeVentParticleOptions(VENT_COLOR, VENT_SCALE),
                x,
                y,
                z,
                direction.getStepX() / 3.0D + xMotion,
                direction.getStepY() / 3.0D + yMotion,
                direction.getStepZ() / 3.0D + zMotion
        );
    }
}
