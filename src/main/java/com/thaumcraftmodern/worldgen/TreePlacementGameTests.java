package com.thaumcraftmodern.worldgen;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class TreePlacementGameTests {
    private TreePlacementGameTests() {
    }

    @GameTest(
            template = "empty",
            batch = "treeSitePolicy",
            timeoutTicks = 100
    )
    public static void thaumcraftTreesRejectFloodedPlantingCells(
            GameTestHelper helper
    ) {
        BlockPos silverwood = helper.absolutePos(new BlockPos(7, 8, 7));
        BlockPos greatwood = helper.absolutePos(new BlockPos(17, 8, 7));
        BlockPos magicOak = helper.absolutePos(new BlockPos(27, 8, 7));
        prepareFloodedSoil(helper, silverwood, 3);
        prepareFloodedSoil(helper, greatwood, 3);
        prepareFloodedSoil(helper, magicOak, 3);

        helper.assertTrue(
                !SilverwoodTreeFeature.placeTree(
                        helper.getLevel(),
                        silverwood,
                        RandomSource.create(91L),
                        true
                ),
                "Silverwood spawned in water"
        );
        helper.assertTrue(
                !GreatwoodTreeFeature.placeTree(
                        helper.getLevel(),
                        greatwood,
                        RandomSource.create(92L),
                        true
                ),
                "Greatwood spawned in water"
        );
        helper.assertTrue(
                !BigMagicOakTreeFeature.placeTree(
                        helper.getLevel(),
                        magicOak,
                        RandomSource.create(93L)
                ),
                "Big magic oak spawned in water"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "treeSitePolicy",
            timeoutTicks = 100
    )
    public static void silverwoodRejectsUnsupportedRoot(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(12, 8, 12));
        prepareDrySoil(helper, origin, 3);
        helper.setBlock(origin.offset(2, -2, 0), Blocks.AIR);

        helper.assertTrue(
                !SilverwoodTreeFeature.placeTree(
                        helper.getLevel(),
                        origin,
                        RandomSource.create(94L),
                        true
                ),
                "Silverwood spawned with a floating cardinal root"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "treeSitePolicy",
            timeoutTicks = 100
    )
    public static void wildSilverwoodRejectsStoneRootSoil(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(12, 8, 12));
        prepareDrySoil(helper, origin, 3);
        helper.setBlock(origin.offset(2, -1, 0), Blocks.STONE);

        helper.assertTrue(
                !SilverwoodTreeFeature.placeTree(
                        helper.getLevel(),
                        origin,
                        RandomSource.create(95L),
                        true
                ),
                "Wild Silverwood spawned with a root planted on stone"
        );
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "treeSitePolicy",
            timeoutTicks = 100
    )
    public static void wildSilverwoodRequiresEightyPercentDryGround(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(12, 8, 12));
        prepareDrySoil(helper, origin, 3);
        int[][] floodedNonRootCells = {
                {-2, -2}, {-2, -1}, {-2, 1},
                {2, -2}, {2, -1}, {2, 1}
        };
        for (int[] offset : floodedNonRootCells) {
            helper.setBlock(
                    origin.offset(offset[0], 0, offset[1]),
                    Blocks.WATER
            );
        }

        helper.assertTrue(
                !SilverwoodTreeFeature.placeTree(
                        helper.getLevel(),
                        origin,
                        RandomSource.create(96L),
                        true
                ),
                "Wild Silverwood spawned with only 76 percent dry ground"
        );
        helper.succeed();
    }

    private static void prepareFloodedSoil(
            GameTestHelper helper,
            BlockPos origin,
            int radius
    ) {
        prepareDrySoil(helper, origin, radius);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                helper.getLevel().setBlock(
                        origin.offset(x, 0, z),
                        Blocks.WATER.defaultBlockState(),
                        2
                );
            }
        }
    }

    private static void prepareDrySoil(
            GameTestHelper helper,
            BlockPos origin,
            int radius
    ) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                helper.getLevel().setBlock(
                        origin.offset(x, -1, z),
                        Blocks.DIRT.defaultBlockState(),
                        2
                );
                helper.getLevel().setBlock(
                        origin.offset(x, -2, z),
                        Blocks.STONE.defaultBlockState(),
                        2
                );
            }
        }
    }
}
