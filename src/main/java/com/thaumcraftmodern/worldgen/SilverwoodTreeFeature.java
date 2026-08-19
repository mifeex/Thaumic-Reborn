package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * TC4 Silverwood geometry: a five-wide cross trunk, flared roots, luminous
 * spherical crown and occasional pure node embedded in the trunk.
 */
public final class SilverwoodTreeFeature
        extends Feature<NoneFeatureConfiguration> {
    private static final int[][] TRUNK_SUPPORT = {{0, 0}};
    private static final int[][] NATURAL_ROOT_SUPPORTS = {
            {0, 0},
            {-1, 0}, {1, 0}, {0, -1}, {0, 1},
            {-2, 0}, {2, 0}, {0, -2}, {0, 2}
    };

    public SilverwoodTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return placeTree(
                context.level(),
                context.origin(),
                context.random(),
                false
        );
    }

    public static boolean placeTree(
            WorldGenLevel level,
            BlockPos origin,
            RandomSource random,
            boolean wild
    ) {
        int height = 7 + random.nextInt(4);
        int[][] supports = wild ? NATURAL_ROOT_SUPPORTS : TRUNK_SUPPORT;
        if (!TreeSitePolicy.hasDrySupportedSoil(
                    level,
                    origin,
                    supports
                )
                || (wild && !hasDryReplaceableRoots(level, origin))
                || (wild && !TreeSitePolicy.hasDryDirtCoverage(
                        level,
                        origin,
                        MagicalForestGenerationPolicy
                                .SILVERWOOD_GROUND_RADIUS,
                        MagicalForestGenerationPolicy
                                .SILVERWOOD_MIN_GROUND_PERCENT
                ))
                || level.isOutsideBuildHeight(origin.above(height + 4))) {
            return false;
        }
        for (int y = 1; y <= height + 3; y++) {
            /*
             * Match TC4's permissive site test: reserve the cross-shaped
             * trunk and only the inner crown. Leaves already skip solid
             * blocks while being painted, so requiring an empty 11x11 crown
             * rejected almost every wild tree on slopes or in dense forest.
             */
            int radius = MagicalForestGenerationPolicy
                    .silverwoodClearanceRadius(y, height);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!state.isAir()
                            && !state.is(BlockTags.LEAVES)
                            && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }

        BlockState vertical = ModBlocks.SILVERWOOD_LOG.get()
                .defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y);
        int nodeChanceBound =
                SilverwoodNodeGeneration.initialChanceBound(height);
        int nodeCount = 0;
        for (int y = 0; y < height; y++) {
            if (SilverwoodNodeGeneration.shouldPlaceForTree(
                            wild,
                            y,
                            nodeCount,
                            random.nextInt(nodeChanceBound)
                    )) {
                BlockPos nodePos = origin.above(y);
                level.setBlock(
                        nodePos,
                        ModBlocks.SILVERWOOD_NODE.get().defaultBlockState(),
                        2
                );
                if (level.getBlockEntity(nodePos)
                        instanceof AuraNodeBlockEntity node) {
                    node.initializeOnce(
                            ClassicAuraNodeWorldFactory.createSilverwood(
                                    level,
                                    nodePos,
                                    random
                            )
                    );
                }
                nodeCount++;
                nodeChanceBound = SilverwoodNodeGeneration.nextChanceBound(
                        nodeChanceBound,
                        height
                );
            } else {
                level.setBlock(origin.above(y), vertical, 2);
            }
            level.setBlock(origin.offset(-1, y, 0), vertical, 2);
            level.setBlock(origin.offset(1, y, 0), vertical, 2);
            level.setBlock(origin.offset(0, y, -1), vertical, 2);
            level.setBlock(origin.offset(0, y, 1), vertical, 2);
        }

        for (int x = -2; x <= 2; x++) {
            if (x != 0) {
                level.setBlock(origin.offset(x, 0, 0), vertical, 2);
                level.setBlock(origin.offset(0, 0, x), vertical, 2);
            }
        }

        BlockPos crown = origin.above(height);
        for (int x = -5; x <= 5; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -5; z <= 5; z++) {
                    double distance = x * x + z * z + y * y * 1.35D;
                    if (distance < 17.0D + random.nextInt(8)) {
                        BlockPos leafPos = crown.offset(x, y, z);
                        BlockState state = level.getBlockState(leafPos);
                        if (state.isAir() || state.canBeReplaced()) {
                            level.setBlock(
                                    leafPos,
                                    ModBlocks.SILVERWOOD_LEAVES.get()
                                            .defaultBlockState()
                                            .setValue(
                                                    BlockStateProperties.PERSISTENT,
                                                    false
                                            ),
                                    2
                            );
                        }
                    }
                }
            }
        }

        if (wild) {
            /*
             * TC4 invokes WorldGenCustomFlowers for every wild Silverwood:
             * eighteen nearby attempts form the characteristic Shimmerleaf
             * ring instead of a few unrelated biome-wide flowers.
             */
            for (int attempt = 0;
                 attempt
                         < MagicalForestGenerationPolicy.SHIMMERLEAF_ATTEMPTS;
                 attempt++) {
                BlockPos flower = origin.offset(
                        random.nextInt(8) - random.nextInt(8),
                        0,
                        random.nextInt(8) - random.nextInt(8)
                );
                BlockPos surface = LegacyVegetationFeature.groundSurface(
                        level,
                        flower.getX(),
                        flower.getZ()
                );
                BlockState shimmerleaf = ModBlocks.SHIMMERLEAF.get()
                        .defaultBlockState();
                if (level.isEmptyBlock(surface)
                        && shimmerleaf.canSurvive(level, surface)) {
                    level.setBlock(
                            surface,
                            shimmerleaf,
                            2
                    );
                }
            }
        }
        return true;
    }

    private static boolean hasDryReplaceableRoots(
            WorldGenLevel level,
            BlockPos origin
    ) {
        for (int[] offset : NATURAL_ROOT_SUPPORTS) {
            BlockPos root = origin.offset(offset[0], 0, offset[1]);
            BlockState state = level.getBlockState(root);
            if (!level.getFluidState(root).isEmpty()
                    || (!state.isAir()
                    && !state.is(BlockTags.LEAVES)
                    && !state.canBeReplaced())) {
                return false;
            }
        }
        return true;
    }

}
