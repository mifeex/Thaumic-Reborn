package com.thaumcraftmodern.worldgen;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Dimension-local index of real legacy structure markers found in chunks.
 *
 * <p>Vanilla structure starts can survive even when the procedural placement
 * later rejects unsuitable terrain. This index contains only sites whose
 * characteristic blocks actually exist in the saved world.</p>
 */
public final class LegacyStructureMarkerIndex extends SavedData {
    private static final String DATA_NAME =
            "thaumic_reborn_legacy_structure_markers";

    private final Map<LegacyStructureKind, Set<Long>> markers =
            new EnumMap<>(LegacyStructureKind.class);

    public LegacyStructureMarkerIndex() {
        for (LegacyStructureKind kind : indexedKinds()) {
            markers.put(kind, new java.util.HashSet<>());
        }
    }

    public static LegacyStructureMarkerIndex get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                LegacyStructureMarkerIndex::load,
                LegacyStructureMarkerIndex::new,
                DATA_NAME
        );
    }

    public Optional<BlockPos> nearest(
            LegacyStructureKind kind,
            BlockPos origin
    ) {
        Set<Long> positions = markers.get(kind);
        if (positions == null || positions.isEmpty()) {
            return Optional.empty();
        }
        BlockPos nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (long packed : positions) {
            BlockPos candidate = BlockPos.of(packed);
            long dx = (long) candidate.getX() - origin.getX();
            long dz = (long) candidate.getZ() - origin.getZ();
            long distance = dx * dx + dz * dz;
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        return Optional.ofNullable(nearest);
    }

    public void replaceChunk(
            ChunkPos chunk,
            Map<LegacyStructureKind, List<BlockPos>> discovered
    ) {
        boolean changed = false;
        // Only block-detectable standalone sites are reconstructed on chunk
        // load. Village pieces are recorded directly when their jigsaw
        // element succeeds; clearing them here would erase valid markers.
        for (LegacyStructureKind kind : blockDetectedKinds()) {
            Set<Long> positions = markers.get(kind);
            changed |= positions.removeIf(packed -> {
                BlockPos position = BlockPos.of(packed);
                return SectionChunkCoordinates.matches(chunk, position);
            });
            for (BlockPos position : discovered.getOrDefault(kind, List.of())) {
                changed |= positions.add(position.asLong());
            }
        }
        if (changed) {
            setDirty();
        }
    }

    public void record(LegacyStructureKind kind, BlockPos position) {
        if (markers.get(kind).add(position.immutable().asLong())) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        for (LegacyStructureKind kind : indexedKinds()) {
            long[] packed = markers.get(kind).stream()
                    .mapToLong(Long::longValue)
                    .toArray();
            root.putLongArray(kind.serializedName(), packed);
        }
        return root;
    }

    static LegacyStructureMarkerIndex load(CompoundTag root) {
        LegacyStructureMarkerIndex index = new LegacyStructureMarkerIndex();
        for (LegacyStructureKind kind : indexedKinds()) {
            for (long packed : root.getLongArray(kind.serializedName())) {
                index.markers.get(kind).add(packed);
            }
        }
        return index;
    }

    private static EnumSet<LegacyStructureKind> indexedKinds() {
        return EnumSet.allOf(LegacyStructureKind.class);
    }

    private static EnumSet<LegacyStructureKind> blockDetectedKinds() {
        return EnumSet.of(
                LegacyStructureKind.ANCIENT_MOUND,
                LegacyStructureKind.ELDRITCH_RING,
                LegacyStructureKind.HILLTOP_STONES,
                LegacyStructureKind.AURA_TOTEM
        );
    }

    private static final class SectionChunkCoordinates {
        private SectionChunkCoordinates() {
        }

        private static boolean matches(ChunkPos chunk, BlockPos position) {
            return Math.floorDiv(position.getX(), 16) == chunk.x
                    && Math.floorDiv(position.getZ(), 16) == chunk.z;
        }
    }
}
