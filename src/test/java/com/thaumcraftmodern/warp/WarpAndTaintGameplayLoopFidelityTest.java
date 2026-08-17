package com.thaumcraftmodern.warp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WarpAndTaintGameplayLoopFidelityTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"
    );
    private static final Path SOURCES = Path.of(
            "src/main/java/com/thaumcraftmodern"
    );

    @Test
    void everyTc4WarpResearchRegistrationIsMaterialized() throws Exception {
        Map<String, Integer> expected = Map.ofEntries(
                Map.entry("focushellbat", 2),
                Map.entry("rod_bone", 1),
                Map.entry("rod_bone_staff", 1),
                Map.entry("sinstone", 2),
                Map.entry("infernalfurnace", 2),
                Map.entry("jarbrain", 3),
                Map.entry("maskangryghost", 1),
                Map.entry("masksippingfiend", 1),
                Map.entry("liquiddeath", 3),
                Map.entry("bottletaint", 2),
                Map.entry("golemflesh", 3),
                Map.entry("corebutcher", 1),
                Map.entry("advancedgolem", 5),
                Map.entry("researcher2", 1),
                Map.entry("crimson", 3),
                Map.entry("oculus", 6),
                Map.entry("primnode", 1),
                Map.entry("cap_void", 1),
                Map.entry("focusprimal", 2),
                Map.entry("rod_primal_staff", 3)
        );
        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            JsonObject research = JsonParser.parseString(Files.readString(
                    RESEARCH.resolve(entry.getKey() + ".json")
            )).getAsJsonObject();
            assertEquals(
                    entry.getValue().intValue(),
                    research.get("completion_warp").getAsInt(),
                    entry.getKey()
            );
        }
    }

    @Test
    void purchasedForbiddenResearchSendsTheSameWarpFeedbackAsDiscoveries()
            throws Exception {
        String purchase = source("research/ResearchPurchaseService.java");
        assertTrue(purchase.contains("sendCompletionWarpFeedback"));
        assertTrue(purchase.contains("WarpFeedbackPacket.PERMANENT"));
        assertTrue(purchase.contains("WarpFeedbackPacket.NORMAL"));
    }

    @Test
    void taintEcologyCompletesOriginalCreatureFluxAndCleanupRoutes()
            throws Exception {
        String crust = source("world/block/SpreadingTaintBlock.java");
        assertTrue(crust.contains("random.nextInt(200)"));
        assertTrue(crust.contains("TAINT_SPORE_SWARMER"));
        assertTrue(crust.contains("convertSurroundedTaintToFlux"));
        assertTrue(crust.contains("ModSounds.ROOTS"));

        String plants = source("world/block/TaintedPlantBlock.java");
        assertTrue(plants.contains("SPORE_STALK"));
        assertTrue(plants.contains("MATURE_SPORE_STALK"));
        assertTrue(plants.contains("TAINT_SPORE"));

        String leaves = source("world/block/SpreadingTaintedLeavesBlock.java");
        assertTrue(leaves.contains("TaintEcology.randomTick"));

        String biomes = source("world/block/BiomeColumnService.java");
        assertTrue(biomes.contains("level.isLoaded(position)"));
        assertTrue(biomes.contains("resendBiomesForChunks"));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(SOURCES.resolve(relative));
    }
}
