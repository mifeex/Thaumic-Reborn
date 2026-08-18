package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Repairs feature-14 spawners generated before their mob id was configured. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsSpawnerMigrationEvents {
    private OuterLandsSpawnerMigrationEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(OuterLandsDimensions.OUTER_LANDS)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        OuterLandsChunkMigrationScheduler.nextTick(
                level.getServer(),
                () -> repairLoadedChunk(level, chunk)
        );
    }

    static boolean repairLoadedChunk(ServerLevel level, LevelChunk chunk) {
        if (level.getChunkSource().getChunkNow(
                chunk.getPos().x, chunk.getPos().z
        ) != chunk) {
            return false;
        }
        OuterLandsMaze.RegionCell located = OuterLandsMaze.at(
                level.getSeed(), chunk.getPos().x, chunk.getPos().z
        );
        if (!located.exists() || located.cell().feature() != 14) {
            return false;
        }
        BlockPos spawnerPosition = new BlockPos(
                chunk.getPos().getMinBlockX() + 8,
                OuterLandsLabyrinthGenerator.BASE_Y + 4,
                chunk.getPos().getMinBlockZ() + 8
        );
        if (chunk.getBlockEntity(spawnerPosition)
                instanceof SpawnerBlockEntity spawner
                && OuterLandsMindSpiderSpawners.isConfigured(spawner)) {
            return false;
        }
        boolean configured = OuterLandsMindSpiderSpawners.configure(
                level, spawnerPosition
        );
        if (configured) {
            chunk.setUnsaved(true);
        }
        return configured;
    }
}
