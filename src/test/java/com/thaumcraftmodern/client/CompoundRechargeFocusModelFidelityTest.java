package com.thaumcraftmodern.client;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CompoundRechargeFocusModelFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path ASSETS = ROOT.resolve(
            "src/main/resources/assets/thaumic_reborn");
    private static final Path ORIGINAL = ROOT.resolve(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/assets/thaumcraft");

    @Test
    void modelIsByteExactAfterRequiredModernNamespaceMigration() throws Exception {
        byte[] original = Files.readAllBytes(
                ORIGINAL.resolve("models/block/blockstonedevice_8.json"));
        byte[] expected = new String(original, java.nio.charset.StandardCharsets.UTF_8)
                .replace("\"parent\": \"block/block\"",
                        "\"parent\": \"minecraft:block/block\"")
                .replace("thaumcraft:blocks/", "thaumic_reborn:block/")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertArrayEquals(expected, Files.readAllBytes(
                ASSETS.resolve("models/block/compound_recharge_focus.json")));
    }

    @Test
    void texturesAreByteExactTc4Assets() throws Exception {
        Map<String, String> hashes = Map.of(
                "wandpedestal_focus_bot.png",
                "ec8534989a6ec3d66ffd4715458b25f4f074c418d39f9f2e0629bb83d1512cf4",
                "wandpedestal_focus_side.png",
                "6a02c9a7681db07f2858d77297363c828efbf12a842ccbac703d4628008c213c",
                "wandpedestal_focus_top.png",
                "3473a4597e7c9a0705064d78347a5499c63b6f28047466b0f8310afade83d3fb");
        for (Map.Entry<String, String> entry : hashes.entrySet()) {
            Path modern = ASSETS.resolve("textures/block").resolve(entry.getKey());
            Path original = ORIGINAL.resolve("textures/blocks").resolve(entry.getKey());
            assertArrayEquals(Files.readAllBytes(original), Files.readAllBytes(modern));
            assertEquals(entry.getValue(), sha256(modern));
        }
    }

    private static String sha256(Path path) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(Files.readAllBytes(path)));
    }
}
