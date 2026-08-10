package com.thaumcraftmodern.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thaumcraftmodern.item.SinisterLodestoneVisibility;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

final class ClientSinisterNodeTrackerTest {
    private static final Path MAIN =
            Path.of("src/main/java/com/thaumcraftmodern");

    @Test
    void itemPropertyUsesAnEventMaintainedNodeIndex() throws Exception {
        String tracker = Files.readString(MAIN.resolve(
                "client/ClientSinisterNodeTracker.java"));
        String node = Files.readString(MAIN.resolve(
                "aura/AuraNodeBlockEntity.java"));

        assertFalse(tracker.contains("getChunkSource().getChunk("),
                "The item property must never poll the client chunk grid");
        assertFalse(tracker.contains("sampledTick"));
        assertTrue(tracker.contains("onChunkLoad(ChunkEvent.Load event)"));
        assertTrue(tracker.contains("onChunkUnload(ChunkEvent.Unload event)"));
        assertTrue(tracker.contains("SinisterLodestoneVisibility.isVisibleTo("),
                "Indexed candidates must still use the original 256-block FOV check");
        assertTrue(node.contains("notifyClientIndexChanged();"),
                "State sync must update the index when a node changes type");
        assertTrue(node.contains("AuraNodeClientLifecycle.removed(this);"),
                "Removed nodes must not remain in the index");
    }

    @Test
    void indexedCandidatesStillRespectDirectionAndExactRange() {
        Vec3 eye = new Vec3(0.5D, 0.5D, 0.5D);
        Vec3 forward = new Vec3(0.0D, 0.0D, 1.0D);

        assertTrue(SinisterLodestoneVisibility.isVisibleTo(
                eye,
                forward,
                new BlockPos(0, 0, 256)
        ));
        assertFalse(SinisterLodestoneVisibility.isVisibleTo(
                eye,
                forward,
                new BlockPos(0, 0, 257)
        ));
        assertFalse(SinisterLodestoneVisibility.isVisibleTo(
                eye,
                forward,
                new BlockPos(0, 0, -10)
        ));
    }
}
