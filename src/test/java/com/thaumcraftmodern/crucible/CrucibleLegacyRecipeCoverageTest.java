package com.thaumcraftmodern.crucible;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrucibleLegacyRecipeCoverageTest {
    private static final Path LEGACY = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/recipes_legacy"
    );
    private static final Path RUNTIME = Path.of(
            "src/main/resources/data/thaumic_reborn/thaumcraft/crucible_recipes"
    );

    @Test
    void everyOriginalCrucibleRegistrationHasAnAuditedModernDefinition()
            throws Exception {
        int registrations = 0;
        Set<String> modernIds = new LinkedHashSet<>();
        try (var files = Files.list(LEGACY)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".json")).toList()) {
                JsonObject legacy = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                if (!"crucible".equals(legacy.get("legacy_kind").getAsString())) continue;
                registrations++;
                assertTrue(legacy.has("modern_recipe"), path.toString());
                assertFalse(legacy.getAsJsonArray("modern_recipe").isEmpty(), path.toString());
                legacy.getAsJsonArray("modern_recipe").forEach(id ->
                        modernIds.add(id.getAsString().split(":", 2)[1]));
            }
        }
        assertEquals(49, registrations);
        assertEquals(54, modernIds.size());
        for (String id : modernIds) {
            assertTrue(Files.isRegularFile(RUNTIME.resolve(id + ".json")), id);
        }
        assertTrue(modernIds.containsAll(Set.of(
                "balanced_air", "alumentum", "thaumium", "voidseed",
                "altgunpowder", "altweb", "altbonemeal", "etherealbloom",
                "pureiron", "sanesoap"
        )));
    }

    @Test
    void restoredNativeClustersAreActiveWhileUnsupportedOutputsRemainAudited() throws Exception {
        JsonObject nativeIron = recipe("pureiron");
        assertFalse(nativeIron.has("inactive"));

        JsonObject duplication = recipe("altgunpowder");
        assertFalse(duplication.has("inactive"));
    }

    @Test
    void crucibleAndThaumatoriumUseTheSameRegistryAndClassicSpecificityRule()
            throws Exception {
        String registry = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/crucible/CrucibleRecipeRegistry.java"
        ));
        String thaumatorium = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/entity/ThaumatoriumBlockEntity.java"
        ));
        assertTrue(registry.contains("recipe.aspects().size()"));
        assertTrue(registry.contains(".reversed()"));
        assertTrue(thaumatorium.contains("CrucibleRecipeRegistry.all()"));
        assertFalse(thaumatorium.contains("ThaumatoriumRecipeRegistry"));
    }

    private static JsonObject recipe(String id) throws Exception {
        return JsonParser.parseString(
                Files.readString(RUNTIME.resolve(id + ".json"))
        ).getAsJsonObject();
    }
}
