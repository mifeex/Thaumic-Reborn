package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Replaces the old generic pedestals in already generated Outer Lands rooms. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsPedestalMigrationEvents {
    private static final int[][] LIBRARY_PEDESTALS = {
            {5, 3, 5}, {5, 8, 5},
            {5, 3, 11}, {5, 8, 11},
            {11, 3, 5}, {11, 8, 5},
            {11, 3, 11}, {11, 8, 11},
            {8, 2, 8}, {8, 9, 8}
    };

    private OuterLandsPedestalMigrationEvents() {
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

        int repaired = switch (located.cell().feature()) {
            case 1, 6 -> replaceCapstone(level, chunk);
            case 2, 3, 4, 5 -> replaceBossPedestals(level, chunk);
            case 8 -> replaceLibraryPedestals(level, chunk);
            default -> 0;
        };
        if (repaired > 0) {
            chunk.setUnsaved(true);
        }
        return repaired;
    }

    private static int replaceCapstone(ServerLevel level, LevelChunk chunk) {
        BlockPos position = local(chunk, 8, 2, 8);
        if (!level.getBlockState(position).is(ModBlocks.ARCANE_PEDESTAL.get())) {
            return 0;
        }
        ItemStack stored = ItemStack.EMPTY;
        if (level.getBlockEntity(position)
                instanceof ArcanePedestalBlockEntity oldPedestal) {
            stored = oldPedestal.removeItemNoUpdate(0);
        }
        level.setBlock(
                position,
                ModBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(),
                Block.UPDATE_ALL
        );
        if (!stored.isEmpty() && level.getBlockEntity(position)
                instanceof ArcanePedestalBlockEntity newPedestal) {
            newPedestal.setItem(0, stored);
        }
        return 1;
    }

    private static int replaceLibraryPedestals(
            ServerLevel level,
            LevelChunk chunk
    ) {
        int repaired = 0;
        for (int[] offset : LIBRARY_PEDESTALS) {
            BlockPos position = local(
                    chunk, offset[0], offset[1], offset[2]
            );
            if (!level.getBlockState(position).is(
                    ModBlocks.ARCANE_PEDESTAL.get()
            )) {
                continue;
            }
            level.setBlock(
                    position,
                    ModBlocks.ELDRITCH_PEDESTAL.get().defaultBlockState(),
                    Block.UPDATE_ALL
            );
            repaired++;
        }
        return repaired;
    }

    private static int replaceBossPedestals(
            ServerLevel level,
            LevelChunk chunk
    ) {
        int repaired = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = OuterLandsLabyrinthGenerator.BASE_Y;
                        y <= OuterLandsLabyrinthGenerator.BASE_Y + 12;
                        y++) {
                    cursor.set(minX + x, y, minZ + z);
                    if (!level.getBlockState(cursor).is(
                            ModBlocks.ARCANE_PEDESTAL.get()
                    )) {
                        continue;
                    }
                    if (level.getBlockEntity(cursor)
                            instanceof ArcanePedestalBlockEntity pedestal
                            && !pedestal.isEmpty()) {
                        continue;
                    }
                    boolean supportsObelisk = level.getBlockState(
                            cursor.above()
                    ).is(ModBlocks.ELDRITCH_ALTAR_PART.get());
                    level.setBlock(
                            cursor,
                            (supportsObelisk
                                    ? ModBlocks.ELDRITCH_CAPSTONE
                                    : ModBlocks.ANCIENT_CRUST
                            ).get().defaultBlockState(),
                            Block.UPDATE_ALL
                    );
                    repaired++;
                }
            }
        }
        return repaired;
    }

    private static BlockPos local(
            LevelChunk chunk,
            int x,
            int y,
            int z
    ) {
        return new BlockPos(
                chunk.getPos().getMinBlockX() + x,
                OuterLandsLabyrinthGenerator.BASE_Y + y,
                chunk.getPos().getMinBlockZ() + z
        );
    }
}
