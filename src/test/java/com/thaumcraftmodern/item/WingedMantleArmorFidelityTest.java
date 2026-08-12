package com.thaumcraftmodern.item;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class WingedMantleArmorFidelityTest {
    private static final int ARMOR_ATLAS_SCALE = 16;

    @Test
    void chestPieceUsesForgeElytraContract() throws Exception {
        String item = source("com/thaumcraftmodern/item/WingedMantleArmorItem.java");
        assertTrue(item.contains("canElytraFly"));
        assertTrue(item.contains("elytraFlightTick"));
        assertTrue(item.contains("(flightTicks + 1) % 20 == 0"));
        assertTrue(item.contains("EquipmentSlot.CHEST"));
        assertTrue(item.contains("EquipmentSlot.LEGS"));
        assertTrue(item.contains("winged_mantle_leggings.png"));
    }

    @Test
    void modelKeepsApprovedArmorMantleAndWingDetails() throws Exception {
        String model = source("com/thaumcraftmodern/client/render/WingedMantleArmorModel.java");
        for (String part : new String[]{"hood1", "hood2", "hood3", "hood4", "pauldron_top",
                "pauldron_stud", "bracer", "cleric_forearm_fold", "cleric_forearm_ridge",
                "chest_yoke", "chest_strap_top", "chest_strap_low",
                "chest_focus", "focus_core", "focus_crown",
                "praetor_collar_front", "praetor_collar_back",
                "praetor_collar_left", "praetor_collar_right",
                "praetor_chestplate", "praetor_chestcloth_left",
                "praetor_chestcloth_right", "praetor_backplate",
                "praetor_belt_left", "praetor_belt_right", "raised_chest_focus",
                "back_focus", "back_focus_core",
                "left_tail", "right_tail", "back_mantle", "elytra_bridge",
                "book", "book_clasp", "scroll", "pouch",
                "left_wing", "right_wing", "glyph", "upper_stud", "middle_stud"}) {
            assertTrue(model.contains("\"" + part + "\""), part);
        }
        assertTrue(model.contains("LayerDefinition.create(mesh, 4096, 4096)"));
        assertTrue(model.contains("-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F"));
        assertTrue(model.contains("-0.2268928F"));
        assertTrue(model.contains("-0.3490659F"));
        assertTrue(model.contains("-0.5759587F"));
        assertTrue(model.contains("-4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F"));
        assertTrue(model.contains("-4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F"));
        assertTrue(model.contains("-3.0F, 2.8125F, -4.8F, 6.0F, 6.0F, 1.0F"));
        assertFalse(model.contains("praetor_cloak"));
        assertTrue(model.contains("WingedMantleElytraLayer"));
        assertFalse(model.contains("texOffs(22, 128)"));
        assertTrue(model.contains("mirror ? -1.5F : -3.5F"));
        assertTrue(model.contains("5.0F, 13.0F, 5.0F"));
        assertFalse(model.contains("fitArmsToBody"));
        assertTrue(model.contains("configureForSlot"));
        assertFalse(model.contains("centerNarrowSleeveOverArm"));
        assertFalse(model.contains("sleeve.xScale = 0.8F"));
        assertTrue(model.contains("mirror ? -1.0F : -3.0F, 5.5F, 2.5F"));
        assertTrue(model.contains("mirror ? -0.5F : -2.5F, 3.5F, 2.5F"));
        assertTrue(model.contains("cube(208, 224, -5.7F, 8.6F, -2.9F, 11.4F"));
        assertTrue(model.contains("rightLeg.getChild(\"greave\").visible = true"));
        assertTrue(model.contains("rightLeg.getChild(\"boot\").visible = true"));
        assertTrue(model.contains("body.getChild(\"left_tail\").visible = false"));
        assertTrue(model.contains("empty(body, \"back_mantle\", PartPose.ZERO)"));
        assertFalse(model.contains("cube(28, 50, -4.4F, 5.0F, 2.15F, 8.8F, 13.5F, 1.1F)"));
        assertTrue(model.contains("empty(arm, \"bracer\""));
        assertTrue(model.contains("0.4363323F"));
        assertTrue(model.contains("238, 37"));
        assertTrue(model.contains("empty(body, \"buckle\""));
        assertTrue(model.contains("empty(body, \"scroll\""));
        String extensions = source("com/thaumcraftmodern/client/render/WingedMantleClientExtensions.java");
        assertFalse(extensions.contains("model.animateWings"));
        assertFalse(extensions.contains("fitArmsToBody"));
        assertTrue(extensions.contains("EnumMap<EquipmentSlot"));
        assertTrue(extensions.contains("model.configureForSlot(slot)"));
        String layer = source("com/thaumcraftmodern/client/render/WingedMantleElytraLayer.java");
        assertTrue(layer.contains("new ElytraModel<>(root)"));
        assertTrue(layer.contains("ModelLayers.ELYTRA"));
        assertTrue(layer.contains("RenderType.armorCutoutNoCull(TEXTURE)"));
        assertTrue(layer.contains("limbSwing * 0.6662F"));
        assertTrue(layer.contains("WingedMantleArmorItem"));
        String generator = Files.readString(Path.of("tools/generate_winged_mantle_textures.py"));
        assertTrue(generator.contains("cultist_robe_armor.png"));
        assertTrue(generator.contains("paste_recolored_cultist_hood"));
        assertTrue(generator.contains("light front to dark tail"));
        assertTrue(generator.contains("paint_praetor_gorget"));
        assertTrue(generator.contains("paint_raised_focus"));
        assertTrue(generator.contains("crop((17, 3, 60, 41))"));
        assertTrue(generator.contains("focus_margin = round(w * 0.10)"));
        assertTrue(generator.contains(") * 1.10"));
        assertTrue(generator.contains("the shipped atlas is static"));
        assertTrue(generator.contains("box(draw, 208, 224, 11.4, 2.0, 5.8"));
        assertTrue(generator.contains("box(draw, 208, 240, 4.0, 4.0, 2.0)"));
        assertTrue(generator.contains("box(draw, 224, 240, 3.0, 2.0, 1.0)"));
        assertFalse(generator.contains("paint_void_robe_greave"));
        assertTrue(generator.contains("_original_box_face"));
        assertTrue(generator.contains("_recolor_original_pixels"));
        assertTrue(generator.contains("paint_original_robe_sleeve_tones"));
        assertTrue(generator.contains("Preserve every source pixel and alpha"));
        assertTrue(generator.contains("warm_leather"));
        assertTrue(generator.contains("Legacy helper retained for old tooling"));
        assertTrue(generator.contains("recolor_void_leggings_icon"));
        assertTrue(generator.contains("item/void_robe_leggings.png"));
        assertTrue(generator.contains("generate_leggings_texture"));
        assertTrue(generator.contains("models/void_robe_armor.png"));
        assertTrue(generator.contains("models/void_robe_armor_overlay.png"));
        assertTrue(generator.contains("_recolor_void_robe_details"));
        assertTrue(generator.contains("paste_recolored_praetor_armor"));
        assertTrue(generator.contains("recolor_praetor_trim_to_pauldron"));
        assertTrue(generator.contains("(145, 159, 9.0, 4.0, 1.0)"));
        assertTrue(generator.contains("(145, 154, 9.0, 4.0, 1.0)"));
        assertTrue(generator.contains("(145, 139, 1.0, 4.0, 11.0)"));
        assertTrue(generator.contains("(148, 175, 3.0, 9.0, 1.0)"));
        assertTrue(generator.contains("cultist_leader_armor.png"));
        assertTrue(generator.contains("recolored.resize((128 * SCALE, 64 * SCALE)"));
        assertTrue(generator.contains("hand-authored static atlas"));
        assertFalse(generator.contains("    generate_atlas()"));
        assertTrue(generator.contains("void_robe_hood"));
        assertTrue(generator.contains("cultist_praetor_chestplate"));
        assertTrue(generator.contains("cultist_praetor_leggings"));
        assertTrue(generator.contains("void_boots"));
        assertTrue(generator.contains("Keep an original TC4 icon silhouette"));
    }

    @Test
    void exactPixelAssetsAndAllFourItemsArePackaged() throws Exception {
        var armor = ImageIO.read(Path.of("src/main/resources/assets/thaumcraftmodern/textures/entity/models/winged_mantle_armor.png").toFile());
        assertEquals(4096, armor.getWidth());
        assertEquals(4096, armor.getHeight());
        for (int u : new int[] {0, 64}) {
            int faceY = (192 + 5) * ARMOR_ATLAS_SCALE;
            int frontX = (u + 5) * ARMOR_ATLAS_SCALE;
            int leftX = u * ARMOR_ATLAS_SCALE;
            int rightX = (u + 10) * ARMOR_ATLAS_SCALE;
            int gloveY = faceY + 7 * ARMOR_ATLAS_SCALE;
            int gloveSize = 5 * ARMOR_ATLAS_SCALE;
            for (int y = 0; y < gloveSize; y++) {
                for (int x = 0; x < gloveSize; x++) {
                    int source = armor.getRGB(frontX + x, gloveY + y);
                    assertEquals(source, armor.getRGB(leftX + x, gloveY + y));
                    assertEquals(source, armor.getRGB(rightX + x, gloveY + y));
                }
            }
        }
        var elytra = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/entity/winged_mantle_elytra.png").toFile());
        assertEquals(64, elytra.getWidth());
        assertEquals(32, elytra.getHeight());
        var leggings = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/entity/models/winged_mantle_leggings.png").toFile());
        assertEquals(256, leggings.getWidth());
        assertEquals(128, leggings.getHeight());
        var originalLeggings = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/models/void_robe_armor.png").toFile());
        var originalLeggingsOverlay = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumcraftmodern/textures/models/void_robe_armor_overlay.png").toFile());
        boolean recoloredPixel = false;
        for (int y = 0; y < originalLeggings.getHeight(); y++) {
            for (int x = 0; x < originalLeggings.getWidth(); x++) {
                int original = originalLeggings.getRGB(x, y);
                int overlay = originalLeggingsOverlay.getRGB(x, y);
                int recolored = leggings.getRGB(x, y);
                boolean expectedVisible = (original >>> 24) != 0 || (overlay >>> 24) != 0;
                assertEquals(expectedVisible, (recolored >>> 24) != 0,
                        "leggings lost an original render-pass pixel at " + x + "," + y);
                recoloredPixel |= (original & 0xFFFFFF) != (recolored & 0xFFFFFF)
                        && expectedVisible;
            }
        }
        assertTrue(recoloredPixel, "leggings must change RGB while preserving original pixels");
        var colors = new HashSet<Integer>();
        boolean hasGold = false;
        boolean hasEmerald = false;
        boolean hasLavender = false;
        for (int y = 0; y < armor.getHeight(); y++) {
            for (int x = 0; x < armor.getWidth(); x++) {
                int argb = armor.getRGB(x, y);
                Color color = new Color(argb, true);
                if (color.getAlpha() == 0) {
                    continue;
                }
                colors.add(argb);
                hasGold |= color.getRed() > 170 && color.getGreen() > 90
                        && color.getBlue() < 70;
                hasEmerald |= color.getGreen() > 110
                        && color.getGreen() > color.getRed() * 2;
                hasLavender |= color.getBlue() > 130 && color.getRed() > 90;
            }
        }
        assertTrue(colors.size() >= 14, "armor atlas needs readable material separation");
        assertTrue(hasGold, "armor atlas needs gold trim");
        assertTrue(hasEmerald, "armor atlas needs an emerald focus");
        assertTrue(hasLavender, "armor atlas needs visible arcane glyphs");
        for (String item : new String[]{"hood", "chestplate", "leggings", "boots"}) {
            Path texture = Path.of("src/main/resources/assets/thaumcraftmodern/textures/item/winged_mantle_" + item + ".png");
            Path model = Path.of("src/main/resources/assets/thaumcraftmodern/models/item/winged_mantle_" + item + ".json");
            assertTrue(Files.isRegularFile(texture), texture.toString());
            assertTrue(Files.isRegularFile(model), model.toString());
        }
        String[][] iconSources = {
                {"hood", "void_robe_hood"},
                {"chestplate", "cultist_praetor_chestplate"},
                {"leggings", "cultist_praetor_leggings"},
                {"boots", "void_boots"}
        };
        for (String[] iconSource : iconSources) {
            var icon = ImageIO.read(Path.of("src/main/resources/assets/thaumcraftmodern/textures/item/winged_mantle_"
                    + iconSource[0] + ".png").toFile());
            var original = ImageIO.read(Path.of("src/main/resources/assets/thaumcraftmodern/textures/item/"
                    + iconSource[1] + ".png").toFile());
            assertEquals(16, icon.getWidth());
            assertEquals(16, icon.getHeight());
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    assertEquals(original.getRGB(x, y) >>> 24, icon.getRGB(x, y) >>> 24,
                            iconSource[0] + " changed the original icon silhouette at " + x + "," + y);
                }
            }
        }
        String registry = source("com/thaumcraftmodern/registry/ModItems.java");
        assertTrue(registry.contains("WINGED_MANTLE_HOOD"));
        assertTrue(registry.contains("WINGED_MANTLE_CHESTPLATE"));
        assertTrue(registry.contains("WINGED_MANTLE_LEGGINGS"));
        assertTrue(registry.contains("WINGED_MANTLE_BOOTS"));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(Path.of("src/main/java").resolve(relative));
    }
}
