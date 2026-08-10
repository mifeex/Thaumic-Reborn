package com.thaumcraftmodern.construction;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.ClassicPartBlock;
import com.thaumcraftmodern.world.block.InfernalFurnaceBlock;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/** Restores every surviving source block when a wand-built machine is broken. */
public final class CraftingStructureDisassembly {
    private static boolean restoring;

    private CraftingStructureDisassembly() {}

    public static void partRemoved(ServerLevel level, BlockPos removed, BlockState oldState) {
        if (restoring) return;
        if (oldState.is(ModBlocks.INFUSION_PILLAR.get())) findInfusion(level, removed);
        else if (oldState.is(ModBlocks.INFERNAL_FURNACE.get())) findInfernal(level, removed);
        else if (oldState.is(ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())) findAdvanced(level, removed);
        else if (oldState.is(ModBlocks.THAUMATORIUM.get())) restoreThaumatorium(level, removed, oldState);
    }

    public static void matrixRemoved(ServerLevel level, BlockPos removed, BlockState oldState) {
        if (!restoring && oldState.getValue(RunicMatrixBlock.ACTIVE)) restoreInfusion(level, removed, removed);
    }

    public static void invalidInfusionMatrix(ServerLevel level, BlockPos matrix) {
        if (!restoring) restoreInfusion(level, matrix, null);
    }

    private static void findInfusion(ServerLevel level, BlockPos removed) {
        for (int x = -1; x <= 1; x += 2) for (int z = -1; z <= 1; z += 2) for (int y = 1; y <= 2; y++) {
            BlockPos matrix = removed.offset(-x, y, -z);
            BlockState state = level.getBlockState(matrix);
            if (state.is(ModBlocks.RUNIC_MATRIX.get()) && state.getValue(RunicMatrixBlock.ACTIVE)) {
                restoreInfusion(level, matrix, removed);
                return;
            }
        }
    }

    private static void restoreInfusion(ServerLevel level, BlockPos matrix, BlockPos removed) {
        restoring = true;
        try {
            restore(level, matrix, removed, ModBlocks.RUNIC_MATRIX.get().defaultBlockState()
                    .setValue(RunicMatrixBlock.ACTIVE, false));
            for (int x : new int[]{-1, 1}) for (int z : new int[]{-1, 1}) {
                restore(level, matrix.offset(x, -1, z), removed, ModBlocks.ARCANE_STONE.get().defaultBlockState());
                restore(level, matrix.offset(x, -2, z), removed, ModBlocks.ARCANE_STONE_BRICK.get().defaultBlockState());
            }
        } finally { restoring = false; }
    }

    private static void findInfernal(ServerLevel level, BlockPos removed) {
        for (int x = removed.getX() - 2; x <= removed.getX(); x++)
            for (int y = removed.getY() - 2; y <= removed.getY(); y++)
                for (int z = removed.getZ() - 2; z <= removed.getZ(); z++) {
                    BlockPos anchor = new BlockPos(x, y, z);
                    if (infernalMatches(level, anchor, removed)) {
                        restoreInfernal(level, anchor, removed);
                        return;
                    }
                }
    }

    private static boolean infernalMatches(ServerLevel level, BlockPos anchor, BlockPos removed) {
        if (removed.getX() < anchor.getX() || removed.getX() > anchor.getX() + 2
                || removed.getY() < anchor.getY() || removed.getY() > anchor.getY() + 2
                || removed.getZ() < anchor.getZ() || removed.getZ() > anchor.getZ() + 2) return false;
        for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) {
            if (x == 1 && y == 2 && z == 1) continue;
            BlockPos pos = anchor.offset(x, y, z);
            if (!pos.equals(removed) && !level.getBlockState(pos).is(ModBlocks.INFERNAL_FURNACE.get())) return false;
        }
        return true;
    }

    private static void restoreInfernal(ServerLevel level, BlockPos anchor, BlockPos removed) {
        restoring = true;
        try {
            for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) {
                if (x == 1 && y == 2 && z == 1) continue;
                BlockPos pos = anchor.offset(x, y, z);
                BlockState current = level.getBlockState(pos);
                BlockState source;
                if (x != 1 && z != 1) source = Blocks.NETHERRACK.defaultBlockState();
                else if (x == 1 && y == 1 && z == 1) source = Blocks.LAVA.defaultBlockState();
                else if (current.is(ModBlocks.INFERNAL_FURNACE.get())
                        && current.getValue(InfernalFurnaceBlock.PART) == 10) source = Blocks.IRON_BARS.defaultBlockState();
                else source = Blocks.OBSIDIAN.defaultBlockState();
                restore(level, pos, removed, source);
            }
        } finally { restoring = false; }
    }

    private static void findAdvanced(ServerLevel level, BlockPos removed) {
        for (int x = -1; x <= 1; x++) for (int y = -1; y <= 0; y++) for (int z = -1; z <= 1; z++) {
            BlockPos center = removed.offset(x, y, z);
            if (advancedMatches(level, center, removed)) {
                restoreAdvanced(level, center, removed);
                return;
            }
        }
    }

    private static boolean advancedMatches(ServerLevel level, BlockPos center, BlockPos removed) {
        int rx = Math.abs(removed.getX() - center.getX());
        int ry = removed.getY() - center.getY();
        int rz = Math.abs(removed.getZ() - center.getZ());
        if (rx > 1 || rz > 1 || ry < 0 || ry > 1 || rx == 0 && ry == 1 && rz == 0) return false;
        for (int y = 0; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            if (x == 0 && y == 1 && z == 0) continue;
            BlockPos pos = center.offset(x, y, z);
            if (!pos.equals(removed) && !level.getBlockState(pos).is(ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())) return false;
        }
        return true;
    }

    private static void restoreAdvanced(ServerLevel level, BlockPos center, BlockPos removed) {
        restoring = true;
        try {
            for (int y = 0; y <= 1; y++) for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                if (x == 0 && y == 1 && z == 0) continue;
                boolean corner = x != 0 && z != 0;
                BlockState source = y == 0 && x == 0 && z == 0
                        ? ModBlocks.ALCHEMICAL_FURNACE.get().defaultBlockState()
                        : y == 0 ? ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState()
                        : corner ? ModBlocks.ARCANE_ALEMBIC.get().defaultBlockState()
                        : ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState();
                restore(level, center.offset(x, y, z), removed, source);
            }
        } finally { restoring = false; }
    }

    private static void restoreThaumatorium(ServerLevel level, BlockPos removed, BlockState oldState) {
        BlockPos other = oldState.getValue(ThaumatoriumBlock.HALF) == DoubleBlockHalf.LOWER
                ? removed.above() : removed.below();
        restoring = true;
        try { restore(level, other, removed, ModBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState()); }
        finally { restoring = false; }
    }

    private static void restore(ServerLevel level, BlockPos pos, BlockPos removed, BlockState source) {
        if (removed != null && pos.equals(removed)) return;
        BlockState current = level.getBlockState(pos);
        if (current.is(ModBlocks.RUNIC_MATRIX.get()) || current.is(ModBlocks.INFUSION_PILLAR.get())
                || current.is(ModBlocks.INFERNAL_FURNACE.get())
                || current.is(ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())
                || current.is(ModBlocks.THAUMATORIUM.get())) level.setBlock(pos, source, 3);
    }
}
