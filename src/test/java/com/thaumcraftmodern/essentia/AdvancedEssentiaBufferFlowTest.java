package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.essentia.AdvancedBufferFlowController.Signals;
import com.thaumcraftmodern.essentia.AdvancedBufferFlowController.Snapshot;
import com.thaumcraftmodern.essentia.AdvancedBufferFlowController.State;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AdvancedEssentiaBufferFlowTest {
    private static final Signals SUPPLY = new Signals(
            true, false, false, true);
    private static final Signals READY_RETURN = new Signals(
            false, true, true, true);

    @Test
    void configuredInputCanPullFromAnOrdinaryJar() {
        assertTrue(AdvancedEssentiaBufferBlockEntity.INPUT_SUCTION
                > EssentiaJarBlockEntity.SUCTION);
        assertEquals(AdvancedEssentiaBufferBlockEntity.INPUT_SUCTION,
                AdvancedEssentiaBufferBlockEntity.suctionForRole(
                        AdvancedBufferSideRole.INPUT));
        assertEquals(0, AdvancedEssentiaBufferBlockEntity.suctionForRole(
                AdvancedBufferSideRole.BLOCKED));
    }

    @Test
    void bothOutputRolesArePassiveSources() {
        assertTrue(AdvancedEssentiaBufferBlockEntity.isOutputRole(
                AdvancedBufferSideRole.MAIN_OUTPUT));
        assertTrue(AdvancedEssentiaBufferBlockEntity.isOutputRole(
                AdvancedBufferSideRole.RESERVE_OUTPUT));
        assertEquals(0, AdvancedEssentiaBufferBlockEntity.suctionForRole(
                AdvancedBufferSideRole.MAIN_OUTPUT));
        assertEquals(0, AdvancedEssentiaBufferBlockEntity.suctionForRole(
                AdvancedBufferSideRole.RESERVE_OUTPUT));
    }

    @Test
    void consumerWinsWhileTheMainPathIsForward() {
        Snapshot state = AdvancedBufferFlowController.advance(
                Snapshot.idle(), SUPPLY, 20);
        assertEquals(State.SUPPLY, state.state());
    }

    @Test
    void reversedMainPathRoutesQueuedEssentiaToReserve() {
        Snapshot state = AdvancedBufferFlowController.advance(
                Snapshot.idle(), READY_RETURN, 30);
        assertEquals(State.RESERVE, state.state());
    }

    @Test
    void bufferedEssentiaWithoutReversalStaysInTheBuffer() {
        Signals queuedForReserve = new Signals(
                false, false, false, true);

        Snapshot state = AdvancedBufferFlowController.advance(
                new Snapshot(State.SUPPLY, 0, 0), queuedForReserve, 30);

        assertEquals(State.IDLE, state.state());
    }

    @Test
    void reserveBlockageRemainsBlockedWithoutTheRemovedEmergencyVent() {
        Signals blocked = new Signals(
                false, true, true, false);
        Snapshot state = AdvancedBufferFlowController.advance(
                new Snapshot(State.RESERVE, 0, 0), blocked, 30);
        assertEquals(State.BLOCKED, state.state());
        for (int tick = 0; tick < 80; tick++) {
            state = AdvancedBufferFlowController.advance(state, blocked, 30);
        }
        assertEquals(State.BLOCKED, state.state());
    }

    @Test
    void specialPoliciesAndSwitchDelayAreBounded() {
        assertTrue(TubePolicyRegistry.require(TubePolicyRegistry.REVERSIBLE)
                .reversibleController());
        assertTrue(TubePolicyRegistry.require(TubePolicyRegistry.REVERSIBLE)
                .directional());
        assertThrows(IllegalArgumentException.class, () ->
                TubePolicyRegistry.require(new net.minecraft.resources.ResourceLocation(
                        "thaumcraftmodern", "emergency_vent")));
        assertFalse(TubePolicyRegistry.require(TubePolicyRegistry.PLAIN)
                .reversibleController());
        for (int value = -20; value <= 20; value++) {
            int delay = EssentiaTubeBlockEntity.switchDelayTicks(
                    new BlockPos(value, value * 3, -value));
            assertTrue(delay >= 20 && delay <= 40);
        }
    }

    @Test
    void controllerUsesOnlyDirectNeighboursAndHonoursReturnBoundary()
            throws Exception {
        String buffer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "AdvancedEssentiaBufferBlockEntity.java"));
        String tube = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "EssentiaTubeBlockEntity.java"));
        assertTrue(buffer.contains("EssentiaConnections.neighbour("));
        assertFalse(buffer.contains("BlockPos.betweenClosed"));
        assertFalse(buffer.contains("getChunk"));
        assertFalse(buffer.contains("TubeVentParticleOptions"));
        assertFalse(buffer.contains("requestAutomaticReturn"));
        assertFalse(buffer.contains("pullReturnedEssentia"));
        assertFalse(buffer.contains("returnController("));
        assertFalse(buffer.contains("tube.returnEnabled()"));
        assertFalse(tube.contains("automaticReturnRequested"));
        assertFalse(tube.contains("requestAutomaticReturn"));
        assertTrue(tube.contains("!remote.canReturnEssentia()"));
        assertTrue(tube.contains("suctionFlowMode == EssentiaFlowMode.RETURN"));
    }

    @Test
    void roleSyncInvalidatesTheClientModelOnlyWhenColorsChange()
            throws Exception {
        String buffer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "AdvancedEssentiaBufferBlockEntity.java"));
        assertTrue(buffer.contains(
                "AdvancedBufferSideRole[] previousRoles = roles.clone()"));
        assertTrue(buffer.contains(
                "!Arrays.equals(previousRoles, roles)"));
        assertTrue(buffer.contains(
                "level.sendBlockUpdated(worldPosition, state, state,"));
        assertTrue(buffer.contains("Block.UPDATE_CLIENTS"));
    }

    @Test
    void bothOutputsExposeTheSameSupplyAndNeverPushDirectly()
            throws Exception {
        String buffer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "AdvancedEssentiaBufferBlockEntity.java"));
        assertTrue(buffer.contains(
                "role == AdvancedBufferSideRole.MAIN_OUTPUT"));
        assertTrue(buffer.contains(
                "role == AdvancedBufferSideRole.RESERVE_OUTPUT"));
        assertTrue(buffer.contains("return supply;"));
        assertTrue(buffer.contains(
                "remote.suctionType(side.getOpposite())"));
        assertTrue(buffer.contains(
                "store.amount(wanted) > 0"));
        assertFalse(buffer.contains("sendToReserve("));
        assertFalse(buffer.contains("queueSupplyForReserve("));
    }
}
