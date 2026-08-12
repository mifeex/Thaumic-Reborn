package com.thaumcraftmodern.essentia.tube;

import net.minecraft.core.Direction;

import java.util.Objects;
import java.util.function.Predicate;

/** Pure TC4 tube-facing selection used by valves and directional tubes. */
public final class TubeFacingRules {
    private TubeFacingRules() {
    }

    /**
     * Points a device toward the preferred side. Repeating the same aimed
     * face flips it to the exact opposite, matching the wand control shared
     * by the essentia valve and reservoir.
     */
    public static Direction toggleFacing(
            Direction current,
            Direction preferred
    ) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(preferred, "preferred");
        return current == preferred ? preferred.getOpposite() : preferred;
    }

    /**
     * TC4 advances through ForgeDirection order and accepts a candidate only
     * when the side opposite that facing both contains an essentia transport
     * and is locally open. If no other connected side is available, the loop
     * reaches the current facing again and keeps it.
     *
     * @param connectedOpenSide tests the controlled side opposite a candidate
     */
    public static Direction nextConnectedFacing(Direction current,
            Predicate<Direction> connectedOpenSide) {
        Objects.requireNonNull(current, "current");
        Objects.requireNonNull(connectedOpenSide, "connectedOpenSide");
        Direction[] directions = Direction.values();
        int start = current.ordinal();
        for (int step = 1; step <= directions.length; step++) {
            Direction candidate = directions[(start + step) % directions.length];
            if (connectedOpenSide.test(candidate.getOpposite())) {
                return candidate;
            }
        }
        return current;
    }
}
