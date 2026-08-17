package com.thaumcraftmodern.warp;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WarpSpawnPlacementFidelityTest {
    @Test
    void eachAxisUsesTheClassicZeroOrSevenToTwentyFourBlockOffset() {
        RandomSource random = RandomSource.create(0x544334L);
        int origin = 37;
        boolean sawUnchanged = false;
        boolean sawPositive = false;
        boolean sawNegative = false;

        for (int sample = 0; sample < 1_000; sample++) {
            int offset = WarpEvents.randomSpawnCoordinate(origin, random) - origin;
            assertTrue(offset == 0 || Math.abs(offset) >= 7);
            assertTrue(Math.abs(offset) <= 24);
            sawUnchanged |= offset == 0;
            sawPositive |= offset > 0;
            sawNegative |= offset < 0;
        }

        assertTrue(sawUnchanged);
        assertTrue(sawPositive);
        assertTrue(sawNegative);
    }

    @Test
    void warpSpawnsUseThreeDimensionalCollisionAndLiquidChecks()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/warp/WarpEvents.java"
        ));

        assertTrue(source.contains(
                "int y = randomSpawnCoordinate(player.blockPosition().getY(), random);"
        ));
        assertTrue(source.contains(
                "!level.containsAnyLiquid(mob.getBoundingBox())"
        ));
        assertTrue(source.contains("mob.setLastHurtByMob(player);"));
        assertFalse(source.contains("Heightmap.Types.MOTION_BLOCKING_NO_LEAVES"));
    }
}
