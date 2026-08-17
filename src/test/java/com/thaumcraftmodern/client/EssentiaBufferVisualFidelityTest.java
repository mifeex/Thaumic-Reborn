package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaBufferVisualFidelityTest {
    @Test
    void connectedBufferSidesUseClassicDynamicConduitArms() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EssentiaBufferBlockEntityRenderer.java"
        ));
        String registration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/ClientModEvents.java"
        ));
        String hud = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "AspectContainerHudRegistry.java"
        ));
        JsonObject model = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/thaumic_reborn/models/block/"
                        + "essentia_buffer.json"
        ))).getAsJsonObject();

        assertTrue(renderer.contains("for (Direction side : Direction.values())"));
        assertTrue(renderer.contains("buffer.sideOpen(side)"));
        assertTrue(renderer.contains("EssentiaConnections.neighbour("));
        assertTrue(renderer.contains("BODY_MIN = 4.0F / 16.0F"));
        assertTrue(renderer.contains("BODY_MAX = 12.0F / 16.0F"));
        assertTrue(renderer.contains("ARM_MIN = 7.0F / 16.0F"));
        assertTrue(renderer.contains("ARM_MAX = 9.0F / 16.0F"));
        assertTrue(registration.contains("ModBlockEntities.ESSENTIA_BUFFER.get()"));
        assertTrue(registration.contains("EssentiaBufferBlockEntityRenderer::new"));
        assertTrue(hud.contains("register(AdvancedEssentiaBufferBlockEntity.class"));
        assertTrue(hud.contains("buffer.contents()"));

        assertEquals(1, model.getAsJsonArray("elements").size());
        JsonObject body = model.getAsJsonArray("elements").get(0).getAsJsonObject();
        assertEquals("[4,4,4]", body.getAsJsonArray("from").toString());
        assertEquals("[12,12,12]", body.getAsJsonArray("to").toString());
        assertFalse(model.toString().contains("[7,0,7]"));
    }
}
