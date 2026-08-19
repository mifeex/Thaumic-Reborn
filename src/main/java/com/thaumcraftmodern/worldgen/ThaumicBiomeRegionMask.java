package com.thaumcraftmodern.worldgen;

import net.minecraft.world.level.biome.Climate;

/**
 * Seeded low-frequency Voronoi regions for naturally generated TC4 biomes.
 * Weight chooses whole regions; it never shrinks the selected region itself.
 */
public final class ThaumicBiomeRegionMask {
    static final int TAINTED_REGION_SIZE_QUARTS = 160;
    static final int MAGICAL_REGION_SIZE_QUARTS = 120;
    private static final int VANILLA_WEIGHT = 100;

    private ThaumicBiomeRegionMask() {
    }

    public static Selection select(
            int quartX,
            int quartZ,
            Climate.Sampler sampler,
            int magicalWeight,
            int taintWeight
    ) {
        return select(
                quartX,
                quartZ,
                seedSignature(sampler),
                magicalWeight,
                taintWeight
        );
    }

    public static Selection select(
            int quartX,
            int quartZ,
            long seed,
            int magicalWeight,
            int taintWeight
    ) {
        int magical = Math.max(0, magicalWeight);
        if (magical > 0 && selectedRegion(
                quartX,
                quartZ,
                seed ^ 0x4D41474943414C5FL,
                MAGICAL_REGION_SIZE_QUARTS,
                magical
        )) {
            return Selection.MAGICAL_FOREST;
        }
        int tainted = Math.max(0, taintWeight);
        if (tainted > 0 && selectedRegion(
                quartX,
                quartZ,
                seed ^ 0x5441494E5445445FL,
                TAINTED_REGION_SIZE_QUARTS,
                tainted
        )) {
            return Selection.TAINTED_LANDS;
        }
        return Selection.NONE;
    }

    private static boolean selectedRegion(
            int quartX,
            int quartZ,
            long seed,
            int regionSize,
            int weight
    ) {
        int baseCellX = Math.floorDiv(quartX, regionSize);
        int baseCellZ = Math.floorDiv(quartZ, regionSize);
        long closestDistance = Long.MAX_VALUE;
        int closestCellX = 0;
        int closestCellZ = 0;
        for (int cellX = baseCellX - 1; cellX <= baseCellX + 1; cellX++) {
            for (int cellZ = baseCellZ - 1;
                 cellZ <= baseCellZ + 1;
                 cellZ++) {
                long centerX = (long) cellX * regionSize
                        + jitter(seed, cellX, cellZ, 0x51A7L, regionSize);
                long centerZ = (long) cellZ * regionSize
                        + jitter(seed, cellX, cellZ, 0x7A11L, regionSize);
                long dx = quartX - centerX;
                long dz = quartZ - centerZ;
                long distance = dx * dx + dz * dz;
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCellX = cellX;
                    closestCellZ = cellZ;
                }
            }
        }

        int total = VANILLA_WEIGHT + weight;
        int roll = Math.floorMod(
                mix(seed, closestCellX, closestCellZ, 0xB10FEL),
                total
        );
        return roll < weight;
    }

    public static long seedSignature(Climate.Sampler sampler) {
        Climate.TargetPoint first = sampler.sample(0, 0, 0);
        Climate.TargetPoint second = sampler.sample(137, 0, -211);
        long seed = 0x9E3779B97F4A7C15L;
        seed = mix(seed ^ first.temperature());
        seed = mix(seed ^ first.humidity());
        seed = mix(seed ^ first.continentalness());
        seed = mix(seed ^ first.erosion());
        seed = mix(seed ^ first.weirdness());
        seed = mix(seed ^ second.temperature());
        seed = mix(seed ^ second.humidity());
        seed = mix(seed ^ second.continentalness());
        seed = mix(seed ^ second.erosion());
        return mix(seed ^ second.weirdness());
    }

    private static int jitter(
            long seed,
            int cellX,
            int cellZ,
            long salt,
            int regionSize
    ) {
        int margin = regionSize / 5;
        int range = regionSize - margin * 2;
        return margin + Math.floorMod(mix(seed, cellX, cellZ, salt), range);
    }

    private static int mix(long seed, int x, int z, long salt) {
        long value = seed ^ salt;
        value ^= (long) x * 0x632BE59BD9B4E019L;
        value ^= (long) z * 0x9E3779B97F4A7C15L;
        return (int) mix(value);
    }

    private static long mix(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public enum Selection {
        NONE,
        MAGICAL_FOREST,
        TAINTED_LANDS
    }
}
