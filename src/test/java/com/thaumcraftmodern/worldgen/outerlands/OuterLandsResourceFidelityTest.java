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
        assertTrue(generator.contains("wallDirection.getOpposite()"));
        assertTrue(generator.contains("generateClassicConnections(level, chunk, cell, random, 3, true)"));
        assertTrue(Files.exists(RESOURCES.resolve(
                "assets/thaumic_reborn/blockstates/eldritch_nothing.json"
        )));
    }

    @Test
    void bossDoorUsesTc4PatternFieldTabletAndOpeningSequence() throws IOException {
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
        assertTrue(renderer.contains("FIELD_MIN = -2.0F"));
        assertTrue(renderer.contains("FIELD_MAX = 3.0F"));
        assertTrue(renderer.contains("layer < 16"));
        assertTrue(renderer.contains("5 - (count + arm * 5) / 20"));
        assertTrue(renderer.contains("ModItems.RUNED_TABLET"));
        assertTrue(renderer.contains("isBarrierCell"));
        assertTrue(renderer.contains("0.5F - facing.getStepZ() * 0.02F"));
        assertTrue(renderer.contains("0.25F,0.25F,0.50F,0.50F"));
        assertTrue(renderer.contains("0.75F,0.25F,1.00F,0.50F"));
        assertTrue(renderer.contains("float parallaxScale = scale * (0.75F + depth * 0.015625F)"));
        assertFalse(renderer.contains("brightness * 1.55F"));

        String renderType = Files.readString(java.resolve(
                "client/render/EldritchLockRenderType.java"
        ));
        assertTrue(renderType.contains("GL11.GL_REPEAT"));
        assertTrue(renderType.contains("REPEAT_TEXTURE"));
        assertTrue(renderType.contains("repeat ? REPEAT_TEXTURE : DEFAULT_TEXTURING"));

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
                "EYE_VERTICAL_OFFSET = 0.2F + 5.0F / 16.0F"
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
        assertTrue(portalEntity.contains("OuterLandsPortalAllocationData.get(source).allocate()"));

        String portalBlock = Files.readString(java.resolve(
                "world/block/OuterLandsPortalBlock.java"
        ));
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
        for (String name : new String[]{"enterouter", "outerrev"}) {
            JsonObject research = JsonParser.parseString(Files.readString(
                    RESOURCES.resolve(
                            "data/thaumic_reborn/thaumcraft/research/legacy/"
                                    + name + ".json"
                    )
            )).getAsJsonObject();
            assertFalse(research.has("inactive")
                    && research.get("inactive").getAsBoolean());
        }
    }
}
