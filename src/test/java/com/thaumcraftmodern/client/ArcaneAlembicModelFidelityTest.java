package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneAlembicModelFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn");

    @Test
    void westFacingObjPanelIsRotatedToEachBlockFacing() throws Exception {
        JsonObject variants = JsonParser.parseString(Files.readString(
                ASSETS.resolve("blockstates/arcane_alembic.json")))
                .getAsJsonObject().getAsJsonObject("variants");

        assertEquals(90, variants.getAsJsonObject("facing=north").get("y").getAsInt());
        assertEquals(180, variants.getAsJsonObject("facing=east").get("y").getAsInt());
        assertEquals(270, variants.getAsJsonObject("facing=south").get("y").getAsInt());
        assertFalse(variants.getAsJsonObject("facing=west").has("y"));

        String obj = Files.readString(
                ASSETS.resolve("textures/models/alembic_block.obj"));
        assertTrue(obj.contains("# object Panel"));
        assertTrue(obj.contains("v 0.094000 0.347100 0.625000"));
    }

    @Test
    void originalLabelTexturesAndRendererArePresent() throws Exception {
        assertTrue(Files.size(ASSETS.resolve("textures/models/label.png")) > 0);
        assertTrue(Files.size(ASSETS.resolve("textures/item/jar_label.png")) > 0);
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ArcaneAlembicBlockEntityRenderer.java"));
        assertTrue(renderer.contains("textures/models/label.png"));
        assertTrue(renderer.contains("alembic.filterAspect()"));
        assertFalse(renderer.contains("renderEssentiaLevel"));
        assertFalse(renderer.contains("animatedglow.png"));
        assertFalse(renderer.contains("font.drawInBatch"));

        String readout = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "ClientAspectContainerReadout.java"));
        String registry = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/"
                        + "AspectContainerHudRegistry.java"));
        assertTrue(registry.contains("register(ArcaneAlembicBlockEntity.class"));
        assertTrue(registry.contains("alembic.storedAspect()"));
        assertTrue(registry.contains("alembic.storedAmount()"));
        assertTrue(registry.contains("onHitFace(hit)"));
    }
}
