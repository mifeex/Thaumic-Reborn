package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.OuterLandsPortalBlockEntity;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsDimensions;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsLabyrinthGenerator;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsMaze;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsPortalAllocationData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

/** Bidirectional, server-authoritative gate between an altar and its maze. */
public final class OuterLandsPortalBlock extends BaseEntityBlock {
    private static final int DESTINATION_PRELOAD_RADIUS = 0;
    private static final String RETURN_X = "OuterLandsReturnX";
    private static final String RETURN_Y = "OuterLandsReturnY";
    private static final String RETURN_Z = "OuterLandsReturnZ";

    public OuterLandsPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        return new OuterLandsPortalBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                ModBlockEntities.OUTER_LANDS_PORTAL.get(),
                level.isClientSide
                        ? OuterLandsPortalBlockEntity::clientTick
                        : OuterLandsPortalBlockEntity::serverTick
        );
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos position,
            CollisionContext context
    ) {
        return net.minecraft.world.phys.shapes.Shapes.empty();
    }

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos position,
            Entity entity
    ) {
        if (!(level instanceof ServerLevel source)
                || !(entity instanceof ServerPlayer player)) {
            return;
        }
        transferPlayer(source, position, player);
    }

    public static void transferPlayer(
            ServerLevel source,
            BlockPos position,
            ServerPlayer player
    ) {
        if (player.isPassenger() || player.isVehicle()) {
            return;
        }
        if (player.isOnPortalCooldown()) {
            // TC4 keeps refreshing this while the player remains within the
            // gate, preventing an automatic bounce back after 100 ticks.
            player.setPortalCooldown(100);
            return;
        }
        boolean leaving = source.dimension().equals(
                OuterLandsDimensions.OUTER_LANDS
        );
        ServerLevel target = source.getServer().getLevel(
                leaving ? Level.OVERWORLD : OuterLandsDimensions.OUTER_LANDS
        );
        if (target == null) {
            return;
        }

        Vec3 arrival;
        CompoundTag persistent = player.getPersistentData();
        if (leaving) {
            arrival = persistent.contains(RETURN_Y)
                    ? new Vec3(
                            persistent.getDouble(RETURN_X),
                            persistent.getDouble(RETURN_Y),
                            persistent.getDouble(RETURN_Z)
                    )
                    : Vec3.atBottomCenterOf(target.getSharedSpawnPos());
        } else {
            if (!(source.getBlockEntity(position)
                    instanceof OuterLandsPortalBlockEntity portal)) {
                return;
            }
            OuterLandsPortalAllocationData.Destination maze =
                    portal.destination(source);
            int regionX = maze.regionX();
            int regionZ = maze.regionZ();
            int targetChunkX = regionX * OuterLandsMaze.REGION_SIZE_CHUNKS
                    + OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
            int targetChunkZ = regionZ * OuterLandsMaze.REGION_SIZE_CHUNKS
                    + OuterLandsMaze.REGION_SIZE_CHUNKS / 2;
            ChunkPos destinationChunk = new ChunkPos(
                    targetChunkX,
                    targetChunkZ
            );
            BlockPos ticketKey = new BlockPos(
                    targetChunkX * 16 + 8,
                    OuterLandsLabyrinthGenerator.BASE_Y + 4,
                    targetChunkZ * 16 + 8
            );
            /*
             * A direct ServerLevel#getChunk call blocks the integrated server
             * while Minecraft creates the complete dependency neighbourhood
             * for a fresh, distant maze. Request it through the chunk ticket
             * system and leave this tick immediately; the portal retries once
             * the fully decorated destination chunk is available. Radius zero
             * is deliberate: the Outer Lands feature pass builds thousands of
             * blocks per chunk, so vanilla's radius-three portal warm-up would
             * synchronously decorate dozens of labyrinth rooms before the next
             * server tick. The player's own ticket loads the surroundings after
             * transfer.
             */
            target.getChunkSource().addRegionTicket(
                    TicketType.PORTAL,
                    destinationChunk,
                    DESTINATION_PRELOAD_RADIUS,
                    ticketKey
            );
            if (target.getChunkSource().getChunkNow(
                    targetChunkX,
                    targetChunkZ
            ) == null) {
                return;
            }
            KnowledgeAccess.mutate(player, knowledge -> {
                knowledge.recordResearchCriterion(
                        "thaumic_reborn:legacy_clue/enterouter"
                );
                knowledge.completeResearch("enterouter");
            });
            persistent.putDouble(RETURN_X, player.getX());
            persistent.putDouble(RETURN_Y, player.getY());
            persistent.putDouble(RETURN_Z, player.getZ());
            arrival = new Vec3(
                    targetChunkX * 16 + 8.5D,
                    OuterLandsLabyrinthGenerator.BASE_Y + 4.0D,
                    targetChunkZ * 16 + 8.5D
            );
        }
        player.setPortalCooldown(100);
        Vec3 destination = arrival;
        player.changeDimension(target, new ITeleporter() {
            @Override
            public PortalInfo getPortalInfo(
                    Entity traveler,
                    ServerLevel destinationLevel,
                    java.util.function.Function<ServerLevel, PortalInfo> fallback
            ) {
                return new PortalInfo(
                        destination,
                        Vec3.ZERO,
                        traveler.getYRot(),
                        traveler.getXRot()
                );
            }
        });
    }
}
