package com.thaumcraftmodern.crystal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrystalClusterFidelityTest {
    private static final Path ROOT = Path.of("src/main");
    private static final Path ASSETS = ROOT.resolve(
            "resources/assets/thaumcraftmodern"
    );
    private static final Path RECIPES = ROOT.resolve(
            "resources/data/thaumcraftmodern/recipes"
    );
    private static final List<String> VARIANTS = List.of(
            "air", "fire", "water", "earth", "order", "entropy"
    );

    @Test
    void usesExactOriginalTextureAndModelCrystalCoordinates()
            throws Exception {
        assertEquals(
                "0438f3e65afc78f9a11817685fa79b1899b4632c53ca2590076fe333343213a0",
                sha256(ASSETS.resolve("textures/models/crystal.png"))
        );
        String model = source(
                "java/com/thaumcraftmodern/client/render/CrystalClusterModel.java"
        );
        assertTrue(model.contains(".texOffs(0, 0)"));
        assertTrue(model.contains(
                "-16.0F,\n                                -16.0F,\n"
                        + "                                0.0F,"
        ));
        assertTrue(model.contains(
                "16.0F,\n                                16.0F,\n"
                        + "                                16.0F"
        ));
        assertTrue(model.contains("0.0F,\n                        32.0F"));
        assertTrue(model.contains("0.7071F"));
        assertTrue(model.contains("LayerDefinition.create(mesh, 64, 32)"));
    }

    @Test
    void sixSpikesAndBalancedColorsFollowOriginalRenderer() throws Exception {
        String renderer = source(
                "java/com/thaumcraftmodern/client/render/CrystalClusterRenderer.java"
        );
        assertTrue(renderer.contains("for (int index = 1; index < 6; index++)"));
        assertTrue(renderer.contains("random.nextInt(36) + 72.0F * index"));
        assertTrue(renderer.contains("15.0F + random.nextInt(15)"));
        assertTrue(renderer.contains("0.15F + random.nextFloat() * 0.075F"));
        assertTrue(renderer.contains("0.5F + random.nextFloat() * 0.1F"));
        assertTrue(renderer.contains("0.15F + random.nextFloat() * 0.05F"));
        assertTrue(renderer.contains("poses.translate(0.5D, -0.3D, 0.5D)"));
        assertTrue(!renderer.contains("orient(poses, facing)"),
                "TC4 crystal clusters must render upright regardless of support face");

        assertEquals(0xEECCFF,
                CrystalClusterVariant.BALANCED.crystalColor(0));
        assertEquals(0xFFFF7E,
                CrystalClusterVariant.BALANCED.crystalColor(1));
        assertEquals(0xFF3C01,
                CrystalClusterVariant.BALANCED.crystalColor(2));
        assertEquals(0x0090FF,
                CrystalClusterVariant.BALANCED.crystalColor(3));
        assertEquals(0x00A000,
                CrystalClusterVariant.BALANCED.crystalColor(4));
        assertEquals(0x555577,
                CrystalClusterVariant.BALANCED.crystalColor(5));
    }

    @Test
    void allSevenOriginalShapelessRecipesAreActive() throws Exception {
        for (String variant : VARIANTS) {
            JsonObject recipe = json(
                    RECIPES.resolve(variant + "_crystal_cluster.json")
            );
            assertEquals("minecraft:crafting_shapeless",
                    recipe.get("type").getAsString());
            JsonArray ingredients = recipe.getAsJsonArray("ingredients");
            assertEquals(6, ingredients.size());
            ingredients.forEach(ingredient -> assertEquals(
                    "thaumcraftmodern:" + variant + "_shard",
                    ingredient.getAsJsonObject().get("item").getAsString()
            ));
            assertEquals(
                    "thaumcraftmodern:" + variant + "_crystal_cluster",
                    recipe.getAsJsonObject("result").get("item").getAsString()
            );
        }

        JsonObject balanced = json(
                RECIPES.resolve("balanced_crystal_cluster.json")
        );
        List<String> ingredients = balanced.getAsJsonArray("ingredients")
                .asList().stream()
                .map(element -> element.getAsJsonObject()
                        .get("item").getAsString())
                .toList();
        assertEquals(VARIANTS.stream()
                .map(id -> "thaumcraftmodern:" + id + "_shard")
                .toList(), ingredients);
    }

    @Test
    void oreResearchAndCreativeTabExposeAllSevenClusters() throws Exception {
        JsonObject research = json(ROOT.resolve(
                "resources/data/thaumcraftmodern/thaumcraft/research/legacy/ore.json"
        ));
        List<JsonObject> recipePages = research.getAsJsonArray("pages").asList()
                .stream()
                .map(element -> element.getAsJsonObject())
                .filter(page -> "recipe".equals(page.get("type").getAsString()))
                .toList();
        assertEquals(1, recipePages.size());
        JsonObject recipePage = recipePages.get(0);
        assertEquals(
                "thaumcraftmodern:air_crystal_cluster",
                recipePage.get("recipe").getAsString()
        );
        List<String> recipeIds = recipePage.getAsJsonArray("recipes").asList()
                .stream()
                .map(element -> element.getAsString())
                .toList();
        assertEquals(List.of(
                "thaumcraftmodern:air_crystal_cluster",
                "thaumcraftmodern:fire_crystal_cluster",
                "thaumcraftmodern:water_crystal_cluster",
                "thaumcraftmodern:earth_crystal_cluster",
                "thaumcraftmodern:order_crystal_cluster",
                "thaumcraftmodern:entropy_crystal_cluster",
                "thaumcraftmodern:balanced_crystal_cluster"
        ), recipeIds);

        String screen = source(
                "java/com/thaumcraftmodern/client/screen/ThaumonomiconPageRenderer.java"
        );
        assertTrue(screen.contains("System.currentTimeMillis() / 1000L"));

        String blocks = source(
                "java/com/thaumcraftmodern/registry/ModBlocks.java"
        );
        assertTrue(blocks.contains(".strength(0.7F, 1.0F)"));
        assertTrue(blocks.contains(".lightLevel(state -> 8)"));
        assertTrue(blocks.contains(".requiresCorrectToolForDrops()"));

        String creative = source(
                "java/com/thaumcraftmodern/registry/ModCreativeTabs.java"
        );
        for (String constant : List.of(
                "AIR", "FIRE", "WATER", "EARTH", "ORDER", "ENTROPY",
                "BALANCED"
        )) {
            assertTrue(creative.contains(
                    "output.accept(ModItems." + constant
                            + "_CRYSTAL_CLUSTER.get());"
            ));
        }
    }

    @Test
    void usesOriginalCrystalBreakAndClassicSupportingSounds()
            throws Exception {
        assertEquals(
                "2737cd4dcda0d15e79c99a15b134e81f643cfff9235ff1277bc51651263fd0d7",
                sha256(ASSETS.resolve("sounds/crystal.ogg"))
        );
        JsonObject sounds = json(ASSETS.resolve("sounds.json"));
        assertEquals(
                "master",
                sounds.getAsJsonObject("crystal")
                        .get("category").getAsString()
        );

        String soundType = source(
                "java/com/thaumcraftmodern/world/block/"
                        + "ClassicCrystalSoundType.java"
        );
        assertTrue(soundType.contains("public SoundEvent getBreakSound()"));
        assertTrue(soundType.contains("public SoundEvent getStepSound()"));
        assertTrue(soundType.contains("public SoundEvent getPlaceSound()"));
        assertTrue(soundType.contains("public SoundEvent getHitSound()"));
        assertTrue(soundType.contains("public SoundEvent getFallSound()"));
        assertEquals(5, occurrences(soundType, "return crystal();"));
        assertTrue(soundType.contains("return ModSounds.CRYSTAL.get();"));

        String blocks = source(
                "java/com/thaumcraftmodern/registry/ModBlocks.java"
        );
        assertTrue(blocks.contains(
                ".sound(ClassicCrystalSoundType.INSTANCE)"
        ));
    }

    @Test
    void allClustersUseOriginalTintedBreakingParticles() throws Exception {
        assertEquals(
                "03da91cc2fdaa152fb18240c988041e3b9982e9e1ddbb5365ef476266d15203e",
                sha256(ASSETS.resolve("textures/block/crystal.png"))
        );
        JsonObject particleModel = json(
                ASSETS.resolve("models/block/crystal_cluster_particles.json")
        );
        assertEquals(
                "thaumcraftmodern:block/crystal",
                particleModel.getAsJsonObject("textures")
                        .get("particle").getAsString()
        );

        String clientEvents = source(
                "java/com/thaumcraftmodern/client/ClientModEvents.java"
        );
        for (String constant : List.of(
                "AIR", "FIRE", "WATER", "EARTH", "ORDER", "ENTROPY",
                "BALANCED"
        )) {
            assertTrue(clientEvents.contains(
                    "ModBlocks." + constant + "_CRYSTAL_CLUSTER.get()"
            ));
        }
        assertTrue(clientEvents.contains(
                "ThreadLocalRandom.current().nextInt(1, 7)"
        ));
        assertTrue(clientEvents.contains(
                "return variant.crystalColor(crystalIndex);"
        ));
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String source(String relative) throws Exception {
        return Files.readString(ROOT.resolve(relative));
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(Files.readAllBytes(path))
        );
    }

    private static int occurrences(String source, String needle) {
        return (source.length() - source.replace(needle, "").length())
                / needle.length();
    }
}
