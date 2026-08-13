package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToIntFunction;

/** Server-authoritative TC4 vis-network node with the original eight-block reach. */
public abstract class VisNetworkNodeBlockEntity extends BlockEntity {
    public static final int RANGE = 8;
    static final int NETWORK_RESCAN_INTERVAL = 40;
    private @Nullable BlockPos parentPosition;
    private int networkCounter = NETWORK_RESCAN_INTERVAL - 1;
    private byte attunement = -1;
    private int pulseTicks;
    private @Nullable PrimalAspect pulseAspect;
    private @Nullable PrimalAspect dominantAspect;
    private float pulseRed = 0.5F;
    private float pulseGreen = 0.5F;
    private float pulseBlue = 0.5F;
    private float beamOpacity = 0.3F;
    private int previousClientPulseTicks;

    protected VisNetworkNodeBlockEntity(
            BlockEntityType<?> type,
            BlockPos position,
            BlockState state
    ) {
        super(type, position, state);
    }

    public static void serverTick(
            net.minecraft.server.level.ServerLevel level,
            BlockPos position,
            BlockState state,
            VisNetworkNodeBlockEntity node
    ) {
        if (node.pulseTicks > 0) {
            node.pulseTicks--;
        }
        if (++node.networkCounter >= NETWORK_RESCAN_INTERVAL) {
            node.networkCounter = 0;
            BlockPos nextParent = node.isSource()
                    ? null : VisNetwork.findParent(level, node);
            if (!Objects.equals(nextParent, node.parentPosition)) {
                node.parentPosition = nextParent;
                VisNetworkSpatialIndex.topologyChanged(node);
                node.sync();
            }
        }
        node.serverNetworkTick();
        PrimalAspect nextDominant = node.currentDominantAspect();
        if (nextDominant != node.dominantAspect) {
            node.dominantAspect = nextDominant;
            node.sync();
        }
    }

    /** TC4 TileVisRelay.drawEffect: five-tick pulse, then +0.025 RGB fade. */
    public static void clientTick(
            net.minecraft.world.level.Level level,
            BlockPos position,
            BlockState state,
            VisNetworkNodeBlockEntity node
    ) {
        if (node.pulseTicks > 0
                && node.previousClientPulseTicks <= 0
                && node.pulseAspect != null) {
            int color = com.thaumcraftmodern.aura.PrimalAspectColors.color(
                    node.pulseAspect);
            node.pulseRed = ((color >> 16) & 255) / 255.0F;
            node.pulseGreen = ((color >> 8) & 255) / 255.0F;
            node.pulseBlue = (color & 255) / 255.0F;
            node.beamOpacity = 0.8F;
        }
        node.previousClientPulseTicks = node.pulseTicks;
        if (node.pulseTicks > 0) {
            node.pulseTicks--;
        }
        node.pulseRed = Math.min(1.0F, node.pulseRed + 0.025F);
        node.pulseGreen = Math.min(1.0F, node.pulseGreen + 0.025F);
        node.pulseBlue = Math.min(1.0F, node.pulseBlue + 0.025F);
        node.beamOpacity = Math.max(0.3F, node.beamOpacity - 0.025F);
    }

    protected void serverNetworkTick() {
    }

    public abstract boolean isSource();

    protected int consumeSource(PrimalAspect aspect, int amount) {
        return 0;
    }

    protected int availableSource(PrimalAspect aspect) {
        return 0;
    }

    public final int consumeVis(PrimalAspect aspect, int amount) {
        if (amount <= 0 || !(level instanceof ServerLevel serverLevel)) {
            return 0;
        }
        VisNetworkSpatialIndex.Route route =
                VisNetworkSpatialIndex.route(serverLevel, this);
        if (route == null
                || !(serverLevel.getBlockEntity(BlockPos.of(
                        route.sourcePosition()))
                instanceof VisNetworkNodeBlockEntity source)) {
            return 0;
        }
        int consumed = source.consumeSource(aspect, amount);
        // TC4 TileVisRelay.addPulse only starts a new five-tick pulse after
        // the previous one finished. Rewriting it to five on every transfer
        // prevents the client from ever observing the next rising edge while
        // a charger continuously accepts vis.
        if (consumed > 0) {
            for (long position : route.positions()) {
                if (serverLevel.getBlockEntity(BlockPos.of(position))
                        instanceof VisNetworkNodeBlockEntity node) {
                    node.addPulse(aspect);
                }
            }
        }
        return consumed;
    }

