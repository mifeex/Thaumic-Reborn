package com.thaumcraftmodern.client.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class ClassicSceptreGuiOrientationTest {
    @Test
    void sceptreUsesTheSameGuiRotationAsClassicWands() throws Exception {
        JsonObject model = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/thaumcraftmodern/models/item/"
                        + "classic_sceptre_base.json"
        ))).getAsJsonObject();
        JsonArray rotation = model.getAsJsonObject("display")
                .getAsJsonObject("gui")
                .getAsJsonArray("rotation");

        assertEquals(0, rotation.get(0).getAsInt());
        assertEquals(0, rotation.get(1).getAsInt());
        assertEquals(0, rotation.get(2).getAsInt());
    }
}
