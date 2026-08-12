package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchDefinition;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconResearchInteractionTest {
    @Test
    void hitTestingUsesExactInnerViewportAndFirstRenderedNode() {
        ResearchDefinition first = definition("first");
        ResearchDefinition second = definition("second");

        assertEquals(
                first,
                ThaumonomiconResearchInteraction.researchAt(
                        List.of(first, second),
                        ignored -> 20,
                        ignored -> 20,
                        26,
                        25,
                        25
                ).orElseThrow()
        );
        assertTrue(ThaumonomiconResearchInteraction.isWithinViewport(16, 16));
        assertTrue(!ThaumonomiconResearchInteraction.isWithinViewport(10, 10));
    }

    private static ResearchDefinition definition(String id) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "research." + id,
                "",
                false,
                false,
                false,
                "",
                List.of(),
                0,
                0,
                List.of()
        );
    }
}
