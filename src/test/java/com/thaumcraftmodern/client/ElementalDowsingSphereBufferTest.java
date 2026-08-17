package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementalDowsingSphereBufferTest {
    @Test
    void sphereCompletesPositionColorVerticesWithoutCrashing() {
        BufferBuilder builder = new BufferBuilder(65_536);
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        ElementalDowsingClient.renderMarker(new PoseStack().last(), builder,
                ElementalDowsingClient.ORE_MARKER_RADIUS);
        BufferBuilder.RenderedBuffer rendered = builder.end();
        try {
            assertFalse(rendered.isEmpty());
        } finally {
            rendered.release();
        }
    }

    @Test
    void markerIsAWorldPointRatherThanABlockSizedOverlay() {
        assertTrue(ElementalDowsingClient.ORE_MARKER_RADIUS >= 0.49F);
        assertTrue(ElementalDowsingClient.ORE_MARKER_RADIUS <= 0.51F);
    }

    @Test
    void haloUsesTc4WispAtlasAnimationWithoutParticleEmission() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ElementalDowsingClient.java"
        ));
        assertTrue(source.contains("WISP_ATLAS_FRAMES = 16"));
        assertTrue(source.contains("getGameTime()"));
        assertTrue(source.contains("markers(animationFrame)"));
        assertFalse(source.contains("DowsingWisp"));
        assertFalse(source.contains("addParticle"));
    }

    @Test
    void eachAnimationFrameIsOneExactCellOfTheOriginalAtlas()
            throws Exception {
        BufferedImage atlas = ImageIO.read(Path.of(
                "src/main/resources/assets/thaumic_reborn/textures/"
                        + "entity/misc/wisp.png").toFile());
        for (int frame = 0; frame < 16; frame++) {
            BufferedImage image = ImageIO.read(Path.of(
                    "src/main/resources/assets/thaumic_reborn/textures/"
                            + "entity/misc/wisp_frames/wisp_%02d.png"
                            .formatted(frame)).toFile());
            assertEquals(64, image.getWidth());
            assertEquals(64, image.getHeight());
            int sourceX = frame % 4 * 64;
            int sourceY = frame / 4 * 64;
            for (int y = 0; y < 64; y++) {
                for (int x = 0; x < 64; x++) {
                    assertEquals(
                            atlas.getRGB(sourceX + x, sourceY + y),
                            image.getRGB(x, y)
                    );
                }
            }
        }
    }

    @Test
    void revealUsesWorldCoordinatesAndNoHudProjection() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ElementalDowsingClient.java"
        ));

        assertTrue(source.contains("RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES"));
        assertTrue(source.contains("orePosition.getX() + 0.5D"));
        assertTrue(source.contains("cameraOrientation()"));
        assertFalse(source.contains("DowsingWisp"));
        assertFalse(source.contains("tickWisps"));
        assertFalse(source.contains("gravity"));
        assertTrue(source.contains("ElementalOreSphereRenderType"));
        assertFalse(source.contains("GuiGraphics"));

        String renderType = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ElementalOreSphereRenderType.java"));
        assertTrue(renderType.contains("wisp_frames/wisp_%02d.png"));
        assertTrue(renderType.contains("ADDITIVE_TRANSPARENCY"));
        assertTrue(renderType.contains("NO_DEPTH_TEST"));
        assertFalse(renderType.contains("ClassicNodeRenderTypes.node"));
    }
}