    private void addPulse(PrimalAspect aspect) {
        if (pulseTicks <= 0) {
            pulseAspect = aspect;
            pulseTicks = 5;
            sync();
        }
    }

    public final int availableVis(PrimalAspect aspect) {
        if (level instanceof ServerLevel serverLevel) {
            VisNetworkSpatialIndex.Route route =
                    VisNetworkSpatialIndex.route(serverLevel, this);
            if (route == null) {
                return 0;
            }
            return serverLevel.getBlockEntity(BlockPos.of(
                    route.sourcePosition()))
                    instanceof VisNetworkNodeBlockEntity source
                    ? source.availableSource(aspect) : 0;
        }
        return availableVis(aspect, new HashSet<>());
    }

    private int availableVis(PrimalAspect aspect, Set<BlockPos> visited) {
        if (level == null || !visited.add(worldPosition)) {
            return 0;
        }
        if (isSource()) {
            return availableSource(aspect);
        }
        VisNetworkNodeBlockEntity parent = parent();
        return parent == null ? 0 : parent.availableVis(aspect, visited);
    }

    public final boolean hasRouteToSource(Set<BlockPos> visited) {
        if (level instanceof ServerLevel serverLevel) {
            VisNetworkSpatialIndex.Route route =
                    VisNetworkSpatialIndex.route(serverLevel, this);
            if (route == null) {
                return false;
            }
            for (long position : route.positions()) {
                if (!visited.add(BlockPos.of(position))) {
                    return false;
                }
            }
            return true;
        }
        if (!visited.add(worldPosition)) {
            return false;
        }
        if (isSource()) {
            return true;
        }
        VisNetworkNodeBlockEntity parent = parent();
        return parent != null && parent.hasRouteToSource(visited);
    }

    public final boolean hasRouteToSource() {
        return level instanceof ServerLevel serverLevel
                && VisNetworkSpatialIndex.route(serverLevel, this) != null;
    }

    public final byte attunement() {
        return attunement;
    }

    public final void setAttunement(byte value) {
        if (value < -1 || value > 5) {
            throw new IllegalArgumentException("attunement must be -1..5");
        }
        attunement = value;
        parentPosition = null;
        networkCounter = NETWORK_RESCAN_INTERVAL - 1;
        VisNetworkSpatialIndex.topologyChanged(this);
        sync();
    }

    public final @Nullable BlockPos parentPosition() {
        return parentPosition;
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos parent = parentPosition;
        if (parent == null) {
            return new AABB(worldPosition).inflate(1.0D);
        }
        // The BER owns both the relay model and the complete beam from its
        // parent. A one-block default box lets LevelRenderer discard the
        // entire effect while either endpoint is still visible.
        return new AABB(
                Math.min(worldPosition.getX(), parent.getX()) - 1.0D,
                Math.min(worldPosition.getY(), parent.getY()) - 1.0D,
                Math.min(worldPosition.getZ(), parent.getZ()) - 1.0D,
                Math.max(worldPosition.getX(), parent.getX()) + 2.0D,
                Math.max(worldPosition.getY(), parent.getY()) + 2.0D,
                Math.max(worldPosition.getZ(), parent.getZ()) + 2.0D
        );
    }

    public final int pulseTicks() {
        return pulseTicks;
    }

    public final @Nullable PrimalAspect pulseAspect() {
        return pulseAspect;
    }

