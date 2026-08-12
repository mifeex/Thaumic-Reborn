package com.thaumcraftmodern.research;

import com.thaumcraftmodern.aspect.AspectCost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchRegistryTest {
    @AfterEach
    void clearRegistry() {
        ResearchRegistry.replace(List.of());
    }

    @Test
    void allReusesImmutableSnapshotUntilRegistryReplacement() {
        ResearchRegistry.replace(List.of(definition("cached", false)));

        List<ResearchDefinition> first = ResearchRegistry.all();
        long firstRevision = ResearchRegistry.revision();

        assertSame(first, ResearchRegistry.all());

        ResearchRegistry.replace(List.of(definition("replacement", false)));

        assertEquals(firstRevision + 1L, ResearchRegistry.revision());
        assertFalse(first == ResearchRegistry.all());
    }

    @Test
    void networkSerializationPreservesAutoUnlockInactiveAndVirtualFlags() {
        ResearchRegistry.replace(List.of(
                definition("automatic", true),
                definition("manual", false),
                definition("inactive", true, true),
                new ResearchDefinition(
                        "virtual",
                        "basics",
                        "minecraft:book",
                        "",
                        "research.virtual",
                        "",
                        false,
                        true,
                        false,
                        true,
                        "",
                        List.of(),
                        List.of(),
                        ResearchCondition.ALWAYS,
                        ResearchCondition.ALWAYS,
                        0,
                        0,
                        List.of()
                )
        ));

        List<ResearchDefinition> restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        );

        assertTrue(find(restored, "automatic").autoUnlock());
        assertFalse(find(restored, "manual").autoUnlock());
        assertTrue(find(restored, "inactive").inactive());
        assertTrue(find(restored, "virtual").virtual());
    }

    @Test
    void missingSerializedAutoUnlockDefaultsToFalse() {
        ResearchRegistry.replace(List.of(definition("automatic", true)));
        CompoundTag serialized = ResearchRegistry.serialize();
        serialized.getList("entries", Tag.TAG_COMPOUND)
                .getCompound(0)
                .remove("autoUnlock");

        ResearchDefinition restored = ResearchRegistry.deserialize(serialized).get(0);

        assertFalse(restored.autoUnlock());
    }

    @Test
    void missingSerializedVirtualDefaultsToFalse() {
        ResearchRegistry.replace(List.of(
                new ResearchDefinition(
                        "virtual",
                        "basics",
                        "minecraft:book",
                        "",
                        "research.virtual",
                        "",
                        false,
                        true,
                        false,
                        true,
                        "",
                        List.of(),
                        List.of(),
                        ResearchCondition.ALWAYS,
                        ResearchCondition.ALWAYS,
                        0,
                        0,
                        List.of()
                )
        ));
        CompoundTag serialized = ResearchRegistry.serialize();
        serialized.getList("entries", Tag.TAG_COMPOUND)
                .getCompound(0)
                .remove("virtual");

        ResearchDefinition restored = ResearchRegistry.deserialize(serialized).get(0);

        assertFalse(restored.virtual());
    }

    @Test
    void networkSerializationPreservesCompletionWarp() {
        ResearchDefinition warped = new ResearchDefinition(
                "warped",
                "basics",
                "minecraft:book",
                "",
                "research.warped",
                "",
                false,
                false,
                false,
                false,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                3,
                ResearchDefinition.NodeFrame.PRIMARY,
                false
        );
        ResearchRegistry.replace(List.of(warped));

        ResearchDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0);

        assertEquals(3, restored.completionWarp());
    }

    @Test
    void networkSerializationPreservesConfiguredNodeStyle() {
        ResearchDefinition styled = new ResearchDefinition(
                "styled",
                "basics",
                "minecraft:book",
                "",
                "research.styled",
                "",
                false,
                true,
                false,
                false,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                ResearchDefinition.NodeFrame.SECONDARY,
                true
        );
        ResearchRegistry.replace(List.of(styled));

        ResearchDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0);

        assertEquals(
                ResearchDefinition.NodeFrame.SECONDARY,
                restored.nodeFrame()
        );
        assertTrue(restored.specialFrame());
    }

    @Test
    void networkSerializationPreservesResearchPurchaseCost() {
        ResearchDefinition purchasable = new ResearchDefinition(
                "secondary",
                "basics",
                "minecraft:book",
                "",
                "research.secondary",
                "",
                false,
                false,
                false,
                false,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                0,
                ResearchDefinition.NodeFrame.SECONDARY,
                false,
                List.of(
                        new AspectCost("aer", 3),
                        new AspectCost("praecantatio", 1)
                )
        );
        ResearchRegistry.replace(List.of(purchasable));

        ResearchDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0);

        assertEquals(purchasable.purchaseCost(), restored.purchaseCost());
        assertTrue(restored.purchasable());
    }

    @Test
    void networkSerializationPreservesOriginalResearchAspectCost() {
        ResearchDefinition definition = new ResearchDefinition(
                "copyable",
                "basics",
                "minecraft:book",
                "",
                "research.copyable",
                "",
                false,
                false,
                false,
                false,
                "",
                List.of(),
                List.of(),
                ResearchCondition.ALWAYS,
                ResearchCondition.ALWAYS,
                0,
                0,
                List.of(),
                0,
                ResearchDefinition.NodeFrame.ROUND,
                false,
                List.of(
                        new AspectCost("cognitio", 6),
                        new AspectCost("ordo", 3)
                ),
                List.of(),
                List.of()
        );
        ResearchRegistry.replace(List.of(definition));

        ResearchDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0);

        assertEquals(definition.researchCost(), restored.researchCost());
    }

    @Test
    void networkSerializationPreservesHiddenParentsAndConditionTrees() {
        ResearchCondition reveal = new ResearchCondition.AllOf(List.of(
                new ResearchCondition.ResearchCompleted("parent"),
                new ResearchCondition.AnyOf(List.of(
                        new ResearchCondition.ScanCompleted(
                                "item:minecraft:ender_pearl"
                        ),
                        new ResearchCondition.WarpAtLeast(
                                ResearchCondition.WarpMeasure.NON_TEMPORARY,
                                5
                        )
                ))
        ));
        ResearchDefinition definition = new ResearchDefinition(
                "conditional",
                "basics",
                "minecraft:book",
                "research.conditional",
                "",
                true,
                false,
                false,
                "",
                List.of("parent"),
                List.of("hidden_parent"),
                reveal,
                new ResearchCondition.AspectKnown("alienis"),
                1,
                2,
                List.of()
        );
        ResearchRegistry.replace(List.of(definition));

        ResearchDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0);

        assertEquals(List.of("hidden_parent"), restored.hiddenParents());
        assertEquals(reveal, restored.revealWhen());
        assertEquals(
                new ResearchCondition.AspectKnown("alienis"),
                restored.unlockWhen()
        );
    }

    @Test
    void networkSerializationPreservesGenericPageAspectCosts() {
        ResearchPageDefinition page = new ResearchPageDefinition(
                ResearchPageDefinition.Type.TEXT,
                "",
                "research.cost.body",
                "",
                List.of(
                        new AspectCost("aer", 7),
                        new AspectCost("praecantatio", 12)
                )
        );
        ResearchDefinition definition = new ResearchDefinition(
                "costed_action",
                "basics",
                "minecraft:stick",
                "research.costed_action",
                "",
                false,
                false,
                false,
                "",
                List.of(),
                0,
                0,
                List.of(page)
        );
        ResearchRegistry.replace(List.of(definition));

        ResearchPageDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0).pages().get(0);

        assertEquals(page.aspectCosts(), restored.aspectCosts());
    }

    @Test
    void networkSerializationPreservesCyclingRecipePage() {
        List<String> recipes = List.of(
                "thaumcraftmodern:air_crystal_cluster",
                "thaumcraftmodern:balanced_crystal_cluster"
        );
        ResearchPageDefinition page = new ResearchPageDefinition(
                ResearchPageDefinition.Type.RECIPE,
                "",
                "",
                recipes.get(0),
                List.of(),
                null,
                recipes
        );
        ResearchDefinition definition = new ResearchDefinition(
                "cluster_recipes",
                "basics",
                "thaumcraftmodern:air_crystal_cluster",
                "research.cluster_recipes",
                "",
                false,
                true,
                false,
                "",
                List.of(),
                0,
                0,
                List.of(page)
        );
        ResearchRegistry.replace(List.of(definition));

        ResearchPageDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0).pages().get(0);

        assertEquals(recipes, restored.recipeIds());
        assertEquals(recipes.get(0), restored.recipeId());
    }

    @Test
    void networkSerializationPreservesInfusionDisplayLayout() {
        InfusionDisplayDefinition display = new InfusionDisplayDefinition(
                "minecraft:enchanted_book",
                "minecraft:book",
                List.of(
                        new InfusionDisplayDefinition.ComponentStack(
                                "minecraft:amethyst_shard",
                                2
                        ),
                        new InfusionDisplayDefinition.ComponentStack(
                                "minecraft:ender_pearl",
                                1
                        ),
                        InfusionDisplayDefinition.ComponentStack.tagged(
                                "minecraft:fishes",
                                1
                        )
                ),
                InfusionDisplayDefinition.Instability.DANGEROUS,
                "research.infusion.detail"
        );
        ResearchPageDefinition page = new ResearchPageDefinition(
                ResearchPageDefinition.Type.INFUSION,
                "recipe.type.infusion",
                "",
                "",
                List.of(new AspectCost("praecantatio", 64)),
                display
        );
        ResearchDefinition definition = new ResearchDefinition(
                "infusion_preview",
                "infusion_layout_test",
                "thaumcraftmodern:runic_matrix",
                "research.infusion_preview",
                "",
                false,
                true,
                false,
                "",
                List.of(),
                0,
                0,
                List.of(page)
        );
        ResearchRegistry.replace(List.of(definition));

        ResearchPageDefinition restored = ResearchRegistry.deserialize(
                ResearchRegistry.serialize()
        ).get(0).pages().get(0);

        assertEquals(ResearchPageDefinition.Type.INFUSION, restored.type());
        assertEquals(display, restored.infusionDisplay());
        assertEquals(page.aspectCosts(), restored.aspectCosts());
    }

    private static ResearchDefinition find(List<ResearchDefinition> values, String id) {
        return values.stream()
                .filter(research -> research.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static ResearchDefinition definition(String id, boolean autoUnlock) {
        return definition(id, autoUnlock, false);
    }

    private static ResearchDefinition definition(
            String id,
            boolean autoUnlock,
            boolean inactive
    ) {
        return new ResearchDefinition(
                id,
                "basics",
                "minecraft:book",
                "research." + id,
                "",
                false,
                autoUnlock,
                inactive,
                "",
                List.of(),
                0,
                0,
                List.of()
        );
    }
}
