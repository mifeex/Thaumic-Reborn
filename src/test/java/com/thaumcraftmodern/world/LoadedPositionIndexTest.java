package com.thaumcraftmodern.world;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LoadedPositionIndexTest {
    @Test
    void exactRadiusCrossesPositiveAndNegativeChunkBoundaries() {
        LoadedPositionIndex index = new LoadedPositionIndex();
        BlockPos origin = new BlockPos(0, 64, 0);
        index.track(new BlockPos(8, 64, 0));
        index.track(new BlockPos(-8, 64, 0));
        index.track(new BlockPos(8, 65, 0));
        index.track(new BlockPos(9, 64, 0));

        Set<Long> found = new HashSet<>();
        index.within(origin, 8).forEach((long packed) -> found.add(packed));

        assertEquals(Set.of(
                new BlockPos(8, 64, 0).asLong(),
                new BlockPos(-8, 64, 0).asLong()
        ), found);
    }

    @Test
    void chunkUnloadAndPositionRemovalCannotLeaveCandidates() {
        LoadedPositionIndex index = new LoadedPositionIndex();
        BlockPos first = new BlockPos(1, 70, 1);
        BlockPos second = new BlockPos(17, 70, 1);
        index.track(first);
        index.track(second);

        index.untrack(first);
        index.clearChunk(new ChunkPos(second));

        assertTrue(index.within(BlockPos.ZERO, 64).isEmpty());
    }

    @Test
    void anyWithinStopsOnFirstMatchingPosition() {
        LoadedPositionIndex index = new LoadedPositionIndex();
        BlockPos expected = new BlockPos(4, 0, 0);
        index.track(expected);
        index.track(new BlockPos(40, 0, 0));

        assertTrue(index.anyWithin(
                BlockPos.ZERO,
                8,
                packed -> packed == expected.asLong()
        ));
        assertFalse(index.anyWithin(
                BlockPos.ZERO,
                8,
                packed -> false
        ));
    }

    @Test
    void cubeKeepsLegacyCornerCandidatesOutsideSphericalRadius() {
        LoadedPositionIndex index = new LoadedPositionIndex();
        BlockPos corner = new BlockPos(10, 10, 10);
        index.track(corner);

        assertFalse(index.within(BlockPos.ZERO, 10).contains(corner.asLong()));
        assertTrue(index.withinCube(BlockPos.ZERO, 10)
                .contains(corner.asLong()));
    }
}
