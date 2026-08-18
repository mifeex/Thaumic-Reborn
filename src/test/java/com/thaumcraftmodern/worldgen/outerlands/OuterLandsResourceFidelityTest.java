package com.thaumcraftmodern.worldgen.outerlands;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class OuterLandsResourceFidelityTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path ORIGINAL = Path.of(
            "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                    + "assets/thaumcraft/textures"
    );

    @Test
    void dimensionUsesVoidGeneratorAndEldritchBiome() throws IOException {
        JsonObject dimension = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(
                        "data/thaumic_reborn/dimension/outer_lands.json"
                )
        )).getAsJsonObject();
        JsonObject generator = dimension.getAsJsonObject("generator");
        assertTrue(generator.get("type").getAsString()
                .equals("thaumic_reborn:outer_lands"));
        assertTrue(generator.getAsJsonObject("biome_source")
                .get("biome").getAsString()
                .equals("thaumic_reborn:eldritch"));

        JsonObject type = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(
                        "data/thaumic_reborn/dimension_type/outer_lands.json"
                )
        )).getAsJsonObject();
        assertFalse(type.get("has_skylight").getAsBoolean());
        assertFalse(type.get("natural").getAsBoolean());
        assertTrue(type.get("has_ceiling").getAsBoolean());
    }

    @Test
    void masonryAndPortalTexturesAreCopiedFromTc4() throws IOException {
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/es_1.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/ancient_stone.png"
                ))
        );
        for (int index = 2; index <= 8; index++) {
            assertArrayEquals(
                    Files.readAllBytes(ORIGINAL.resolve(
                            "blocks/es_" + index + ".png"
                    )),
                    Files.readAllBytes(RESOURCES.resolve(
                            "assets/thaumic_reborn/textures/block/"
                                    + "ancient_stone_" + index + ".png"
                    ))
            );
        }
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve(
                        "misc/eldritch_portal.png"
                )),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/"
                                + "outer_lands_portal.png"
                ))
        );
        BufferedImage portal = ImageIO.read(RESOURCES.resolve(
                "assets/thaumic_reborn/textures/block/"
                        + "outer_lands_portal.png"
        ).toFile());
        assertEquals(2048, portal.getWidth());
        assertEquals(128, portal.getHeight());

        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/deco_2.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/eldritch_lock_center.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/deco_3.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/eldritch_door.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("models/eldritch_cube.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/models/eldritch_cube.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("models/crabvent.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/models/crabvent.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("models/crabvent.obj")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/models/crabvent.obj"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("models/obelisk_cap_2.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/models/obelisk_cap_2.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/es_p.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/eldritch_pedestal.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/es_5.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/ancient_rock_classic.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/es_6.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/ancient_nospawn_classic.png"
                ))
        );
        String rockOneModel = Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/ancient_rock_1.json"
        ));
        String rockTwoModel = Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/ancient_rock_2.json"
        ));
        assertTrue(rockOneModel.contains("thaumic_reborn:block/ancient_rock_1"));
        assertTrue(rockTwoModel.contains("thaumic_reborn:block/ancient_rock_2"));
        assertFalse(rockOneModel.contains("ancient_rock_classic"));
        assertFalse(rockTwoModel.contains("ancient_nospawn_classic"));
    }

    @Test
    void labyrinthUsesTc4PassageProfileAndDedicatedRoomGenerators()
            throws IOException {
        String generator = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsLabyrinthGenerator.java"
        ));
        assertTrue(generator.contains("{1,8,2,5,9,9,9,6,2,8,1}"));
        assertTrue(generator.contains("generateClassicPassage(level"));
        assertTrue(generator.contains("generateClassicPortalRoom(level"));
        assertTrue(generator.contains("generateClassicKeyRoom(level"));
        assertTrue(generator.contains("generateClassicNestRoom(level"));
        assertTrue(generator.contains("generateClassicLibraryRoom(level"));
        assertTrue(generator.contains("case 14 -> generateWebRoom(level"));
        assertTrue(generator.contains("OuterLandsMindSpiderSpawners.configure(level, center)"));
        assertTrue(generator.contains("OuterLandsCrabVents.populate(level, chunk, worldSeed, cell)"));
        assertTrue(generator.contains(
                "ModBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState()"
        ));
        assertTrue(generator.contains(
                "ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState()"
        ));
        assertFalse(generator.contains(
                "ModBlocks.ARCANE_PEDESTAL.get().defaultBlockState()"
        ));
        assertTrue(generator.contains("StairBlock.FACING, wallDirection"));
        assertTrue(generator.contains("placeClassicPassageCornerStairs(level"));
        assertTrue(generator.contains("passageCornerPair(level, x + 5, z + 5"));
        assertTrue(generator.contains("Block.UPDATE_ALL"));
        assertFalse(generator.contains(
                "for (int across : new int[]{4, 12})"
        ));
        assertTrue(generator.contains(
                "for (int across = 4; across <= 12; across++)"
        ));
        assertTrue(generator.contains(
                "ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState()"
        ));
        assertFalse(generator.contains(
                "across == 4 || across == 12"
        ));
        assertTrue(generator.contains(
                "TC4 sandwich: masonry, one void layer, outer seal."
        ));
        assertTrue(generator.contains(
                "passageSidePos(x, z, side, w + 3, y, 0)"
        ));
        assertTrue(generator.contains(
                "passageSidePos(x, z, side, w + 3, y, 1)"
        ));
        assertTrue(generator.contains(
                "passageSidePos(x, z, side, w + 3, y, 2)"
        ));
        assertTrue(generator.contains("boolean[][] stairBlocked"));
        assertTrue(generator.contains(
                "OuterLandsStairTopology.isAncientWall("
        ));
        assertTrue(generator.contains("makePreviousConnectionStairCorner("));
        assertTrue(generator.contains("repairClassicConnectionStairTips("));
        assertTrue(generator.contains("blocksConnectionStair("));
        assertTrue(generator.contains(
                "hasStairBacking(level, target, stair)"
        ));
        assertFalse(generator.contains("classicState(10, side.getOpposite()"));
        assertFalse(generator.contains("classicState(11, side.getOpposite()"));
        assertFalse(generator.contains("wallDirection.getOpposite()"));
        assertTrue(generator.contains(
                "case 19 -> ModBlocks.ANCIENT_STONE.get().defaultBlockState()"
        ));
        assertTrue(generator.contains(
                "if (inset && (a == 8 || b == 8) && c == 6)"
        ));
        String keyRoomMigration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsKeyRoomMigrationEvents.java"
        ));
        assertTrue(keyRoomMigration.contains("located.cell().feature() != 6"));
        assertTrue(keyRoomMigration.contains("!cell.north()"));
        assertTrue(keyRoomMigration.contains(
                "boolean centerWindow = height == 6 && across == 8"
        ));
        assertTrue(keyRoomMigration.contains("repairVoidBacking("));
        assertTrue(keyRoomMigration.contains("isReplaceableStructureState"));
        assertTrue(keyRoomMigration.contains("chunk.setUnsaved(true)"));
        String stairMigration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsStairMigrationEvents.java"
        ));
        assertTrue(stairMigration.contains("OuterLandsDimensions.OUTER_LANDS"));
        assertTrue(stairMigration.contains("current.getOpposite()"));
        assertTrue(stairMigration.contains("repairClassicPassageCornerStairs"));
        assertTrue(stairMigration.contains("repairClassicClosedPassageEdges"));
        assertTrue(stairMigration.contains(
                "repairClassicConnectionStairTips"
        ));
        assertTrue(stairMigration.contains("OuterLandsStairTopology.refresh"));
        assertTrue(stairMigration.contains("chunk.setUnsaved(true)"));
        String topology = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsStairTopology.java"
        ));
        assertFalse(topology.contains("repairInnerCorners"));
        assertTrue(topology.contains("setValue(StairBlock.FACING"));
        assertTrue(topology.contains(
                "frontFacing == facing.getCounterClockWise()"
        ));
        assertTrue(topology.contains(
                "behindFacing == facing.getCounterClockWise()"
        ));
        assertTrue(topology.contains(
                "setValue(StairBlock.SHAPE, StairsShape.STRAIGHT)"
        ));
        assertTrue(topology.contains("fillMissingCorners(level, chunk)"));
        assertTrue(topology.contains(
                "fillMissingStraightStairs(level, chunk)"
        ));
        assertTrue(topology.contains("formsStraightRunGap("));
        assertTrue(topology.contains(
                "trimStairsTouchingNothing(level, chunk)"
        ));
        assertTrue(topology.contains("touchesNothingHorizontally("));
        assertTrue(topology.contains(
                "ordinaryAncientWall(level, removal)"
        ));
        assertTrue(topology.contains("formsMissingCorner("));
        assertTrue(topology.contains("canTakeShape("));
        assertTrue(topology.contains("isPerpendicularStair("));
        assertTrue(topology.contains("Block.UPDATE_ALL"));
        assertFalse(topology.contains("resolved.updateShape("));
        assertTrue(topology.contains("isAncientWall(BlockState state)"));
        String pedestalMigration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsPedestalMigrationEvents.java"
        ));
        assertTrue(pedestalMigration.contains("case 1, 6 -> replaceCapstone"));
        assertTrue(pedestalMigration.contains(
                "case 2, 3, 4, 5 -> replaceBossPedestals"
        ));
        assertTrue(pedestalMigration.contains("case 8 -> replaceLibraryPedestals"));
        assertTrue(pedestalMigration.contains("removeItemNoUpdate(0)"));
        assertTrue(pedestalMigration.contains("newPedestal.setItem(0, stored)"));
        assertTrue(generator.contains("generateClassicConnections(level, chunk, cell, random, 3, true)"));
        assertTrue(Files.exists(RESOURCES.resolve(
                "assets/thaumic_reborn/blockstates/eldritch_nothing.json"
        )));
    }

    @Test
    void classicCrabVentsGenerateMigrateRenderAndSpawn() throws IOException {
        Path java = Path.of("src/main/java/com/thaumcraftmodern");
        String vents = Files.readString(java.resolve(
                "worldgen/outerlands/OuterLandsCrabVents.java"
        ));
        assertTrue(vents.contains("case 7 -> 25"));
        assertTrue(vents.contains("case 12 -> 50"));
        assertTrue(vents.contains("case 0 -> 1250"));
        assertTrue(vents.contains("singleOpening(level, cursor)"));
        assertTrue(vents.contains(
                "position.relative(direction)).isAir()"
        ));
        assertFalse(vents.contains("EldritchNothingBlock.isNothing"));
        assertTrue(vents.contains("ELDRITCH_CRAB_VENT"));
        assertTrue(vents.contains("EldritchCrabVentBlock.CRUSTED"));
        assertTrue(vents.contains("cell.feature() == 7 && !existingVent"));
        assertTrue(vents.contains("state.is(ModBlocks.ANCIENT_STONE.get())"));
        assertTrue(vents.contains("state.is(ModBlocks.ANCIENT_CRUST.get())"));

        String blockEntity = Files.readString(java.resolve(
                "world/block/entity/EldritchCrabVentBlockEntity.java"
        ));
        assertTrue(blockEntity.contains("countdown == 15"));
        assertTrue(blockEntity.contains("16.0D"));
        assertTrue(blockEntity.contains(".inflate(32.0D)"));
        assertTrue(blockEntity.contains(".size() > 5"));
        assertTrue(blockEntity.contains("ModEntities.ELDRITCH_CRAB"));
        assertTrue(blockEntity.contains("MobSpawnType.STRUCTURE"));

        String migration = Files.readString(java.resolve(
                "worldgen/outerlands/OuterLandsCrabVentMigrationEvents.java"
        ));
        assertTrue(migration.contains("OuterLandsDimensions.OUTER_LANDS"));
        assertTrue(migration.contains("chunk.setUnsaved(true)"));

        String renderer = Files.readString(java.resolve(
                "client/render/EldritchCrabVentRenderer.java"
        ));
        assertTrue(renderer.contains("crabvent.obj"));
        assertTrue(renderer.contains("crabvent.png"));
        assertTrue(renderer.contains("LegacyObjMesh.get(MODEL)"));
        assertTrue(renderer.contains("RenderType.entityCutoutNoCull(TEXTURE)"));

        String ventBlock = Files.readString(java.resolve(
                "world/block/EldritchCrabVentBlock.java"
        ));
        assertTrue(ventBlock.contains("RenderShape.MODEL"));
        assertTrue(ventBlock.contains("BooleanProperty CRUSTED"));
        String ventState = Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/blockstates/eldritch_crab_vent.json"
        ));
        assertTrue(ventState.contains("crusted=false,facing=north"));
        assertTrue(ventState.contains("crusted=true,facing=north"));
        assertTrue(Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/eldritch_crab_vent.json"
        )).contains("thaumic_reborn:block/ancient_stone"));
        assertTrue(Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/"
                        + "eldritch_crab_vent_crusted.json"
        )).contains("thaumic_reborn:block/ancient_crust"));
    }

    @Test
    void runedAncientStoneUsesOriginalRateTexturesAndTrapBehavior()
            throws IOException {
        Path java = Path.of("src/main/java/com/thaumcraftmodern");
        String placement = Files.readString(java.resolve(
                "worldgen/outerlands/OuterLandsRunedStones.java"
        ));
        assertTrue(placement.contains("CANDIDATE_SALT, 25"));
        assertTrue(placement.contains("CRAB_SALT, 50"));
        assertTrue(placement.contains("DECO_BRANCH_SALT, 3"));
        assertTrue(placement.contains("RUNED_SALT, 8"));
        assertTrue(placement.contains("cell.feature() == 7"));
        assertTrue(placement.contains("ELDRITCH_RUNED_STONE"));

        String trap = Files.readString(java.resolve(
                "world/block/entity/EldritchRunedStoneBlockEntity.java"
        ));
        assertTrue(trap.contains("private int count = 20"));
        assertTrue(trap.contains("10 + level.random.nextInt(25)"));
        assertTrue(trap.contains("3.0D"));
        assertTrue(trap.contains("damageSources().magic(), 2.0F"));
        assertTrue(trap.contains("level.random.nextBoolean()"));
        assertTrue(trap.contains("1 + level.random.nextInt(2)"));
        assertTrue(trap.contains("WarpType.TEMPORARY"));
        assertTrue(trap.contains("new NodeZapPacket("));

        JsonObject variants = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(
                        "assets/thaumic_reborn/blockstates/"
                                + "eldritch_runed_stone.json"
                )
        )).getAsJsonObject().getAsJsonObject("variants");
        assertEquals(4, variants.size());
        for (int index = 0; index < 4; index++) {
            String suffix = index == 0 ? "" : "_" + (index + 1);
            String model = Files.readString(RESOURCES.resolve(
                    "assets/thaumic_reborn/models/block/"
                            + "eldritch_runed_stone" + suffix + ".json"
            ));
            assertTrue(model.contains(
                    "thaumic_reborn:block/ancient_stone_" + (index + 5)
            ));
        }

        String migration = Files.readString(java.resolve(
                "worldgen/outerlands/"
                        + "OuterLandsRunedStoneMigrationEvents.java"
        ));
        assertTrue(migration.contains("OuterLandsDimensions.OUTER_LANDS"));
        assertTrue(migration.contains("chunk.setUnsaved(true)"));
    }

    @Test
    void libraryGlyphedStoneUsesExactTc4ModelTextureAndDrop()
            throws IOException {
        assertArrayEquals(
                Files.readAllBytes(ORIGINAL.resolve("blocks/es_i_2.png")),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/"
                                + "eldritch_glyphed_stone.png"
                ))
        );

        String model = Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/"
                        + "eldritch_glyphed_stone.json"
        ));
        assertTrue(model.contains("\"ambientocclusion\": false"));
        assertTrue(model.contains("\"from\": [2, 2, 2]"));
        assertTrue(model.contains("\"to\": [14, 14, 14]"));

        String blocks = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java"
        ));
        assertTrue(blocks.contains("ELDRITCH_GLYPHED_STONE"));
        assertTrue(blocks.contains("UniformInt.of(1, 4)"));
        assertTrue(blocks.contains(".lightLevel(state -> 12)"));

        String loot = Files.readString(RESOURCES.resolve(
                "data/thaumic_reborn/loot_tables/blocks/"
                        + "eldritch_glyphed_stone.json"
        ));
        assertTrue(loot.contains("thaumic_reborn:knowledge_fragment"));

        String generator = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsLabyrinthGenerator.java"
        ));
        assertTrue(generator.contains("ModBlocks.ELDRITCH_GLYPHED_STONE"));

        String migration = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsPedestalMigrationEvents.java"
        ));
        assertTrue(migration.contains("LIBRARY_GLYPHED_STONES"));
        assertTrue(migration.contains("replaceLibraryGlyphedStones"));
        assertTrue(migration.contains("ModBlocks.ANCIENT_ROCK"));
        assertTrue(migration.contains("ModBlocks.ELDRITCH_GLYPHED_STONE"));
    }

    @Test
    void glowingCrustUsesByteExactTc4AssetsAndDecorationRules()
            throws IOException {
        Path originalAssets = Path.of(
                "reference/Thaumcraft-4.2-FOREVA-master/src/main/resources/"
                        + "assets/thaumcraft"
        );
        Path portedAssets = RESOURCES.resolve("assets/thaumcraft");
        assertArrayEquals(
                Files.readAllBytes(originalAssets.resolve(
                        "models/block/blockeldritch_4.json"
                )),
                Files.readAllBytes(portedAssets.resolve(
                        "models/block/blockeldritch_4.json"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(originalAssets.resolve(
                        "textures/blocks/es_i_1.png"
                )),
                Files.readAllBytes(portedAssets.resolve(
                        "textures/blocks/es_i_1.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(originalAssets.resolve(
                        "textures/blocks/es_i_1.png.mcmeta"
                )),
                Files.readAllBytes(portedAssets.resolve(
                        "textures/blocks/es_i_1.png.mcmeta"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(originalAssets.resolve(
                        "textures/blocks/es_i_1.png"
                )),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/"
                                + "eldritch_glowing_crust.png"
                ))
        );
        assertArrayEquals(
                Files.readAllBytes(originalAssets.resolve(
                        "textures/blocks/es_i_1.png.mcmeta"
                )),
                Files.readAllBytes(RESOURCES.resolve(
                        "assets/thaumic_reborn/textures/block/"
                                + "eldritch_glowing_crust.png.mcmeta"
                ))
        );

        String glowingCrustModel = Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/"
                        + "eldritch_glowing_crust.json"
        ));
        assertTrue(glowingCrustModel.contains(
                "thaumic_reborn:block/eldritch_glowing_crust"
        ));

        String connectedModel = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EldritchCrustBakedModel.java"
        ));
        assertTrue(connectedModel.contains("eldritch_glowing_crust"));
        assertTrue(connectedModel.contains("eldritch_glyphed_stone"));
        assertTrue(connectedModel.contains("neighbour.isFaceSturdy"));
        assertTrue(connectedModel.contains(
                "hasNeighbour(mask, Direction.UP) ? 16.0F : 14.0F"
        ));
        assertTrue(connectedModel.contains(
                "hasNeighbour(mask, Direction.DOWN) ? 0.0F : 2.0F"
        ));

        String blocks = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/ModBlocks.java"
        ));
        assertTrue(blocks.contains("ELDRITCH_GLOWING_CRUST"));
        assertTrue(blocks.contains(".strength(2.0F, 30.0F)"));
        assertTrue(blocks.contains(".lightLevel(state -> 12)"));

        String decorations = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsRunedStones.java"
        ));
        assertTrue(decorations.contains("CANDIDATE_SALT, 25"));
        assertTrue(decorations.contains("CRUST_GLOW_SALT, 25"));
        assertTrue(decorations.contains("DECO_BRANCH_SALT, 3"));
        assertTrue(decorations.contains("RUNED_SALT, 8"));
        assertTrue(decorations.contains("CRYSTAL_SALT, 12"));
        assertTrue(decorations.contains("ELDRITCH_GLOWING_CRUST"));
        assertTrue(decorations.contains("ELDRITCH_GLYPHED_STONE"));
        assertTrue(decorations.contains("ELDRITCH_CRYSTAL_CLUSTER"));
        assertTrue(decorations.contains("replaceLegacyCrystalPlaceholders"));
        assertTrue(decorations.contains("BALANCED_CRYSTAL_CLUSTER"));
        assertTrue(decorations.contains("isBedrockShowing"));
        assertTrue(decorations.contains("position.offset(x, y, z)"));
        assertTrue(decorations.contains("replaceLegacyCrossLights"));
        assertTrue(decorations.contains(
                "state.getValue(AncientStoneBlock.VARIANT) != 3"
        ));

        String generator = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands/"
                        + "OuterLandsLabyrinthGenerator.java"
        ));
        assertTrue(generator.contains(
                "case 7 -> ModBlocks.ELDRITCH_GLOWING_CRUST"
        ));

        String loot = Files.readString(RESOURCES.resolve(
                "data/thaumic_reborn/loot_tables/blocks/"
                        + "eldritch_glowing_crust.json"
        ));
        assertTrue(loot.contains("thaumic_reborn:eldritch_glowing_crust"));
    }

    @Test
    void eldritchNothingUsesTc4StarFieldCollisionAndDamage() throws IOException {
        Path java = Path.of("src/main/java/com/thaumcraftmodern");
        String block = Files.readString(java.resolve(
                "world/block/EldritchNothingBlock.java"
        ));
        assertTrue(block.contains("Block.box(2, 2, 2, 14, 14, 14)"));
        assertTrue(block.contains("entity.tickCount > 20"));
        assertTrue(block.contains("player.getAbilities().flying"));
        assertTrue(block.contains("damageSources().fellOutOfWorld()"));
        assertTrue(block.contains("8.0F"));
        assertTrue(block.contains("BooleanProperty.create(\"exposed\")"));
        assertTrue(block.contains("extends Block"));
        assertFalse(block.contains("extends BaseEntityBlock"));

        String anchorBlock = Files.readString(java.resolve(
                "world/block/EldritchNothingAnchorBlock.java"
        ));
        assertTrue(anchorBlock.contains("extends BaseEntityBlock"));
        assertTrue(anchorBlock.contains(
                "new EldritchNothingBlockEntity(pos, state)"
        ));

        String registry = Files.readString(java.resolve(
                "registry/ModBlocks.java"
        ));
        assertTrue(registry.contains(".strength(-1.0F, 6000000.0F)"));
        assertTrue(registry.contains(".lightLevel(state -> 3)"));

        String renderer = Files.readString(java.resolve(
                "client/render/EldritchNothingBlockEntityRenderer.java"
        ));
        String portableHole = Files.readString(java.resolve(
                "client/render/TemporaryHoleBlockEntityRenderer.java"
        ));
        assertTrue(renderer.contains(
                "TemporaryHoleBlockEntityRenderer.drawFieldFace("
        ));
        assertTrue(portableHole.contains("static void drawFieldFace("));

        String migration = Files.readString(java.resolve(
                "worldgen/outerlands/"
                        + "OuterLandsEldritchNothingMigrationEvents.java"
        ));
        assertTrue(migration.contains("OuterLandsDimensions.OUTER_LANDS"));
        assertTrue(migration.contains(
                "OuterLandsEldritchNothingExposure.refresh("
        ));
        assertTrue(migration.contains("chunk.setUnsaved(true)"));

        JsonObject variants = JsonParser.parseString(Files.readString(
                RESOURCES.resolve(
                        "assets/thaumic_reborn/blockstates/eldritch_nothing.json"
                )
        )).getAsJsonObject().getAsJsonObject("variants");
        assertTrue(variants.has("exposed=false"));
        assertTrue(variants.has("exposed=true"));
        assertTrue(Files.exists(RESOURCES.resolve(
                "assets/thaumic_reborn/blockstates/"
                        + "eldritch_nothing_anchor.json"
        )));
    }

    @Test
    void bossDoorUsesEndPortalFieldTabletAndOpeningSequence() throws IOException {
        Path java = Path.of("src/main/java/com/thaumcraftmodern");
        String generator = Files.readString(java.resolve(
                "worldgen/outerlands/OuterLandsLabyrinthGenerator.java"
        ));
        assertTrue(generator.contains("{0, 2, 2, 2, 2, 2, 0}"));
        assertTrue(generator.contains("{2, 9, 9, 1, 9, 9, 2}"));
        assertTrue(generator.contains("ModBlocks.ELDRITCH_DOOR"));
        assertTrue(generator.contains("ModBlocks.ELDRITCH_BARRIER"));

        String renderer = Files.readString(java.resolve(
                "client/render/EldritchLockRenderer.java"
        ));
        assertTrue(renderer.contains("RenderType.endPortal()"));
        assertFalse(renderer.contains("getMainCamera()"));
        assertFalse(renderer.contains("layer < 16"));
        assertTrue(renderer.contains("5 - (count + arm * 5) / 20"));
        assertTrue(renderer.contains("ModItems.RUNED_TABLET"));
        assertTrue(renderer.contains("0.3475D"));
        assertTrue(renderer.contains("isBarrierCell"));
        assertTrue(renderer.contains("0.5F - facing.getStepZ() * 0.02F"));
        assertTrue(renderer.contains("0.25F,0.25F,0.50F,0.50F"));
        assertTrue(renderer.contains("0.75F,0.25F,1.00F,0.50F"));
        assertTrue(renderer.contains("fieldVertex"));
        assertFalse(renderer.contains("BACKGROUND_SHADE"));

        String renderType = Files.readString(java.resolve(
                "client/render/EldritchLockRenderType.java"
        ));
        assertTrue(renderType.contains("GL11.GL_REPEAT"));
        assertTrue(renderType.contains("REPEAT_TEXTURE"));
        assertTrue(renderType.contains("repeat ? REPEAT_TEXTURE : DEFAULT_TEXTURING"));
        assertTrue(renderType.contains("RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER"));
        assertTrue(renderType.contains("new TextureStateShard(texture, false, false)"));

        String lock = Files.readString(java.resolve(
                "world/block/entity/EldritchLockBlockEntity.java"
        ));
        assertTrue(lock.contains("UNLOCK_TICKS = 100"));
        assertTrue(lock.contains("migrateLegacyDoor"));
        assertTrue(lock.contains("getUpdatePacket()"));
    }

    @Test
    void portalSelectsOneAtlasFrameInsteadOfWrappingAtlasAroundCube()
            throws IOException {
        Path java = Path.of("src/main/java/com/thaumcraftmodern");
        String block = Files.readString(java.resolve(
                "world/block/OuterLandsPortalBlock.java"
        ));
        assertTrue(block.contains("return RenderShape.INVISIBLE"));
        String model = Files.readString(RESOURCES.resolve(
                "assets/thaumic_reborn/models/block/"
                        + "outer_lands_portal.json"
        ));
        assertFalse(model.contains("\"elements\""));

        String renderer = Files.readString(java.resolve(
                "client/render/OuterLandsPortalRenderer.java"
        ));
        assertTrue(renderer.contains("ATLAS_FRAMES = 16"));
        assertTrue(renderer.contains("frame / (float) ATLAS_FRAMES"));
        assertTrue(renderer.contains("getMainCamera().rotation()"));
        assertTrue(renderer.contains("LightTexture.FULL_BRIGHT"));

        String altarRenderer = Files.readString(java.resolve(
                "client/render/EldritchAltarPartRenderer.java"
        ));
        assertTrue(altarRenderer.contains("altar.insertedEyes()"));
        assertTrue(altarRenderer.contains("side * 90.0F"));
        assertTrue(altarRenderer.contains("ModItems.ELDRITCH_EYE"));
        assertTrue(altarRenderer.contains(
                "EYE_VERTICAL_OFFSET = 0.2F + 2.0F / 16.0F"
        ));

        String altar = Files.readString(java.resolve(
                "world/block/entity/EldritchAltarPartBlockEntity.java"
        ));
        assertTrue(altar.contains("getUpdatePacket()"));
        assertTrue(altar.contains("Block.UPDATE_CLIENTS"));

        String portalEntity = Files.readString(java.resolve(
                "world/block/entity/OuterLandsPortalBlockEntity.java"
        ));
        assertTrue(portalEntity.contains("TRANSFER_INTERVAL_TICKS = 5"));
        assertTrue(portalEntity.contains(
                "ENTRY_HORIZONTAL_RADIUS = 3.0D"
        ));
        assertTrue(portalEntity.contains("List.copyOf(server.players())"));
        assertTrue(portalEntity.contains("OuterLandsPortalBlock.transferPlayer"));
        assertTrue(portalEntity.contains("HasMazeDestination"));

        String portalBlock = Files.readString(java.resolve(
                "world/block/OuterLandsPortalBlock.java"
        ));
        assertTrue(portalBlock.contains("TicketType.PORTAL"));
        assertTrue(portalBlock.contains(
                "DESTINATION_PRELOAD_RADIUS = 0"
        ));
        assertTrue(portalBlock.contains("addRegionTicket("));
        assertTrue(portalBlock.contains("getChunkNow("));
        assertFalse(portalBlock.contains(
                "target.getChunk(targetChunkX, targetChunkZ)"
        ));
        assertTrue(
                portalBlock.indexOf("getChunkNow(")
                        < portalBlock.indexOf("KnowledgeAccess.mutate(")
        );
        assertTrue(portalEntity.contains("OuterLandsPortalAllocationData.get(source).allocate()"));
        assertTrue(portalBlock.contains("portal.destination(source)"));
        assertFalse(portalBlock.contains(
                "Math.floorDiv(player.blockPosition().getX(), 16)"
        ));

        String migration = Files.readString(java.resolve(
                "world/block/entity/OuterLandsPortalMigrationEvents.java"
        ));
        assertTrue(migration.contains("chunk.findBlocks"));
        assertTrue(migration.contains("chunk.setBlockEntity(portal)"));
        assertTrue(migration.contains("Block.UPDATE_CLIENTS"));
    }

    @Test
    void outerResearchIsNoLongerPublishedAsInactive() throws IOException {
        for (String[] entry : new String[][]{
                {"enterouter", "r_outer.png"},
                {"outerrev", "r_outerrev.png"}
        }) {
            JsonObject research = JsonParser.parseString(Files.readString(
                    RESOURCES.resolve(
                            "data/thaumic_reborn/thaumcraft/research/legacy/"
                                    + entry[0] + ".json"
                    )
            )).getAsJsonObject();
            assertFalse(research.has("inactive")
                    && research.get("inactive").getAsBoolean());
            assertEquals(
                    "thaumic_reborn:textures/misc/" + entry[1],
                    research.get("icon_resource").getAsString()
            );
            assertArrayEquals(
                    Files.readAllBytes(ORIGINAL.resolve("misc/" + entry[1])),
                    Files.readAllBytes(RESOURCES.resolve(
                            "assets/thaumic_reborn/textures/misc/" + entry[1]
                    ))
            );
        }
    }

    @Test
    void outerLandsChunkMigrationsCannotRunReentrantlyDuringChunkLoad()
            throws IOException {
        Path outerLands = Path.of(
                "src/main/java/com/thaumcraftmodern/worldgen/outerlands"
        );
        String scheduler = Files.readString(outerLands.resolve(
                "OuterLandsChunkMigrationScheduler.java"
        ));
        assertTrue(scheduler.contains("new TickTask("));
        assertTrue(scheduler.contains("server.getTickCount() + 1"));

        for (String migration : new String[]{
                "OuterLandsPedestalMigrationEvents.java",
                "OuterLandsKeyRoomMigrationEvents.java",
                "OuterLandsStairMigrationEvents.java",
                "OuterLandsSpawnerMigrationEvents.java",
                "OuterLandsCrabVentMigrationEvents.java",
                "OuterLandsRunedStoneMigrationEvents.java",
                "OuterLandsEldritchNothingMigrationEvents.java"
        }) {
            String source = Files.readString(outerLands.resolve(migration));
            assertTrue(source.contains(
                    "OuterLandsChunkMigrationScheduler.nextTick("
            ));
            assertFalse(source.contains("getServer().execute("));
        }
    }

    @Test
    void strangeCrystalUsesExactTc4AssetsAndMetadataSevenBehavior()
            throws IOException {
        Path originalJar = Path.of(
                "reference/original/Thaumcraft_1.7.10_4.2.3.5.jar"
        );
        try (ZipFile jar = new ZipFile(originalJar.toFile())) {
            for (String asset : new String[]{
                    "textures/models/vcrystal.obj",
                    "textures/models/vcrystal.png",
                    "textures/blocks/crust.png"
            }) {
                String entryName = "assets/thaumcraft/" + asset;
                var entry = jar.getEntry(entryName);
                assertTrue(entry != null, "Missing TC4 asset " + entryName);
                assertArrayEquals(
                        jar.getInputStream(entry).readAllBytes(),
                        Files.readAllBytes(RESOURCES.resolve(entryName))
                );
            }
        }

        String mesh = Files.readString(RESOURCES.resolve(
                "assets/thaumcraft/textures/models/vcrystal.obj"
        ));
        assertTrue(mesh.contains("g Crystal"));
        assertTrue(mesh.contains("g Base"));

        String block = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/world/block/"
                        + "EldritchCrystalBlock.java"
        ));
        assertTrue(block.contains("BALANCED_SHARD"));
        assertTrue(block.contains("isFaceSturdy"));

        String renderer = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/client/render/"
                        + "EldritchCrystalRenderer.java"
        ));
        assertTrue(renderer.contains("vcrystal.obj"));
        assertTrue(renderer.contains("vcrystal.png"));
        assertTrue(renderer.contains("crust.png"));
        assertTrue(renderer.contains("Math.floorMod(crystal.hashCode(), 4)"));
        assertTrue(renderer.contains("Mth.sin(ticks / 6.0F) * 0.075F + 0.925F"));
        assertTrue(renderer.contains("1.0F, 1.0F, 1.0F, 0.7F"));

        String creative = Files.readString(Path.of(
                "src/main/java/com/thaumcraftmodern/registry/"
                        + "ModCreativeTabs.java"
        ));
        assertFalse(creative.contains("ELDRITCH_CRYSTAL_CLUSTER"));
    }
}
