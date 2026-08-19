package com.thaumcraftmodern.item;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class WardedGlassFidelityTest {
    private static final Path JAVA = Path.of("src/main/java/com/thaumcraftmodern");
    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/block");
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                    + "assets/thaumcraft/textures/blocks");

    @Test
    void wardedGlassIsAnOwnerBoundWandRemovableProtectedBlock() throws Exception {
        String block = Files.readString(JAVA.resolve("world/block/WardedGlassBlock.java"));
        String entity = Files.readString(JAVA.resolve(
                "world/block/entity/WardedGlassBlockEntity.java"));
        String blocks = Files.readString(JAVA.resolve("registry/ModBlocks.java"));
        String items = Files.readString(JAVA.resolve("registry/ModItems.java"));
        assertTrue(block.contains("extends GlassBlock"));
        assertTrue(block.contains("implements EntityBlock, WandInteractable"));
        assertTrue(block.contains("canEntityDestroy"));
        assertTrue(block.contains("onBlockExploded"));
        assertTrue(block.contains("glass.owner().equals"));
        assertTrue(block.contains("popResource(level, pos, new ItemStack(this))"));
        assertTrue(entity.contains("tag.putString(\"owner\", owner)"));
        assertTrue(blocks.contains("strength(-1.0F, 999.0F)"));
        assertTrue(items.contains("case \"warded_glass\" -> blockItem(name, ModBlocks.WARDED_GLASS)"));
    }

    @Test
    void allFortySevenOriginalConnectedTexturesAreUsed() throws Exception {
        String model = Files.readString(JAVA.resolve(
                "client/render/WardedGlassBakedModel.java"));
        assertTrue(model.contains("TEXTURE_BY_MASK"));
        assertTrue(model.contains("textureIndex(level, pos, side)"));
        assertTrue(model.contains("\"block/warded_glass_\" + (index + 1)"));
        for (int index = 1; index <= 47; index++) {
            String name = "warded_glass_" + index + ".png";
            assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve(name)),
                    Files.readAllBytes(TEXTURES.resolve(name)), name);
        }
    }

    @Test
    void resourceModelsAndLootMatchTheProtectedBlockContract() throws Exception {
        Path resources = Path.of("src/main/resources");
        assertEquals("thaumic_reborn:block/warded_glass",
                JsonParser.parseString(Files.readString(resources.resolve(
                        "assets/thaumic_reborn/models/item/warded_glass.json")))
                        .getAsJsonObject().get("parent").getAsString());
        assertEquals(0, JsonParser.parseString(Files.readString(resources.resolve(
                        "data/thaumic_reborn/loot_tables/blocks/warded_glass.json")))
                .getAsJsonObject().getAsJsonArray("pools").size());
    }
}
