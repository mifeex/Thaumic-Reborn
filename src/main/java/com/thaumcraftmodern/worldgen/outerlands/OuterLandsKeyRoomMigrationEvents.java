package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Restores the TC4 wall palette and central VOID windows in tablet rooms. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class OuterLandsKeyRoomMigrationEvents {
    private OuterLandsKeyRoomMigrationEvents() {
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
        if (!located.exists() || located.cell().feature() != 6) {
            return 0;
        }

        OuterLandsCell cell = located.cell();
        int repaired = 0;
        if (!cell.north()) {
            repaired += repairWall(level, chunk, Wall.NORTH);
        }
        if (!cell.south()) {
            repaired += repairWall(level, chunk, Wall.SOUTH);
        }
        if (!cell.west()) {
            repaired += repairWall(level, chunk, Wall.WEST);
        }
        if (!cell.east()) {
            repaired += repairWall(level, chunk, Wall.EAST);
        }
        if (repaired > 0) {
            chunk.setUnsaved(true);
        }
        return repaired;
    }

    private static int repairWall(
            ServerLevel level,
            LevelChunk chunk,
            Wall wall
    ) {
        int repaired = 0;
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int across = 3; across <= 13; across++) {
            for (int height = 2; height < 11; height++) {
                int localX = wall.vertical ? wall.coordinate : across;
                int localZ = wall.vertical ? across : wall.coordinate;
                cursor.set(
                        minX + localX,
                        OuterLandsLabyrinthGenerator.BASE_Y + height,
                        minZ + localZ
                );
                BlockState current = level.getBlockState(cursor);
                if (!isReplaceableStructureState(current)) {
                    continue;
                }
                boolean centerWindow = height == 6 && across == 8;
                boolean inset = (height > 3 && height < 9 && across == 8)
                        || (height > 4 && height < 8
                        && (across == 7 || across == 9));
                BlockState expected = centerWindow
                        ? Blocks.AIR.defaultBlockState()
                        : inset
                        ? ModBlocks.ANCIENT_STONE.get().defaultBlockState()
                        : ModBlocks.ANCIENT_ROCK.get().defaultBlockState();
                if (!current.equals(expected)) {
                    level.setBlock(cursor, expected, 2);
                    repaired++;
                }
                if (centerWindow) {
                    repaired += repairVoidBacking(
                            level, minX, minZ, across, height, wall, cursor
                    );
                }
            }
        }
        return repaired;
    }

    private static int repairVoidBacking(
            ServerLevel level,
            int minX,
            int minZ,
            int across,
            int height,
            Wall wall,
            BlockPos.MutableBlockPos cursor
    ) {
        int backingCoordinate = wall.coordinate < 8 ? 2 : 14;
        int localX = wall.vertical ? backingCoordinate : across;
        int localZ = wall.vertical ? across : backingCoordinate;
        cursor.set(
                minX + localX,
                OuterLandsLabyrinthGenerator.BASE_Y + height,
                minZ + localZ
        );
        BlockState current = level.getBlockState(cursor);
        if (EldritchNothingBlock.isNothing(current)
                || !isReplaceableStructureState(current)) {
            return 0;
        }
        level.setBlock(
                cursor,
                ModBlocks.ELDRITCH_NOTHING.get().defaultBlockState(),
                2
        );
        return 1;
    }

    private static boolean isReplaceableStructureState(BlockState state) {
        return state.isAir()
                || EldritchNothingBlock.isNothing(state)
                || state.is(ModBlocks.ANCIENT_STONE.get())
                || state.is(ModBlocks.ANCIENT_ROCK.get());
    }

    private enum Wall {
        NORTH(false, 3),
        SOUTH(false, 13),
        WEST(true, 3),
        EAST(true, 13);

        private final boolean vertical;
        private final int coordinate;

        Wall(boolean vertical, int coordinate) {
            this.vertical = vertical;
            this.coordinate = coordinate;
        }
    }
}
