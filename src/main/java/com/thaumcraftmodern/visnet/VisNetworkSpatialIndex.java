package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.LoadedPositionIndex;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/** Server-thread index of loaded VisNet nodes, partitioned by dimension. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class VisNetworkSpatialIndex {
    private static final Map<ServerLevel, LevelIndex> BY_LEVEL =
            new IdentityHashMap<>();

    private VisNetworkSpatialIndex() {
    }

    static Comparator<BlockPos> legacyScanOrder() {
        return Comparator.comparingInt((BlockPos position) -> position.getZ())
                .thenComparingInt(BlockPos::getY)
                .thenComparingInt(BlockPos::getX);
    }

    static void track(VisNetworkNodeBlockEntity node) {
        if (node.getLevel() instanceof ServerLevel level) {
            LevelIndex index = index(level);
            index.positions.track(node.getBlockPos());
            index.invalidatePositions();
        }
    }

    static void untrack(VisNetworkNodeBlockEntity node) {
        if (node.getLevel() instanceof ServerLevel level) {
            LevelIndex index = BY_LEVEL.get(level);
            if (index != null) {
                index.positions.untrack(node.getBlockPos());
                index.invalidatePositions();
            }
        }
    }

    static void topologyChanged(VisNetworkNodeBlockEntity node) {
        if (node.getLevel() instanceof ServerLevel level) {
            LevelIndex index = BY_LEVEL.get(level);
            if (index != null) {
                index.invalidateRoutes();
            }
        }
    }

    static long[] networkCandidates(ServerLevel level, BlockPos origin) {
        LevelIndex index = index(level);
        long key = origin.asLong();
        long[] cached = index.neighbourhoods.get(key);
        if (cached != null) {
            return cached;
        }
        LongList packed = index.positions.within(
                origin,
                VisNetworkNodeBlockEntity.RANGE
        );
        LongArrayList candidates = new LongArrayList(packed.size());
        for (long position : packed) {
            BlockPos candidate = BlockPos.of(position);
            if (level.hasChunkAt(candidate)
                    && level.getBlockEntity(candidate)
                    instanceof VisNetworkNodeBlockEntity) {
                candidates.add(position);
            }
        }
        candidates.sort((first, second) -> compareLegacyOrder(first, second));
        long[] result = candidates.toLongArray();
        index.neighbourhoods.put(key, result);
        return result;
    }

    static long[] machineCandidates(ServerLevel level, BlockPos origin) {
        LevelIndex index = index(level);
        long key = origin.asLong();
        long[] cached = index.machineRoutes.get(key);
        if (cached != null) {
            return cached;
        }
        LongArrayList candidates = new LongArrayList();
        for (long position : networkCandidates(level, origin)) {
            if (level.getBlockEntity(BlockPos.of(position))
                    instanceof VisNetworkNodeBlockEntity node
                    && route(level, node) != null) {
                candidates.add(position);
            }
        }
        candidates.sort((first, second) -> {
            int distance = Long.compare(
                    squaredDistance(first, origin),
                    squaredDistance(second, origin)
            );
            return distance != 0 ? distance : Long.compare(first, second);
        });
        // Several nearby relays can lead to the same energized source. Once
        // its pool is drained, retrying that source through every other relay
        // cannot yield more Vis. Keep the nearest route for each source.
        LongOpenHashSet includedSources = new LongOpenHashSet();
        LongArrayList nearestPerSource = new LongArrayList();
        for (long position : candidates) {
            if (level.getBlockEntity(BlockPos.of(position))
                    instanceof VisNetworkNodeBlockEntity node) {
                Route route = route(level, node);
                if (route != null
                        && includedSources.add(route.sourcePosition())) {
                    nearestPerSource.add(position);
                }
            }
        }
        long[] result = nearestPerSource.toLongArray();
        index.machineRoutes.put(key, result);
        return result;
    }

    static @Nullable Route route(
            ServerLevel level,
            VisNetworkNodeBlockEntity start
    ) {
        LevelIndex index = index(level);
        long startPosition = start.getBlockPos().asLong();
        Route cached = index.routes.get(startPosition);
        if (cached != null) {
            return cached.valid() ? cached : null;
        }

        Route resolved = resolveRoute(level, start);
        index.routes.put(
                startPosition,
                resolved == null ? Route.INVALID : resolved
        );
        return resolved;
    }

    static boolean routeAvoids(
            ServerLevel level,
            VisNetworkNodeBlockEntity start,
            BlockPos excluded
    ) {
        Route route = route(level, start);
        return route != null && !route.contains(excluded.asLong());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            LevelIndex index = BY_LEVEL.get(level);
            if (index != null) {
                index.positions.clearChunk(event.getChunk().getPos());
                index.invalidatePositions();
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel level) {
            BY_LEVEL.remove(level);
        }
    }

    private static LevelIndex index(ServerLevel level) {
        return BY_LEVEL.computeIfAbsent(
                level,
                ignored -> new LevelIndex()
        );
    }

    private static @Nullable Route resolveRoute(
            ServerLevel level,
            VisNetworkNodeBlockEntity start
    ) {
        LongArrayList positions = new LongArrayList();
        LongOpenHashSet visited = new LongOpenHashSet();
        VisNetworkNodeBlockEntity current = start;
        while (true) {
            long currentPosition = current.getBlockPos().asLong();
            if (!visited.add(currentPosition)) {
                return null;
            }
            positions.add(currentPosition);
            if (current.isSource()) {
                return new Route(positions.toLongArray());
            }
            BlockPos parentPosition = current.parentPosition();
            if (parentPosition == null || !level.hasChunkAt(parentPosition)
                    || !(level.getBlockEntity(parentPosition)
                    instanceof VisNetworkNodeBlockEntity parent)) {
                return null;
            }
            current = parent;
        }
    }

    private static int compareLegacyOrder(long first, long second) {
        int z = Integer.compare(BlockPos.getZ(first), BlockPos.getZ(second));
        if (z != 0) {
            return z;
        }
        int y = Integer.compare(BlockPos.getY(first), BlockPos.getY(second));
        return y != 0
                ? y
                : Integer.compare(BlockPos.getX(first), BlockPos.getX(second));
    }

    private static long squaredDistance(long position, BlockPos origin) {
        long x = (long) BlockPos.getX(position) - origin.getX();
        long y = (long) BlockPos.getY(position) - origin.getY();
        long z = (long) BlockPos.getZ(position) - origin.getZ();
        return x * x + y * y + z * z;
    }

    static final class Route {
        private static final Route INVALID = new Route(new long[0]);
        private final long[] positions;

        private Route(long[] positions) {
            this.positions = positions;
        }

        boolean valid() {
            return positions.length > 0;
        }

        long sourcePosition() {
            return positions[positions.length - 1];
        }

        long[] positions() {
            return positions;
        }

        boolean contains(long position) {
            for (long candidate : positions) {
                if (candidate == position) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class LevelIndex {
        private final LoadedPositionIndex positions =
                new LoadedPositionIndex();
        private final Long2ObjectOpenHashMap<Route> routes =
                new Long2ObjectOpenHashMap<>();
        private final Long2ObjectOpenHashMap<long[]> neighbourhoods =
                new Long2ObjectOpenHashMap<>();
        private final Long2ObjectOpenHashMap<long[]> machineRoutes =
                new Long2ObjectOpenHashMap<>();

        private void invalidateRoutes() {
            routes.clear();
            machineRoutes.clear();
        }

        private void invalidatePositions() {
            invalidateRoutes();
            neighbourhoods.clear();
        }
    }
}
