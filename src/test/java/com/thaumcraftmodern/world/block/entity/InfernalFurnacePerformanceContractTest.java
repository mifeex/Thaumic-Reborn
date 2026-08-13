package com.thaumcraftmodern.world.block.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class InfernalFurnacePerformanceContractTest {
    @Test
    void missingNodeSearchUsesLongCooldownAndLoadedAuraIndex() throws Exception {
        assertEquals(10, InfernalFurnaceBlockEntity.AURA_SEARCH_RADIUS);
        assertEquals(40,
                InfernalFurnaceBlockEntity.INITIAL_AURA_SEARCH_INTERVAL);
        assertEquals(40, InfernalFurnaceBlockEntity.AURA_SEARCH_BACKOFF_STEP);
        assertEquals(200, InfernalFurnaceBlockEntity.MAX_AURA_SEARCH_INTERVAL);
        int interval = InfernalFurnaceBlockEntity.INITIAL_AURA_SEARCH_INTERVAL;
        interval = InfernalFurnaceBlockEntity.nextAuraSearchInterval(interval);
        assertEquals(80, interval);
        interval = InfernalFurnaceBlockEntity.nextAuraSearchInterval(interval);
        assertEquals(120, interval);
        interval = InfernalFurnaceBlockEntity.nextAuraSearchInterval(interval);
        assertEquals(160, interval);
        interval = InfernalFurnaceBlockEntity.nextAuraSearchInterval(interval);
        assertEquals(200, interval);
        assertEquals(200,
                InfernalFurnaceBlockEntity.nextAuraSearchInterval(interval));

        String source = furnaceSource();
        assertTrue(source.contains("AuraNodeSpatialIndex.nearest("));
        assertFalse(source.contains("for (int x = -10"));
    }

    @Test
    void idleFurnaceCannotSearchOrDrainAura() throws Exception {
        String source = furnaceSource();
        assertTrue(source.contains("if (cookedFlag && speedyTime <= 0)"));
        assertTrue(source.contains("node.drainForMachine("));
        assertFalse(source.contains("node.snapshotState()"));
    }

    @Test
    void visNetworkIsTriedBeforeDirectAuraFallback() throws Exception {
        String source = furnaceSource();
        int network = source.indexOf("VisMachineAccess.consumeNearest(");
        int aura = source.indexOf("AuraNodeSpatialIndex.nearest(");

        assertTrue(network >= 0);
        assertTrue(aura > network);
    }

    private static String furnaceSource() throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/InfernalFurnaceBlockEntity.java"));
    }
}
