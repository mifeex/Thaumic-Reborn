package com.thaumcraftmodern.infusion;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TallowCandleStabilityTest {
    private static final BlockPos MATRIX = new BlockPos(0, 4, 0);

    @Test
    void fourCandlesOfOneColorGainHarmony() {
        Map<BlockPos, String> candles = new LinkedHashMap<>();
        candles.put(new BlockPos(3, 2, 0), "purple");
        candles.put(new BlockPos(-3, 2, 0), "purple");
        candles.put(new BlockPos(0, 2, 3), "purple");
        candles.put(new BlockPos(0, 2, -3), "purple");
        assertEquals(-0.15F, TallowCandleStability.bonus(MATRIX, candles), 0.0001F);
    }

    @Test
    void mirroredMulticolorOrnamentRewardsMatchingOpposites() {
        Map<BlockPos, String> candles = Map.of(
                new BlockPos(3, 2, 0), "red",
                new BlockPos(-3, 2, 0), "red",
                new BlockPos(0, 2, 3), "blue",
                new BlockPos(0, 2, -3), "blue"
        );
        assertEquals(-0.20F, TallowCandleStability.bonus(MATRIX, candles), 0.0001F);
    }

    @Test
    void unmatchedColorsDoNotReceiveAnOrnamentBonus() {
        Map<BlockPos, String> candles = Map.of(
                new BlockPos(3, 2, 0), "red",
                new BlockPos(-3, 2, 0), "blue"
        );
        assertEquals(0.0F, TallowCandleStability.bonus(MATRIX, candles), 0.0001F);
    }

    @Test
    void redCornerYellowBorderOrnamentGetsMaximumBonus() {
        Map<BlockPos, String> candles = new LinkedHashMap<>();
        for (int x : new int[]{-2, -1, 1, 2}) {
            for (int z : new int[]{-2, -1, 1, 2}) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }
                String color = Math.abs(x) == 2 && Math.abs(z) == 2
                        ? "red" : "yellow";
                candles.put(new BlockPos(x, 2, z), color);
            }
        }
        assertEquals(-0.50F, TallowCandleStability.bonus(MATRIX, candles), 0.0001F);
    }

    @Test
    void fourDifferentUnpairedColorsGainOneTenth() {
        Map<BlockPos, String> candles = Map.of(
                new BlockPos(1, 2, 0), "red",
                new BlockPos(2, 2, 0), "yellow",
                new BlockPos(3, 2, 0), "blue",
                new BlockPos(4, 2, 0), "green"
        );
        assertEquals(-0.10F, TallowCandleStability.bonus(MATRIX, candles), 0.0001F);
    }
}
