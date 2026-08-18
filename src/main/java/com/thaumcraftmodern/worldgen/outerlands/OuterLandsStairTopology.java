package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;

/** Resolves straight runs and their wall-bounded inner corners. */
final class OuterLandsStairTopology {
    private OuterLandsStairTopology() {
    }

    static int refresh(LevelAccessor level, ChunkPos chunk) {
        int changed = trimStairsTouchingNothing(level, chunk);
        changed += fillMissingStraightStairs(level, chunk);
        changed += fillMissingCorners(level, chunk);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();

        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 2;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 10; y++) {
            for (int x = minX - 1; x <= minX + 16; x++) {
                for (int z = minZ - 1; z <= minZ + 16; z++) {
                    cursor.set(x, y, z);
                    BlockState state = level.getBlockState(cursor);
                    if (!state.is(ModBlocks.ANCIENT_STAIRS.get())) {
                        continue;
                    }
                    BlockState resolved = resolveShape(level, cursor, state);
                    if (resolved.equals(state)) {
                        continue;
                    }
                    level.setBlock(
                            cursor,
                            resolved,
                            Block.UPDATE_CLIENTS
                    );
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int fillMissingStraightStairs(
            LevelAccessor level,
            ChunkPos chunk
    ) {
        List<StairPlacement> placements = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 2;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 10; y++) {
            for (int x = minX - 1; x <= minX + 16; x++) {
                for (int z = minZ - 1; z <= minZ + 16; z++) {
                    cursor.set(x, y, z);
                    if (!isCornerGap(level.getBlockState(cursor))) {
                        continue;
                    }
                    BlockState replacement = straightGapAt(level, cursor);
                    if (replacement != null) {
                        placements.add(new StairPlacement(
                                cursor.immutable(), replacement
                        ));
                    }
                }
            }
        }
        for (StairPlacement placement : placements) {
            level.setBlock(
                    placement.position(),
                    placement.state(),
                    Block.UPDATE_ALL
            );
        }
        return placements.size();
    }

    private static BlockState straightGapAt(
            LevelAccessor level,
            BlockPos gap
    ) {
        if (touchesNothingHorizontally(level, gap)) {
            return null;
        }
        for (Direction axisDirection : new Direction[]{
                Direction.EAST, Direction.SOUTH
        }) {
            BlockState first = level.getBlockState(
                    gap.relative(axisDirection)
            );
            BlockState second = level.getBlockState(
                    gap.relative(axisDirection.getOpposite())
            );
            if (!formsStraightRunGap(first, second, axisDirection)) {
                continue;
            }
            Direction facing = first.getValue(StairBlock.FACING);
            if (!isAncientWall(level.getBlockState(gap.relative(facing)))) {
                continue;
            }
            return first.setValue(
                    StairBlock.SHAPE, StairsShape.STRAIGHT
            );
        }
        return null;
    }

    static boolean formsStraightRunGap(
            BlockState first,
            BlockState second,
            Direction runDirection
    ) {
        if (!first.is(ModBlocks.ANCIENT_STAIRS.get())
                || !second.is(ModBlocks.ANCIENT_STAIRS.get())) {
            return false;
        }
        Direction firstFacing = first.getValue(StairBlock.FACING);
        return firstFacing == second.getValue(StairBlock.FACING)
                && first.getValue(StairBlock.HALF)
                == second.getValue(StairBlock.HALF)
                && runDirection.getAxis() != firstFacing.getAxis();
    }

    private static int trimStairsTouchingNothing(
            LevelAccessor level,
            ChunkPos chunk
    ) {
        List<BlockPos> removals = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 2;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 10; y++) {
            for (int x = minX - 1; x <= minX + 16; x++) {
                for (int z = minZ - 1; z <= minZ + 16; z++) {
                    cursor.set(x, y, z);
                    BlockState stair = level.getBlockState(cursor);
                    if (!stair.is(ModBlocks.ANCIENT_STAIRS.get())) {
                        continue;
                    }
                    if (touchesNothingHorizontally(level, cursor)) {
                        removals.add(cursor.immutable());
                    }
                }
            }
        }
        for (BlockPos removal : removals) {
            level.setBlock(
                    removal,
                    ordinaryAncientWall(level, removal),
                    Block.UPDATE_ALL
            );
        }
        return removals.size();
    }

    private static boolean touchesNothingHorizontally(
            LevelAccessor level,
            BlockPos position
    ) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (EldritchNothingBlock.isNothing(level.getBlockState(
                    position.relative(direction)
            ))) {
                return true;
            }
        }
        return false;
    }

    private static BlockState ordinaryAncientWall(
            LevelAccessor level,
            BlockPos position
    ) {
        for (Direction direction : Direction.values()) {
            BlockState neighbour = level.getBlockState(
                    position.relative(direction)
            );
            if (neighbour.is(ModBlocks.ANCIENT_STONE.get())
                    || neighbour.is(ModBlocks.ANCIENT_ROCK.get())
                    || neighbour.is(ModBlocks.ANCIENT_CRUST.get())) {
                return neighbour;
            }
        }
        return ModBlocks.ANCIENT_STONE.get().defaultBlockState();
    }

    private static int fillMissingCorners(
            LevelAccessor level,
            ChunkPos chunk
    ) {
        List<StairPlacement> placements = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 2;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 10; y++) {
            for (int x = minX - 1; x <= minX + 16; x++) {
                for (int z = minZ - 1; z <= minZ + 16; z++) {
                    cursor.set(x, y, z);
                    BlockState current = level.getBlockState(cursor);
                    if (!isCornerGap(current)) {
                        continue;
                    }
                    StairPlacement placement = cornerAt(level, cursor);
                    if (placement != null) {
                        placements.add(new StairPlacement(
                                cursor.immutable(), placement.state()
                        ));
                    }
                }
            }
        }
        for (StairPlacement placement : placements) {
            level.setBlock(
                    placement.position(),
                    placement.state(),
                    Block.UPDATE_ALL
            );
        }
        return placements.size();
    }

    private static StairPlacement cornerAt(
            LevelAccessor level,
            BlockPos gap
    ) {
        if (touchesNothingHorizontally(level, gap)) {
            return null;
        }
        List<StairNeighbour> stairs = new ArrayList<>(2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockState state = level.getBlockState(gap.relative(direction));
            if (state.is(ModBlocks.ANCIENT_STAIRS.get())) {
                stairs.add(new StairNeighbour(direction, state));
            }
        }
        if (stairs.size() != 2) {
            return null;
        }
        StairNeighbour first = stairs.get(0);
        StairNeighbour second = stairs.get(1);
        Direction firstFacing = first.state().getValue(StairBlock.FACING);
        Direction secondFacing = second.state().getValue(StairBlock.FACING);
        Half half = first.state().getValue(StairBlock.HALF);
        if (!formsMissingCorner(
                first.direction(),
                firstFacing,
                half,
                second.direction(),
                secondFacing,
                second.state().getValue(StairBlock.HALF)
        )) {
            return null;
        }
        Direction pathIntoGap = first.direction().getOpposite();
        StairsShape shape = pathIntoGap == firstFacing.getCounterClockWise()
                ? StairsShape.INNER_LEFT
                : StairsShape.INNER_RIGHT;
        BlockState corner = ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, firstFacing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.SHAPE, shape);
        if (!isAncientWall(level.getBlockState(
                gap.relative(firstFacing)
        ))) {
            return null;
        }
        return new StairPlacement(gap.immutable(), corner);
    }

    static boolean formsMissingCorner(
            Direction firstDirection,
            Direction firstFacing,
            Half firstHalf,
            Direction secondDirection,
            Direction secondFacing,
            Half secondHalf
    ) {
        return firstDirection.getAxis() != secondDirection.getAxis()
                && firstFacing.getAxis() != secondFacing.getAxis()
                && firstDirection.getAxis() != firstFacing.getAxis()
                && secondDirection.getAxis() != secondFacing.getAxis()
                && firstHalf == secondHalf;
    }

    private static boolean isCornerGap(BlockState state) {
        return state.isAir()
                || EldritchNothingBlock.isNothing(state)
                || isAncientWall(state);
    }

    private static BlockState resolveShape(
            LevelAccessor level,
            BlockPos position,
            BlockState state
    ) {
        StairsShape touchingCorner = touchingPerpendicularCorner(
                level, position, state
        );
        if (touchingCorner != null) {
            return state.setValue(StairBlock.SHAPE, touchingCorner);
        }
        StairPlacement joinedRuns = cornerAt(level, position);
        if (joinedRuns != null
                && joinedRuns.state().getValue(StairBlock.HALF)
                == state.getValue(StairBlock.HALF)) {
            return joinedRuns.state();
        }
        Direction facing = state.getValue(StairBlock.FACING);
        boolean wallOnLeft = isAncientWall(level.getBlockState(
                position.relative(facing.getCounterClockWise())
        ));
        boolean wallOnRight = isAncientWall(level.getBlockState(
                position.relative(facing.getClockWise())
        ));
        if (wallOnLeft != wallOnRight) {
            return state.setValue(
                    StairBlock.SHAPE,
                    wallOnLeft ? StairsShape.INNER_LEFT
                            : StairsShape.INNER_RIGHT
            );
        }
        return state.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
    }

    private static StairsShape touchingPerpendicularCorner(
            LevelAccessor level,
            BlockPos position,
            BlockState state
    ) {
        Direction facing = state.getValue(StairBlock.FACING);
        Half half = state.getValue(StairBlock.HALF);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        /*
         * Only the stair at the end of its run owns the corner. A neighbour
         * in front or behind belongs to the crossing run and stays straight;
         * resolving both sides produced two adjacent corner stairs.
         */
        if (isPerpendicularStair(
                level.getBlockState(position.relative(left)), facing, half
        )) {
            return StairsShape.INNER_LEFT;
        }
        if (isPerpendicularStair(
                level.getBlockState(position.relative(right)), facing, half
        )) {
            return StairsShape.INNER_RIGHT;
        }
        return null;
    }

    private static boolean isPerpendicularStair(
            BlockState neighbour,
            Direction facing,
            Half half
    ) {
        return neighbour.is(ModBlocks.ANCIENT_STAIRS.get())
                && neighbour.getValue(StairBlock.HALF) == half
                && neighbour.getValue(StairBlock.FACING).getAxis()
                != facing.getAxis();
    }

    static boolean isAncientWall(BlockState state) {
        return state.is(ModBlocks.ANCIENT_STONE.get())
                || state.is(ModBlocks.ANCIENT_ROCK.get())
                || state.is(ModBlocks.ANCIENT_CRUST.get())
                || state.is(ModBlocks.ELDRITCH_RUNED_STONE.get());
    }

    private record StairNeighbour(Direction direction, BlockState state) {
    }

    private record StairPlacement(BlockPos position, BlockState state) {
    }
}
