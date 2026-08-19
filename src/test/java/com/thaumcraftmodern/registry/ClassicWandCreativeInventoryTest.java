package com.thaumcraftmodern.registry;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicWandCreativeInventoryTest {
    @Test
    void exposesOnlyTheFourFinishedCastingToolsFromTc4235()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        assertEquals(1, occurrences(source,
                "wand.createFilled(\"wood\", \"iron\")"));
        assertEquals(1, occurrences(source,
                "\"greatwood\", \"gold\""));
        assertEquals(2, occurrences(source,
                "\"silverwood\", \"thaumium\""));
        assertFalse(source.contains("for (var rod : catalog.rods())"));
        assertFalse(source.contains("for (var cap : catalog.caps())"));
        assertFalse(source.contains("ModItems.CODEX_WAND"));
        assertFalse(source.contains("ModItems.GREATWOOD_STAFF.get()"));
    }

    @Test
    void componentRowsMatchOriginalRodAndCapSubitems() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));
        assertFalse(source.contains("ModItems.WOODEN_WAND_ROD.get()"));
        assertTrue(source.contains("\"inert_silver_wand_cap\""));
        assertTrue(source.contains("\"inert_thaumium_wand_cap\""));
        assertTrue(source.contains("\"inert_void_wand_cap\""));
    }

    @Test
    void exposesAllBasicWandFociAndTheFocusPouch() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        assertEquals(1, occurrences(source, ".get(\"focus_fire\").get()"));
        assertEquals(1, occurrences(source, ".get(\"focus_frost\").get()"));
        assertEquals(1, occurrences(source, ".get(\"focus_shock\").get()"));
        assertEquals(1, occurrences(source,
                ".get(\"focus_excavation\").get()"));
        assertEquals(1, occurrences(source, ".get(\"focus_trade\").get()"));
        assertEquals(1, occurrences(source, ".get(\"focus_primal\").get()"));
        assertEquals(1, occurrences(source, ".get(\"focus_pouch\").get()"));
    }

    @Test
    void exposesBoneBowAndEveryPrimalArrowSubtype() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        assertEquals(1, occurrences(source, ".get(\"bone_bow\").get()"));
        for (String aspect : new String[] {
                "aer", "ignis", "aqua", "terra", "ordo", "perditio"
        }) {
            assertEquals(1, occurrences(source,
                    ".get(\"" + aspect + "_primal_arrow\").get()"));
        }
    }

    private static int occurrences(String source, String needle) {
        return (source.length() - source.replace(needle, "").length())
                / needle.length();
    }
}
