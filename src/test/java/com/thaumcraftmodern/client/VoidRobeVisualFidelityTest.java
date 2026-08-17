package com.thaumcraftmodern.client;

import com.thaumcraftmodern.item.VoidRobeArmorItem;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VoidRobeVisualFidelityTest {
    private static final Path MOD = Path.of("src/main/resources/assets/thaumic_reborn");
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft"
    );

    @Test
    void wearableAndInventoryTexturesAreByteExactTc4Assets() throws Exception {
        assertSameBytes("textures/models/void_robe_armor.png");
        assertSameBytes("textures/models/void_robe_armor_overlay.png");
        assertSameBytes(
                "textures/item/void_robe_hood.png",
                "textures/items/voidrobehelm.png"
        );
        assertSameBytes(
                "textures/item/void_robe_chestplate.png",
                "textures/items/voidrobechest.png"
        );
        assertSameBytes(
                "textures/item/void_robe_chestplate_overlay.png",
                "textures/items/voidrobechestover.png"
        );
        assertSameBytes(
                "textures/item/void_robe_leggings.png",
                "textures/items/voidrobelegs.png"
        );
        assertSameBytes(
                "textures/item/void_robe_leggings_overlay.png",
                "textures/items/voidrobelegsover.png"
        );
    }

    @Test
    void modelContainsEveryAttachedTc4CuboidAndOriginalClothAnimation()
            throws Exception {
        List<String> rows = Files.readAllLines(
                MOD.resolve("models/entity/void_robe_armor.csv")
        ).stream().filter(line -> !line.isBlank() && !line.startsWith("#"))
                .toList();
        assertEquals(56, rows.size());
        assertTrue(rows.stream().anyMatch(line -> line.contains(
                "both,head,hood_4,53,15,false,-3.0,-10.7,3.5,6,7,3"
        )));
        assertTrue(rows.stream().anyMatch(line -> line.contains(
                "outer,body,book,81,16,false,1.0,0.0,4.0,5,7,2"
        )));
        assertTrue(rows.stream().anyMatch(line -> line.contains(
                "inner,leftLeg,focus_pouch,100,20,false,3.5,0.5,-2.5,3,6,5"
        )));
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/VoidRobeArmorModel.java"
        ));
        assertTrue(model.contains("LayerDefinition.create(mesh, 128, 64)"));
        assertTrue(model.contains("limbSwing * 0.6662F"));
        assertTrue(model.contains("cloth - 0.1047198F"));
        assertTrue(model.contains("-cloth + 0.2268928F"));
    }

    @Test
    void robeSleevesMatchCrimsonClericWidths() throws Exception {
        List<String> rows = Files.readAllLines(
                MOD.resolve("models/entity/void_robe_armor.csv")
        );
        assertTrue(rows.contains(
                "both,rightArm,shoulder,16,45,true,-3.5,-2.5,-2.5,5,5,5,0,0,0,0,0,0"
        ));
        assertTrue(rows.contains(
                "both,rightArm,arm_1,88,39,false,-3.5,2.5,-2.5,5,7,5,0,0,0,0,0,0"
        ));
        assertTrue(rows.contains(
                "both,leftArm,shoulder,16,45,true,-1.5,-2.5,-2.5,5,5,5,0,0,0,0,0,0"
        ));
        assertTrue(rows.contains(
                "both,leftArm,arm_1,88,39,true,-1.5,2.5,-2.5,5,7,5,0,0,0,0,0,0"
        ));
        String extension = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/VoidRobeClientExtensions.java"
        ));
        assertFalse(extension.contains("narrowSleeves"));
    }

    @Test
    void onlyOrdinaryVoidChestplateNarrowsSleevesHorizontally() throws Exception {
        String item = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/VoidArmorItem.java"
        ));
        assertTrue(item.contains("VoidArmorClientExtensions.create()"));
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/VoidArmorChestModel.java"
        ));
        assertTrue(model.contains("SLEEVE_HORIZONTAL_SCALE = 0.65F"));
        assertTrue(model.contains("rightArm.xScale = SLEEVE_HORIZONTAL_SCALE"));
        assertTrue(model.contains("leftArm.xScale = SLEEVE_HORIZONTAL_SCALE"));
        String extension = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/VoidArmorClientExtensions.java"
        ));
        assertTrue(extension.contains("slot != EquipmentSlot.CHEST"));
        assertTrue(extension.contains("chest.narrowSleevesHorizontally()"));
    }

    @Test
    void voidRobeUsesItsOwnTwoLayerModelAndOriginalDefaultDye() throws Exception {
        assertEquals(0x6A3880, VoidRobeArmorItem.DEFAULT_COLOR);
        String item = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/VoidRobeArmorItem.java"
        ));
        assertTrue(item.contains("VoidRobeClientExtensions.create()"));
        String extension = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/VoidRobeClientExtensions.java"
        ));
        assertTrue(extension.contains("slot == EquipmentSlot.HEAD"));
        assertTrue(extension.contains("slot == EquipmentSlot.LEGS"));
        assertTrue(extension.contains("VoidRobeArmorModel.OUTER_LAYER"));
        assertTrue(extension.contains("VoidRobeArmorModel.INNER_LAYER"));
    }

    private static void assertSameBytes(String relative) throws Exception {
        assertSameBytes(relative, relative);
    }

    private static void assertSameBytes(String modern, String original)
            throws Exception {
        assertEquals(
                hash(ORIGINAL.resolve(original)),
                hash(MOD.resolve(modern)),
                modern
        );
    }

    private static String hash(Path path) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(Files.readAllBytes(path))
        );
    }
}
