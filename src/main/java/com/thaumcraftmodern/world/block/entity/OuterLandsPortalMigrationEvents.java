package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Restores portals saved before they acquired a block entity renderer. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsPortalMigrationEvents {
    private OuterLandsPortalMigrationEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        level.getServer().execute(() -> restoreLoadedChunk(level, chunk));
    }

    static int restoreLoadedChunk(ServerLevel level, LevelChunk chunk) {
        if (level.getChunkSource().getChunkNow(
                chunk.getPos().x,
                chunk.getPos().z
        ) != chunk) {
            return 0;
        }
        int[] restored = {0};
        chunk.findBlocks(
                state -> state.is(ModBlocks.OUTER_LANDS_PORTAL.get()),
                (position, state) -> {
                    if (chunk.getBlockEntity(position)
                            instanceof OuterLandsPortalBlockEntity) {
                        return;
                    }
                    OuterLandsPortalBlockEntity portal =
                            new OuterLandsPortalBlockEntity(position, state);
                    chunk.setBlockEntity(portal);
                    level.sendBlockUpdated(
                            position,
                            state,
                            state,
                            Block.UPDATE_CLIENTS
                    );
                    restored[0]++;
                }
        );
        if (restored[0] > 0) {
            chunk.setUnsaved(true);
        }
        return restored[0];
    }
}
