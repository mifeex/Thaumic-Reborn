package com.thaumcraftmodern.client.screen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumonomiconTransformationRecipeLayoutTest {
    @Test
    void usesOriginalTc4SmeltingArrowRegionAndItemPositions() {
        assertEquals(112, ThaumonomiconTransformationRecipeLayout.WIDTH);
        assertEquals(
                384,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_SOURCE_Y
        );
        assertEquals(
                128,
                ThaumonomiconTransformationRecipeLayout.OVERLAY_HEIGHT
        );
        assertEquals(48, ThaumonomiconTransformationRecipeLayout.INPUT_X);
        assertEquals(64, ThaumonomiconTransformationRecipeLayout.INPUT_Y);
        assertEquals(48, ThaumonomiconTransformationRecipeLayout.OUTPUT_X);
        assertEquals(144, ThaumonomiconTransformationRecipeLayout.OUTPUT_Y);
        assertEquals(
                20,
                ThaumonomiconTransformationRecipeLayout.left(10, 132)
        );
    }

    @Test
    void salisMundusPageRoutesThroughTheSmeltingTransformation()
            throws Exception {
        try (InputStream stream = getClass().getResourceAsStream(
                "/data/thaumcraftmodern/recipes/salis_mundus.json"
        )) {
            assertNotNull(stream);
            JsonObject recipe = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonObject();
            assertEquals(
                    "minecraft:smelting",
                    recipe.get("type").getAsString()
            );
            assertEquals(
                    "thaumcraftmodern:salis_mundus",
                    recipe.get("result").getAsString()
            );
        }

        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/"
                        + "ThaumonomiconScreen.java"
        )) + Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/screen/"
                        + "ThaumonomiconPageRenderer.java"
        ));
        assertTrue(source.contains(
                "if (!(recipe instanceof ArcaneRecipe)\n"
                        + "                "
                        + "&& !(recipe instanceof CraftingRecipe))"
        ));
        assertTrue(source.contains(
                "ThaumonomiconTransformationRecipeLayout.OVERLAY_SOURCE_Y"
        ));
        assertTrue(source.contains(
                "Component.translatable(\"recipe.type.smelting\")"
        ));
    }
}
