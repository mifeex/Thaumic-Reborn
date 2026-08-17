package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InhabitedZombieFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");

    @Test
    void usesOriginalTextureAndVanillaZombieModel() throws Exception {
        assertArrayEquals(
                Files.readAllBytes(Path.of(
                        "reference/Thaumcraft-4.2-FOREVA-master/src/main/"
                                + "resources/assets/thaumcraft/textures/models/"
                                + "czombie.png"
                )),
                Files.readAllBytes(Path.of(
                        "src/main/resources/assets/thaumic_reborn/textures/"
                                + "entity/models/czombie.png"
                ))
        );

        String renderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "InhabitedZombieRenderer.java"
        );
        assertTrue(renderer.contains("BrainyZombieModel"));
        assertTrue(renderer.contains("ModelLayers.ZOMBIE"));
        assertTrue(renderer.contains(
                "\"textures/entity/models/czombie.png\""
        ));
        assertTrue(renderer.contains("HumanoidArmorLayer"));
        assertTrue(renderer.contains("ModelLayers.ZOMBIE_INNER_ARMOR"));
        assertTrue(renderer.contains("ModelLayers.ZOMBIE_OUTER_ARMOR"));

        String registration = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(registration.contains(
                "entry.getKey() == LegacyMobKind.INHABITED_ZOMBIE"
        ));
        assertTrue(registration.contains("InhabitedZombieRenderer::new"));

        String entity = source(
                "com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        );
        assertTrue(entity.contains("equipInhabitedZombieArmor()"));
        assertTrue(entity.contains("ModItems.CULTIST_KNIGHT_HELMET.get()"));
        assertTrue(entity.contains("? 0.9F : 0.6F"));
        assertTrue(entity.contains("ModItems.CULTIST_KNIGHT_CHESTPLATE.get()"));
        assertTrue(entity.contains("ModItems.CULTIST_KNIGHT_LEGGINGS.get()"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
