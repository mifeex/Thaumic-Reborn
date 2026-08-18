package com.thaumcraftmodern.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneBoreFidelityTest {
    private static final Path ORIGINAL = Path.of(
            "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar");
    private static final Path ROOT = Path.of("src/main/resources");

    @Test void boreGuiAndVortexAreByteExactJarAssets() throws Exception {
        Map<String, String> assets = Map.of(
                "assets/thaumcraft/textures/models/Bore.png",
                "assets/thaumic_reborn/textures/models/bore.png",
                "assets/thaumcraft/textures/gui/gui_arcanebore.png",
                "assets/thaumic_reborn/textures/gui/gui_arcanebore.png",
                "assets/thaumcraft/textures/misc/vortex.png",
                "assets/thaumic_reborn/textures/misc/vortex.png",
                "assets/thaumcraft/textures/models/jar.png",
                "assets/thaumic_reborn/textures/models/jar.png",
                "assets/thaumcraft/sounds/rumble.ogg",
                "assets/thaumic_reborn/sounds/rumble.ogg");
        try (ZipFile jar = new ZipFile(ORIGINAL.toFile())) {
            for (var asset : assets.entrySet()) try (InputStream source =
                    jar.getInputStream(jar.getEntry(asset.getKey()))) {
                assertArrayEquals(source.readAllBytes(),
                        Files.readAllBytes(ROOT.resolve(asset.getValue())), asset.getValue());
            }
        }
        assertEquals("d1527e8c995e3e576a37200bc334e77f35f628515a678565d811fb618e7ed71f",
                sha("assets/thaumic_reborn/textures/models/bore.png"));
        assertEquals("55d92e67bc3583cc18d7c1ee80345ddb51fc5ef0fece41c132e8c90171108de5",
                sha("assets/thaumic_reborn/textures/gui/gui_arcanebore.png"));
    }

    @Test void modelKeepsOriginalCuboidsPivotsAndUvs() throws Exception {
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/ArcaneBoreModel.java"));
        assertTrue(model.contains("texOffs(64, 24).mirror().addBox(-8, 0, -8, 16, 2, 16)"));
        assertTrue(model.contains("texOffs(0, 32).mirror().addBox(-6, 0, -6, 12, 2, 12)"));
        assertTrue(model.contains("texOffs(30, 14).mirror().addBox(4, -2.5F, -2.5F, 4, 5, 5)"));
        assertTrue(model.contains("texOffs(66, 0).mirror().addBox(-2, -4, -2, 4, 4, 4)"));
        assertTrue(model.contains("createBoreLayer()"));
        assertTrue(model.contains("LayerDefinition.create(mesh, 128, 64)"));
        assertTrue(model.contains("createEmitterLayer()"));
        assertTrue(model.contains("createSupportLayer()"));
    }

    @Test void itemKeepsThePreviouslyWorkingGuiTransform() throws Exception {
        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/ArcaneBoreItemRenderer.java"));
        assertFalse(renderer.contains("GUI_SCALE"));
        assertFalse(renderer.contains("context == ItemDisplayContext.GUI"));

        JsonObject model = JsonParser.parseString(Files.readString(ROOT.resolve(
                "assets/thaumic_reborn/models/item/arcane_bore.json")))
                .getAsJsonObject();
        JsonObject gui = model.getAsJsonObject("display").getAsJsonObject("gui");
        assertFalse(gui.has("translation"));
        assertEquals(0.625F, gui.getAsJsonArray("scale").get(0).getAsFloat());
    }

    @Test void miningAndPowerConstantsMatchTc4() throws Exception {
        String bore = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/ArcaneBoreBlockEntity.java"));
        assertTrue(bore.contains("MAX_RADIUS = 2"));
        assertTrue(bore.contains("MAX_DEPTH = 64"));
        assertTrue(bore.contains("MAX_SPEEDY_TIME = 20"));
        assertTrue(bore.contains("MAX_VIS_PER_TICK = 100"));
        assertTrue(bore.contains("VIS_PER_FAST_BLOCK = 5"));
        assertTrue(bore.contains("0.2F + dropFortune * 0.075F"));
        assertTrue(bore.contains("delay * 4"));
    }

    @Test void infusionRecipeIsOriginalTc4Recipe() throws Exception {
        JsonObject recipe = JsonParser.parseString(Files.readString(ROOT.resolve(
                "data/thaumic_reborn/thaumcraft/infusion_recipes/arcane_bore.json")))
                .getAsJsonObject();
        assertEquals(4, recipe.get("instability").getAsInt());
        assertEquals("minecraft:diamond_block", recipe.getAsJsonObject("central")
                .get("item").getAsString());
        assertEquals(8, recipe.getAsJsonArray("components").size());
        assertEquals(32, recipe.getAsJsonObject("essentia").get("perfodio").getAsInt());
        assertEquals(32, recipe.getAsJsonObject("essentia").get("machina").getAsInt());
    }

    @Test void researchShowsTheImplementedInfusionRecipe() throws Exception {
        JsonObject research = JsonParser.parseString(Files.readString(ROOT.resolve(
                "data/thaumic_reborn/thaumcraft/research/legacy/arcanebore.json")))
                .getAsJsonObject();
        JsonObject page = research.getAsJsonArray("pages").get(1).getAsJsonObject();
        assertEquals("infusion", page.get("type").getAsString());
        assertEquals("thaumic_reborn:arcane_bore", page.get("recipe").getAsString());
        assertEquals("thaumic_reborn:arcane_bore", page.get("output").getAsString());
        assertEquals(8, page.getAsJsonArray("components").size());
    }

    private static String sha(String path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(ROOT.resolve(path))));
    }
}
