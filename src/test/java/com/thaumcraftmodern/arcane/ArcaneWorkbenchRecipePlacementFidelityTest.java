package com.thaumcraftmodern.arcane;

import net.minecraft.world.item.crafting.ShapedRecipe;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneWorkbenchRecipePlacementFidelityTest {
    @Test
    void arcaneShapedRecipesExposeVanillaPlacementContract()
            throws Exception {
        assertTrue(ShapedRecipe.class.isAssignableFrom(ArcaneShapedRecipe.class));

        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/arcane/"
                        + "ArcaneShapedRecipe.java"
        ));
        assertTrue(source.contains("extends ShapedRecipe implements ArcaneRecipe"));
        assertTrue(source.contains("CraftingBookCategory.MISC"));
    }
}
