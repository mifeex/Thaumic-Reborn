package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

class DefaultWorldPresetResourceTest {
    @Test
    void normalWorldsUseTheThaumcraftBiomeOverlay() throws IOException {
        JsonObject preset = read(
                "/data/minecraft/worldgen/world_preset/normal.json"
        );
        JsonObject biomeSource = preset
                .getAsJsonObject("dimensions")
                .getAsJsonObject("minecraft:overworld")
                .getAsJsonObject("generator")
                .getAsJsonObject("biome_source");

        assertEquals(
                "thaumic_reborn:legacy_overworld",
                biomeSource.get("type").getAsString()
        );
        assertEquals(
                "thaumic_reborn:magical_forest",
                biomeSource.get("magical_forest").getAsString()
        );
        assertEquals(
                "thaumic_reborn:tainted_lands",
                biomeSource.get("tainted_lands").getAsString()
        );
        assertEquals(
                "thaumic_reborn:eerie",
                biomeSource.get("eerie").getAsString()
        );
    }

    private static JsonObject read(String path) throws IOException {
        InputStream stream =
                DefaultWorldPresetResourceTest.class.getResourceAsStream(path);
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
