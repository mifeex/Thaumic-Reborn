package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FocusPouchAndArcanePressurePlateFidelityTest {
    private static final Path JAVA = Path.of("src/main/java/com/thaumcraftmodern");
    private static final Path ASSETS = Path.of("src/main/resources/assets/thaumic_reborn");
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft");

    @Test
    void focusPouchKeepsTheClassicEighteenSingleFocusSlotsAndGui() throws Exception {
        String item = Files.readString(JAVA.resolve("item/FocusPouchItem.java"));
        String menu = Files.readString(JAVA.resolve("world/menu/FocusPouchMenu.java"));
        String screen = Files.readString(JAVA.resolve("client/screen/FocusPouchScreen.java"));

        assertTrue(item.contains("SLOT_COUNT = 18"));
        assertTrue(item.contains("INVENTORY_TAG = \"Inventory\""));
        assertTrue(item.contains("putByte(\"Slot\""));
        assertTrue(menu.contains("instanceof WandFocusItem"));
        assertTrue(menu.contains("getMaxStackSize() { return 1; }"));
        assertTrue(menu.contains("37 + column * 18, 51 + row * 18"));
        assertTrue(menu.contains("151 + row * 18"));
        assertTrue(menu.contains("8 + column * 18, 209"));
        assertTrue(screen.contains("imageWidth = 175"));
        assertTrue(screen.contains("imageHeight = 232"));
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("textures/gui/gui_focuspouch.png")),
                Files.readAllBytes(ASSETS.resolve("textures/gui/gui_focuspouch.png")));
    }

    @Test
    void wandSelectorReadsAndExchangesFociInsideThePouch() throws Exception {
        String radial = Files.readString(JAVA.resolve("client/screen/WandFocusRadialScreen.java"));
        String service = Files.readString(JAVA.resolve("focus/WandFocusService.java"));
        assertTrue(radial.contains("FocusPouchItem.loadInventory(stack).stream()"));
        assertTrue(service.contains("FocusSource.pouch(slot, pouchSlot)"));
        assertTrue(service.contains("takeFocus(player, selected)"));
        assertTrue(service.contains("addToFocusPouch(player, previous)"));
        assertTrue(service.contains("restoreFocus(player, selected, chosen)"));
    }

    @Test
    void arcanePlateHasAllClassicModesAndPressureBehavior() throws Exception {
        String block = Files.readString(JAVA.resolve("world/block/ArcanePressurePlateBlock.java"));
        String entity = Files.readString(JAVA.resolve(
                "world/block/entity/ArcanePressurePlateBlockEntity.java"));
        assertTrue(block.contains("IntegerProperty.create(\"mode\", 0, 2)"));
        assertTrue(block.contains("new AABB(0.125, 0, 0.125, 0.875, 0.25, 0.875)"));
        assertTrue(block.contains("entity.isIgnoringBlockTriggers()"));
        assertTrue(block.contains("level.scheduleTick(pos, this, 20)"));
        assertTrue(block.contains("state.getValue(POWERED) ? 15 : 0"));
        assertTrue(entity.contains("access.contains(\"0\" + name)"));
        assertTrue(entity.contains("access.contains(\"1\" + name)"));

        JsonObject variants = JsonParser.parseString(Files.readString(
                ASSETS.resolve("blockstates/arcane_pressure_plate.json")))
                .getAsJsonObject().getAsJsonObject("variants");
        assertEquals(6, variants.size());
        assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve("textures/blocks/applate1.png")),
                Files.readAllBytes(ASSETS.resolve("textures/block/arcane_pressure_plate_all.png")));
        assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve("textures/blocks/applate2.png")),
                Files.readAllBytes(ASSETS.resolve("textures/block/arcane_pressure_plate_except_owner.png")));
        assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve("textures/blocks/applate3.png")),
                Files.readAllBytes(ASSETS.resolve("textures/block/arcane_pressure_plate_owner_only.png")));
    }
}
