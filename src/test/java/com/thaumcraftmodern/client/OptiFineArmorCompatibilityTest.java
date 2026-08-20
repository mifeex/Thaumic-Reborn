package com.thaumcraftmodern.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

final class OptiFineArmorCompatibilityTest {
    @Test
    void everyCustomArmorExtensionBypassesOptiFinesPatchedArmorLayer()
            throws Exception {
        for (String extension : List.of(
                "CultistArmorClientExtensions.java",
                "FortressArmorClientExtensions.java",
                "ThaumaturgeRobeClientExtensions.java",
                "VoidRobeClientExtensions.java",
                "VoidArmorClientExtensions.java",
                "WingedMantleClientExtensions.java"
        )) {
            String source = renderSource(extension);
            assertTrue(source.contains(
                            "OptiFineArmorCompatibility.invisibleModel()"),
                    extension + " must suppress OptiFine's broken pass");
        }
    }

    @Test
    void everyCustomArmorItemMakesOptiFinesOriginalPassTransparent()
            throws Exception {
        for (String item : List.of(
                "CultistArmorItem.java",
                "FortressArmorItem.java",
                "ThaumaturgeRobeItem.java",
                "VoidRobeArmorItem.java",
                "VoidArmorItem.java",
                "WingedMantleArmorItem.java"
        )) {
            String source = itemSource(item);
            assertTrue(source.contains("OptiFinePresence.loaded()"),
                    item + " must target only OptiFine");
            assertTrue(source.contains("transparent_armor.png"),
                    item + " must suppress the duplicate textured pass");
        }
        assertTrue(Files.exists(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/"
                        + "entity/models/transparent_armor.png")));

        String voidArmor = itemSource("VoidArmorItem.java");
        assertTrue(voidArmor.contains(
                        "OptiFinePresence.loaded() && slot == EquipmentSlot.CHEST"),
                "ordinary Void helmet, leggings and boots need vanilla's pass");
    }

    @Test
    void optiFineDetectionIsSafeForCommonAndDedicatedServerCode()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/compat/"
                        + "OptiFinePresence.java"));
        assertTrue(source.contains("Class.forName(\"net.optifine.Config\""));
        assertTrue(!source.contains("net.minecraft.client"));
    }

    @Test
    void replacementLayerIsOptiFineOnlyAndCoversEveryCustomArmorFamily()
            throws Exception {
        String source = renderSource("OptiFineArmorCompatibility.java");

        assertTrue(source.contains("OptiFinePresence.loaded()"));
        assertTrue(source.contains("class InvisibleArmorModel"));
        assertTrue(source.contains("class ArmorLayer"));
        assertTrue(source.contains("ItemRenderer.getArmorFoilBuffer"));
        assertTrue(source.contains("WingedMantleArmorModel.OPTIFINE_LAYER"));
        assertTrue(source.contains("winged_mantle_armor_optifine.png"));
        assertTrue(source.contains("cultist_plate_armor.png"));
        assertTrue(source.contains("fortress_armor.png"));
        for (String armor : List.of(
                "CultistArmorItem", "FortressArmorItem",
                "ThaumaturgeRobeItem", "VoidRobeArmorItem",
                "VoidArmorItem", "WingedMantleArmorItem"
        )) {
            assertTrue(source.contains("instanceof " + armor),
                    armor + " must use the replacement render layer");
        }
    }

    @Test
    void clientModBusInstallsTheCompatibilityLayer() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientModEvents.java"));
        assertTrue(source.contains(
                "OptiFineArmorCompatibility.addLayers(event)"));
    }

    private static String renderSource(String name) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render", name));
    }

    private static String itemSource(String name) throws Exception {
        return Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item", name));
    }
}
