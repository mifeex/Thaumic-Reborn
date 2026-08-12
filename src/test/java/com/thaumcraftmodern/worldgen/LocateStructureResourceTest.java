package com.thaumcraftmodern.worldgen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LocateStructureResourceTest {
    private static final Path DATA = Path.of(
            "src/main/resources/data/thaumcraftmodern"
    );

    @Test
    void everyLegacySiteHasStructureAndPlacementData() throws IOException {
        for (LegacyStructureKind kind : LegacyStructureKind.values()) {
            String name = kind.serializedName();
            Path structure = DATA.resolve(
                    "worldgen/structure/" + name + ".json"
            );
            assertTrue(Files.isRegularFile(structure), name);
            String structureJson = Files.readString(structure);
            assertTrue(
                    structureJson.contains(
                            "\"type\": \"thaumcraftmodern:"
                                    + "legacy_world_structure\""
                    ),
                    name
            );
            assertTrue(
                    structureJson.contains("\"kind\": \"" + name + "\""),
                    name
            );
        }

        Path sets = DATA.resolve("worldgen/structure_set");
        String allSets;
        try (var files = Files.list(sets)) {
            allSets = files
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .reduce("", String::concat);
        }
        for (LegacyStructureKind kind : LegacyStructureKind.values()) {
            if (kind.isVillageBuilding()) {
                assertFalse(
                        allSets.contains(
                                "\"structure\": \"thaumcraftmodern:"
                                        + kind.serializedName() + "\""
                        ),
                        kind.serializedName()
                );
                continue;
            }
            assertTrue(
                    allSets.contains(
                            "\"structure\": \"thaumcraftmodern:"
                                    + kind.serializedName() + "\""
                    ),
                    kind.serializedName()
            );
        }
    }

    @Test
    void classicProfessionBuildingsUseVanillaVillagePools()
            throws IOException {
        String injector = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVillagePoolInjector.java"
        ));
        assertTrue(injector.contains("villagePool(\"plains\")"));
        assertTrue(injector.contains("villagePool(\"desert\")"));
        assertTrue(injector.contains("villagePool(\"savanna\")"));
        assertTrue(injector.contains("villagePool(\"snowy\")"));
        assertTrue(injector.contains("villagePool(\"taiga\")"));
        assertTrue(injector.contains("LegacyStructureKind.WIZARD_TOWER"));
        assertTrue(injector.contains("LegacyStructureKind.BANKER_HOME"));
        assertEquals(17, LegacyVillagePoolInjector.WIZARD_TOWER_WEIGHT);
        assertEquals(29, LegacyVillagePoolInjector.BANKER_HOME_WEIGHT);
    }

    @Test
    void standaloneStructureDensityUsesConfiguredClassicMultipliers()
            throws IOException {
        Map<LegacyStructureKind, PlacementExpectation> expected = Map.of(
                LegacyStructureKind.ANCIENT_MOUND,
                new PlacementExpectation("ancient_mounds", 11),
                LegacyStructureKind.ELDRITCH_RING,
                new PlacementExpectation("eldritch_rings", 7),
                LegacyStructureKind.HILLTOP_STONES,
                new PlacementExpectation("hilltop_stones", 5),
                LegacyStructureKind.AURA_TOTEM,
                new PlacementExpectation("aura_totems", 17)
        );

        for (var entry : expected.entrySet()) {
            JsonObject placement = JsonParser.parseString(
                    Files.readString(DATA.resolve(
                            "worldgen/structure_set/"
                                    + entry.getValue().fileName()
                                    + ".json"
                    ))
            ).getAsJsonObject().getAsJsonObject("placement");
            int spacing = placement.get("spacing").getAsInt();
            double frequency = placement.get("frequency").getAsDouble();
            assertEquals(entry.getValue().spacing(), spacing);
            double multiplier = entry.getKey()
                    == LegacyStructureKind.ELDRITCH_RING ? 1.0D : 1.15D;
            assertEquals(
                    multiplier / entry.getKey().rarity(),
                    frequency / (spacing * spacing),
                    1.0E-12D,
                    entry.getKey().serializedName()
            );
        }
    }

    @Test
    void standaloneStructuresUseClassicChecksPlusSurfaceWaterGate()
            throws IOException {
        String feature = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyStructuresFeature.java"
        ));
        String policy = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "StructureSitePolicy.java"
        ));
        String piece = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyWorldStructurePiece.java"
        ));
        assertTrue(feature.contains("isClassicSpawnSurface("));
        assertTrue(feature.contains("hasDrySurfaceColumns("));
        assertTrue(feature.contains("isDryTotemReplaceable("));
        assertFalse(feature.substring(
                feature.indexOf("static boolean placeRegistered("),
                feature.indexOf("static boolean placeVillageBuilding(")
        ).contains("hasDrySupportedFloor("));
        assertTrue(feature.contains("hasDrySupportedFloor("));
        assertTrue(policy.contains("!floorFluid.isEmpty()"));
        assertTrue(policy.contains("!supportFluid.isEmpty()"));
        assertTrue(policy.contains("supportState.isAir()"));
        assertTrue(policy.contains("isFaceSturdy("));
        assertTrue(piece.contains("Heightmap.Types.OCEAN_FLOOR_WG"));
        assertFalse(piece.contains("Heightmap.Types.WORLD_SURFACE_WG"));
    }

    @Test
    void oldFeatureBiomeModifierCannotGenerateDuplicates() {
        assertFalse(Files.exists(DATA.resolve(
                "forge/biome_modifier/add_legacy_structures.json"
        )));
    }

    @Test
    void locateCandidateSelectionNeverSamplesTerrain() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyWorldStructure.java"
        ));
        assertFalse(source.contains(".getBaseHeight("));
        assertFalse(source.contains(".getBaseColumn("));
        assertTrue(source.contains(".getSeaLevel()"));
    }

    @Test
    void thaumcraftLocateIdsUseRealPlacementOverrides()
            throws IOException {
        String commands = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/command/"
                        + "ThaumcraftCommands.java"
        ));
        String detector = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyStructureMarkerDetector.java"
        ));
        assertTrue(commands.contains("registerMarkerLocateOverrides"));
        assertTrue(commands.contains("LegacyStructureKind.values()"));
        assertTrue(commands.contains("LegacyVillageBuildingSearch.find"));
        assertTrue(commands.contains("LegacyStructureMarkerSearch.find"));
        assertTrue(commands.contains(":banker_house"));
        assertTrue(detector.contains("isMoundGuardianSpawner()"));
        assertTrue(detector.contains("ELDRITCH_ALTAR_PART"));
        assertTrue(detector.contains("isWispSpawner(spawner)"));
        assertTrue(detector.contains("hasTotemColumn"));
        assertTrue(detector.contains("classify(chunk, blockEntity)"));
        assertTrue(detector.contains("chunk.getBlockState("));
        assertTrue(detector.contains("chunk.getBlockEntity("));
        assertFalse(detector.contains("level.getBlockState("));
        assertFalse(detector.contains("level.getBlockEntity("));
    }

    @Test
    void villageLocateChecksPlannedPiecesAndSuccessfulPlacementMarkers()
            throws IOException {
        String search = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyVillageBuildingSearch.java"
        ));
        String markerIndex = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyStructureMarkerIndex.java"
        ));
        String feature = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyStructuresFeature.java"
        ));
        assertTrue(search.contains("StructureTags.VILLAGE"));
        assertTrue(search.contains("PoolElementStructurePiece"));
        assertTrue(search.contains("LegacyVillagePoolElement legacy"));
        assertTrue(search.contains("ChunkStatus.FULL"));
        assertTrue(search.contains(".filter(marker::equals)"));
        assertTrue(search.contains("hasPhysicalSignature("));
        assertTrue(search.contains("Blocks.GLOWSTONE"));
        assertTrue(search.contains("Blocks.IRON_BARS"));
        assertTrue(markerIndex.contains("EnumSet.allOf("));
        assertTrue(markerIndex.contains("public void record("));
        assertTrue(feature.contains("LegacyStructureMarkerIndex.get("));
        assertTrue(feature.contains(".record("));
    }

    @Test
    void moundUsesDedicatedSurfaceBiomeTag() throws IOException {
        String structure = Files.readString(DATA.resolve(
                "worldgen/structure/ancient_mound.json"
        ));
        assertTrue(structure.contains(
                "\"biomes\": \"#thaumcraftmodern:has_ancient_mounds\""
        ));
        assertTrue(Files.isRegularFile(DATA.resolve(
                "tags/worldgen/biome/has_ancient_mounds.json"
        )));
    }

    @Test
    void worldgenSpawnerSetupCannotRequestAChunk() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/"
                        + "LegacyStructuresFeature.java"
        ));
        assertFalse(source.contains(
                "entityType,\n                    level.getLevel(),"
        ));
        assertTrue(source.contains(
                "entityType,\n                    null,"
        ));
    }

    private record PlacementExpectation(String fileName, int spacing) {
    }
}
