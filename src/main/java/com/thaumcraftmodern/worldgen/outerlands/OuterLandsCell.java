package com.thaumcraftmodern.worldgen.outerlands;

/** Compact TC4 labyrinth cell: four exits and one room feature byte. */
public record OuterLandsCell(
        boolean north,
        boolean south,
        boolean east,
        boolean west,
        int feature
) {
    public static OuterLandsCell unpack(int packed) {
        return new OuterLandsCell(
                (packed & OuterLandsMaze.NORTH) != 0,
                (packed & OuterLandsMaze.SOUTH) != 0,
                (packed & OuterLandsMaze.EAST) != 0,
                (packed & OuterLandsMaze.WEST) != 0,
                packed >>> 8 & 0xff
        );
    }

    public int exitCount() {
        return (north ? 1 : 0)
                + (south ? 1 : 0)
                + (east ? 1 : 0)
                + (west ? 1 : 0);
    }
}
