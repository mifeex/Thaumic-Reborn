package com.thaumcraftmodern.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.function.LongPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

/**
 * Temporary packed-position index for objects belonging to loaded chunks.
 * It stores no level or BlockEntity references and therefore cannot retain a
 * chunk after its lifecycle owner clears that chunk.
 */
public final class LoadedPositionIndex {
    private final Long2ObjectOpenHashMap<LongSet> positionsByChunk =
            new Long2ObjectOpenHashMap<>();

    public void track(BlockPos position) {
        track(position.asLong());
    }

    public void track(long packedPosition) {
        long chunkKey = ChunkPos.asLong(
                BlockPos.getX(packedPosition) >> 4,
                BlockPos.getZ(packedPosition) >> 4
        );
        positionsByChunk.computeIfAbsent(
                chunkKey,
                ignored -> new LongOpenHashSet()
        ).add(packedPosition);
    }

    public void untrack(BlockPos position) {
        untrack(position.asLong());
    }

    public void untrack(long packedPosition) {
        long chunkKey = ChunkPos.asLong(
                BlockPos.getX(packedPosition) >> 4,
                BlockPos.getZ(packedPosition) >> 4
        );
        LongSet positions = positionsByChunk.get(chunkKey);
        if (positions == null) {
            return;
        }
        positions.remove(packedPosition);
        if (positions.isEmpty()) {
            positionsByChunk.remove(chunkKey);
        }
    }

    public void clearChunk(ChunkPos chunk) {
        positionsByChunk.remove(chunk.toLong());
    }

    public void clear() {
        positionsByChunk.clear();
    }

    /** Returns packed positions within the exact spherical block radius. */
    public LongList within(BlockPos origin, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative");
        }
        LongArrayList result = new LongArrayList();
        visitCandidateChunks(origin, radius, packedPosition -> {
            if (withinRadius(origin, radius, packedPosition)) {
                result.add(packedPosition);
            }
            return false;
        });
        return result;
    }

    /** Returns packed positions in the exact axis-aligned search cube. */
    public LongList withinCube(BlockPos origin, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative");
        }
        LongArrayList result = new LongArrayList();
        visitCandidateChunks(origin, radius, packedPosition -> {
            if (withinCube(origin, radius, packedPosition)) {
                result.add(packedPosition);
            }
            return false;
        });
        return result;
    }

    /** Allocation-free early-exit query over the exact spherical radius. */
    public boolean anyWithin(
            BlockPos origin,
            int radius,
            LongPredicate match
    ) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius must be non-negative");
        }
        return visitCandidateChunks(origin, radius, packedPosition ->
                withinRadius(origin, radius, packedPosition)
                        && match.test(packedPosition)
        );
    }

    private boolean visitCandidateChunks(
            BlockPos origin,
            int radius,
            LongPredicate stop
    ) {
        int minimumChunkX = (origin.getX() - radius) >> 4;
        int maximumChunkX = (origin.getX() + radius) >> 4;
        int minimumChunkZ = (origin.getZ() - radius) >> 4;
        int maximumChunkZ = (origin.getZ() + radius) >> 4;
        for (int chunkX = minimumChunkX;
                chunkX <= maximumChunkX;
                chunkX++) {
            for (int chunkZ = minimumChunkZ;
                    chunkZ <= maximumChunkZ;
                    chunkZ++) {
                LongSet positions = positionsByChunk.get(
                        ChunkPos.asLong(chunkX, chunkZ)
                );
                if (positions == null) {
                    continue;
                }
                for (long packedPosition : positions) {
                    if (stop.test(packedPosition)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean withinRadius(
            BlockPos origin,
            int radius,
            long packedPosition
    ) {
        long deltaX = (long) BlockPos.getX(packedPosition) - origin.getX();
        long deltaY = (long) BlockPos.getY(packedPosition) - origin.getY();
        long deltaZ = (long) BlockPos.getZ(packedPosition) - origin.getZ();
        long radiusSquared = (long) radius * radius;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ
                <= radiusSquared;
    }

    private static boolean withinCube(
            BlockPos origin,
            int radius,
            long packedPosition
    ) {
        return Math.abs((long) BlockPos.getX(packedPosition) - origin.getX())
                        <= radius
                && Math.abs((long) BlockPos.getY(packedPosition) - origin.getY())
                        <= radius
                && Math.abs((long) BlockPos.getZ(packedPosition) - origin.getZ())
                        <= radius;
    }
}
