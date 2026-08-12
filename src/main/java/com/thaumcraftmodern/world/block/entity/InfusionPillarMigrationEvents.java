package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** One-shot server migration for legacy pillar blocks missing BlockEntity NBT. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class InfusionPillarMigrationEvents {
    private InfusionPillarMigrationEvents() {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        // ChunkEvent.Load may run before promotion to FULL. Always defer level
        // interaction to the server queue and avoid reloading an unloaded chunk.
        level.getServer().execute(() -> migrateLoadedChunk(
                level,
                chunk,
                event.isNewChunk()
        ));
    }

    static int migrateLoadedChunk(
            ServerLevel level,
            LevelChunk chunk,
            boolean newChunk
    ) {
        if (level.getChunkSource().getChunkNow(
                chunk.getPos().x,
                chunk.getPos().z
        ) != chunk) {
            return 0;
        }
        InfusionPillarMigrationData migration =
                InfusionPillarMigrationData.get(level);
        if (migration.isMigrated(chunk.getPos())) {
            return 0;
        }
        if (newChunk) {
            migration.markMigrated(chunk.getPos());
            return 0;
        }

        LongSet missing = new LongOpenHashSet();
        chunk.findBlocks(
                state -> state.is(ModBlocks.INFUSION_PILLAR.get())
                        && !state.getValue(InfusionPillarBlock.CAP),
                (position, state) -> {
                    if (!(chunk.getBlockEntity(position)
                            instanceof InfusionPillarBlockEntity)) {
                        missing.add(position.asLong());
                    }
                }
        );

        int restored = 0;
        for (long packedPosition : missing) {
            BlockPos position = BlockPos.of(packedPosition);
            if (chunk.getBlockEntity(
                    position,
                    LevelChunk.EntityCreationType.IMMEDIATE
            ) instanceof InfusionPillarBlockEntity) {
                restored++;
                var state = chunk.getBlockState(position);
                level.sendBlockUpdated(
                        position,
                        state,
                        state,
                        Block.UPDATE_CLIENTS
                );
            }
        }
        if (restored > 0) {
            chunk.setUnsaved(true);
        }
        migration.markMigrated(chunk.getPos());
        return restored;
    }
}
