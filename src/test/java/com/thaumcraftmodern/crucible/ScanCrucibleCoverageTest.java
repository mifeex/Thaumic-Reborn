package com.thaumcraftmodern.crucible;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ScanCrucibleCoverageTest {
    private static final Set<String> ITEM_LIKE = Set.of(
            "item", "block", "item_tag", "block_tag"
    );

    @Test
    void everyActiveItemLikeScanCanDissolveIntoAtLeastOneAspect()
            throws Exception {
        Path root = Path.of(
                "src/main/resources/data/thaumic_reborn/thaumcraft/scans"
        );
        boolean sawWispEssence = false;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.filter(file -> file.toString().endsWith(".json")).toList()) {
                JsonObject scan = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                boolean active = !scan.has("inactive") || !scan.get("inactive").getAsBoolean();
                if (!active || !ITEM_LIKE.contains(scan.get("type").getAsString())) continue;
                assertTrue(scan.has("aspects"), path.toString());
                assertFalse(scan.getAsJsonArray("aspects").isEmpty(), path.toString());
                if ("thaumic_reborn:ethereal_essence".equals(
                        scan.get("target").getAsString())) {
                    sawWispEssence = scan.getAsJsonArray("aspects").toString()
                            .contains("auram");
                }
            }
        }
        assertTrue(sawWispEssence);
    }
}
