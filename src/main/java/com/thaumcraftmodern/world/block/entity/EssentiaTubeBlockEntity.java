package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumicreborn.api.essentia.EssentiaFlowMode;
import com.thaumcraftmodern.essentia.EssentiaSync;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumcraftmodern.essentia.tube.TubeFlowRules;
import com.thaumcraftmodern.essentia.tube.TubeFacingRules;
import com.thaumcraftmodern.essentia.tube.TubePolicy;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.particle.TubeVentParticleOptions;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class EssentiaTubeBlockEntity extends BlockEntity
        implements EssentiaTransport {
    public static final int TRANSFER_INTERVAL = 5;
    public static final int SUCTION_INTERVAL = 2;
    public static final int VENT_TICKS = 40;

    private final boolean[] openSides = {true, true, true, true, true, true};
    private Direction facing = Direction.NORTH;
    private @Nullable String essentiaType;
    private int essentiaAmount;
    private @Nullable String suctionType;
    private int suction;
    private @Nullable String filter;
    private int venting;
    private int ventColor = 0xAAAAAA;
    private int count;
    private boolean tickInitialized;
    private boolean flowAllowed = true;
    private boolean poweredLastTick;
    private boolean manualReturnRequested;
    private boolean reverseTarget;
    private boolean returnEnabled;
    private boolean reversibleArrowVisible = true;
    private int reverseSwitchTicks;
    private EssentiaFlowMode suctionFlowMode = EssentiaFlowMode.SUPPLY;
    private long suctionController;
    private boolean controllerConflict;
    private float valveRotation;
    private float previousValveRotation;
    private final float[] sideRetraction = new float[6];
    private final float[] previousSideRetraction = new float[6];

    public EssentiaTubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENTIA_TUBE.get(), pos, state);
        if (state.getBlock() instanceof EssentiaTubeBlock tube
                && tube.policyId().equals(TubePolicyRegistry.REVERSIBLE)) {
            facing = Direction.DOWN;
        } else if (state.hasProperty(EssentiaTubeBlock.FACING)) {
            facing = state.getValue(EssentiaTubeBlock.FACING);
        }
    }

    public TubePolicy policy() {
        return TubePolicyRegistry.require(policyId());
    }

    private ResourceLocation policyId() {
        if (getBlockState().getBlock() instanceof EssentiaTubeBlock tube) {
            return tube.policyId();
        }
        return TubePolicyRegistry.PLAIN;
    }

    public static void serverTick(net.minecraft.world.level.Level rawLevel, BlockPos pos,
            BlockState state, EssentiaTubeBlockEntity tube) {
        if (!(rawLevel instanceof ServerLevel level)) return;
        if (!tube.tickInitialized) {
            tube.tickInitialized = true;
            tube.count = level.random.nextInt(10);
            EssentiaTubeBlock.refreshConnections(level, pos);
        }
        if (tube.venting > 0) {
            tube.venting--;
            if (tube.venting > 0) return;
        }
        tube.count++;
        TubePolicy policy = tube.policy();
        if (policy.reversibleController()) {
            tube.tickReversibleControl(level);
        }
        if (policy.redstoneValve() && tube.count % TRANSFER_INTERVAL == 0) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered != tube.poweredLastTick) {
                tube.poweredLastTick = powered;
                tube.setFlowAllowed(!powered);
                level.playSound(null, pos, ModSounds.SQUEEK.get(),
                        SoundSource.BLOCKS, 0.7F,
                        0.9F + level.random.nextFloat() * 0.2F);
            }
        }
        if (tube.count % SUCTION_INTERVAL == 0) {
            tube.calculateSuction();
            tube.checkVenting(level);
            if (tube.essentiaAmount == 0) {
                tube.essentiaType = null;
            }
        }
        if (tube.count % TRANSFER_INTERVAL == 0 && tube.suction > 0) {
            tube.equalize(level);
        }
    }

    public static void clientTick(net.minecraft.world.level.Level level, BlockPos pos,
            BlockState state, EssentiaTubeBlockEntity tube) {
        if (tube.venting > 0) {
            tube.venting--;
            if (tube.venting > 0) {
                RandomSource random = RandomSource.create(pos.asLong() * 4L);
                float pitch = random.nextFloat() * 360.0F;
                float yaw = random.nextFloat() * 360.0F;
                double xSpeed = -Mth.sin(yaw * Mth.DEG_TO_RAD)
                        * Mth.cos(pitch * Mth.DEG_TO_RAD);
                double zSpeed = Mth.cos(yaw * Mth.DEG_TO_RAD)
                        * Mth.cos(pitch * Mth.DEG_TO_RAD);
                double ySpeed = -Mth.sin(pitch * Mth.DEG_TO_RAD);
                level.addParticle(
                        new TubeVentParticleOptions(tube.ventColor),
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        xSpeed / 5.0D,
                        ySpeed / 5.0D,
                        zSpeed / 5.0D
                );
            }
        }
        tube.previousValveRotation = tube.valveRotation;
        if (!tube.flowAllowed && tube.valveRotation < 360.0F) {
            tube.valveRotation = Math.min(360.0F, tube.valveRotation + 20.0F);
        } else if (tube.flowAllowed && tube.valveRotation > 0.0F) {
            tube.valveRotation = Math.max(0.0F, tube.valveRotation - 20.0F);
        }
        for (Direction side : Direction.values()) {
            int index = side.ordinal();
            tube.previousSideRetraction[index] = tube.sideRetraction[index];
            float target = tube.openSides[index] ? 0.0F : 1.0F;
            if (tube.sideRetraction[index] < target) {
                tube.sideRetraction[index] = Math.min(target,
                        tube.sideRetraction[index] + 0.2F);
            } else if (tube.sideRetraction[index] > target) {
                tube.sideRetraction[index] = Math.max(target,
                        tube.sideRetraction[index] - 0.2F);
            }
        }
    }

    private void calculateSuction() {
        suction = 0;
        suctionType = null;
        suctionFlowMode = EssentiaFlowMode.SUPPLY;
        suctionController = 0L;
        controllerConflict = false;
        TubePolicy policy = policy();
        if (!flowAllowed || reverseSwitchTicks > 0) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (!TubeFlowRules.acceptsSuctionFrom(policy, facing, direction)) {
                continue;
            }
            if (!isConnectable(direction) || level == null) {
                continue;
            }
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, direction).orElse(null);
            if (remote == null) {
                continue;
            }
            Direction remoteSide = direction.getOpposite();
            String remoteSuction = remote.suctionType(remoteSide);
            if (filter != null && remoteSuction != null
                    && !filter.equals(remoteSuction)) {
                continue;
            }
            if (filter == null && essentiaAmount > 0
                    && remoteSuction != null
                    && !Objects.equals(essentiaType, remoteSuction)) {
                continue;
            }
            if (filter != null && essentiaAmount > 0
                    && essentiaType != null && remoteSuction != null
                    && !Objects.equals(essentiaType, remoteSuction)) {
                continue;
            }
            int remoteAmount = remote.suctionAmount(remoteSide);
            EssentiaFlowMode remoteMode = remote.suctionFlowMode(remoteSide);
            if (policy.reversibleController()
                    && (remoteMode == EssentiaFlowMode.RETURN) != returnEnabled) {
                continue;
            }
            long remoteController = remoteMode == EssentiaFlowMode.RETURN
                    ? remote.suctionController(remoteSide)
                    : 0L;
            if (remoteMode == EssentiaFlowMode.RETURN
                    && policy.reversibleController()) {
                remoteController = worldPosition.asLong();
            }
            int propagated = TubeFlowRules.propagatedSuction(policy, remoteAmount);
            if (remoteMode == EssentiaFlowMode.RETURN
                    && suctionFlowMode == EssentiaFlowMode.RETURN
                    && suctionController != 0L && remoteController != 0L
                    && suctionController != remoteController
                    && propagated == suction) {
                controllerConflict = true;
                suction = 0;
                suctionType = null;
                suctionController = 0L;
                return;
            }
            if (remoteAmount <= 0 || propagated <= suction) {
                continue;
            }
            setSuction(remoteSuction == null ? filter : remoteSuction,
                    propagated);
            suctionFlowMode = remoteMode;
            suctionController = remoteController;
        }
    }

    private void checkVenting(ServerLevel level) {
        for (Direction direction : Direction.values()) {
            if (!isConnectable(direction)) {
                continue;
            }
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, direction).orElse(null);
            if (remote == null) {
                continue;
            }
            int remoteSuction = remote.suctionAmount(direction.getOpposite());
            if (suction > 0
                    && (remoteSuction == suction || remoteSuction == suction - 1)
                    && !Objects.equals(suctionType,
                            remote.suctionType(direction.getOpposite()))) {
                ventColor = suctionType == null
                        ? 0xAAAAAA
                        : AspectRegistryRuntime.find(suctionType)
                                .map(definition -> definition.color())
                                .orElse(0xAAAAAA);
                level.blockEvent(
                        worldPosition,
                        getBlockState().getBlock(),
                        1,
                        ventColor
                );
                venting = VENT_TICKS;
                return;
            }
        }
    }

    private void equalize(ServerLevel level) {
        if (essentiaAmount > 0 || !flowAllowed) {
            return;
        }
        for (Direction direction : Direction.values()) {
            if (!TubeFlowRules.mayPullFrom(policy(), facing, direction)) {
                continue;
            }
            if (!isConnectable(direction)) {
                continue;
            }
            EssentiaTransport remote = EssentiaConnections.neighbour(
                    level, worldPosition, direction).orElse(null);
            if (remote == null || !remote.canOutputTo(direction.getOpposite())) {
                continue;
            }
            if (suctionFlowMode == EssentiaFlowMode.RETURN
                    && !remote.canReturnEssentia()) {
                continue;
            }
            String remoteType = remote.essentiaType(direction.getOpposite());
            String wanted = TubeFlowRules.requestedAspect(suctionType, remoteType);
            if (suction <= remote.suctionAmount(direction.getOpposite())
                    || suction < remote.minimumSuction()) {
                continue;
            }
            if (wanted == null) {
                wanted = remote.essentiaType(null);
            }
            if (wanted == null) {
                continue;
            }
            int taken = remote.takeEssentia(
                    wanted, 1, direction.getOpposite());
            if (taken > 0 && addEssentia(wanted, taken, direction) > 0) {
                if (level.random.nextInt(100) == 0) {
                    level.blockEvent(
                            worldPosition,
                            getBlockState().getBlock(),
                            0,
                            0
                    );
                }
                EssentiaSync.changed(this);
                return;
            }
        }
    }

    public void toggleSide(Direction side) {
        boolean open = !openSides[side.ordinal()];
        setSideOpen(side, open);
        if (level != null && level.getBlockEntity(worldPosition.relative(side))
                instanceof EssentiaTubeBlockEntity remote) {
            remote.setSideOpen(side.getOpposite(), open);
        }
        refreshVisualConnections();
    }

    private void setSideOpen(Direction side, boolean open) {
        int index = side.ordinal();
        if (openSides[index] == open) return;
        openSides[index] = open;
        EssentiaSync.changed(this);
    }

    public void rotateFacing() {
        if (level == null) return;
        facing = TubeFacingRules.nextConnectedFacing(facing,
                side -> isSideOpen(side)
                        && level.getBlockEntity(worldPosition.relative(side))
                                instanceof EssentiaTransport);
        EssentiaSync.changed(this);
        refreshVisualConnections();
    }

    public void setFacing(Direction facing) {
        this.facing = Objects.requireNonNull(facing, "facing");
        EssentiaSync.changed(this);
        refreshVisualConnections();
    }

    public Direction facing() {
        return facing;
    }

    public boolean isSideOpen(Direction side) {
        return openSides[side.ordinal()];
    }

    public float sideRetraction(Direction side, float partialTick) {
        int index = side.ordinal();
        return previousSideRetraction[index]
                + (sideRetraction[index] - previousSideRetraction[index]) * partialTick;
    }

    public @Nullable String filter() {
        return filter;
    }

    public void setFilter(@Nullable String filter) {
        this.filter = filter == null || filter.isBlank() ? null : filter;
        EssentiaSync.changed(this);
    }

    public boolean flowAllowed() {
        return flowAllowed;
    }

    public float valveRotation(float partialTick) {
        return previousValveRotation
                + (valveRotation - previousValveRotation) * partialTick;
    }

    public void setFlowAllowed(boolean flowAllowed) {
        this.flowAllowed = flowAllowed;
        if (!flowAllowed) {
            suction = 0;
            suctionType = null;
        }
        EssentiaSync.changed(this);
    }

    private void tickReversibleControl(ServerLevel level) {
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered != poweredLastTick) {
            poweredLastTick = powered;
            level.playSound(null, worldPosition, ModSounds.SQUEEK.get(),
                    SoundSource.BLOCKS, 0.7F,
                    0.9F + level.random.nextFloat() * 0.2F);
        }
        boolean desired = reversibleArrowVisible
                && (manualReturnRequested || powered);
        if (desired != reverseTarget) {
            reverseTarget = desired;
            reverseSwitchTicks = switchDelayTicks(worldPosition);
            EssentiaSync.changed(this);
        }
        if (reverseSwitchTicks > 0 && --reverseSwitchTicks == 0) {
            if (returnEnabled != reverseTarget) {
                returnEnabled = reverseTarget;
                facing = facing.getOpposite();
                refreshVisualConnections();
            }
            suction = 0;
            suctionType = null;
            suctionFlowMode = EssentiaFlowMode.SUPPLY;
            suctionController = 0L;
            level.playSound(null, worldPosition, ModSounds.SQUEEK.get(),
                    SoundSource.BLOCKS, 0.8F,
                    returnEnabled ? 0.75F : 1.15F);
            EssentiaSync.changed(this);
        }
    }

    public static int switchDelayTicks(BlockPos pos) {
        return 20 + Math.floorMod(Long.hashCode(pos.asLong()), 21);
    }

    public void toggleManualReturn() {
        manualReturnRequested = !reverseTarget;
        EssentiaSync.changed(this);
    }

    public void toggleManualReturnFromWand() {
        selectReversibleHead();
        toggleManualReturn();
    }

    public void selectReversibleHead() {
        if (!policy().reversibleController()) return;
        boolean wasVisible = reversibleArrowVisible;
        if (level != null && !level.isClientSide) {
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> visited = new HashSet<>();
            queue.add(worldPosition);
            visited.add(worldPosition);
            while (!queue.isEmpty() && visited.size() <= 64) {
                BlockPos current = queue.removeFirst();
                for (Direction direction : Direction.values()) {
                    BlockPos neighbour = current.relative(direction);
                    if (!visited.add(neighbour) || !level.hasChunkAt(neighbour)
                            || !(level.getBlockEntity(neighbour)
                                    instanceof EssentiaTubeBlockEntity other)
                            || !other.policy().reversibleController()) continue;
                    queue.addLast(neighbour);
                    if (other != this) other.resetReversibleHead();
                }
            }
        }
        if (!wasVisible) resetReversibleControl(true);
        reversibleArrowVisible = true;
        EssentiaSync.changed(this);
    }

    private void resetReversibleHead() {
        resetReversibleControl(false);
    }

    private void resetReversibleControl(boolean arrowVisible) {
        reversibleArrowVisible = arrowVisible;
        manualReturnRequested = false;
        reverseTarget = false;
        returnEnabled = false;
        reverseSwitchTicks = 0;
        facing = Direction.DOWN;
        suction = 0;
        suctionType = null;
        suctionFlowMode = EssentiaFlowMode.SUPPLY;
        suctionController = 0L;
        refreshVisualConnections();
        EssentiaSync.changed(this);
    }

    public boolean reversibleArrowVisible() {
        return policy().reversibleController() && reversibleArrowVisible;
    }

    public boolean returnEnabled() {
        return returnEnabled && reverseSwitchTicks == 0;
    }

    public boolean externalReturnRequested() {
        return reversibleArrowVisible
                && (manualReturnRequested || poweredLastTick);
    }

    public int reverseSwitchTicks() {
        return reverseSwitchTicks;
    }

    public boolean controllerConflict() {
        return controllerConflict;
    }

    public boolean releaseCloggedEssentia(
            ServerLevel level,
            boolean createsFlux
    ) {
        String vented = essentiaType;
        if (vented == null || vented.isBlank() || essentiaAmount <= 0) return false;
        essentiaAmount = 0;
        essentiaType = null;
        int color = AspectRegistryRuntime.find(vented)
                .map(definition -> definition.color()).orElse(0x8A3D8F);
        ventColor = color;
        venting = VENT_TICKS;
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, color);
        if (createsFlux) placeFluxGas(level);
        EssentiaSync.changed(this);
        return true;
    }

    private void placeFluxGas(ServerLevel level) {
        Direction[] order = {
                Direction.UP, Direction.NORTH, Direction.SOUTH,
                Direction.WEST, Direction.EAST, Direction.DOWN
        };
        for (Direction direction : order) {
            BlockPos gasPos = worldPosition.relative(direction);
            if (!level.getBlockState(gasPos).isAir()) continue;
            level.setBlock(gasPos,
                    ModBlocks.FLUX_GAS.get().defaultBlockState(), 3);
            return;
        }
    }

    private void refreshVisualConnections() {
        if (level == null || level.isClientSide) return;
        EssentiaTubeBlock.refreshConnections(level, worldPosition);
        for (Direction direction : Direction.values()) {
            BlockPos remote = worldPosition.relative(direction);
            if (level.getBlockState(remote).getBlock() instanceof EssentiaTubeBlock) {
                EssentiaTubeBlock.refreshConnections(level, remote);
            }
        }
    }

    public int ventingTicks() {
        return venting;
    }

    public int ventColor() {
        return ventColor;
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (level != null && level.isClientSide) {
            if (id == 0) {
                level.playLocalSound(
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D,
                        ModSounds.CREAK.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.3F + level.random.nextFloat() * 0.2F,
                        false
                );
                return true;
            }
            if (id == 1) {
                if (venting <= 0) {
                    level.playLocalSound(
                            worldPosition.getX() + 0.5D,
                            worldPosition.getY() + 0.5D,
                            worldPosition.getZ() + 0.5D,
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            0.1F,
                            1.0F + level.random.nextFloat() * 0.1F,
                            false
                    );
                }
                venting = 50;
                ventColor = data;
                return true;
            }
        }
        return super.triggerEvent(id, data);
    }

    @Override
    public boolean isConnectable(Direction side) {
        if (!openSides[side.ordinal()]) {
            return false;
        }
        return !policy().redstoneValve() || side != facing;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return flowAllowed && isConnectable(side);
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return flowAllowed && isConnectable(side);
    }

    @Override
    public void setSuction(@Nullable String aspect, int amount) {
        if (flowAllowed) {
            suctionType = aspect;
            suction = Math.max(0, amount);
        }
    }

    @Override
    public @Nullable String suctionType(Direction side) {
        return suctionType;
    }

    @Override
    public int suctionAmount(Direction side) {
        return suction;
    }

    @Override
    public EssentiaFlowMode suctionFlowMode(Direction side) {
        return suctionFlowMode;
    }

    @Override
    public long suctionController(Direction side) {
        return suctionFlowMode == EssentiaFlowMode.RETURN
                ? suctionController : 0L;
    }

    @Override
    public @Nullable String essentiaType(Direction side) {
        return essentiaType;
    }

    @Override
    public int essentiaAmount(Direction side) {
        return essentiaAmount;
    }

    @Override
    public int minimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(String aspect, int amount, Direction side) {
        if (amount <= 0 || !canOutputTo(side) || essentiaAmount <= 0
                || !Objects.equals(aspect, essentiaType)) {
            return 0;
        }
        essentiaAmount = 0;
        essentiaType = null;
        EssentiaSync.changed(this);
        return 1;
    }

    @Override
    public int addEssentia(String aspect, int amount, Direction side) {
        if (amount <= 0 || !canInputFrom(side) || essentiaAmount != 0) {
            return 0;
        }
        essentiaType = aspect;
        essentiaAmount = 1;
        EssentiaSync.changed(this);
        return 1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Policy", policyId().toString());
        tag.putInt("Side", facing.ordinal());
        tag.putByteArray("Open", openBytes());
        if (essentiaType != null) tag.putString("Type", essentiaType);
        tag.putInt("Amount", essentiaAmount);
        if (suctionType != null) tag.putString("SuctionType", suctionType);
        tag.putInt("Suction", suction);
        if (filter != null) tag.putString("Filter", filter);
        tag.putInt("Venting", venting);
        tag.putInt("VentColor", ventColor);
        tag.putBoolean("Flow", flowAllowed);
        tag.putBoolean("Powered", poweredLastTick);
        tag.putBoolean("ManualReturn", manualReturnRequested);
        tag.putBoolean("ReverseTarget", reverseTarget);
        tag.putBoolean("ReturnEnabled", returnEnabled);
        tag.putBoolean("ReverseArrowVisible", reversibleArrowVisible);
        tag.putInt("ReverseSwitch", reverseSwitchTicks);
        tag.putString("FlowMode", suctionFlowMode.name());
        tag.putLong("FlowController", suctionController);
        tag.putBoolean("ControllerConflict", controllerConflict);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Direction[] directions = Direction.values();
        int side = tag.getInt("Side");
        facing = side >= 0 && side < directions.length
                ? directions[side]
                : policy().reversibleController()
                        ? Direction.DOWN : Direction.NORTH;
        byte[] open = tag.getByteArray("Open");
        if (open.length == openSides.length) {
            for (int i = 0; i < open.length; i++) openSides[i] = open[i] != 0;
        }
        essentiaType = blankToNull(tag.getString("Type"));
        essentiaAmount = Math.min(1, Math.max(0, tag.getInt("Amount")));
        suctionType = blankToNull(tag.getString("SuctionType"));
        suction = Math.max(0, tag.getInt("Suction"));
        filter = blankToNull(tag.getString("Filter"));
        venting = Math.max(0, tag.getInt("Venting"));
        ventColor = tag.contains("VentColor")
                ? tag.getInt("VentColor")
                : 0xAAAAAA;
        flowAllowed = !tag.contains("Flow") || tag.getBoolean("Flow");
        poweredLastTick = tag.getBoolean("Powered");
        manualReturnRequested = tag.getBoolean("ManualReturn");
        reverseTarget = tag.getBoolean("ReverseTarget");
        returnEnabled = tag.getBoolean("ReturnEnabled");
        reversibleArrowVisible = !tag.contains("ReverseArrowVisible")
                || tag.getBoolean("ReverseArrowVisible");
        reverseSwitchTicks = Math.max(0, tag.getInt("ReverseSwitch"));
        try {
            suctionFlowMode = EssentiaFlowMode.valueOf(tag.getString("FlowMode"));
        } catch (IllegalArgumentException ignored) {
            suctionFlowMode = EssentiaFlowMode.SUPPLY;
        }
        suctionController = tag.getLong("FlowController");
        controllerConflict = tag.getBoolean("ControllerConflict");
    }

    private byte[] openBytes() {
        byte[] bytes = new byte[openSides.length];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) (openSides[i] ? 1 : 0);
        return bytes;
    }

    private static @Nullable String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
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
        if (tag != null) load(tag);
    }
}
