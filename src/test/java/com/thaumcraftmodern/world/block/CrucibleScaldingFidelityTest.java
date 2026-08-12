package com.thaumcraftmodern.world.block;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrucibleScaldingFidelityTest {
    @Test
    void boilingCrucibleUsesClassicDamageTimingAndHollowCollision()
            throws Exception {
        assertEquals(10, CrucibleBlock.SCALD_INTERVAL_TICKS);
        assertEquals(1.0F, CrucibleBlock.SCALD_DAMAGE);

        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/CrucibleBlock.java"
        ));
        assertTrue(source.contains("crucible.canProcessItems()"));
        assertTrue(source.contains("level.damageSources().generic()"));
        assertTrue(source.contains("SoundEvents.FIRE_EXTINGUISH"));
        assertTrue(source.contains("2.0F + level.random.nextFloat() * 0.4F"));
        assertTrue(source.contains("Block.box(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D)"));
        assertTrue(source.contains("return COLLISION_SHAPE;"));
    }
}
