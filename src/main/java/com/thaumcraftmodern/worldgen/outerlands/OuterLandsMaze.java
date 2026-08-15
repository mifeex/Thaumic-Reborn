package com.thaumcraftmodern.worldgen.outerlands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Deterministic port of TC4 4.2.3.5 {@code MazeGenerator}.
 *
 * <p>The packed low nibble contains N/S/E/W exits and the high byte contains
 * the original room feature id. Each maze retains the central portal room,
 * one runed-tablet room and the four-quadrant boss arena.</p>
 */
public final class OuterLandsMaze {
    public static final int NORTH = 1;
    public static final int SOUTH = 2;
    public static final int EAST = 4;
    public static final int WEST = 8;
    public static final int MIN_SIZE = 15;
    public static final int SIZE_VARIANTS = 8;
    public static final int REGION_SIZE_CHUNKS = 48;

    private final int width;
    private final int height;
    private final int[][] cells;

    private OuterLandsMaze(int width, int height, int[][] cells) {
        this.width = width;
        this.height = height;
        this.cells = cells;
    }

    public static OuterLandsMaze forRegion(
            long worldSeed,
            int regionX,
        int regionZ
    ) {
        long regionSeed = mix(worldSeed, regionX, regionZ);
        Random layoutRandom = new Random(regionSeed);
        int width = MIN_SIZE + layoutRandom.nextInt(SIZE_VARIANTS) * 2;
        int height = MIN_SIZE + layoutRandom.nextInt(SIZE_VARIANTS) * 2;
        long seed = layoutRandom.nextLong();
        for (int attempt = 0; attempt < 1024; attempt++) {
            Generator generator = new Generator(width, height, seed + attempt);
            if (generator.generate()) {
                return new OuterLandsMaze(width, height, generator.grid);
            }
        }
        throw new IllegalStateException("Unable to generate TC4 Outer Lands maze");
    }

    public static RegionCell at(long worldSeed, int chunkX, int chunkZ) {
        int regionX = Math.floorDiv(chunkX, REGION_SIZE_CHUNKS);
        int regionZ = Math.floorDiv(chunkZ, REGION_SIZE_CHUNKS);
        int centerX = regionX * REGION_SIZE_CHUNKS
                + REGION_SIZE_CHUNKS / 2;
        int centerZ = regionZ * REGION_SIZE_CHUNKS
                + REGION_SIZE_CHUNKS / 2;
        OuterLandsMaze maze = forRegion(worldSeed, regionX, regionZ);
        int originX = centerX - (1 + maze.width / 2);
        int originZ = centerZ - (1 + maze.height / 2);
        int column = chunkX - originX;
        int row = chunkZ - originZ;
        if (column < 0 || column >= maze.width
                || row < 0 || row >= maze.height) {
            return new RegionCell(
                    regionX,
                    regionZ,
                    originX,
                    originZ,
                    column,
                    row,
                    null
            );
        }
        int packed = maze.cells[row][column];
        return new RegionCell(
                regionX,
                regionZ,
                originX,
                originZ,
                column,
                row,
                packed > 0 ? OuterLandsCell.unpack(packed) : null
        );
    }

