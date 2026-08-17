package com.thaumcraftmodern.nodejar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Objects;

/**
 * Overworld-owned persistence wrapper for duplicate protection across chunk
 * unload and server restart.
 */
public final class NodeJarSavedData extends SavedData {
    private static final String DATA_NAME = "thaumic_reborn_node_jars";
    private static final String LEDGER_KEY = "ledger";

    private final NodeJarLedger ledger;

    public NodeJarSavedData() {
        this(new NodeJarLedger());
    }

    private NodeJarSavedData(NodeJarLedger ledger) {
        this.ledger = Objects.requireNonNull(ledger, "ledger");
    }

    public static NodeJarSavedData get(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        ServerLevel owner = Objects.requireNonNull(
                level.getServer().overworld(),
                "overworld"
        );
        return owner.getDataStorage().computeIfAbsent(
                NodeJarSavedData::load,
                NodeJarSavedData::new,
                DATA_NAME
        );
    }

    private static NodeJarSavedData load(CompoundTag root) {
        return new NodeJarSavedData(
                NodeJarLedger.deserialize(root.getCompound(LEDGER_KEY))
        );
    }

    public NodeJarLedger ledger() {
        return ledger;
    }

    public void markLedgerChanged() {
        setDirty();
    }

    public boolean returnPlacedJarToItem(
            NodeJarData data,
            String placementKey
    ) {
        boolean changed = ledger.returnToJarOrRecoverLegacyCapture(
                data,
                placementKey
        );
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean releasePlacedNode(
            NodeJarData data,
            String placementKey
    ) {
        boolean changed = ledger.releasePlacedNode(data, placementKey);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean restoreReleasedNode(
            NodeJarData data,
            String placementKey
    ) {
        boolean changed = ledger.restoreReleasedNode(data, placementKey);
        if (changed) {
            setDirty();
        }
        return changed;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        root.put(LEDGER_KEY, ledger.serialize());
        return root;
    }
}
