package com.thaumcraftmodern.client.screen;

import com.thaumcraftmodern.research.ResearchCategoryDefinition;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchCondition;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchRegistry;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconBrowserModelTest {
    @AfterEach
    void clearRegistries() {
        ResearchRegistry.replace(List.of());
        ResearchCategoryRegistry.replace(List.of());
    }

    @Test
    void categoryViewCachesVisibleNodesConnectionsAndBounds() {
        ResearchCategoryRegistry.replace(List.of(new ResearchCategoryDefinition(
                "basics",
                "category.basics",
                "minecraft:book",
                "minecraft:textures/gui/options_background.png",
                0
        )));
        ResearchRegistry.replace(List.of(
                definition("parent", -20, 10, false, List.of(), List.of("sibling")),
                definition("child", 30, -5, false, List.of("parent"), List.of()),
                definition("sibling", 50, 40, false, List.of(), List.of()),
                definition("virtual", 1000, 1000, true, List.of(), List.of())
        ));

        ThaumonomiconBrowserModel model = ThaumonomiconBrowserModel.create();
        ThaumonomiconBrowserModel.CategoryView view =
                model.categoryView("basics", research -> true);

        assertEquals(3, view.research().size());
        assertEquals(2, view.connections().size());
        assertEquals(
                new ThaumonomiconBrowserModel.Bounds(-20, 50, -5, 40),
                view.bounds()
        );
        assertTrue(model.isCurrent());

        ResearchRegistry.replace(List.of());

        assertFalse(model.isCurrent());
    }

    private static ResearchDefinition definition(
            String id,
            int x,
            int y,
            boolean virtual,
            List<String> parents,
            List<String> siblings
    ) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "",
                "research." + id,
                "",
                false,
                false,
                false,
                virtual,
                "",
                parents,
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                x,
                y,
                List.of(),
                0,
                ResearchDefinition.NodeFrame.PRIMARY,
                false,
                List.of(),
                List.of(),
                siblings
        );
    }
}
