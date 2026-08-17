package com.thaumcraftmodern.world.block;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcanePedestalShapeTest {
    @Test
    void outlineCoversEveryVisibleModelSection() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/ArcanePedestalBlock.java"
        ));
        JsonObject model = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/thaumic_reborn/models/block/arcane_pedestal.json"
        ))).getAsJsonObject();
        JsonArray elements = model.getAsJsonArray("elements");

        assertEquals(3, elements.size());
        assertTrue(source.contains("box(0, 0, 0, 16, 4, 16)"));
        assertTrue(source.contains("box(4, 4, 4, 12, 12, 12)"));
        assertTrue(source.contains("box(2, 12, 2, 14, 16, 14)"));
        for (JsonObject element : elements.asList().stream()
                .map(raw -> raw.getAsJsonObject()).toList()) {
            JsonArray from = element.getAsJsonArray("from");
            JsonArray to = element.getAsJsonArray("to");
            String expected = "box(" + from.get(0).getAsInt() + ", "
                    + from.get(1).getAsInt() + ", " + from.get(2).getAsInt()
                    + ", " + to.get(0).getAsInt() + ", "
                    + to.get(1).getAsInt() + ", " + to.get(2).getAsInt() + ")";
            assertTrue(source.contains(expected), expected);
        }
    }
}
