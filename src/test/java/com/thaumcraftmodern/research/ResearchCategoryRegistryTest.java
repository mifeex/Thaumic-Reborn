package com.thaumcraftmodern.research;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchCategoryRegistryTest {

    @Test
    void allReusesImmutableSnapshotUntilRegistryReplacement() {
        ResearchCategoryRegistry.replace(List.of(definition("cached", 0)));

        List<ResearchCategoryDefinition> first =
                ResearchCategoryRegistry.all();
        long firstRevision = ResearchCategoryRegistry.revision();

        assertSame(first, ResearchCategoryRegistry.all());

        ResearchCategoryRegistry.replace(List.of(definition("replacement", 0)));

        assertEquals(firstRevision + 1L, ResearchCategoryRegistry.revision());
        assertFalse(first == ResearchCategoryRegistry.all());
    }

    @Test
    void categoriesKeepDataDrivenOrderThroughNetworkSerialization() {
        ResearchCategoryDefinition later = new ResearchCategoryDefinition(
                "research",
                "category.research",
                "minecraft:book",
                "minecraft:textures/gui/options_background.png",
                10
        );
        ResearchCategoryDefinition first = new ResearchCategoryDefinition(
                "basics",
                "category.basics",
                "minecraft:paper",
                "minecraft:textures/gui/options_background.png",
                0
        );
        ResearchCategoryRegistry.replace(List.of(later, first));

        List<ResearchCategoryDefinition> restored = ResearchCategoryRegistry.deserialize(
                ResearchCategoryRegistry.serialize()
        );

        assertEquals(List.of(first, later), restored);
    }

    @Test
    void categoryResourceIconSurvivesNetworkSerialization() {
        ResearchCategoryDefinition original = new ResearchCategoryDefinition(
                "alchemy",
                "category.alchemy",
                "",
                "thaumic_reborn:textures/misc/r_crucible.png",
                "thaumic_reborn:textures/gui/gui_researchback.png",
                2
        );
        ResearchCategoryRegistry.replace(List.of(original));

        ResearchCategoryDefinition restored = ResearchCategoryRegistry.deserialize(
                ResearchCategoryRegistry.serialize()
        ).get(0);

        assertEquals(original, restored);
        assertTrue(restored.iconItem().isBlank());
    }

    private static ResearchCategoryDefinition definition(
            String id,
            int order
    ) {
        return new ResearchCategoryDefinition(
                id,
                "category." + id,
                "minecraft:book",
                "minecraft:textures/gui/options_background.png",
                order
        );
    }
}
