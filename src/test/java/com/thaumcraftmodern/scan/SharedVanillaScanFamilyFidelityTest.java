package com.thaumcraftmodern.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SharedVanillaScanFamilyFidelityTest {
    private static final Path SCANS = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans");
    private static final Path TAGS = Path.of(
            "src/main/resources/data/thaumic_reborn/tags/blocks");

    @Test
    void sharedFamiliesCarryRequestedAspects() throws IOException {
        assertScan("vanilla_banner_family.json", "minecraft:banners",
                aspects("alienis", 1, "arbor", 2, "pannus", 3));
        assertScan("vanilla_bed_family.json", "minecraft:beds",
                aspects("pannus", 9, "fabrico", 2, "arbor", 2));
        assertScan("vanilla_cauldron_family.json", "minecraft:cauldrons",
                aspects("metallum", 21));
        assertScan("vanilla_candle_family.json", "thaumic_reborn:candles_and_cakes",
                aspects("sensus", 1, "lux", 2));
        assertScan("vanilla_concrete_family.json", "thaumic_reborn:concrete",
                aspects("fabrico", 2, "terra", 4, "aqua", 4));
        assertScan("vanilla_concrete_powder_family.json", "thaumic_reborn:concrete_powder",
                aspects("fabrico", 2, "perditio", 2, "terra", 4));
        assertScan("vanilla_glazed_terracotta_family.json", "thaumic_reborn:glazed_terracotta",
                aspects("perfodio", 1, "ordo", 4));
    }

    @Test
    void colouredMaterialTagsContainAllSixteenColours() throws IOException {
        assertEquals(16, json(TAGS.resolve("concrete.json")).getAsJsonArray("values").size());
        assertEquals(16, json(TAGS.resolve("concrete_powder.json")).getAsJsonArray("values").size());
        assertEquals(16, json(TAGS.resolve("glazed_terracotta.json")).getAsJsonArray("values").size());
    }

    @Test
    void thaumcraftPennantsShareVanillaBannerKnowledgeKey() throws IOException {
        JsonObject pennants = json(SCANS.resolve(
                "legacy/object_313_new_itemstack_configblocks.blockwoodendevice_1_8.json"));
        assertEquals("thaumic_reborn:vanilla_banners",
                pennants.get("knowledge_key").getAsString());
        assertEquals("thaumic_reborn:vanilla_banners",
                json(SCANS.resolve("vanilla_banner_family.json"))
                        .get("knowledge_key").getAsString());
        assertTrue(json(Path.of("src/main/resources/data/thaumic_reborn/tags/items/thaumcraft_banners.json"))
                .getAsJsonArray("values").toString().contains("#minecraft:banners"));
    }

    private static void assertScan(String file, String target,
                                   Map<String, Integer> expected) throws IOException {
        JsonObject scan = json(SCANS.resolve(file));
        assertEquals("block_tag", scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        Map<String, Integer> actual = new LinkedHashMap<>();
        JsonArray rewards = scan.getAsJsonArray("aspects");
        rewards.forEach(value -> {
            JsonObject reward = value.getAsJsonObject();
            actual.put(reward.get("id").getAsString(), reward.get("amount").getAsInt());
        });
        assertEquals(expected, actual);
    }

    private static Map<String, Integer> aspects(Object... values) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < values.length; index += 2) {
            result.put((String) values[index], (Integer) values[index + 1]);
        }
        return result;
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
