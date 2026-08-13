package com.thaumcraftmodern.world.block.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class HotMachinePollingPerformanceTest {
    @Test
    void emptyRechargePedestalStillHonoursRescanInterval() throws Exception {
        String source = source("WandRechargePedestalBlockEntity.java");

        assertTrue(source.contains("pedestal.counter == 1"));
        assertTrue(source.contains("pedestal.counter % RESCAN_INTERVAL == 0"));
        assertFalse(source.contains("pedestal.nodes.isEmpty() ||"));
        assertTrue(source.contains("AuraNodeSpatialIndex.withinCube("));
        assertFalse(source.contains("for (int x = -RANGE"));
    }

    @Test
    void fluxScrubberUsesPackedArrayAndCursorWithoutFrontRemoval()
            throws Exception {
        String source = source("FluxScrubberBlockEntity.java");

        assertTrue(source.contains("private long[] checklist"));
        assertTrue(source.contains("private int checklistCursor"));
        assertTrue(source.contains("checklist[checklistCursor++]"));
        assertTrue(source.contains("BlockPos.asLong("));
        assertFalse(source.contains("List<BlockPos> checklist"));
        assertFalse(source.contains("checklist.remove(0)"));
        assertFalse(source.contains("Collections.shuffle"));
    }

    private static String source(String file) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/" + file));
    }
}
