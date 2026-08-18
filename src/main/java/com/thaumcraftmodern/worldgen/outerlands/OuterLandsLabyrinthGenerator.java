package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.world.block.AncientStoneBlock;
import com.thaumcraftmodern.world.block.EldritchLockBlock;
import com.thaumcraftmodern.world.block.entity.EldritchLockBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;

/** Builds one original-scale labyrinth cell in an otherwise absolute void. */
public final class OuterLandsLabyrinthGenerator {
    private static final int[][] BOSS_DOOR_PATTERN = {
            {0, 2, 2, 2, 2, 2, 0},
            {2, 2, 9, 9, 9, 2, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 9, 9, 1, 9, 9, 2},
            {2, 9, 9, 9, 9, 9, 2},
            {2, 2, 9, 9, 9, 2, 2},
            {0, 2, 2, 2, 2, 2, 0}
    };
    public static final int BASE_Y = 50;
    private static final int FLOOR_Y = BASE_Y + 1;
    private static final int CEILING_Y = BASE_Y + 11;
    private static final int[][] CONNECTION = {
            {0,1,1,1,1,1,1,1,1,1,0},
            {1,8,8,8,8,8,8,8,8,8,1},
            {1,8,8,2,2,2,2,2,8,8,1},
            {1,8,2,5,9,9,9,6,2,8,1},
            {1,8,2,9,9,9,9,9,2,8,1},
            {1,8,2,9,9,9,9,9,2,8,1},
            {1,8,2,9,9,9,9,9,2,8,1},
            {1,8,2,3,9,9,9,4,2,8,1},
            {1,8,8,2,2,2,2,2,8,8,1},
            {1,8,8,8,8,8,8,8,8,8,1},
            {0,1,1,1,1,1,1,1,1,1,0}
    };

    private OuterLandsLabyrinthGenerator() {
    }

    public static void generateChunk(
            WorldGenLevel level,
            ChunkPos chunk,
            long worldSeed
    ) {
        OuterLandsMaze.RegionCell located = OuterLandsMaze.at(
                worldSeed,
                chunk.x,
                chunk.z
        );
        if (!located.exists()) {
            return;
        }
        OuterLandsCell cell = located.cell();
        Random random = new Random(
                worldSeed ^ chunk.x * 341873128712L
                        ^ chunk.z * 132897987541L
        );
        if (cell.feature() >= 2 && cell.feature() <= 5) {
            generateBossQuadrant(level, chunk, cell, random);
            generateClassicConnections(level, chunk, cell, random, 3, true);
            if (cell.exitCount() > 0) {
                generateBossDoor(level, chunk, located, cell);
            }
            OuterLandsStairTopology.refresh(level, chunk);
            OuterLandsEldritchNothingExposure.refresh(level, chunk);
            return;
        }

        switch (cell.feature()) {
            case 1 -> generateClassicPortalRoom(level, chunk, cell, random);
            case 6 -> generateClassicKeyRoom(level, chunk, cell, random);
            case 7 -> generateClassicNestRoom(level, chunk, cell, random);
            case 8 -> generateClassicLibraryRoom(level, chunk, cell, random);
            default -> {
                generateClassicPassage(level, chunk, cell, random);
                switch (cell.feature()) {
                    case 10, 11 -> decoratePassage(level, chunk, random);
                    case 12 -> generateCrustedRoom(level, chunk, random);
                    case 13 -> generateTaintRoom(level, chunk, random);
                    case 14 -> generateWebRoom(level, chunk, random);
                    default -> decoratePassage(level, chunk, random);
                }
            }
        }
        OuterLandsCrabVents.populate(level, chunk, worldSeed, cell);
        OuterLandsRunedStones.populate(level, chunk, worldSeed, cell);
        OuterLandsStairTopology.refresh(level, chunk);
        OuterLandsEldritchNothingExposure.refresh(level, chunk);
    }

