package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaintacleModelFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path TEXTURE = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/entity/"
                    + "models/taintacle.png"
    );

    @Test
    void usesExactClassicTextureAndSegmentGeometry() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(TEXTURE));
        assertEquals(
                "1b0b7d9e70577b834c29dda94f5b977b9296213295120c1c1e71efb4d09e0991",
                HexFormat.of().formatHex(digest)
        );

        String model = source(
                "com/thaumcraftmodern/client/render/TaintacleModel.java"
        );
        assertTrue(model.contains("NORMAL_LENGTH = 10"));
        assertTrue(model.contains("TENDRIL_LENGTH = 6"));
        assertTrue(model.contains("GIANT_LENGTH = 14"));
        assertTrue(model.contains("CHILD_SCALE = 0.88F"));
        assertTrue(model.contains(".texOffs(0, 16)"));
        assertTrue(model.contains(".texOffs(0, 32)"));
        assertTrue(model.contains(".texOffs(0, 56)"));
        assertTrue(model.contains("LayerDefinition.create(mesh, 64, 64)"));
    }

    @Test
    void dedicatedRendererReplacesHumanoidFallback() throws Exception {
        String registration = source(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        );
        assertTrue(registration.contains("entry.getKey().taintacle()"));
        assertTrue(registration.contains(
                "context -> new TaintacleRenderer(context, kind)"
        ));
        assertTrue(registration.contains(
                "TaintacleModel::createNormalLayer"
        ));
        assertTrue(registration.contains(
                "TaintacleModel::createTendrilLayer"
        ));
        assertTrue(registration.contains(
                "TaintacleModel::createGiantLayer"
        ));

        String renderer = source(
                "com/thaumcraftmodern/client/render/TaintacleRenderer.java"
        );
        assertTrue(renderer.contains("extends MobRenderer<"));
        assertTrue(renderer.contains("classicScale *= 1.33F"));
        assertTrue(renderer.contains("RenderType.eyes("));
        assertTrue(renderer.contains("LightTexture.FULL_BRIGHT"));

        String kinds = source(
                "com/thaumcraftmodern/entity/LegacyMobKind.java"
        );
        assertTrue(kinds.contains(
                "0.66F, 3.0F, \"models/taintacle.png\""
        ));
        assertTrue(kinds.contains(
                "0.22F, 1.0F, \"models/taintacle.png\""
        ));
        assertTrue(kinds.contains(
                "1.1F, 6.0F, \"models/taintacle.png\""
        ));
    }

    private static String source(String relativePath) throws Exception {
        return Files.readString(JAVA.resolve(relativePath));
    }
}
