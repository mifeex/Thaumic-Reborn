package com.thaumcraftmodern.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DeconstructionTableFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );
    private static final Path ORIGINAL_ASSETS = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                    + "assets/thaumcraft"
    );
    private static final Path JAVA = Path.of("src/main/java");

    @Test
    void guiAndModelTexturesAreByteForByteOriginal() throws Exception {
        assertOriginalAsset(
                "textures/gui/gui_decontable.png",
                "6c26e086c30eab750571a42933b95d6a83adf315161df8f0388ad61594224d40"
        );
        assertOriginalAsset(
                "textures/models/decontable.png",
                "0a916f79cf770d0502ad42a13200babe8b37a236c6c42c63c6056b1085962305"
        );
    }

    @Test
    void modelUsesAllSixOriginalCubesAndTextureCoordinates() throws Exception {
        String model = source(
                "com/thaumcraftmodern/client/render/DeconstructionTableModel.java"
        );
        assertTrue(model.contains("LayerDefinition.create(mesh, 128, 64)"));
        assertTrue(model.contains(".texOffs(0, 0).mirror()"));
        assertTrue(model.contains(
                ".addBox(-8.0F, 0.0F, -8.0F, 16.0F, 8.0F, 16.0F)"
        ));
        assertTrue(model.contains(".texOffs(0, 32).mirror()"));
        assertTrue(model.contains(
                ".addBox(-8.0F, 12.0F, -8.0F, 16.0F, 4.0F, 16.0F)"
        ));
        assertEquals(4, occurrences(model, ".texOffs(72, 0).mirror()"));
        assertTrue(model.contains(
                ".addBox(3.0F, 8.0F, -7.0F, 4.0F, 4.0F, 4.0F)"
        ));
        assertTrue(model.contains(
                ".addBox(-7.0F, 8.0F, 3.0F, 4.0F, 4.0F, 4.0F)"
        ));
        assertTrue(model.contains(
                ".addBox(3.0F, 8.0F, 3.0F, 4.0F, 4.0F, 4.0F)"
        ));
        assertTrue(model.contains(
                ".addBox(-7.0F, 8.0F, -7.0F, 4.0F, 4.0F, 4.0F)"
        ));
    }

    @Test
    void guiKeepsOriginalDimensionsSlotsProgressAndAspectHitbox() throws Exception {
        String screen = source(
                "com/thaumcraftmodern/client/screen/DeconstructionTableScreen.java"
        );
        String menu = source(
                "com/thaumcraftmodern/world/menu/DeconstructionTableMenu.java"
        );
        assertTrue(screen.contains("imageWidth = 176"));
        assertTrue(screen.contains("imageHeight = 166"));
        assertTrue(screen.contains("private static final int ASPECT_X = 64"));
        assertTrue(screen.contains("private static final int ASPECT_Y = 48"));
        assertTrue(screen.contains("private static final int ASPECT_SIZE = 16"));
        assertTrue(screen.contains("leftPos + 93"));
        assertTrue(screen.contains("topPos + 15 + 46 - progress"));
        assertTrue(screen.contains("176,\n                    46 - progress"));
        assertTrue(screen.contains("9,\n                    progress"));
        assertTrue(menu.contains("new Slot(table, 0, 64, 16)"));
        assertTrue(menu.contains("84 + row * 18"));
        assertTrue(menu.contains("8 + column * 18,\n                    142"));
        assertTrue(screen.contains("TC4 intentionally draws no foreground labels"));
    }

    @Test
    void tabletopPresentationUsesOriginalScaleAndRequestedCalibration()
            throws Exception {
        String renderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "DeconstructionTableBlockEntityRenderer.java"
        );
        String itemRenderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "DeconstructionTableItemRenderer.java"
        );
        assertTrue(renderer.contains("THAUMOMETER_SCALE = 0.8F"));
        assertTrue(renderer.contains("Axis.XP.rotationDegrees(-90.0F)"));
        assertTrue(renderer.contains("Axis.YP.rotationDegrees(180.0F)"));
        assertTrue(renderer.contains("THAUMOMETER_TABLE_Y = 1.026D"));
        assertTrue(renderer.contains("1.15D + Math.sin("));
        assertTrue(renderer.contains("poses.scale(0.75F, 0.75F, 0.75F)"));
        assertTrue(renderer.contains("float radius = 0.12F"));
        assertTrue(itemRenderer.contains(
                "displayContext == ItemDisplayContext.GUI"
        ));
        assertTrue(itemRenderer.contains(
                "DeconstructionTableBlockEntityRenderer.renderThaumometer("
        ));
    }

    private static void assertOriginalAsset(
            String relative,
            String expectedSha256
    ) throws Exception {
        byte[] modern = Files.readAllBytes(ASSETS.resolve(relative));
        byte[] original = Files.readAllBytes(ORIGINAL_ASSETS.resolve(relative));
        assertArrayEquals(original, modern);
        assertEquals(
                expectedSha256,
                HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256").digest(modern)
                )
        );
    }

    private static int occurrences(String source, String needle) {
        return (source.length() - source.replace(needle, "").length())
                / needle.length();
    }

    private static String source(String relative) throws Exception {
        return Files.readString(JAVA.resolve(relative));
    }
}
