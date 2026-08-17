package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrainyZombieFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");

    @Test
    void dedicatedRendererUsesExactClassicBrainyTexture() throws Exception {
        Path texture = Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/entity/"
                        + "models/bzombie.png"
        );
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(texture));
        assertEquals(
                "802e3a399b6bd9466fb2282605a081635dd8e4fe2328604ee53ee94c90ac92c7",
                HexFormat.of().formatHex(digest)
        );

        String renderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "BrainyZombieRenderer.java"
        );
        assertTrue(renderer.contains("BrainyZombieModel"));
        assertTrue(renderer.contains("ModelLayers.ZOMBIE"));
        assertTrue(renderer.contains("\"textures/entity/models/bzombie.png\""));

        String model = source(
                "com/thaumcraftmodern/client/render/BrainyZombieModel.java"
        );
        assertTrue(model.contains(
                "extends AbstractZombieModel<LegacyThaumcraftMob>"
        ));
        assertTrue(renderer.contains(
                "entity.kind() == LegacyMobKind.FURIOUS_ZOMBIE"
        ));
        assertTrue(renderer.contains("float anger = entity.furiousAnger()"));
        assertTrue(renderer.contains(
                "pose.scale(anger, anger, anger)"
        ));
        assertFalse(model.contains(
                "renderToBuffer("
        ));
    }

    @Test
    void angryAndFuriousStayDistinctAndAvoidHumanoidFallback()
            throws Exception {
        String registration = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(registration.contains(
                "entry.getKey() == LegacyMobKind.ANGRY_ZOMBIE"
        ));
        assertTrue(registration.contains(
                "entry.getKey() == LegacyMobKind.FURIOUS_ZOMBIE"
        ));
        assertTrue(registration.contains("BrainyZombieRenderer::new"));

        String mappings = source(
                "com/thaumcraftmodern/data/LegacyScanMappings.java"
        );
        assertTrue(mappings.contains(
                "\"Thaumcraft.BrainyZombie\", "
                        + "\"thaumic_reborn:angry_zombie\""
        ));
        assertTrue(mappings.contains(
                "\"Thaumcraft.GiantBrainyZombie\", "
                        + "\"thaumic_reborn:furious_zombie\""
        ));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
