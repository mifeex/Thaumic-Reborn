package com.thaumcraftmodern.registry;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicJarSoundFidelityTest {
    @Test
    void everyClassicBlockJarVariantUsesTheOriginalJarSound() throws Exception {
        String blocks = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java"
        ));
        assertEquals(4, occurrences(blocks, ".sound(ClassicJarSoundType.INSTANCE)"));

        String soundType = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/ClassicJarSoundType.java"
        ));
        assertTrue(soundType.contains("getBreakSound()"));
        assertTrue(soundType.contains("getPlaceSound()"));
        assertEquals(2, occurrences(soundType, "return jar();"));
        assertTrue(soundType.contains("return ModSounds.JAR.get();"));
    }

    private static int occurrences(String value, String needle) {
        return (value.length() - value.replace(needle, "").length())
                / needle.length();
    }
}
