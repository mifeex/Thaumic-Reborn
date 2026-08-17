package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WizardTowerLootFidelityTest {
    private static final Path TABLE = Path.of(
            "src/main/resources/data/thaumic_reborn/loot_tables/chests/"
                    + "wizard_tower.json"
    );

    @Test
    void entriesAndWeightsMatchTc4235TowerChest() throws IOException {
        JsonObject root = JsonParser.parseString(Files.readString(TABLE))
                .getAsJsonObject();
        JsonObject pool = root.getAsJsonArray("pools")
                .get(0)
                .getAsJsonObject();
        JsonObject rolls = pool.getAsJsonObject("rolls");
        assertEquals(4, rolls.get("min").getAsInt());
        assertEquals(9, rolls.get("max").getAsInt());

        Map<String, Integer> actual = new LinkedHashMap<>();
        pool.getAsJsonArray("entries").forEach(element -> {
            JsonObject entry = element.getAsJsonObject();
            actual.put(
                    entry.get("name").getAsString(),
                    entry.get("weight").getAsInt()
            );
        });
        assertEquals(
                Map.ofEntries(
                        Map.entry("minecraft:glowstone_dust", 3),
                        Map.entry("minecraft:glass_bottle", 10),
                        Map.entry("minecraft:gold_nugget", 5),
                        Map.entry("minecraft:fire_charge", 5),
                        Map.entry("minecraft:skeleton_skull", 3),
                        Map.entry("thaumic_reborn:knowledge_fragment", 20),
                        Map.entry("thaumic_reborn:alumentum", 5),
                        Map.entry("thaumic_reborn:nitor", 5),
                        Map.entry("thaumic_reborn:thaumium_ingot", 5),
                        Map.entry("thaumic_reborn:thaumonomicon", 20)
                ),
                actual
        );
    }

    @Test
    void bankerHomeDoesNotInventAChestTable() {
        assertFalse(Files.exists(TABLE.resolveSibling("banker_home.json")));
    }
}
