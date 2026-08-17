package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class PechClassicAssetFidelityTest {
    private static final Path ORIGINAL = Path.of(
            "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar"
    );

    @Test
    void interactionGuiAndSoundsAreByteExactOriginalAssets()
            throws Exception {
        Map<String, String> assets = Map.of(
                "assets/thaumcraft/textures/gui/gui_pech.png",
                "assets/thaumic_reborn/textures/gui/gui_pech.png",
                "assets/thaumcraft/sounds/pech_charge1.ogg",
                "assets/thaumic_reborn/sounds/pech_charge1.ogg",
                "assets/thaumcraft/sounds/pech_charge2.ogg",
                "assets/thaumic_reborn/sounds/pech_charge2.ogg",
                "assets/thaumcraft/sounds/pech_trade.ogg",
                "assets/thaumic_reborn/sounds/pech_trade.ogg",
                "assets/thaumcraft/sounds/pech_dice.ogg",
                "assets/thaumic_reborn/sounds/pech_dice.ogg"
        );
        try (ZipFile original = new ZipFile(ORIGINAL.toFile())) {
            for (Map.Entry<String, String> asset : assets.entrySet()) {
                try (InputStream source = original.getInputStream(
                        original.getEntry(asset.getKey())
                )) {
                    assertArrayEquals(
                            source.readAllBytes(),
                            Files.readAllBytes(Path.of(
                                    "src/main/resources",
                                    asset.getValue()
                            )),
                            asset.getValue()
                    );
                }
            }
        }
    }
}
