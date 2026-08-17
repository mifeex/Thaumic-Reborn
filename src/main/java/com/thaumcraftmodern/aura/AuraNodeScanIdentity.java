package com.thaumcraftmodern.aura;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.UUID;

/**
 * Stable scan identity for an aura phenomenon.
 *
 * <p>The shared target id resolves the data-driven scan definition. The
 * persistent node UUID is part of the player's completed-scan key, so every
 * world node can be studied once without allowing a replacement block entity
 * to substitute itself into an in-flight scan.</p>
 */
public record AuraNodeScanIdentity(UUID nodeId) {
    public static final ResourceLocation TARGET_ID = new ResourceLocation(
            "thaumic_reborn",
            "aura_node"
    );
    public static final String SCAN_KEY = "phenomenon:" + TARGET_ID;

    public AuraNodeScanIdentity {
        nodeId = Objects.requireNonNull(nodeId, "nodeId");
    }

    public String scanKey() {
        return SCAN_KEY + "/" + nodeId;
    }

    public boolean stillMatches(AuraNodeState state) {
        return state != null && nodeId.equals(state.nodeId());
    }
}
