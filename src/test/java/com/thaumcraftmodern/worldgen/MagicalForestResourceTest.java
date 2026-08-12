package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class MagicalForestResourceTest {
    @Test
    void biomeDoesNotInstallTheVanillaBirchAndSmallOakFeature()
            throws IOException {
        JsonObject biome = read(
                "/data/thaumcraftmodern/worldgen/biome/magical_forest.json"
        );
        assertEquals(0.6D, biome.get("temperature").getAsDouble());
        assertEquals(0.7D, biome.get("downfall").getAsDouble());

        JsonArray vegetation = biome.getAsJsonArray("features")
                .get(9)
                .getAsJsonArray();
        assertFalse(contains(vegetation, "minecraft:trees_birch_and_oak"));
        assertFalse(contains(vegetation, "minecraft:forest_flowers"));
        assertFalse(contains(vegetation, "minecraft:flower_default"));
        assertFalse(contains(vegetation, "minecraft:patch_grass_forest"));
        assertFalse(contains(vegetation, "minecraft:brown_mushroom_normal"));
        assertFalse(contains(vegetation, "minecraft:red_mushroom_normal"));
        assertFalse(contains(vegetation, "minecraft:patch_sugar_cane"));
    }

    @Test
    void magicalForestKeepsClassicPechAndWispSpawns()
            throws IOException {
        JsonObject modifier = read(
                "/data/thaumcraftmodern/forge/biome_modifier/"
                        + "add_magical_forest_mobs.json"
        );
        assertEquals(
                "thaumcraftmodern:magical_forest",
                modifier.get("biomes").getAsString()
        );
        JsonArray spawners = modifier.getAsJsonArray("spawners");
        assertEquals(2, spawners.size());
        assertTrue(hasSpawn(spawners, "thaumcraftmodern:pech", 10, 1, 2));
        assertTrue(hasSpawn(spawners, "thaumcraftmodern:wisp", 10, 1, 2));

        JsonObject biome = read(
                "/data/thaumcraftmodern/worldgen/biome/magical_forest.json"
        );
        JsonObject biomeSpawners = biome.getAsJsonObject("spawners");
        JsonArray creatures = biomeSpawners.getAsJsonArray("creature");
        JsonArray monsters = biomeSpawners.getAsJsonArray("monster");
        assertTrue(hasSpawn(creatures, "minecraft:wolf", 2, 1, 3));
        assertTrue(hasSpawn(creatures, "minecraft:horse", 2, 1, 3));
        assertTrue(hasSpawn(monsters, "minecraft:witch", 3, 1, 1));
        assertTrue(hasSpawn(monsters, "minecraft:enderman", 10, 1, 1));
    }

    private static boolean contains(JsonArray values, String expected) {
        for (int index = 0; index < values.size(); index++) {
            if (expected.equals(values.get(index).getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasSpawn(
            JsonArray values,
            String type,
            int weight,
            int minimum,
            int maximum
    ) {
        for (int index = 0; index < values.size(); index++) {
            JsonObject spawn = values.get(index).getAsJsonObject();
            if (type.equals(spawn.get("type").getAsString())
                    && weight == spawn.get("weight").getAsInt()
                    && minimum == spawn.get("minCount").getAsInt()
                    && maximum == spawn.get("maxCount").getAsInt()) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject read(String path) throws IOException {
        InputStream stream =
                MagicalForestResourceTest.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing test resource " + path);
        try (stream;
             InputStreamReader reader = new InputStreamReader(
                     stream,
                     StandardCharsets.UTF_8
             )) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
