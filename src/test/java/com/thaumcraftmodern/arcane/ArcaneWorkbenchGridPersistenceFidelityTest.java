package com.thaumcraftmodern.arcane;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ArcaneWorkbenchGridPersistenceFidelityTest {
    @Test
    void sparseCraftingGridPersistsEveryOriginalSlot() throws Exception {
        String inventory = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ArcaneCraftingInventory.java"
        ));
        String workbench = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "ArcaneWorkbenchBlockEntity.java"
        ));

        assertTrue(inventory.contains("item.putByte(\"Slot\", (byte) slot)"));
        assertTrue(inventory.contains("Byte.toUnsignedInt(item.getByte(\"Slot\"))"));
        assertTrue(inventory.contains(": legacySlot++"));
        assertTrue(workbench.contains("crafting.createSlotTag()"));
        assertTrue(workbench.contains("crafting.fromSlotTag("));
    }
}
