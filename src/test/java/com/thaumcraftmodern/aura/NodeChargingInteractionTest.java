package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class NodeChargingInteractionTest {
    @Test
    void clientHoldConsumesUseWithoutVanillaInteractionSwing() {
        assertSame(
                net.minecraft.world.InteractionResult.CONSUME,
                NodeChargingService.CLIENT_HOLD_RESULT
        );
        assertTrue(
                NodeChargingService.CLIENT_HOLD_RESULT.consumesAction()
        );
        assertFalse(
                NodeChargingService.CLIENT_HOLD_RESULT.shouldSwing()
        );
    }

    @Test
    void nodeTapperResearchRaisesDrainFromOneToThreeVisPerTransfer() {
        assertEquals(1, NodeChargingService.drainRate(false, false));
        assertEquals(2, NodeChargingService.drainRate(true, false));
        assertEquals(3, NodeChargingService.drainRate(true, true));
        assertEquals(3, NodeChargingService.drainRate(false, true));
    }

    @Test
    void fractionalCentivisRoomStillAllowsNodeCharging() {
        assertTrue(NodeChargingService.hasChargeRoom(5));
        assertTrue(NodeChargingService.hasChargeRoom(1));
        assertFalse(NodeChargingService.hasChargeRoom(0));
    }
}
