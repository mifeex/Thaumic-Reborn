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
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArcaneSpaFidelityTest {
    private static final Path ORIGINAL = Path.of(
            "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar");
    private static final Path RESOURCES = Path.of("src/main/resources");

    @Test
    void spaAndGuiPixelsAreByteExactOriginalJarAssets() throws Exception {
        Map<String, String> assets = Map.of(
                "assets/thaumcraft/textures/blocks/spa_side.png",
                "assets/thaumic_reborn/textures/block/spa_side.png",
                "assets/thaumcraft/textures/blocks/spa_top.png",
                "assets/thaumic_reborn/textures/block/spa_top.png",
                "assets/thaumcraft/textures/gui/gui_spa.png",
                "assets/thaumic_reborn/textures/gui/gui_spa.png");
        try (ZipFile jar = new ZipFile(ORIGINAL.toFile())) {
            for (Map.Entry<String, String> asset : assets.entrySet()) {
                try (InputStream source = jar.getInputStream(jar.getEntry(asset.getKey()))) {
                    assertArrayEquals(source.readAllBytes(),
                            Files.readAllBytes(RESOURCES.resolve(asset.getValue())),
                            asset.getValue());
                }
            }
        }
        assertEquals("1a029bfdc15cd4ba6dd44c83d7364ca499006d6f94806036c4fa204c2033a00c",
                sha256("assets/thaumic_reborn/textures/block/spa_side.png"));
        assertEquals("759852af1626df8803a34de1cbaee548519f7af76d69189ac716318d62fb1e50",
                sha256("assets/thaumic_reborn/textures/block/spa_top.png"));
        assertEquals("770041f59e175222c3f827747dfcc112a73b47022ccff2ff7fad3b1ef82d8ddf",
                sha256("assets/thaumic_reborn/textures/gui/gui_spa.png"));
    }

    @Test
    void fullCubeUsesTheOriginalBottomTopAndSideAssignments() throws Exception {
        JsonObject model = JsonParser.parseString(Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/arcane_spa.json"))).getAsJsonObject();
        assertEquals("minecraft:block/cube_bottom_top", model.get("parent").getAsString());
        JsonObject textures = model.getAsJsonObject("textures");
        assertEquals("thaumic_reborn:block/pedestal_top", textures.get("bottom").getAsString());
        assertEquals("thaumic_reborn:block/spa_top", textures.get("top").getAsString());
        assertEquals("thaumic_reborn:block/spa_side", textures.get("side").getAsString());
    }

    @Test
    void machineKeepsOriginalCapacityCadenceAreaAndSideRules() throws Exception {
        String entity = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/ArcaneSpaBlockEntity.java"));
        assertTrue(entity.contains("CAPACITY = 5000"));
        assertTrue(entity.contains("DISPENSE_AMOUNT = 1000"));
        assertTrue(entity.contains("DISPENSE_INTERVAL = 40"));
        assertTrue(entity.contains("for (int x = -2; x <= 2; x++)"));
        assertTrue(entity.contains("for (int z = -2; z <= 2; z++)"));
        assertTrue(entity.contains("level.hasNeighborSignal(pos)"));
        assertTrue(entity.contains("side != Direction.UP"));
        assertTrue(entity.contains("items.get(0).getItem() instanceof BathSaltsItem"));
    }

    private static String sha256(String relative) throws Exception {
        byte[] bytes = Files.readAllBytes(RESOURCES.resolve(relative));
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }
}
