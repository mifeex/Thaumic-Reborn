package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.AuraNodeStateSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * Persistent world owner for an aura node.
 *
 * <p>The constructor takes its registered type so the package stays detached
 * from the central registry files. See the integration hook list in the
 * handoff.</p>
 */
public final class AuraNodeBlockEntity extends BlockEntity {
    private static final String STATE_KEY = "AuraNode";
    private static final String MOUND_GUARDIAN_KEY = "MoundGuardianSpawner";
    private static final String LAST_ACTIVE_KEY = "lastActive";
    private static final String REGENERATION_WAIT_KEY = "RegenerationWait";

    private AuraNodeState state;
    private boolean explicitlyInitialized;
    private String recoveryDiagnostic = "";
    private int drainEntityId = -1;
    private PrimalAspect drainAspect;
    private long drainGameTime = Long.MIN_VALUE;
    private int classicTicks;
    private int regenerationWait;
    private long lastActiveMillis;
    private boolean catchUpPending;
    private boolean moundGuardianSpawner;

    public AuraNodeBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state
    ) {
        super(Objects.requireNonNull(type, "type"), position, state);
        this.state = AuraNodeFactory.newWorldNode();
    }

    public synchronized AuraNodeState snapshotState() {
        return state.copy();
    }

    public synchronized AuraNodeScanIdentity scanIdentity() {
        return new AuraNodeScanIdentity(state.nodeId());
    }

    public synchronized boolean initializeOnce(AuraNodeState initialState) {
        Objects.requireNonNull(initialState, "initialState");
        if (explicitlyInitialized) {
            return false;
        }
        state = initialState.copy();
        explicitlyInitialized = true;
        markChangedAndSync();
        return true;
    }

    public synchronized void enableMoundGuardianSpawner() {
        if (moundGuardianSpawner) {
            return;
        }
        moundGuardianSpawner = true;
        markChangedAndSync();
    }

    public synchronized boolean isMoundGuardianSpawner() {
        return moundGuardianSpawner;
    }

    public synchronized boolean migrateLegacyUniformDark(
            AuraNodeState replacement
    ) {
        Objects.requireNonNull(replacement, "replacement");
        if (level == null
                || level.isClientSide
                || !LegacyUniformDarkNodeMigration.matches(state.snapshot())
                || replacement.type() != AuraNodeType.DARK
                || !replacement.nodeId().equals(state.nodeId())) {
            return false;
        }
        state = replacement.copy();
        explicitlyInitialized = true;
        markChangedAndSync();
        return true;
    }

    public synchronized boolean replaceCurrent(
            long expectedRevision,
            Map<PrimalAspect, Integer> nextCurrent
    ) {
        if (level == null || level.isClientSide) {
            return false;
        }
        boolean changed = state.replaceCurrent(expectedRevision, nextCurrent);
        if (changed) {
            markChangedAndSync();
        }
        return changed;
    }

    public synchronized NodeVisTransferService.Result transferToWand(
            NodeVisTransferService service,
            NodeVisTransferService.Request request,
            WandVisStore wand
    ) {
        Objects.requireNonNull(service, "service");
        NodeVisTransferService.Result result = service.transfer(request, state, wand);
        if (result.status() == NodeVisTransferService.Status.TRANSFERRED) {
            markChangedAndSync();
        }
        return result;
    }

    public synchronized NodeVisTransferService.Result transferToWand(
            NodeVisTransferService service,
            NodeVisTransferService.Request request,
            WandVisStore wand,
            PrimalAspect aspect,
            int maximumWholeVis,
            boolean preserveLastVis
    ) {
        Objects.requireNonNull(service, "service");
        NodeVisTransferService.Result result = service.transferAspect(
                request,
                state,
                wand,
                aspect,
                maximumWholeVis,
                preserveLastVis
        );
        if (result.status() == NodeVisTransferService.Status.TRANSFERRED) {
            markChangedAndSync();
        }
        return result;
    }

    public synchronized void markDrain(
            int entityId,
            PrimalAspect aspect,
            long gameTime
    ) {
        drainEntityId = entityId;
        drainAspect = Objects.requireNonNull(aspect, "aspect");
        drainGameTime = gameTime;
        markChangedAndSync();
    }

    public synchronized void clearDrain(int entityId) {
        if (drainEntityId != entityId) {
            return;
        }
        drainEntityId = -1;
        drainAspect = null;
        drainGameTime = Long.MIN_VALUE;
        markChangedAndSync();
    }

    public synchronized int drainEntityId() {
        return drainEntityId;
    }

    public synchronized @Nullable PrimalAspect drainAspect() {
        return drainAspect;
    }

    public synchronized long drainGameTime() {
        return drainGameTime;
    }

    synchronized void advanceClassicTick() {
        classicTicks++;
    }

    synchronized int classicTicks() {
        return classicTicks;
    }

    synchronized int regenerationWait() {
        return regenerationWait;
    }

    synchronized void decrementRegenerationWait() {
        if (regenerationWait > 0) {
            regenerationWait--;
        }
    }

    synchronized void setRegenerationWait(int ticks) {
        regenerationWait = Math.max(0, ticks);
    }

    synchronized long lastActiveMillis() {
        return lastActiveMillis;
    }

    synchronized void initializeLastActive(long nowMillis) {
        if (lastActiveMillis > 0L) {
            return;
        }
        lastActiveMillis = Math.max(1L, nowMillis);
        setChanged();
    }

    synchronized void setLastActiveMillis(long nextMillis) {
        lastActiveMillis = Math.max(0L, nextMillis);
        setChanged();
    }

    synchronized boolean consumeCatchUpPending() {
        boolean pending = catchUpPending;
        catchUpPending = false;
        return pending;
    }

    public synchronized boolean replaceAspects(
            long expectedRevision,
            Map<String, Integer> current,
            Map<String, Integer> maximum
    ) {
        if (level == null || level.isClientSide) {
            return false;
        }
        boolean changed = state.replaceAspects(
                expectedRevision,
                current,
                maximum
        );
        if (changed) {
            markChangedAndSync();
        }
        return changed;
    }

    synchronized boolean replaceType(AuraNodeType nextType) {
        Objects.requireNonNull(nextType, "nextType");
        if (level == null || level.isClientSide || state.type() == nextType) {
            return false;
        }
        AuraNodeState.Snapshot snapshot = state.snapshot();
        state = AuraNodeState.withAspects(
                snapshot.nodeId(),
                nextType,
                snapshot.modifier(),
                snapshot.aspectsCurrent(),
                snapshot.aspectsMaximum(),
                Math.addExact(snapshot.revision(), 1L)
        );
        markChangedAndSync();
        return true;
    }

    synchronized boolean replaceModifier(AuraNodeModifier nextModifier) {
        Objects.requireNonNull(nextModifier, "nextModifier");
        if (level == null || level.isClientSide
                || state.modifier() == nextModifier) {
            return false;
        }
        AuraNodeState.Snapshot snapshot = state.snapshot();
        state = AuraNodeState.withAspects(
                snapshot.nodeId(),
                snapshot.type(),
                nextModifier,
                snapshot.aspectsCurrent(),
                snapshot.aspectsMaximum(),
                Math.addExact(snapshot.revision(), 1L)
        );
        markChangedAndSync();
        return true;
    }

    public static void serverTick(
            net.minecraft.server.level.ServerLevel level,
            BlockPos position,
            BlockState blockState,
            AuraNodeBlockEntity node
    ) {
        AuraNodeServerTicker.tick(level, position, node);
    }

    public synchronized String recoveryDiagnostic() {
        return recoveryDiagnostic;
    }

    @Override
    protected synchronized void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(STATE_KEY, AuraNodeCodec.encode(state));
        if (lastActiveMillis > 0L) {
            tag.putLong(LAST_ACTIVE_KEY, lastActiveMillis);
        }
        if (regenerationWait > 0) {
            tag.putInt(REGENERATION_WAIT_KEY, regenerationWait);
        }
        if (moundGuardianSpawner) {
            tag.putBoolean(MOUND_GUARDIAN_KEY, true);
        }
    }

    @Override
    public synchronized void load(CompoundTag tag) {
        super.load(tag);
        AuraNodeCodec.DecodeResult decoded = AuraNodeCodec.decodeOrRecover(
                tag.getCompound(STATE_KEY),
                () -> AuraNodeFactory.recoveredWorldNode(worldPosition)
        );
        state = decoded.state();
        explicitlyInitialized = true;
        lastActiveMillis = Math.max(0L, tag.getLong(LAST_ACTIVE_KEY));
        regenerationWait = Math.max(0, tag.getInt(REGENERATION_WAIT_KEY));
        catchUpPending = lastActiveMillis > 0L;
        moundGuardianSpawner = tag.getBoolean(MOUND_GUARDIAN_KEY);
        recoveryDiagnostic = decoded.diagnostic();
        if (tag.contains("DrainEntity", CompoundTag.TAG_INT)
                && tag.contains("DrainAspect", CompoundTag.TAG_STRING)) {
            try {
                drainEntityId = tag.getInt("DrainEntity");
                drainAspect = PrimalAspect.fromId(tag.getString("DrainAspect"));
                drainGameTime = tag.getLong("DrainGameTime");
            } catch (RuntimeException exception) {
                drainEntityId = -1;
                drainAspect = null;
                drainGameTime = Long.MIN_VALUE;
            }
        } else {
            drainEntityId = -1;
            drainAspect = null;
            drainGameTime = Long.MIN_VALUE;
        }
        if (decoded.recovered()) {
            ThaumcraftModern.LOGGER.error(
                    "Recovered invalid aura node at {}: {}",
                    worldPosition,
                    recoveryDiagnostic
            );
        }
        notifyClientIndexChanged();
    }

    @Override
    public synchronized void onLoad() {
        super.onLoad();
        notifyClientIndexChanged();
    }

    @Override
    public synchronized void setRemoved() {
        if (level != null && level.isClientSide) {
            AuraNodeClientLifecycle.removed(this);
        }
        super.setRemoved();
    }

    @Override
    public synchronized CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        if (drainEntityId >= 0 && drainAspect != null) {
            tag.putInt("DrainEntity", drainEntityId);
            tag.putString("DrainAspect", drainAspect.id());
            tag.putLong("DrainGameTime", drainGameTime);
        }
        return tag;
    }

    @Override
    public synchronized @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public synchronized void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    private void markChangedAndSync() {
        setChanged();
        if (level != null) {
            BlockState blockState = getBlockState();
            level.sendBlockUpdated(worldPosition, blockState, blockState, 3);
            if (level instanceof ServerLevel serverLevel) {
                ModNetwork.sendToTrackingChunk(
                        serverLevel,
                        worldPosition,
                        new AuraNodeStateSyncPacket(
                                worldPosition,
                                getUpdateTag()
                        )
                );
            }
        }
    }

    private void notifyClientIndexChanged() {
        if (level != null && level.isClientSide) {
            AuraNodeClientLifecycle.changed(this);
        }
    }
}
