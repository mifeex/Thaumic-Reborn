package com.thaumcraftmodern.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class RequestedClassicResearchIconFidelityTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"
    );
    private static final Path ORIGINAL_TEXTURES = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                    + "assets/thaumcraft/textures"
    );
    private static final Path MODERN_TEXTURES = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures"
    );

    @Test
    void textureBackedResearchUsesOriginalResourceIcons() throws Exception {
        Map<String, String> icons = Map.of(
                "vampbat", "foci/vampirebats.png",
                "alchemicalduplication", "misc/r_alchmult.png",
                "alchemicalmanufacture", "misc/r_alchman.png",
                "entropicprocessing", "misc/r_alchent.png"
        );
        for (Map.Entry<String, String> entry : icons.entrySet()) {
            JsonObject research = read(entry.getKey());
            assertFalse(research.has("icon"), entry.getKey());
            assertEquals(
                    "thaumic_reborn:textures/" + entry.getValue(),
                    research.get("icon_resource").getAsString(),
                    entry.getKey()
            );
            assertArrayEquals(
                    Files.readAllBytes(ORIGINAL_TEXTURES.resolve(entry.getValue())),
                    Files.readAllBytes(MODERN_TEXTURES.resolve(entry.getValue())),
                    entry.getKey()
            );
        }
    }

    @Test
    void researchTableUsesTheRequestedScribingToolsIcon()
            throws Exception {
        JsonObject research = read("restable");
        assertEquals("thaumic_reborn:scribing_tools",
                research.get("icon").getAsString());
        assertEquals("new ItemStack(ConfigBlocks.blockTable, 1, 1)",
                research.getAsJsonObject("legacy")
                        .get("icon_raw").getAsString());
    }

    private static JsonObject read(String id) throws Exception {
        return JsonParser.parseString(Files.readString(
                RESEARCH.resolve(id + ".json")
        )).getAsJsonObject();
    }
}
