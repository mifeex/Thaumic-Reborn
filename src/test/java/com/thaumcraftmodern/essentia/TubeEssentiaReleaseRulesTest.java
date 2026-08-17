package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.essentia.tube.TubeEssentiaReleaseRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TubeEssentiaReleaseRulesTest {
    private static final String ICON = "thaumic_reborn:aspects/test";
    private static final AspectCatalog CATALOG = new AspectCatalog(List.of(
            primal("aer"), primal("ignis"), primal("aqua"),
            primal("terra"), primal("ordo"), primal("perditio"),
            compound("lux", "aer", "ignis"),
            compound("motus", "aer", "ordo"),
            compound("volatus", "aer", "motus"),
            compound("alienis", "lux", "motus")
    ));

    @Test
    void directCompositionDefinesTheFourRequestedComplexityBands() {
        assertBand("aer", 1, 125);
        assertBand("lux", 2, 250);
        assertBand("volatus", 3, 500);
        assertBand("alienis", 4, 750);
    }

    @Test
    void fluxAppearsOnlyWhenAccumulatedRiskStrictlyExceedsEight() {
        int risk = 0;
        for (int interaction = 0; interaction < 8; interaction++) {
            TubeEssentiaReleaseRules.Release release =
                    TubeEssentiaReleaseRules.accumulate(risk,
                            TubeEssentiaReleaseRules.Complexity.PRIMAL);
            assertFalse(release.createsFlux());
            risk = release.accumulatedRisk();
        }
        assertEquals(8, risk);
        assertTrue(TubeEssentiaReleaseRules.accumulate(risk,
                TubeEssentiaReleaseRules.Complexity.PRIMAL).createsFlux());
    }

    @Test
    void releaseBurnsTheConfiguredAmountOfAllSixPrimalVisTypes() {
        for (TubeEssentiaReleaseRules.Complexity complexity
                : TubeEssentiaReleaseRules.Complexity.values()) {
            var cost = TubeEssentiaReleaseRules.baseVisCostCentivis(complexity);
            assertEquals(6, cost.size());
            assertTrue(cost.values().stream().allMatch(
                    amount -> amount == complexity.visCentivis()));
        }
    }

    private static void assertBand(String aspect, int risk, int centivis) {
        TubeEssentiaReleaseRules.Complexity complexity =
                TubeEssentiaReleaseRules.complexity(CATALOG, aspect);
        assertEquals(risk, complexity.risk());
        assertEquals(centivis, complexity.visCentivis());
    }

    private static AspectDefinition primal(String id) {
        return new AspectDefinition(id, 0xFFFFFF, ICON);
    }

    private static AspectDefinition compound(String id, String first, String second) {
        return new AspectDefinition(id, 0xFFFFFF, ICON, first, second);
    }
}
