package com.thaumcraftmodern.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimalCrusherFidelityTest {
    private static final Path RESEARCH = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy"
    );
    private static final Path INFUSION = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes"
    );

    @Test
    void tierAndMiningPlaneMatchTc4() {
        assertEquals(5, PrimalCrusherTier.INSTANCE.getLevel());
        assertEquals(500, PrimalCrusherTier.INSTANCE.getUses());
        assertEquals(8.0F, PrimalCrusherTier.INSTANCE.getSpeed());
        assertEquals(4.0F,
                PrimalCrusherTier.INSTANCE.getAttackDamageBonus());
        assertEquals(20, PrimalCrusherTier.INSTANCE.getEnchantmentValue());
    }

    @Test
    void activeResearchAndInfusionUseOriginalRecipe() throws IOException {
        JsonObject research = read(RESEARCH.resolve("primalcrusher.json"));
        assertFalse(research.get("inactive").getAsBoolean());
        assertEquals("thaumic_reborn:primal_crusher",
                research.get("icon").getAsString());
        JsonObject page = research.getAsJsonArray("pages")
                .get(1).getAsJsonObject();
        assertEquals("infusion", page.get("type").getAsString());
        assertEquals("dangerous", page.get("instability").getAsString());
        assertComponents(page.getAsJsonArray("components"));

        JsonObject recipe = read(INFUSION.resolve("primal_crusher.json"));
        assertEquals("primalcrusher", recipe.get("research").getAsString());
        assertEquals(6, recipe.get("instability").getAsInt());
        assertEquals("thaumic_reborn:primordial_pearl",
                recipe.getAsJsonObject("central").get("item").getAsString());
        assertComponents(recipe.getAsJsonArray("components"));
        JsonObject essentia = recipe.getAsJsonObject("essentia");
        assertEquals(24, essentia.get("perfodio").getAsInt());
        assertEquals(24, essentia.get("instrumentum").getAsInt());
        for (String id : List.of(
                "perditio", "vacuos", "telum", "alienis", "lucrum")) {
            assertEquals(16, essentia.get(id).getAsInt(), id);
        }
    }

    @Test
    void originalAnimatedCrusherAndNodeIconsAreByteExact()
            throws IOException {
        Path classic = Path.of(
                "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                        + "assets/thaumcraft/textures"
        );
        Path modern = Path.of(
                "src/main/resources/assets/thaumic_reborn/textures"
        );
        assertArrayEquals(
                Files.readAllBytes(classic.resolve("items/primal_crusher.png")),
                Files.readAllBytes(modern.resolve("item/primal_crusher.png"))
        );
        assertArrayEquals(
                Files.readAllBytes(classic.resolve(
                        "items/primal_crusher.png.mcmeta")),
                Files.readAllBytes(modern.resolve(
                        "item/primal_crusher.png.mcmeta"))
        );
        assertArrayEquals(
                Files.readAllBytes(classic.resolve("misc/r_nodes_2.png")),
                Files.readAllBytes(modern.resolve("misc/r_nodes_2.png"))
        );

        JsonObject primnode = read(RESEARCH.resolve("primnode.json"));
        assertFalse(primnode.get("inactive").getAsBoolean());
        assertFalse(primnode.has("icon"));
        assertEquals("thaumic_reborn:textures/misc/r_nodes_2.png",
                primnode.get("icon_resource").getAsString());
    }

    @Test
    void runtimeIncludesRepairWarpClustersAndOriginalSound() throws IOException {
        String item = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/item/PrimalCrusherItem.java"
        ));
        assertTrue(item.contains("repairOnePerSecond"));
        assertTrue(item.contains("player.gameMode.destroyBlock(target)"));
        assertTrue(item.contains("case Y -> new BlockPos(first, 0, second)"));
        assertTrue(item.contains("case Z -> new BlockPos(first, second, 0)"));
        assertTrue(item.contains("case X -> new BlockPos(0, second, first)"));
        String warp = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/warp/WarpGearService.java"
        ));
        assertTrue(warp.contains("stack.is(ModItems.PRIMAL_CRUSHER.get())"));
        String loot = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/loot/ElementalPickaxeLootModifier.java"
        ));
        assertTrue(loot.contains("instanceof PrimalCrusherItem"));
        assertTrue(loot.contains("SoundEvents.EXPERIENCE_ORB_PICKUP"));
    }

    private static void assertComponents(JsonArray components) {
        List<String> expected = List.of(
                "thaumic_reborn:primal_charm",
                "thaumic_reborn:void_pickaxe",
                "thaumic_reborn:void_shovel",
                "thaumic_reborn:primal_charm",
                "thaumic_reborn:pickaxe_of_the_core",
                "thaumic_reborn:shovel_of_the_earthmover"
        );
        assertEquals(expected.size(), components.size());
        for (int index = 0; index < expected.size(); index++) {
            assertEquals(expected.get(index), components.get(index)
                    .getAsJsonObject().get("item").getAsString());
        }
    }

    private static JsonObject read(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
