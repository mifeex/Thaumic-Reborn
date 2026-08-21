package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElementalEquipmentFidelityTest {
    @Test
    void elementalTierMatchesTc4Material() {
        assertEquals(4, ElementalTier.INSTANCE.getLevel());
        assertEquals(1561, ElementalTier.INSTANCE.getUses());
        assertEquals(10.0F, ElementalTier.INSTANCE.getSpeed());
        assertEquals(4.0F, ElementalTier.INSTANCE.getAttackDamageBonus());
        assertEquals(22, ElementalTier.INSTANCE.getEnchantmentValue());
        assertEquals(8, ElementalPickaxeItem.DOWSING_RADIUS);
        assertEquals(5_000L, ElementalPickaxeItem.DOWSING_MILLIS);
        assertEquals(200, ElementalAxeItem.CHOPPED_DROP_ATTRACTION_TICKS);
        assertEquals(1.3F, ElementalSwordItem.DEFENSIVE_ATTACK_WEAR_MULTIPLIER);
    }

    @Test
    void thaumonomiconExplainsModernElementalToolControls() throws Exception {
        JsonObject russian = json(Path.of(
                "src/main/resources/assets/thaumic_reborn/lang/ru_ru.json"));
        String sword = russian.get("tc.research_page.ELEMENTALSWORD.1").getAsString();
        String pickaxe = russian.get("tc.research_page.ELEMENTALPICK.1").getAsString();
        String axe = russian.get("tc.research_page.ELEMENTALAXE.2").getAsString();

        assertTrue(sword.contains("Shift + ПКМ"));
        assertTrue(sword.contains("удерживать клавиши не требуется"));
        assertTrue(sword.contains("повторите комбинацию или смените предмет в основной руке"));
        assertTrue(sword.contains("прочность на 30% быстрее"));
        assertTrue(sword.contains("атаки блокируются"));
        assertTrue(sword.contains("подъём в защитной стойке не действуют"));
        assertTrue(pickaxe.contains("оранжевыми частицами"));
        assertTrue(pickaxe.contains("сквозь камень"));
        assertTrue(axe.contains("пока Вы не подберёте"));
    }

    @Test
    void allSixExecutableInfusionsUseOriginalInstabilityAndResearch() throws Exception {
        for (String id : List.of("elemental_pick", "elemental_axe", "elemental_sword",
                "elemental_shovel", "elemental_hoe", "boots_traveller")) {
            JsonObject recipe = json(Path.of("src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes",
                    id + ".json"));
            assertEquals(1, recipe.get("instability").getAsInt(), id);
            assertFalse(recipe.getAsJsonArray("components").isEmpty(), id);
            assertTrue(recipe.getAsJsonObject("essentia").size() > 0, id);
            assertTrue(recipe.getAsJsonObject("result").get("item").getAsString()
                    .startsWith("thaumic_reborn:"), id);
        }
    }

    @Test
    void matchingResearchPagesAreActiveInfusionPages() throws Exception {
        for (String id : List.of("elementalpick", "elementalaxe", "elementalsword",
                "elementalshovel", "elementalhoe", "bootstraveller")) {
            JsonObject research = json(Path.of(
                    "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy", id + ".json"));
            assertFalse(research.get("inactive").getAsBoolean(), id);
            assertTrue(research.getAsJsonArray("pages").asList().stream()
                    .anyMatch(page -> page.getAsJsonObject().get("type").getAsString().equals("infusion")), id);
            assertTrue(research.get("icon").getAsString().startsWith("thaumic_reborn:"), id);
        }
    }

    @Test
    void travellerBootsUseOnlyTheFourRawFishVariants() throws Exception {
        JsonObject recipe = json(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes/boots_traveller.json"));
        JsonObject research = json(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/bootstraveller.json"));

        assertEquals("thaumic_reborn:raw_fishes", recipe.getAsJsonArray("components")
                .get(5).getAsJsonObject().get("tag").getAsString());
        JsonObject display = research.getAsJsonArray("pages").get(1).getAsJsonObject();
        assertEquals("thaumic_reborn:raw_fishes", display.getAsJsonArray("components")
                .get(5).getAsJsonObject().get("tag").getAsString());

        JsonObject tag = json(Path.of(
                "src/main/resources/data/thaumic_reborn/tags/items/raw_fishes.json"));
        assertEquals(List.of(
                "minecraft:cod",
                "minecraft:salmon",
                "minecraft:pufferfish",
                "minecraft:tropical_fish"
        ), tag.getAsJsonArray("values").asList().stream()
                .map(value -> value.getAsString())
                .toList());
    }

    @Test
    void packagedIconsAreByteExactOriginalAssets() throws Exception {
        for (String texture : List.of("elementalpick.png", "elementalaxe.png", "elementalsword.png",
                "elementalshovel.png", "elementalhoe.png", "bootstraveler.png")) {
            byte[] original = Files.readAllBytes(Path.of(
                    "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures/items",
                    texture));
            try (InputStream packaged = getClass().getResourceAsStream(
                    "/assets/thaumic_reborn/textures/item/" + texture)) {
                assertNotNull(packaged, texture);
                assertEquals(sha256(original), sha256(packaged.readAllBytes()), texture);
            }
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static String sha256(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
