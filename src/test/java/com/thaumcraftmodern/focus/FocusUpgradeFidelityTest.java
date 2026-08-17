package com.thaumcraftmodern.focus;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class FocusUpgradeFidelityTest {
    @Test void stableClassicUpgradeIdsRemainComplete() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/focus/FocusUpgradeType.java"));
        for (int id = 0; id <= 20; id++)
            assertTrue(source.contains("(" + id + ","), "missing stable TC4 upgrade id " + id);
        assertTrue(source.contains("VAMPIRE_BATS(19"));
        assertTrue(source.contains("DOWSING(20"));
    }

    @Test void remainingFociExposeOriginalSpecialRankChoices() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/thaumcraftmodern/focus/WandFocusType.java"));
        assertTrue(source.contains("case HELLBAT"));
        assertTrue(source.contains("FocusUpgradeType.BAT_BOMBS"));
        assertTrue(source.contains("case PORTABLE_HOLE"));
        assertTrue(source.contains("FocusUpgradeType.EXTEND"));
        assertTrue(source.contains("case WARDING"));
        assertTrue(source.contains("FocusUpgradeType.ARCHITECT"));
    }

    @Test void remainingFocusAssetsAndInfusionVerticalsArePresent() {
        Path assets = Path.of("src/main/resources/assets/thaumic_reborn");
        assertTrue(Files.isRegularFile(assets.resolve("textures/item/focus_hellbat.png")));
        assertTrue(Files.isRegularFile(assets.resolve("textures/item/focus_portablehole.png")));
        assertTrue(Files.isRegularFile(assets.resolve("textures/item/focus_warding.png")));
        assertTrue(Files.isRegularFile(assets.resolve("textures/gui/gui_wandtable.png")));
        assertTrue(Files.isRegularFile(assets.resolve("textures/block/wandtable_inventory.png")));
        try {
            String model = Files.readString(assets.resolve("models/block/focal_manipulator.json"));
            assertTrue(model.contains("thaumic_reborn:block/wandtable_inventory"));
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
        Path infusion = Path.of("src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes");
        assertTrue(Files.isRegularFile(infusion.resolve("focus_hellbat.json")));
        assertTrue(Files.isRegularFile(infusion.resolve("focus_portable_hole.json")));
        assertTrue(Files.isRegularFile(infusion.resolve("focus_warding.json")));
    }

    @Test void focalManipulatorKeepsTheOriginalTwoStepGuiContract() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/FocalManipulatorScreen.java"));
        assertTrue(source.contains("selectedUpgrade = selectedUpgrade == id ? -1 : id"));
        assertTrue(source.contains("inside(mouseX, mouseY, START_X, START_Y"));
        assertTrue(source.contains("ClassicUiRender.drawAspectVisRow"));
        assertTrue(source.contains("protected void renderLabels"));
    }
}
