package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.config.ThaumicOverworldConfig;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;

/** Applies the TC4 regional mask after the active biome source chose a biome. */
public final class ThaumicRegionalBiomeSelector {
    private ThaumicRegionalBiomeSelector() {
    }

    public static Holder<Biome> select(
            int quartX,
            int quartZ,
            Holder<Biome> original
    ) {
        if (!ThaumicOverworldConfig.generateTc4Biomes()
                || preserve(original)) {
            return original;
        }
        ThaumicBiomeRegionMask.Selection selection =
                ThaumicBiomeRegionMask.select(
                        quartX,
                        quartZ,
                        ThaumicOverworldBiomeHolders.worldSeed(),
                        ThaumicOverworldConfig.magicalForestWeight(),
                        ThaumicOverworldConfig.taintedLandsWeight()
                );
        if (selection == ThaumicBiomeRegionMask.Selection.MAGICAL_FOREST) {
            Holder<Biome> magicalForest =
                    ThaumicOverworldBiomeHolders.magicalForest();
            if (magicalForest != null
                    && !original.is(BiomeTags.IS_MOUNTAIN)
                    && MagicalForestGenerationPolicy.supportsClimate(
                            original.value().getBaseTemperature(),
                            original.is(Tags.Biomes.IS_COLD)
                    )) {
                return magicalForest;
            }
        } else if (selection
                == ThaumicBiomeRegionMask.Selection.TAINTED_LANDS) {
            Holder<Biome> taintedLands =
                    ThaumicOverworldBiomeHolders.taintedLands();
            if (taintedLands != null) {
                return taintedLands;
            }
        }
        return original;
    }

    private static boolean preserve(Holder<Biome> original) {
        return original.is(BiomeTags.IS_OCEAN)
                || original.is(BiomeTags.IS_RIVER)
                || original.is(BiomeTags.IS_NETHER)
                || original.is(BiomeTags.IS_END)
                || original.is(Biomes.LUSH_CAVES)
                || original.is(Biomes.DRIPSTONE_CAVES)
                || original.is(Biomes.DEEP_DARK);
    }
}
