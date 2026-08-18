package com.thaumcraftmodern.registry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class OriginalCreativeInventoryFidelityTest {
    private static final Path CREATIVE_TAB = Path.of(
            "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
    );

    @Test
    void hidesModernAndTestOnlyContentFromThaumcraftTab() throws Exception {
        String creative = Files.readString(CREATIVE_TAB);

        for (String nonOriginal : List.of(
                "WINGED_MANTLE_HOOD",
                "WINGED_MANTLE_CHESTPLATE",
                "WINGED_MANTLE_LEGGINGS",
                "WINGED_MANTLE_BOOTS",
                "TAINTED_CAVE_MOSS_TEST",
                "TAINTED_CAVE_VINE_TEST",
                "TAINTED_GLOW_BERRY_VINE_TEST",
                "MATURE_SPORE_STALK",
                "FACELESS_WITNESS_SPAWN_EGG"
        )) {
            assertFalse(creative.contains("ModItems." + nonOriginal),
                    nonOriginal + " is not original Thaumcraft creative content");
        }
    }

    @Test
    void keepsUserRequestedImprovedTransportInCreativeTab()
            throws Exception {
        String creative = Files.readString(CREATIVE_TAB);

        assertTrue(creative.contains(
                "ModItems.REVERSIBLE_ESSENTIA_TUBE.get()"
        ));
        assertTrue(creative.contains(
                "ModItems.ADVANCED_ESSENTIA_BUFFER.get()"
        ));
    }

    @Test
    void matchesOriginalTaintCreativeVariants() throws Exception {
        String creative = Files.readString(CREATIVE_TAB);

        for (String original : List.of(
                "CRUSTED_TAINT",
                "TAINTED_SOIL",
                "FLESH_BLOCK",
                "TAINT_FIBRES",
                "SHORT_TAINTED_GRASS",
                "TALL_TAINTED_GRASS",
                "SPORE_STALK",
                "FLUX_GOO",
                "FLUX_GAS"
        )) {
            assertTrue(creative.contains("ModItems." + original + ".get()"),
                    "missing original TC4 taint creative entry " + original);
        }
    }

    @Test
    void keepsUsefulModernDeepslateInfusedStoneVariants() throws Exception {
        String creative = Files.readString(CREATIVE_TAB);

        for (String variant : List.of(
                "AIR",
                "FIRE",
                "WATER",
                "EARTH",
                "ORDER",
                "ENTROPY"
        )) {
            assertTrue(creative.contains(
                    "ModItems.DEEPSLATE_" + variant + "_INFUSED_STONE.get()"
            ), "missing useful deepslate " + variant.toLowerCase()
                    + " infused stone variant");
        }
    }

    @Test
    void keepsOriginalThaumcraftSpawnEggFamily() throws Exception {
        String creative = Files.readString(CREATIVE_TAB);

        assertTrue(creative.contains("ModItems.SPAWN_EGGS.get(mob).get()"),
                "TC4 registered spawn eggs for its original mobs");
        assertTrue(creative.contains("mob == LegacyMobKind.CONVERTED_VILLAGER"));
        assertTrue(creative.contains("mob == LegacyMobKind.CRIMSON_INQUISITOR"));
    }
}
