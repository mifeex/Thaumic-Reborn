package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumicSlimeRendererFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path TEXTURE = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/entity/"
                    + "models/tslime.png"
    );

    @Test
    void usesExactTc4TextureAndClassicTwoLayerSlimeModel() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(TEXTURE));
        assertEquals(
                "db074675d6230ec6b820888800a9c2a28c2d60f50375921509fb6b5c4b250893",
                HexFormat.of().formatHex(digest)
        );

        String renderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "ThaumicSlimeRenderer.java"
        );
        assertTrue(renderer.contains("ModelLayers.SLIME"));
        assertTrue(renderer.contains("new SlimeOuterLayer<>("));
        assertTrue(renderer.contains("CLASSIC_SHADOW_RADIUS = 0.25F"));
    }

    @Test
    void thaumicSlimeDoesNotFallBackToHumanoidRenderer() throws Exception {
        String registration = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(registration.contains(
                "entry.getKey() == LegacyMobKind.THAUMIC_SLIME"
        ));
        assertTrue(registration.contains("ThaumicSlimeRenderer::new"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
