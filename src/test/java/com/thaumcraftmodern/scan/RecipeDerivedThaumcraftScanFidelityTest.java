package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecipeDerivedThaumcraftScanFidelityTest {
    private static final Path ROOT = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans/recipe_derived");

    @Test
    void classicRecipeDerivedBlocksHaveMaterializedScans() throws IOException {
        List<String> expected = List.of(
                "nitor", "deconstruction_table", "arcane_pedestal",
                "runic_matrix", "wand_recharge_pedestal", "mnemonic_matrix",
                "paving_stone_of_travel", "paving_stone_of_warding",
                "air_crystal_cluster", "fire_crystal_cluster",
                "water_crystal_cluster", "earth_crystal_cluster",
                "order_crystal_cluster", "entropy_crystal_cluster",
                "balanced_crystal_cluster"
        );
        for (String id : expected) {
            JsonObject scan = json(ROOT.resolve(id + ".json"));
            assertEquals("thaumic_reborn:" + id, scan.get("target").getAsString());
            assertFalse(scan.get("inactive").getAsBoolean());
            assertFalse(scan.getAsJsonArray("aspects").isEmpty());
            assertTrue(scan.has("recipe_derivation"));
            assertTrue(scan.has("derivation_source"));
        }
    }

    @Test
    void tableMetadataTwoMapsToResearchTable() throws IOException {
        JsonObject scan = json(Path.of("src/main/resources/data/thaumic_reborn/"
                + "thaumcraft/scans/legacy/"
                + "object_233_new_itemstack_configblocks.blocktable_1_2.json"));
        assertEquals("thaumic_reborn:research_table",
                scan.get("target").getAsString());
    }

    @Test
    void generatorKeepsExactClassicScalingRules() throws IOException {
        String source = Files.readString(Path.of("tools/generate_recipe_derived_scans.py"));
        assertTrue(source.contains("int(amount * 0.75 / count)"));
        assertTrue(source.contains("int(math.sqrt(amount) / count)"));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
