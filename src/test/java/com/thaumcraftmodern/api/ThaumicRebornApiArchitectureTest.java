package com.thaumcraftmodern.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ThaumicRebornApiArchitectureTest {
    @Test
    void publicApiComesFromVersionedGitHubReleaseAndIsBundledByTheMod()
            throws IOException {
        String mainBuild = Files.readString(Path.of("build.gradle"));
        String properties = Files.readString(Path.of("gradle.properties"));
        assertFalse(mainBuild.contains("srcDir 'thaumic-reborn-api"));
        assertTrue(mainBuild.contains(
                "github.com/mifeex/thaumic-reborn-api/releases/download"));
        assertTrue(mainBuild.contains(
                "com.thaumicreborn:thaumic-reborn-api:${thaumic_reborn_api_version}"));
        assertTrue(mainBuild.contains("include 'com/thaumicreborn/api/**'"));
        assertTrue(mainBuild.contains("verifyBundledThaumicRebornApi"));
        assertTrue(properties.contains("thaumic_reborn_api_version=1.1.0"));
    }

    @Test
    void runtimeBridgeStaysOnTheCommonSide() throws IOException {
        String bridge = Files.readString(Path.of(
                "src", "main", "java", "com", "thaumcraftmodern",
                "integration", "api", "ThaumicRebornApiServices.java"
        ));
        assertFalse(bridge.contains("net.minecraft.client"));
        assertFalse(bridge.contains("com.thaumcraftmodern.client"));
    }

    @Test
    void clientBridgeIsIsolatedFromTheCommonEntrypoint() throws IOException {
        String common = Files.readString(Path.of(
                "src", "main", "java", "com", "thaumcraftmodern",
                "ThaumcraftModern.java"));
        assertFalse(common.contains("ThaumicRebornClientApi"));
        assertTrue(Files.exists(Path.of(
                "src", "main", "java", "com", "thaumcraftmodern",
                "integration", "api", "ThaumicRebornClientApiServices.java")));
    }

    @Test
    void v11ContractsReachRuntimeSystemsAndExampleData() throws IOException {
        String connections = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/essentia/EssentiaConnections.java"));
        assertTrue(connections.contains(
                "com.thaumicreborn.api.essentia.EssentiaTransport"));

        String focus = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/focus/WandFocusService.java"));
        assertTrue(focus.contains("instanceof FocusItem"));
        assertTrue(focus.contains("AddonFocusRegistry"));

        String revealing = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/RevealingGear.java"));
        assertTrue(revealing.contains(
                "com.thaumicreborn.api.equipment.RevealingGear"));

    }
}
