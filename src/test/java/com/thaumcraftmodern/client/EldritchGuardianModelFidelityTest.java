package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EldritchGuardianModelFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path TEXTURE = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/entity/"
                    + "models/eldritch_guardian.png"
    );

    @Test
    void usesExactClassicTextureAndDedicatedMultipartModel() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(TEXTURE));
        assertEquals(
                "cca6fa177cdd9f533e2b5a1e51287dbf967b6f7bb470b550e3a58ea8d8a5aaad",
                HexFormat.of().formatHex(digest)
        );

        String model = source(
                "com/thaumcraftmodern/client/render/"
                        + "EldritchGuardianModel.java"
        );
        assertTrue(model.contains("LayerDefinition.create(mesh, 128, 64)"));
        assertTrue(model.contains("\"shoulder_plate_right_top\""));
        assertTrue(model.contains("\"hood_eye\""));
        assertTrue(model.contains("\"center_panel_3\""));
        assertTrue(model.contains("\"cloak_3\""));
        assertTrue(model.contains("RenderType::entityTranslucent"));
    }

    @Test
    void rendererPreservesClassicFadeAndIsExplicitlyRegistered()
            throws Exception {
        String renderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "EldritchGuardianRenderer.java"
        );
        assertTrue(renderer.contains("fullAlphaDistanceSquared = 256.0F"));
        assertTrue(renderer.contains("? 576.0F"));
        assertTrue(renderer.contains(": 1024.0F"));
        assertTrue(renderer.contains("return 0.6F"));

        String registration = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(registration.contains(
                "entry.getKey() == LegacyMobKind.ELDRITCH_GUARDIAN"
        ));
        assertTrue(registration.contains("EldritchGuardianRenderer::new"));
        assertTrue(registration.contains("EldritchWardenRenderer::new"));
        assertTrue(registration.contains(
                "EldritchGuardianModel::createBodyLayer"
        ));

        String wardenRenderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "EldritchWardenRenderer.java"
        );
        assertTrue(wardenRenderer.contains(
                "pose.scale(1.5F, 1.5F, 1.5F)"
        ));
        assertTrue(wardenRenderer.contains("ADDITIVE_TRANSPARENCY"));
        assertTrue(wardenRenderer.contains("setLightmapState(LIGHTMAP)"));
        assertTrue(wardenRenderer.contains("CLASSIC_EYE_LIGHT_BASE = 210"));
        assertTrue(wardenRenderer.contains(
                "CLASSIC_EYE_LIGHT_AMPLITUDE = 15"
        ));
        assertTrue(!wardenRenderer.contains("LightTexture.FULL_BRIGHT"));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
