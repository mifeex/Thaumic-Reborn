package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OuterLandsStairTopologyGameTests {
    private OuterLandsStairTopologyGameTests() {
    }

    @GameTest(template = "empty", batch = "outerLands", timeoutTicks = 40)
    public static void fillsMissingStairBetweenPerpendicularRuns(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 2));
        BlockPos gap = new BlockPos(
                origin.getX(),
                OuterLandsLabyrinthGenerator.BASE_Y + 3,
                origin.getZ()
        );
        BlockState stair = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.HALF, Half.BOTTOM);
        helper.getLevel().setBlock(
                gap,
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                gap.east(),
                stair.setValue(StairBlock.FACING, Direction.NORTH),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                gap.south(),
                stair.setValue(StairBlock.FACING, Direction.WEST),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                gap.north(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );

        OuterLandsStairTopology.refresh(
                helper.getLevel(), new ChunkPos(gap)
        );

        BlockState filled = helper.getLevel().getBlockState(gap);
        helper.assertTrue(filled.is(ModBlocks.ANCIENT_STAIRS.get()),
                "Perpendicular stair runs kept an empty corner");
        helper.assertTrue(
                filled.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT,
                "Inserted stair was not resolved as a corner"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "outerLands", timeoutTicks = 40)
    public static void removesUpperAndLowerStairsTouchingEldritchNothing(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos lower = new BlockPos(
                origin.getX(),
                OuterLandsLabyrinthGenerator.BASE_Y + 3,
                origin.getZ()
        );
        BlockPos upper = lower.above(4);
        placeLeakingRun(helper, lower, Half.BOTTOM);
        placeLeakingRun(helper, upper, Half.TOP);

        OuterLandsStairTopology.refresh(
                helper.getLevel(), new ChunkPos(lower)
        );

        for (BlockPos removed : new BlockPos[]{lower, upper}) {
            helper.assertTrue(helper.getLevel().getBlockState(removed)
                            .is(ModBlocks.ANCIENT_STONE.get()),
                    "Stair touching Eldritch Nothing was not replaced by masonry");
            BlockState previous = helper.getLevel().getBlockState(
                    removed.east()
            );
            helper.assertTrue(previous.is(ModBlocks.ANCIENT_STAIRS.get()),
                    "Previous stair in the run was removed");
            helper.assertTrue(previous.getValue(StairBlock.SHAPE)
                            == StairsShape.INNER_LEFT,
                    "Previous stair was not converted to an inner corner");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "outerLands", timeoutTicks = 40)
    public static void removesLeakingStairOneBlockBeyondChunkBoundary(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(6, 2, 6));
        ChunkPos sourceChunk = new ChunkPos(origin);
        BlockPos leaked = new BlockPos(
                sourceChunk.getMaxBlockX() + 1,
                OuterLandsLabyrinthGenerator.BASE_Y + 3,
                sourceChunk.getMinBlockZ() + 8
        );
        BlockState stair = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, Half.BOTTOM);
        helper.getLevel().setBlock(leaked, stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(leaked.west(), stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(
                leaked.east(),
                ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                leaked.north(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                leaked.south(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );

        OuterLandsStairTopology.refresh(helper.getLevel(), sourceChunk);

        helper.assertTrue(helper.getLevel().getBlockState(leaked)
                        .is(ModBlocks.ANCIENT_STONE.get()),
                "Boundary stair touching Eldritch Nothing was left in place");
        BlockState previous = helper.getLevel().getBlockState(leaked.west());
        helper.assertTrue(previous.is(ModBlocks.ANCIENT_STAIRS.get()),
                "Previous boundary stair was removed");
        helper.assertTrue(previous.getValue(StairBlock.SHAPE)
                        == StairsShape.INNER_RIGHT,
                "Previous boundary stair was not converted to a corner");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "outerLands", timeoutTicks = 40)
    public static void turnsUpperAndLowerStairTipsAtSolidWall(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos lower = new BlockPos(
                origin.getX(),
                OuterLandsLabyrinthGenerator.BASE_Y + 3,
                origin.getZ()
        );
        BlockPos upper = lower.above(4);
        placeWallBoundRun(helper, lower, Half.BOTTOM);
        placeWallBoundRun(helper, upper, Half.TOP);

        OuterLandsStairTopology.refresh(
                helper.getLevel(), new ChunkPos(lower)
        );

        for (BlockPos tip : new BlockPos[]{lower, upper}) {
            BlockState state = helper.getLevel().getBlockState(tip);
            helper.assertTrue(state.is(ModBlocks.ANCIENT_STAIRS.get()),
                    "Wall-bound stair tip was replaced");
            helper.assertTrue(
                    state.getValue(StairBlock.SHAPE)
                            == StairsShape.INNER_LEFT,
                    "Wall-bound stair tip stayed straight instead of turning"
            );
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "outerLands", timeoutTicks = 40)
    public static void turnsOnlyRunEndAtPerpendicularContact(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos lower = new BlockPos(
                origin.getX(),
                OuterLandsLabyrinthGenerator.BASE_Y + 3,
                origin.getZ()
        );
        BlockPos upper = lower.above(4);
        placePerpendicularPair(helper, lower, Half.BOTTOM);
        placePerpendicularPair(helper, upper, Half.TOP);

        OuterLandsStairTopology.refresh(
                helper.getLevel(), new ChunkPos(lower)
        );

        for (BlockPos first : new BlockPos[]{lower, upper}) {
            BlockState firstState = helper.getLevel().getBlockState(first);
            BlockState secondState = helper.getLevel().getBlockState(
                    first.east()
            );
            helper.assertTrue(firstState.getValue(StairBlock.SHAPE)
                            == StairsShape.INNER_RIGHT,
                    "First touching stair stayed straight");
            helper.assertTrue(secondState.getValue(StairBlock.SHAPE)
                            == StairsShape.STRAIGHT,
                    "Crossing-run stair incorrectly became a second corner");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "outerLands", timeoutTicks = 40)
    public static void restoresMissingStairsInsideStraightRuns(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(6, 2, 6));
        BlockPos lowerGap = new BlockPos(
                origin.getX(),
                OuterLandsLabyrinthGenerator.BASE_Y + 3,
                origin.getZ()
        );
        BlockPos upperGap = lowerGap.above(4);
        placeBrokenStraightRun(helper, lowerGap, Half.BOTTOM,
                ModBlocks.ANCIENT_STONE.get().defaultBlockState());
        placeBrokenStraightRun(helper, upperGap, Half.TOP,
                ModBlocks.ANCIENT_STONE.get().defaultBlockState());

        OuterLandsStairTopology.refresh(
                helper.getLevel(), new ChunkPos(lowerGap)
        );

        assertRestoredStraightStair(helper, lowerGap, Half.BOTTOM);
        assertRestoredStraightStair(helper, upperGap, Half.TOP);
        helper.succeed();
    }

    private static void placeLeakingRun(
            GameTestHelper helper,
            BlockPos stairPosition,
            Half half
    ) {
        BlockState stair = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, half);
        helper.getLevel().setBlock(stairPosition, stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(
                stairPosition.east(), stair, Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                stairPosition.north(),
                ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                stairPosition.west(),
                ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                stairPosition.south(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
    }

    private static void placeWallBoundRun(
            GameTestHelper helper,
            BlockPos tip,
            Half half
    ) {
        BlockState stair = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, half);
        helper.getLevel().setBlock(tip, stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(tip.east(), stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(
                tip.north(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                tip.west(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
    }

    private static void placeBrokenStraightRun(
            GameTestHelper helper,
            BlockPos gap,
            Half half,
            BlockState gapState
    ) {
        BlockState stair = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, half);
        helper.getLevel().setBlock(gap, gapState, Block.UPDATE_ALL);
        helper.getLevel().setBlock(gap.east(), stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(gap.west(), stair, Block.UPDATE_ALL);
        helper.getLevel().setBlock(
                gap.north(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
    }

    private static void placePerpendicularPair(
            GameTestHelper helper,
            BlockPos first,
            Half half
    ) {
        BlockState stair = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.HALF, half);
        helper.getLevel().setBlock(
                first,
                stair.setValue(StairBlock.FACING, Direction.NORTH),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                first.east(),
                stair.setValue(StairBlock.FACING, Direction.EAST),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                first.north(),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        helper.getLevel().setBlock(
                first.east(2),
                ModBlocks.ANCIENT_STONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
    }

    private static void assertRestoredStraightStair(
            GameTestHelper helper,
            BlockPos gap,
            Half half
    ) {
        BlockState state = helper.getLevel().getBlockState(gap);
        helper.assertTrue(state.is(ModBlocks.ANCIENT_STAIRS.get()),
                "Straight stair run still contains a missing segment");
        helper.assertTrue(state.getValue(StairBlock.FACING) == Direction.NORTH,
                "Restored straight stair faces the wrong direction");
        helper.assertTrue(state.getValue(StairBlock.HALF) == half,
                "Restored stair is in the wrong half");
        helper.assertTrue(state.getValue(StairBlock.SHAPE)
                        == StairsShape.STRAIGHT,
                "Restored stair is not straight");
    }
}
