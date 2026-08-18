package com.thaumcraftmodern.world.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BlockEntityRenderBoundsTest {
    @Test
    void obeliskBoundsContainTheCompleteAnimatedModel() {
        BlockPos position = new BlockPos(10, 20, 30);
        AABB bounds = EldritchAltarPartBlockEntity.renderBoundingBox(
                position,
                1
        );

        assertTrue(bounds.minX < position.getX());
        assertTrue(bounds.maxX > position.getX() + 1.0D);
        assertTrue(bounds.maxY >= position.getY() + 4.2D);
        assertTrue(bounds.minZ < position.getZ());
        assertTrue(bounds.maxZ > position.getZ() + 1.0D);
    }

    @Test
    void thaumatoriumBoundsContainMachineAndFrontDisplayForEveryFacing() {
        BlockPos position = new BlockPos(10, 20, 30);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            AABB bounds = ThaumatoriumBlockEntity.renderBoundingBox(
                    position,
                    facing
            );
            assertTrue(bounds.minX <= position.getX());
            assertTrue(bounds.maxX >= position.getX() + 1.0D);
            assertTrue(bounds.minY <= position.getY());
            assertTrue(bounds.maxY >= position.getY() + 2.0D);
            assertTrue(bounds.minZ <= position.getZ());
            assertTrue(bounds.maxZ >= position.getZ() + 1.0D);

            double displayX = position.getX() + 0.5D
                    + facing.getStepX() / 1.99D;
            double displayZ = position.getZ() + 0.5D
                    + facing.getStepZ() / 1.99D;
            assertTrue(bounds.minX < displayX);
            assertTrue(bounds.maxX > displayX);
            assertTrue(bounds.minZ < displayZ);
            assertTrue(bounds.maxZ > displayZ);
        }
    }

    @Test
    void eldritchLockBoundsContainTheWholeFiveByFiveDoorField() {
        BlockPos position = new BlockPos(10, 20, 30);

        AABB northSouth = EldritchLockBlockEntity.renderBoundingBox(
                position,
                Direction.NORTH
        );
        assertEquals(8.0D, northSouth.minX);
        assertEquals(18.0D, northSouth.minY);
        assertEquals(30.0D, northSouth.minZ);
        assertEquals(13.0D, northSouth.maxX);
        assertEquals(23.0D, northSouth.maxY);
        assertEquals(31.0D, northSouth.maxZ);

        AABB eastWest = EldritchLockBlockEntity.renderBoundingBox(
                position,
                Direction.EAST
        );
        assertEquals(10.0D, eastWest.minX);
        assertEquals(18.0D, eastWest.minY);
        assertEquals(28.0D, eastWest.minZ);
        assertEquals(11.0D, eastWest.maxX);
        assertEquals(23.0D, eastWest.maxY);
        assertEquals(33.0D, eastWest.maxZ);
    }
}