    public OuterLandsCell cell(int column, int row) {
        if (column < 0 || column >= width || row < 0 || row >= height) {
            return null;
        }
        int packed = cells[row][column];
        return packed > 0 ? OuterLandsCell.unpack(packed) : null;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private static long mix(long seed, int x, int z) {
        long mixed = seed;
        mixed ^= (long) x * 341873128712L;
        mixed ^= (long) z * 132897987541L;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdl;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53l;
        return mixed ^ mixed >>> 33;
    }

    public record RegionCell(
            int regionX,
            int regionZ,
            int mazeOriginChunkX,
            int mazeOriginChunkZ,
            int column,
            int row,
            OuterLandsCell cell
    ) {
        public boolean exists() {
            return cell != null;
        }

        public int bossCenterBlockX() {
            return (mazeOriginChunkX + bossUpperLeftColumn() + 1) * 16;
        }

        public int bossCenterBlockZ() {
            return (mazeOriginChunkZ + bossUpperLeftRow() + 1) * 16;
        }

        private int bossUpperLeftColumn() {
            return switch (cell == null ? 0 : cell.feature()) {
                case 2, 4 -> column;
                case 3, 5 -> column - 1;
                default -> column;
            };
        }

        private int bossUpperLeftRow() {
            return switch (cell == null ? 0 : cell.feature()) {
                case 2, 3 -> row;
                case 4, 5 -> row - 1;
                default -> row;
            };
        }
    }

    private static final class Generator {
        private final int width;
        private final int height;
        private final Random random;
        private final int[][] grid;

        private Generator(int width, int height, long seed) {
            this.width = width;
            this.height = height;
            this.random = new Random(seed);
            this.grid = new int[height][width];
        }

        private boolean generate() {
            int bossX;
            int bossY;
            switch (random.nextInt(4)) {
                case 0 -> {
                    bossX = 0;
                    bossY = 0;
                }
                case 1 -> {
                    bossX = width - 2;
                    bossY = height - 2;
                }
                case 2 -> {
                    bossX = width - 2;
                    bossY = 0;
                }
                default -> {
                    bossX = 0;
                    bossY = height - 2;
                }
            }
            grid[bossY][bossX] = 2 << 8;
            grid[bossY][bossX + 1] = 3 << 8;
            grid[bossY + 1][bossX] = 4 << 8;
            grid[bossY + 1][bossX + 1] = 5 << 8;

            int portalX = 1 + width / 2;
            int portalY = 1 + height / 2;
            grid[portalY][portalX] = 1 << 8;

            int blockedAttempts = (width + height) / 4;
            for (int index = 0; index < blockedAttempts; index++) {
                int size = 1 + random.nextInt(3);
                if (size > 2) {
                    blockedAttempts--;
                }
                int startX = random.nextInt(width - size);
                int startY = random.nextInt(height - size);
                for (int x = startX; x < startX + size; x++) {
                    for (int y = startY; y < startY + size; y++) {
                        if (grid[y][x] == 0) {
                            grid[y][x] = -1;
                        }
                    }
                }
            }

            List<Integer> directions = new ArrayList<>(
                    List.of(NORTH, SOUTH, EAST, WEST)
            );
            Collections.shuffle(directions, random);
            int first = directions.get(0);
            int firstX = portalX + dx(first);
            int firstY = portalY + dy(first);
            grid[portalY][portalX] |= first;
            if (grid[firstY][firstX] < 0) {
                grid[firstY][firstX] = 0;
            }
            grid[firstY][firstX] |= opposite(first);
            List<Location> frontier = new ArrayList<>();
            frontier.add(new Location(firstX, firstY));

            boolean success = false;
            while (!frontier.isEmpty()) {
                int index = nextIndex(frontier.size());
                Location location = frontier.get(index);
                Collections.shuffle(directions, random);
                boolean carved = false;
                for (int direction : directions) {
                    int nextX = location.x + dx(direction);
                    int nextY = location.y + dy(direction);
                    if (nextX <= 0 || nextX >= width - 1
                            || nextY <= 0 || nextY >= height - 1) {
                        continue;
                    }
                    if (grid[nextY][nextX] == 0) {
                        grid[location.y][location.x] |= direction;
                        grid[nextY][nextX] |= opposite(direction);
                        frontier.add(new Location(nextX, nextY));
                        carved = true;
                    }
                    if (carved) {
                        success = true;
                        break;
                    }
                }
                if (!carved) {
                    frontier.remove(index);
                }
            }
            if (!success) {
                return false;
            }

            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    if (grid[row][column] < 0) {
                        grid[row][column] = 0;
                    }
                }
            }

            Collections.shuffle(directions, random);
            for (int direction : directions) {
                int x = portalX + dx(direction);
                int y = portalY + dy(direction);
                if (x <= 0 || x >= width - 1 || y <= 0 || y >= height - 1
                        || grid[y][x] <= 0 || !random.nextBoolean()) {
                    continue;
                }
                grid[y][x] |= opposite(direction);
                grid[portalY][portalX] |= direction;
            }

            if (!connectBossRoom(bossX, bossY, directions)) {
                return false;
            }

            List<Location> deadEnds = new ArrayList<>();
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    OuterLandsCell cell = OuterLandsCell.unpack(
                            grid[row][column]
                    );
                    if (cell.exitCount() == 1 && cell.feature() == 0) {
                        deadEnds.add(new Location(column, row));
                    }
                }
            }
            if (deadEnds.isEmpty()) {
                return false;
            }

            int keyIndex = random.nextInt(deadEnds.size());
            Location key = deadEnds.remove(keyIndex);
            grid[key.y][key.x] = withFeature(grid[key.y][key.x], 6);

            int specialRooms = deadEnds.size() / 2;
            for (int index = 0; index < specialRooms && !deadEnds.isEmpty(); index++) {
                Location room = deadEnds.remove(random.nextInt(deadEnds.size()));
                grid[room.y][room.x] = withFeature(
                        grid[room.y][room.x],
                        7 + random.nextInt(3)
                );
            }

            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    OuterLandsCell cell = OuterLandsCell.unpack(
                            grid[row][column]
                    );
                    if (cell.feature() != 0 || cell.exitCount() == 0
                            || random.nextInt(25) != 0) {
                        continue;
                    }
                    int feature = switch (random.nextInt(8)) {
                        case 0 -> 8;
                        case 1 -> 10;
                        case 2, 3 -> 11;
                        case 4, 5 -> 12;
                        case 6 -> 13;
                        default -> 14;
                    };
                    grid[row][column] = withFeature(
                            grid[row][column], feature
                    );
                }
            }
            return true;
        }

        private boolean connectBossRoom(
                int bossX,
                int bossY,
                List<Integer> directions
        ) {
            Collections.shuffle(directions, random);
            for (int offsetX = 0; offsetX < 2; offsetX++) {
                for (int offsetY = 0; offsetY < 2; offsetY++) {
                    for (int direction : directions) {
                        int nextX = bossX + offsetX + dx(direction);
                        int nextY = bossY + offsetY + dy(direction);
                        if (nextX <= 0 || nextX >= width - 1
                                || nextY <= 0 || nextY >= height - 1
                                || grid[nextY][nextX] <= 0) {
                            continue;
                        }
                        OuterLandsCell neighbor = OuterLandsCell.unpack(
                                grid[nextY][nextX]
                        );
                        if (neighbor.feature() != 0) {
                            continue;
                        }
                        grid[nextY][nextX] |= opposite(direction);
                        grid[bossY + offsetY][bossX + offsetX] |= direction;
                        return true;
                    }
                }
            }
            return carveBossConnection(bossX, bossY, directions);
        }

        private boolean carveBossConnection(
                int bossX,
                int bossY,
                List<Integer> directions
        ) {
            for (int offsetX = 0; offsetX < 2; offsetX++) {
                for (int offsetY = 0; offsetY < 2; offsetY++) {
                    Collections.shuffle(directions, random);
                    for (int entranceDirection : directions) {
                        int startX = bossX + offsetX + dx(entranceDirection);
                        int startY = bossY + offsetY + dy(entranceDirection);
                        if (!inside(startX, startY) || grid[startY][startX] != 0) {
                            continue;
                        }
                        List<Location> frontier = new ArrayList<>();
                        frontier.add(new Location(startX, startY));
                        while (!frontier.isEmpty()) {
                            int index = nextIndex(frontier.size());
                            Location location = frontier.get(index);
                            Collections.shuffle(directions, random);
                            boolean progressed = false;
                            for (int direction : directions) {
                                int nextX = location.x + dx(direction);
                                int nextY = location.y + dy(direction);
                                if (!inside(nextX, nextY)) {
                                    continue;
                                }
                                if (grid[nextY][nextX] == 0) {
                                    grid[location.y][location.x] |= direction;
                                    grid[nextY][nextX] |= opposite(direction);
                                    frontier.add(new Location(nextX, nextY));
                                    progressed = true;
                                    break;
                                }
                                OuterLandsCell neighbor = OuterLandsCell.unpack(
                                        grid[nextY][nextX]
                                );
                                if (neighbor.feature() == 0) {
                                    grid[location.y][location.x] |= direction;
                                    grid[nextY][nextX] |= opposite(direction);
                                    grid[startY][startX] |= opposite(
                                            entranceDirection
                                    );
                                    grid[bossY + offsetY][bossX + offsetX]
                                            |= entranceDirection;
                                    return true;
                                }
                            }
                            if (!progressed) {
                                frontier.remove(index);
                            }
                        }
                    }
                }
            }
            return false;
        }

        private boolean inside(int x, int y) {
            return x > 0 && x < width - 1 && y > 0 && y < height - 1;
        }

        private int nextIndex(int size) {
            float roll = random.nextFloat();
            if (roll <= 0.45F) {
                return size - 1;
            }
            if (roll <= 0.9F) {
                return random.nextInt(size);
            }
            return 0;
        }

        private static int withFeature(int packed, int feature) {
            return packed & 0xff | feature << 8;
        }
    }

    private static int opposite(int direction) {
        return switch (direction) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
            default -> throw new IllegalArgumentException(
                    "Unknown maze direction " + direction
            );
        };
    }

    private static int dx(int direction) {
        return direction == EAST ? 1 : direction == WEST ? -1 : 0;
    }

    private static int dy(int direction) {
        return direction == SOUTH ? 1 : direction == NORTH ? -1 : 0;
    }

    private record Location(int x, int y) {
    }
}
