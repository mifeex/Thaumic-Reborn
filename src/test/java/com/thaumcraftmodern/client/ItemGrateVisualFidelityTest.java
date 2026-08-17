package com.thaumcraftmodern.client;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ItemGrateVisualFidelityTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test void grateUsesByteExactTc4TexturesAndSeparateInventoryGeometry() throws Exception {
        Path modern = ROOT.resolve("src/main/resources/assets/thaumic_reborn");
        Path original = ROOT.resolve("reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                + "assets/thaumcraft/textures/blocks");
        for (String texture : new String[]{"grate.png", "grate_hatch.png"})
            assertArrayEquals(Files.readAllBytes(original.resolve(texture)),
                    Files.readAllBytes(modern.resolve("textures/block/" + texture)), texture);

        String open = Files.readString(modern.resolve("models/block/item_grate_open.json"));
        String closed = Files.readString(modern.resolve("models/block/item_grate_closed.json"));
        String item = Files.readString(modern.resolve("models/item/item_grate.json"));
        assertTrue(open.contains("thaumic_reborn:block/grate"));
        assertTrue(closed.contains("thaumic_reborn:block/grate_hatch"));
        assertTrue(item.contains("[0, 8.2, 0]"));
        assertTrue(item.contains("thaumic_reborn:block/grate_hatch"));
    }
}
