package com.thaumcraftmodern.visnet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisTamingVerticalFidelityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test
    void originalModelsTexturesAnimationAndSoundsAreBitExact()
            throws Exception {
        Map<String, String> hashes = Map.ofEntries(
                Map.entry("textures/models/node_stabilizer.obj",
                        "9e41491f1da585d50fc743218a98f1f899b11a873f18028aa0e2e444e0411d9f"),
                Map.entry("textures/models/node_stabilizer.png",
                        "0568b98bb69e56a068818bdeb027e527a08f50fb177772a9e32d680b01da1d39"),
                Map.entry("textures/models/node_stabilizer_over.png",
                        "2604fbf6f8f5f4e1002fe66d6019d1725fb96c1d8f094b8f9beeb91dba87f57f"),
                Map.entry("textures/models/node_converter.png",
                        "22412becfcf1fed7c379edaf42ee67e98c1dd672d5da633fec83b5e8d6648339"),
                Map.entry("textures/models/node_converter_over.png",
                        "28851f313fd31f743b1d3c6ece525675f6eb0ec28036f26d826953bd426be986"),
                Map.entry("textures/models/vis_relay.obj",
                        "d26653aa75f5e73296162322d9d132eec567e956dbef3ff37e5788bf9a265aeb"),
                Map.entry("textures/models/vis_relay.png",
                        "f124f1e32cee243dc445ac9900c225359e9cf053918336b47f295639a9005973"),
                Map.entry("textures/item/lightningringv.png",
                        "b90e47b84b1cfe82853bf08625f75812145d11adbdb921e74b0bed992225801d"),
                Map.entry("textures/misc/beam.png",
                        "59da9ea033b50a56be9919661803d865cb4ce03a37181dcbf3bf3b1093544778"),
                Map.entry("textures/misc/beam1.png",
                        "5ab71394678e02d0dbfc369974f85850c2457e6ddba0d8f02918f2bcfaf37469"),
                Map.entry("textures/misc/p_large.png",
                        "1d93bbf9edc18ceedb24a1df26922b2ad339d67a963e3a11d29df705a9ca8188"),
                Map.entry("textures/misc/p_small.png",
                        "bd33eac16c2c26b56372748f706a3327262b32ae9b3be110c90a784da2392a3e"),
                Map.entry("sounds/craftfail.ogg",
                        "f0abe9199ca6f5d47e27958ebfcc66b94dcae68310639c5340da432fa59ce9c5"),
                Map.entry("sounds/crystal.ogg",
                        "2737cd4dcda0d15e79c99a15b134e81f643cfff9235ff1277bc51651263fd0d7")
        );
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            Path path = RESOURCES.resolve("assets/thaumic_reborn")
                    .resolve(entry.getKey());
            assertEquals(entry.getValue(), sha256(path), entry.getKey());
        }
    }

    @Test
    void everyPublicDeviceHasItsOriginalRecipeOutput() throws IOException {
        assertRecipeOutput("node_stabilizer", "node_stabilizer");
        assertRecipeOutput("node_transducer", "node_transducer");
        assertRecipeOutput("node_relay", "vis_relay");
        assertRecipeOutput("node_charge_relay", "vis_charge_relay");
    }

    @Test
    void advancedStabilizerResearchIsActiveButRecipeIsHidden()
            throws IOException {
        JsonObject research = json(RESOURCES.resolve(
                "data/thaumic_reborn/thaumcraft/research/legacy/"
                        + "nodestabilizeradv.json"));
        assertFalse(research.get("inactive").getAsBoolean());
        JsonArray pages = research.getAsJsonArray("pages");
        for (var page : pages) {
            JsonObject object = page.getAsJsonObject();
            assertFalse(object.has("recipe"));
            assertFalse(object.has("recipes"));
            assertFalse("unavailable".equals(object.get("type").getAsString()));
        }
        assertFalse(Files.exists(RESOURCES.resolve(
                "data/thaumic_reborn/recipes/advanced_node_stabilizer.json")));
        assertEquals("thaumic_reborn:node_stabilizer",
                research.get("icon").getAsString());
    }

    @Test
    void itemModelsUseTheAnimatedBlockEntityRenderer() throws IOException {
        for (String id : new String[]{
                "node_stabilizer", "advanced_node_stabilizer",
                "node_transducer", "vis_relay", "vis_charge_relay"
        }) {
            JsonObject model = json(RESOURCES.resolve(
                    "assets/thaumic_reborn/models/item/" + id + ".json"));
            assertEquals("minecraft:builtin/entity",
                    model.get("parent").getAsString(), id);
            JsonObject gui = model.getAsJsonObject("display")
                    .getAsJsonObject("gui");
            assertEquals(30, gui.getAsJsonArray("rotation").get(0).getAsInt());
            assertEquals(225, gui.getAsJsonArray("rotation").get(1).getAsInt());
            assertEquals(0.625F,
                    gui.getAsJsonArray("scale").get(0).getAsFloat());
        }
    }

    @Test
    void energizedOutputUsesOriginalModifierThenSquareRootFormula() {
        assertEquals(4, EnergizedAuraNodeBlockEntity.energizedStrength(
                20, AuraNodeModifier.NORMAL));
        assertEquals(4, EnergizedAuraNodeBlockEntity.energizedStrength(
                20, AuraNodeModifier.BRIGHT));
        assertEquals(4, EnergizedAuraNodeBlockEntity.energizedStrength(
                20, AuraNodeModifier.PALE));
        assertEquals(3, EnergizedAuraNodeBlockEntity.energizedStrength(
                20, AuraNodeModifier.FADING));
        assertEquals(10, EnergizedAuraNodeBlockEntity.energizedStrength(
                100, AuraNodeModifier.NORMAL));
    }

    @Test
    void energizedRendererUsesAuraBaseWithoutMutatingDrainedOriginal() {
        AuraNodeState drained = AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.BRIGHT,
                Map.of("aer", 0, "lux", 0),
                Map.of("aer", 36, "lux", 12),
                7L
        );

        AuraNodeState display = EnergizedAuraNodeBlockEntity.displayState(
                drained);

        assertEquals(Map.of("aer", 36, "lux", 12),
                display.snapshot().aspectsCurrent());
        assertEquals(Map.of("aer", 0, "lux", 0),
                drained.snapshot().aspectsCurrent());
        assertEquals(drained.nodeId(), display.nodeId());
        assertEquals(drained.revision(), display.revision());
    }

    private static void assertRecipeOutput(String recipe, String item)
            throws IOException {
        JsonObject root = json(RESOURCES.resolve(
                "data/thaumic_reborn/recipes/" + recipe + ".json"));
        assertEquals("thaumic_reborn:" + item,
                root.getAsJsonObject("result").get("item").getAsString());
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String sha256(Path path)
            throws IOException, NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path));
        return java.util.HexFormat.of().formatHex(digest);
    }
}
