package com.thaumcraftmodern.client;

import com.thaumcraftmodern.entity.LegacyMobKind;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicSmallMobRendererFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");
    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/entity"
    );

    @Test
    void wispUsesOneAtlasFrameAndDominantAspectColor() throws Exception {
        assertTextureHash(
                "misc/wisp.png",
                "d211675284b73d8a380335fff34794a500b0401924cd91fa053f9eaeaa35f0ed"
        );
        String renderer = source("WispRenderer.java");
        assertTrue(renderer.contains("ATLAS_COLUMNS = 4"));
        assertTrue(renderer.contains("ATLAS_FRAMES = 16"));
        assertTrue(renderer.contains(
                "AspectRegistryRuntime.find(entity.wispAspect())"
        ));
        assertTrue(renderer.contains("LightTexture.FULL_BRIGHT"));

        String renderType = source("WispRenderType.java");
        assertTrue(renderType.contains("ADDITIVE_TRANSPARENCY"));
        assertTrue(renderType.contains("COLOR_WRITE"));
    }

    @Test
    void firebatUsesClassicMultipartModelAndParticles() throws Exception {
        assertTextureHash(
                "models/firebat.png",
                "433bbaa4c294de8eb1bdacfbca5022241a5002622fa09cafac6655b16a15f8ec"
        );
        String model = source("FireBatModel.java");
        assertTrue(model.contains("extends HierarchicalModel"));
        assertTrue(model.contains("\"outer_right_wing\""));
        assertTrue(model.contains("Mth.cos(ageInTicks * 1.3F)"));

        String renderer = source("FireBatRenderer.java");
        assertTrue(renderer.contains("pose.scale(0.35F, 0.35F, 0.35F)"));

        String entity = Files.readString(JAVA.resolve(
                "com/thaumcraftmodern/entity/LegacyThaumcraftMob.java"
        ));
        assertTrue(entity.contains("ParticleTypes.SMOKE"));
        assertTrue(entity.contains("ParticleTypes.FLAME"));
    }

    @Test
    void mindSpiderUsesTinyVanillaSpiderGeometryAndClassicSkin()
            throws Exception {
        assertEquals(0.3F, LegacyMobKind.MIND_SPIDER.width());
        assertEquals(0.3F, LegacyMobKind.MIND_SPIDER.height());
        assertTextureHash(
                "models/taint_spider.png",
                "900311dfe5e143c8e0e3f2f6700ce081d303fb9235cec945bb575ca92859272c"
        );
        assertTextureHash(
                "models/taint_spider_eyes.png",
                "29aab356cb087b21bb43aae60a301325bc651e8117acef3b38d36aae2691be58"
        );

        String model = source("MindSpiderModel.java");
        assertTrue(model.contains(
                "extends SpiderModel<LegacyThaumcraftMob>"
        ));
        String renderer = source("MindSpiderRenderer.java");
        assertTrue(renderer.contains("ModelLayers.SPIDER"));
        assertTrue(renderer.contains("pose.scale(0.3F, 0.3F, 0.3F)"));
        assertTrue(renderer.contains("Math.min("));
        assertTrue(renderer.contains("0.1F"));
    }

    @Test
    void allThreeKindsBypassGenericFallbackRenderers() throws Exception {
        String registration = Files.readString(JAVA.resolve(
                "com/thaumcraftmodern/client/WorldContentClientEvents.java"
        ));
        assertTrue(registration.contains("WispRenderer::new"));
        assertTrue(registration.contains("FireBatRenderer::new"));
        assertTrue(registration.contains("MindSpiderRenderer::new"));
    }

    private static String source(String name) throws Exception {
        return Files.readString(JAVA.resolve(
                "com/thaumcraftmodern/client/render/" + name
        ));
    }

    private static void assertTextureHash(
            String relativePath,
            String expected
    ) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(TEXTURES.resolve(relativePath)));
        assertEquals(expected, HexFormat.of().formatHex(digest));
    }
}
