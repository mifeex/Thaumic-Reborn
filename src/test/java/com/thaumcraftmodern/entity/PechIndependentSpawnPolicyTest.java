package com.thaumcraftmodern.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PechIndependentSpawnPolicyTest {
    @Test
    void rareAttemptOnlyRunsOnItsIntervalAndWinningRoll() {
        assertTrue(PechIndependentSpawnPolicy.isCheckTick(200L));
        assertFalse(PechIndependentSpawnPolicy.isCheckTick(199L));
        assertTrue(PechIndependentSpawnPolicy.winsRareRoll(0));
        assertFalse(PechIndependentSpawnPolicy.winsRareRoll(1));
    }

    @Test
    void independentAttemptKeepsClassicOneToTwoGroup() {
        assertEquals(1, PechIndependentSpawnPolicy.groupSize(0));
        assertEquals(2, PechIndependentSpawnPolicy.groupSize(1));
        assertEquals(1, PechIndependentSpawnPolicy.groupSize(2));
    }
}
