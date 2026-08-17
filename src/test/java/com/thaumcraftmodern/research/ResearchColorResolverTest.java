package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import com.thaumcraftmodern.aspect.AspectDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ResearchColorResolverTest {
    @Test
    void usesExactColorOfFirstTc4ResearchAspect() {
        Map<String, AspectDefinition> aspects = Map.of(
                "cognitio", new AspectDefinition(
                        "cognitio", 0xF9967A,
                        "thaumic_reborn:aspects/cognitio")
        );
        assertEquals(0xF9967A, ResearchColorResolver.color(
                List.of(new AspectCost("cognitio", 6)),
                id -> Optional.ofNullable(aspects.get(id))
        ));
    }

    @Test
    void unknownOrEmptyResearchUsesClassicGrayFallback() {
        assertEquals(0x999999, ResearchColorResolver.color(
                List.of(), ignored -> Optional.empty()
        ));
        assertEquals(0x999999, ResearchColorResolver.color(
                List.of(new AspectCost("cognitio", 6)),
                ignored -> Optional.empty()
        ));
    }
}
