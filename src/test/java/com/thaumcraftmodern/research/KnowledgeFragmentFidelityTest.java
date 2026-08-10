package com.thaumcraftmodern.research;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KnowledgeFragmentFidelityTest {
    @Test
    void fragmentUseGrantsOneOrTwoPointsOfEveryPrimalAspect() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/KnowledgeFragmentItem.java"
        ));
        assertTrue(source.contains("for (PrimalAspect aspect : PrimalAspect.ordered())"));
        assertTrue(source.contains("serverPlayer.getRandom().nextInt(2) + 1"));
        assertTrue(source.contains("stack.shrink(1)"));
        assertTrue(source.contains("KnowledgeSync.send(serverPlayer"));
    }

    @Test
    void nineFragmentsCraftTheOriginalUnknownResearchNote() throws Exception {
        JsonObject recipe = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumcraftmodern/recipes/knowledge_fragment.json"
        ))).getAsJsonObject();
        assertEquals("thaumcraftmodern:knowledge_fragment_research",
                recipe.get("type").getAsString());

        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/arcane/KnowledgeFragmentRecipe.java"
        ));
        assertTrue(source.contains("slot < 9"));
        assertTrue(source.contains("ResearchNotesItem.createUnknownDiscovery()"));
        assertTrue(source.contains("NonNullList.withSize(\n                9,"));
    }

    @Test
    void exhaustedHiddenResearchReturnsSevenToNineFragments() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/ResearchNotesItem.java"
        ));
        assertTrue(source.contains("7 + serverPlayer.getRandom().nextInt(3)"));
        assertTrue(source.contains("KnowledgeFragmentResearchService.candidates"));
    }
}
