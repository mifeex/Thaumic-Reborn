package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WispZapRendererFidelityTest {
    private static final Path RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/render/"
                    + "ClientWispZapRenderer.java"
    );
    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/misc"
    );

    @Test
    void usesExactTc4LightningTextures() throws Exception {
        assertDigest(
                "p_large.png",
                "1d93bbf9edc18ceedb24a1df26922b2ad339d67a963e3a11d29df705a9ca8188"
        );
        assertDigest(
                "p_small.png",
                "bd33eac16c2c26b56372748f706a3327262b32ae9b3be110c90a784da2392a3e"
        );
    }

    @Test
    void rendersShortLivedTexturedFullBrightBolt() throws Exception {
        String source = Files.readString(RENDERER);
        assertTrue(source.contains("LIFETIME_TICKS = 4"));
        assertTrue(source.contains("SEGMENTS = 16"));
        assertTrue(source.contains("entityTranslucentEmissive(LARGE)"));
        assertTrue(source.contains("entityTranslucentEmissive(SMALL)"));
        assertTrue(source.contains("LightTexture.FULL_BRIGHT"));
    }

    private static void assertDigest(
            String filename,
            String expected
    ) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(TEXTURES.resolve(filename)));
        assertEquals(expected, HexFormat.of().formatHex(digest));
    }
}
