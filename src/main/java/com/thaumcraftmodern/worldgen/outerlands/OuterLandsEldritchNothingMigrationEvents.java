package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Adds star-field render anchors to void blocks in existing labyrinths. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsEldritchNothingMigrationEvents {
    private OuterLandsEldritchNothingMigrationEvents() {
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

    static int repairLoadedChunk(ServerLevel level, LevelChunk chunk) {
        if (level.getChunkSource().getChunkNow(
                chunk.getPos().x, chunk.getPos().z
        ) != chunk) {
            return 0;
        }
        OuterLandsMaze.RegionCell located = OuterLandsMaze.at(
                level.getSeed(), chunk.getPos().x, chunk.getPos().z
        );
        if (!located.exists()) {
            return 0;
        }
        int repaired = OuterLandsEldritchNothingExposure.refresh(
                level,
                chunk.getPos()
        );
        if (repaired > 0) {
            chunk.setUnsaved(true);
        }
        return repaired;
    }
}
