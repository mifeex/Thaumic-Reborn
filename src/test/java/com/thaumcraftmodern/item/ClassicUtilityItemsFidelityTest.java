package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassicUtilityItemsFidelityTest {
    private static final Path JAVA = Path.of("src/main/java/com/thaumcraftmodern");
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/item");
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures/items");
    private static final Path RECIPES = Path.of(
            "src/main/resources/data/thaumic_reborn/recipes");

    @Test
    void purifyingFluidIsAFlowingBucketFluidWithSourceOnlyWard() throws Exception {
        String fluids = Files.readString(JAVA.resolve("registry/ModFluids.java"));
        String block = Files.readString(JAVA.resolve("world/block/PurifyingFluidBlock.java"));
        String items = Files.readString(JAVA.resolve("registry/ModItems.java"));
        assertTrue(fluids.contains("PURIFYING_SOURCE"));
        assertTrue(fluids.contains(".viscosity(1500)"));
        assertTrue(fluids.contains(".lightLevel(8)"));
        assertTrue(block.contains("extends LiquidBlock"));
        assertTrue(block.contains("getFluidState(pos).isSource()"));
        assertTrue(items.contains("new BucketItem("));
        assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve("bucket_pure.png")),
                Files.readAllBytes(ASSETS.resolve("purifying_fluid_bucket.png")));
    }

    @Test
    void nativeCinnabarKeepsOriginalProcessingAndSprite() throws Exception {
        JsonObject recipe = JsonParser.parseString(Files.readString(
                RECIPES.resolve("native_cinnabar_cluster_smelting.json"))).getAsJsonObject();
        assertEquals("thaumic_reborn:double_smelting", recipe.get("type").getAsString());
        assertEquals("thaumic_reborn:quicksilver", recipe.get("result").getAsString());
        assertEquals(2, recipe.get("count").getAsInt());
        assertEquals(1.0F, recipe.get("experience").getAsFloat());
        assertTrue(Files.readString(JAVA.resolve(
                "world/block/entity/InfernalFurnaceBlockEntity.java"))
                .contains("NATIVE_CINNABAR_CLUSTER"));
        assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve("clustercinnabar.png")),
                Files.readAllBytes(ASSETS.resolve("native_cinnabar_cluster.png")));
    }

    @Test
    void tripleMeatTreatHasAllFourOriginalCombinationsAndEffects() throws Exception {
        String item = Files.readString(JAVA.resolve("item/TripleMeatTreatItem.java"));
        assertTrue(item.contains("REGENERATION_TICKS = 200"));
        assertTrue(item.contains("STRENGTH_TICKS = 600"));
        String registry = Files.readString(JAVA.resolve("registry/ModItems.java"));
        assertTrue(registry.contains(".nutrition(6)"));
        assertTrue(registry.contains(".saturationMod(0.8F)"));
        assertTrue(registry.contains(".stacksTo(16)"));
        Set<String> recipes = Set.of(
                "triple_meat_treat_chicken_beef_pork.json",
                "triple_meat_treat_chicken_beef_fish.json",
                "triple_meat_treat_chicken_pork_fish.json",
                "triple_meat_treat_beef_pork_fish.json");
        for (String recipe : recipes) assertTrue(Files.isRegularFile(RECIPES.resolve(recipe)));
        assertArrayEquals(Files.readAllBytes(ORIGINAL.resolve("tripletreat.png")),
                Files.readAllBytes(ASSETS.resolve("triple_meat_treat.png")));
    }
}
