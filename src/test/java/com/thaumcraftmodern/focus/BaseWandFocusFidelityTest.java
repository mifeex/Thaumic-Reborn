package com.thaumcraftmodern.focus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseWandFocusFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final List<WandFocusType> BASE_FOCI = List.of(
            WandFocusType.FIRE, WandFocusType.FROST, WandFocusType.SHOCK,
            WandFocusType.TRADE, WandFocusType.EXCAVATION, WandFocusType.PRIMAL);

    @Test
    void baseCostsColorsAndCadenceMatchTc4UnupgradedFoci() {
        assertEquals(Map.of("ignis", 10), WandFocusType.FIRE.centivisCost());
        assertEquals(Map.of("aqua", 5, "ignis", 2, "perditio", 2),
                WandFocusType.FROST.centivisCost());
        assertEquals(Map.of("aer", 25), WandFocusType.SHOCK.centivisCost());
        assertEquals(Map.of("perditio", 5, "terra", 5, "ordo", 5),
                WandFocusType.TRADE.centivisCost());
        assertEquals(Map.of("terra", 15), WandFocusType.EXCAVATION.centivisCost());
        assertEquals(0xFF4500, WandFocusType.FIRE.color());
        assertEquals(0x4F69CC, WandFocusType.FROST.color());
        assertEquals(0xFFFF7E, WandFocusType.SHOCK.color());
        assertEquals(0x00CED1, WandFocusType.TRADE.color());
        assertEquals(0x064006, WandFocusType.EXCAVATION.color());
        assertEquals(4, WandFocusType.FROST.cooldownTicks());
        assertEquals(5, WandFocusType.SHOCK.cooldownTicks());
        assertEquals(10, WandFocusType.PRIMAL.cooldownTicks());
        assertEquals(true, WandFocusType.FIRE.perTickCost());
        assertEquals(true, WandFocusType.EXCAVATION.perTickCost());
        assertFalse(WandFocusType.SHOCK.perTickCost());
    }

    @Test
    void itemTexturesAreByteExactTc4Assets() throws Exception {
        Path modern = ROOT.resolve("src/main/resources/assets/thaumic_reborn/textures/item");
        Path original = ROOT.resolve("reference/Thaumcraft-4.2-FOREVA-master/thaumcraft_src/"
                + "assets/thaumcraft/textures/items");
        for (WandFocusType type : BASE_FOCI) {
            String file = type.itemId() + ".png";
            assertArrayEquals(Files.readAllBytes(original.resolve(file)),
                    Files.readAllBytes(modern.resolve(file)), file);
        }
    }

    @Test
    void radialSelectorUsesOriginalTexturesAndHoldReleaseContract()
            throws Exception {
        Path original = ROOT.resolve("reference/Thaumcraft-4.2-FOREVA-master/"
                + "thaumcraft_src/assets/thaumcraft/textures/misc");
        Path modern = ROOT.resolve(
                "src/main/resources/assets/thaumic_reborn/textures/misc");
        for (String file : new String[]{"radial.png", "radial2.png"}) {
            assertArrayEquals(Files.readAllBytes(original.resolve(file)),
                    Files.readAllBytes(modern.resolve(file)), file);
        }

        String screen = Files.readString(ROOT.resolve("src/main/java/"
                + "com/thaumcraftmodern/client/screen/WandFocusRadialScreen.java"));
        String keys = Files.readString(ROOT.resolve("src/main/java/"
                + "com/thaumcraftmodern/client/screen/WandFocusKeyEvents.java"));
        assertTrue(keys.contains("GLFW.GLFW_KEY_F"));
        assertTrue(keys.contains("minecraft.player.isShiftKeyDown()"));
        assertTrue(keys.contains("CHANGE_FOCUS.matches(keyCode, scanCode)"));
        assertTrue(screen.contains("public boolean keyReleased("));
        assertTrue(screen.contains("WandFocusKeyEvents.matchesFocusKey("));
        assertFalse(screen.contains("!WandFocusKeyEvents.focusKeyDown()"));
        assertTrue(screen.contains("sendHovered()"));
        assertTrue(screen.contains("currentFocus"));
        assertTrue(screen.contains("radius * 2.75F"));
        assertTrue(screen.contains("radius * 2.55F"));
    }

    @Test
    void recipesAndThaumonomiconPagesAreActiveAndLinked() throws Exception {
        Path resources = ROOT.resolve("src/main/resources");
        for (WandFocusType type : BASE_FOCI) {
            String compact = "focus" + type.id();
            JsonObject recipe = json(resources.resolve("data/thaumic_reborn/recipes/"
                    + type.itemId() + ".json"));
            assertEquals("thaumic_reborn:arcane_shaped", recipe.get("type").getAsString());
            assertEquals("thaumic_reborn:" + type.itemId(),
                    recipe.getAsJsonObject("result").get("item").getAsString());
            JsonObject research = json(resources.resolve(
                    "data/thaumic_reborn/thaumcraft/research/legacy/" + compact + ".json"));
            assertFalse(research.get("inactive").getAsBoolean());
            boolean linked = research.getAsJsonArray("pages").asList().stream()
                    .map(element -> element.getAsJsonObject())
                    .anyMatch(page -> page.has("recipe") && page.get("recipe").getAsString()
                            .equals("thaumic_reborn:" + type.itemId()));
            assertEquals(true, linked, compact);
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8))
                .getAsJsonObject();
    }
}
