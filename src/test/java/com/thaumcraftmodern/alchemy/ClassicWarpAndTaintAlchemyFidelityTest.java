package com.thaumcraftmodern.alchemy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicWarpAndTaintAlchemyFidelityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test
    void crucibleRecipesKeepExactTc4CatalystsAndAspectCosts() throws Exception {
        JsonObject bath = crucible("bathsalts");
        assertEquals("thaumic_reborn:salis_mundus", catalystItem(bath));
        for (String aspect : new String[]{"cognitio", "auram", "ordo", "sano"}) {
            assertEquals(6, bath.getAsJsonObject("aspects").get(aspect).getAsInt());
        }
        JsonObject soap = crucible("sanesoap");
        for (String aspect : new String[]{"cognitio", "alienis", "ordo", "sano"}) {
            assertEquals(16, soap.getAsJsonObject("aspects").get(aspect).getAsInt());
        }
        JsonObject taint = crucible("bottletaint");
        assertEquals("thaumic_reborn:essentia_phial", catalystItem(taint));
        assertEquals("vitium", taint.getAsJsonObject("catalyst").get("aspect").getAsString());
        assertEquals(8, taint.getAsJsonObject("aspects").get("vitium").getAsInt());
        assertEquals(8, taint.getAsJsonObject("aspects").get("praecantatio").getAsInt());
        JsonObject death = crucible("liquiddeath");
        assertEquals("minecraft:bucket", catalystItem(death));
        for (String aspect : new String[]{"mortuus", "venenum", "perditio"}) {
            assertEquals(32, death.getAsJsonObject("aspects").get(aspect).getAsInt());
        }
    }

    @Test
    void researchProgressionPagesTriggersCostsAndWarpAreActive() throws Exception {
        JsonObject bath = research("bathsalts");
        assertEquals(11, bath.getAsJsonObject("reveal_when").get("minimum").getAsInt());
        assertTrue(bath.getAsJsonArray("parents").isEmpty());
        assertActiveRecipe(bath, "thaumic_reborn:bathsalts");
        assertEquals("bathsalts", research("sanesoap").getAsJsonArray("parents").get(0).getAsString());

        JsonObject taint = research("bottletaint");
        assertEquals("entropicprocessing", taint.getAsJsonArray("parents").get(0).getAsString());
        assertEquals("vitium", taint.getAsJsonObject("reveal_when").get("id").getAsString());
        assertEquals(2, taint.get("completion_warp").getAsInt());
        assertActiveRecipe(taint, "thaumic_reborn:bottletaint");

        JsonObject death = research("liquiddeath");
        assertEquals("entropicprocessing", death.getAsJsonArray("parents").get(0).getAsString());
        assertEquals(3, death.get("completion_warp").getAsInt());
        assertActiveRecipe(death, "thaumic_reborn:liquiddeath");
    }

    @Test
    void packagedThaumonomiconContainsRecipeItemLinkClass() throws Exception {
        Path jar = Files.list(Path.of("build/libs"))
                .filter(path -> path.getFileName().toString().endsWith(".jar"))
                .filter(path -> !path.getFileName().toString().endsWith(
                        "-sources.jar"))
                .findFirst().orElseThrow();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            assertTrue(zip.getEntry(
                    "com/thaumcraftmodern/client/screen/ThaumonomiconItemLinkRegion.class")
                    != null);
        }
    }

    @Test
    void serverMechanicsRetainOriginalTimingRadiusAndChances() throws Exception {
        String salts = source("item/BathSaltsItem.java");
        assertTrue(salts.contains("WATER_CONVERSION_TICKS = 200"));
        String soap = source("item/SanitySoapItem.java");
        assertTrue(soap.contains("USE_TICKS = 200"));
        assertTrue(soap.contains("REQUIRED_TICKS = 196"));
        assertTrue(soap.contains("float chance = 0.33F"));
        assertTrue(soap.contains("chance += 0.25F"));
        assertTrue(soap.contains("WarpType.PERMANENT"));
        String projectile = source("entity/BottledTaintProjectile.java");
        assertTrue(projectile.contains("EFFECT_RADIUS = 5.0D"));
        assertTrue(projectile.contains("INFECTION_TICKS = 100"));
        assertTrue(projectile.contains("TERRAIN_ATTEMPTS = 10"));
        String death = source("world/block/LiquidDeathBlock.java");
        assertTrue(death.contains("FULL_DAMAGE = 4.0F"));
        assertTrue(death.contains("instanceof ItemEntity"));
    }

    @Test
    void etherealBloomUsesRealIconExactColorsAndOnlyOriginalBiomePurification() throws Exception {
        JsonObject bloom = research("etherealbloom");
        assertEquals("thaumic_reborn:ethereal_bloom", bloom.get("icon").getAsString());
        assertFalse(bloom.get("inactive").getAsBoolean());
        String renderer = source("client/render/EtherealBloomBlockEntityRenderer.java");
        assertTrue(renderer.contains("NODE_RED = 0xAA"));
        assertTrue(renderer.contains("NODE_GREEN = 0xDD"));
        assertTrue(renderer.contains("NODE_BLUE = 0xFF"));
        assertTrue(renderer.contains("PLANT_COLOR = 0xFFFFFF"));
        String ticker = source("world/block/entity/EtherealBloomBlockEntity.java");
        assertTrue(ticker.contains("% 20"));
        assertTrue(ticker.contains("distSqr(target) > 81.0D"));
        assertFalse(ticker.contains("FLUX_GOO"));
        assertFalse(ticker.contains("FLUX_GAS"));
    }

    private static void assertActiveRecipe(JsonObject research, String recipe) {
        assertFalse(research.get("inactive").getAsBoolean());
        assertTrue(research.getAsJsonArray("pages").toString().contains(recipe));
    }

    private static String catalystItem(JsonObject recipe) {
        return recipe.getAsJsonObject("catalyst").get("item").getAsString();
    }

    private static JsonObject crucible(String id) throws Exception {
        return json(RESOURCES.resolve("data/thaumic_reborn/thaumcraft/crucible_recipes/" + id + ".json"));
    }

    private static JsonObject research(String id) throws Exception {
        return json(RESOURCES.resolve("data/thaumic_reborn/thaumcraft/research/legacy/" + id + ".json"));
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String source(String path) throws Exception {
        return Files.readString(Path.of("src/main/java/com/thaumcraftmodern").resolve(path));
    }
}
