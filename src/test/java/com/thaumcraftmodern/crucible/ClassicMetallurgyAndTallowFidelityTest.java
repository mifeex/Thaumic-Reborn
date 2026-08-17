package com.thaumcraftmodern.crucible;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicMetallurgyAndTallowFidelityTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test
    void tc4CrucibleCostsAndOutputsAreExecutable() throws IOException {
        assertTransmutation("transcopper", "permutatio");
        assertTransmutation("transtin", "vitreus");
        assertTransmutation("transsilver", "lucrum");
        assertTransmutation("translead", "ordo");

        for (String metal : new String[]{"iron", "gold", "copper", "tin", "silver", "lead"}) {
            JsonObject recipe = crucible("pure" + metal);
            assertEquals("forge:ores/" + metal,
                    recipe.getAsJsonObject("catalyst").get("tag").getAsString());
            assertEquals(1, recipe.getAsJsonObject("aspects").get("metallum").getAsInt());
            assertEquals(1, recipe.getAsJsonObject("aspects").get("ordo").getAsInt());
            assertFalse(recipe.has("inactive"));

            JsonObject smelting = recipe("native_" + metal + "_cluster_smelting");
            assertEquals(2, smelting.get("count").getAsInt());
            assertEquals(1.0F, smelting.get("experience").getAsFloat());

            JsonObject blasting = recipe("native_" + metal + "_cluster_blasting");
            assertEquals("thaumic_reborn:double_blasting",
                    blasting.get("type").getAsString());
            assertEquals(2, blasting.get("count").getAsInt());
            assertEquals(100, blasting.get("cookingtime").getAsInt());
        }
    }

    @Test
    void tallowAndThreeCandleRecipeMatchTc4() throws IOException {
        JsonObject tallow = crucible("tallow");
        assertEquals("minecraft:rotten_flesh",
                tallow.getAsJsonObject("catalyst").get("item").getAsString());
        assertEquals(2, tallow.getAsJsonObject("aspects").get("praecantatio").getAsInt());
        assertFalse(tallow.has("inactive"));

        JsonObject candle = recipe("tallow_candle");
        assertEquals(" S ", candle.getAsJsonArray("pattern").get(0).getAsString());
        assertEquals(" T ", candle.getAsJsonArray("pattern").get(1).getAsString());
        assertEquals(" T ", candle.getAsJsonArray("pattern").get(2).getAsString());
        assertEquals(3, candle.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void thaumonomiconPagesAreActiveAndPointAtRuntimeRecipes() throws IOException {
        for (String id : new String[]{
                "tallow", "pureiron", "puregold", "purecopper", "puretin",
                "puresilver", "purelead", "transiron", "transgold", "transcopper", "transtin",
                "transsilver", "translead"
        }) {
            JsonObject research = json(ROOT.resolve(
                    "data/thaumic_reborn/thaumcraft/research/legacy/" + id + ".json"));
            assertFalse(research.get("inactive").getAsBoolean(), id);
            assertTrue(research.getAsJsonArray("pages").toString()
                    .contains("thaumic_reborn:" + id), id);
            assertFalse("thaumic_reborn:thaumonomicon".equals(
                    research.get("icon").getAsString()), id);
        }
    }

    @Test
    void metallurgyAndTallowResearchUseTheirActualTc4ContentIcons() throws IOException {
        for (String metal : new String[]{"iron", "gold", "copper", "tin", "silver", "lead"}) {
            JsonObject research = json(ROOT.resolve(
                    "data/thaumic_reborn/thaumcraft/research/legacy/pure" + metal + ".json"));
            assertEquals("thaumic_reborn:native_" + metal + "_cluster",
                    research.get("icon").getAsString());
        }
        JsonObject tallow = json(ROOT.resolve(
                "data/thaumic_reborn/thaumcraft/research/legacy/tallow.json"));
        assertEquals("thaumic_reborn:thaumic_tallow",
                tallow.get("icon").getAsString());
    }

    private static void assertTransmutation(String id, String secondary) throws IOException {
        JsonObject recipe = crucible(id);
        assertEquals(3, recipe.getAsJsonObject("output").get("count").getAsInt());
        assertEquals(2, recipe.getAsJsonObject("aspects").get("metallum").getAsInt());
        assertEquals(1, recipe.getAsJsonObject("aspects").get(secondary).getAsInt());
        assertFalse(recipe.has("inactive"));
    }

    private static JsonObject crucible(String id) throws IOException {
        return json(ROOT.resolve("data/thaumic_reborn/thaumcraft/crucible_recipes/" + id + ".json"));
    }

    private static JsonObject recipe(String id) throws IOException {
        return json(ROOT.resolve("data/thaumic_reborn/recipes/" + id + ".json"));
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
