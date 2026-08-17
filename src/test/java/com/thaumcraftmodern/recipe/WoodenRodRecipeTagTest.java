package com.thaumcraftmodern.recipe;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class WoodenRodRecipeTagTest {
    @Test
    void recipesNeverUseTheNonexistentMinecraftWoodenRodsTag()
            throws Exception {
        Path recipes = Path.of(
                "src/main/resources/data/thaumic_reborn/recipes"
        );
        try (var files = Files.walk(recipes)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json"))
                    .toList()) {
                assertFalse(
                        Files.readString(file).contains("minecraft:wooden_rods"),
                        () -> "invalid empty wooden rod tag in " + file
                );
            }
        }
    }
}
