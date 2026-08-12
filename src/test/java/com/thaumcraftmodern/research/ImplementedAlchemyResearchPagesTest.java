package com.thaumcraftmodern.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImplementedAlchemyResearchPagesTest {
    private static final Path ROOT = Path.of(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy"
    );

    @Test
    void implementedJarAndThaumatoriumPagesDoNotRenderAsInactive() throws Exception {
        JsonObject jarLabel = read("jarlabel");
        assertTrue(jarLabel.get("auto_unlock").getAsBoolean(),
                "TC4 JARLABEL is a stub and must complete without research notes");
        JsonObject thaumatorium = read("thaumatorium");

        assertFalse(jarLabel.get("inactive").getAsBoolean());
        assertFalse(thaumatorium.get("inactive").getAsBoolean());
        assertTrue(hasPage(jarLabel.getAsJsonArray("pages"), "recipe",
                "thaumcraftmodern:jar_label"));
        assertTrue(hasPage(thaumatorium.getAsJsonArray("pages"),
                "compound_crafting",
                "thaumcraftmodern:thaumatorium_construct"));
        assertFalse(hasUnavailablePage(jarLabel.getAsJsonArray("pages")));
        assertFalse(hasUnavailablePage(thaumatorium.getAsJsonArray("pages")));
    }

    @Test
    void implementedEssentiaProgressionIsActiveAndHasRuntimePages() throws Exception {
        for (String id : new String[]{
                "distilessentia",
                "phial",
                "jarlabel",
                "tubes",
                "tubefilter",
                "jarvoid",
                "centrifuge",
                "essentiacrystal",
                "thaumatorium"
        }) {
            JsonObject research = read(id);
            assertFalse(research.get("inactive").getAsBoolean(), id);
            assertFalse(hasUnavailablePage(research.getAsJsonArray("pages")), id);
        }
    }

    private static JsonObject read(String id) throws Exception {
        return JsonParser.parseString(Files.readString(ROOT.resolve(id + ".json")))
                .getAsJsonObject();
    }

    private static boolean hasPage(JsonArray pages, String type, String recipe) {
        for (var element : pages) {
            JsonObject page = element.getAsJsonObject();
            if (type.equals(page.get("type").getAsString())
                    && page.has("recipe")
                    && recipe.equals(page.get("recipe").getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasUnavailablePage(JsonArray pages) {
        for (var element : pages) {
            if ("unavailable".equals(element.getAsJsonObject()
                    .get("type").getAsString())) {
                return true;
            }
        }
        return false;
    }
}
