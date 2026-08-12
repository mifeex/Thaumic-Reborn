package com.thaumcraftmodern.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ThaumonomiconClassicIconFidelityTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy"
    );
    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/thaumcraftmodern/textures/misc"
    );

    @Test
    void implementedResourceIconResearchUsesOriginalTc4Textures()
            throws IOException {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("aspects", "r_aspects.png");
        expected.put("eldritchminor", "r_eldritchminor.png");
        expected.put("eldritchmajor", "r_eldritchmajor.png");
        expected.put("researcher1", "r_researcher1.png");
        expected.put("researcher2", "r_researcher2.png");
        expected.put("nodetapper2", "r_nodetap2.png");
        expected.put("warp", "r_warp.png");

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            JsonObject research = JsonParser.parseString(Files.readString(
                    RESEARCH.resolve(entry.getKey() + ".json")
            )).getAsJsonObject();
            assertFalse(research.get("inactive").getAsBoolean(), entry.getKey());
            assertFalse(research.has("icon"), entry.getKey());
            assertEquals(
                    "thaumcraftmodern:textures/misc/" + entry.getValue(),
                    research.get("icon_resource").getAsString(),
                    entry.getKey()
            );
            assertTrue(
                    Files.isRegularFile(TEXTURES.resolve(entry.getValue())),
                    entry.getValue()
            );
        }
        assertArrayEquals(
                Files.readAllBytes(Path.of(
                        "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                                + "assets/thaumcraft/textures/misc/r_eldritchminor.png"
                )),
                Files.readAllBytes(TEXTURES.resolve("r_eldritchminor.png"))
        );
    }

    @Test
    void visibleLockedResearchKeepsItsClassicIconInsteadOfQuestionMark()
            throws IOException {
        String screen = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/"
                        + "ThaumonomiconScreen.java"
        ));
        assertTrue(screen.contains(
                "renderResearchIcon(graphics, research, x, y, iconTint);"
        ));
        assertTrue(screen.contains("drawTintedItemCentered("));
        assertFalse(screen.contains("\"?\""));
    }

    @Test
    void classicCategoryTabsUseOriginalTc4ResourceIcons() throws IOException {
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("basics", "textures/items/thaumonomiconcheat.png");
        expected.put("legacy/thaumaturgy", "textures/misc/r_thaumaturgy.png");
        expected.put("legacy/alchemy", "textures/misc/r_crucible.png");
        expected.put("legacy/artifice", "textures/misc/r_artifice.png");
        expected.put("legacy/golemancy", "textures/misc/r_golemancy.png");
        expected.put("legacy/eldritch", "textures/misc/r_eldritch.png");

        Path categoryRoot = Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/categories"
        );
        Path assetRoot = Path.of(
                "src/main/resources/assets/thaumcraftmodern"
        );
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            JsonObject category = JsonParser.parseString(Files.readString(
                    categoryRoot.resolve(entry.getKey() + ".json")
            )).getAsJsonObject();
            assertFalse(category.has("icon"), entry.getKey());
            assertEquals(
                    "thaumcraftmodern:" + entry.getValue(),
                    category.get("icon_resource").getAsString(),
                    entry.getKey()
            );
            assertTrue(
                    Files.isRegularFile(assetRoot.resolve(entry.getValue())),
                    entry.getValue()
            );
        }
        JsonObject eldritch = JsonParser.parseString(Files.readString(
                categoryRoot.resolve("legacy/eldritch.json")
        )).getAsJsonObject();
        assertEquals(
                "thaumcraftmodern:textures/gui/gui_researchbackeldritch.png",
                eldritch.get("background").getAsString()
        );
    }
}
