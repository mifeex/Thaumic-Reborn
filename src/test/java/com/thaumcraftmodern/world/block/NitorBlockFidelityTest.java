package com.thaumcraftmodern.world.block;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class NitorBlockFidelityTest {
    @Test
    void nitorRemainsPlaceableAndHeatsTheCrucible() throws Exception {
        String items = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModItems.java"));
        String crucible = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/"
                        + "CrucibleBlockEntity.java"));

        assertTrue(items.contains("new BlockItem(\n"
                + "                            ModBlocks.NITOR.get(),"));
        assertTrue(crucible.contains("|| state.is(ModBlocks.NITOR.get());"));
    }

    @Test
    void placedNitorKeepsTheClassicSelectableNonCollidingCore()
            throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/NitorBlock.java"));

        assertTrue(source.contains("4.8D, 4.8D, 4.8D,"));
        assertTrue(source.contains("11.2D, 11.2D, 11.2D"));
        assertTrue(source.contains("return SELECTION_SHAPE;"));
        assertTrue(source.contains("return Shapes.empty();"));
    }

    @Test
    void brokenNitorReturnsThePlaceableNitorItem() throws Exception {
        JsonObject loot = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/loot_tables/blocks/"
                        + "nitor.json"))).getAsJsonObject();

        JsonObject entry = loot.getAsJsonArray("pools")
                .get(0).getAsJsonObject()
                .getAsJsonArray("entries")
                .get(0).getAsJsonObject();
        assertEquals("minecraft:item", entry.get("type").getAsString());
        assertEquals("thaumic_reborn:nitor", entry.get("name").getAsString());
    }
}
