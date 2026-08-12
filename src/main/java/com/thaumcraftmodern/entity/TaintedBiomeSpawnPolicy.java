package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared biome and placement gate for natural taint-creature spawning.
 */
final class TaintedBiomeSpawnPolicy {
    private TaintedBiomeSpawnPolicy() {
    }

    static boolean allows(
            LegacyMobKind kind,
            boolean taintedBiome,
            boolean hostileSpawnRules,
            boolean sturdyGround
    ) {
        return kind.tainted()
                && taintedBiome
                && hostileSpawnRules
                && (kind.flying() || sturdyGround);
    }

    static boolean validTaintacleGround(
            BlockState at,
            BlockState below
    ) {
        return at.is(ModBlocks.TAINT_FIBRES.get())
                || at.is(ModBlocks.CRUSTED_TAINT.get())
                || below.is(ModBlocks.TAINT_FIBRES.get())
                || below.is(ModBlocks.CRUSTED_TAINT.get());
    }
}
