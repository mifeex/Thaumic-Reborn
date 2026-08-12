package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.essentia.AdvancedBufferFlowController;
import com.thaumcraftmodern.essentia.AdvancedBufferSideRole;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaFlowMode;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Four-point, side-programmable buffer and local flow controller. It only
 * inspects its six direct neighbours; ordinary tube suction carries remote
 * demand and return intent to those neighbours.
 */
public final class AdvancedEssentiaBufferBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int CAPACITY_PER_ASPECT = 4;
    /**
     * Must be strictly stronger than an ordinary jar. Essentia transport only
     * moves when local suction is greater than source suction; the previous
     * 32 == 32 tie made the configured input visibly connect but never pull.
     */
    public static final int INPUT_SUCTION = EssentiaJarBlockEntity.SUCTION + 1;
    public static final int RETURN_SUCTION = 64;
    public static final int MAIN_OUTPUT_DECISION_TICKS = 40;

    private final EssentiaStore supply = new EssentiaStore();
    private final EssentiaStore returned = new EssentiaStore();
    private final AdvancedBufferSideRole[] roles = {
            AdvancedBufferSideRole.INPUT,
            AdvancedBufferSideRole.BLOCKED,
            AdvancedBufferSideRole.MAIN_OUTPUT,
            AdvancedBufferSideRole.RESERVE_OUTPUT,
            AdvancedBufferSideRole.BLOCKED,
            AdvancedBufferSideRole.BLOCKED
    };
    private AdvancedBufferFlowController.Snapshot controller =
            AdvancedBufferFlowController.Snapshot.idle();
    private String blockedReasonKey =
            "diagnostic.thaumcraftmodern.advanced_buffer.ok";
    private int tickCount;
    private int mainOutputMissTicks;

    public AdvancedEssentiaBufferBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_ESSENTIA_BUFFER.get(), pos, state);
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel,
            BlockPos pos, BlockState state,
            AdvancedEssentiaBufferBlockEntity buffer) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        buffer.tickCount++;

        buffer.restoreReserveQueue();
        boolean activeConsumer = buffer.activeConsumer(level);
        AdvancedBufferFlowController.Signals signals =
                new AdvancedBufferFlowController.Signals(
                        activeConsumer,
                        false,
                        false,
                        true
                );
        AdvancedBufferFlowController.Snapshot previous = buffer.controller;
        buffer.controller = AdvancedBufferFlowController.advance(
                previous, signals, cooldownTicks(pos));
        if (!buffer.controller.equals(previous)) {
            EssentiaSync.changed(buffer);
        }

        if (buffer.tickCount % 5 != 0) return;
        buffer.fillFromInputs(level);
    }

    public static int cooldownTicks(BlockPos pos) {
        return 20 + Math.floorMod(Long.hashCode(pos.asLong() ^ 0x5F3759DFL), 21);
    }

    private boolean activeConsumer(ServerLevel level) {
        if (supply.isEmpty()) return false;
        for (Direction side : Direction.values()) {
            AdvancedBufferSideRole role = role(side);
            if (role != AdvancedBufferSideRole.MAIN_OUTPUT
                    && role != AdvancedBufferSideRole.RESERVE_OUTPUT) {
                continue;
            }
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, side).orElse(null);
            if (remote == null || !remote.canInputFrom(side.getOpposite())) {
                continue;
            }
            Direction remoteSide = side.getOpposite();
            if (remote.suctionAmount(remoteSide) <= minimumSuction()) continue;
            String wanted = remote.suctionType(remoteSide);
            if (wanted == null ? !supply.isEmpty() : supply.amount(wanted) > 0) {
                return true;
            }
        }
        return false;
    }

    private void restoreReserveQueue() {
        if (returned.isEmpty()) return;
        for (Map.Entry<String, Integer> entry
                : new ArrayList<>(returned.view().entrySet())) {
            int amount = entry.getValue();
            if (amount <= 0 || !returned.remove(entry.getKey(), amount)) continue;
            supply.add(entry.getKey(), amount);
        }
        EssentiaSync.changed(this);
    }

    private void fillFromInputs(ServerLevel level) {
        for (Direction side : Direction.values()) {
            if (role(side) != AdvancedBufferSideRole.INPUT) continue;
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, side).orElse(null);
            if (remote == null || !remote.canOutputTo(side.getOpposite())) continue;
            Direction remoteSide = side.getOpposite();
            String aspect = remote.essentiaType(remoteSide);
            if (aspect == null) aspect = remote.essentiaType(null);
            if (!hasRoom(aspect)
                    || suctionAmount(side) <= remote.suctionAmount(remoteSide)
                    || suctionAmount(side) < remote.minimumSuction()) continue;
            int taken = remote.takeEssentia(aspect, 1, remoteSide);
            if (taken > 0) {
                supply.add(aspect, 1);
                EssentiaSync.changed(this);
                return;
            }
        }
    }

    private boolean hasRoom(@Nullable String aspect) {
        return aspect != null && !aspect.isBlank()
                && supply.amount(aspect) + returned.amount(aspect)
                        < CAPACITY_PER_ASPECT;
    }

    private static @Nullable String first(EssentiaStore store) {
        return store.view().keySet().stream().findFirst().orElse(null);
    }

    public AdvancedBufferSideRole role(Direction side) {
        return roles[side.ordinal()];
    }

    public void cycleRole(Direction side) {
        AdvancedBufferSideRole next = role(side).next();
        if (next != AdvancedBufferSideRole.BLOCKED) {
            for (Direction other : Direction.values()) {
                if (other != side && role(other) == next) {
                    roles[other.ordinal()] = AdvancedBufferSideRole.BLOCKED;
                }
            }
        }
        roles[side.ordinal()] = next;
        EssentiaSync.changed(this);
        if (level != null && !level.isClientSide) {
            for (Direction direction : Direction.values()) {
                EssentiaTubeBlock.refreshConnections(
                        level, worldPosition.relative(direction));
            }
        }
    }

    public @Nullable Direction sideFor(AdvancedBufferSideRole role) {
        for (Direction side : Direction.values()) {
            if (role(side) == role) return side;
        }
        return null;
    }

    public AdvancedBufferFlowController.State flowState() {
        return controller.state();
    }

    public int stateTimer() {
        return controller.timer();
    }

    public int totalAmount() {
        return supply.total() + returned.total();
    }

    public Map<String, Integer> supplyContents() {
        return supply.view();
    }

    public Map<String, Integer> returnedContents() {
        return returned.view();
    }

    public Map<String, Integer> contents() {
        Map<String, Integer> combined = new LinkedHashMap<>(supply.view());
        returned.view().forEach((aspect, amount) ->
                combined.merge(aspect, amount, Integer::sum));
        return Map.copyOf(combined);
    }

    public String diagnosticReasonKey() {
        if (controller.state() == AdvancedBufferFlowController.State.BLOCKED) {
            if (sideFor(AdvancedBufferSideRole.MAIN_OUTPUT) == null) {
                return "diagnostic.thaumcraftmodern.advanced_buffer.no_main";
            }
            if (sideFor(AdvancedBufferSideRole.RESERVE_OUTPUT) == null) {
                return "diagnostic.thaumcraftmodern.advanced_buffer.no_reserve";
            }
            return blockedReasonKey;
        }
        return "diagnostic.thaumcraftmodern.advanced_buffer.ok";
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side != null && role(side) != AdvancedBufferSideRole.BLOCKED;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        if (side == null) return false;
        return role(side) == AdvancedBufferSideRole.INPUT;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        if (side == null) return false;
        return isOutputRole(role(side));
    }

    @Override
    public void setSuction(@Nullable String aspect, int amount) {
    }

    @Override
    public @Nullable String suctionType(Direction side) {
        return null;
    }

    @Override
    public int suctionAmount(Direction side) {
        return side == null ? 0 : suctionForRole(role(side));
    }

    public static boolean isOutputRole(AdvancedBufferSideRole role) {
        return role == AdvancedBufferSideRole.MAIN_OUTPUT
                || role == AdvancedBufferSideRole.RESERVE_OUTPUT;
    }

    public static int suctionForRole(AdvancedBufferSideRole role) {
        return role == AdvancedBufferSideRole.INPUT ? INPUT_SUCTION : 0;
    }

    @Override
    public EssentiaFlowMode suctionFlowMode(Direction side) {
        return EssentiaFlowMode.SUPPLY;
    }

    @Override
    public long suctionController(Direction side) {
        return 0L;
    }

    @Override
    public @Nullable String essentiaType(Direction side) {
        return requestedAspect(outputStore(side), side);
    }

    @Override
    public int essentiaAmount(Direction side) {
        return outputStore(side).total();
    }

    @Override
    public int minimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(String aspect, int amount, Direction side) {
        if (amount <= 0 || !canOutputTo(side)) return 0;
        EssentiaStore store = outputStore(side);
        if (!store.remove(aspect, 1)) return 0;
        EssentiaSync.changed(this);
        return 1;
    }

    private EssentiaStore outputStore(@Nullable Direction side) {
        return supply;
    }

    private @Nullable String requestedAspect(EssentiaStore store,
            @Nullable Direction side) {
        if (level != null && side != null) {
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, side).orElse(null);
            String wanted = remote == null ? null
                    : remote.suctionType(side.getOpposite());
            if (wanted != null && store.amount(wanted) > 0) return wanted;
        }
        return first(store);
    }

    @Override
    public int addEssentia(String aspect, int amount, Direction side) {
        if (amount != 1 || !canInputFrom(side) || !hasRoom(aspect)) return 0;
        supply.add(aspect, 1);
        EssentiaSync.changed(this);
        return 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Supply", supply.save());
        tag.put("Returned", returned.save());
        byte[] encodedRoles = new byte[roles.length];
        for (int index = 0; index < roles.length; index++) {
            encodedRoles[index] = (byte) roles[index].ordinal();
        }
        tag.putByteArray("Roles", encodedRoles);
        tag.putString("ControllerState", controller.state().name());
        tag.putInt("ControllerTimer", controller.timer());
        tag.putInt("ControllerQuiet", controller.quietTicks());
        tag.putString("BlockedReason", blockedReasonKey);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        supply.load(tag.getCompound("Supply"));
        returned.load(tag.getCompound("Returned"));
        trimCapacity();
        byte[] encodedRoles = tag.getByteArray("Roles");
        if (encodedRoles.length == roles.length) {
            AdvancedBufferSideRole[] values = AdvancedBufferSideRole.values();
            for (int index = 0; index < roles.length; index++) {
                int role = encodedRoles[index];
                roles[index] = role >= 0 && role < values.length
                        ? values[role] : AdvancedBufferSideRole.BLOCKED;
            }
        }
        try {
            controller = new AdvancedBufferFlowController.Snapshot(
                    AdvancedBufferFlowController.State.valueOf(
                            tag.getString("ControllerState")),
                    tag.getInt("ControllerTimer"),
                    tag.getInt("ControllerQuiet"));
        } catch (IllegalArgumentException ignored) {
            controller = AdvancedBufferFlowController.Snapshot.idle();
        }
        String reason = tag.getString("BlockedReason");
        blockedReasonKey = reason.isBlank()
                ? "diagnostic.thaumcraftmodern.advanced_buffer.ok" : reason;
    }

    private void trimCapacity() {
        for (String aspect : new ArrayList<>(supply.view().keySet())) {
            int overflow = supply.amount(aspect) + returned.amount(aspect)
                    - CAPACITY_PER_ASPECT;
            if (overflow > 0) supply.remove(aspect,
                    Math.min(overflow, supply.amount(aspect)));
        }
        for (String aspect : new ArrayList<>(returned.view().keySet())) {
            int overflow = supply.amount(aspect) + returned.amount(aspect)
                    - CAPACITY_PER_ASPECT;
            if (overflow > 0) returned.remove(aspect,
                    Math.min(overflow, returned.amount(aspect)));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection,
            ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag == null) return;
        AdvancedBufferSideRole[] previousRoles = roles.clone();
        load(tag);
        if (level != null && level.isClientSide
                && !Arrays.equals(previousRoles, roles)) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state,
                    Block.UPDATE_CLIENTS);
        }
    }
}
