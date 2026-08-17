package com.thaumcraftmodern.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ModernMaterialScanFidelityTest {
    private static final Path SCANS = Path.of("").toAbsolutePath().resolve(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans"
    );

    @Test
    void stainedGlassAndPanesGainOneSensusOverGlass() throws IOException {
        assertScan("legacy/object_071_new_itemstack_blocks.field_150399_cn_1_32767.json",
                "block_tag", "forge:stained_glass", Map.of("vitreus", 1, "sensus", 1));
        assertScan("stained_glass_panes.json", "block_tag", "forge:stained_glass_panes",
                Map.of("vitreus", 1, "sensus", 1));
    }

    @Test
    void amethystUsesExplicitShardAndFlooredBlockValues() throws IOException {
        assertScan("amethyst_shard.json", "item", "minecraft:amethyst_shard",
                Map.of("sensus", 2, "lucrum", 4, "vitreus", 2));
        assertScan("amethyst_block.json", "block", "minecraft:amethyst_block",
                Map.of("sensus", 3, "lucrum", 6, "vitreus", 3));
    }

    private static void assertScan(String file, String type, String target,
                                   Map<String, Integer> expected) throws IOException {
        JsonObject value = JsonParser.parseString(Files.readString(SCANS.resolve(file)))
                .getAsJsonObject();
        assertEquals(type, value.get("type").getAsString());
        assertEquals(target, value.get("target").getAsString());
        Map<String, Integer> actual = new LinkedHashMap<>();
        value.getAsJsonArray("aspects").forEach(element -> {
            JsonObject aspect = element.getAsJsonObject();
            actual.put(aspect.get("id").getAsString(), aspect.get("amount").getAsInt());
        });
        assertEquals(expected, actual);
    }
}
