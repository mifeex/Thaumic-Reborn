package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ScanRegistryTest {
    @AfterEach
    void resetRegistry() {
        ScanRegistry.replace(List.of());
    }

    @Test
    void serializedDefinitionsPreserveTargetNamesAndEmbeddedAspectAmounts() {
        ScanDefinition stone = new ScanDefinition(
                ScanTargetType.BLOCK,
                "minecraft:stone",
                "block.minecraft.stone",
                List.of(new AspectReward("terra", 3))
        );
        ScanRegistry.replace(List.of(stone));

        List<ScanDefinition> restored = ScanRegistry.deserialize(ScanRegistry.serialize());

        assertEquals(List.of(stone), restored);
    }

    @Test
    void serializedDefinitionsPreserveSharedKnowledgeKey() {
        ScanDefinition pennant = new ScanDefinition(
                ScanTargetType.ITEM_TAG,
                "thaumcraftmodern:thaumcraft_banners",
                "",
                List.of(new AspectReward("pannus", 3)),
                "thaumcraftmodern:vanilla_banners"
        );
        ScanRegistry.replace(List.of(pennant));

        ScanDefinition restored = ScanRegistry.deserialize(ScanRegistry.serialize()).get(0);

        assertEquals("item_tag:thaumcraftmodern:thaumcraft_banners", restored.scanKey());
        assertEquals("thaumcraftmodern:vanilla_banners", restored.knowledgeKey());
    }

    @Test
    void targetLookupReturnsSharedKnowledgeKey() {
        ScanDefinition familyMember = new ScanDefinition(
                ScanTargetType.BLOCK,
                "minecraft:andesite_slab",
                "",
                List.of(new AspectReward("terra", 2)),
                "block_tag:thaumcraftmodern:andesite_family"
        );
        ScanRegistry.replace(List.of(familyMember));

        assertEquals(
                "block_tag:thaumcraftmodern:andesite_family",
                ScanRegistry.knowledgeKey(
                        ScanTargetType.BLOCK,
                        "minecraft:andesite_slab"
                )
        );
    }

    @Test
    void implicitAspectTagKeepsEachResolvedBlockIndividuallyScannable() {
        ScanDefinition cobblestone = new ScanDefinition(
                ScanTargetType.BLOCK_TAG,
                "forge:cobblestone",
                "",
                List.of(new AspectReward("terra", 1))
        );

        assertEquals(
                "block:minecraft:cobblestone_stairs",
                ScanRegistry.resolvedKnowledgeKey(
                        cobblestone,
                        ScanTargetType.BLOCK,
                        "minecraft:cobblestone_stairs"
                )
        );
    }

    @Test
    void narrowerAspectTagWinsOverBroadForgeStoneTag() {
        ScanDefinition forgeStone = new ScanDefinition(
                ScanTargetType.BLOCK_TAG,
                "forge:stone",
                "",
                List.of(new AspectReward("terra", 2))
        );
        ScanDefinition graniteFamily = new ScanDefinition(
                ScanTargetType.BLOCK_TAG,
                "thaumcraftmodern:granite_family",
                "",
                List.of(new AspectReward("terra", 2), new AspectReward("ignis", 2))
        );

        ScanDefinition selected = List.of(forgeStone, graniteFamily).stream()
                .min(ScanRegistry.tagDefinitionComparator(definition ->
                        definition == graniteFamily ? 7 : 128))
                .orElseThrow();

        assertEquals(graniteFamily, selected);
    }

    @Test
    void explicitTagKnowledgeKeyStillSharesProgress() {
        ScanDefinition beds = new ScanDefinition(
                ScanTargetType.BLOCK_TAG,
                "minecraft:beds",
                "",
                List.of(new AspectReward("pannus", 9)),
                "thaumcraftmodern:vanilla_beds"
        );

        assertEquals(
                "thaumcraftmodern:vanilla_beds",
                ScanRegistry.resolvedKnowledgeKey(
                        beds,
                        ScanTargetType.BLOCK,
                        "minecraft:red_bed"
                )
        );
    }

    @Test
    void runtimeDefinitionsNeverOverrideAndAreRemovedWithTheGeneratedLayer() {
        ScanDefinition explicit = new ScanDefinition(
                ScanTargetType.ITEM, "example:gear", "",
                List.of(new AspectReward("machina", 7)));
        ScanDefinition collision = new ScanDefinition(
                ScanTargetType.ITEM, "example:gear", "",
                List.of(new AspectReward("machina", 1)));
        ScanDefinition generated = new ScanDefinition(
                ScanTargetType.ITEM, "example:shaft", "",
                List.of(new AspectReward("machina", 2)));
        ScanRegistry.replace(List.of(explicit));

        ScanRegistry.replaceGenerated(List.of(collision, generated));
        assertEquals(explicit, ScanRegistry.findHistorical(
                ScanTargetType.ITEM, "example:gear").orElseThrow());
        assertEquals(generated, ScanRegistry.findHistorical(
                ScanTargetType.ITEM, "example:shaft").orElseThrow());

        ScanRegistry.replaceGenerated(List.of());
        assertFalse(ScanRegistry.find(
                ScanTargetType.ITEM, "example:shaft", false,
                (type, target) -> Optional.empty()).isPresent());
    }

    @Test
    void missingDefinitionDoesNotUseHeuristicsByDefault() {
        ScanRegistry.replace(List.of());

        assertFalse(
                ScanRegistry.find(ScanTargetType.ITEM, "minecraft:feather")
                        .isPresent()
        );
    }

    @Test
    void phenomenonDefinitionKeepsStableNodeScanKeyAcrossSync() {
        ScanDefinition node = new ScanDefinition(
                ScanTargetType.PHENOMENON,
                "thaumcraftmodern:aura_node",
                "block.thaumcraftmodern.aura_node",
                List.of()
        );
        ScanRegistry.replace(List.of(node));

        ScanDefinition restored = ScanRegistry.deserialize(ScanRegistry.serialize())
                .get(0);

        assertEquals(node, restored);
        assertEquals(
                "phenomenon:thaumcraftmodern:aura_node",
                restored.scanKey()
        );
    }

    @Test
    void explicitDefinitionAlwaysWinsInFidelityMode() {
        ScanDefinition feather = new ScanDefinition(
                ScanTargetType.ITEM,
                "minecraft:feather",
                "item.minecraft.feather",
                List.of(new AspectReward("aer", 2))
        );
        ScanRegistry.replace(List.of(feather));

        assertEquals(
                feather,
                ScanRegistry.find(
                                ScanTargetType.ITEM,
                                "minecraft:feather",
                                true,
                                (type, targetId) -> {
                                    throw new AssertionError(
                                            "Fallback must not replace an explicit definition"
                                    );
                                }
                        )
                        .orElseThrow()
        );
    }

    @Test
    void automaticFallbackCanBeEnabledExplicitly() {
        ScanRegistry.replace(List.of());
        ScanDefinition inferred = new ScanDefinition(
                ScanTargetType.ITEM,
                "minecraft:structure_void",
                "",
                List.of(new AspectReward("ordo", 1))
        );

        assertEquals(
                inferred,
                ScanRegistry.find(
                                ScanTargetType.ITEM,
                                "minecraft:structure_void",
                                true,
                                (type, targetId) -> Optional.of(inferred)
                        )
                        .orElseThrow()
        );
    }

    @Test
    void deepslateInfusedStoneUsesTheOrdinaryOreScanKey() {
        assertEquals(
                "thaumcraftmodern:air_infused_stone",
                ScanRegistry.canonicalBlockId(
                        "thaumcraftmodern:deepslate_air_infused_stone"
                )
        );
        assertEquals(
                "thaumcraftmodern:entropy_infused_stone",
                ScanRegistry.canonicalBlockId(
                        "thaumcraftmodern:deepslate_entropy_infused_stone"
                )
        );
    }
}
