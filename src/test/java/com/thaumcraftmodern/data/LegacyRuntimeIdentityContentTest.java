package com.thaumcraftmodern.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LegacyRuntimeIdentityContentTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path SCAN_ROOT = ROOT.resolve(
            "src/main/resources/data/thaumic_reborn/thaumcraft/scans"
    );
    private static final Path RESEARCH = ROOT.resolve(
            "src/main/resources/data/thaumic_reborn/thaumcraft/research"
    );
    private static final Path REPORT = ROOT.resolve(
            "data/legacy_tc4_4_2_3_5/modern_migration/runtime_ids.json"
    );

    @Test
    void allActiveScanResourcesHaveUniqueModernIdentities() throws IOException {
        int unresolved = 0;
        Set<String> activeKeys = new HashSet<>();
        Set<String> activeRecipeDerivedVanillaBlocks = new HashSet<>();
        try (var paths = Files.walk(SCAN_ROOT)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json"))
                    .toList()) {
                JsonObject scan = json(path);
                String type = scan.get("type").getAsString();
                String target = scan.get("target").getAsString();
                boolean inactive = scan.has("inactive")
                        && scan.get("inactive").getAsBoolean();
                if (type.startsWith("legacy_")) {
                    unresolved++;
                    assertTrue(inactive, () -> "active unresolved scan: " + path);
                    continue;
                }
                assertFalse(
                        target.contains("field_")
                                || target.contains("ConfigItems.")
                                || target.contains("ConfigBlocks.")
                                || target.startsWith("new ItemStack")
                                || target.matches("[a-z]+[A-Z].*"),
                        () -> "legacy runtime target remains in " + path + ": " + target
                );
                if (!inactive) {
                    assertTrue(
                            activeKeys.add(type + ":" + target),
                            () -> "duplicate active scan identity in " + path
                    );
                    if ("block".equals(type)
                            && target.startsWith("minecraft:")
                            && scan.has("legacy")
                            && "recipe_derived_modifier".equals(
                                    scan.getAsJsonObject("legacy")
                                            .get("registration_mode")
                                            .getAsString()
                            )) {
                        activeRecipeDerivedVanillaBlocks.add(target);
                    }
                }
            }
        }
        assertTrue(
                activeKeys.contains("block:thaumic_reborn:ancient_stone"),
                "the canonical Ancient Stone scan is not active"
        );
        assertTrue(
                activeKeys.contains("block:minecraft:grass_block"),
                "legacy Blocks.grass must map to the modern grass block, "
                        + "not the short-grass plant"
        );
        assertTrue(
                activeKeys.contains("block:minecraft:grass"),
                "the short-grass scan must recover after the false collision "
                        + "with legacy Blocks.grass is removed"
        );
        assertTrue(
                activeKeys.contains("block_tag:thaumic_reborn:sandstone_equivalents"),
                "the shared recipe-derived sandstone scan is not active"
        );
        assertEquals(
                25,
                activeRecipeDerivedVanillaBlocks.size(),
                "all migrated recipe-derived vanilla block scans must be active"
        );
        JsonObject report = json(REPORT);
        assertEquals(
                report.getAsJsonArray("unresolved_without_1_20_1_equivalent").size(),
                unresolved
        );
        assertEquals(6, unresolved);
    }

    @Test
    void liveResearchFieldsContainNoLegacyRegistryExpressions()
            throws IOException {
        try (var paths = Files.walk(RESEARCH)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json"))
                    .toList()) {
                JsonObject research = json(path);
                StringBuilder live = new StringBuilder(
                        research.has("icon")
                                ? research.get("icon").getAsString()
                                : ""
                );
                if (research.has("pages")) {
                    research.getAsJsonArray("pages").forEach(element -> {
                        JsonObject page = element.getAsJsonObject();
                        if (page.has("recipe")) {
                            live.append(page.get("recipe").getAsString());
                        }
                    });
                }
                assertFalse(
                        live.toString().contains("field_")
                                || live.toString().contains("ConfigItems.")
                                || live.toString().contains("ConfigBlocks.")
                                || live.toString().contains("new ItemStack"),
                        () -> "legacy registry expression leaked into live research: "
                                + path
                );
            }
        }
    }

    @Test
    void migrationUpdatesResearchIconsFromRuntimeRecipeResults()
            throws IOException {
        JsonObject bellows = json(RESEARCH.resolve("legacy/bellows.json"));
        assertEquals(
                "thaumic_reborn:arcane_bellows",
                bellows.get("icon").getAsString()
        );
        JsonObject focus = json(RESEARCH.resolve("legacy/focusfire.json"));
        assertEquals(
                "thaumic_reborn:focus_fire",
                focus.get("icon").getAsString()
        );
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}
