package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VisExhaustClientExtensionsTest {
    private static final Path SOURCE = Path.of(
            "src/main/java/com/thaumcraftmodern/client/"
                    + "VisExhaustClientExtensions.java"
    );

    @Test
    void inventoryIconHasAnIndependentVerticalTuningConstant()
            throws Exception {
        assertEquals(14, VisExhaustClientExtensions.INVENTORY_ICON_Y_OFFSET);
        String source = Files.readString(SOURCE);
        assertTrue(source.contains(
                "y + INVENTORY_ICON_Y_OFFSET"
        ));
        assertTrue(source.contains(
                "public static final int INVENTORY_ICON_Y_OFFSET"
        ));
    }
}
