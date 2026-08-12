package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.registry.ModEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Gives Pech a rare surface spawn opportunity which is independent of the
 * vanilla MONSTER cap. The entity remains a monster and its ordinary biome
 * spawn entry is intentionally retained.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class PechIndependentSpawner {
    private static final TagKey<Biome> MAGICAL_BIOMES = TagKey.create(
            Registries.BIOME,
            new ResourceLocation("forge", "is_magical")
    );
    private static final int PLAYER_DISTANCE_SQUARED =
            PechIndependentSpawnPolicy.MIN_PLAYER_DISTANCE
                    * PechIndependentSpawnPolicy.MIN_PLAYER_DISTANCE;

    private PechIndependentSpawner() {
    }

    @SubscribeEvent
    public static void levelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)
                || level.getDifficulty() == Difficulty.PEACEFUL
                || !ThaumcraftModernServerConfig.spawnPech()) {
            return;
        }
        if (!PechIndependentSpawnPolicy.isCheckTick(level.getGameTime())) {
            return;
        }
        var random = level.getRandom();
        if (!PechIndependentSpawnPolicy.winsRareRoll(
                random.nextInt(PechIndependentSpawnPolicy.RARE_ROLL_BOUND))) {
            return;
        }
        List<ServerPlayer> players = level.players().stream()
                .filter(player -> !player.isSpectator())
                .toList();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer player = players.get(random.nextInt(players.size()));
        trySpawnGroup(level, player);
    }

    private static void trySpawnGroup(ServerLevel level, ServerPlayer player) {
        var random = level.getRandom();
        for (int attempt = 0;
                attempt < PechIndependentSpawnPolicy.POSITION_ATTEMPTS;
                attempt++) {
            int x = player.getBlockX() + signedDistance(level);
            int z = player.getBlockZ() + signedDistance(level);
            BlockPos column = new BlockPos(x, player.getBlockY(), z);
            if (!level.hasChunkAt(column)) {
                continue;
            }
            int y = level.getHeight(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    x,
                    z
            );
            BlockPos origin = new BlockPos(x, y, z);
            if (!level.getBiome(origin).is(MAGICAL_BIOMES)
                    || !farEnoughFromPlayersAndWorldSpawn(level, origin)) {
                continue;
            }
            spawnGroup(level, origin);
            return;
        }
    }

    private static int signedDistance(ServerLevel level) {
        int distance = PechIndependentSpawnPolicy.MIN_PLAYER_DISTANCE
                + level.getRandom().nextInt(
                        PechIndependentSpawnPolicy.MAX_PLAYER_DISTANCE
                                - PechIndependentSpawnPolicy.MIN_PLAYER_DISTANCE
                                + 1
                );
        return level.getRandom().nextBoolean() ? distance : -distance;
    }

    private static boolean farEnoughFromPlayersAndWorldSpawn(
            ServerLevel level,
            BlockPos position
    ) {
        if (position.distSqr(level.getSharedSpawnPos())
                < PLAYER_DISTANCE_SQUARED) {
            return false;
        }
        return level.players().stream().noneMatch(player ->
                !player.isSpectator()
                        && player.distanceToSqr(
                                position.getX() + 0.5D,
                                position.getY(),
                                position.getZ() + 0.5D
                        ) < PLAYER_DISTANCE_SQUARED
        );
    }

    private static void spawnGroup(ServerLevel level, BlockPos origin) {
        int requested = PechIndependentSpawnPolicy.groupSize(
                level.getRandom().nextInt()
        );
        SpawnGroupData groupData = null;
        for (int member = 0; member < requested; member++) {
            BlockPos position = member == 0
                    ? origin
                    : nearbySurfacePosition(level, origin);
            if (position == null
                    || !level.getBiome(position).is(MAGICAL_BIOMES)
                    || !hasOrdinarySurface(level, position)
                    || !SpawnPlacements.checkSpawnRules(
                            ModEntities.PECH.get(),
                            level,
                            MobSpawnType.NATURAL,
                            position,
                            level.getRandom()
                    )) {
                continue;
            }
            LegacyThaumcraftMob pech = ModEntities.PECH.get().create(level);
            if (pech == null) {
                continue;
            }
            pech.moveTo(
                    position.getX() + 0.5D,
                    position.getY(),
                    position.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F,
                    0.0F
            );
            if (!pech.checkSpawnObstruction(level)
                    || !level.noCollision(pech)) {
                continue;
            }
            groupData = pech.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(position),
                    MobSpawnType.NATURAL,
                    groupData,
                    null
            );
            level.addFreshEntityWithPassengers(pech);
        }
    }

    private static boolean hasOrdinarySurface(
            ServerLevel level,
            BlockPos position
    ) {
        return level.getBlockState(position.below()).isFaceSturdy(
                        level,
                        position.below(),
                        net.minecraft.core.Direction.UP
                )
                && level.getFluidState(position).isEmpty()
                && level.getFluidState(position.above()).isEmpty();
    }

    private static BlockPos nearbySurfacePosition(
            ServerLevel level,
            BlockPos origin
    ) {
        int x = origin.getX() + level.getRandom().nextInt(5) - 2;
        int z = origin.getZ() + level.getRandom().nextInt(5) - 2;
        BlockPos column = new BlockPos(x, origin.getY(), z);
        if (!level.hasChunkAt(column)) {
            return null;
        }
        return new BlockPos(
                x,
                level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        x,
                        z
                ),
                z
        );
    }
}
