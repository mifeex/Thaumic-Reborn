package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Repairs wall trim generated with its stair facing reversed. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsStairMigrationEvents {
    private OuterLandsStairMigrationEvents() {
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

        int repaired = 0;
        repaired += OuterLandsLabyrinthGenerator.repairClassicPassageCornerStairs(
                level,
                chunk.getPos(),
                located.cell(),
                level.getSeed()
        );
        repaired += OuterLandsLabyrinthGenerator.repairClassicClosedPassageEdges(
                level,
                chunk.getPos(),
                located.cell()
        );
        repaired += OuterLandsLabyrinthGenerator.repairClassicConnectionStairTips(
                level,
                chunk.getPos(),
                located.cell()
        );
        repaired += OuterLandsLabyrinthGenerator.repairClassicPortalVoidWalls(
                level,
                chunk.getPos(),
                located.cell()
        );
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        for (int y = OuterLandsLabyrinthGenerator.BASE_Y + 2;
                y <= OuterLandsLabyrinthGenerator.BASE_Y + 10; y++) {
            for (int x = minX; x <= minX + 15; x++) {
                for (int z = minZ; z <= minZ + 15; z++) {
                    cursor.set(x, y, z);
                    BlockState stair = level.getBlockState(cursor);
                    if (!stair.is(ModBlocks.ANCIENT_STAIRS.get())) {
                        continue;
                    }
                    Direction current = stair.getValue(StairBlock.FACING);
                    Direction wallDirection = current.getOpposite();
                    BlockState behind = level.getBlockState(
                            cursor.relative(wallDirection)
                    );
                    BlockState inFront = level.getBlockState(
                            cursor.relative(current)
                    );
                    if (!isWall(behind) || !isOpen(inFront)) {
                        continue;
                    }
                    level.setBlock(cursor, stair.setValue(
                            StairBlock.FACING, wallDirection
                    ), 2);
                    repaired++;
                }
            }
        }
        repaired += OuterLandsStairTopology.refresh(level, chunk.getPos());
        repaired += OuterLandsEldritchNothingExposure.refresh(
                level,
                chunk.getPos()
        );
        if (repaired > 0) {
            chunk.setUnsaved(true);
        }
        return repaired;
    }

    private static boolean isWall(BlockState state) {
        return !isOpen(state)
                && !state.is(ModBlocks.ANCIENT_STAIRS.get());
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || EldritchNothingBlock.isNothing(state);
    }
}
