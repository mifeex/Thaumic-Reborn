package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

/**
 * TC4's vegetation pass kept tree frequency separate from biome decoration.
 * This feature preserves that contract while allowing modern biomes and cave
 * terrain to remain untouched.
 */
public final class LegacyVegetationFeature
        extends Feature<NoneFeatureConfiguration> {
    public LegacyVegetationFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkMinX = Math.floorDiv(context.origin().getX(), 16) * 16;
        int chunkMinZ = Math.floorDiv(context.origin().getZ(), 16) * 16;
        BlockPos center = surface(
                level,
                chunkMinX + 8,
                chunkMinZ + 8
        );
        Holder<Biome> biome = level.getBiome(center);
        if (biome.is(BiomeTags.IS_OCEAN)
                || biome.is(BiomeTags.IS_RIVER)
                || biome.is(BiomeTags.IS_END)
                || biome.is(BiomeTags.IS_NETHER)) {
            return false;
        }

        boolean placed = false;
        boolean magicalForest = biome.is(ModWorldgenKeys.MAGICAL_FOREST);
        boolean taintedLands = biome.is(ModWorldgenKeys.TAINTED_LANDS);
        if (magicalForest) {
            placed |= placeMagicalForestBoulders(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            placed |= placeMagicalForestMushrooms(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
        }
        if (taintedLands) {
            placed |= placeTaintedSoilPatches(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            placed |= placeTaintedLandBlobs(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
        }

        if (ThaumcraftModernServerConfig.generateTrees()) {
            if (magicalForest) {
                placed |= placeMagicalForestTrees(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
            }
            if (taintedLands) {
                placed |= placeInfectedTrees(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
            }
            if (!magicalForest && !taintedLands && random.nextInt(
                    ThaumcraftModernServerConfig.silverwoodRarity()
            ) == 3 % ThaumcraftModernServerConfig.silverwoodRarity()
                    && biome.is(Tags.Biomes.IS_MAGICAL)) {
                BlockPos origin = randomSurface(level, random, chunkMinX, chunkMinZ);
                placed |= placeWildSilverwood(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ,
                        origin
                );
            }

            if (!magicalForest && !taintedLands && random.nextInt(
                    ThaumcraftModernServerConfig.greatwoodRarity()
            ) == 7 % ThaumcraftModernServerConfig.greatwoodRarity()) {
                float support = greatwoodSupport(biome);
                if (random.nextFloat() < support) {
                    BlockPos origin = randomSurface(
                            level,
                            random,
                            chunkMinX,
                            chunkMinZ
                    );
                    placed |= GreatwoodTreeFeature.placeTree(
                            level,
                            origin,
                            random,
                            true
                    );
                }
            }
        }

        if (ThaumcraftModernServerConfig.generatePlants()) {
            if (biome.is(Tags.Biomes.IS_HOT)
                    && random.nextInt(30) == 0) {
                BlockPos origin = randomSurface(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
                placed |= flowerPatch(
                        level,
                        origin,
                        random,
                        ModBlocks.CINDERPEARL.get()
                );
            }
            if (magicalForest) {
                placed |= placeMagicalForestGroundCover(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
                for (int attempt = 0;
                     attempt < MagicalForestGenerationPolicy.VISHROOM_ATTEMPTS;
                     attempt++) {
                    BlockPos origin = randomGroundSurface(
                            level,
                            random,
                            chunkMinX,
                            chunkMinZ
                    );
                    placed |= adjacentToLog(level, origin)
                            && placePlant(
                                    level,
                                    origin,
                                    ModBlocks.VISHROOM.get()
                            );
                }
                for (int attempt = 0;
                     attempt < MagicalForestGenerationPolicy.MANA_POD_ATTEMPTS;
                     attempt++) {
                    placed |= placeManaPod(
                            level,
                            random,
                            chunkMinX,
                            chunkMinZ
                    );
                }
            }
            if (taintedLands) {
                placed |= placeTaintedGroundCover(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
                placed |= placeTaintedLandFibres(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
                placed |= placeTaintedPlants(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ
                );
            }
        }
        return placed;
    }

    /**
     * Keeps TC4's selector order while using the tuned Magical Forest density:
     * Silverwood first, Greatwood second, then the big magic oak. The reduced
     * attempt count opens the oak canopy; stronger rare rolls compensate for
     * modern clearance failures around the much larger landmark trees.
     */
    private static boolean placeMagicalForestTrees(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        for (int attempt = 0;
             attempt < MagicalForestGenerationPolicy.TREE_ATTEMPTS;
             attempt++) {
            BlockPos origin = randomGroundSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            if (random.nextInt(
                    MagicalForestGenerationPolicy.SILVERWOOD_CHANCE
            ) == 0) {
                placed |= placeWildSilverwood(
                        level,
                        random,
                        chunkMinX,
                        chunkMinZ,
                        origin
                );
            } else if (random.nextInt(
                    MagicalForestGenerationPolicy
                            .GREATWOOD_CHANCE_AFTER_SILVERWOOD
            ) == 0) {
                placed |= GreatwoodTreeFeature.placeTree(
                        level,
                        origin,
                        random,
                        true
                );
            } else {
                placed |= BigMagicOakTreeFeature.placeTree(
                        level,
                        origin,
                        random
                );
            }
        }
        return placed;
    }

    private static boolean placeWildSilverwood(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ,
            BlockPos firstOrigin
    ) {
        if (SilverwoodTreeFeature.placeTree(
                level,
                firstOrigin,
                random,
                true
        )) {
            return true;
        }
        for (int attempt = 1;
             attempt < MagicalForestGenerationPolicy
                     .SILVERWOOD_SITE_ATTEMPTS;
             attempt++) {
            BlockPos origin = randomGroundSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            if (SilverwoodTreeFeature.placeTree(
                    level,
                    origin,
                    random,
                    true
            )) {
                return true;
            }
        }
        return false;
    }

    /**
     * TC4 generated zero to two rock blobs per Magical Forest chunk. The
     * modern profile uses the mossy-cobblestone material of taiga boulders,
     * matching the requested classic forest landmark.
     */
    private static boolean placeMagicalForestBoulders(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        int boulders = random.nextInt(
                MagicalForestGenerationPolicy.BOULDER_VARIANTS
        );
        for (int attempt = 0; attempt < boulders; attempt++) {
            BlockPos surface = randomGroundSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            if (!level.getBlockState(surface.below()).is(BlockTags.DIRT)) {
                continue;
            }
            BlockPos center = surface;
            for (int blob = 0; blob < 3; blob++) {
                int radiusX = 1 + random.nextInt(2);
                int radiusY = 1 + random.nextInt(2);
                int radiusZ = 1 + random.nextInt(2);
                for (int x = -radiusX; x <= radiusX; x++) {
                    for (int y = -radiusY; y <= radiusY; y++) {
                        for (int z = -radiusZ; z <= radiusZ; z++) {
                            double distance =
                                    x * x / (double) (radiusX * radiusX)
                                    + y * y / (double) (radiusY * radiusY)
                                    + z * z / (double) (radiusZ * radiusZ);
                            if (distance <= 1.0D) {
                                BlockPos position = center.offset(x, y, z);
                                BlockState current = level.getBlockState(position);
                                if (!current.is(BlockTags.LOGS)
                                        && !current.is(BlockTags.LEAVES)) {
                                    placed |= level.setBlock(
                                            position,
                                            Blocks.MOSSY_COBBLESTONE
                                                    .defaultBlockState(),
                                            2
                                    );
                                }
                            }
                        }
                    }
                }
                center = center.offset(
                        random.nextInt(3) - 1,
                        -random.nextInt(2),
                        random.nextInt(3) - 1
                );
            }
        }
        return placed;
    }

    /**
     * TC4 generated {@code random.nextInt(3)} WorldGenBlockBlob formations
     * made from metadata-0 BlockTaint (Crusted Taint) per biome chunk.
     */
    private static boolean placeTaintedLandBlobs(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        int blobs = random.nextInt(
                TaintedLandsGenerationPolicy.TAINT_BLOB_VARIANTS
        );
        for (int attempt = 0; attempt < blobs; attempt++) {
            BlockPos surface = randomGroundSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            if (!level.getBlockState(surface.below()).is(BlockTags.DIRT)
                    && !level.getBlockState(surface.below()).is(
                            ModBlocks.TAINTED_SOIL.get()
                    )
                    && !level.getBlockState(surface.below()).is(
                            ModBlocks.CRUSTED_TAINT.get()
                    )
                    && !level.getBlockState(surface.below()).is(Blocks.STONE)) {
                continue;
            }
            BlockPos center = surface;
            for (int blob = 0; blob < 3; blob++) {
                int radiusX = 1 + random.nextInt(2);
                int radiusY = 1 + random.nextInt(2);
                int radiusZ = 1 + random.nextInt(2);
                for (int x = -radiusX; x <= radiusX; x++) {
                    for (int y = -radiusY; y <= radiusY; y++) {
                        for (int z = -radiusZ; z <= radiusZ; z++) {
                            double distance =
                                    x * x / (double) (radiusX * radiusX)
                                    + y * y / (double) (radiusY * radiusY)
                                    + z * z / (double) (radiusZ * radiusZ);
                            if (distance <= 1.0D) {
                                BlockPos position = center.offset(x, y, z);
                                BlockState current = level.getBlockState(position);
                                if (!current.is(BlockTags.LOGS)
                                        && !current.is(BlockTags.LEAVES)) {
                                    placed |= level.setBlock(
                                            position,
                                            ModBlocks.CRUSTED_TAINT.get()
                                                    .defaultBlockState(),
                                            2
                                    );
                                }
                            }
                        }
                    }
                }
                center = center.offset(
                        random.nextInt(3) - 1,
                        -random.nextInt(2),
                        random.nextInt(3) - 1
                );
            }
        }
        return placed;
    }

    private static boolean placeTaintedSoilPatches(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        for (int attempt = 0;
             attempt
                     < TaintedLandsGenerationPolicy
                             .TAINTED_SOIL_PATCH_ATTEMPTS;
             attempt++) {
            BlockPos origin = randomGroundSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            ).below();
            int radius = 1 + random.nextInt(3);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z > radius * radius
                            || random.nextInt(5) == 0) {
                        continue;
                    }
                    BlockPos surface = groundSurface(
                            level,
                            origin.getX() + x,
                            origin.getZ() + z
                    ).below();
                    BlockState state = level.getBlockState(surface);
                    if (state.is(BlockTags.DIRT)
                            || state.is(Blocks.STONE)
                            || state.is(BlockTags.SAND)) {
                        placed |= level.setBlock(
                                surface,
                                ModBlocks.TAINTED_SOIL.get()
                                        .defaultBlockState(),
                                2
                        );
                    }
                }
            }
        }
        return placed;
    }

    /**
     * A generated snapshot of TC4's spreading ecology. Stage zero has mostly
     * intact oak, stage one is visibly colonised, and stage two is close to
     * the original conversion endpoint where logs and leaves become crust.
     */
    private static boolean placeInfectedTrees(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.INFECTED_TREE_ATTEMPTS;
             attempt++) {
            BlockPos origin = randomGroundSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            placed |= placeInfectedTree(
                    level,
                    origin,
                    random,
                    random.nextInt(
                            TaintedLandsGenerationPolicy.INFECTED_TREE_STAGES
                    )
            );
        }
        return placed;
    }

    private static boolean placeInfectedTree(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            int stage
    ) {
        BlockState ground = level.getBlockState(origin.below());
        if (!TreeSitePolicy.hasDrySupportedBase(level, origin)
                || (!ground.is(BlockTags.DIRT)
                && !ground.is(ModBlocks.TAINTED_SOIL.get())
                && !ground.is(ModBlocks.CRUSTED_TAINT.get()))) {
            return false;
        }
        int height = 4 + random.nextInt(3);
        for (int y = 0; y <= height + 2; y++) {
            int radius = y < height - 2 ? 0 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockState current = level.getBlockState(
                            origin.offset(x, y, z)
                    );
                    if (!current.isAir()
                            && !current.is(BlockTags.LEAVES)
                            && !current.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        double trunkInfection = switch (stage) {
            case 0 -> 0.08D;
            case 1 -> 0.28D;
            default -> 0.55D;
        };
        double leafInfection = switch (stage) {
            case 0 -> 0.15D;
            case 1 -> 0.38D;
            default -> 0.68D;
        };
        BlockState crust = ModBlocks.CRUSTED_TAINT.get().defaultBlockState();
        for (int y = 0; y < height; y++) {
            level.setBlock(
                    origin.above(y),
                    random.nextDouble() < trunkInfection
                            ? crust
                            : Blocks.OAK_LOG.defaultBlockState(),
                    2
            );
        }
        BlockState leaves = ModBlocks.TAINTED_LEAVES.get()
                .defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, true);
        for (int y = height - 2; y <= height + 1; y++) {
            int radius = y == height + 1 ? 1 : 2;
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) == radius
                            && Math.abs(z) == radius
                            && random.nextBoolean()) {
                        continue;
                    }
                    BlockPos position = origin.offset(x, y, z);
                    if (level.getBlockState(position).isAir()) {
                        level.setBlock(
                                position,
                                random.nextDouble() < leafInfection
                                        ? crust
                                        : leaves,
                                2
                        );
                    }
                }
            }
        }
        for (int fibre = 0; fibre < 8 + stage * 6; fibre++) {
            BlockPos position = origin.offset(
                    random.nextInt(7) - 3,
                    random.nextInt(height + 3),
                    random.nextInt(7) - 3
            );
            if (level.isEmptyBlock(position)) {
                placeTaintFibres(level, position);
            }
        }
        return true;
    }

    /** Keeps TC4's four-by-four giant-mushroom grid and one-in-forty roll. */
    private static boolean placeMagicalForestMushrooms(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        for (int gridX = 0;
             gridX < MagicalForestGenerationPolicy.GIANT_MUSHROOM_GRID_SIZE;
             gridX++) {
            for (int gridZ = 0;
                 gridZ < MagicalForestGenerationPolicy
                         .GIANT_MUSHROOM_GRID_SIZE;
                 gridZ++) {
                if (random.nextInt(
                        MagicalForestGenerationPolicy.GIANT_MUSHROOM_CHANCE
                ) != 0) {
                    continue;
                }
                BlockPos origin = groundSurface(
                        level,
                        chunkMinX + gridX * 4 + 1 + random.nextInt(3),
                        chunkMinZ + gridZ * 4 + 1 + random.nextInt(3)
                );
                placed |= placeHugeMushroom(
                        level,
                        origin,
                        random,
                        random.nextBoolean()
                );
            }
        }
        return placed;
    }

    private static boolean placeHugeMushroom(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            boolean red
    ) {
        ConfiguredFeature<?, ?> mushroom = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(red
                        ? TreeFeatures.HUGE_RED_MUSHROOM
                        : TreeFeatures.HUGE_BROWN_MUSHROOM)
                .value();
        return mushroom.place(
                level,
                level.getLevel().getChunkSource().getGenerator(),
                random,
                origin
        );
    }

    /**
     * Reproduces the Magical Forest's BiomeDecorator counts instead of
     * stacking the equivalent modern placed features on top of this pass.
     */
    private static boolean placeMagicalForestGroundCover(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        Block[] flowers = {
                Blocks.DANDELION,
                Blocks.POPPY,
                Blocks.BLUE_ORCHID,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.RED_TULIP,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY
        };
        for (int attempt = 0;
             attempt < MagicalForestGenerationPolicy.FLOWER_ATTEMPTS;
             attempt++) {
            placed |= placePlantPatch(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    random,
                    flowers[random.nextInt(flowers.length)],
                    64,
                    8
            );
        }
        for (int attempt = 0;
             attempt < MagicalForestGenerationPolicy.GRASS_ATTEMPTS;
             attempt++) {
            Block grass = random.nextInt(
                    MagicalForestGenerationPolicy.FERN_CHANCE
            ) == 0 ? Blocks.FERN : Blocks.GRASS;
            placed |= placePlantPatch(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    random,
                    grass,
                    128,
                    8
            );
        }
        for (int attempt = 0;
             attempt
                     < MagicalForestGenerationPolicy.NORMAL_MUSHROOM_ATTEMPTS;
             attempt++) {
            Block mushroom = random.nextBoolean()
                    ? Blocks.BROWN_MUSHROOM
                    : Blocks.RED_MUSHROOM;
            placed |= placePlantPatch(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    random,
                    mushroom,
                    64,
                    8
            );
        }
        for (int attempt = 0;
             attempt < MagicalForestGenerationPolicy.REED_ATTEMPTS;
             attempt++) {
            placed |= placeSugarCane(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
        }
        return placed;
    }

    /**
     * Original BiomeDecorator values: two flower patches, two grass patches,
     * no trees and no normal mushroom patches.
     */
    private static boolean placeTaintedGroundCover(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        Block[] flowers = {
                Blocks.DANDELION,
                Blocks.POPPY,
                Blocks.BLUE_ORCHID,
                Blocks.ALLIUM,
                Blocks.AZURE_BLUET,
                Blocks.RED_TULIP,
                Blocks.ORANGE_TULIP,
                Blocks.WHITE_TULIP,
                Blocks.PINK_TULIP,
                Blocks.OXEYE_DAISY
        };
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.FLOWER_ATTEMPTS;
             attempt++) {
            placed |= placePlantPatch(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    random,
                    flowers[random.nextInt(flowers.length)],
                    64,
                    8
            );
        }
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.GRASS_ATTEMPTS;
             attempt++) {
            placed |= placePlantPatch(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    random,
                    Blocks.GRASS,
                    128,
                    8
            );
        }
        return placed;
    }

    /**
     * Keeps the two distinct TC4 fibre passes. The first only seeds fibres
     * directly over grass; the second accepts any exposed solid face.
     */
    private static boolean placeTaintedLandFibres(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.SURFACE_FIBRE_ATTEMPTS;
             attempt++) {
            BlockPos position = randomSurface(
                    level,
                    random,
                    chunkMinX,
                    chunkMinZ
            );
            if (level.getBlockState(position.below()).is(Blocks.GRASS_BLOCK)) {
                placed |= placeTaintFibres(level, position);
            }
        }
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.SPREAD_FIBRE_ATTEMPTS;
             attempt++) {
            placed |= placeTaintFibres(
                    level,
                    randomSurface(level, random, chunkMinX, chunkMinZ)
            );
        }
        return placed;
    }

    private static boolean placeTaintedPlants(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        boolean placed = false;
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.TAINTED_PLANT_ATTEMPTS;
             attempt++) {
            Block plant = random.nextInt(4) == 0
                    ? ModBlocks.TALL_TAINTED_GRASS.get()
                    : ModBlocks.SHORT_TAINTED_GRASS.get();
            placed |= placePlant(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    plant
            );
        }
        for (int attempt = 0;
             attempt < TaintedLandsGenerationPolicy.SPORE_STALK_ATTEMPTS;
             attempt++) {
            Block plant = random.nextInt(4) == 0
                    ? ModBlocks.MATURE_SPORE_STALK.get()
                    : ModBlocks.SPORE_STALK.get();
            placed |= placePlant(
                    level,
                    randomGroundSurface(level, random, chunkMinX, chunkMinZ),
                    plant
            );
        }
        return placed;
    }

    private static boolean placePlantPatch(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Block plant,
            int attempts,
            int spread
    ) {
        boolean placed = false;
        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos position = origin.offset(
                    random.nextInt(spread) - random.nextInt(spread),
                    random.nextInt(4) - random.nextInt(4),
                    random.nextInt(spread) - random.nextInt(spread)
            );
            placed |= placePlant(level, position, plant);
        }
        return placed;
    }

    private static boolean placeSugarCane(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        BlockPos patchOrigin = randomSurface(
                level,
                random,
                chunkMinX,
                chunkMinZ
        );
        boolean placed = false;
        for (int candidate = 0; candidate < 20; candidate++) {
            int x = patchOrigin.getX()
                    + random.nextInt(4) - random.nextInt(4);
            int z = patchOrigin.getZ()
                    + random.nextInt(4) - random.nextInt(4);
            BlockPos origin = surface(level, x, z);
            int height = 2 + random.nextInt(3);
            for (int y = 0; y < height; y++) {
                BlockPos position = origin.above(y);
                BlockState cane = Blocks.SUGAR_CANE.defaultBlockState();
                if (!level.isEmptyBlock(position)
                        || !cane.canSurvive(level, position)) {
                    break;
                }
                placed |= level.setBlock(position, cane, 2);
            }
        }
        return placed;
    }

    private static boolean adjacentToLog(
            WorldGenLevel level,
            BlockPos position
    ) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if ((x != 0 || y != 0 || z != 0)
                            && level.getBlockState(position.offset(x, y, z))
                            .is(BlockTags.LOGS)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static float greatwoodSupport(Holder<Biome> biome) {
        if (biome.is(Tags.Biomes.IS_MAGICAL)
                || biome.is(BiomeTags.IS_FOREST)) {
            return 1.0F;
        }
        if (biome.is(Tags.Biomes.IS_LUSH)) {
            return 0.5F;
        }
        if (biome.is(Tags.Biomes.IS_CONIFEROUS)
                || biome.is(Tags.Biomes.IS_PLAINS)
                || biome.is(BiomeTags.IS_SAVANNA)
                || biome.is(Tags.Biomes.IS_SWAMP)) {
            return 0.2F;
        }
        return 0.0F;
    }

    private static boolean flowerPatch(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            Block plant
    ) {
        boolean placed = false;
        for (int attempt = 0; attempt < 18; attempt++) {
            int x = origin.getX() + random.nextInt(8) - random.nextInt(8);
            int z = origin.getZ() + random.nextInt(8) - random.nextInt(8);
            BlockPos position = surface(level, x, z);
            placed |= placePlant(level, position, plant);
        }
        return placed;
    }

    private static boolean placePlant(
            WorldGenLevel level,
            BlockPos position,
            Block plant
    ) {
        BlockState state = plant.defaultBlockState();
        if (!level.isEmptyBlock(position)
                || !state.canSurvive(level, position)) {
            return false;
        }
        return level.setBlock(position, state, 2);
    }

    private static boolean placeTaintFibres(
            WorldGenLevel level,
            BlockPos position
    ) {
        if (!level.isEmptyBlock(position)) {
            return false;
        }
        MultifaceBlock fibres = (MultifaceBlock) ModBlocks.TAINT_FIBRES.get();
        BlockState state = level.getBlockState(position);
        for (Direction direction : Direction.values()) {
            BlockState withFace = fibres.getStateForPlacement(
                    state,
                    level,
                    position,
                    direction
            );
            if (withFace != null) {
                state = withFace;
            }
        }
        return state.is(fibres) && level.setBlock(position, state, 2);
    }

    private static boolean placeManaPod(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        int x = chunkMinX + random.nextInt(16);
        int z = chunkMinZ + random.nextInt(16);
        int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        for (int y = surface; y < Math.min(surface + 24, level.getMaxBuildHeight()); y++) {
            BlockPos position = new BlockPos(x, y, z);
            BlockState above = level.getBlockState(position.above());
            if (level.isEmptyBlock(position)
                    && (above.is(BlockTags.LOGS)
                    || above.is(ModBlocks.GREATWOOD_LOG.get())
                    || above.is(ModBlocks.SILVERWOOD_LOG.get()))) {
                BlockState pod = ModBlocks.MANA_POD.get()
                        .defaultBlockState()
                        .setValue(
                                com.thaumcraftmodern.world.block.ManaPodBlock.AGE,
                                3 + random.nextInt(5)
                );
                if (pod.canSurvive(level, position)) {
                    if (!level.setBlock(position, pod, 2)) {
                        return false;
                    }
                    if (level.getBlockEntity(position)
                            instanceof com.thaumcraftmodern.world.block.entity.ManaPodBlockEntity
                            manaPod) {
                        manaPod.initializeWorldgen(random);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static BlockPos randomSurface(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        return surface(
                level,
                chunkMinX + random.nextInt(16),
                chunkMinZ + random.nextInt(16)
        );
    }

    private static BlockPos randomGroundSurface(
            WorldGenLevel level,
            RandomSource random,
            int chunkMinX,
            int chunkMinZ
    ) {
        return groundSurface(
                level,
                chunkMinX + random.nextInt(16),
                chunkMinZ + random.nextInt(16)
        );
    }

    static BlockPos groundSurface(WorldGenLevel level, int x, int z) {
        int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        for (int y = top; y > level.getMinBuildHeight(); y--) {
            BlockPos ground = new BlockPos(x, y - 1, z);
            BlockState state = level.getBlockState(ground);
            if (state.is(BlockTags.DIRT)
                    || state.is(ModBlocks.TAINTED_SOIL.get())
                    || state.is(ModBlocks.CRUSTED_TAINT.get())) {
                return ground.above();
            }
        }
        return new BlockPos(x, top, z);
    }

    private static BlockPos surface(WorldGenLevel level, int x, int z) {
        return new BlockPos(
                x,
                level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z),
                z
        );
    }
}
