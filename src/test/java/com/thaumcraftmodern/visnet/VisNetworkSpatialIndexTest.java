package com.thaumcraftmodern.visnet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

final class VisNetworkSpatialIndexTest {
    @Test
    void indexedCandidatesKeepBetweenClosedTieOrder() {
        List<BlockPos> legacy = new ArrayList<>();
        BlockPos.betweenClosed(-1, -1, -1, 1, 1, 1)
                .forEach(position -> legacy.add(position.immutable()));
        List<BlockPos> indexed = new ArrayList<>(legacy);
        indexed.sort(VisNetworkSpatialIndex.legacyScanOrder());

        assertEquals(legacy, indexed);
    }

    @Test
    void hotConsumersNoLongerPollEveryBlock() throws Exception {
        String access = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/visnet/VisMachineAccess.java"));
        String network = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/visnet/VisNetwork.java"));

        assertFalse(access.contains("BlockPos.betweenClosed"));
        assertFalse(network.contains("BlockPos.betweenClosed"));
        assertFalse(access.contains("new ArrayList"));
        assertFalse(access.contains("new HashSet"));
        assertFalse(access.contains(".sort("));
        assertTrue(access.contains("VisNetworkSpatialIndex.machineCandidates"));
        assertTrue(network.contains("VisNetworkSpatialIndex.networkCandidates"));
    }

    @Test
    void routeAndNeighbourCachesInvalidateWithTopology() throws Exception {
        String index = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/visnet/VisNetworkSpatialIndex.java"));
        String node = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/visnet/VisNetworkNodeBlockEntity.java"));

        assertTrue(index.contains("Long2ObjectOpenHashMap<Route> routes"));
        assertTrue(index.contains("Long2ObjectOpenHashMap<long[]> neighbourhoods"));
        assertTrue(index.contains("Long2ObjectOpenHashMap<long[]> machineRoutes"));
        assertTrue(index.contains("machineRoutes.clear()"));
        assertTrue(index.contains("LongOpenHashSet includedSources"));
        assertTrue(index.contains(
                "includedSources.add(route.sourcePosition())"));
        assertTrue(node.contains(
                "private int networkCounter = NETWORK_RESCAN_INTERVAL - 1"));
        assertTrue(node.contains(
                "if (++node.networkCounter >= NETWORK_RESCAN_INTERVAL)"));
        assertFalse(node.contains("|| !node.hasValidParent()"));
    }
}
