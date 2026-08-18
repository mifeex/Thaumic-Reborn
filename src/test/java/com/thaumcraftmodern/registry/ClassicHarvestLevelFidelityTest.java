package com.thaumcraftmodern.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the TC4 setHarvestLevel families after their metadata split into blocks. */
class ClassicHarvestLevelFidelityTest {
    private static final Path TAG_ROOT = Path.of(
            "src/main/resources/data/minecraft/tags/blocks");

    @Test
    void stonePickaxeTierMatchesClassicMetalDevices() throws IOException {
        assertEquals(Set.of(
                id("crucible"),
                id("arcane_lamp"),
                id("lamp_growth"),
                id("lamp_fertility"),
                id("item_grate"),
                id("arcane_alembic"),
                id("mnemonic_matrix"),
                id("alchemical_construct"),
                id("advanced_alchemical_construct"),
                id("thaumatorium")
        ), values("needs_stone_tool.json"));
    }

    @Test
    void ironPickaxeTierMatchesClassicOresAndStructureBlocks()
            throws IOException {
        assertEquals(Set.of(
                id("infernal_furnace"),
                id("advanced_alchemical_furnace"),
                id("cinnabar_ore"),
                id("air_infused_stone"),
                id("fire_infused_stone"),
                id("water_infused_stone"),
                id("earth_infused_stone"),
                id("order_infused_stone"),
                id("entropy_infused_stone"),
                id("amber_ore"),
                id("eldritch_altar_part"),
                id("eldritch_glyphed_stone"),
                id("eldritch_glowing_crust"),
                id("ancient_stone"),
                id("ancient_rock"),
                id("ancient_stairs"),
                id("ancient_slab"),
                id("ancient_crust")
        ), values("needs_iron_tool.json"));
    }

    @Test
    void classicTaintBlocksUseShovelsWithoutATierRequirement()
            throws IOException {
        assertEquals(Set.of(id("crusted_taint"), id("tainted_soil")),
                values("mineable/shovel.json"));
    }

    @Test
    void classicWoodBlocksAndTheirSplitShapesUseAxes() throws IOException {
        assertEquals(Set.of(
                id("research_table"),
                id("thaumcraft_table"),
                id("arcane_workbench"),
                id("deconstruction_table"),
                id("loot_crate"),
                id("greatwood_log"),
                id("silverwood_log"),
                id("silverwood_node"),
                id("greatwood_planks"),
                id("silverwood_planks"),
                id("greatwood_stairs"),
                id("silverwood_stairs"),
                id("greatwood_slab"),
                id("silverwood_slab")
                , id("arcane_bellows")
                , id("arcane_ear")
                , id("arcane_pressure_plate")
                , id("arcane_bore_base")
                , id("arcane_bore")
        ), values("mineable/axe.json"));
    }

    @Test
    void classicTubesArePickaxeMineableWithoutAHarvestTier()
            throws IOException {
        Set<String> pickaxe = values("mineable/pickaxe.json");
        Set<String> tubes = Set.of(
                id("essentia_tube"),
                id("filtered_essentia_tube"),
                id("restricted_essentia_tube"),
                id("one_way_essentia_tube"),
                id("essentia_valve"),
                id("reversible_essentia_tube")
        );
        assertTrue(pickaxe.containsAll(tubes));
        assertTrue(java.util.Collections.disjoint(
                tubes,
                values("needs_stone_tool.json")
        ));
        assertTrue(java.util.Collections.disjoint(
                tubes,
                values("needs_iron_tool.json")
        ));
        String blocks = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java"
        ));
        assertTrue(blocks.contains(".strength(0.5F, 5.0F)"));
        assertTrue(blocks.contains("classicTubeProperties().noOcclusion()"));
    }

    private static Set<String> values(String relativePath) throws IOException {
        JsonArray array = JsonParser.parseString(Files.readString(
                TAG_ROOT.resolve(relativePath))).getAsJsonObject()
                .getAsJsonArray("values");
        Set<String> values = new HashSet<>();
        array.forEach(element -> values.add(element.getAsString()));
        return values;
    }

    private static String id(String path) {
        return "thaumic_reborn:" + path;
    }
}
