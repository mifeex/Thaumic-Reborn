package com.thaumcraftmodern.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuriosAccessoryFidelityTest {
    @Test
    void visStorageUsesClassicCapacitiesAndClampedCentivis() {
        CompoundTag storage = VisStorageState.initialize(new CompoundTag());
        int stoneCapacity = VisStorageState.capacityCentivis(25);

        assertEquals(2_500, stoneCapacity);
        assertEquals(25_000, VisStorageState.capacityCentivis(250));
        assertEquals(2_500, VisStorageState.addCentivis(
                storage, PrimalAspect.AER, 3_000, stoneCapacity));
        assertEquals(2_500, VisStorageState.visCentivis(
                storage, PrimalAspect.AER));
        assertEquals(125, VisStorageState.removeCentivis(
                storage, PrimalAspect.AER, 125));
        assertEquals(2_375, VisStorageState.visCentivis(
                storage, PrimalAspect.AER));
    }

    @Test
    void curiosDefinesClassicPlayerSlotsAndAssignments() throws Exception {
        JsonObject slots = json(Path.of(
                "src/main/resources/data/thaumic_reborn/curios/entities/players.json"));
        assertTrue(slots.getAsJsonArray("slots").asList().stream()
                .anyMatch(value -> value.getAsString().equals("necklace")));
        assertTrue(slots.getAsJsonArray("slots").asList().stream()
                .anyMatch(value -> value.getAsString().equals("belt")));

        JsonObject ringSlot = json(Path.of(
                "src/main/resources/data/thaumic_reborn/curios/slots/ring.json"));
        assertEquals(2, ringSlot.get("size").getAsInt());

        JsonObject rings = json(Path.of(
                "src/main/resources/data/curios/tags/items/ring.json"));
        assertEquals(11, rings.getAsJsonArray("values").size());

        JsonObject necklaces = json(Path.of(
                "src/main/resources/data/curios/tags/items/necklace.json"));
        assertTrue(necklaces.getAsJsonArray("values").asList().stream()
                .anyMatch(value -> value.getAsString()
                        .equals("thaumic_reborn:vis_storage_amulet")));
    }

    @Test
    void visStorageWearingIsNotGatedBehindResearch() {
        assertFalse(Arrays.stream(VisStorageItem.class.getDeclaredMethods())
                .anyMatch(method -> method.getName().equals("canEquip")));
    }

    @Test
    void visStorageResearchAndRecipeAreExecutableAndSynchronized()
            throws Exception {
        JsonObject research = json(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/research/legacy/visamulet.json"));
        JsonObject recipe = json(Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/infusion_recipes/vis_storage_amulet.json"));

        assertFalse(research.get("inactive").getAsBoolean());
        JsonObject page = research.getAsJsonArray("pages").get(1)
                .getAsJsonObject();
        assertEquals("infusion", page.get("type").getAsString());
        assertEquals("thaumic_reborn:vis_storage_amulet",
                page.get("recipe").getAsString());
        assertEquals(6, recipe.get("instability").getAsInt());
        assertEquals("thaumic_reborn:mundane_amulet",
                recipe.getAsJsonObject("central").get("item").getAsString());
        assertEquals(6, recipe.getAsJsonArray("components").size());
        assertEquals(24, recipe.getAsJsonObject("essentia")
                .get("auram").getAsInt());
        assertEquals(64, recipe.getAsJsonObject("essentia")
                .get("potentia").getAsInt());
        assertEquals(64, recipe.getAsJsonObject("essentia")
                .get("praecantatio").getAsInt());
        assertEquals(24, recipe.getAsJsonObject("essentia")
                .get("vacuos").getAsInt());
    }

    @Test
    void packagedAccessoryTexturesAreByteExactTc4Assets() throws Exception {
        Path jarPath = Path.of(
                "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar");
        try (JarFile original = new JarFile(jarPath.toFile())) {
            for (String texture : List.of(
                    "bauble_amulet.png", "bauble_ring.png",
                    "bauble_belt.png", "bauble_ring_iron.png",
                    "vis_amulet.png", "vis_amulet_lesser.png")) {
                var entry = original.getJarEntry(
                        "assets/thaumcraft/textures/items/" + texture);
                assertNotNull(entry, texture);
                byte[] expected;
                try (InputStream stream = original.getInputStream(entry)) {
                    expected = stream.readAllBytes();
                }
                try (InputStream packaged = getClass().getResourceAsStream(
                        "/assets/thaumic_reborn/textures/item/" + texture)) {
                    assertNotNull(packaged, texture);
                    assertArrayEquals(hash(expected), hash(packaged.readAllBytes()),
                            texture);
                }
            }
        }
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(
                path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static byte[] hash(byte[] value) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(value);
    }
}
