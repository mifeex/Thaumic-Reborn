package com.thaumcraftmodern.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class ArmorItemDedicatedServerClassloadingTest {
    @Test
    void commonArmorItemsDoNotEmbedClientModelImplementations() throws Exception {
        String fortress = source("item/FortressArmorItem.java");
        String cultist = source("item/CultistArmorItem.java");

        assertFalse(fortress.contains("net.minecraft.client"));
        assertFalse(fortress.contains("FortressArmorModel"));
        assertTrue(fortress.contains("FortressArmorClientExtensions.create()"));
        assertFalse(cultist.contains("net.minecraft.client"));
        assertFalse(cultist.contains("CrimsonCultArmorModel"));
        assertTrue(cultist.contains("CultistArmorClientExtensions.create(() -> set)"));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(Path.of("src/main/java/com/thaumcraftmodern/" + relative));
    }
}
