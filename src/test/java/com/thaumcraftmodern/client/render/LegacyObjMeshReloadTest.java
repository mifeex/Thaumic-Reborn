package com.thaumcraftmodern.client.render;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class LegacyObjMeshReloadTest {
    private static final Path RENDER = Path.of(
            "src/main/java/com/thaumcraftmodern/client/render"
    );

    @Test
    void runtimeMeshesArePreparedAndAtomicallyPublishedOnReload()
            throws Exception {
        String mesh = Files.readString(RENDER.resolve("LegacyObjMesh.java"));

        assertTrue(mesh.contains("SimplePreparableReloadListener<"));
        assertTrue(mesh.contains("Map.copyOf(prepared)"));
        assertTrue(mesh.contains("loadedMeshes = prepared;"));
        assertTrue(mesh.contains("List.copyOf(faces)"));
        assertFalse(mesh.contains("Minecraft.getInstance()"));
    }

    @Test
    void noRuntimeRendererParsesObjOnItsRenderPath() throws Exception {
        for (String renderer : new String[]{
                "VisRelayBlockEntityRenderer.java",
                "NodeDeviceBlockEntityRenderer.java",
                "InfusionPillarBlockEntityRenderer.java",
                "AdvancedAlchemicalFurnaceBlockEntityRenderer.java"
        }) {
            String source = Files.readString(RENDER.resolve(renderer));
            assertFalse(source.contains("LegacyObjMesh.load("), renderer);
            assertTrue(source.contains("LegacyObjMesh.get("), renderer);
        }
    }
}
