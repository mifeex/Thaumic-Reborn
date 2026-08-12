package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArmorLeggingsRenderFidelityTest {
    @Test
    void fortressLeggingsSuppressTheChestModelChildren() throws Exception {
        String source = source("client/render/FortressArmorModel.java");

        assertTrue(source.contains("suppressChestGeometryForLeggings(slot)"));
        assertTrue(source.contains("slot == EquipmentSlot.LEGS"));
        assertTrue(source.contains("body.getAllParts().skip(1)"));
    }

    @Test
    void everyCrimsonLeggingsSetSuppressesTheChestModelChildren() throws Exception {
        String model = source("client/render/CrimsonCultArmorModel.java");
        String extensions = source(
                "client/render/CultistArmorClientExtensions.java"
        );

        assertTrue(model.contains("slot == EquipmentSlot.LEGS"));
        assertTrue(model.contains("body.getAllParts().skip(1)"));
        assertTrue(extensions.contains(
                "model.suppressChestGeometryForLeggings(slot)"
        ));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(Path.of("src/main/java/com/thaumcraftmodern")
                .resolve(relativePath));
    }
}
