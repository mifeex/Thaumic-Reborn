package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shared biome and placement gate for natural taint-creature spawning.
 */
final class TaintedBiomeSpawnPolicy {
    static final int VILLAGE_PROXIMITY_SECTIONS = 2;

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

    /**
     * TC4 normally obtains these creatures from taint blocks or Flux Taint
     * conversion, both of which can happen in daylight. The modern biome
     * spawn list is the fallback for that incomplete block lifecycle, so it
     * must not accidentally apply the vanilla darkness gate a second time.
     * Taintacles retain their original hostile natural-spawn light rules.
     */
    static boolean usesEcologyLifecycleRules(LegacyMobKind kind) {
        return switch (kind) {
            case TAINTED_CRAWLER, TAINT_SPORE, TAINT_SPORE_SWARMER,
                    TAINT_SWARM, TAINTED_CHICKEN, TAINTED_COW,
                    TAINTED_CREEPER, TAINTED_PIG, TAINTED_SHEEP,
                    TAINTED_VILLAGER, THAUMIC_SLIME -> true;
            default -> false;
        };
    }

    static boolean requiresNearbyVillage(LegacyMobKind kind) {
        return kind == LegacyMobKind.TAINTED_VILLAGER;
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
