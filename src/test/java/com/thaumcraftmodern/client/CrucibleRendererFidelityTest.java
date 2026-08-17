package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrucibleRendererFidelityTest {
    private static final Path RENDERER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/render/"
                    + "CrucibleBlockEntityRenderer.java"
    );

    @Test
    void filledStateDoesNotAddASecondStaticWaterSurface() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(
                "/assets/thaumic_reborn/blockstates/crucible.json"
        )) {
            assertNotNull(stream);
            JsonObject root = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();
            JsonObject variants = root.getAsJsonObject("variants");
            assertNotNull(variants);
            assertEquals(
                    "thaumic_reborn:block/crucible",
                    variants.getAsJsonObject("filled=false")
                            .get("model")
                            .getAsString()
            );
            assertEquals(
                    "thaumic_reborn:block/crucible",
                    variants.getAsJsonObject("filled=true")
                            .get("model")
                            .getAsString()
            );
            assertFalse(root.has("multipart"));
        }
    }

    @Test
    void dynamicSurfaceKeepsOriginalFullBlockCoverageAndUvDirection()
            throws Exception {
        String source = Files.readString(RENDERER);
        assertTrue(source.contains("WATER_SURFACE_MIN_X = 0.0F"));
        assertTrue(source.contains("WATER_SURFACE_MAX_X = 1.0F"));
        assertTrue(source.contains("WATER_SURFACE_MIN_Z = 0.0F"));
        assertTrue(source.contains("WATER_SURFACE_MAX_Z = 1.0F"));
        assertTrue(source.contains("WATER_SURFACE_Y_OFFSET = 0.0F"));
        assertTrue(source.contains(
                "WATER_SURFACE_MIN_X,\n"
                        + "                height,\n"
                        + "                WATER_SURFACE_MAX_Z,\n"
                        + "                1.0F,\n"
                        + "                1.0F"
        ));
        assertTrue(source.contains(
                "WATER_SURFACE_MAX_X,\n"
                        + "                height,\n"
                        + "                WATER_SURFACE_MIN_Z,\n"
                        + "                0.0F,\n"
                        + "                0.0F"
        ));
        assertFalse(source.contains("water.getU0()"));
        assertFalse(source.contains("water.getV0()"));
    }

    @Test
    void innerBowlUsesTheFullOriginalTc4Textures() throws Exception {
        String model = Files.readString(Path.of(
                "src/main/resources/assets/thaumic_reborn/models/block/"
                        + "crucible.json"
        ));
        assertTrue(model.contains(
                "\"inner\": \"thaumic_reborn:block/crucible5\""
        ));
        assertTrue(model.contains(
                "\"inner_bottom\": \"thaumic_reborn:block/crucible6\""
        ));
        assertEquals(4, count(model, "\"texture\": \"#inner\""));
        assertEquals(2, count(model, "\"texture\": \"#inner_bottom\""));
        assertFalse(model.contains(
                "\"texture\": \"#inner\", \"uv\""
        ));
        assertFalse(model.contains(
                "\"texture\": \"#inner_bottom\", \"uv\""
        ));
    }

    @Test
    void modernWaterTintFeedsTheOriginalTc4EssentiaModifier()
            throws Exception {
        String source = Files.readString(RENDERER);
        assertTrue(source.contains(
                "BiomeColors.getAverageWaterColor("
        ));
        assertTrue(source.contains(
                "CrucibleFluidPresentation.color("
        ));
        assertTrue(source.contains(
                "crucible.essentiaAmount()"
        ));
    }

    private static int count(String source, String needle) {
        int matches = 0;
        int offset = 0;
        while ((offset = source.indexOf(needle, offset)) >= 0) {
            matches++;
            offset += needle.length();
        }
        return matches;
    }
}
