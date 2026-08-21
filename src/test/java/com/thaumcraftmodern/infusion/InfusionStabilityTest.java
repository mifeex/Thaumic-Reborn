package com.thaumcraftmodern.infusion;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InfusionStabilityTest {
    private static final BlockPos MATRIX = new BlockPos(0, 4, 0);

    @Test
    void mirroredPedestalsAndItemsAreNeutralButOneOccupiedPedestalCostsThree() {
        BlockPos west = new BlockPos(-3, 2, 0);
        BlockPos east = new BlockPos(3, 2, 0);
        assertEquals(0.0F, symmetry(
                List.of(new InfusionStability.Pedestal(west, true),
                        new InfusionStability.Pedestal(east, true)), Set.of()), 0.0001F);
        assertEquals(3.0F, symmetry(
                List.of(new InfusionStability.Pedestal(west, true)), Set.of()), 0.0001F);
    }

    @Test
    void sixMirroredStabilizerPairsPreserveFractionalHarmony() {
        Set<BlockPos> stabilizers = new HashSet<>();
        for (int z : new int[]{-3, -2, -1, 1, 2, 3}) {
            stabilizers.add(new BlockPos(-5, 2, z));
            stabilizers.add(new BlockPos(5, 2, z));
        }
        assertEquals(-1.2F, symmetry(List.of(), stabilizers), 0.0001F);
    }

    private static float symmetry(List<InfusionStability.Pedestal> pedestals,
            Set<BlockPos> stabilizers) {
        Set<BlockPos> pedestalPositions = new HashSet<>();
        Set<BlockPos> occupied = new HashSet<>();
        for (InfusionStability.Pedestal pedestal : pedestals) {
            pedestalPositions.add(pedestal.position());
            if (pedestal.occupied()) occupied.add(pedestal.position());
        }
        return InfusionStability.symmetry(MATRIX, pedestals,
                new ArrayList<>(stabilizers), pedestalPositions::contains,
                occupied::contains, stabilizers::contains);
    }
}
