package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Saves and restores one block displaced by the portable-hole focus. */
public final class TemporaryHoleBlockEntity extends BlockEntity {
    public static final int OPENING_LAYERS_PER_TICK = 4;
    public static final int SAFE_REPLACEMENT_FLAGS = Block.UPDATE_CLIENTS
            | Block.UPDATE_KNOWN_SHAPE
            | Block.UPDATE_SUPPRESS_DROPS;

    private BlockState stored;
    private long restoreAt;
    private int remainingDepth;
    private int durationTicks;
    private @Nullable Direction tunnelDirection;
    private @Nullable UUID owner;

    public TemporaryHoleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEMPORARY_HOLE.get(), pos, state);
    }
    public void configure(
            BlockState stored,
            long restoreAt,
            int durationTicks,
            @Nullable Direction tunnelDirection,
            int remainingDepth,
            @Nullable UUID owner
    ) {
        this.stored = stored;
        this.restoreAt = restoreAt;
        this.durationTicks = durationTicks;
        this.tunnelDirection = tunnelDirection;
        this.remainingDepth = remainingDepth;
        this.owner = owner;
        setChanged();
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  TemporaryHoleBlockEntity hole) {
        if (level instanceof ServerLevel server
                && hole.tunnelDirection != null
                && hole.remainingDepth > 0) {
            Direction direction = hole.tunnelDirection;
            int depth = hole.remainingDepth;
            hole.tunnelDirection = null;
            hole.remainingDepth = 0;
            hole.setChanged();
            ServerPlayer player = hole.owner == null
                    ? null
                    : server.getServer().getPlayerList().getPlayer(hole.owner);
            BlockPos cursor = pos;
            TemporaryHoleBlockEntity current = hole;
            int opened = 0;
            while (depth > 0 && opened < OPENING_LAYERS_PER_TICK) {
                current.openPlane(server, cursor, direction, player);
                depth--;
                opened++;
                if (depth == 0 || opened == OPENING_LAYERS_PER_TICK) break;
                cursor = cursor.relative(direction);
                if (!createTunnelCell(
                        server,
                        cursor,
                        hole.durationTicks,
                        null,
                        0,
                        hole.owner
                )) {
                    depth = 0;
                    break;
                }
                if (!(server.getBlockEntity(cursor)
                        instanceof TemporaryHoleBlockEntity created)) {
                    depth = 0;
                    break;
                }
                current = created;
            }
            if (depth > 0) {
                createTunnelCell(
                        server,
                        cursor.relative(direction),
                        hole.durationTicks,
                        direction,
                        depth,
                        hole.owner
                );
            }
        }
        if (hole.stored != null && level.getGameTime() >= hole.restoreAt) {
            setBlockWithoutNeighborUpdates(level, pos, hole.stored);
        }
    }

    public static boolean canCreateTunnelCell(
            Level level,
            BlockPos pos,
            @Nullable Player player
    ) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir()
                && !state.hasBlockEntity()
                && !state.is(Blocks.BEDROCK)
                && !state.is(Blocks.IRON_DOOR)
                && !state.is(ModBlocks.TEMPORARY_HOLE.get())
                && !state.is(ModBlocks.WARDED_BLOCK.get())
                && state.blocksMotion()
                && state.getFluidState().isEmpty()
                && state.getDestroySpeed(level, pos) >= 0.0F
                && (player == null || player.mayInteract(level, pos));
    }

    public static boolean createTunnelCell(
            ServerLevel level,
            BlockPos pos,
            int durationTicks,
            @Nullable Direction direction,
            int remainingDepth,
            @Nullable UUID owner
    ) {
        ServerPlayer player = owner == null
                ? null
                : level.getServer().getPlayerList().getPlayer(owner);
        if (owner != null && player == null) return false;
        if (!canCreateTunnelCell(level, pos, player)) return false;
        BlockState stored = level.getBlockState(pos);
        if (!setBlockWithoutNeighborUpdates(
                level,
                pos,
                ModBlocks.TEMPORARY_HOLE.get().defaultBlockState()
        )) {
            return false;
        }
        if (!(level.getBlockEntity(pos) instanceof TemporaryHoleBlockEntity hole)) {
            setBlockWithoutNeighborUpdates(level, pos, stored);
            return false;
        }
        hole.configure(
                stored,
                level.getGameTime() + durationTicks,
                durationTicks,
                direction,
                remainingDepth,
                owner
        );
        return true;
    }

    private void openPlane(
            ServerLevel level,
            BlockPos center,
            Direction direction,
            @Nullable ServerPlayer player
    ) {
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                if (first == 0 && second == 0) continue;
                BlockPos candidate = planeOffset(
                        center,
                        direction,
                        first,
                        second
                );
                if (canCreateTunnelCell(level, candidate, player)) {
                    createTunnelCell(
                            level,
                            candidate,
                            durationTicks,
                            null,
                            0,
                            owner
                    );
                }
            }
        }
    }

    private static BlockPos planeOffset(
            BlockPos center,
            Direction direction,
            int first,
            int second
    ) {
        return switch (direction.getAxis()) {
            case Y -> center.offset(first, 0, second);
            case Z -> center.offset(first, second, 0);
            case X -> center.offset(0, first, second);
        };
    }

    /**
     * Swaps a portable-hole cell without notifying attached plants, torches or
     * crystal clusters that their support disappeared for a few seconds.
     */
    public static boolean setBlockWithoutNeighborUpdates(
            Level level,
            BlockPos pos,
            BlockState state
    ) {
        return level.setBlock(pos, state, SAFE_REPLACEMENT_FLAGS);
    }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (stored != null) tag.put("Stored", NbtUtils.writeBlockState(stored));
        tag.putLong("RestoreAt", restoreAt);
        tag.putInt("Duration", durationTicks);
        tag.putInt("RemainingDepth", remainingDepth);
        if (tunnelDirection != null) {
            tag.putByte(
                    "Direction",
                    (byte) tunnelDirection.get3DDataValue()
            );
        }
        if (owner != null) tag.putUUID("Owner", owner);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Stored")) stored = NbtUtils.readBlockState(
                level != null ? level.holderLookup(Registries.BLOCK)
                        : net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(),
                tag.getCompound("Stored"));
        restoreAt = tag.getLong("RestoreAt");
        durationTicks = tag.getInt("Duration");
        remainingDepth = tag.getInt("RemainingDepth");
        tunnelDirection = tag.contains("Direction")
                ? Direction.from3DDataValue(tag.getByte("Direction"))
                : null;
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }
}
