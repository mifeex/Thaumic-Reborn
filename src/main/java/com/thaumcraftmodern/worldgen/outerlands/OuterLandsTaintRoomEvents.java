package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.world.block.TaintBiomeService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Activates feature-13 cells as bounded, living taint encounters. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsTaintRoomEvents {
    private static final String ROOM_MOB_TAG = "OuterLandsTaintRoom";

    private OuterLandsTaintRoomEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(OuterLandsDimensions.OUTER_LANDS)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        OuterLandsChunkMigrationScheduler.nextTick(
                level.getServer(),
                () -> activateLoadedRoom(level, chunk)
        );
    }

    static int activateLoadedRoom(ServerLevel level, LevelChunk chunk) {
        if (level.getChunkSource().getChunkNow(
                chunk.getPos().x, chunk.getPos().z
        ) != chunk) {
            return 0;
        }
        OuterLandsMaze.RegionCell located = OuterLandsMaze.at(
                level.getSeed(), chunk.getPos().x, chunk.getPos().z
        );
        if (!located.exists() || located.cell().feature() != 13) {
            return 0;
        }
        int changed = TaintBiomeService.taintChunk(level, chunk.getPos())
                ? 1 : 0;
        changed += repairSurface(level, chunk);
        changed += replenishEncounter(level, chunk);
        if (changed > 0) {
            chunk.setUnsaved(true);
        }
        return changed;
    }

    private static int repairSurface(ServerLevel level, LevelChunk chunk) {
        int changed = 0;
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int x = minX + 4; x <= minX + 12; x++) {
            for (int z = minZ + 4; z <= minZ + 12; z++) {
                for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 3;
                        y <= OuterLandsLabyrinthGenerator.BASE_Y + 7; y++) {
                    BlockPos position = new BlockPos(x, y, z);
                    if (level.getBlockState(position).is(
                            ModBlocks.TAINT_FIBRES.get()
                    ) && roll(level.getSeed(), position, 5) != 0) {
                        level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
                        changed++;
                    }
                }
            }
        }
        for (int x = minX + 5; x <= minX + 11; x++) {
            for (int z = minZ + 5; z <= minZ + 11; z++) {
                BlockPos floor = new BlockPos(
                        x, OuterLandsLabyrinthGenerator.BASE_Y + 2, z
                );
                BlockState current = level.getBlockState(floor);
                if (roll(level.getSeed(), floor, 3) == 0
                        && (current.is(ModBlocks.ANCIENT_STONE.get())
                        || current.is(ModBlocks.ANCIENT_ROCK.get())
                        || current.is(ModBlocks.ANCIENT_CRUST.get()))) {
                    level.setBlock(floor, ModBlocks.CRUSTED_TAINT.get()
                            .defaultBlockState(), 2);
                    changed++;
                }
                BlockPos plant = floor.above();
                if (roll(level.getSeed() ^ 0x5461696e7447726fL, plant, 7) == 0
                        && OuterLandsTunnelDecorations.isInteriorAir(
                                level, plant
                        ) && level.getBlockState(floor).is(
                                ModBlocks.CRUSTED_TAINT.get()
                        )) {
                    BlockState growth = roll(level.getSeed(), plant, 4) == 0
                            ? ModBlocks.SPORE_STALK.get().defaultBlockState()
                            : ModBlocks.SHORT_TAINTED_GRASS.get()
                                    .defaultBlockState();
                    if (growth.canSurvive(level, plant)) {
                        level.setBlock(plant, growth, 2);
                        changed++;
                    }
                }
            }
        }
        return changed;
    }

    private static int replenishEncounter(
            ServerLevel level,
            LevelChunk chunk
    ) {
        if (level.getDifficulty() == Difficulty.PEACEFUL
                || !ThaumcraftModernServerConfig.spawnTaintCreatures()) {
            return 0;
        }
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        AABB room = new AABB(
                minX + 3, OuterLandsLabyrinthGenerator.BASE_Y + 2, minZ + 3,
                minX + 14, OuterLandsLabyrinthGenerator.BASE_Y + 9, minZ + 14
        );
        var existing = level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                room,
                mob -> mob.kind().tainted()
        );
        int spawned = 0;
        if (existing.stream().noneMatch(
                mob -> mob.kind() == LegacyMobKind.TAINTACLE
        )) {
            spawned += spawn(level, chunk, ModEntities.TAINTACLE.get());
        }
        if (existing.size() + spawned < 2) {
            EntityType<LegacyThaumcraftMob> secondary = switch (
                    level.random.nextInt(5)
            ) {
                case 0 -> ModEntities.TAINTED_CRAWLER.get();
                case 1 -> ModEntities.TAINT_SPORE.get();
                case 2 -> ModEntities.TAINT_SPORE_SWARMER.get();
                case 3 -> ModEntities.TAINT_SWARM.get();
                default -> ModEntities.THAUMIC_SLIME.get();
            };
            spawned += spawn(level, chunk, secondary);
        }
        return spawned;
    }

    private static int spawn(
            ServerLevel level,
            LevelChunk chunk,
            EntityType<LegacyThaumcraftMob> type
    ) {
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos position = new BlockPos(
                    minX + 6 + level.random.nextInt(5),
                    OuterLandsLabyrinthGenerator.BASE_Y + 3,
                    minZ + 6 + level.random.nextInt(5)
            );
            if (!level.getBlockState(position).isAir()
                    || !level.getBlockState(position.below()).isFaceSturdy(
                            level, position.below(), net.minecraft.core.Direction.UP
                    )) {
                continue;
            }
            LegacyThaumcraftMob mob = type.create(level);
            if (mob == null) {
                return 0;
            }
            mob.moveTo(
                    position.getX() + 0.5D,
                    position.getY(),
                    position.getZ() + 0.5D,
                    level.random.nextFloat() * 360.0F,
                    0.0F
            );
            if (!level.noCollision(mob)) {
                mob.discard();
                continue;
            }
            mob.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(position),
                    MobSpawnType.STRUCTURE,
                    null,
                    null
            );
            if (mob.kind() == LegacyMobKind.THAUMIC_SLIME) {
                mob.setThaumicSlimeSize(2 + level.random.nextInt(2));
            }
            mob.restrictTo(new BlockPos(
                    minX + 8,
                    OuterLandsLabyrinthGenerator.BASE_Y + 3,
                    minZ + 8
            ), 10);
            mob.setPersistenceRequired();
            mob.getPersistentData().putBoolean(ROOM_MOB_TAG, true);
            return level.addFreshEntity(mob) ? 1 : 0;
        }
        return 0;
    }

    private static int roll(long seed, BlockPos position, int bound) {
        long value = seed ^ position.asLong();
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdl;
        value ^= value >>> 33;
        return Math.floorMod((int) (value ^ value >>> 32), bound);
    }
}
