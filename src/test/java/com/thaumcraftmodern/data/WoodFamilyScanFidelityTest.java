package com.thaumcraftmodern.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WoodFamilyScanFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCANS = ROOT.resolve(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans/legacy"
    );
    private static final Path BLOCK_TAGS = ROOT.resolve(
            "src/main/resources/data/minecraft/tags/blocks"
    );

    @Test
    void everyLogAndStrippedWoodUsesTheOakLogScan() throws IOException {
        assertTagScan("object_002_logwood.json", "minecraft:logs",
                Map.of("arbor", 4));
        assertTagContains("logs.json", Set.of(
                "#minecraft:bamboo_blocks",
                "minecraft:bamboo",
                "thaumic_reborn:greatwood_log",
                "thaumic_reborn:silverwood_log",
                "thaumic_reborn:silverwood_node"
        ));
    }

    @Test
    void everyPlankSlabAndStairUsesItsOakEquivalentScan() throws IOException {
        assertTagScan("object_003_plankwood.json", "minecraft:planks",
                Map.of("arbor", 1));
        assertTagScan("object_004_slabwood.json", "minecraft:wooden_slabs",
                Map.of("arbor", 1));
        assertTagScan("object_005_stairwood.json", "minecraft:wooden_stairs",
                Map.of("arbor", 1));

        assertTagContains("planks.json", Set.of(
                "minecraft:bamboo_mosaic",
                "thaumic_reborn:greatwood_planks",
                "thaumic_reborn:silverwood_planks"
        ));
        assertTagContains("wooden_slabs.json", Set.of(
                "minecraft:bamboo_mosaic_slab",
                "thaumic_reborn:greatwood_slab",
                "thaumic_reborn:silverwood_slab"
        ));
        assertTagContains("wooden_stairs.json", Set.of(
                "minecraft:bamboo_mosaic_stairs",
                "thaumic_reborn:greatwood_stairs",
                "thaumic_reborn:silverwood_stairs"
        ));
    }

    @Test
    void everyLeafBlockUsesTheOakLeavesScan() throws IOException {
        assertTagScan("object_008_treeleaves.json", "minecraft:leaves",
                Map.of("herba", 1));
        assertTagContains("leaves.json", Set.of(
                "thaumic_reborn:greatwood_leaves",
                "thaumic_reborn:silverwood_leaves",
                "thaumic_reborn:tainted_leaves"
        ));
    }

    @Test
    void bambooStalkAndSaplingUseTheOakTreeScans() throws IOException {
        assertTagScan("object_007_treesapling.json", "minecraft:saplings",
                Map.of("arbor", 1, "herba", 2));
        assertTagContains("saplings.json", Set.of(
                "minecraft:bamboo_sapling",
                "thaumic_reborn:greatwood_sapling",
                "thaumic_reborn:silverwood_sapling"
        ));
    }

    @Test
    void everyCraftedWoodFamilyUsesTheOakEquivalentScan() throws IOException {
        assertTagScan("object_220_new_itemstack_blocks.field_150471_bo_1_32767.json",
                "minecraft:wooden_buttons", Map.of("machina", 1));
        assertTagScan("object_201_new_itemstack_blocks.field_150396_be_1_32767.json",
                "minecraft:fence_gates", Map.of("arbor", 4, "machina", 1, "iter", 1));
        assertTagScan("object_202_new_itemstack_blocks.field_150452_aw_1_32767.json",
                "minecraft:wooden_pressure_plates",
                Map.of("arbor", 1, "machina", 1, "sensus", 1));
        assertTagScan("object_212_new_itemstack_blocks.field_150415_at_1_32767.json",
                "minecraft:wooden_trapdoors", Map.of("arbor", 2, "motus", 1));

        assertStandaloneTagScan("wooden_doors.json", "minecraft:wooden_doors",
                Map.of("arbor", 4, "machina", 1, "motus", 1));
        assertStandaloneTagScan("wooden_fences.json", "minecraft:wooden_fences",
                Map.of("arbor", 2));
        assertStandaloneTagScan("all_wooden_signs.json", "minecraft:all_signs",
                Map.of("arbor", 1));
    }

    private static void assertTagScan(
            String file,
            String target,
            Map<String, Integer> expectedAspects
    ) throws IOException {
        JsonObject scan = json(SCANS.resolve(file));
        assertEquals("block_tag", scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        assertEquals(expectedAspects, aspects(scan));
    }

    private static void assertTagContains(String file, Set<String> expected)
            throws IOException {
        JsonArray values = json(BLOCK_TAGS.resolve(file)).getAsJsonArray("values");
        Set<String> actual = new java.util.HashSet<>();
        values.forEach(value -> actual.add(value.getAsString()));
        expected.forEach(value -> assertTrue(actual.contains(value),
                () -> file + " must contain " + value));
    }

    private static void assertStandaloneTagScan(
            String file,
            String target,
            Map<String, Integer> expectedAspects
    ) throws IOException {
        JsonObject scan = json(SCANS.getParent().resolve(file));
        assertEquals("block_tag", scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        assertEquals(expectedAspects, aspects(scan));
    }

    private static Map<String, Integer> aspects(JsonObject scan) {
        Map<String, Integer> result = new LinkedHashMap<>();
        scan.getAsJsonArray("aspects").forEach(element -> {
            JsonObject aspect = element.getAsJsonObject();
            result.merge(aspect.get("id").getAsString(),
                    aspect.get("amount").getAsInt(), Integer::sum);
        });
        return result;
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
