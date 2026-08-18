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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

class TaintedLandsResourceTest {
    @Test
    void biomeUsesClassicClimateColorsAndSparseDecoration()
            throws IOException {
        JsonObject biome = read(
                "/data/thaumic_reborn/worldgen/biome/tainted_lands.json"
        );
        assertEquals(0.5D, biome.get("temperature").getAsDouble());
        assertEquals(0.5D, biome.get("downfall").getAsDouble());
        JsonObject effects = biome.getAsJsonObject("effects");
        assertEquals(0x6D4189, effects.get("grass_color").getAsInt());
        assertEquals(0x7C6D87, effects.get("foliage_color").getAsInt());
        assertEquals(0x7C44FF, effects.get("sky_color").getAsInt());
        assertEquals(0xCC1188, effects.get("water_color").getAsInt());

        JsonArray vegetation = biome.getAsJsonArray("features")
                .get(9)
                .getAsJsonArray();
        assertFalse(contains(vegetation, "minecraft:patch_dead_bush"));
        assertFalse(contains(vegetation, "minecraft:brown_mushroom_normal"));
        assertFalse(contains(vegetation, "minecraft:red_mushroom_normal"));
    }

    @Test
    void taintedLandSpawnsTheRequestedTaintEcology()
            throws IOException {
        JsonObject modifier = read(
                "/data/thaumic_reborn/forge/biome_modifier/"
                        + "add_tainted_mobs.json"
        );
        JsonArray spawners = modifier.getAsJsonArray("spawners");
        // The original ecology creates most of these through taint blocks and
        // Flux Taint conversion. Until every random-tick conversion source is
        // present, the biome list must keep the generated biome populated.
        assertEquals(12, spawners.size());
        assertTrue(hasSpawn(
                spawners,
                "thaumic_reborn:taintacle",
                10,
                1,
                1
        ));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:taint_spore_swarmer", 15, 1, 1));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:taint_swarm", 20, 1, 2));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:taint_spore", 5, 1, 1));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_crawler", 10, 1, 2));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:thaumic_slime", 15, 1, 2));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_sheep", 12, 2, 4));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_pig", 10, 2, 4));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_chicken", 10, 2, 4));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_cow", 8, 2, 4));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_creeper", 10, 1, 2));
        assertTrue(hasSpawn(spawners,
                "thaumic_reborn:tainted_villager", 2, 1, 1));
    }

    @Test
    void classicCrustedTaintPresentationIsPresent() throws IOException {
        assertNotNull(resource(
                "/assets/thaumic_reborn/textures/block/crusted_taint.png"
        ));
        JsonObject model = read(
                "/assets/thaumic_reborn/models/block/crusted_taint.json"
        );
        assertEquals(
                "thaumic_reborn:block/crusted_taint",
                model.getAsJsonObject("textures").get("all").getAsString()
        );
        JsonObject wasteland = read(
                "/data/forge/tags/worldgen/biome/is_wasteland.json"
        );
        assertTrue(contains(
                wasteland.getAsJsonArray("values"),
                "thaumic_reborn:tainted_lands"
        ));
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/tainted_soil.png",
                "504710d5b517a59f246f03f1889ed52de7f87fa38222a26626b88fc71b9ae251"
        );
        assertEquals(
                "minecraft:block/oak_leaves",
                read("/assets/thaumic_reborn/models/block/"
                        + "tainted_leaves.json")
                        .get("parent")
                        .getAsString()
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/"
                        + "short_tainted_grass.png",
                "3f816bc5a03669a96b10c65666aaf43b83d2ed12f1def1a8e6d11a6f6dd284a6"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/"
                        + "tall_tainted_grass.png",
                "4dcbbc65aa4ada2dc8be52583d9ac733cf07033ba5656a5eb939cbef97137016"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/spore_stalk.png",
                "a9965119bf9ecf91a089506234644bf78e8aa69b4cf3e25cac1b32ef3e7a5c6d"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/"
                        + "mature_spore_stalk.png",
                "bfeebbb15be726521344e22c61f300c11f049b40cd6272d469133df3702286c4"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/flux_goo.png",
                "61ee827d9564d5a315655ffed41e6e98312b45cbab03c97416aba41f3a6ccf47"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/flux_gas.png",
                "08afdd060d230f7f8471edcdb5410fa4bc7ba8874d81b757d5de4965fc67ab0c"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/"
                        + "purifier_leaves.png",
                "25f94bf17e9d403a4cd8a3cb366276f769efc98bdef7c9dd7a8ab2e934ec0986"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/block/"
                        + "purifier_stalk.png",
                "de2ba132ca225209cca3738f816af71fe2774d3f3354fc1d19aaad93632f8091"
        );
        assertResourceHash(
                "/assets/thaumic_reborn/textures/models/"
                        + "crystalcapacitor.png",
                "ff75a55dd1ebc8a2cfa2b4d5a1f20604487d0a75d973a97a971563af896a2ce3"
        );
        assertEquals(
                "thaumic_reborn:block/tainted_cube_all",
                read("/assets/thaumic_reborn/models/block/tainted_soil.json")
                        .get("parent").getAsString()
        );
        for (String plant : new String[] {
                "short_tainted_grass",
                "tall_tainted_grass",
                "spore_stalk",
                "mature_spore_stalk"
        }) {
            assertEquals(
                    "minecraft:block/tinted_cross",
                    read("/assets/thaumic_reborn/models/block/"
                            + plant + ".json")
                            .get("parent").getAsString()
            );
        }
        assertTrue(
                read("/assets/thaumic_reborn/models/block/"
                        + "tainted_cube_all.json")
                        .toString().contains("\"tintindex\":0")
        );
        assertNotNull(resource(
                "/assets/thaumic_reborn/textures/block/taint_over_1.png"
        ));
        assertNotNull(resource(
                "/assets/thaumic_reborn/textures/block/taint_over_2.png"
        ));
        assertNotNull(resource(
                "/assets/thaumic_reborn/textures/block/taint_over_3.png"
        ));

        JsonObject fibres = read(
                "/assets/thaumic_reborn/blockstates/taint_fibres.json"
        );
        String fibresJson = fibres.toString();
        assertTrue(fibresJson.contains("taint_fibres_overlay_blank"));
        assertTrue(fibresJson.contains("taint_fibres_overlay_1"));
        assertTrue(fibresJson.contains("taint_fibres_overlay_2"));
        assertTrue(fibresJson.contains("taint_fibres_overlay_3"));
    }

    private static void assertResourceHash(
            String path,
            String expected
    ) throws IOException {
        try (InputStream stream = resource(path)) {
            assertNotNull(stream, "Missing resource " + path);
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(stream.readAllBytes());
            assertEquals(
                    expected,
                    HexFormat.of().formatHex(digest),
                    "Wrong classic texture at " + path
            );
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
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

    private static InputStream resource(String path) {
        return TaintedLandsResourceTest.class.getResourceAsStream(path);
    }

    private static JsonObject read(String path) throws IOException {
        InputStream stream = resource(path);
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
