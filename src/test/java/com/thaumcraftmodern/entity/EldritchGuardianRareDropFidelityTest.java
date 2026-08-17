package com.thaumcraftmodern.entity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class EldritchGuardianRareDropFidelityTest {
    @Test
    void eldritchEyeUsesClassicRareDropChanceAndLootingIncrease()
            throws Exception {
        JsonObject root = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/loot_tables/entities/"
                        + "legacy/eldritch_guardian.json"
        ))).getAsJsonObject();
        JsonObject condition = root.getAsJsonArray("pools").get(0)
                .getAsJsonObject().getAsJsonArray("entries").get(0)
                .getAsJsonObject().getAsJsonArray("conditions").get(0)
                .getAsJsonObject();

        assertEquals("minecraft:random_chance_with_looting",
                condition.get("condition").getAsString());
        assertEquals(0.025D, condition.get("chance").getAsDouble());
        assertEquals(0.005D,
                condition.get("looting_multiplier").getAsDouble());
    }
}
