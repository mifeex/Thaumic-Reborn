package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;

final class OptiFineResourceLocationCompatibilityTest {
    @Test
    void runtimeCodeAvoidsResourceLocationBackportsRemovedByOldOptiFine()
            throws Exception {
        try (var sources = Files.walk(Path.of("src/main/java"))) {
            for (Path source : sources
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                String code = Files.readString(source);
                for (String incompatible : new String[]{
                        "ResourceLocation.fromNamespaceAndPath(",
                        "ResourceLocation.withDefaultNamespace(",
                        "ResourceLocation.parse("
                }) {
                    assertFalse(code.contains(incompatible),
                            source + " uses " + incompatible);
                }
            }
        }
    }
}
