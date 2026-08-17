package com.thaumcraftmodern.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class NetherMobSpawnResourceTest {
    private static final Path RESOURCE = Path.of(
            "src/main/resources/data/thaumic_reborn/forge/biome_modifier/"
                    + "add_nether_mobs.json"
    );

    @Test
    void netherWispIsRareWithoutChangingItsGroupSize() throws Exception {
        JsonObject modifier = JsonParser.parseString(
                Files.readString(RESOURCE)
        ).getAsJsonObject();
        assertEquals("#minecraft:is_nether",
                modifier.get("biomes").getAsString());

        JsonObject wisp = spawn(modifier.getAsJsonArray("spawners"),
                "thaumic_reborn:wisp");
        assertEquals(1, wisp.get("weight").getAsInt());
        assertEquals(1, wisp.get("minCount").getAsInt());
        assertEquals(1, wisp.get("maxCount").getAsInt());

        JsonObject firebat = spawn(modifier.getAsJsonArray("spawners"),
                "thaumic_reborn:firebat");
        assertEquals(10, firebat.get("weight").getAsInt());
    }

    private static JsonObject spawn(JsonArray spawners, String type) {
        for (var element : spawners) {
            JsonObject spawn = element.getAsJsonObject();
            if (type.equals(spawn.get("type").getAsString())) {
                return spawn;
            }
        }
        return fail("Missing Nether spawn entry for " + type);
    }
}
