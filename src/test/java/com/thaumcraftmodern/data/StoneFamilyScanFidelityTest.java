package com.thaumcraftmodern.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class StoneFamilyScanFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCANS = ROOT.resolve(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/scans");
    private static final Path TC_TAGS = ROOT.resolve(
            "src/main/resources/data/thaumcraftmodern/tags/blocks");

    @Test
    void modernStoneFamiliesDeclareTheirDistinctAspectProfiles() throws IOException {
        assertScan("vanilla_granite_family.json", "thaumcraftmodern:granite_family",
                Map.of("terra", 2, "ignis", 1));
        assertScan("vanilla_diorite_family.json", "thaumcraftmodern:diorite_family",
                Map.of("terra", 2, "ordo", 1));
        assertScan("vanilla_andesite_family.json", "thaumcraftmodern:andesite_family",
                Map.of("terra", 2, "aer", 1));

        assertTag(TC_TAGS.resolve("granite_family.json"),
                Set.of("minecraft:granite", "minecraft:polished_granite",
                        "minecraft:granite_slab", "minecraft:granite_stairs",
                        "minecraft:granite_wall", "minecraft:polished_granite_slab",
                        "minecraft:polished_granite_stairs"));
        assertTag(TC_TAGS.resolve("diorite_family.json"),
                Set.of("minecraft:diorite", "minecraft:polished_diorite",
                        "minecraft:diorite_slab", "minecraft:diorite_stairs",
                        "minecraft:diorite_wall", "minecraft:polished_diorite_slab",
                        "minecraft:polished_diorite_stairs"));
        assertTag(TC_TAGS.resolve("andesite_family.json"),
                Set.of("minecraft:andesite", "minecraft:polished_andesite",
                        "minecraft:andesite_slab", "minecraft:andesite_stairs",
                        "minecraft:andesite_wall", "minecraft:polished_andesite_slab",
                        "minecraft:polished_andesite_stairs"));
    }

    @Test
    void legacyCobblestoneCompatibilityStillIncludesStoneShapes() throws IOException {
        assertScan("legacy/object_001_cobblestone.json", "forge:cobblestone",
                Map.of("terra", 1, "perditio", 1));
        assertTag(ROOT.resolve("src/main/resources/data/forge/tags/blocks/cobblestone.json"),
                Set.of("minecraft:andesite_slab", "minecraft:andesite_stairs",
                        "minecraft:andesite_wall", "minecraft:diorite_slab",
                        "minecraft:diorite_stairs", "minecraft:diorite_wall",
                        "minecraft:granite_slab", "minecraft:granite_stairs",
                        "minecraft:granite_wall", "minecraft:polished_andesite_slab",
                        "minecraft:polished_andesite_stairs",
                        "minecraft:polished_diorite_slab",
                        "minecraft:polished_diorite_stairs",
                        "minecraft:polished_granite_slab",
                        "minecraft:polished_granite_stairs"));
    }

    @Test
    void brickSandstoneNetherAndQuartzFamiliesUseTheirOriginalBases() throws IOException {
        assertScan("legacy/object_075_new_itemstack_blocks.field_150417_av_1_32767.json",
                "thaumcraftmodern:stone_brick_equivalents", Map.of("terra", 2));
        assertScan("legacy/object_076_new_itemstack_blocks.field_150417_av_1_1.json",
                "thaumcraftmodern:mossy_stone_brick_equivalents",
                Map.of("terra", 1, "herba", 1));
        assertScan("legacy/object_079_new_itemstack_blocks.field_150322_a_1_32767.json",
                "thaumcraftmodern:sandstone_equivalents",
                Map.of("terra", 3, "perditio", 3));
        assertScan("legacy/object_080_new_itemstack_blocks.field_150322_a_1_1.json",
                "thaumcraftmodern:chiseled_sandstone_equivalents",
                Map.of("terra", 2, "perditio", 3, "praecantatio", 1));
        assertScan("legacy/object_081_new_itemstack_blocks.field_150322_a_1_2.json",
                "thaumcraftmodern:cut_sandstone_equivalents",
                Map.of("terra", 2, "perditio", 3, "ordo", 1));
        assertScan("legacy/object_069_new_itemstack_blocks.field_150385_bj.json",
                "thaumcraftmodern:nether_brick_equivalents",
                Map.of("terra", 2, "ignis", 1));
        assertScan("quartz_block_equivalents.json",
                "thaumcraftmodern:quartz_block_equivalents",
                Map.of("vitreus", 3, "potentia", 3));

        assertTag(TC_TAGS.resolve("stone_brick_equivalents.json"),
                Set.of("minecraft:stone_brick_slab", "minecraft:stone_brick_stairs",
                        "minecraft:stone_brick_wall"));
        assertTag(TC_TAGS.resolve("sandstone_equivalents.json"),
                Set.of("minecraft:red_sandstone", "minecraft:smooth_sandstone",
                        "minecraft:sandstone_wall"));
        assertTag(TC_TAGS.resolve("nether_brick_equivalents.json"),
                Set.of("minecraft:red_nether_bricks", "minecraft:chiseled_nether_bricks",
                        "minecraft:cracked_nether_bricks"));
        assertTag(TC_TAGS.resolve("quartz_block_equivalents.json"),
                Set.of("minecraft:quartz_bricks", "minecraft:smooth_quartz",
                        "minecraft:quartz_slab", "minecraft:quartz_stairs"));
    }

    private static void assertScan(String file, String target,
                                   Map<String, Integer> expected) throws IOException {
        JsonObject value = json(SCANS.resolve(file));
        assertEquals("block_tag", value.get("type").getAsString());
        assertEquals(target, value.get("target").getAsString());
        Map<String, Integer> actual = new LinkedHashMap<>();
        value.getAsJsonArray("aspects").forEach(element -> {
            JsonObject aspect = element.getAsJsonObject();
            actual.put(aspect.get("id").getAsString(), aspect.get("amount").getAsInt());
        });
        assertEquals(expected, actual);
    }

    private static void assertTag(Path file, Set<String> expected) throws IOException {
        Set<String> values = new HashSet<>();
        json(file).getAsJsonArray("values").forEach(value -> values.add(value.getAsString()));
        expected.forEach(value -> assertTrue(values.contains(value),
                () -> file.getFileName() + " must contain " + value));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
