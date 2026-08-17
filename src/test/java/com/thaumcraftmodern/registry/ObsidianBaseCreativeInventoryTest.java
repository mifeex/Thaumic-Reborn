package com.thaumcraftmodern.registry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ObsidianBaseCreativeInventoryTest {
    @Test
    void obsidianBaseHasBlockItemAndCreativeTabEntry() throws Exception {
        String items = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModItems.java"
        ));
        String creativeTab = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        ));

        assertTrue(items.contains(
                "blockItem(\"obsidian_tile\", ModBlocks.OBSIDIAN_TILE)"
        ));
        assertTrue(creativeTab.contains(
                "output.accept(ModItems.OBSIDIAN_TILE.get())"
        ));
    }

    @Test
    void obsidianBaseDropsItselfWithAnObsidianGradePickaxe() throws Exception {
        String blocks = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java"));
        int declaration = blocks.indexOf("OBSIDIAN_TILE = BLOCKS.register(");
        int nextDeclaration = blocks.indexOf(
                "RegistryObject<Block>", declaration + 1);
        String registration = blocks.substring(declaration, nextDeclaration);
        assertFalse(registration.contains(".noLootTable()"));

        JsonObject loot = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/thaumic_reborn/loot_tables/blocks/obsidian_tile.json"
        ))).getAsJsonObject();
        assertTrue(loot.toString().contains("thaumic_reborn:obsidian_tile"));

        JsonObject diamondTag = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/data/minecraft/tags/blocks/needs_diamond_tool.json"
        ))).getAsJsonObject();
        assertTrue(diamondTag.toString().contains("thaumic_reborn:obsidian_tile"));
    }
}
