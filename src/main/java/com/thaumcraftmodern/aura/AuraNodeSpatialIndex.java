package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.LoadedPositionIndex;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Server-thread spatial index containing loaded aura-node positions. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class AuraNodeSpatialIndex {
    private static final Map<ServerLevel, LoadedPositionIndex> BY_LEVEL =
            new IdentityHashMap<>();

    private AuraNodeSpatialIndex() {
    }

    static void track(AuraNodeBlockEntity node) {
        if (node.getLevel() instanceof ServerLevel level) {
            index(level).track(node.getBlockPos());
        }
    }

    static void untrack(AuraNodeBlockEntity node) {
        if (node.getLevel() instanceof ServerLevel level) {
            LoadedPositionIndex index = BY_LEVEL.get(level);
            if (index != null) {
                index.untrack(node.getBlockPos());
            }
        }
    }

    public static List<BlockPos> withinCube(
            ServerLevel level,
            BlockPos origin,
            int radius
    ) {
        LongList packed = index(level).withinCube(origin, radius);
        List<BlockPos> result = new ArrayList<>(packed.size());
        for (long position : packed) {
            BlockPos candidate = BlockPos.of(position);
            if (level.hasChunkAt(candidate)
                    && level.getBlockEntity(candidate)
                    instanceof AuraNodeBlockEntity) {
                result.add(candidate);
            }
        }
        result.sort(scanOrder());
        return List.copyOf(result);
    }

    public static AuraNodeBlockEntity nearest(
            ServerLevel level,
            BlockPos origin,
            int radius,
            Predicate<AuraNodeBlockEntity> filter
    ) {
        AuraNodeBlockEntity nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (BlockPos candidatePosition : withinCube(level, origin, radius)) {
            AuraNodeBlockEntity candidate = level.getBlockEntity(
                    candidatePosition
            ) instanceof AuraNodeBlockEntity node ? node : null;
            if (candidate == null || !filter.test(candidate)) {
                continue;
            }
            long distance = squaredDistance(candidatePosition, origin);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LoadedPositionIndex index = BY_LEVEL.get(level);
            if (index != null) {
                index.clearChunk(event.getChunk().getPos());
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BY_LEVEL.remove(level);
        }
    }

    private static LoadedPositionIndex index(ServerLevel level) {
        return BY_LEVEL.computeIfAbsent(
                level,
                ignored -> new LoadedPositionIndex()
        );
    }

    private static Comparator<BlockPos> scanOrder() {
        return Comparator.comparingInt((BlockPos position) -> position.getX())
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getZ);
    }

    private static long squaredDistance(BlockPos first, BlockPos second) {
        long x = (long) first.getX() - second.getX();
        long y = (long) first.getY() - second.getY();
        long z = (long) first.getZ() - second.getZ();
        return x * x + y * y + z * z;
    }
}
