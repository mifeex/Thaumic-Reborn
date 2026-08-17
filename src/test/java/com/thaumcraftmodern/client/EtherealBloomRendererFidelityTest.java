package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EtherealBloomRendererFidelityTest {
    private static final Path RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/render/"
                    + "EtherealBloomBlockEntityRenderer.java"
    );
    private static final Path CRYSTAL_TEXTURE = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/models/"
                    + "crystalcapacitor.png"
    );

    @Test
    void crystalTopUsesOriginalAdditiveModelCubeContract()
            throws Exception {
        String source = Files.readString(RENDERER);
        assertTrue(source.contains(
                "EtherealBloomRenderType.crystal()"
        ));
        assertTrue(source.contains(
                "0.5D - scale / 8.0F"
        ));
        assertTrue(source.contains(
                "height - scale / 6.0F"
        ));
        assertFalse(source.contains("renderCube("));

        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EtherealBloomCrystalModel.java"
        ));
        assertTrue(model.contains(".mirror()"));
        assertTrue(model.contains(
                ".addBox("
        ));
        assertTrue(model.contains(
                "PartPose.offset(8.0F, 8.0F, 8.0F)"
        ));
        assertTrue(model.contains(
                "LayerDefinition.create(mesh, 64, 32)"
        ));

        String renderType = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EtherealBloomRenderType.java"
        ));
        assertTrue(renderType.contains("LIGHTNING_TRANSPARENCY"));
        assertFalse(renderType.contains(
                ".setTransparencyState(ADDITIVE_TRANSPARENCY)"
        ));
        assertTrue(renderType.contains(".setCullState(CULL)"));
        assertTrue(renderType.contains(
                ".setWriteMaskState(COLOR_DEPTH_WRITE)"
        ));
    }

    @Test
    void crystalTextureRemainsByteExactTc4Asset() throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(CRYSTAL_TEXTURE));
        assertEquals(
                "ff75a55dd1ebc8a2cfa2b4d5a1f20604487d0a75d973a97a971563af896a2ce3",
                HexFormat.of().formatHex(digest)
        );
    }
}
