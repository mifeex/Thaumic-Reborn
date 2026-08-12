package com.thaumcraftmodern.wand;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WandClassicContentTest {
    @Test
    void basicRecipesKeepClassicPatternsAndConfiguredNbt() throws IOException {
        JsonObject cap = json(
                "/data/thaumcraftmodern/recipes/iron_wand_cap.json"
        );
        JsonObject wand = json(
                "/data/thaumcraftmodern/recipes/basic_wand.json"
        );

        assertEquals("NNN", cap.getAsJsonArray("pattern").get(0).getAsString());
        assertEquals("N N", cap.getAsJsonArray("pattern").get(1).getAsString());
        assertEquals(
                "forge:nuggets/iron",
                cap.getAsJsonObject("key")
                        .getAsJsonObject("N")
                        .get("tag")
                        .getAsString()
        );

        assertEquals("  I", wand.getAsJsonArray("pattern").get(0).getAsString());
        assertEquals(" S ", wand.getAsJsonArray("pattern").get(1).getAsString());
        assertEquals("I  ", wand.getAsJsonArray("pattern").get(2).getAsString());
        assertEquals(
                "forge:rods/wooden",
                wand.getAsJsonObject("key")
                        .getAsJsonObject("S")
                        .get("tag")
                        .getAsString()
        );
        JsonObject state = wand.getAsJsonObject("result")
                .getAsJsonObject("nbt")
                .getAsJsonObject(WandStateCodec.ROOT_KEY);
        assertEquals(WandStateCodec.SERIAL_VERSION, state.get("version").getAsInt());
        assertEquals("wood", state.get("rod").getAsString());
        assertEquals("iron", state.get("cap").getAsString());
        assertEquals(6, state.getAsJsonObject("vis").size());
    }

    @Test
    void silverwoodIsReadyWithoutRecipeAndBootstrapBookRecipeIsGone() {
        assertNotNull(resource(
                "/assets/thaumcraftmodern/models/item/silverwood_wand.json"
        ));
        assertNotNull(resource(
                "/data/thaumcraftmodern/thaumcraft/wands/silverwood.json"
        ));
        assertTrue(resource(
                "/data/thaumcraftmodern/recipes/silverwood_wand.json"
        ) == null);
        assertTrue(resource(
                "/data/thaumcraftmodern/recipes/thaumonomicon.json"
        ) == null);
    }

    @Test
    void classicComponentSetAndCodexWandResourcesAreComplete()
            throws IOException {
        String[] rods = {
                "wood", "greatwood", "silverwood", "obsidian", "blaze",
                "ice", "quartz", "bone", "reed", "greatwood_staff",
                "silverwood_staff", "obsidian_staff", "blaze_staff",
                "ice_staff", "quartz_staff", "bone_staff", "reed_staff",
                "primal_staff", "codex"
        };
        String[] caps = {
                "iron", "gold", "copper", "silver", "thaumium", "void"
        };
        for (String rod : rods) {
            JsonObject definition = json(
                    "/data/thaumcraftmodern/thaumcraft/wands/"
                            + rod
                            + ".json"
            );
            assertEquals(rod, definition.get("id").getAsString());
        }
        for (String cap : caps) {
            JsonObject definition = json(
                    "/data/thaumcraftmodern/thaumcraft/wands/"
                            + cap
                            + ".json"
            );
            assertEquals(cap, definition.get("id").getAsString());
        }
        assertEquals(
                1000,
                json("/data/thaumcraftmodern/thaumcraft/wands/codex.json")
                        .get("capacity_vis")
                        .getAsInt()
        );
        assertNotNull(resource(
                "/assets/thaumcraftmodern/models/item/codex_wand.json"
        ));
        assertTrue(resource(
                "/data/thaumcraftmodern/recipes/codex_wand.json"
        ) == null);
    }

    @Test
    void dynamicAssemblyRecipesAndClassicCraftCostsArePresent()
            throws IOException {
        assertEquals("thaumcraftmodern:arcane_wand_assembly",
                json("/data/thaumcraftmodern/recipes/arcane_wand_assembly.json")
                        .get("type").getAsString());
        assertEquals("thaumcraftmodern:arcane_sceptre_assembly",
                json("/data/thaumcraftmodern/recipes/arcane_sceptre_assembly.json")
                        .get("type").getAsString());
        assertEquals(1, wandCraftCost("wood"));
        assertEquals(3, wandCraftCost("greatwood"));
        assertEquals(9, wandCraftCost("silverwood"));
        assertEquals(8, wandCraftCost("greatwood_staff"));
        assertEquals(14, wandCraftCost("obsidian_staff"));
        assertEquals(24, wandCraftCost("silverwood_staff"));
        assertEquals(32, wandCraftCost("primal_staff"));
        assertEquals(1, wandCraftCost("iron"));
        assertEquals(2, wandCraftCost("copper"));
        assertEquals(3, wandCraftCost("gold"));
        assertEquals(4, wandCraftCost("silver"));
        assertEquals(6, wandCraftCost("thaumium"));
        assertEquals(9, wandCraftCost("void"));
        JsonObject sceptre = json(
                "/data/thaumcraftmodern/thaumcraft/research/legacy/sceptre.json");
        assertFalse(sceptre.get("inactive").getAsBoolean());
        assertEquals("thaumcraftmodern:arcane_sceptre_assembly",
                sceptre.getAsJsonArray("pages").get(1).getAsJsonObject()
                        .get("recipe").getAsString());
    }

    @Test
    void infusionLayoutResearchProvidesSixIncreasingDensityPreviews()
            throws IOException {
        JsonObject research = json(
                "/data/thaumcraftmodern/thaumcraft/research/"
                        + "infusion_layout_test.json"
        );
        assertTrue(research.get("auto_unlock").getAsBoolean());
        assertEquals(
                "thaumcraftmodern:runic_matrix",
                research.get("icon").getAsString()
        );
        long infusionPages = research.getAsJsonArray("pages").asList().stream()
                .filter(element -> element.getAsJsonObject()
                        .get("type")
                        .getAsString()
                        .equals("infusion"))
                .count();
        assertEquals(6, infusionPages);
        assertEquals(
                12,
                research.getAsJsonArray("pages")
                        .get(8)
                        .getAsJsonObject()
                        .getAsJsonArray("components")
                        .size()
        );
    }

    @Test
    void thaumonomiconDocumentsCurrentWandAndConstructionBoundaries()
            throws IOException {
        JsonObject thaumaturgy = json(
                "/data/thaumcraftmodern/thaumcraft/research/legacy/"
                        + "basicthaumaturgy.json"
        );
        assertFalse(thaumaturgy.get("inactive").getAsBoolean());
        assertEquals(
                "research.thaumcraftmodern.wand_catalog.rods.body",
                thaumaturgy.getAsJsonArray("pages")
                        .get(6)
                        .getAsJsonObject()
                        .get("body")
                        .getAsString()
        );
        assertEquals(
                "research.thaumcraftmodern.wand_catalog.availability.body",
                thaumaturgy.getAsJsonArray("pages")
                        .get(8)
                        .getAsJsonObject()
                        .get("body")
                        .getAsString()
        );

        JsonObject workbench = json(
                "/data/thaumcraftmodern/thaumcraft/research/legacy/"
                        + "arctable.json"
        );
        assertFalse(workbench.get("inactive").getAsBoolean());
        assertEquals(
                "research.thaumcraftmodern.constructions.status.body",
                workbench.getAsJsonArray("pages")
                        .get(3)
                        .getAsJsonObject()
                        .get("body")
                        .getAsString()
        );
    }

    @Test
    void animatedCapsAndStaffCoreIconsKeepClassicResourceLayout()
            throws IOException {
        for (String cap : new String[]{"silver", "thaumium", "void"}) {
            JsonObject metadata = json(
                    "/assets/thaumcraftmodern/textures/item/wand_cap_"
                            + cap
                            + ".png.mcmeta"
            );
            assertEquals(
                    3,
                    metadata.getAsJsonObject("animation")
                            .get("frametime")
                            .getAsInt()
            );
            assertEquals(
                    4,
                    metadata.getAsJsonObject("animation")
                            .getAsJsonArray("frames")
                            .size()
            );
        }
        for (String rod : new String[]{
                "greatwood", "silverwood", "obsidian", "blaze", "ice",
                "quartz", "bone", "reed", "primal"
        }) {
            assertNotNull(resource(
                    "/assets/thaumcraftmodern/textures/item/staff_rod_"
                            + rod
                            + ".png"
            ));
        }
        JsonObject wandModel = json(
                "/assets/thaumcraftmodern/models/item/classic_wand_base.json"
        );
        assertEquals("minecraft:builtin/entity",
                wandModel.get("parent").getAsString());
        JsonObject gui = wandModel.getAsJsonObject("display")
                .getAsJsonObject("gui");
        assertEquals("[0,0,0]", gui.getAsJsonArray("rotation").toString());
        assertEquals("[0,0,0]", gui.getAsJsonArray("translation").toString());
        assertEquals("[1,1,1]", gui.getAsJsonArray("scale").toString());
    }

    @Test
    void copiedClassicTexturesAndSoundsKeepExactJarHashes()
            throws IOException, NoSuchAlgorithmException {
        assertHash(
                "/assets/thaumcraftmodern/textures/item/wand_cap_iron.png",
                "d1af5486916767147f77fa18cca0cafc10e9696c4c00b60b8bbef0c011d32df2"
        );
        assertHash(
                "/assets/thaumcraftmodern/textures/item/wand_rod_silverwood.png",
                "6b5a886b3a63cee38f8b4d65cee84f0859dd674407dfad2059f485c3d3b0e58f"
        );
        assertHash(
                "/assets/thaumcraftmodern/textures/item/wand_cap_iron_model.png",
                "f21b3dc72fefd8e1df26393756ec5aa47aecf8559f6a7d25e0ab062efc361314"
        );
        assertHash(
                "/assets/thaumcraftmodern/textures/item/wand_rod_wood_model.png",
                "851f35e5bc18ef7f04cf15ef6c3bd3bfc12d02162926c089810abb43970527d6"
        );
        assertHash(
                "/assets/thaumcraftmodern/textures/item/wand_rod_silverwood_model.png",
                "09f6fc4133c0427be99b9f773c81a711baedff18fc13ce86ae3b4ad10a42c480"
        );
        assertHash(
                "/assets/thaumcraftmodern/sounds/wand1.ogg",
                "b3a2b2f94599e1189fc82d5086f33aa2b048bff8387cf148f916417ab6b971da"
        );
        assertHash(
                "/assets/thaumcraftmodern/sounds/wand2.ogg",
                "d690d1947b3226685bfd6fb00c6509fab8a6814aa41eb0992e2e9a76689e41cf"
        );
        assertHash(
                "/assets/thaumcraftmodern/sounds/wand3.ogg",
                "fad1a12fd2d0edcbb51d31da51215667438cc76dace8f40f8bdb276fd4ba19ab"
        );
    }

    private static JsonObject json(String path) throws IOException {
        try (InputStream stream = resource(path)) {
            assertNotNull(stream, "Missing resource " + path);
            return JsonParser.parseString(
                    new String(stream.readAllBytes(), StandardCharsets.UTF_8)
            ).getAsJsonObject();
        }
    }

    private static int wandCraftCost(String id) throws IOException {
        return json("/data/thaumcraftmodern/thaumcraft/wands/" + id + ".json")
                .get("craft_cost_vis").getAsInt();
    }

    private static InputStream resource(String path) {
        return WandClassicContentTest.class.getResourceAsStream(path);
    }

    private static void assertHash(String path, String expected)
            throws IOException, NoSuchAlgorithmException {
        try (InputStream stream = resource(path)) {
            assertNotNull(stream, "Missing resource " + path);
            String actual = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(stream.readAllBytes())
            );
            assertEquals(expected, actual, "Changed classic asset " + path);
        }
    }
}
