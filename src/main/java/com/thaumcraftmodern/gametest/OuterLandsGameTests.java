package com.thaumcraftmodern.gametest;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.entity.EldritchLockBlockEntity;
import com.thaumcraftmodern.world.block.EldritchCrabVentBlock;
import com.thaumcraftmodern.world.block.entity.EldritchCrabVentBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import com.thaumcraftmodern.world.block.entity.OuterLandsPortalBlockEntity;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsDimensions;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsCell;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsLabyrinthGenerator;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsMaze;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsMindSpiderSpawners;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsSpawnRules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OuterLandsGameTests {
    private OuterLandsGameTests() {
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 40
    )
    public static void separatePortalsKeepSeparateMazeDestinations(
            GameTestHelper helper
    ) {
        BlockPos firstPosition = new BlockPos(2, 2, 2);
        BlockPos secondPosition = new BlockPos(5, 2, 2);
        helper.setBlock(firstPosition, ModBlocks.OUTER_LANDS_PORTAL.get());
        helper.setBlock(secondPosition, ModBlocks.OUTER_LANDS_PORTAL.get());
        OuterLandsPortalBlockEntity first =
                (OuterLandsPortalBlockEntity) helper.getBlockEntity(
                        firstPosition
                );
        OuterLandsPortalBlockEntity second =
                (OuterLandsPortalBlockEntity) helper.getBlockEntity(
                        secondPosition
                );
        var firstDestination = first.destination(helper.getLevel());
        var secondDestination = second.destination(helper.getLevel());
        helper.assertTrue(!firstDestination.equals(secondDestination),
                "Two distinct portals reused one Outer Lands maze region");
        helper.assertTrue(firstDestination.equals(
                        first.destination(helper.getLevel())
                ), "A portal changed its maze destination between uses");
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 40
    )
    public static void crabVentActuallyReleasesCrab(GameTestHelper helper) {
        BlockPos ventPosition = new BlockPos(2, 2, 2);
        helper.setBlock(ventPosition, ModBlocks.ELDRITCH_CRAB_VENT.get()
                .defaultBlockState().setValue(
                        EldritchCrabVentBlock.FACING, Direction.EAST
                ));
        EldritchCrabVentBlockEntity vent =
                (EldritchCrabVentBlockEntity) helper.getBlockEntity(
                        ventPosition
                );
        helper.assertTrue(vent.releaseCrab(
                helper.getLevel(), Direction.EAST
        ), "Crab vent could not add its crab to the server level");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                new AABB(helper.absolutePos(ventPosition)).inflate(4.0D),
                mob -> mob.kind() == LegacyMobKind.ELDRITCH_CRAB
        ).size() == 1, "Crab vent did not release an eldritch crab");
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 300
    )
    public static void generatedNestRoomContainsCrabVents(
            GameTestHelper helper
    ) {
        ServerLevel outer = helper.getLevel().getServer().getLevel(
                OuterLandsDimensions.OUTER_LANDS
        );
        helper.assertTrue(outer != null,
                "Outer Lands dimension was not registered");
        int regionX = 302;
        int regionZ = 302;
        OuterLandsMaze maze = OuterLandsMaze.forRegion(
                outer.getSeed(), regionX, regionZ
        );
        int centerChunkX = regionX * OuterLandsMaze.REGION_SIZE_CHUNKS
                + OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
        int centerChunkZ = regionZ * OuterLandsMaze.REGION_SIZE_CHUNKS
                + OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
        int originChunkX = centerChunkX - (1 + maze.width() / 2);
        int originChunkZ = centerChunkZ - (1 + maze.height() / 2);
        ChunkPos nestChunk = null;
        for (int row = 0; row < maze.height() && nestChunk == null; row++) {
            for (int column = 0; column < maze.width(); column++) {
                if (maze.cell(column, row) != null
                        && maze.cell(column, row).feature() == 7) {
                    nestChunk = new ChunkPos(
                            originChunkX + column,
                            originChunkZ + row
                    );
                    break;
                }
            }
        }
        helper.assertTrue(nestChunk != null,
                "Generated maze has no crab-nest room");
        ChunkPos target = nestChunk;
        OuterLandsCell targetCell = OuterLandsMaze.at(
                outer.getSeed(), target.x, target.z
        ).cell();
        outer.getChunk(target.x, target.z);
        helper.runAfterDelay(20, () -> {
            int vents = 0;
            for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 1;
                    y <= OuterLandsLabyrinthGenerator.BASE_Y + 11; y++) {
                for (int x = target.getMinBlockX();
                        x <= target.getMaxBlockX(); x++) {
                    for (int z = target.getMinBlockZ();
                            z <= target.getMaxBlockZ(); z++) {
                        if (outer.getBlockState(new BlockPos(x, y, z))
                                .is(ModBlocks.ELDRITCH_CRAB_VENT.get())) {
                            vents++;
                        }
                    }
                }
            }
            helper.assertTrue(vents >= 3,
                    "Generated crab-nest room contains fewer than three crab vents: "
                            + vents);
            assertConnectionTipStairs(helper, outer, target, targetCell);
            helper.succeed();
        });
    }

    private static void assertConnectionTipStairs(
            GameTestHelper helper,
            ServerLevel level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            boolean connected = switch (side) {
                case NORTH -> cell.north();
                case SOUTH -> cell.south();
                case WEST -> cell.west();
                case EAST -> cell.east();
                default -> false;
            };
            if (!connected) {
                continue;
            }
            for (int width : new int[]{3, 7}) {
                for (int y : new int[]{
                        OuterLandsLabyrinthGenerator.BASE_Y + 3,
                        OuterLandsLabyrinthGenerator.BASE_Y + 7
                }) {
                    int x = side.getAxis() == Direction.Axis.Z
                            ? chunk.getMinBlockX() + 3 + width
                            : side == Direction.WEST
                                    ? chunk.getMinBlockX() + 3
                                    : chunk.getMinBlockX() + 13;
                    int z = side.getAxis() == Direction.Axis.X
                            ? chunk.getMinBlockZ() + 3 + width
                            : side == Direction.NORTH
                                    ? chunk.getMinBlockZ() + 3
                                    : chunk.getMinBlockZ() + 13;
                    BlockPos position = new BlockPos(x, y, z);
                    helper.assertTrue(level.getBlockState(position)
                                    .is(ModBlocks.ANCIENT_STAIRS.get()),
                            "Room connection tip lost its stair at " + position);
                }
            }
        }
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 200
    )
    public static void dimensionBuildsPortalCellInsideRealVoid(
            GameTestHelper helper
    ) {
        ServerLevel outer = helper.getLevel().getServer().getLevel(
                OuterLandsDimensions.OUTER_LANDS
        );
        helper.assertTrue(outer != null,
                "Outer Lands dimension was not registered");
        int centerChunk = OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
        outer.getChunk(centerChunk, centerChunk);
        int centerX = centerChunk * 16 + 8;
        int centerZ = centerChunk * 16 + 8;
        helper.assertTrue(outer.getBlockState(new BlockPos(
                        centerX,
                        OuterLandsLabyrinthGenerator.BASE_Y + 2,
                        centerZ
                )).is(ModBlocks.ELDRITCH_CAPSTONE.get()),
                "Center portal room did not generate its pedestal");
        helper.assertTrue(outer.getBlockState(new BlockPos(
                        centerX,
                        OuterLandsLabyrinthGenerator.BASE_Y + 3,
                        centerZ
                )).is(ModBlocks.OUTER_LANDS_PORTAL.get()),
                "Center portal room did not generate its return gate");

        outer.getChunk(0, 0);
        helper.assertTrue(outer.getBlockState(new BlockPos(
                        8,
                        OuterLandsLabyrinthGenerator.BASE_Y + 1,
                        8
                )).isAir(),
                "Space outside the finite maze is not void");
        helper.succeed();
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 300
    )
    public static void dimensionBuildsPersistentRunedTabletRoom(
            GameTestHelper helper
    ) {
        ServerLevel outer = helper.getLevel().getServer().getLevel(
                OuterLandsDimensions.OUTER_LANDS
        );
        helper.assertTrue(outer != null,
                "Outer Lands dimension was not registered");

        int regionX = 301;
        int regionZ = 301;
        OuterLandsMaze maze = OuterLandsMaze.forRegion(
                outer.getSeed(), regionX, regionZ
        );
        int centerChunkX = regionX * OuterLandsMaze.REGION_SIZE_CHUNKS
                + OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
        int centerChunkZ = regionZ * OuterLandsMaze.REGION_SIZE_CHUNKS
                + OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
        int originChunkX = centerChunkX - (1 + maze.width() / 2);
        int originChunkZ = centerChunkZ - (1 + maze.height() / 2);
        BlockPos tabletCenter = null;
        for (int row = 0; row < maze.height() && tabletCenter == null; row++) {
            for (int column = 0; column < maze.width(); column++) {
                if (maze.cell(column, row) != null
                        && maze.cell(column, row).feature() == 6) {
                    tabletCenter = new BlockPos(
                            (originChunkX + column) * 16 + 8,
                            OuterLandsLabyrinthGenerator.BASE_Y + 2,
                            (originChunkZ + row) * 16 + 8
                    );
                    break;
                }
            }
        }
        helper.assertTrue(tabletCenter != null,
                "Generated maze has no runed-tablet room");
        BlockPos roomCenter = tabletCenter;
        outer.getChunkAt(roomCenter);

        helper.runAfterDelay(20, () -> {
            var monsterSpawns = outer.getBiome(roomCenter).value()
                    .getMobSettings().getMobs(MobCategory.MONSTER).unwrap();
            helper.assertTrue(monsterSpawns.stream().anyMatch(
                            spawn -> spawn.type
                                    == ModEntities.INHABITED_ZOMBIE.get()),
                    "Eldritch biome has no natural inhabited-zombie spawn");
            helper.assertTrue(monsterSpawns.stream().anyMatch(
                            spawn -> spawn.type
                                    == ModEntities.ELDRITCH_GUARDIAN.get()),
                    "Eldritch biome has no natural guardian spawn");
            helper.assertTrue(outer.getBlockState(roomCenter).is(
                            ModBlocks.ELDRITCH_CAPSTONE.get()),
                    "Runed-tablet room did not generate its center pedestal");
            helper.assertTrue(outer.getBlockEntity(roomCenter)
                            instanceof ArcanePedestalBlockEntity pedestal
                            && pedestal.item().is(ModItems.RUNED_TABLET.get()),
                    "Runed tablet was not persisted on its pedestal");
            helper.assertTrue(OuterLandsSpawnRules.isEnclosedMazePosition(
                            outer, roomCenter.above()),
                    "Tablet room was not recognized as enclosed maze interior");
            helper.assertTrue(!OuterLandsSpawnRules.isEnclosedMazePosition(
                            outer,
                            new BlockPos(roomCenter.getX(),
                                    OuterLandsLabyrinthGenerator.BASE_Y + 15,
                                    roomCenter.getZ())),
                    "Labyrinth roof was accepted as a natural spawn position");
            helper.succeed();
        });
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 300
    )
    public static void mindSpiderSpawnerStoresConfiguredEntityType(
            GameTestHelper helper
    ) {
        BlockPos relative = new BlockPos(3, 2, 3);
        helper.setBlock(relative, net.minecraft.world.level.block.Blocks.SPAWNER);
        BlockPos center = helper.absolutePos(relative);
        helper.assertTrue(OuterLandsMindSpiderSpawners.configure(
                        helper.getLevel(), center),
                "Mind-spider spawner could not be configured");
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(helper.getLevel().getBlockEntity(center)
                            instanceof SpawnerBlockEntity,
                    "Configured spawner lost its block entity");
            SpawnerBlockEntity spawner = (SpawnerBlockEntity)
                    helper.getLevel().getBlockEntity(center);
            helper.assertTrue(
                    OuterLandsMindSpiderSpawners.isConfigured(spawner),
                    "Spawner does not contain mind_spider spawn data"
            );
            helper.succeed();
        });
    }

    @GameTest(
            template = "empty",
            batch = "outerLands",
            timeoutTicks = 160
    )
    public static void runedLockStartsOneOriginalBossEncounter(
            GameTestHelper helper
    ) {
        BlockPos relativeLock = new BlockPos(5, 4, 5);
        BlockPos lock = helper.absolutePos(relativeLock);
        BlockPos boss = helper.absolutePos(new BlockPos(8, 3, 8));
        for (int horizontal = -3; horizontal <= 3; horizontal++) {
            for (int vertical = -3; vertical <= 3; vertical++) {
                if (horizontal == 0 && vertical == 0) continue;
                helper.setBlock(
                        relativeLock.offset(horizontal, vertical, 0),
                        ModBlocks.ANCIENT_SEAL.get()
                );
            }
        }
        helper.setBlock(relativeLock, ModBlocks.ELDRITCH_LOCK.get());
        EldritchLockBlockEntity blockEntity =
                (EldritchLockBlockEntity) helper.getBlockEntity(relativeLock);
        blockEntity.setBossCenter(boss);
        helper.assertTrue(blockEntity.beginUnlock(),
                "Runed lock rejected its first tablet activation");

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(
                            lock.offset(0, 3, 0)
                    ).is(ModBlocks.ELDRITCH_DOOR.get()),
                    "Legacy flat wall was not migrated to the TC4 door frame");
            helper.assertTrue(helper.getLevel().getBlockState(
                            lock.offset(1, 0, 0)
                    ).is(ModBlocks.ELDRITCH_BARRIER.get()),
                    "TC4 star-field barrier did not retain a physical gate");
        });

        helper.runAfterDelay(
                EldritchLockBlockEntity.UNLOCK_TICKS + 5,
                () -> {
                    helper.assertTrue(helper.getLevel().getBlockState(lock).isAir(),
                            "Runed lock did not open after 100 ticks");
                    helper.assertTrue(helper.getLevel().getBlockState(
                                    lock.offset(1, 0, 0)
                            ).isAir(),
                            "Star-field barrier remained after the opening animation");
                    helper.assertTrue(helper.getLevel().getBlockState(
                                    lock.offset(0, 3, 0)
                            ).is(ModBlocks.ELDRITCH_DOOR.get()),
                            "TC4 doorway frame vanished with the star field");
                    var encounters = helper.getLevel().getEntitiesOfClass(
                            LegacyThaumcraftMob.class,
                            new AABB(boss).inflate(12.0D),
                            mob -> mob.getPersistentData().getBoolean(
                                    "OuterLandsBoss"
                            )
                    );
                    helper.assertTrue(encounters.size() == 1,
                            "Expected exactly one original Outer Lands encounter, got "
                                    + encounters.size());
                    LegacyMobKind kind = encounters.get(0).kind();
                    helper.assertTrue(kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                                    || kind == LegacyMobKind.ELDRITCH_WARDEN
                                    || kind == LegacyMobKind.CRIMSON_PRAETOR
                                    || kind == LegacyMobKind.GIANT_TAINTACLE,
                            "Non-original boss selected: " + kind);
                    helper.succeed();
                }
        );
    }
}
