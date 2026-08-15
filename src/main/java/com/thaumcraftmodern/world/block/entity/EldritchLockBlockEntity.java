package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.world.block.EldritchLockBlock;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Server-authoritative delayed unlock and random original boss encounter. */
public final class EldritchLockBlockEntity extends BlockEntity {
    public static final int UNLOCK_TICKS = 100;
    private static final String COUNTDOWN_TAG = "UnlockCountdown";
    private static final String BOSS_X_TAG = "BossX";
    private static final String BOSS_Y_TAG = "BossY";
    private static final String BOSS_Z_TAG = "BossZ";

    private int countdown = -1;
    private BlockPos bossCenter;

    public EldritchLockBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.ELDRITCH_LOCK.get(), position, state);
        bossCenter = position;
    }

    public void setBossCenter(BlockPos center) {
        bossCenter = center.immutable();
        setChanged();
    }

    public boolean beginUnlock() {
        if (countdown >= 0) {
            return false;
        }
        countdown = 0;
        setChanged();
        sync();
        return true;
    }

    public int countdown() {
        return countdown;
    }

    public static void clientTick(
            net.minecraft.world.level.Level ignored,
            BlockPos position,
            BlockState state,
            EldritchLockBlockEntity lock
    ) {
        if (lock.countdown >= 0 && lock.countdown < UNLOCK_TICKS) {
            lock.countdown++;
        }
    }

    public static void serverTick(
            net.minecraft.world.level.Level ignored,
            BlockPos position,
            BlockState state,
            EldritchLockBlockEntity lock
    ) {
        if (!(lock.level instanceof ServerLevel level)) {
            return;
        }
        lock.migrateLegacyDoor(level, state.getValue(EldritchLockBlock.FACING));
        if (lock.countdown < 0) return;
        lock.countdown++;
        if (lock.countdown % 5 == 0) {
            level.playSound(
                    null,
                    position,
                    ModSounds.PUMP.get(),
                    SoundSource.BLOCKS,
                    0.8F,
                    0.85F + lock.countdown / 500.0F
            );
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    position.getX() + 0.5D,
                    position.getY() + 0.5D,
                    position.getZ() + 0.5D,
                    12,
                    0.8D,
                    0.8D,
                    0.8D,
                    0.08D
            );
        }
        if (lock.countdown < UNLOCK_TICKS) {
            lock.setChanged();
            return;
        }
        lock.spawnRandomEncounter(level);
        lock.openDoor(level, state.getValue(EldritchLockBlock.FACING));
        level.playSound(
                null,
                position,
                SoundEvents.END_PORTAL_SPAWN,
                SoundSource.HOSTILE,
                1.2F,
                0.8F
        );
        level.setBlock(position, Blocks.AIR.defaultBlockState(), 3);
    }

    private void spawnRandomEncounter(ServerLevel level) {
        switch (level.random.nextInt(4)) {
            case 0 -> {
                announce(level, "tc.boss.golem");
                decorateGolemRoom(level);
                spawn(level, ModEntities.ELDRITCH_CONSTRUCT, 0, 0, true);
            }
            case 1 -> {
                announce(level, "tc.boss.warden");
                decorateWardenRoom(level);
                spawn(level, ModEntities.ELDRITCH_WARDEN, 0, 0, true);
            }
            case 2 -> {
                announce(level, "tc.boss.crimson");
                decorateCultistRoom(level);
                LegacyThaumcraftMob praetor = spawn(
                        level,
                        ModEntities.CRIMSON_PRAETOR,
                        0,
                        0,
                        true
                );
                if (praetor != null) {
                    praetor.getPersistentData().putBoolean(
                            "OuterLandsPearlReward",
                            true
                    );
                }
                spawn(level, ModEntities.CRIMSON_KNIGHT, 4, 4, false);
                spawn(level, ModEntities.CRIMSON_KNIGHT, -4, -4, false);
                spawn(level, ModEntities.CRIMSON_CLERIC, 4, -4, false);
                spawn(level, ModEntities.CRIMSON_CLERIC, -4, 4, false);
            }
            default -> {
                announce(level, "tc.boss.taint");
                decorateTaintRoom(level);
                spawn(level, ModEntities.GIANT_TAINTACLE, 0, 0, true);
                spawn(level, ModEntities.TAINTACLE, 4, 4, false);
                spawn(level, ModEntities.TAINTACLE, -4, -4, false);
                spawn(level, ModEntities.TAINTACLE, 4, -4, false);
                spawn(level, ModEntities.TAINTACLE, -4, 4, false);
            }
        }
    }

    private void announce(ServerLevel level, String translationKey) {
        for (net.minecraft.server.level.ServerPlayer player : level.players()) {
            if (player.distanceToSqr(
                    worldPosition.getX() + 0.5D,
                    worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D
            ) < 1024.0D) {
                player.sendSystemMessage(Component.translatable(translationKey));
            }
        }
    }

    private void decorateGolemRoom(ServerLevel level) {
        placePedestal(level, bossCenter);
        placeObelisk(level, bossCenter.offset(7, -1, 7));
        placeObelisk(level, bossCenter.offset(-7, -1, 7));
        placeObelisk(level, bossCenter.offset(7, -1, -7));
        for (int x = -9; x <= 9; x += 3) {
            for (int z = -9; z <= 9; z += 3) {
                if ((Math.abs(x) <= 3 || Math.abs(z) <= 3)
                        || level.random.nextFloat() >= 0.18F) {
                    continue;
                }
                BlockPos target = bossCenter.offset(x, -1, z);
                if (level.getBlockState(target).isAir()) {
                    level.setBlockAndUpdate(
                            target,
                            level.random.nextBoolean()
                                    ? ModBlocks.LOOT_URN.get().defaultBlockState()
                                    : ModBlocks.LOOT_CRATE.get().defaultBlockState()
                    );
                }
            }
        }
    }

    private void decorateWardenRoom(ServerLevel level) {
        placePedestal(level, bossCenter.offset(0, 0, 7));
        placeObelisk(level, bossCenter.offset(7, -1, 0));
        placeObelisk(level, bossCenter.offset(0, -1, -7));
        for (int x : new int[]{-9, -6, 6, 9}) {
            for (int z : new int[]{-9, -6, 6, 9}) {
                if (level.random.nextFloat() < 0.7F) {
                    BlockPos target = bossCenter.offset(x, -1, z);
                    if (level.getBlockState(target).isAir()) {
                        level.setBlockAndUpdate(
                                target,
                                ModBlocks.LOOT_URN.get().defaultBlockState()
                        );
                    }
                }
            }
        }
    }

    private void decorateCultistRoom(ServerLevel level) {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                int radius = Math.max(Math.abs(x), Math.abs(z));
                if (radius >= 3 && radius <= 5
                        && level.random.nextFloat() < 0.72F) {
                    level.setBlockAndUpdate(
                            bossCenter.offset(x, -2, z),
                            ModBlocks.ANCIENT_CRUST.get().defaultBlockState()
                    );
                }
            }
        }
        for (int x : new int[]{-8, -4, 0, 4, 8}) {
            for (int z : new int[]{-8, 8}) {
                placeCultPillar(level, bossCenter.offset(x, -1, z));
            }
        }
        for (int z : new int[]{-4, 0, 4}) {
            placeCultPillar(level, bossCenter.offset(-8, -1, z));
            placeCultPillar(level, bossCenter.offset(8, -1, z));
        }
    }

    private void decorateTaintRoom(ServerLevel level) {
        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance > 11.5D
                        || level.random.nextDouble() > 0.78D - distance / 80.0D) {
                    continue;
                }
                level.setBlockAndUpdate(
                        bossCenter.offset(x, -2, z),
                        level.random.nextFloat() < 0.2F
                                ? ModBlocks.CRUSTED_TAINT.get().defaultBlockState()
                                : ModBlocks.ANCIENT_CRUST.get().defaultBlockState()
                );
                if (level.random.nextInt(7) == 0) {
                    BlockPos fibre = bossCenter.offset(x, -1, z);
                    if (level.getBlockState(fibre).isAir()) {
                        level.setBlockAndUpdate(
                                fibre,
                                ModBlocks.TAINT_FIBRES.get().defaultBlockState()
                        );
                    }
                }
            }
        }
    }

    private void placePedestal(ServerLevel level, BlockPos center) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    level.setBlockAndUpdate(
                            center.offset(x, -1, z),
                            ModBlocks.ARCANE_PEDESTAL.get().defaultBlockState()
                    );
                } else {
                    level.setBlockAndUpdate(
                            center.offset(x, -2, z),
                            ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                    );
                }
            }
        }
    }

    private void placeObelisk(ServerLevel level, BlockPos base) {
        level.setBlockAndUpdate(
                base,
                ModBlocks.ARCANE_PEDESTAL.get().defaultBlockState()
        );
        level.setBlockAndUpdate(
                base.above(),
                ModBlocks.ELDRITCH_ALTAR_PART.get().defaultBlockState()
                        .setValue(EldritchAltarPartBlock.PART, 1)
        );
    }

    private void placeCultPillar(ServerLevel level, BlockPos base) {
        level.setBlockAndUpdate(
                base,
                ModBlocks.OBSIDIAN_TILE.get().defaultBlockState()
        );
        level.setBlockAndUpdate(
                base.above(),
                ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState()
        );
        level.setBlockAndUpdate(
                base.above(2),
                ModBlocks.OBSIDIAN_TOTEM.get().defaultBlockState()
        );
    }

    private LegacyThaumcraftMob spawn(
            ServerLevel level,
            Supplier<EntityType<LegacyThaumcraftMob>> type,
            int xOffset,
            int zOffset,
            boolean boss
    ) {
        LegacyThaumcraftMob mob = type.get().create(level);
        if (mob == null) {
            return null;
        }
        BlockPos spawn = bossCenter.offset(xOffset, 0, zOffset);
        mob.moveTo(
                spawn.getX() + 0.5D,
                spawn.getY(),
                spawn.getZ() + 0.5D,
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawn),
                MobSpawnType.STRUCTURE,
                null,
                null
        );
        mob.restrictTo(bossCenter, 24);
        if (boss) {
            mob.getPersistentData().putBoolean("OuterLandsBoss", true);
            mob.setPersistenceRequired();
        }
        level.addFreshEntity(mob);
        return mob;
    }

    private void openDoor(ServerLevel level, Direction facing) {
        boolean northSouth = facing.getAxis() == Direction.Axis.Z;
        for (int horizontal = -3; horizontal <= 3; horizontal++) {
            for (int vertical = -3; vertical <= 3; vertical++) {
                BlockPos target = northSouth
                        ? worldPosition.offset(horizontal, vertical, 0)
                        : worldPosition.offset(0, vertical, horizontal);
                BlockState targetState = level.getBlockState(target);
                if (targetState.is(ModBlocks.ELDRITCH_BARRIER.get())
                        || targetState.is(ModBlocks.ANCIENT_SEAL.get())) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }

    private void migrateLegacyDoor(ServerLevel level, Direction facing) {
        boolean northSouth = facing.getAxis() == Direction.Axis.Z;
        BlockPos probe = worldPosition.offset(0, 3, 0);
        if (!level.getBlockState(probe).is(ModBlocks.ANCIENT_SEAL.get())) return;
        int[][] pattern = {
                {0, 2, 2, 2, 2, 2, 0}, {2, 2, 9, 9, 9, 2, 2},
                {2, 9, 9, 9, 9, 9, 2}, {2, 9, 9, 1, 9, 9, 2},
                {2, 9, 9, 9, 9, 9, 2}, {2, 2, 9, 9, 9, 2, 2},
                {0, 2, 2, 2, 2, 2, 0}
        };
        for (int horizontal = -3; horizontal <= 3; horizontal++) {
            for (int vertical = -3; vertical <= 3; vertical++) {
                if (horizontal == 0 && vertical == 0) continue;
                BlockPos target = northSouth
                        ? worldPosition.offset(horizontal, vertical, 0)
                        : worldPosition.offset(0, vertical, horizontal);
                BlockState replacement = switch (pattern[horizontal + 3][vertical + 3]) {
                    case 2 -> ModBlocks.ELDRITCH_DOOR.get().defaultBlockState();
                    case 9 -> ModBlocks.ELDRITCH_BARRIER.get().defaultBlockState();
                    default -> Blocks.AIR.defaultBlockState();
                };
                level.setBlock(target, replacement, 3);
            }
        }
    }

    private void sync() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) load(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(COUNTDOWN_TAG, countdown);
        tag.putInt(BOSS_X_TAG, bossCenter.getX());
        tag.putInt(BOSS_Y_TAG, bossCenter.getY());
        tag.putInt(BOSS_Z_TAG, bossCenter.getZ());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        countdown = tag.contains(COUNTDOWN_TAG)
                ? tag.getInt(COUNTDOWN_TAG)
                : -1;
        bossCenter = tag.contains(BOSS_X_TAG)
                && tag.contains(BOSS_Y_TAG)
                && tag.contains(BOSS_Z_TAG)
                ? new BlockPos(
                        tag.getInt(BOSS_X_TAG),
                        tag.getInt(BOSS_Y_TAG),
                        tag.getInt(BOSS_Z_TAG)
                )
                : worldPosition;
    }
}
