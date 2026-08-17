package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class ThaumiumEquipmentFidelityTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void materialValuesMatchTc4() {
        assertEquals(3, ThaumiumTier.INSTANCE.getLevel());
        assertEquals(500, ThaumiumTier.INSTANCE.getUses());
        assertEquals(7.0F, ThaumiumTier.INSTANCE.getSpeed());
        assertEquals(2.5F, ThaumiumTier.INSTANCE.getAttackDamageBonus());
        assertEquals(18, ThaumiumTier.INSTANCE.getEnchantmentValue());
        assertEquals(25, ThaumiumArmorMaterial.INSTANCE.getEnchantmentValue());
        assertEquals(2, ThaumiumArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.HELMET));
        assertEquals(6, ThaumiumArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.CHESTPLATE));
        assertEquals(5, ThaumiumArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.LEGGINGS));
        assertEquals(2, ThaumiumArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.BOOTS));
    }

    @Test
    void allClassicRecipesAndResearchPagesAreReachable() throws Exception {
        List<String> equipment = List.of("axe", "sword", "pickaxe", "shovel",
                "hoe", "helmet", "chestplate", "leggings", "boots");
        JsonObject research = json(ROOT.resolve(
                "data/thaumic_reborn/thaumcraft/research/legacy/thaumium.json"));
        assertFalse(research.get("inactive").getAsBoolean());
        String pages = research.getAsJsonArray("pages").toString();
        for (String name : equipment) {
            Path recipe = ROOT.resolve("data/thaumic_reborn/recipes/thaumium_"
                    + name + ".json");
            assertEquals("thaumic_reborn:thaumium_" + name,
                    json(recipe).getAsJsonObject("result").get("item").getAsString());
            assertEquals(true, pages.contains("thaumic_reborn:thaumium_" + name));
        }
    }

    @Test
    void originalTexturesAreCopiedByteForByte() throws Exception {
        assertHash("textures/item/thaumium_sword.png",
                "f302f41782bd41b3e76e09f066ee42d88cf6950405e161826bdfca2a569e5991");
        assertHash("textures/item/thaumium_pickaxe.png",
                "2242bd52a462719c3c6e7335c5653a797c68e32b5f901fa193c2b40f016c58f1");
        assertHash("textures/models/armor/thaumium_layer_1.png",
                "d1ed81f59eb326ae14ce016c4284f8a343349282caec54141485e7131c826d51");
        assertHash("textures/models/armor/thaumium_layer_2.png",
                "a4a83435d0c8e03e90abdaa3e8637a4ffd5d3bac1ebf284c116eec77c9e985e9");
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static void assertHash(String relative, String expected) throws Exception {
        byte[] data = Files.readAllBytes(ROOT.resolve(
                "assets/thaumic_reborn").resolve(relative));
        assertEquals(expected, HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(data)));
    }
}
