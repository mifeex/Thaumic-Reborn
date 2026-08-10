package com.thaumcraftmodern.world.menu;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ResearchTableClassicAspectOrderTest {
    @Test
    void paletteUsesTc4AspectListAlphabeticalTagOrder() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/menu/ResearchTableMenu.java"
        ));

        assertTrue(source.contains(
                ".map(com.thaumcraftmodern.aspect.AspectDefinition::id)\n"
                        + "                .sorted()\n"
                        + "                .toList()"
        ));
    }
}
