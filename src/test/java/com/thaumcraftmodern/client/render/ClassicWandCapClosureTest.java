package com.thaumcraftmodern.client.render;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ClassicWandCapClosureTest {
    private static final Path TEXTURES = Path.of(
            "src/main/resources/assets/thaumic_reborn/textures/item");

    @Test
    void everyClassicCapHasAnOpaquePatchForTheClosingPlate()
            throws Exception {
        for (String material : new String[]{
                "copper", "gold", "iron", "silver", "thaumium", "void"
        }) {
            var image = ImageIO.read(TEXTURES.resolve(
                    "wand_cap_" + material + "_model.png").toFile());
            for (int y = 2; y < 4; y++) {
                for (int x = 3; x < 5; x++) {
                    assertEquals(255, image.getRGB(x, y) >>> 24,
                            material + " cap closure contains transparency");
                }
            }
        }
    }

    @Test
    void bothWandEndsUseTheSharedClosingPlate() throws Exception {
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ClassicWandModel.java"));
        assertTrue(model.contains(
                "-1.01F / 16.0F, -1.0F"));
        assertTrue(model.contains(
                "21.01F / 16.0F, 1.0F"));
        assertTrue(model.contains("renderCapClosure(poseStack.last()"));
    }

    @Test
    void modelPassesItsDeclaredTextureDimensionsToTheBakedLayer()
            throws Exception {
        String model = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "ClassicWandModel.java"));
        assertTrue(model.contains(
                "LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT)"));
    }
}
