package com.thaumcraftmodern.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WandFocusTextureFidelityTest {
    private static final Path RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/render/"
                    + "ClassicWandItemRenderer.java"
    );
    private static final Path ORIGINAL_TEXTURE = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                    + "assets/thaumcraft/textures/models/wand.png"
    );
    private static final Path PORT_TEXTURE = Path.of(
            "src/main/resources/assets/thaumcraftmodern/textures/models/wand.png"
    );

    @Test
    void focusCubeUsesTheOriginalTexturedTc4Material() throws Exception {
        String renderer = Files.readString(RENDERER);

        assertTrue(renderer.contains(
                "ThaumcraftModern.MOD_ID, \"textures/models/wand.png\""
        ));
        assertFalse(renderer.contains("white_concrete.png"));
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL_TEXTURE),
                Files.readAllBytes(PORT_TEXTURE)
        );
    }
}
