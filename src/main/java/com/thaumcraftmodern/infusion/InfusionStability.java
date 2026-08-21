package com.thaumcraftmodern.infusion;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.function.Predicate;

/** Exact TC4 TileInfusionMatrix symmetry arithmetic, isolated for testing. */
public final class InfusionStability {
    private InfusionStability() {
    }

    public record Pedestal(BlockPos position, boolean occupied) {
    }

    public static float symmetry(
            BlockPos matrix,
            List<Pedestal> pedestals,
            List<BlockPos> stabilizers,
            Predicate<BlockPos> hasPedestal,
            Predicate<BlockPos> occupiedPedestal,
            Predicate<BlockPos> isStabilizer
    ) {
        int symmetry = 0;
        for (Pedestal pedestal : pedestals) {
            symmetry += 2;
            if (pedestal.occupied()) {
                symmetry++;
            }
            BlockPos mirror = mirror(matrix, pedestal.position());
            if (hasPedestal.test(mirror)) {
                symmetry -= 2;
                if (pedestal.occupied() && occupiedPedestal.test(mirror)) {
                    symmetry--;
                }
            }
        }

        float stabilizerSymmetry = 0.0F;
        for (BlockPos stabilizer : stabilizers) {
            if (isStabilizer.test(stabilizer)) {
                stabilizerSymmetry += 0.1F;
            }
            if (isStabilizer.test(mirror(matrix, stabilizer))) {
                stabilizerSymmetry -= 0.2F;
            }
        }
        return symmetry + stabilizerSymmetry;
    }

    public static BlockPos mirror(BlockPos matrix, BlockPos position) {
        return new BlockPos(
                matrix.getX() * 2 - position.getX(),
                position.getY(),
                matrix.getZ() * 2 - position.getZ()
        );
    }
}
