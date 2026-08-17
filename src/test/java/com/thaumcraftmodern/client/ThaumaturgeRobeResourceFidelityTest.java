package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ThaumaturgeRobeResourceFidelityTest {
    private static final Path ASSETS = Path.of(
            "src/main/resources/assets/thaumic_reborn"
    );

    @Test
    void originalOverlayAndWornTexturesArePreservedByteForByte()
            throws Exception {
        Map<String, String> expected = Map.ofEntries(
                Map.entry("textures/item/thaumaturge_boots_overlay.png",
                        "b0efd723268e0ddcc674a1fd502b0b7451335abbade8cbec0c7e5d376c857893"),
                Map.entry("textures/item/thaumaturge_leggings_overlay.png",
                        "993c1dc086337242f7daf899b25895859d11a7ddf07286c70bb65296520e4aab"),
                Map.entry("textures/item/thaumaturge_robe_overlay.png",
                        "47b3c4ea1356bc533bcb5ad2ab0a1cdbde527056393d10987ffdf8a3567215d5"),
                Map.entry("textures/models/robes_1.png",
                        "e0e1c4923c17f07f984af17790c80b2afd2523b4b5bc1536aa2d50dcc1eee47e"),
                Map.entry("textures/models/robes_1_overlay.png",
                        "2f0cf93617377f85c18fed283cf687bdf5524549f70713239c6bdcceac9b54d5"),
                Map.entry("textures/models/robes_2.png",
                        "aad4f2dfcf1e255aa8719acf5b87d765bb3416e6a741af920a779d0f0550f526"),
                Map.entry("textures/models/robes_2_overlay.png",
                        "e8b7eabad1e8325cf3a45dc615c276eab097dc27be3b6a252380183f331d4307")
        );
        for (Map.Entry<String, String> asset : expected.entrySet()) {
            assertEquals(asset.getValue(), sha256(ASSETS.resolve(asset.getKey())));
        }
    }

    @Test
    void everyRobeItemModelUsesTintedBaseAndUntintedOverlay()
            throws Exception {
        for (String item : new String[]{
                "thaumaturge_robe",
                "thaumaturge_leggings",
                "thaumaturge_boots"
        }) {
            String model = Files.readString(
                    ASSETS.resolve("models/item/" + item + ".json")
            );
            assertTrue(model.contains(
                    "\"layer0\": \"thaumic_reborn:item/" + item + "\""
            ));
            assertTrue(model.contains(
                    "\"layer1\": \"thaumic_reborn:item/"
                            + item + "_overlay\""
            ));
        }
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
}
