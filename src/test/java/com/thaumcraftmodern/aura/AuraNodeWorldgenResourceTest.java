package com.thaumcraftmodern.aura;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class AuraNodeWorldgenResourceTest {
    @Test
    void auraNodesUseTheCustomFeatureAndConfiguredDimensionBiomeTag()
            throws IOException {
        JsonObject configured = read(
                "/data/thaumic_reborn/worldgen/configured_feature/aura_node.json"
        );
        assertEquals("thaumic_reborn:aura_node", configured.get("type").getAsString());
        assertEquals(0, configured.getAsJsonObject("config").size());

        JsonObject placed = read(
                "/data/thaumic_reborn/worldgen/placed_feature/aura_node.json"
        );
        assertEquals("thaumic_reborn:aura_node", placed.get("feature").getAsString());
        JsonArray placement = placed.getAsJsonArray("placement");
        assertEquals(2, placement.size());
        assertEquals("minecraft:in_square", placement.get(0)
                .getAsJsonObject().get("type").getAsString());
        assertEquals("minecraft:biome", placement.get(1)
                .getAsJsonObject().get("type").getAsString());

        JsonObject biomeModifier = read(
                "/data/thaumic_reborn/forge/biome_modifier/add_aura_nodes.json"
        );
        assertEquals("forge:add_features", biomeModifier.get("type").getAsString());
        assertEquals(
                "#thaumic_reborn:has_aura_nodes",
                biomeModifier.get("biomes").getAsString()
        );
        assertEquals(
                "thaumic_reborn:aura_node",
                biomeModifier.get("features").getAsString()
        );
        assertEquals(
                "top_layer_modification",
                biomeModifier.get("step").getAsString()
        );

        JsonObject biomeTag = read(
                "/data/thaumic_reborn/tags/worldgen/biome/has_aura_nodes.json"
        );
        assertEquals(false, biomeTag.get("replace").getAsBoolean());
        JsonArray biomes = biomeTag.getAsJsonArray("values");
        assertEquals(2, biomes.size());
        assertEquals("#minecraft:is_overworld", biomes.get(0).getAsString());
        assertEquals("#minecraft:is_nether", biomes.get(1).getAsString());
    }

    @Test
    void nodeUsesPhenomenonScanDefinitionWithRuntimeDerivedAspects()
            throws IOException {
        JsonObject scan = read(
                "/data/thaumic_reborn/thaumcraft/scans/aura_node.json"
        );
        assertEquals("phenomenon", scan.get("type").getAsString());
        assertEquals("thaumic_reborn:aura_node", scan.get("target").getAsString());
        assertEquals(0, scan.getAsJsonArray("aspects").size());
    }

    private static JsonObject read(String path) throws IOException {
        InputStream stream = AuraNodeWorldgenResourceTest.class.getResourceAsStream(path);
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
