package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArmorLeggingsRenderFidelityTest {
    @Test
    void fortressLeggingsUseTheExactTc4InnerAttachments() throws Exception {
        String source = source("client/render/FortressArmorModel.java");

        assertTrue(source.contains("LEGGINGS_LAYER"));
        assertTrue(source.contains("isLeggingsPart(line)"));
        assertTrue(source.contains("value[1].equals(\"mbelt\")"));
        assertTrue(source.contains("value[1].equals(\"mbeltl\")"));
        assertTrue(source.contains("value[1].equals(\"mbeltr\")"));
    }

    @Test
    void fortressBootsUseTheNormalBootModelInsteadOfLegPanels()
            throws Exception {
        String item = source("item/FortressArmorItem.java");
        String extensions = source(
                "client/render/FortressArmorClientExtensions.java");
        String optiFine = source(
                "client/render/OptiFineArmorCompatibility.java");

        assertTrue(item.contains("slot == EquipmentSlot.FEET"));
        assertTrue(item.contains("thaumium_layer_1.png"));
        assertTrue(extensions.contains("return defaultModel"));
        assertTrue(optiFine.contains("Fortress boots stay on the original"));
        assertTrue(optiFine.contains("return null"));
    }

    @Test
    void everyCrimsonSetHasARealInnerLeggingsModel() throws Exception {
        String model = source("client/render/CrimsonCultArmorModel.java");
        String extensions = source(
                "client/render/CultistArmorClientExtensions.java"
        );
        String optiFine = source(
                "client/render/OptiFineArmorCompatibility.java"
        );
        String registrations = source(
                "client/WorldContentClientEvents.java"
        );

        assertTrue(model.contains("KNIGHT_LEGGINGS_LAYER"));
        assertTrue(model.contains("CLERIC_LEGGINGS_LAYER"));
        assertTrue(model.contains("PRAETOR_LEGGINGS_LAYER"));
        assertTrue(model.contains("emptyHumanoidMesh()"));
        assertTrue(model.contains("BASIC_BELT_PARTS"));
        assertTrue(model.contains("CLERIC_INNER_BODY_PARTS"));
        assertTrue(model.contains("isAttachedInOriginal"));
        assertTrue(extensions.contains("leggingsModel"));
        assertTrue(extensions.contains("model.configureForSlot(slot)"));
        assertTrue(optiFine.contains("models.knightLeggings"));
        assertTrue(optiFine.contains("models.clericLeggings"));
        assertTrue(optiFine.contains("models.praetorLeggings"));
        assertTrue(optiFine.contains("cultist.configureForSlot(slot)"));
        assertTrue(registrations.contains("createKnightLeggingsLayer"));
        assertTrue(registrations.contains("createClericLeggingsLayer"));
        assertTrue(registrations.contains("createPraetorLeggingsLayer"));
    }

    @Test
    void crimsonInnerLayersCannotReintroduceTorsoArmor() throws Exception {
        String model = source("client/render/CrimsonCultArmorModel.java");
        String innerBodyAllowlist = between(
                model,
                "private static final Set<String> BASIC_BELT_PARTS",
                "public CrimsonCultArmorModel"
        );

        assertTrue(model.contains(
                "MeshDefinition mesh = emptyHumanoidMesh();"
        ));
        assertTrue(model.contains("return !leggingsOnly;"));
        assertFalse(innerBodyAllowlist.contains("chestplate"));
        assertFalse(innerBodyAllowlist.contains("chestthing"));
        assertFalse(innerBodyAllowlist.contains("chestornament"));
        assertFalse(innerBodyAllowlist.contains("backplate"));
        assertFalse(innerBodyAllowlist.contains("clothchest"));
        assertFalse(innerBodyAllowlist.contains("collar"));
    }

    @Test
    void crimsonSlotVisibilityKeepsJacketsOffTheLegRoots() throws Exception {
        String model = source("client/render/CrimsonCultArmorModel.java");
        String chestCase = between(
                model,
                "case CHEST -> {",
                "case LEGS -> {"
        );
        String legsCase = between(
                model,
                "case LEGS -> {",
                "case FEET -> {"
        );

        assertTrue(chestCase.contains("setTreeVisible(body, true)"));
        assertTrue(chestCase.contains("setTreeVisible(rightArm, true)"));
        assertTrue(chestCase.contains("setTreeVisible(leftArm, true)"));
        assertFalse(chestCase.contains("rightLeg"));
        assertFalse(chestCase.contains("leftLeg"));
        assertTrue(legsCase.contains("setTreeVisible(body, true)"));
        assertTrue(legsCase.contains("setTreeVisible(rightLeg, true)"));
        assertTrue(legsCase.contains("setTreeVisible(leftLeg, true)"));
        assertFalse(legsCase.contains("rightArm"));
        assertFalse(legsCase.contains("leftArm"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(Path.of("src/main/java/com/thaumcraftmodern")
                .resolve(relativePath));
    }

    private static String between(String source, String start, String end) {
        int startIndex = source.indexOf(start);
        int endIndex = source.indexOf(end, startIndex);
        assertTrue(startIndex >= 0, "Missing start marker: " + start);
        assertTrue(endIndex > startIndex, "Missing end marker: " + end);
        return source.substring(startIndex, endIndex);
    }
}
