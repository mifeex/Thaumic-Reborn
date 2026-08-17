package com.thaumcraftmodern.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ReadyFocusResearchCoverageTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"
    );

    @Test
    void everyImplementedFocusHasAnActiveResearchNode() throws Exception {
        for (String id : List.of(
                "focusfire", "focusfrost", "focusshock", "focustrade",
                "focusexcavation", "focusprimal", "focushellbat",
                "focusportablehole", "focuswarding"
        )) {
            JsonObject research = json(RESEARCH.resolve(id + ".json"));
            assertFalse(research.get("inactive").getAsBoolean(), id);
            assertTrue(research.has("pages"), id);
        }
    }

    @Test
    void implementedInfusionFocusPagesMatchTheirExecutableRecipes()
            throws Exception {
        Map<String, ExpectedInfusion> expected = Map.of(
                "focushellbat", new ExpectedInfusion(
                        "thaumic_reborn:focus_hellbat",
                        "minecraft:magma_cream", 6, "minor"),
                "focusportablehole", new ExpectedInfusion(
                        "thaumic_reborn:focus_portable_hole",
                        "minecraft:ender_pearl", 6, "minor"),
                "focuswarding", new ExpectedInfusion(
                        "thaumic_reborn:focus_warding",
                        "minecraft:nether_star", 8, "moderate")
        );
        for (var entry : expected.entrySet()) {
            JsonObject research = json(RESEARCH.resolve(entry.getKey() + ".json"));
            JsonObject page = research.getAsJsonArray("pages").asList().stream()
                    .map(element -> element.getAsJsonObject())
                    .filter(candidate -> "infusion".equals(
                            candidate.get("type").getAsString()))
                    .findFirst().orElseThrow();
            ExpectedInfusion infusion = entry.getValue();
            assertEquals(infusion.output(), page.get("output").getAsString());
            assertEquals(infusion.central(), page.get("central").getAsString());
            assertEquals(infusion.components(),
                    page.getAsJsonArray("components").size());
            assertTrue(page.getAsJsonArray("aspect_costs").size() >= 4);
            assertEquals(infusion.instability(),
                    page.get("instability").getAsString());
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private record ExpectedInfusion(
            String output,
            String central,
            int components,
            String instability
    ) {
    }
}
