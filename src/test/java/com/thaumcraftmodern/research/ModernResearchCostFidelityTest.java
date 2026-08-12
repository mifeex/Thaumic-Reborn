package com.thaumcraftmodern.research;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernResearchCostFidelityTest {
    @Test
    void reversibleTubeCanCreateResearchNotesWithoutLegacyMetadata()
            throws Exception {
        String loader = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/data/ResearchReloadListener.java"
        ));
        JsonObject research = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/thaumcraft/research/"
                        + "reversible_essentia_tube.json"
        ))).getAsJsonObject();

        assertTrue(loader.contains("json.has(\"research_cost\")"));
        JsonArray cost = research.getAsJsonArray("research_cost");
        assertEquals(4, cost.size());
        assertEquals("motus", cost.get(0).getAsJsonObject()
                .get("id").getAsString());
    }
}
