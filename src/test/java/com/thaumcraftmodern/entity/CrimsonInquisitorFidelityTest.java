package com.thaumcraftmodern.entity;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrimsonInquisitorFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void reusesTheOriginalKnightArmorAndCarriesAnIronAxe() throws Exception {
        String kind = source("com/thaumcraftmodern/entity/LegacyMobKind.java");
        assertTrue(kind.contains("CRIMSON_INQUISITOR"));
        assertTrue(kind.contains("models/cultist.png"));

        String entity = source(
                "com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        );
        assertTrue(entity.contains("new ItemStack(Items.IRON_AXE)"));
        assertTrue(entity.contains("player.disableShield(true)"));
        assertTrue(entity.contains("player.getUseItem().is(Items.SHIELD)"));

        String armor = source(
                "com/thaumcraftmodern/client/render/CrimsonCultArmorLayer.java"
        );
        assertTrue(armor.contains("CRIMSON_INQUISITOR"));
        assertTrue(armor.contains("texture(\"inquisitor_plate_armor.png\")"));
        assertTrue(Files.isRegularFile(ASSETS.resolve(
                "textures/entity/models/inquisitor_plate_armor.png"
        )));
    }

    @Test
    void entityEggAndTranslationsArePresent() throws Exception {
        assertTrue(source("com/thaumcraftmodern/registry/ModEntities.java")
                .contains("CRIMSON_INQUISITOR = mob"));
        assertTrue(Files.isRegularFile(ASSETS.resolve(
                "models/item/crimson_inquisitor_spawn_egg.json"
        )));
        assertTrue(Files.readString(ASSETS.resolve("lang/en_us.json"))
                .contains("Crimson Inquisitor"));
        assertTrue(Files.readString(ASSETS.resolve("lang/ru_ru.json"))
                .contains("Инквизитор Багряных"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
