package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

/**
 * Server-owned biome-column mutation used by TC4 taint and Ethereal Blooms.
 */
public final class TaintBiomeService {
    public static final int DEFAULT_TAINT_SPREAD_RATE = 200;

    private TaintBiomeService() {
    }

    public static boolean isTainted(ServerLevel level, BlockPos position) {
        if (!level.isLoaded(position)) {
            return false;
        }
        return level.getChunkAt(position).getNoiseBiome(
                QuartPos.fromBlock(position.getX()),
                QuartPos.fromBlock(position.getY()),
                QuartPos.fromBlock(position.getZ())
        ).is(ModWorldgenKeys.TAINTED_LANDS);
    }

    public static int spreadChanceBound() {
        return Math.multiplyExact(
                ThaumcraftModernServerConfig.taintSpreadRate(),
                5
        );
    }

    public static boolean taintColumn(ServerLevel level, BlockPos position) {
        Holder<Biome> tainted = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ModWorldgenKeys.TAINTED_LANDS);
        return BiomeColumnService.replace(
                level,
                position,
                quartY -> tainted
        );
    }

    /** Converts one complete loaded labyrinth cell to the tainted biome. */
    public static boolean taintChunk(ServerLevel level, ChunkPos position) {
        LevelChunk chunk = level.getChunkSource().getChunkNow(
                position.x, position.z
        );
        if (chunk == null) {
            return false;
        }
        Holder<Biome> tainted = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ModWorldgenKeys.TAINTED_LANDS);
        int quartX = QuartPos.fromBlock(position.getMinBlockX());
        int quartZ = QuartPos.fromBlock(position.getMinBlockZ());
        int minQuartY = QuartPos.fromBlock(level.getMinBuildHeight());
        int maxQuartY = QuartPos.fromBlock(level.getMaxBuildHeight() - 1);
        boolean changed = false;
        for (int x = quartX; x < quartX + 4 && !changed; x++) {
            for (int z = quartZ; z < quartZ + 4 && !changed; z++) {
                for (int y = minQuartY; y <= maxQuartY; y++) {
                    if (!chunk.getNoiseBiome(x, y, z).is(
                            ModWorldgenKeys.TAINTED_LANDS
                    )) {
                        changed = true;
                        break;
                    }
                }
            }
        }
        if (!changed) {
            return false;
        }
        chunk.fillBiomesFromNoise(
                (sampleX, sampleY, sampleZ, sampler) -> tainted,
                level.getChunkSource().randomState().sampler()
        );
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
        return true;
    }

    public static boolean purifyColumn(ServerLevel level, BlockPos position) {
        if (!isTainted(level, position)) {
            return false;
        }
        Holder<Biome> plains = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(Biomes.PLAINS);
        int quartX = QuartPos.fromBlock(position.getX());
        int quartZ = QuartPos.fromBlock(position.getZ());
        return BiomeColumnService.replace(level, position, quartY -> {
            Holder<Biome> generated = level.getChunkSource()
                    .getGenerator()
                    .getBiomeSource()
                    .getNoiseBiome(
                            quartX,
                            quartY,
                            quartZ,
                            level.getChunkSource().randomState().sampler()
                    );
            return generated.is(ModWorldgenKeys.TAINTED_LANDS)
                    ? plains
                    : generated;
        });
    }

}