    /** Literal modern translation of TC4 GenPassage.generateDefaultPassage. */
    private static void generateClassicPassage(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        generateClassicConnections(level, chunk, cell, random, 4, false);
        boolean cross = cell.north() && cell.south() && cell.west()
                && cell.east() && random.nextBoolean();

        for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
                int px = x + 4 + w;
                int pz = z + 4 + h;
                BlockState surface = cross && w == 4 && h == 4
                        ? classicState(7, Direction.UP, cell, random)
                        : classicState(2, Direction.UP, cell, random);
                set(level, px, BASE_Y + 2, pz, surface);
                set(level, px, BASE_Y + 8, pz, surface);
                set(level, px, BASE_Y, pz, classicState(1, Direction.UP, cell, random));
                set(level, px, BASE_Y + 10, pz, classicState(1, Direction.UP, cell, random));
                set(level, px, BASE_Y + 1, pz, classicState(8, Direction.UP, cell, random));
                set(level, px, BASE_Y + 9, pz, classicState(8, Direction.UP, cell, random));
            }
        }
        buildClassicPassageSide(level, chunk, cell, random, Direction.NORTH, cell.north(), cross);
        buildClassicPassageSide(level, chunk, cell, random, Direction.SOUTH, cell.south(), cross);
        buildClassicPassageSide(level, chunk, cell, random, Direction.WEST, cell.west(), cross);
        buildClassicPassageSide(level, chunk, cell, random, Direction.EAST, cell.east(), cross);
        placeClassicPassageCornerStairs(level, chunk, cell, cross);
    }

    /** Exact corner trim from TC4 GenPassage, including four-way crossings. */
    private static int placeClassicPassageCornerStairs(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell,
            boolean cross
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        int changed = 0;
        if (cross) {
            changed += passageCornerPair(level, x + 5, z + 5, 3, Direction.EAST);
            changed += passageCornerPair(level, x + 5, z + 6, 3, Direction.NORTH);
            changed += passageCornerPair(level, x + 11, z + 5, 3, Direction.EAST);
            changed += passageCornerPair(level, x + 11, z + 6, 4, Direction.NORTH);
            changed += passageCornerPair(level, x + 5, z + 11, 3, Direction.NORTH);
            changed += passageCornerPair(level, x + 6, z + 11, 4, Direction.EAST);
            changed += passageCornerPair(level, x + 11, z + 11, 4, Direction.NORTH);
            changed += passageCornerPair(level, x + 10, z + 11, 4, Direction.EAST);
            return changed;
        }

        /* Preserve TC4's N, S, E, W write order at shared corner positions. */
        if (cell.north() && cell.west()) {
            changed += passageCornerPair(level, x + 6, z + 6, 3, Direction.EAST);
        }
        if (cell.north() && cell.east()) {
            changed += passageCornerPair(level, x + 10, z + 6, 3, Direction.EAST);
        }
        if (cell.south() && cell.west()) {
            changed += passageCornerPair(level, x + 6, z + 10, 4, Direction.EAST);
        }
        if (cell.south() && cell.east()) {
            changed += passageCornerPair(level, x + 10, z + 10, 4, Direction.EAST);
        }
        if (cell.east() && cell.north()) {
            changed += passageCornerPair(level, x + 10, z + 6, 4, Direction.NORTH);
        }
        if (cell.east() && cell.south()) {
            changed += passageCornerPair(level, x + 10, z + 10, 4, Direction.NORTH);
        }
        if (cell.west() && cell.north()) {
            changed += passageCornerPair(level, x + 6, z + 6, 3, Direction.NORTH);
        }
        if (cell.west() && cell.south()) {
            changed += passageCornerPair(level, x + 6, z + 10, 3, Direction.NORTH);
        }
        return changed;
    }

    private static int passageCornerPair(WorldGenLevel level, int x, int z,
            int lowerCode, Direction metadataDirection) {
        int changed = placePassageCornerStair(level, x, BASE_Y + 3, z,
                classicConnectionStair(metadataDirection, lowerCode));
        return changed + placePassageCornerStair(level, x, BASE_Y + 7, z,
                classicConnectionStair(metadataDirection, lowerCode + 2));
    }

    private static int placePassageCornerStair(WorldGenLevel level,
            int x, int y, int z, BlockState expected) {
        if (level.getBlockState(new BlockPos(x, y, z)).equals(expected)) {
            return 0;
        }
        set(level, x, y, z, expected);
        return 1;
    }

    static int repairClassicPassageCornerStairs(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell,
            long worldSeed
    ) {
        int feature = cell.feature();
        if (feature >= 1 && feature <= 8) {
            return 0;
        }
        boolean cross = cell.north() && cell.south() && cell.west()
                && cell.east() && new Random(
                        worldSeed ^ chunk.x * 341873128712L
                                ^ chunk.z * 132897987541L
                ).nextBoolean();
        return placeClassicPassageCornerStairs(level, chunk, cell, cross);
    }

    static int repairClassicClosedPassageEdges(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        if (cell.feature() >= 1 && cell.feature() <= 8) {
            return 0;
        }
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        int changed = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            boolean open = switch (side) {
                case NORTH -> cell.north();
                case SOUTH -> cell.south();
                case WEST -> cell.west();
                case EAST -> cell.east();
                default -> false;
            };
            if (open) {
                continue;
            }
            for (int h = 1; h < 8; h++) {
                int y = BASE_Y + 9 - h;
                for (int across = 4; across <= 12; across++) {
                    BlockPos edge = passageSidePos(x, z, side, across, y, 1);
                    BlockState expected = ModBlocks.ELDRITCH_NOTHING.get()
                            .defaultBlockState();
                    if (!level.getBlockState(edge).is(expected.getBlock())) {
                        set(level, edge.getX(), edge.getY(), edge.getZ(),
                                expected);
                        changed++;
                    }
                }
            }
        }
        return changed;
    }

    static int repairClassicConnectionStairTips(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        if (cell.feature() != 1 && cell.feature() != 6
                && cell.feature() != 7 && cell.feature() != 8) {
            return 0;
        }
        BlockState wall = switch (cell.feature()) {
            case 6 -> ModBlocks.ANCIENT_ROCK.get().defaultBlockState();
            case 7 -> ModBlocks.ANCIENT_CRUST.get().defaultBlockState();
            default -> ModBlocks.ANCIENT_STONE.get().defaultBlockState();
        };
        int changed = 0;
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
            for (int w = 2; w < 9; w++) {
                for (int h = 2; h < 9; h++) {
                    int code = CONNECTION[h][w];
                    if (code < 3 || code > 6) {
                        continue;
                    }
                    BlockPos target = classicConnectionPos(
                            chunk, side, 3, w, BASE_Y + 10 - h
                    );
                    BlockState expected = classicConnectionStair(side, code);
                    BlockState current = level.getBlockState(target);
                    if (!hasStairBacking(level, target, expected)) {
                        if (!current.is(ModBlocks.ANCIENT_STAIRS.get())) {
                            continue;
                        }
                        set(level, target.getX(), target.getY(), target.getZ(), wall);
                        makePreviousConnectionStairCorner(
                                level, target, side, expected
                        );
                        changed++;
                        continue;
                    }
                    if (current.equals(expected)) {
                        continue;
                    }
                    set(level, target.getX(), target.getY(), target.getZ(), expected);
                    changed++;
                }
            }
        }
        return changed;
    }

    private static void buildClassicPassageSide(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random, Direction side,
            boolean open, boolean cross) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        if (open) {
            int inset = cross ? 3 : 2;
            for (int w = inset; w < 11 - inset; w++) {
                for (int h = inset; h < 11 - inset; h++) {
                    BlockPos pos = passageSidePos(x, z, side, w + 3,
                            BASE_Y + 10 - h, 0);
                    set(level, pos.getX(), pos.getY(), pos.getZ(),
                            classicState(CONNECTION[h][w], side, cell, random));
                }
            }
            return;
        }
        for (int w = 1; w < 8; w++) {
            for (int h = 1; h < 8; h++) {
                int y = BASE_Y + 9 - h;
                BlockPos wall = passageSidePos(x, z, side, w + 3, y, 0);
                BlockPos nothing = passageSidePos(x, z, side, w + 3, y, 1);
                BlockPos shell = passageSidePos(x, z, side, w + 3, y, 2);
                /* TC4 sandwich: masonry, one void layer, outer seal. */
                set(level, wall.getX(), y, wall.getZ(), classicState(2, side, cell, random));
                set(level, nothing.getX(), y, nothing.getZ(), classicState(8, side, cell, random));
                set(level, shell.getX(), y, shell.getZ(), classicState(1, side, cell, random));
                if (h == 7) {
                    for (int yy : new int[]{BASE_Y + 1, BASE_Y + 9}) {
                        BlockPos edge = passageSidePos(x, z, side, w + 3, yy, 1);
                        set(level, edge.getX(), yy, edge.getZ(), classicState(1, side, cell, random));
                    }
                }
            }
        }
        for (int w = 2; w < 7; w++) {
            BlockPos lower = passageSidePos(x, z, side, w + 3, BASE_Y + 3, -1);
            BlockPos upper = passageSidePos(x, z, side, w + 3, BASE_Y + 7, -1);
            set(level, lower.getX(), lower.getY(), lower.getZ(),
                    classicState(10, side, cell, random));
            set(level, upper.getX(), upper.getY(), upper.getZ(),
                    classicState(11, side, cell, random));
        }
    }

    private static BlockPos passageSidePos(int x, int z, Direction side,
            int across, int y, int outward) {
        return switch (side) {
            case NORTH -> new BlockPos(x + across, y, z + 5 - outward);
            case SOUTH -> new BlockPos(x + across, y, z + 11 + outward);
            case WEST -> new BlockPos(x + 5 - outward, y, z + across);
            case EAST -> new BlockPos(x + 11 + outward, y, z + across);
            default -> throw new IllegalArgumentException("Horizontal side required");
        };
    }

    private static void generateClassicConnections(WorldGenLevel level,
            ChunkPos chunk, OuterLandsCell cell, Random random,
            int depth, boolean justTip) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            boolean connected = switch (side) {
                case NORTH -> cell.north(); case SOUTH -> cell.south();
                case WEST -> cell.west(); case EAST -> cell.east();
                default -> false;
            };
            if (!connected) continue;
            boolean[][] stairBlocked = new boolean[11][11];
            for (int d = 0; d <= depth; d++) {
                int start = justTip && d == depth ? 2
                        : justTip && d == depth - 1 ? 1 : 0;
                int end = justTip && d == depth ? 9
                        : justTip && d == depth - 1 ? 10 : 11;
                for (int w = start; w < end; w++) {
                    for (int h = start; h < end; h++) {
                        int code = CONNECTION[h][w];
                        if (d == depth && justTip && code == 8) continue;
                        int by = BASE_Y + 10 - h;
                        BlockPos target = classicConnectionPos(
                                chunk, side, d, w, by
                        );
                        BlockState state = classicState(
                                code, side, cell, random
                        );
                        if (state.is(ModBlocks.ANCIENT_STAIRS.get())) {
                            if (stairBlocked[h][w]) {
                                continue;
                            }
                            if (blocksConnectionStair(
                                    level,
                                    target,
                                    state
                            )) {
                                stairBlocked[h][w] = true;
                                makePreviousConnectionStairCorner(
                                        level, target, side, state
                                );
                                continue;
                            }
                        }
                        set(level, target.getX(), by, target.getZ(), state);
                    }
                }
            }
        }
    }

    static boolean blocksConnectionStair(
            WorldGenLevel level,
            BlockPos target,
            BlockState stair
    ) {
        return blocksConnectionStair(
                OuterLandsStairTopology.isAncientWall(
                        level.getBlockState(target)
                ),
                hasStairBacking(level, target, stair)
        );
    }

    static boolean blocksConnectionStair(
            boolean ancientWall,
            boolean ancientBacking
    ) {
        /*
         * A stair may replace masonry only when another masonry block remains
         * behind its high face.  Without that backing, the stair cutout exposes
         * Eldritch Nothing through the room's main wall.
         */
        return ancientWall && !ancientBacking;
    }

    private static boolean hasStairBacking(
            WorldGenLevel level,
            BlockPos position,
            BlockState stair
    ) {
        Direction facing = stair.getValue(StairBlock.FACING);
        return OuterLandsStairTopology.isAncientWall(
                level.getBlockState(position.relative(facing))
        );
    }

    private static BlockPos classicConnectionPos(
            ChunkPos chunk,
            Direction side,
            int depth,
            int width,
            int y
    ) {
        int x = switch (side) {
            case NORTH, SOUTH -> chunk.getMinBlockX() + 3 + width;
            case WEST -> chunk.getMinBlockX() + depth;
            case EAST -> chunk.getMinBlockX() + 16 - depth;
            default -> throw new IllegalArgumentException(
                    "Horizontal side required"
            );
        };
        int z = switch (side) {
            case WEST, EAST -> chunk.getMinBlockZ() + 3 + width;
            case NORTH -> chunk.getMinBlockZ() + depth;
            case SOUTH -> chunk.getMinBlockZ() + 16 - depth;
            default -> throw new IllegalArgumentException(
                    "Horizontal side required"
            );
        };
        return new BlockPos(x, y, z);
    }

    private static void makePreviousConnectionStairCorner(
            WorldGenLevel level,
            BlockPos wall,
            Direction side,
            BlockState attempted
    ) {
        Direction path = switch (side) {
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.NORTH;
            case WEST -> Direction.EAST;
            case EAST -> Direction.WEST;
            default -> throw new IllegalArgumentException(
                    "Horizontal side required"
            );
        };
        BlockPos previous = wall.relative(path.getOpposite());
        BlockState stair = level.getBlockState(previous);
        if (!stair.is(ModBlocks.ANCIENT_STAIRS.get())
                || stair.getValue(StairBlock.HALF)
                != attempted.getValue(StairBlock.HALF)) {
            return;
        }
        Direction facing = stair.getValue(StairBlock.FACING);
        StairsShape corner = path == facing.getCounterClockWise()
                ? StairsShape.INNER_LEFT
                : StairsShape.INNER_RIGHT;
        set(level, previous.getX(), previous.getY(), previous.getZ(),
                stair.setValue(StairBlock.SHAPE, corner));
    }

    private static BlockState classicState(int code, Direction direction,
            OuterLandsCell cell, Random random) {
        if (code == 2 && cell.feature() == 7 && random.nextInt(3) == 0) code = 21;
        return switch (code) {
            case 1, 99 -> ModBlocks.ANCIENT_SEAL.get().defaultBlockState();
            case 2 -> ModBlocks.ANCIENT_STONE.get().defaultBlockState();
            case 3, 4, 5, 6 -> classicConnectionStair(direction, code);
            case 7 -> ModBlocks.ANCIENT_STONE.get().defaultBlockState()
                    .setValue(AncientStoneBlock.VARIANT, 3);
            case 8 -> ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState();
            case 9 -> Blocks.AIR.defaultBlockState();
            case 10 -> ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                    .setValue(StairBlock.FACING, direction)
                    .setValue(StairBlock.HALF, Half.BOTTOM);
            case 11 -> ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                    .setValue(StairBlock.FACING, direction)
                    .setValue(StairBlock.HALF, Half.TOP);
            case 12 -> ModBlocks.ANCIENT_SLAB.get().defaultBlockState();
            case 18 -> ModBlocks.ANCIENT_ROCK.get().defaultBlockState();
            // TC4 meta 13 used the same es_1..es_4 masonry family as
            // ordinary eldritch stone; only its spawn rule differed.
            case 19 -> ModBlocks.ANCIENT_STONE.get().defaultBlockState();
            case 21 -> ModBlocks.ANCIENT_CRUST.get().defaultBlockState();
            default -> Blocks.AIR.defaultBlockState();
        };
    }

    private static BlockState classicConnectionStair(Direction side, int code) {
        Direction facing;
        if (side.getAxis() == Direction.Axis.Z) {
            facing = code == 3 || code == 5 ? Direction.WEST : Direction.EAST;
        } else {
            facing = code == 3 || code == 5 ? Direction.NORTH : Direction.SOUTH;
        }
        return ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, code >= 5 ? Half.TOP : Half.BOTTOM);
    }

    private static void generateRoomShell(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell,
            Random random
    ) {
        int startX = chunk.getMinBlockX();
        int startZ = chunk.getMinBlockZ();
        for (int x = 1; x <= 15; x++) {
            for (int z = 1; z <= 15; z++) {
                for (int y = BASE_Y; y <= BASE_Y + 12; y++) {
                    boolean boundary = x == 1 || x == 15 || z == 1 || z == 15;
                    if (boundary) {
                        set(level, startX + x, y, startZ + z,
                                ModBlocks.ANCIENT_SEAL.get().defaultBlockState());
                        continue;
                    }
                    if (x == 2 || x == 14 || z == 2 || z == 14) {
                        set(level, startX + x, y, startZ + z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        for (int x = 2; x <= 14; x++) {
            for (int z = 2; z <= 14; z++) {
                set(level, startX + x, BASE_Y, startZ + z,
                        ModBlocks.ANCIENT_SEAL.get().defaultBlockState());
                setAncient(level, startX + x, FLOOR_Y, startZ + z, random.nextLong());
                for (int y = BASE_Y + 2; y < CEILING_Y; y++) {
                    set(level, startX + x, y, startZ + z, Blocks.AIR.defaultBlockState());
                }
                setAncient(level, startX + x, CEILING_Y, startZ + z, random.nextLong());
                set(level, startX + x, BASE_Y + 12, startZ + z,
                        ModBlocks.ANCIENT_SEAL.get().defaultBlockState());
            }
        }
        for (int x = 3; x <= 13; x++) {
            for (int z = 3; z <= 13; z++) {
                if (x != 3 && x != 13 && z != 3 && z != 13) {
                    continue;
                }
                for (int y = BASE_Y + 2; y <= BASE_Y + 10; y++) {
                    setAncient(level, startX + x, y, startZ + z, random.nextLong());
                }
            }
        }
        carveDoorways(level, chunk, cell);
        addCardinalTrim(level, chunk);
    }

    private static void carveDoorways(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        int startX = chunk.getMinBlockX();
        int startZ = chunk.getMinBlockZ();
        if (cell.north()) {
            carve(level, startX + 4, startX + 11, BASE_Y + 2,
                    BASE_Y + 9, startZ + 1, true);
        }
        if (cell.south()) {
            carve(level, startX + 4, startX + 11, BASE_Y + 2,
                    BASE_Y + 9, startZ + 15, true);
        }
        if (cell.west()) {
            carve(level, startZ + 4, startZ + 11, BASE_Y + 2,
                    BASE_Y + 9, startX + 1, false);
        }
        if (cell.east()) {
            carve(level, startZ + 4, startZ + 11, BASE_Y + 2,
                    BASE_Y + 9, startX + 15, false);
        }
    }

    private static void carve(
            WorldGenLevel level,
            int horizontalStart,
            int horizontalEnd,
            int yStart,
            int yEnd,
            int fixed,
            boolean xAxis
    ) {
        for (int horizontal = horizontalStart;
                horizontal <= horizontalEnd; horizontal++) {
            for (int y = yStart; y <= yEnd; y++) {
                set(level,
                        xAxis ? horizontal : fixed,
                        y,
                        xAxis ? fixed : horizontal,
                        Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void generateConnections(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell
    ) {
        if (cell.north()) {
            connection(level, chunk, Direction.NORTH);
        }
        if (cell.south()) {
            connection(level, chunk, Direction.SOUTH);
        }
        if (cell.east()) {
            connection(level, chunk, Direction.EAST);
        }
        if (cell.west()) {
            connection(level, chunk, Direction.WEST);
        }
    }

    private static void connection(
            WorldGenLevel level,
            ChunkPos chunk,
            Direction direction
    ) {
        int startX = chunk.getMinBlockX();
        int startZ = chunk.getMinBlockZ();
        for (int depth = 0; depth < 4; depth++) {
            for (int width = 0; width < 11; width++) {
                for (int height = 0; height < 11; height++) {
                    int code = CONNECTION[height][width];
                    if (code == 9) {
                        code = 0;
                    }
                    int x;
                    int z;
                    if (direction == Direction.NORTH) {
                        x = startX + 3 + width;
                        z = startZ + 3 - depth;
                    } else if (direction == Direction.SOUTH) {
                        x = startX + 3 + width;
                        z = startZ + 13 + depth;
                    } else if (direction == Direction.WEST) {
                        x = startX + 3 - depth;
                        z = startZ + 3 + width;
                    } else {
                        x = startX + 13 + depth;
                        z = startZ + 3 + width;
                    }
                    BlockState state = switch (code) {
                        case 1 -> ModBlocks.ANCIENT_SEAL.get().defaultBlockState();
                        case 2 -> ancientState(new BlockPos(x, BASE_Y + height, z), 31L);
                        case 3, 4, 5, 6 -> stairForConnection(direction, code);
                        default -> Blocks.AIR.defaultBlockState();
                    };
                    set(level, x, BASE_Y + height, z, state);
                }
            }
        }
    }

    private static BlockState stairForConnection(
            Direction direction,
            int code
    ) {
        Direction facing = switch (code) {
            case 3, 5 -> direction.getClockWise();
            default -> direction.getCounterClockWise();
        };
        return ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF,
                        code >= 5 ? Half.TOP : Half.BOTTOM);
    }

    private static void addCardinalTrim(WorldGenLevel level, ChunkPos chunk) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int offset = 0; offset < 5; offset++) {
            placeStair(level, x + 6 + offset, BASE_Y + 2, z + 4,
                    Direction.NORTH, Half.BOTTOM);
            placeStair(level, x + 6 + offset, BASE_Y + 2, z + 12,
                    Direction.SOUTH, Half.BOTTOM);
            placeStair(level, x + 4, BASE_Y + 2, z + 6 + offset,
                    Direction.WEST, Half.BOTTOM);
            placeStair(level, x + 12, BASE_Y + 2, z + 6 + offset,
                    Direction.EAST, Half.BOTTOM);
            placeStair(level, x + 6 + offset, BASE_Y + 10, z + 4,
                    Direction.NORTH, Half.TOP);
            placeStair(level, x + 6 + offset, BASE_Y + 10, z + 12,
                    Direction.SOUTH, Half.TOP);
            placeStair(level, x + 4, BASE_Y + 10, z + 6 + offset,
                    Direction.WEST, Half.TOP);
            placeStair(level, x + 12, BASE_Y + 10, z + 6 + offset,
                    Direction.EAST, Half.TOP);
        }
    }

    private static void generateClassicPortalRoom(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random) {
        generateClassicFullRoomShell(level, chunk, cell, random, 0);
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                if (!((a <= 4 || a >= 12) && (b <= 4 || b >= 12))) continue;
                for (int c = 1; c < 12; c++) {
                    set(level, x + a, BASE_Y + c, z + b, Blocks.AIR.defaultBlockState());
                }
            }
        }
        placePortalCornerTrim(level, x, z, 5, 5, Direction.NORTH, Direction.WEST);
        placePortalCornerTrim(level, x, z, 11, 5, Direction.NORTH, Direction.EAST);
        placePortalCornerTrim(level, x, z, 5, 11, Direction.SOUTH, Direction.WEST);
        placePortalCornerTrim(level, x, z, 11, 11, Direction.SOUTH, Direction.EAST);
        generatePortalRoom(level, chunk);
    }

    private static void placePortalCornerTrim(WorldGenLevel level, int x, int z,
            int cornerX, int cornerZ, Direction zFacing, Direction xFacing) {
        int xOuter = cornerX < 8 ? cornerX - 1 : cornerX + 1;
        int zOuter = cornerZ < 8 ? cornerZ - 1 : cornerZ + 1;
        placeStair(level, x + cornerX, BASE_Y + 3, z + cornerZ, zFacing, Half.BOTTOM);
        placeStair(level, x + xOuter, BASE_Y + 3, z + cornerZ, zFacing, Half.BOTTOM);
        placeStair(level, x + cornerX, BASE_Y + 3, z + zOuter, xFacing, Half.BOTTOM);
        placeStair(level, x + cornerX, BASE_Y + 8, z + cornerZ, zFacing, Half.TOP);
        placeStair(level, x + xOuter, BASE_Y + 8, z + cornerZ, zFacing, Half.TOP);
        placeStair(level, x + cornerX, BASE_Y + 8, z + zOuter, xFacing, Half.TOP);
    }

    private static void generateClassicKeyRoom(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random) {
        generateClassicFullRoomShell(level, chunk, cell, random, 1);
        generateTabletRoom(level, chunk, random);
    }

    private static void generateClassicLibraryRoom(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random) {
        generateClassicFullRoomShell(level, chunk, cell, random, 2);
        generateLibraryRoom(level, chunk, random);
    }

    /** roomKind: 0 portal, 1 key, 2 library. Literal layer heights from TC4. */
    private static void generateClassicFullRoomShell(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random, int roomKind) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int a = 1; a <= 15; a++) {
            for (int b = 1; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if (a == 1 || a == 15 || b == 1 || b == 15) {
                        set(level, x + a, BASE_Y + c, z + b, classicState(1, Direction.UP, cell, random));
                    }
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                for (int c = 1; c < 12; c++) {
                    if ((a == 2 || a == 14 || b == 2 || b == 14)
                            && !classicOpening(cell, a, b, c)) {
                        set(level, x + a, BASE_Y + c, z + b, classicState(8, Direction.UP, cell, random));
                    }
                }
            }
        }
        for (int a = 3; a <= 13; a++) {
            for (int b = 3; b <= 13; b++) {
                for (int c = 2; c < 11; c++) {
                    if (a != 3 && a != 13 && b != 3 && b != 13) continue;
                    int code = 2;
                    if (roomKind == 0 && (a <= 4 || a >= 12) && (b <= 4 || b >= 12)) continue;
                    if (roomKind == 1) {
                        boolean inset = (c > 3 && c < 9 && (a == 8 || b == 8))
                                || (c > 4 && c < 8 && (a == 7 || b == 7 || a == 9 || b == 9));
                        if (inset && (a == 8 || b == 8) && c == 6) {
                            /* TC4 leaves the centre open to the VOID layer. */
                            continue;
                        }
                        code = inset ? 19 : 18;
                    }
                    set(level, x + a, BASE_Y + c, z + b, classicState(code, Direction.UP, cell, random));
                }
            }
        }
        for (int a = 2; a <= 14; a++) {
            for (int b = 2; b <= 14; b++) {
                set(level, x + a, BASE_Y - 1, z + b, classicState(1, Direction.UP, cell, random));
                set(level, x + a, BASE_Y, z + b, classicState(8, Direction.UP, cell, random));
                set(level, x + a, BASE_Y + 1, z + b,
                        classicState(roomKind == 0 ? 19 : 2, Direction.UP, cell, random));
                int roofSeal = roomKind == 2 ? BASE_Y + 12 : BASE_Y + 13;
                int roofVoid = roomKind == 2 ? BASE_Y + 11 : BASE_Y + 12;
                int roofStone = roomKind == 2 ? BASE_Y + 10 : BASE_Y + 11;
                set(level, x + a, roofSeal, z + b, classicState(1, Direction.UP, cell, random));
                set(level, x + a, roofVoid, z + b, classicState(8, Direction.UP, cell, random));
                set(level, x + a, roofStone, z + b, classicState(2, Direction.UP, cell, random));
                if (roomKind != 2) {
                    int q = Math.min(Math.abs(8 - a), Math.abs(8 - b));
                    for (int g = 0; g < q - 1; g++) {
                        set(level, x + a, BASE_Y + 1 + g, z + b,
                                classicState(roomKind == 0 ? 19 : 2, Direction.UP, cell, random));
                    }
                    if (a > 3 && a < 13 && b > 3 && b < 13) {
                        for (int g = 0; g < q; g++) {
                            set(level, x + a, BASE_Y + 11 - g, z + b,
                                    classicState(roomKind == 0 ? 19 : 2, Direction.UP, cell, random));
                        }
                    }
                }
            }
        }
        placeClassicLowerStairs(level, chunk);
        generateClassicConnections(level, chunk, cell, random, 3, true);
    }

    private static boolean classicOpening(OuterLandsCell cell, int a, int b, int c) {
        return c < 10 && ((a == 2 && b > 3 && b < 12 && cell.west())
                || (a == 14 && b > 3 && b < 12 && cell.east())
                || (b == 2 && a > 3 && a < 12 && cell.north())
                || (b == 14 && a > 3 && a < 12 && cell.south()));
    }

    private static void placeClassicLowerStairs(WorldGenLevel level, ChunkPos chunk) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int g = 0; g < 5; g++) {
            placeStair(level, x + 6 + g, BASE_Y + 2, z + 4, Direction.NORTH, Half.BOTTOM);
            placeStair(level, x + 6 + g, BASE_Y + 2, z + 12, Direction.SOUTH, Half.BOTTOM);
            placeStair(level, x + 12, BASE_Y + 2, z + 6 + g, Direction.EAST, Half.BOTTOM);
            placeStair(level, x + 4, BASE_Y + 2, z + 6 + g, Direction.WEST, Half.BOTTOM);
        }
    }

    private static void generateClassicNestRoom(WorldGenLevel level, ChunkPos chunk,
            OuterLandsCell cell, Random random) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int a = 1; a <= 15; a++) for (int b = 1; b <= 15; b++) for (int c = 0; c < 11; c++) {
            if (a == 1 || a == 15 || b == 1 || b == 15)
                set(level, x + a, BASE_Y + c, z + b, classicState(1, Direction.UP, cell, random));
        }
        for (int a = 2; a <= 14; a++) for (int b = 2; b <= 14; b++) {
            for (int c = 1; c < 10; c++) {
                if ((a == 2 || a == 14 || b == 2 || b == 14) && !classicOpening(cell, a, b, c))
                    set(level, x + a, BASE_Y + c, z + b, classicState(8, Direction.UP, cell, random));
            }
            set(level, x + a, BASE_Y - 1, z + b, classicState(1, Direction.UP, cell, random));
            set(level, x + a, BASE_Y, z + b, classicState(8, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 1, z + b, classicState(21, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 11, z + b, classicState(1, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 10, z + b, classicState(8, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 9, z + b, classicState(21, Direction.UP, cell, random));
            if (random.nextBoolean()) set(level, x + a, BASE_Y + 8, z + b, classicState(21, Direction.UP, cell, random));
        }
        for (int a = 3; a <= 13; a++) for (int b = 3; b <= 13; b++) for (int c = 2; c < 9; c++) {
            if (a == 3 || a == 13 || b == 3 || b == 13
                    || (((a == 4 && !cell.west()) || (a == 12 && !cell.east())
                    || (b == 4 && !cell.north()) || (b == 12 && !cell.south())) && random.nextBoolean()))
                set(level, x + a, BASE_Y + c, z + b, classicState(21, Direction.UP, cell, random));
        }
        int[][] pillar = {{8,2,8},{8,3,8},{8,4,8},{7,2,8},{9,2,8},{8,2,7},{8,2,9},
                {8,8,8},{8,7,8},{8,6,8},{7,8,8},{9,8,8},{8,8,7},{8,8,9}};
        for (int[] p : pillar) set(level, x + p[0], BASE_Y + p[1], z + p[2], classicState(21, Direction.UP, cell, random));
        generateClassicConnections(level, chunk, cell, random, 3, true);
        generateNestRoom(level, chunk, random);
    }

    private static void generatePortalRoom(WorldGenLevel level, ChunkPos chunk) {
        int x = chunk.getMinBlockX() + 8;
        int z = chunk.getMinBlockZ() + 8;
        set(level, x, BASE_Y + 2, z,
                ModBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState());
        set(level, x, BASE_Y + 3, z,
                ModBlocks.OUTER_LANDS_PORTAL.get().defaultBlockState());
        // Part 1 owns the complete four-block-tall animated obelisk renderer.
        set(level, x, BASE_Y + 4, z,
                ModBlocks.ELDRITCH_ALTAR_PART.get().defaultBlockState()
                        .setValue(com.thaumcraftmodern.world.block.EldritchAltarPartBlock.PART, 1));
    }

    private static void generateTabletRoom(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int centerX = chunk.getMinBlockX() + 8;
        int centerZ = chunk.getMinBlockZ() + 8;
        set(level, centerX, BASE_Y + 2, centerZ,
                ModBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState());
        BlockPos pedestalPosition = new BlockPos(
                centerX, BASE_Y + 2, centerZ
        );
        if (level.getBlockEntity(pedestalPosition)
                instanceof ArcanePedestalBlockEntity pedestal) {
            /*
             * TC4 used EntityPermanentItem here. WorldGenRegion discards that
             * entity in 1.20.1, so the pedestal inventory is the durable
             * modern equivalent and renders the tablet in the same place.
             */
            pedestal.setItem(0, new ItemStack(ModItems.RUNED_TABLET.get()));
        }

        int guardians = 2 + (level.getDifficulty() == Difficulty.HARD
                ? 2 : level.getDifficulty() == Difficulty.NORMAL ? 1 : 0);
        for (int index = 0; index < guardians; index++) {
            spawnMob(level, ModEntities.ELDRITCH_GUARDIAN.get(),
                    centerX + random.nextInt(7) - 3,
                    BASE_Y + 2,
                    centerZ + random.nextInt(7) - 3,
                    new BlockPos(centerX, BASE_Y + 2, centerZ));
        }
    }

    private static void generateNestRoom(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int startX = chunk.getMinBlockX();
        int startZ = chunk.getMinBlockZ();
        for (int a = -5; a <= 5; a++) {
            for (int b = -5; b <= 5; b++) {
                BlockPos target = new BlockPos(startX + 8 + a, BASE_Y + 2, startZ + 8 + b);
                if (random.nextFloat() < 0.15F && level.getBlockState(target).isAir()) {
                    set(level, target.getX(), target.getY(), target.getZ(),
                            ModBlocks.LOOT_URN.get().defaultBlockState());
                }
            }
        }
    }

    private static void generateLibraryRoom(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        int[][] corners = {{5,5},{5,11},{11,5},{11,11}};
        for (int a = 4; a <= 12; a++) {
            for (int b = 4; b <= 12; b++) {
                boolean corner = (a <= 5 || a >= 11) && (b <= 5 || b >= 11);
                if (corner) {
                    set(level, x + a, BASE_Y + 2, z + b,
                            ModBlocks.ANCIENT_STONE.get().defaultBlockState());
                    set(level, x + a, BASE_Y + 9, z + b,
                            ModBlocks.ANCIENT_STONE.get().defaultBlockState());
                }
            }
        }
        for (int g = 0; g < 5; g++) {
            placeStair(level, x + 6 + g, BASE_Y + 9, z + 4, Direction.NORTH, Half.TOP);
            placeStair(level, x + 6 + g, BASE_Y + 9, z + 12, Direction.SOUTH, Half.TOP);
            placeStair(level, x + 12, BASE_Y + 9, z + 6 + g, Direction.EAST, Half.TOP);
            placeStair(level, x + 4, BASE_Y + 9, z + 6 + g, Direction.WEST, Half.TOP);
        }
        for (int[] corner : corners) {
            set(level, x + corner[0], BASE_Y + 3, z + corner[1],
                    ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState());
            set(level, x + corner[0], BASE_Y + 4, z + corner[1],
                    ModBlocks.ANCIENT_ROCK.get().defaultBlockState());
            set(level, x + corner[0], BASE_Y + 5, z + corner[1],
                    ModBlocks.ANCIENT_SLAB.get().defaultBlockState());
            set(level, x + corner[0], BASE_Y + 8, z + corner[1],
                    ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState());
            set(level, x + corner[0], BASE_Y + 7, z + corner[1],
                    ModBlocks.ANCIENT_ROCK.get().defaultBlockState());
            set(level, x + corner[0], BASE_Y + 6, z + corner[1],
                    ModBlocks.ANCIENT_SLAB.get().defaultBlockState()
                            .setValue(SlabBlock.TYPE, SlabType.TOP));
        }
        set(level, x + 8, BASE_Y + 2, z + 8,
                ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState());
        set(level, x + 8, BASE_Y + 3, z + 8,
                ModBlocks.ANCIENT_ROCK.get().defaultBlockState());
        set(level, x + 8, BASE_Y + 4, z + 8,
                ModBlocks.ANCIENT_SLAB.get().defaultBlockState());
        set(level, x + 8, BASE_Y + 9, z + 8,
                ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState());
        set(level, x + 8, BASE_Y + 8, z + 8,
                ModBlocks.ANCIENT_ROCK.get().defaultBlockState());
        set(level, x + 8, BASE_Y + 7, z + 8,
                ModBlocks.ANCIENT_SLAB.get().defaultBlockState()
                        .setValue(SlabBlock.TYPE, SlabType.TOP));
    }

    private static void generatePillaredPassage(
            WorldGenLevel level,
            ChunkPos chunk
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        int[] offsets = {5, 11};
        for (int px : offsets) {
            for (int pz : offsets) {
                for (int y = BASE_Y + 2; y <= BASE_Y + 9; y++) {
                    set(level, x + px, y, z + pz,
                            ModBlocks.ANCIENT_ROCK.get().defaultBlockState());
                }
            }
        }
    }

    private static void generateTrappedPassage(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        decoratePassage(level, chunk, random);
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int offset = 5; offset <= 11; offset += 2) {
            set(level, x + offset, BASE_Y + 2, z + 8,
                    Blocks.CRYING_OBSIDIAN.defaultBlockState());
        }
    }

    private static void generateCrustedRoom(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int index = 0; index < 42; index++) {
            BlockPos target = new BlockPos(
                    x + 3 + random.nextInt(11),
                    BASE_Y + 2 + random.nextInt(9),
                    z + 3 + random.nextInt(11)
            );
            if (!level.getBlockState(target).isAir()) {
                set(level, target.getX(), target.getY(), target.getZ(),
                        ModBlocks.ANCIENT_CRUST.get().defaultBlockState());
            }
        }
    }

    private static void generateTaintRoom(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        for (int w = -4; w <= 4; w++) {
            for (int h = -3; h <= 3; h++) {
                for (int j = -4; j <= 4; j++) {
                    BlockPos target = new BlockPos(x + 8 + w, BASE_Y + 4 + h, z + 8 + j);
                    if (level.getBlockState(target).isAir()
                            && hasSolidNeighbor(level, target)
                            && random.nextInt(3) != 0) {
                        set(level, target.getX(), target.getY(), target.getZ(),
                                ModBlocks.TAINT_FIBRES.get().defaultBlockState());
                    }
                }
            }
        }
    }

    private static void generateWebRoom(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        BlockPos center = new BlockPos(x + 8, BASE_Y + 4, z + 8);
        for (int w = -3; w <= 3; w++) {
            for (int h = -3; h <= 3; h++) {
                for (int j = -3; j <= 3; j++) {
                    BlockPos target = center.offset(w, h, j);
                    if (!target.equals(center) && level.getBlockState(target).isAir()
                            && random.nextFloat() < 0.35F) {
                        set(level, target.getX(), target.getY(), target.getZ(),
                                Blocks.COBWEB.defaultBlockState());
                    }
                }
            }
        }
        set(level, center.getX(), center.getY(), center.getZ(), Blocks.SPAWNER.defaultBlockState());
        OuterLandsMindSpiderSpawners.configure(level, center);
    }

    private static boolean hasSolidNeighbor(WorldGenLevel level, BlockPos position) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = position.relative(direction);
            if (level.getBlockState(neighbor).isSolidRender(level, neighbor)) {
                return true;
            }
        }
        return false;
    }

    private static void decoratePassage(
            WorldGenLevel level,
            ChunkPos chunk,
            Random random
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        if (random.nextInt(4) == 0) {
            set(level, x + 5, BASE_Y + 2, z + 5,
                    ModBlocks.LOOT_URN.get().defaultBlockState());
        }
        if (random.nextInt(7) == 0) {
            spawnMob(level, ModEntities.ELDRITCH_CRAB.get(), x + 8,
                    BASE_Y + 2, z + 8,
                    new BlockPos(x + 8, BASE_Y + 2, z + 8));
        }
    }

    private static void generateBossQuadrant(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsCell cell,
            Random random
    ) {
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        boolean westEdge = cell.feature() == 2 || cell.feature() == 4;
        boolean eastEdge = !westEdge;
        boolean northEdge = cell.feature() == 2 || cell.feature() == 3;
        boolean southEdge = !northEdge;
        int minX = westEdge ? 2 : 0;
        int maxX = eastEdge ? 14 : 15;
        int minZ = northEdge ? 2 : 0;
        int maxZ = southEdge ? 14 : 15;

        for (int a = westEdge ? 1 : 0; a <= 15; a++) {
            for (int b = northEdge ? 1 : 0; b <= 15; b++) {
                for (int c = 0; c < 13; c++) {
                    if ((westEdge && a == 1) || (eastEdge && a == 15)
                            || (northEdge && b == 1) || (southEdge && b == 15)) {
                        set(level, x + a, BASE_Y + c, z + b,
                                classicState(1, Direction.UP, cell, random));
                    }
                }
            }
        }
        for (int a = minX; a <= maxX; a++) for (int b = minZ; b <= maxZ; b++) {
            for (int c = 1; c < 12; c++) {
                boolean voidWall = (westEdge && a == 2) || (eastEdge && a == 14)
                        || (northEdge && b == 2) || (southEdge && b == 14);
                if (voidWall && !classicOpening(cell, a, b, c)) {
                    set(level, x + a, BASE_Y + c, z + b,
                            classicState(8, Direction.UP, cell, random));
                }
            }
            set(level, x + a, BASE_Y - 1, z + b, classicState(1, Direction.UP, cell, random));
            set(level, x + a, BASE_Y, z + b, classicState(8, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 1, z + b, classicState(19, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 13, z + b, classicState(1, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 12, z + b, classicState(8, Direction.UP, cell, random));
            set(level, x + a, BASE_Y + 11, z + b, classicState(2, Direction.UP, cell, random));
        }
        for (int a = westEdge ? 3 : 0; a <= (eastEdge ? 13 : 15); a++) {
            for (int b = northEdge ? 3 : 0; b <= (southEdge ? 13 : 15); b++) {
                if (!((westEdge && a == 3) || (eastEdge && a == 13)
                        || (northEdge && b == 3) || (southEdge && b == 13))) continue;
                for (int c = 2; c < 11; c++) {
                    set(level, x + a, BASE_Y + c, z + b,
                            classicState(18, Direction.UP, cell, random));
                }
            }
        }
        int acrossMin = westEdge ? 4 : 0;
        int acrossMax = eastEdge ? 11 : 15;
        if (northEdge || southEdge) for (int g = acrossMin; g <= acrossMax; g++) {
            int sz = northEdge ? 4 : 12;
            Direction facing = northEdge ? Direction.NORTH : Direction.SOUTH;
            placeStair(level, x + g, BASE_Y + 2, z + sz, facing, Half.BOTTOM);
            placeStair(level, x + g, BASE_Y + 10, z + sz, facing, Half.TOP);
        }
        int verticalMin = northEdge ? 4 : 0;
        int verticalMax = southEdge ? 11 : 15;
        if (westEdge || eastEdge) for (int g = verticalMin; g <= verticalMax; g++) {
            int sx = westEdge ? 4 : 12;
            Direction facing = westEdge ? Direction.WEST : Direction.EAST;
            placeStair(level, x + sx, BASE_Y + 2, z + g, facing, Half.BOTTOM);
            placeStair(level, x + sx, BASE_Y + 10, z + g, facing, Half.TOP);
        }
    }

    private static void generateBossDoor(
            WorldGenLevel level,
            ChunkPos chunk,
            OuterLandsMaze.RegionCell located,
            OuterLandsCell cell
    ) {
        Direction direction = cell.north() ? Direction.NORTH
                : cell.south() ? Direction.SOUTH
                : cell.east() ? Direction.EAST : Direction.WEST;
        int x = chunk.getMinBlockX();
        int z = chunk.getMinBlockZ();
        int fixedX = direction == Direction.EAST ? x + 13
                : direction == Direction.WEST ? x + 3 : x + 8;
        int fixedZ = direction == Direction.NORTH ? z + 3
                : direction == Direction.SOUTH ? z + 13 : z + 8;
        for (int horizontal = -3; horizontal <= 3; horizontal++) {
            for (int vertical = -3; vertical <= 3; vertical++) {
                int px = direction.getAxis() == Direction.Axis.Z
                        ? fixedX + horizontal : fixedX;
                int pz = direction.getAxis() == Direction.Axis.X
                        ? fixedZ + horizontal : fixedZ;
                int part = BOSS_DOOR_PATTERN[horizontal + 3][vertical + 3];
                BlockState doorState = switch (part) {
                    case 1 -> ModBlocks.ELDRITCH_LOCK.get().defaultBlockState()
                            .setValue(EldritchLockBlock.FACING, direction);
                    case 2 -> ModBlocks.ELDRITCH_DOOR.get().defaultBlockState();
                    case 9 -> ModBlocks.ELDRITCH_BARRIER.get().defaultBlockState();
                    default -> Blocks.AIR.defaultBlockState();
                };
                set(level, px, BASE_Y + 5 + vertical, pz, doorState);
            }
        }
        BlockPos lockPosition = new BlockPos(fixedX, BASE_Y + 5, fixedZ);
        if (level.getBlockEntity(lockPosition)
                instanceof EldritchLockBlockEntity lock) {
            lock.setBossCenter(new BlockPos(
                    located.bossCenterBlockX(),
                    BASE_Y + 3,
                    located.bossCenterBlockZ()
            ));
        }
    }

    private static void spawnMob(
            WorldGenLevel level,
            net.minecraft.world.entity.EntityType<LegacyThaumcraftMob> type,
            int x,
            int y,
            int z,
            BlockPos home
    ) {
        LegacyThaumcraftMob mob = type.create(level.getLevel());
        if (mob == null) {
            return;
        }
        mob.moveTo(x + 0.5D, y, z + 0.5D,
                level.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.STRUCTURE, null, null);
        mob.restrictTo(home, 16);
        level.addFreshEntity(mob);
    }

    private static void placeStair(
            WorldGenLevel level,
            int x,
            int y,
            int z,
            Direction wallDirection,
            Half half
    ) {
        /*
         * Call sites describe the wall the trim belongs to.  Vanilla stair
         * facing points towards the high/back edge, so it must point at that
         * wall.  The low/open face then looks into the passage at the player,
         * matching TC4's legacy metadata mapping.
         */
                set(level, x, y, z,
                ModBlocks.ANCIENT_STAIRS.get().defaultBlockState()
                        .setValue(StairBlock.FACING, wallDirection)
                        .setValue(StairBlock.HALF, half));
    }

    private static void setAncient(
            WorldGenLevel level,
            int x,
            int y,
            int z,
            long salt
    ) {
        set(level, x, y, z, ancientState(new BlockPos(x, y, z), salt));
    }

    private static BlockState ancientState(BlockPos position, long salt) {
        return ((AncientStoneBlock) ModBlocks.ANCIENT_STONE.get())
                .stateFor(position, salt);
    }

    private static void set(
            WorldGenLevel level,
            int x,
            int y,
            int z,
            BlockState state
    ) {
        int flags = state.is(ModBlocks.ANCIENT_STAIRS.get())
                ? net.minecraft.world.level.block.Block.UPDATE_ALL
                : net.minecraft.world.level.block.Block.UPDATE_CLIENTS;
        level.setBlock(new BlockPos(x, y, z), state, flags);
    }
}
