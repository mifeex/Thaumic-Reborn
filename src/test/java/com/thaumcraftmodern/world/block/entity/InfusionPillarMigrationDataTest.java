package com.thaumcraftmodern.world.block.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

final class InfusionPillarMigrationDataTest {
    @Test
    void migratedChunksPersistWithTheCurrentVersion() {
        ChunkPos migratedChunk = new ChunkPos(-17, 42);
        InfusionPillarMigrationData data = new InfusionPillarMigrationData();
        data.markMigrated(migratedChunk);

        CompoundTag saved = data.save(new CompoundTag());
        InfusionPillarMigrationData restored =
                InfusionPillarMigrationData.load(saved);

        assertTrue(restored.isMigrated(migratedChunk));
        assertFalse(restored.isMigrated(new ChunkPos(0, 0)));
        assertTrue(saved.getInt("Version")
                == InfusionPillarMigrationData.CURRENT_VERSION);
    }

    @Test
    void olderVersionForcesOneNewMigrationPass() {
        CompoundTag old = new CompoundTag();
        old.putInt("Version", InfusionPillarMigrationData.CURRENT_VERSION - 1);
        old.putLongArray("MigratedChunks", new long[]{new ChunkPos(1, 2).toLong()});

        assertFalse(InfusionPillarMigrationData.load(old)
                .isMigrated(new ChunkPos(1, 2)));
    }
}
