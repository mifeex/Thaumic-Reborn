package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.OuterLandsPortalBlock;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsPortalAllocationData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/** Client opening state used by the original camera-facing portal effect. */
public final class OuterLandsPortalBlockEntity extends BlockEntity {
    public static final int OPEN_TICKS = 30;
    static final int TRANSFER_INTERVAL_TICKS = 5;
    static final double ENTRY_HORIZONTAL_RADIUS = 3.0D;
    static final double ENTRY_VERTICAL_RADIUS = 2.0D;
    private int openCount = -1;
    private int serverTicks;
    private boolean hasDestination;
    private int destinationRegionX;
    private int destinationRegionZ;

    public OuterLandsPortalBlockEntity(BlockPos position, BlockState state) {
        super(ModBlockEntities.OUTER_LANDS_PORTAL.get(), position, state);
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            OuterLandsPortalBlockEntity portal
    ) {
        if (portal.openCount < OPEN_TICKS) {
            portal.openCount++;
        }
    }

    /**
     * TC4 checks an expanded volume instead of relying on the portal block's
     * collision callback. That lets a player enter while standing on the
     * altar directly below the visual effect.
     */
    public static void serverTick(
            Level level,
            BlockPos position,
            BlockState state,
            OuterLandsPortalBlockEntity portal
    ) {
        if (!(level instanceof ServerLevel server)
                || ++portal.serverTicks % TRANSFER_INTERVAL_TICKS != 0) {
            return;
        }
        AABB entryBounds = new AABB(position).inflate(
                ENTRY_HORIZONTAL_RADIUS,
                ENTRY_VERTICAL_RADIUS,
                ENTRY_HORIZONTAL_RADIUS
        );
        // Dimension transfer removes the player from ServerLevel#players.
        // Iterate a snapshot so a successful transfer cannot invalidate the
        // live list's iterator and crash the integrated/dedicated server.
        for (ServerPlayer player : List.copyOf(server.players())) {
            if (player.isAlive()
                    && entryBounds.intersects(player.getBoundingBox())) {
                OuterLandsPortalBlock.transferPlayer(
                        server,
                        position,
                        player
                );
            }
        }
    }

    public float openCount(float partialTick) {
        return Math.min(OPEN_TICKS, openCount + partialTick);
    }

    public OuterLandsPortalAllocationData.Destination destination(
            ServerLevel source
    ) {
        if (!hasDestination) {
            OuterLandsPortalAllocationData.Destination allocated =
                    OuterLandsPortalAllocationData.get(source).allocate();
            destinationRegionX = allocated.regionX();
            destinationRegionZ = allocated.regionZ();
            hasDestination = true;
            setChanged();
        }
        return new OuterLandsPortalAllocationData.Destination(
                destinationRegionX,
                destinationRegionZ
        );
    }

    @Override
    protected void saveAdditional(CompoundTag root) {
        super.saveAdditional(root);
        root.putBoolean("HasMazeDestination", hasDestination);
        if (hasDestination) {
            root.putInt("MazeRegionX", destinationRegionX);
            root.putInt("MazeRegionZ", destinationRegionZ);
        }
    }

    @Override
    public void load(CompoundTag root) {
        super.load(root);
        hasDestination = root.getBoolean("HasMazeDestination");
        destinationRegionX = root.getInt("MazeRegionX");
        destinationRegionZ = root.getInt("MazeRegionZ");
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(1.0D);
    }
}
