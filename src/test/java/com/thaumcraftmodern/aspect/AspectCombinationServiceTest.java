package com.thaumcraftmodern.aspect;

import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.testing.AspectFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AspectCombinationServiceTest {
    @Test
    void combinationConsumesComponentsAndDiscoversCompoundExactlyOnce() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        int startingAer = knowledge.aspectAmount("aer");
        int startingIgnis = knowledge.aspectAmount("ignis");

        AspectCombinationService.Result first = AspectCombinationService.combine(
                AspectFixtures.firstDiscoveryCatalog(),
                knowledge,
                "aer",
                "ignis"
        );

        assertEquals(AspectCombinationService.Status.COMBINED, first.status());
        assertEquals("lux", first.resultAspectId());
        assertTrue(first.newlyDiscovered());
        assertEquals(3, first.createdAmount());
        assertEquals(startingAer - 1, knowledge.aspectAmount("aer"));
        assertEquals(startingIgnis - 1, knowledge.aspectAmount("ignis"));
        assertEquals(3, knowledge.aspectAmount("lux"));

        AspectCombinationService.Result second = AspectCombinationService.combine(
                AspectFixtures.firstDiscoveryCatalog(),
                knowledge,
                "ignis",
                "aer"
        );
        assertTrue(second.combined());
        assertFalse(second.newlyDiscovered());
        assertEquals(1, second.createdAmount());
        assertEquals(4, knowledge.aspectAmount("lux"));
    }

    @Test
    void invalidAffordablePairConsumesBothInputsWithoutCreatingKnowledge() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        AspectCatalog catalog = AspectFixtures.firstDiscoveryCatalog();
        int aerBefore = knowledge.aspectAmount("aer");
        int ordoBefore = knowledge.aspectAmount("ordo");

        AspectCombinationService.Result invalid = AspectCombinationService.combine(
                catalog,
                knowledge,
                "aer",
                "ordo"
        );
        assertEquals(AspectCombinationService.Status.NO_COMBINATION, invalid.status());
        assertEquals(aerBefore - 1, knowledge.aspectAmount("aer"));
        assertEquals(ordoBefore - 1, knowledge.aspectAmount("ordo"));
    }

    @Test
    void emptyPoolDoesNotConsumeOrCreateKnowledge() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        AspectCatalog catalog = AspectFixtures.firstDiscoveryCatalog();

        for (int index = 0; index < PlayerThaumKnowledge.STARTING_PRIMAL_AMOUNT; index++) {
            assertTrue(AspectCombinationService.combine(catalog, knowledge, "aer", "ignis").combined());
        }
        AspectCombinationService.Result exhausted = AspectCombinationService.combine(
                catalog,
                knowledge,
                "aer",
                "ignis"
        );
        assertEquals(AspectCombinationService.Status.NOT_ENOUGH_POINTS, exhausted.status());
        assertEquals(
                PlayerThaumKnowledge.STARTING_PRIMAL_AMOUNT + 2,
                knowledge.aspectAmount("lux")
        );
    }
}
