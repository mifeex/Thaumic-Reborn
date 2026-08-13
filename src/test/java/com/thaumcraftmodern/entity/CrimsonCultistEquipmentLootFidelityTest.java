package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrimsonCultistEquipmentLootFidelityTest {
    @Test
    void cultistsWearRealDroppableArmorInsteadOfVisualOnlyArmor() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        ));

        for (String item : new String[]{
                "CULTIST_KNIGHT_HELMET", "CULTIST_KNIGHT_CHESTPLATE",
                "CULTIST_KNIGHT_LEGGINGS", "CULTIST_CLERIC_HOOD",
                "CULTIST_CLERIC_ROBE", "CULTIST_CLERIC_LEGGINGS",
                "CULTIST_PRAETOR_HELMET", "CULTIST_PRAETOR_CHESTPLATE",
                "CULTIST_PRAETOR_LEGGINGS", "CULTIST_BOOTS"
        }) {
            assertTrue(source.contains("ModItems." + item + ".get()"), item);
        }
        assertTrue(source.contains("setDropChance(slot, 0.085F)"));
        assertTrue(source.contains("== net.minecraft.world.Difficulty.HARD"));
        assertTrue(source.contains("? 0.3F : 0.1F"));
        assertTrue(source.contains("equipCrimsonArmor(true)"));
        assertTrue(source.contains("equipCrimsonArmor(false)"));
    }
}
