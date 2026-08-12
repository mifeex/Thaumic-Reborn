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

import static org.junit.jupiter.api.Assertions.*;

final class VoidResearchVerticalFidelityTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void voidMaterialsMatchTc4Constants() {
        assertEquals(4, VoidTier.INSTANCE.getLevel());
        assertEquals(600, VoidTier.INSTANCE.getUses());
        assertEquals(8.0F, VoidTier.INSTANCE.getSpeed());
        assertEquals(3.0F, VoidTier.INSTANCE.getAttackDamageBonus());
        assertEquals(20, VoidTier.INSTANCE.getEnchantmentValue());
        assertEquals(3, VoidArmorMaterial.INSTANCE.getDefenseForType(ArmorItem.Type.HELMET));
        assertEquals(7, VoidArmorMaterial.INSTANCE.getDefenseForType(ArmorItem.Type.CHESTPLATE));
        assertEquals(6, VoidArmorMaterial.INSTANCE.getDefenseForType(ArmorItem.Type.LEGGINGS));
        assertEquals(4, VoidRobeArmorMaterial.INSTANCE.getDefenseForType(ArmorItem.Type.HELMET));
        assertEquals(8, VoidRobeArmorMaterial.INSTANCE.getDefenseForType(ArmorItem.Type.CHESTPLATE));
        assertEquals(7, VoidRobeArmorMaterial.INSTANCE.getDefenseForType(ArmorItem.Type.LEGGINGS));
    }

    @Test
    void voidGearContributesClassicWornWarp() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/warp/WarpGearService.java"));
        assertTrue(source.contains("VOID_ROBE_HOOD.get()"));
        assertTrue(source.contains("VOID_ROBE_CHESTPLATE.get()"));
        assertTrue(source.contains("VOID_ROBE_LEGGINGS.get()"));
        assertTrue(source.contains("return 2;"));
        assertTrue(source.contains("VOID_HELMET.get()"));
        assertTrue(source.contains("VOID_SWORD.get()"));
        assertTrue(source.contains("return 1;"));
    }

    @Test
    void researchProgressionUsesOriginalWarpKeysAndParents() throws Exception {
        JsonObject minor = research("eldritchminor");
        assertFalse(minor.get("inactive").getAsBoolean());
        assertEquals("warp", minor.getAsJsonObject("reveal_when").get("type").getAsString());
        assertEquals(26, minor.getAsJsonObject("reveal_when").get("minimum").getAsInt());
        JsonObject metal = research("voidmetal");
        assertFalse(metal.get("inactive").getAsBoolean());
        assertEquals(List.of("thaumium", "eldritchminor"), strings(metal, "parents"));
        JsonObject robes = research("armorvoidfortress");
        assertFalse(robes.get("inactive").getAsBoolean());
        assertEquals(List.of("voidmetal", "enchfabric", "eldritchmajor"), strings(robes, "parents"));
        assertEquals(3, robes.getAsJsonArray("pages").asList().stream()
                .filter(page -> page.getAsJsonObject().get("type").getAsString().equals("infusion"))
                .count());
    }

    @Test
    void allVoidEquipmentRecipesAreExecutable() throws Exception {
        for (String id : List.of("void_sword", "void_pickaxe", "void_axe", "void_shovel",
                "void_hoe", "void_helmet", "void_chestplate", "void_leggings", "void_boots")) {
            JsonObject recipe = json(ROOT.resolve("data/thaumcraftmodern/recipes/" + id + ".json"));
            assertEquals("thaumcraftmodern:" + id,
                    recipe.getAsJsonObject("result").get("item").getAsString(), id);
        }
        for (String id : List.of("void_robe_hood", "void_robe_chestplate", "void_robe_leggings")) {
            JsonObject recipe = json(ROOT.resolve(
                    "data/thaumcraftmodern/thaumcraft/infusion_recipes/" + id + ".json"));
            assertEquals("armorvoidfortress", recipe.get("research").getAsString());
            assertEquals(6, recipe.get("instability").getAsInt());
            assertEquals("thaumcraftmodern:" + id,
                    recipe.getAsJsonObject("result").get("item").getAsString());
        }
    }

    @Test
    void packagedVoidTexturesAreByteExactTc4Assets() throws Exception {
        var mapping = java.util.Map.ofEntries(
                java.util.Map.entry("void_sword", "voidsword"),
                java.util.Map.entry("void_pickaxe", "voidpick"),
                java.util.Map.entry("void_axe", "voidaxe"),
                java.util.Map.entry("void_shovel", "voidshovel"),
                java.util.Map.entry("void_hoe", "voidhoe"),
                java.util.Map.entry("void_helmet", "voidhelm"),
                java.util.Map.entry("void_chestplate", "voidchest"),
                java.util.Map.entry("void_leggings", "voidlegs"),
                java.util.Map.entry("void_boots", "voidboots"),
                java.util.Map.entry("void_robe_hood", "voidrobehelm"),
                java.util.Map.entry("void_robe_chestplate", "voidrobechest"),
                java.util.Map.entry("void_robe_leggings", "voidrobelegs"));
        Path original = Path.of("reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures/items");
        Path modern = ROOT.resolve("assets/thaumcraftmodern/textures/item");
        for (var entry : mapping.entrySet()) {
            assertEquals(hash(original.resolve(entry.getValue() + ".png")),
                    hash(modern.resolve(entry.getKey() + ".png")), entry.getKey());
        }
    }

    private static JsonObject research(String id) throws Exception {
        return json(ROOT.resolve("data/thaumcraftmodern/thaumcraft/research/legacy/" + id + ".json"));
    }
    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
    private static List<String> strings(JsonObject object, String field) {
        return object.getAsJsonArray(field).asList().stream().map(value -> value.getAsString()).toList();
    }
    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)));
    }
}
