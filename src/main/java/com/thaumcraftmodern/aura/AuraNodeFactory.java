package com.thaumcraftmodern.aura;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * One authority for safe ordinary-node defaults.
 *
 * <p>The value {@value #SAFE_PRIMAL_VIS} is a deliberate modern test/world
 * acquisition value, not a claim about TC4 world-generation distribution.
 * The current vertical has no advanced node types or modifiers.</p>
 */
public final class AuraNodeFactory {
    public static final int SAFE_PRIMAL_VIS = 100;
    private static final UUID CREATIVE_NODE_ID = UUID.nameUUIDFromBytes(
            "thaumic_reborn:creative_jarred_aura_node:v1"
                    .getBytes(StandardCharsets.UTF_8)
    );

    private AuraNodeFactory() {
    }

    public static AuraNodeState ordinary(UUID nodeId) {
        Map<PrimalAspect, Integer> vis = PrimalVis.uniform(SAFE_PRIMAL_VIS);
        return new AuraNodeState(
                Objects.requireNonNull(nodeId, "nodeId"),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                vis,
                vis,
                0L
        );
    }

    public static AuraNodeState typed(
            UUID nodeId,
            AuraNodeType type,
            AuraNodeModifier modifier,
            int primalVis
    ) {
        Map<PrimalAspect, Integer> vis = PrimalVis.uniform(primalVis);
        return new AuraNodeState(
                Objects.requireNonNull(nodeId, "nodeId"),
                Objects.requireNonNull(type, "type"),
                Objects.requireNonNull(modifier, "modifier"),
                vis,
                vis,
                0L
        );
    }

    public static AuraNodeState silverwoodNode(BlockPos position) {
        Objects.requireNonNull(position, "position");
        UUID id = UUID.nameUUIDFromBytes(
                ("silverwood:" + position.getX() + ":" + position.getY()
                        + ":" + position.getZ()).getBytes(StandardCharsets.UTF_8)
        );
        return typed(
                id,
                AuraNodeType.PURE,
                AuraNodeModifier.NORMAL,
                SAFE_PRIMAL_VIS / 4
        );
    }

    public static AuraNodeState structureNode(
            BlockPos position,
            AuraNodeType type
    ) {
        Objects.requireNonNull(position, "position");
        UUID id = UUID.nameUUIDFromBytes(
                ("structure:" + position.getX() + ":" + position.getY()
                        + ":" + position.getZ()).getBytes(StandardCharsets.UTF_8)
        );
        return typed(
                id,
                type,
                AuraNodeModifier.NORMAL,
                SAFE_PRIMAL_VIS
        );
    }

    public static AuraNodeState newWorldNode() {
        return ordinary(UUID.randomUUID());
    }

    public static AuraNodeState recoveredWorldNode(
            ResourceKey<Level> dimension,
            BlockPos position
    ) {
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(position, "position");
        String stableKey = dimension.location() + ":"
                + position.getX() + ":"
                + position.getY() + ":"
                + position.getZ();
        return ordinary(UUID.nameUUIDFromBytes(stableKey.getBytes(StandardCharsets.UTF_8)));
    }

    public static AuraNodeState recoveredWorldNode(BlockPos position) {
        Objects.requireNonNull(position, "position");
        String stableKey = "unresolved:"
                + position.getX() + ":"
                + position.getY() + ":"
                + position.getZ();
        return ordinary(UUID.nameUUIDFromBytes(stableKey.getBytes(StandardCharsets.UTF_8)));
    }

    public static AuraNodeState deterministicCreativeNode() {
        return ordinary(CREATIVE_NODE_ID);
    }
}
