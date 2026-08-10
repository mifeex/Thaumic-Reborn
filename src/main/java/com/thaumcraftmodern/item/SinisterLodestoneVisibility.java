package com.thaumcraftmodern.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Allocation-free TC4 sinister-lodestone range and field-of-view check. */
public final class SinisterLodestoneVisibility {
    private static final double RANGE_SQUARED = 256.0D * 256.0D;
    private static final double MINIMUM_LOOK_DOT = Math.cos(.66D / 2.0D);

    private SinisterLodestoneVisibility() {
    }

    public static boolean isVisibleTo(
            Vec3 eye,
            Vec3 normalizedLook,
            BlockPos nodePosition
    ) {
        return isVisibleTo(
                eye,
                normalizedLook,
                nodePosition.getX() + 0.5D,
                nodePosition.getY() + 0.5D,
                nodePosition.getZ() + 0.5D
        );
    }

    public static boolean isVisibleTo(
            Vec3 eye,
            Vec3 normalizedLook,
            Vec3 nodePosition
    ) {
        return isVisibleTo(
                eye,
                normalizedLook,
                nodePosition.x,
                nodePosition.y,
                nodePosition.z
        );
    }

    private static boolean isVisibleTo(
            Vec3 eye,
            Vec3 normalizedLook,
            double nodeX,
            double nodeY,
            double nodeZ
    ) {
        double x = nodeX - eye.x;
        double y = nodeY - eye.y;
        double z = nodeZ - eye.z;
        double distanceSquared = x * x + y * y + z * z;
        if (distanceSquared <= 0.0D || distanceSquared > RANGE_SQUARED) {
            return false;
        }
        double lookDot = x * normalizedLook.x
                + y * normalizedLook.y
                + z * normalizedLook.z;
        return lookDot / Math.sqrt(distanceSquared) > MINIMUM_LOOK_DOT;
    }
}
