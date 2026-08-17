package com.thaumcraftmodern.world;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PavingStoneOfTravelResourceTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void modelAndTextureAreTheOriginalTc4Cube() throws Exception {
        JsonObject blockModel = json(ASSETS.resolve(
                "models/block/paving_stone_of_travel.json"
        ));
        assertEquals("minecraft:block/cube_all",
                blockModel.get("parent").getAsString());
        assertEquals("thaumic_reborn:block/paving_stone_of_travel",
                blockModel.getAsJsonObject("textures")
                        .get("all").getAsString());
        assertEquals(
                "f3c890dce70c59cf76b5d0881d90e6f844cd784b",
                sha1(ASSETS.resolve(
                        "textures/block/paving_stone_of_travel.png"
                ))
        );
        assertEquals("thaumic_reborn:block/paving_stone_of_travel",
                json(ASSETS.resolve(
                        "models/item/paving_stone_of_travel.json"
                )).get("parent").getAsString());
        for (int frame = 0; frame < 9; frame++) {
            assertTrue(Files.isRegularFile(ASSETS.resolve(
                    "textures/particle/travel_sparkle_" + frame + ".png"
            )));
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String sha1(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                .digest(Files.readAllBytes(path)));
    }
}
