package com.thaumcraftmodern.scan;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SharedScanKnowledgeUsageTest {
    private static final Path MAIN = Path.of("src/main/java/com/thaumcraftmodern");

    @Test
    void allThaumometerSurfacesUseResolvedKnowledgeKeys() throws Exception {
        for (String relative : new String[]{
                "client/ClientThaumometerTarget.java",
                "client/InventoryThaumometerEvents.java"
        }) {
            String source = Files.readString(MAIN.resolve(relative));
            assertTrue(source.contains("identity.knowledgeKey()"), relative);
        }

        String thaumometer = Files.readString(MAIN.resolve(
                "item/ThaumometerItem.java"));
        assertTrue(thaumometer.contains("identity.knowledgeKey()"));
        assertTrue(thaumometer.contains("ScanRegistry.knowledgeKey("));

        String inventory = Files.readString(MAIN.resolve(
                "client/InventoryThaumometerEvents.java"));
        assertFalse(inventory.contains("ScanRegistry.scanKey("));
    }
}