    /** Aspect with the largest amount currently available at the root node. */
    public final @Nullable PrimalAspect dominantAspect() {
        return dominantAspect;
    }

    private @Nullable PrimalAspect currentDominantAspect() {
        if (!isSource() && parent() == null) {
            return null;
        }
        return dominantAspect(aspect -> availableVis(aspect));
    }

    static @Nullable PrimalAspect dominantAspect(
            ToIntFunction<PrimalAspect> amount
    ) {
        PrimalAspect dominant = null;
        int largest = 0;
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            int available = amount.applyAsInt(aspect);
            if (available > largest) {
                dominant = aspect;
                largest = available;
            }
        }
        return dominant;
    }

    /**
     * Deterministic colour bands for a relay beam.  The source amounts, not
     * the relay's attunement, decide the palette: only primals with vis in the
     * energized node can appear and the largest pool receives the most bands.
     */
    public final List<PrimalAspect> beamAspectBands(int bandCount) {
        return beamAspectBands(aspect -> availableVis(aspect), bandCount);
    }

    static List<PrimalAspect> beamAspectBands(
            ToIntFunction<PrimalAspect> amount,
            int bandCount
    ) {
        if (bandCount <= 0) {
            return List.of();
        }
        int[] weights = new int[PrimalAspect.ordered().size()];
        int total = 0;
        for (int index = 0; index < weights.length; index++) {
            int available = Math.max(0,
                    amount.applyAsInt(PrimalAspect.ordered().get(index)));
            weights[index] = available;
            total += available;
        }
        if (total <= 0) {
            return List.of();
        }

        // Smooth weighted round-robin distributes each colour along the
        // complete beam instead of clumping the dominant aspect at one end.
        int[] current = new int[weights.length];
        List<PrimalAspect> bands = new ArrayList<>(bandCount);
        for (int band = 0; band < bandCount; band++) {
            int selected = -1;
            for (int index = 0; index < weights.length; index++) {
                current[index] += weights[index];
                if (weights[index] > 0 && (selected < 0
                        || current[index] > current[selected])) {
                    selected = index;
                }
            }
            current[selected] -= total;
            bands.add(PrimalAspect.ordered().get(selected));
        }
        return List.copyOf(bands);
    }

    public final float pulseRed() {
        return pulseRed;
    }

    public final float pulseGreen() {
        return pulseGreen;
    }

    public final float pulseBlue() {
        return pulseBlue;
    }

    public final float beamOpacity() {
        return beamOpacity;
    }

    private @Nullable VisNetworkNodeBlockEntity parent() {
        if (level == null || parentPosition == null
                || !level.isLoaded(parentPosition)) {
            return null;
        }
        return level.getBlockEntity(parentPosition)
                instanceof VisNetworkNodeBlockEntity node ? node : null;
    }

    protected final void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        VisNetworkSpatialIndex.track(this);
    }

    @Override
    public void setRemoved() {
        VisNetworkSpatialIndex.untrack(this);
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (parentPosition != null) {
            tag.putLong("VisParent", parentPosition.asLong());
        }
        tag.putByte("Attunement", attunement);
        tag.putByte("PulseTicks", (byte) pulseTicks);
        if (pulseAspect != null) {
            tag.putString("PulseAspect", pulseAspect.id());
        }
        if (dominantAspect != null) {
            tag.putString("DominantAspect", dominantAspect.id());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        parentPosition = tag.contains("VisParent")
                ? BlockPos.of(tag.getLong("VisParent")) : null;
        attunement = tag.contains("Attunement")
                ? tag.getByte("Attunement") : -1;
        pulseTicks = tag.getByte("PulseTicks");
        pulseAspect = tag.contains("PulseAspect")
                ? PrimalAspect.fromId(tag.getString("PulseAspect")) : null;
        dominantAspect = tag.contains("DominantAspect")
                ? PrimalAspect.fromId(tag.getString("DominantAspect")) : null;
        if (pulseTicks > 0) {
            previousClientPulseTicks = 0;
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
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }
}
