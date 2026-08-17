package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.world.item.ArmorItem;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MirrorFortressFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn");

    @Test
    void fortressMaterialRetainsTc4Values() {
        assertEquals(25, FortressArmorMaterial.INSTANCE.getEnchantmentValue());
        assertEquals(3, FortressArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.HELMET));
        assertEquals(7, FortressArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.CHESTPLATE));
        assertEquals(6, FortressArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.LEGGINGS));
        assertEquals(3, FortressArmorMaterial.INSTANCE
                .getDefenseForType(ArmorItem.Type.BOOTS));
        assertEquals(11 * 40, FortressArmorMaterial.INSTANCE
                .getDurabilityForType(ArmorItem.Type.HELMET));
        assertEquals(16 * 40, FortressArmorMaterial.INSTANCE
                .getDurabilityForType(ArmorItem.Type.CHESTPLATE));
        assertEquals(15 * 40, FortressArmorMaterial.INSTANCE
                .getDurabilityForType(ArmorItem.Type.LEGGINGS));
        assertEquals(13 * 40, FortressArmorMaterial.INSTANCE
                .getDurabilityForType(ArmorItem.Type.BOOTS));
    }

    @Test
    void originalMirrorAndFortressTexturesAreByteExact() throws Exception {
        hash("textures/block/mirrorframe.png",
                "0512b6a79b2af3b22174273b24da2836bd4f1cad1ffbc6d8356591693a2dd331");
        hash("textures/block/mirrorframe2.png",
                "55573361752c58b751ad2e3badebec80b524eb0f760f49ca1d9d2cc0ee39387b");
        hash("textures/block/mirrorpane.png",
                "e40cbf5daedfa1bdf7a3d9a4d09bb5306f5fb9f9f0d98e2a2ec3abc8548016a4");
        hash("textures/block/mirrorpanetrans.png",
                "b27c20b4fec3ffd1b9950d36a2137a9bc56b64b023f380511be3a313ac91079a");
        hash("textures/gui/guihandmirror.png",
                "cfd54c70774a48242492a12e2352c7f609b807d3489650b8960b8a08a6a55206");
        hash("textures/entity/models/fortress_armor.png",
                "22e39ef81224365bcb8d41a9014232d46a92a0752109c7994c1ccdc460ad842d");
        hash("textures/misc/r_mask0.png",
                "0b67fe39c41822598f01c89098da5852fc931c711d22fd5347defa3dec24c8a1");
        hash("textures/misc/r_mask1.png",
                "e9c3f14e32cdd298523f5dda4baff7785a0386be62c62b577f186e5f12511cf0");
        hash("textures/misc/r_mask2.png",
                "c1a0523e3666b779251d9e4dbf6d4e98c9c56550b84fe1fa7d22b2166471f20e");
    }

    @Test
    void bothMirrorFamiliesKeepSeparateOpenAndClosedSurfaces() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "MagicMirrorBlockEntityRenderer.java"));
        assertTrue(renderer.contains("mirrorpane.png"));
        assertTrue(renderer.contains("mirrorpanetrans.png"));
        assertTrue(renderer.contains("misc/tunnel.png"));
        assertTrue(renderer.contains("misc/particlefield.png"));
        assertTrue(renderer.contains("itemMirror.visuallyOpen()"));
        assertTrue(renderer.contains("if (open)"));
        assertTrue(renderer.contains("drawOpenSurface"));
        assertTrue(renderer.contains("drawRotated90"));
        assertTrue(renderer.contains("boolean rotateInner = !(mirror instanceof EssentiaMirrorBlockEntity)"));
        assertTrue(renderer.contains("boolean rotate90)"));
        assertTrue(renderer.contains("mirror instanceof EssentiaMirrorBlockEntity"));
        assertTrue(renderer.contains("FRAME_ESSENTIA : FRAME"));
        assertTrue(renderer.contains("packedLight, !(mirror instanceof EssentiaMirrorBlockEntity)"));
        assertTrue(renderer.contains("original essentia frame is already vertical"));
        assertTrue(renderer.contains("int[][] spans"));
        assertTrue(renderer.contains("outwardOffset, 0.0F"));

        String handMirror = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/HandMirrorItem.java"));
        assertTrue(handMirror.contains("mirror.addPortableLink(portableId)"));
    }

    @Test
    void allThreeMaskHelmetsHaveItemsBookOutputsAndRemoteEntityRendering()
            throws Exception {
        record Mask(String id, String research, int value, String hash) { }
        var masks = java.util.List.of(
                new Mask("grinning_devil", "maskgrinningdevil", 0,
                        "0b67fe39c41822598f01c89098da5852fc931c711d22fd5347defa3dec24c8a1"),
                new Mask("angry_ghost", "maskangryghost", 1,
                        "e9c3f14e32cdd298523f5dda4baff7785a0386be62c62b577f186e5f12511cf0"),
                new Mask("sipping_fiend", "masksippingfiend", 2,
                        "c1a0523e3666b779251d9e4dbf6d4e98c9c56550b84fe1fa7d22b2166471f20e"));
        Path data = Path.of("src/main/resources/data/thaumic_reborn/thaumcraft");
        for (Mask mask : masks) {
            String itemId = "thaumic_reborn:fortress_helmet_mask_" + mask.id();
            JsonObject recipe = JsonParser.parseString(Files.readString(data.resolve(
                    "infusion_recipes/fortress_mask_" + mask.id() + ".json")))
                    .getAsJsonObject();
            assertEquals(itemId, recipe.getAsJsonObject("result")
                    .get("item").getAsString());
            assertEquals("item_replacement", recipe.getAsJsonObject(
                    "result_modifier").get("type").getAsString());
            assertEquals(mask.value(), recipe.getAsJsonObject(
                    "result_modifier").get("value").getAsInt());
            JsonObject research = JsonParser.parseString(Files.readString(data.resolve(
                    "research/legacy/" + mask.research() + ".json")))
                    .getAsJsonObject();
            assertEquals("thaumic_reborn:textures/misc/r_mask"
                    + mask.value() + ".png",
                    research.get("icon_resource").getAsString());
            assertEquals(itemId, research.getAsJsonArray("pages").get(1)
                    .getAsJsonObject().get("output").getAsString());
            JsonObject itemModel = JsonParser.parseString(Files.readString(
                    ASSETS.resolve("models/item/fortress_helmet_mask_"
                            + mask.id() + ".json"))).getAsJsonObject();
            assertEquals("thaumic_reborn:item/fortress_helmet",
                    itemModel.getAsJsonObject("textures")
                            .get("layer0").getAsString());
            hash("textures/misc/r_mask" + mask.value() + ".png", mask.hash());
        }

        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/FortressArmorModel.java"));
        assertTrue(model.contains("prepare(LivingEntity entity"));
        assertTrue(model.contains("FortressArmorItem.mask(stack)"));
        assertTrue(model.contains("child(head, \"Mask\" + index"));
    }

    @Test
    void gogglesAndEveryMaskAcceptEveryFortressHelmetVariant()
            throws Exception {
        Path data = Path.of("src/main/resources/data/thaumic_reborn");
        JsonObject tag = JsonParser.parseString(Files.readString(data.resolve(
                "tags/items/fortress_helmets.json"))).getAsJsonObject();
        assertEquals(java.util.List.of(
                "thaumic_reborn:fortress_helmet",
                "thaumic_reborn:fortress_helmet_mask_grinning_devil",
                "thaumic_reborn:fortress_helmet_mask_angry_ghost",
                "thaumic_reborn:fortress_helmet_mask_sipping_fiend"
        ), tag.getAsJsonArray("values").asList().stream()
                .map(value -> value.getAsString())
                .toList());

        for (String recipeName : java.util.List.of(
                "fortress_helmet_goggles",
                "fortress_mask_grinning_devil",
                "fortress_mask_angry_ghost",
                "fortress_mask_sipping_fiend"
        )) {
            JsonObject recipe = JsonParser.parseString(Files.readString(
                    data.resolve("thaumcraft/infusion_recipes/"
                            + recipeName + ".json"))).getAsJsonObject();
            assertEquals("thaumic_reborn:fortress_helmets",
                    recipe.getAsJsonObject("central").get("tag").getAsString(),
                    recipeName);
        }

        String definition = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/infusion/"
                        + "InfusionRecipeDefinition.java"));
        assertTrue(definition.contains(
                "if (centralStack.hasTag()) result.setTag(centralStack.getTag().copy())"));
    }

    private static void hash(String path, String expected) throws Exception {
        byte[] bytes = Files.readAllBytes(ASSETS.resolve(path));
        assertEquals(expected, HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(bytes)), path);
    }
}
