package com.thaumcraftmodern.world.block.entity;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent per-dimension ledger for one-shot legacy pillar migration. */
public final class InfusionPillarMigrationData extends SavedData {
    static final int CURRENT_VERSION = 1;
    private static final String DATA_NAME =
            "thaumic_reborn_infusion_pillar_migration";
    private static final String VERSION_KEY = "Version";
    private static final String CHUNKS_KEY = "MigratedChunks";

    private final LongSet migratedChunks = new LongOpenHashSet();

    public static InfusionPillarMigrationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                InfusionPillarMigrationData::load,
                InfusionPillarMigrationData::new,
                DATA_NAME
        );
    }

    static InfusionPillarMigrationData load(CompoundTag root) {
        InfusionPillarMigrationData data = new InfusionPillarMigrationData();
        if (root.getInt(VERSION_KEY) == CURRENT_VERSION) {
            data.migratedChunks.addAll(
                    new LongOpenHashSet(root.getLongArray(CHUNKS_KEY))
            );
        }
        return data;
    }

    public boolean isMigrated(ChunkPos chunk) {
        return migratedChunks.contains(chunk.toLong());
    }

    public void markMigrated(ChunkPos chunk) {
        if (migratedChunks.add(chunk.toLong())) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        root.putInt(VERSION_KEY, CURRENT_VERSION);
        root.putLongArray(CHUNKS_KEY, migratedChunks.toLongArray());
        return root;
    }
}
