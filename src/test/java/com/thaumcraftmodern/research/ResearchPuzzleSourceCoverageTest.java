package com.thaumcraftmodern.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResearchPuzzleSourceCoverageTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumcraftmodern/thaumcraft/research/legacy"
    );

    @Test
    void directAspectPurchaseExactlyMatchesOriginalSecondaryResearchFlag() throws IOException {
        try (var stream = Files.list(RESEARCH)) {
            for (Path file : stream.filter(path -> path.toString().endsWith(".json")).toList()) {
                JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                JsonObject flags = json.getAsJsonObject("legacy").getAsJsonObject("flags");
                boolean originalSecondary = flags.get("secondary").getAsBoolean();
                boolean directPurchase = json.has("purchase_cost")
                        && !json.getAsJsonArray("purchase_cost").isEmpty();
                assertEquals(originalSecondary, directPurchase, file.toString());
            }
        }
    }

    @Test
    void everyResearchableLegacyEntryHasItsOwnOriginalAspectRecipe() throws IOException {
        List<Path> files;
        try (var stream = Files.list(RESEARCH)) {
            files = stream.filter(path -> path.toString().endsWith(".json")).toList();
        }
        Set<String> researchable = new LinkedHashSet<>();
        for (Path file : files) {
            JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            JsonObject legacy = json.getAsJsonObject("legacy");
            assertTrue(legacy.has("complexity"), file.toString());
            assertTrue(legacy.has("research_aspects"), file.toString());

            boolean manual = !json.get("inactive").getAsBoolean()
                    && !json.get("auto_unlock").getAsBoolean()
                    && (!json.has("purchase_cost")
                            || json.getAsJsonArray("purchase_cost").isEmpty())
                    && !legacy.getAsJsonArray("research_aspects").isEmpty();
            if (!manual) continue;
            researchable.add(json.get("id").getAsString());
            assertFalse(legacy.getAsJsonArray("research_aspects").isEmpty(), file.toString());
        }
        assertTrue(researchable.containsAll(Set.of(
                "alumentum",
                "distilessentia",
                "focalmanipulation",
                "nodejar",
                "thaumatorium",
                "wardedarcana"
        )), researchable.toString());
    }

    @Test
    void parentGatedGolemancyStubsCompleteWithoutResearchNotes() throws IOException {
        for (String id : List.of("golembell", "coregather")) {
            JsonObject json = JsonParser.parseString(Files.readString(
                    RESEARCH.resolve(id + ".json")
            )).getAsJsonObject();
            JsonObject flags = json.getAsJsonObject("legacy")
                    .getAsJsonObject("flags");

            assertTrue(flags.get("stub").getAsBoolean(), id);
            assertTrue(json.get("auto_unlock").getAsBoolean(),
                    id + " must auto-complete after golemstraw");
            assertEquals(List.of("golemstraw"),
                    json.getAsJsonArray("parents").asList().stream()
                            .map(element -> element.getAsString())
                            .toList(), id);
        }
    }
}
