package com.thaumcraftmodern.crucible;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class CrucibleRecipeRegistryIndexTest {
    @Test
    void replacementBuildsAnImmutableIdIndexAndRevision() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/crucible/"
                        + "CrucibleRecipeRegistry.java"
        ));

        assertTrue(source.contains("recipesById.putIfAbsent(recipe.id(), recipe)"));
        assertTrue(source.contains("Collections.unmodifiableMap(recipesById)"));
        assertTrue(source.contains("snapshot.recipesById().get(id)"));
        assertTrue(source.contains("previous.revision() + 1L"));
    }
}
