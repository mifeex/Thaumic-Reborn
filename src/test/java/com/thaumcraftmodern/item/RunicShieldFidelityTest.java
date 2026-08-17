package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunicShieldFidelityTest {
    @Test
    void classicAccessoryChargesAndHardeningArePreserved() {
        assertEquals(1, RunicShieldService.combinedCharge(1, 0));
        assertEquals(5, RunicShieldService.combinedCharge(5, 0));
        assertEquals(4, RunicShieldService.combinedCharge(4, 0));
        assertEquals(8, RunicShieldService.combinedCharge(8, 0));
        assertEquals(7, RunicShieldService.combinedCharge(7, 0));
        assertEquals(10, RunicShieldService.combinedCharge(10, 0));
        assertEquals(9, RunicShieldService.combinedCharge(9, 0));
        assertEquals(6, RunicShieldService.combinedCharge(5, 1));
        assertEquals(40, RunicShieldService.RECHARGE_TICKS);
        assertEquals(80, RunicShieldService.RECHARGE_DELAY_TICKS);
        assertEquals(50, RunicShieldService.RECHARGE_COST_CENTIVIS);
    }

    @Test
    void persistedPlayerChargeSurvivesRoundTrip() {
        PlayerThaumKnowledge knowledge = new PlayerThaumKnowledge();
        knowledge.setRunicCharge(7);
        assertEquals(7, PlayerThaumKnowledge.deserialize(
                knowledge.serialize()).runicCharge());
    }

    @Test
    void completeResearchBranchIsActiveAndExecutable() throws Exception {
        for (String id : new String[]{"runicarmor", "runicaugmentation",
                "runiccharged", "runichealing", "runicemergency", "runickinetic"}) {
            JsonObject research = json(Path.of("src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/"
                    + id + ".json"));
            assertFalse(research.get("inactive").getAsBoolean(), id);
            assertTrue(research.getAsJsonArray("pages").asList().stream()
                    .anyMatch(page -> "infusion".equals(page.getAsJsonObject()
                            .get("type").getAsString())), id);
        }
        JsonObject augmentation = json(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes/runic_augmentation.json"));
        assertEquals("runic_augment", augmentation.getAsJsonObject(
                "result_modifier").get("type").getAsString());

        assertPotion("runic_ring_charged", "minecraft:strong_swiftness");
        assertPotion("runic_ring_regen", "minecraft:long_regeneration");
        assertPotion("runic_amulet_emergency", "minecraft:strong_strength");
        assertPotion("runic_girdle_kinetic", "minecraft:strong_harming");
    }

    @Test
    void augmentationCoversEveryModernCurioAndClassicRunicArmorFamily()
            throws Exception {
        JsonObject augmentable = json(Path.of(
                "src/main/resources/data/thaumic_reborn/tags/items/runic_augmentable.json"));
        Set<String> values = new HashSet<>();
        augmentable.getAsJsonArray("values").forEach(value ->
                values.add(value.getAsString()));
        for (String slot : new String[]{"ring", "necklace", "belt"}) {
            JsonObject curios = json(Path.of(
                    "src/main/resources/data/curios/tags/items/" + slot + ".json"));
            curios.getAsJsonArray("values").forEach(value -> assertTrue(
                    values.contains(value.getAsString()), value.getAsString()));
        }
        for (String item : new String[]{
                "fortress_helmet_mask_grinning_devil",
                "fortress_helmet_mask_angry_ghost",
                "fortress_helmet_mask_sipping_fiend",
                "cultist_knight_helmet", "cultist_knight_chestplate",
                "cultist_knight_leggings", "cultist_cleric_hood",
                "cultist_cleric_robe", "cultist_cleric_leggings",
                "cultist_praetor_helmet", "cultist_praetor_chestplate",
                "cultist_praetor_leggings", "cultist_boots"}) {
            assertTrue(values.contains("thaumic_reborn:" + item), item);
        }
    }

    @Test
    void runicResearchAndAccessoryVisualsAreExactTc4Assets() throws Exception {
        assertSameAsset("textures/misc/r_runicupg.png",
                "textures/misc/r_runicupg.png");
        for (String item : new String[]{
                "runic_ring", "runic_ring_charged", "runic_ring_regen",
                "runic_ring_lesser", "runic_amulet",
                "runic_amulet_emergency", "runic_girdle",
                "runic_girdle_kinetic"}) {
            assertSameAsset("textures/items/" + item + ".png",
                    "textures/item/" + item + ".png");
        }
    }

    private static void assertSameAsset(String original, String modern)
            throws Exception {
        Path originalPath = Path.of("reference/Thaumcraft-4.2-FOREVA-master/"
                + "src/main/resources/assets/thaumcraft/" + original);
        Path modernPath = Path.of("src/main/resources/assets/thaumic_reborn/"
                + modern);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream packaged = RunicShieldFidelityTest.class
                .getResourceAsStream("/assets/thaumic_reborn/" + modern)) {
            assertNotNull(packaged, modernPath.toString());
            assertEquals(java.util.HexFormat.of().formatHex(
                            digest.digest(Files.readAllBytes(originalPath))),
                    java.util.HexFormat.of().formatHex(
                            digest.digest(packaged.readAllBytes())), modern);
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void assertPotion(String recipeId, String potion)
            throws Exception {
        JsonObject recipe = json(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes/"
                        + recipeId + ".json"));
        JsonObject ingredient = recipe.getAsJsonArray("components").asList()
                .stream().map(value -> value.getAsJsonObject())
                .filter(value -> value.has("nbt"))
                .findFirst().orElseThrow();
        assertEquals("forge:nbt", ingredient.get("type").getAsString());
        assertEquals(potion, ingredient.getAsJsonObject("nbt")
                .get("Potion").getAsString());
    }
}
