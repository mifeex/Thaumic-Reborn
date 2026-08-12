package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchDefinition;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

final class ThaumonomiconNavigationControllerTest {
    @Test
    void linkedResearchBackRestoresResearchPageAndCategory() {
        ResearchDefinition source = definition("source", 5);
        ResearchDefinition target = definition("target", 1);
        ThaumonomiconNavigationController navigation =
                new ThaumonomiconNavigationController();

        navigation.openRoot(source);
        navigation.nextPage();
        navigation.nextPage();
        navigation.openLinked(target, "alchemy");

        assertSame(target, navigation.research());
        assertEquals(0, navigation.pagePair());
        assertEquals(1, navigation.depth());

        ThaumonomiconNavigationController.BackResult result =
                navigation.back(id -> Optional.of(source));

        assertSame(source, result.research());
        assertEquals(4, result.pagePair());
        assertEquals("alchemy", result.categoryId());
        assertEquals(0, result.remainingDepth());
    }

    @Test
    void missingReloadedResearchClosesBookAndClearsHistory() {
        ThaumonomiconNavigationController navigation =
                new ThaumonomiconNavigationController();
        navigation.openRoot(definition("source", 1));

        navigation.refresh(id -> Optional.empty());

        assertNull(navigation.research());
        assertEquals(0, navigation.pagePair());
        assertEquals(0, navigation.depth());
    }

    private static ResearchDefinition definition(String id, int pages) {
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
                java.util.stream.IntStream.range(0, pages)
                        .mapToObj(index -> new com.thaumcraftmodern.research.ResearchPageDefinition(
                                com.thaumcraftmodern.research.ResearchPageDefinition.Type.TEXT,
                                "title",
                                "body",
                                ""
                        ))
                        .toList()
        );
    }
}
