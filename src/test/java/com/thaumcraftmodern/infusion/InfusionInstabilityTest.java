package com.thaumcraftmodern.infusion;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfusionInstabilityTest {
    @Test
    void eventTableKeepsAllTwentyOneOriginalOutcomes() {
        Map<InfusionInstability.Event, Integer> counts =
                new EnumMap<>(InfusionInstability.Event.class);
        for (int roll = 0; roll < 21; roll++) {
            counts.merge(InfusionInstability.eventForRoll(roll), 1, Integer::sum);
        }
        assertEquals(Map.ofEntries(
                Map.entry(InfusionInstability.Event.EJECT, 4),
                Map.entry(InfusionInstability.Event.EJECT_GOO, 2),
                Map.entry(InfusionInstability.Event.EJECT_GAS, 2),
                Map.entry(InfusionInstability.Event.DESTROY_GOO, 1),
                Map.entry(InfusionInstability.Event.DESTROY_GAS, 1),
                Map.entry(InfusionInstability.Event.EJECT_EXPLODE, 2),
                Map.entry(InfusionInstability.Event.ZAP_ONE, 3),
                Map.entry(InfusionInstability.Event.ZAP_ALL, 1),
                Map.entry(InfusionInstability.Event.HARM_ONE, 2),
                Map.entry(InfusionInstability.Event.HARM_ALL, 1),
                Map.entry(InfusionInstability.Event.MATRIX_EXPLOSION, 1),
                Map.entry(InfusionInstability.Event.WARP, 1)
        ), counts);
    }

    @Test
    void triggerUsesOriginalInclusiveFiveHundredRoll() {
        assertFalse(InfusionInstability.triggers(0, 0));
        assertTrue(InfusionInstability.triggers(1, 0));
        assertTrue(InfusionInstability.triggers(1, 1));
        assertFalse(InfusionInstability.triggers(1, 2));
    }

    @Test
    void accumulatedInstabilityCapsAtTwentyFive() {
        assertEquals(1, InfusionInstability.increaseCapped(0));
        assertEquals(25, InfusionInstability.increaseCapped(25));
    }

    @Test
    void fractionalCandleHarmonyAffectsTheRoll() {
        assertTrue(InfusionInstability.triggers(1.25F, 1.20F));
        assertFalse(InfusionInstability.triggers(1.25F, 1.30F));
    }

    @Test
    void matrixExplosionCannotDestroyTheMatrixOrCentralPedestal()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "RunicMatrixBlockEntity.java"
        ));
        int event = source.indexOf("case MATRIX_EXPLOSION");
        int warp = source.indexOf("case WARP", event);
        String explosion = source.substring(event, warp);
        assertTrue(explosion.contains("Level.ExplosionInteraction.NONE"));
        assertFalse(explosion.contains("Level.ExplosionInteraction.BLOCK"));
    }
}
