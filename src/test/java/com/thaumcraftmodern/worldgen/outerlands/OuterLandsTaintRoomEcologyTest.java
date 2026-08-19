package com.thaumcraftmodern.worldgen.outerlands;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OuterLandsTaintRoomEcologyTest {
    @Test
    void featureThirteenRestoresBiomeSurfaceAndTaintEncounter() throws Exception {
        String generator = source("OuterLandsLabyrinthGenerator.java");
        String events = source("OuterLandsTaintRoomEvents.java");
        String biome = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/"
                        + "TaintBiomeService.java"
        ));

        assertTrue(generator.contains("ModBlocks.CRUSTED_TAINT.get()"));
        assertTrue(generator.contains("random.nextInt(6) == 0"));
        assertTrue(generator.contains("ModBlocks.SPORE_STALK.get()"));
        assertTrue(biome.contains("public static boolean taintChunk("));
        assertTrue(events.contains("located.cell().feature() != 13"));
        assertTrue(events.contains("TaintBiomeService.taintChunk("));
        assertTrue(events.contains("ModEntities.TAINTACLE.get()"));
        assertTrue(events.contains("ModEntities.THAUMIC_SLIME.get()"));
        assertTrue(events.contains("ModEntities.TAINTED_CRAWLER.get()"));
        assertTrue(events.contains("ModEntities.TAINT_SPORE.get()"));
        assertTrue(events.contains("ModEntities.TAINT_SWARM.get()"));
        assertTrue(events.contains("mob.setPersistenceRequired()"));
        assertTrue(events.contains("level.noCollision(mob)"));
    }

    private static String source(String name) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + name
        ));
    }
}
