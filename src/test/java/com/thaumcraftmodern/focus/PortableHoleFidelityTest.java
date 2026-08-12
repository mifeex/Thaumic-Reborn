package com.thaumcraftmodern.focus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class PortableHoleFidelityTest {
    private static final Path ROOT = Path.of("src/main");

    @Test
    void castBuildsAThreeByThreePlaneForEveryOpenableDepth()
            throws Exception {
        String service = read("java/com/thaumcraftmodern/focus/WandFocusService.java");
        String hole = read("java/com/thaumcraftmodern/world/block/entity/"
                + "TemporaryHoleBlockEntity.java");
        assertTrue(hole.contains("for (int first = -1; first <= 1; first++)"));
        assertTrue(hole.contains("for (int second = -1; second <= 1; second++)"));
        assertTrue(hole.contains("OPENING_LAYERS_PER_TICK = 4"));
        assertTrue(hole.contains("opened < OPENING_LAYERS_PER_TICK"));
        assertTrue(service.contains("10 * depth"));
    }

    @Test
    void supportBlocksAreSwappedAndRestoredWithoutNeighbourUpdates()
            throws Exception {
        String service = read("java/com/thaumcraftmodern/focus/WandFocusService.java");
        String hole = read("java/com/thaumcraftmodern/world/block/entity/"
                + "TemporaryHoleBlockEntity.java");
        String block = read("java/com/thaumcraftmodern/world/block/"
                + "TemporaryHoleBlock.java");
        assertTrue(service.contains("createTunnelCell("));
        assertTrue(hole.contains("setBlockWithoutNeighborUpdates("));
        assertTrue(hole.contains("Block.UPDATE_KNOWN_SHAPE"));
        assertTrue(hole.contains("Block.UPDATE_SUPPRESS_DROPS"));
        assertTrue(block.contains("getBlockSupportShape("));
        assertTrue(block.contains("return Shapes.block();"));
    }

    @Test
    void originalTunnelTextureAndSoundsDriveTheOpening() throws Exception {
        Path original = Path.of("reference/Thaumcraft-4.2-FOREVA-master/"
                + "src/main/resources/assets/thaumcraft/textures/misc/tunnel.png");
        Path port = ROOT.resolve("resources/assets/thaumcraftmodern/"
                + "textures/misc/tunnel.png");
        assertArrayEquals(Files.readAllBytes(original), Files.readAllBytes(port));
        Path originalField = Path.of("reference/Thaumcraft-4.2-FOREVA-master/"
                + "src/main/resources/assets/thaumcraft/textures/misc/"
                + "particlefield.png");
        Path portField = ROOT.resolve("resources/assets/thaumcraftmodern/"
                + "textures/misc/particlefield.png");
        assertArrayEquals(
                Files.readAllBytes(originalField),
                Files.readAllBytes(portField)
        );

        String renderer = read("java/com/thaumcraftmodern/client/render/"
                + "TemporaryHoleBlockEntityRenderer.java");
        String renderTypes = read("java/com/thaumcraftmodern/client/render/"
                + "TemporaryHoleRenderTypes.java");
        String service = read("java/com/thaumcraftmodern/focus/WandFocusService.java");
        assertTrue(renderTypes.contains("textures/misc/tunnel.png"));
        assertTrue(renderTypes.contains("textures/misc/particlefield.png"));
        assertTrue(renderer.contains("for (int layer = 1; layer < 16; layer++)"));
        assertTrue(renderTypes.contains("ADDITIVE_TRANSPARENCY"));
        assertTrue(service.contains("SoundEvents.ENDERMAN_TELEPORT"));
        assertTrue(service.contains("ModSounds.WAND_FAIL.get()"));
    }

    private static String read(String path) throws Exception {
        return Files.readString(ROOT.resolve(path));
    }
}
