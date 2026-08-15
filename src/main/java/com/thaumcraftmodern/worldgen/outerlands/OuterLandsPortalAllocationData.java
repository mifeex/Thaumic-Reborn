package com.thaumcraftmodern.worldgen.outerlands;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Allocates one persistent, non-overlapping maze region to each opened portal. */
public final class OuterLandsPortalAllocationData extends SavedData {
    private static final String DATA_NAME =
            "thaumcraftmodern_outer_lands_portal_allocations";
    private static final String NEXT_ID_KEY = "NextId";
    private static final int FIRST_DEDICATED_REGION_X = 256;
    private static final int REGION_COLUMNS = 256;
    private long nextId;

    public static OuterLandsPortalAllocationData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                OuterLandsPortalAllocationData::load,
                OuterLandsPortalAllocationData::new,
                DATA_NAME
        );
    }

    public Destination allocate() {
        long id = nextId++;
        setDirty();
        int regionX = FIRST_DEDICATED_REGION_X
                + Math.toIntExact(id % REGION_COLUMNS);
        int regionZ = Math.toIntExact(id / REGION_COLUMNS);
        if (regionZ >= 38_000) {
            throw new IllegalStateException(
                    "Outer Lands portal destination space exhausted"
            );
        }
        return new Destination(regionX, regionZ);
    }

    static OuterLandsPortalAllocationData load(CompoundTag root) {
        OuterLandsPortalAllocationData data =
                new OuterLandsPortalAllocationData();
        data.nextId = Math.max(0L, root.getLong(NEXT_ID_KEY));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        root.putLong(NEXT_ID_KEY, nextId);
        return root;
    }

    public record Destination(int regionX, int regionZ) {
    }
}
