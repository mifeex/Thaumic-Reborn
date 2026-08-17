package com.thaumcraftmodern.registry;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CultistArmorCreativeInventoryTest {
    private static final List<String> ITEMS = List.of(
            "cultist_knight_helmet", "cultist_knight_chestplate",
            "cultist_knight_leggings", "cultist_cleric_hood",
            "cultist_cleric_robe", "cultist_cleric_leggings",
            "cultist_praetor_helmet", "cultist_praetor_chestplate",
            "cultist_praetor_leggings", "cultist_boots"
    );

    @Test
    void allThreeOriginalSetsHaveModelsTexturesAndCreativeEntries()
            throws Exception {
        Path assets = Path.of("src/main/resources/assets/thaumic_reborn");
        String creative = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));
        for (String item : ITEMS) {
            assertTrue(Files.isRegularFile(
                    assets.resolve("models/item/" + item + ".json")), item);
            assertTrue(Files.isRegularFile(
                    assets.resolve("textures/item/" + item + ".png")), item);
            String constant = item.toUpperCase();
            assertTrue(creative.contains("ModItems." + constant + ".get()"),
                    item);
        }
    }
}
