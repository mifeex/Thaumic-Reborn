package com.thaumcraftmodern.worldgen.outerlands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OuterLandsMazeFidelityTest {
    @Test
    void originalOddMazeSizeRangeIsPreserved() {
        boolean varied = false;
        int firstWidth = OuterLandsMaze.forRegion(0L, 0, 0).width();
        int firstHeight = OuterLandsMaze.forRegion(0L, 0, 0).height();
        for (long seed = 0; seed < 64; seed++) {
            OuterLandsMaze maze = OuterLandsMaze.forRegion(seed, 0, 0);
            assertTrue(maze.width() >= 15 && maze.width() <= 29);
            assertTrue(maze.height() >= 15 && maze.height() <= 29);
            assertEquals(1, maze.width() & 1);
            assertEquals(1, maze.height() & 1);
            varied |= maze.width() != firstWidth
                    || maze.height() != firstHeight;
        }
        assertTrue(varied, "original maze size randomization was lost");
    }

    @Test
    void generatedMazesContainOriginalMandatoryRoomsAndConnectedExits() {
        for (long seed = 0; seed < 64; seed++) {
            OuterLandsMaze maze = OuterLandsMaze.forRegion(seed, 0, 0);
            int portals = 0;
            int tablets = 0;
            int bossQuadrants = 0;
            int portalX = -1;
            int portalY = -1;
            for (int row = 0; row < maze.height(); row++) {
                for (int column = 0; column < maze.width(); column++) {
                    OuterLandsCell cell = maze.cell(column, row);
                    if (cell == null) {
                        continue;
                    }
                    if (cell.feature() == 1) {
                        portals++;
                        portalX = column;
                        portalY = row;
                    } else if (cell.feature() == 6) {
                        tablets++;
                    } else if (cell.feature() >= 2
                            && cell.feature() <= 5) {
                        bossQuadrants++;
                    }
                    assertSymmetric(maze, column, row, cell);
                }
            }
            assertEquals(1, portals, "seed " + seed);
            assertEquals(1, tablets, "seed " + seed);
            assertEquals(4, bossQuadrants, "seed " + seed);

            Set<Long> reached = flood(maze, portalX, portalY);
            assertTrue(reached.stream().anyMatch(key -> {
                int x = (int) (key >> 32);
                int y = (int) (long) key;
                OuterLandsCell cell = maze.cell(x, y);
                return cell != null && cell.feature() == 6;
            }), "tablet must be reachable for seed " + seed);
            assertTrue(reached.stream().anyMatch(key -> {
                int x = (int) (key >> 32);
                int y = (int) (long) key;
                OuterLandsCell cell = maze.cell(x, y);
                return cell != null && cell.feature() >= 2
                        && cell.feature() <= 5;
            }), "boss room must be reachable for seed " + seed);
        }
    }

    @Test
    void worldIsMostlyVoidAroundEachFiniteMaze() {
        int centerChunk = OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
        assertTrue(OuterLandsMaze.at(42L, centerChunk, centerChunk).exists());
        assertTrue(!OuterLandsMaze.at(42L, 0, 0).exists());
        assertTrue(!OuterLandsMaze.at(42L,
                OuterLandsMaze.REGION_SIZE_CHUNKS - 1,
                OuterLandsMaze.REGION_SIZE_CHUNKS - 1).exists());
    }

    private static void assertSymmetric(
            OuterLandsMaze maze,
            int x,
            int y,
            OuterLandsCell cell
    ) {
        if (cell.north()) {
            assertNotNull(maze.cell(x, y - 1));
            assertTrue(maze.cell(x, y - 1).south());
        }
        if (cell.south()) {
            assertNotNull(maze.cell(x, y + 1));
            assertTrue(maze.cell(x, y + 1).north());
        }
        if (cell.east()) {
            assertNotNull(maze.cell(x + 1, y));
            assertTrue(maze.cell(x + 1, y).west());
        }
        if (cell.west()) {
            assertNotNull(maze.cell(x - 1, y));
            assertTrue(maze.cell(x - 1, y).east());
        }
    }

    private static Set<Long> flood(OuterLandsMaze maze, int x, int y) {
        ArrayDeque<Long> open = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        open.add(pack(x, y));
        while (!open.isEmpty()) {
            long current = open.removeFirst();
            if (!visited.add(current)) {
                continue;
            }
            int cx = (int) (current >> 32);
            int cy = (int) current;
            OuterLandsCell cell = maze.cell(cx, cy);
            if (cell == null) {
                continue;
            }
            if (cell.north()) open.add(pack(cx, cy - 1));
            if (cell.south()) open.add(pack(cx, cy + 1));
            if (cell.east()) open.add(pack(cx + 1, cy));
            if (cell.west()) open.add(pack(cx - 1, cy));
        }
        return visited;
    }

    private static long pack(int x, int y) {
        return (long) x << 32 | y & 0xffffffffL;
    }
}
