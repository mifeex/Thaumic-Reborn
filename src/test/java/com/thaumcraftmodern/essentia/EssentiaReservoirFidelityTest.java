package com.thaumcraftmodern.essentia;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EssentiaReservoirFidelityTest {
    @Test
    void reservoirKeepsTc4CapacityPortAndTransferRules() throws Exception {
        String block = read("src/main/java/com/thaumcraftmodern/world/block/EssentiaReservoirBlock.java");
        String tile = read("src/main/java/com/thaumcraftmodern/world/block/entity/EssentiaReservoirBlockEntity.java");
        String gameTest = read("src/main/java/com/thaumcraftmodern/gametest/FirstDiscoveryGameTests.java");

        assertTrue(tile.contains("CAPACITY = 256"));
        assertTrue(tile.contains("SUCTION = 24"));
        assertTrue(tile.contains("side == facing()"));
        assertTrue(tile.contains("ticks % 5 == 0"));
        assertTrue(tile.contains("EssentiaConnections.neighbour("));
        assertTrue(block.contains("context.getClickedFace().getOpposite()"));
        assertTrue(block.contains("player.isShiftKeyDown()"));
        assertTrue(block.contains("implements WandInteractable"));
        assertTrue(block.contains("onWandRightClick(BlockState state"));
        assertTrue(block.contains("hit.getDirection().getOpposite()"));
        assertTrue(block.contains("TubeFacingRules.toggleFacing("));
        assertTrue(block.contains("FULL_CUBE = box(0, 0, 0, 16, 16, 16)"));
        assertTrue(block.contains("getCollisionShape"));
        assertTrue(gameTest.contains(
                "reservoirConnectsToBufferAndTubeThroughSelectedFace"));
    }

    @Test
    void reservoirUsesUnmodifiedClassicShellAndTextures() throws Exception {
        Path original = Path.of("reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft/textures");
        Path port = Path.of("src/main/resources/assets/thaumcraftmodern/textures");
        assertArrayEquals(Files.readAllBytes(original.resolve("models/reservoir.obj")),
                Files.readAllBytes(port.resolve("models/reservoir.obj")));
        assertTrue(Files.size(port.resolve("models/reservoir.png")) > 0);
        assertArrayEquals(Files.readAllBytes(port.resolve("models/reservoir.png")),
                Files.readAllBytes(port.resolve("block/reservoir.png")));
        assertClassicMagentaKeyWasMadeTransparent(
                original.resolve("models/reservoir.png"),
                port.resolve("models/reservoir.png"));
        assertArrayEquals(Files.readAllBytes(original.resolve("blocks/essentiareservoir.png")),
                Files.readAllBytes(port.resolve("block/essentiareservoir.png")));

        String state = read("src/main/resources/assets/thaumcraftmodern/blockstates/essentia_reservoir.json");
        String shell = read("src/main/resources/assets/thaumcraftmodern/models/block/essentia_reservoir_shell.json");
        assertTrue(shell.contains("reservoir.obj"));
        assertTrue(shell.contains("thaumcraftmodern:block/reservoir"));
        assertTrue(shell.contains("[0.5, 0.5, 0]"));
        assertTrue(state.contains("\"facing\": \"north\""));
        assertTrue(state.contains("\"facing\": \"down\""));
        assertTrue(state.contains("\"x\": 90"));

        String item = read("src/main/resources/assets/thaumcraftmodern/models/item/essentia_reservoir.json");
        String renderer = read("src/main/java/com/thaumcraftmodern/client/render/EssentiaReservoirItemRenderer.java");
        String registration = read("src/main/java/com/thaumcraftmodern/registry/ModItems.java");
        assertTrue(item.contains("minecraft:builtin/entity"));
        assertTrue(item.contains("[30, 45, 0]"));
        assertTrue(renderer.contains("renderSingleBlock"));
        assertTrue(renderer.contains("ESSENTIA_RESERVOIR"));
        assertTrue(registration.contains("new EssentiaReservoirItem("));
    }

    private static String read(String path) throws Exception {
        return Files.readString(Path.of(path));
    }

    private static void assertClassicMagentaKeyWasMadeTransparent(
            Path originalPath, Path portPath) throws Exception {
        BufferedImage original = ImageIO.read(originalPath.toFile());
        BufferedImage port = ImageIO.read(portPath.toFile());
        assertEquals(original.getWidth(), port.getWidth());
        assertEquals(original.getHeight(), port.getHeight());
        int keyedPixels = 0;
        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int source = original.getRGB(x, y);
                int red = source >> 16 & 255;
                int green = source >> 8 & 255;
                int blue = source & 255;
                if (red > 180 && blue > 180 && green < 100) {
                    assertEquals(0, port.getRGB(x, y) >>> 24);
                    keyedPixels++;
                } else {
                    assertEquals(source, port.getRGB(x, y));
                }
            }
        }
        assertEquals(2262, keyedPixels);
    }
}
