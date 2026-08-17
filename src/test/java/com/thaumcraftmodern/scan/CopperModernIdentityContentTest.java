package com.thaumcraftmodern.scan;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CopperModernIdentityContentTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCANS = ROOT.resolve(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans/legacy"
    );

    @Test
    void copperFormsUseCurrentRegistryIdentities() throws IOException {
        JsonObject legacyNugget = json(SCANS.resolve(
                "object_032_nuggetcopper.json"));
        assertEquals("minecraft:copper_ingot",
                legacyNugget.get("target").getAsString());
        assertTrue(legacyNugget.get("inactive").getAsBoolean());
        assertScan(
                "object_033_ingotcopper.json",
                "item",
                "minecraft:copper_ingot"
        );
        assertScan(
                "object_035_raw_copper.json",
                "item",
                "minecraft:raw_copper"
        );
        assertScan(
                "object_035_orecopper.json",
                "block",
                "minecraft:copper_ore"
        );
        assertScan(
                "object_035_deepslate_copper_ore.json",
                "block",
                "minecraft:deepslate_copper_ore"
        );
    }

    @Test
    void copperCapUsesVanillaIngotWhileTc4AlchemyKeepsItsCopperNugget()
            throws IOException {
        JsonObject recipe = json(ROOT.resolve(
                "src/main/resources/data/thaumic_reborn/recipes/"
                        + "wand_cap_copper.json"
        ));
        assertEquals(
                "minecraft:copper_ingot",
                recipe.getAsJsonObject("key")
                        .getAsJsonObject("N")
                        .get("item")
                        .getAsString()
        );
        assertTrue(Files.exists(ROOT.resolve(
                "src/main/resources/data/forge/tags/items/nuggets/copper.json")));
        assertTrue(Files.exists(ROOT.resolve(
                "src/main/resources/assets/thaumic_reborn/models/item/"
                        + "copper_nugget.json")));
    }

    private static void assertScan(String file, String type, String target)
            throws IOException {
        JsonObject scan = json(SCANS.resolve(file));
        assertEquals(type, scan.get("type").getAsString());
        assertEquals(target, scan.get("target").getAsString());
        assertFalse(scan.get("inactive").getAsBoolean());
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
