package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

/**
 * Dynamic-registry holders used by the Overworld biome-source mixin.
 *
 * <p>They cannot be obtained from built-in registries. TerraBlender also
 * keeps mod-region mappings outside MultiNoiseBiomeSource.parameters(), so
 * the running server's biome registry is the common authoritative source.</p>
 */
public final class ThaumicOverworldBiomeHolders {
    private static volatile Holder<Biome> magicalForest;
    private static volatile Holder<Biome> taintedLands;
    private static volatile long worldSeed;

    private ThaumicOverworldBiomeHolders() {
    }

    public static void capture(RegistryAccess registryAccess, long seed) {
        var biomes = registryAccess.registryOrThrow(Registries.BIOME);
        magicalForest = biomes.getHolderOrThrow(
                ModWorldgenKeys.MAGICAL_FOREST
        );
        taintedLands = biomes.getHolderOrThrow(
                ModWorldgenKeys.TAINTED_LANDS
        );
        worldSeed = seed;
        ThaumcraftModern.LOGGER.info(
                "Resolved TC4 Overworld biome holders from the server registry"
        );
    }

    public static Holder<Biome> magicalForest() {
        return magicalForest;
    }

    public static Holder<Biome> taintedLands() {
        return taintedLands;
    }

    public static long worldSeed() {
        return worldSeed;
    }
}
