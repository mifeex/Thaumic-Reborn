package com.thaumcraftmodern.infusion;

import net.minecraft.core.BlockPos;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Additional harmony awarded to colored tallow-candle arrangements. */
public final class TallowCandleStability {
    private static final float FOUR_COLOR_BONUS = 0.10F;
    private static final float WEAK_PAIR_BONUS = 0.05F;
    private static final float FULL_ORNAMENT_PAIR_BONUS = 0.10F;
    private static final float MAX_WEAK_BONUS = 0.25F;
    private static final float MAX_FULL_ORNAMENT_BONUS = 0.50F;

    private TallowCandleStability() {}

    /** Extra color bonus; ordinary candle symmetry is calculated separately. */
    public static <C> float bonus(BlockPos matrix, Map<BlockPos, C> candles) {
        if (candles.isEmpty()) return 0.0F;

        Set<C> colors = new HashSet<>(candles.values());
        int mirroredPairs = 0;
        int colorMatchedPairs = 0;
        for (Map.Entry<BlockPos, C> candle : candles.entrySet()) {
            BlockPos mirror = InfusionStability.mirror(matrix, candle.getKey());
            if (!orderedBefore(candle.getKey(), mirror) || !candles.containsKey(mirror)) continue;
            mirroredPairs++;
            if (candle.getValue() == candles.get(mirror)) colorMatchedPairs++;
        }

        boolean completeOrnament = mirroredPairs > 0
                && mirroredPairs * 2 == candles.size()
                && colorMatchedPairs == mirroredPairs;
        float bonus;
        if (completeOrnament && colors.size() > 1) {
            bonus = Math.min(MAX_FULL_ORNAMENT_BONUS,
                    colorMatchedPairs * FULL_ORNAMENT_PAIR_BONUS);
        } else {
            int qualifyingPairs = completeOrnament ? mirroredPairs : colorMatchedPairs;
            bonus = qualifyingPairs == 0 ? 0.0F : Math.min(MAX_WEAK_BONUS,
                    (qualifyingPairs + 1) * WEAK_PAIR_BONUS);
            if (colors.size() >= 4) bonus = Math.max(bonus, FOUR_COLOR_BONUS);
        }
        return -bonus;
    }

    private static boolean orderedBefore(BlockPos first, BlockPos second) {
        if (first.getY() != second.getY()) return first.getY() < second.getY();
        if (first.getX() != second.getX()) return first.getX() < second.getX();
        return first.getZ() < second.getZ();
    }
}
