package com.thaumcraftmodern.aura;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class AuraNodeStateDrainTest {
    @Test
    void drainIsBoundedAndAdvancesRevisionOncePerMutation() {
        AuraNodeState state = new AuraNodeState(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                aspects(7),
                aspects(10),
                4L
        );

        assertEquals(5, state.drain(PrimalAspect.IGNIS, 5));
        assertEquals(2, state.current(PrimalAspect.IGNIS));
        assertEquals(5L, state.revision());

        assertEquals(2, state.drain(PrimalAspect.IGNIS, 5));
        assertEquals(0, state.drain(PrimalAspect.IGNIS, 5));
        assertEquals(0, state.current(PrimalAspect.IGNIS));
        assertEquals(6L, state.revision());
    }

    private static EnumMap<PrimalAspect, Integer> aspects(int ignis) {
        EnumMap<PrimalAspect, Integer> result =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            result.put(aspect, aspect == PrimalAspect.IGNIS ? ignis : 0);
        }
        return result;
    }
}
