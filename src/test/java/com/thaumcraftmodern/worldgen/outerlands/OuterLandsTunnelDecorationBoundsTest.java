package com.thaumcraftmodern.worldgen.outerlands;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OuterLandsTunnelDecorationBoundsTest {
    @Test
    void airDecorationsRejectCellsTouchingOuterVoid() throws Exception {
        String bounds = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsTunnelDecorations.java"
        ));
        String generator = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsLabyrinthGenerator.java"
        ));
        String crystals = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsRunedStones.java"
        ));
        String migration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsEldritchNothingMigrationEvents.java"
        ));

        assertTrue(bounds.contains("EldritchNothingBlock.isNothing("));
        assertTrue(bounds.contains("state.is(Blocks.COBWEB)"));
        assertTrue(bounds.contains("state.is(ModBlocks.TAINT_FIBRES.get())"));
        assertTrue(generator.contains(
                "OuterLandsTunnelDecorations.isInteriorAir("
        ));
        assertFalse(generator.contains(
                "!target.equals(center) && level.getBlockState(target).isAir()"
        ));
        assertTrue(crystals.contains(
                "OuterLandsTunnelDecorations.isInteriorAir("
        ));
        assertTrue(migration.contains(
                "OuterLandsTunnelDecorations.removeEscaped("
        ));
    }
}
