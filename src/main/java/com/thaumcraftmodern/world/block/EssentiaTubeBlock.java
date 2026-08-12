package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.essentia.EssentiaConnections;
import com.thaumcraftmodern.essentia.EssentiaTransport;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import com.thaumcraftmodern.essentia.tube.TubeEssentiaReleaseRules;
import com.thaumcraftmodern.essentia.tube.TubeEssentiaReleaseRisk;
import com.thaumcraftmodern.essentia.tube.TubeFacingRules;
import com.thaumcraftmodern.essentia.tube.TubeWandTargetResolver;
import com.thaumcraftmodern.item.JarLabelItem;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.wand.WandInteractable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Locale;

public final class EssentiaTubeBlock extends BaseEntityBlock
        implements WandInteractable {
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty FLOWING = BooleanProperty.create("flowing");
    private static final VoxelShape CORE = box(6, 6, 6, 10, 10, 10);

    private final ResourceLocation policyId;

    public EssentiaTubeBlock(Properties properties, ResourceLocation policyId) {
        super(properties);
        TubePolicyRegistry.require(policyId);
        this.policyId = policyId;
        registerDefaultState(stateDefinition.any()
                .setValue(DOWN, false).setValue(UP, false)
                .setValue(NORTH, false).setValue(SOUTH, false)
                .setValue(WEST, false).setValue(EAST, false)
                .setValue(FACING, Direction.NORTH).setValue(FLOWING, true));
    }

    public ResourceLocation policyId() {
        return policyId;
    }

    /**
     * TC4's BlockTubeItem stores the clicked side directly in TileTube.facing.
     * Keeping that side in the block state also gives the client renderer the
     * correct axis immediately, including UP and DOWN placements.
     */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        VoxelShape shape = CORE;
        Direction viewedReconnect = reconnectDirection(
                state, level, pos, context);
        boolean cameraSelection = context instanceof EntityCollisionContext entityContext
                && entityContext.getEntity() != null;
        for (Direction direction : Direction.values()) {
            if (state.getValue(property(direction)) || cameraSelection
                    && direction == viewedReconnect || !cameraSelection
                    && isReconnectCandidate(state, level, pos, direction)) {
                shape = Shapes.or(shape, arm(direction));
            }
        }
        return shape;
    }

    private static @Nullable Direction reconnectDirection(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return null;
        }
        Entity entity = entityContext.getEntity();
        if (entity == null) {
            return null;
        }
        Vec3 eye = entity.getEyePosition(1.0F);
        Vec3 end = eye.add(entity.getViewVector(1.0F).scale(16.0D));
        return selectReconnectDirection(state, level, pos, eye, end);
    }

    public static @Nullable Direction selectReconnectDirection(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Vec3 eye,
            Vec3 end
    ) {
        Direction selected = null;
        double selectedDistance = Double.POSITIVE_INFINITY;
        for (Direction direction : Direction.values()) {
            if (!isReconnectCandidate(state, level, pos, direction)) {
                continue;
            }
            BlockHitResult hit = arm(direction).clip(eye, end, pos);
            if (hit == null) {
                continue;
            }
            double distance = eye.distanceToSqr(hit.getLocation());
            if (distance < selectedDistance) {
                selected = direction;
                selectedDistance = distance;
            }
        }
        return selected;
    }

    private static boolean isReconnectCandidate(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction direction
    ) {
        if (state.getValue(property(direction))
                || !(level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity local)
                || local.isSideOpen(direction)) {
            return false;
        }
        return level.getBlockEntity(pos.relative(direction))
                instanceof EssentiaTransport remote
                && (remote instanceof EssentiaTubeBlockEntity
                        || remote.isConnectable(direction.getOpposite()));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        VoxelShape shape = CORE;
        for (Direction direction : Direction.values()) {
            if (state.getValue(property(direction))) {
                shape = Shapes.or(shape, arm(direction));
            }
        }
        return shape;
    }

    /**
     * Keeps a fully retracted branch selectable while a compatible essentia
     * transport is directly adjacent on that side. It does not become part of
     * the collision, rendered model, or transport connection until the server
     * reopens it.
     */
    private static VoxelShape arm(Direction direction) {
        return switch (direction) {
            case DOWN -> box(7, 0, 7, 9, 6, 9);
            case UP -> box(7, 10, 7, 9, 16, 9);
            case NORTH -> box(7, 7, 0, 9, 9, 6);
            case SOUTH -> box(7, 7, 10, 9, 9, 16);
            case WEST -> box(0, 7, 7, 6, 9, 9);
            case EAST -> box(10, 7, 7, 16, 9, 9);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST, FACING, FLOWING);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos,
            BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.level.block.Block block, BlockPos fromPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, fromPos, movedByPiston);
        if (!level.isClientSide) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos,
            net.minecraft.util.RandomSource random) {
        refreshConnections(level, pos);
    }

    public static void refreshConnections(Level level, BlockPos pos) {
        if (level.isClientSide
                || !(level.getBlockState(pos).getBlock() instanceof EssentiaTubeBlock)
                || !(level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity tube)) return;
        BlockState state = level.getBlockState(pos);
        BlockState updated = state;
        for (Direction direction : Direction.values()) {
            updated = updated.setValue(property(direction),
                    EssentiaConnections.connected(level, pos, direction, tube));
        }
        updated = updated.setValue(FACING, tube.facing())
                .setValue(FLOWING, tube.flowAllowed());
        if (updated != state) level.setBlock(pos, updated, 3);
    }

    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity tube)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (WandApi.state(held).isPresent()) {
            if (!level.isClientSide) {
                double x = hit.getLocation().x - pos.getX();
                double y = hit.getLocation().y - pos.getY();
                double z = hit.getLocation().z - pos.getZ();
                boolean core = TubeWandTargetResolver.hitsCore(x, y, z);
                if (player.isShiftKeyDown() && tube.essentiaAmount(null) > 0
                        && player instanceof ServerPlayer serverPlayer) {
                    releaseCloggedEssentia(
                            (ServerLevel) level, tube, serverPlayer, held);
                } else if (core && tube.policy().reversibleController()) {
                    if (player.isShiftKeyDown()) {
                        tube.selectReversibleHead();
                        tube.rotateFacing();
                    } else {
                        tube.toggleManualReturnFromWand();
                    }
                    player.displayClientMessage(Component.translatable(
                            tube.returnEnabled()
                                    ? "message.thaumcraftmodern.reverse.return"
                                    : "message.thaumcraftmodern.reverse.switching",
                            tube.reverseSwitchTicks()), true);
                } else if (core && tube.policy().redstoneValve()) {
                    tube.setFacing(TubeFacingRules.toggleFacing(
                            tube.facing(), hit.getDirection().getOpposite()));
                } else if (core) {
                    tube.rotateFacing();
                } else {
                    tube.toggleSide(resolveWandSide(hit, pos));
                }
                level.playSound(null, pos, ModSounds.TOOL.get(),
                        SoundSource.BLOCKS, 0.5F,
                        0.9F + level.random.nextFloat() * 0.2F);
                player.swing(hand, true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (tube.policy().filtered()) {
            if (player.isShiftKeyDown() && held.isEmpty() && tube.filter() != null) {
                if (!level.isClientSide) {
                    tube.setFilter(null);
                    Direction facing = hit.getDirection();
                    level.addFreshEntity(new ItemEntity(level,
                            pos.getX() + 0.5D + facing.getStepX() / 3.0D,
                            pos.getY() + 0.5D + facing.getStepY() / 3.0D,
                            pos.getZ() + 0.5D + facing.getStepZ() / 3.0D,
                            new ItemStack(ModItems.JAR_LABEL.get())));
                    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN,
                            SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (held.getItem() instanceof JarLabelItem && tube.filter() == null) {
                String aspect = JarLabelItem.aspect(held).orElse(null);
                if (aspect == null) {
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                if (!level.isClientSide) {
                    tube.setFilter(aspect);
                    if (!(player instanceof ServerPlayer serverPlayer)
                            || !serverPlayer.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN,
                            SoundSource.BLOCKS, 1.0F, 0.9F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if (tube.policy().redstoneValve()) {
            if (!level.isClientSide) {
                tube.setFlowAllowed(!tube.flowAllowed());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onWandRightClick(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        // WandItem uses this callback directly (including while sneaking),
        // while ordinary block activation enters use(). Keep one interaction
        // implementation so valve rotation and sounds cannot diverge.
        return use(state, level, pos, player, hand, hit);
    }

    private static void releaseCloggedEssentia(
            ServerLevel level,
            EssentiaTubeBlockEntity tube,
            ServerPlayer player,
            ItemStack wand
    ) {
        String aspect = tube.essentiaType(null);
        if (aspect == null) return;
        TubeEssentiaReleaseRules.Complexity complexity;
        try {
            complexity = TubeEssentiaReleaseRules.complexity(
                    com.thaumcraftmodern.aspect.AspectRegistryRuntime.catalog(),
                    aspect);
        } catch (RuntimeException exception) {
            return;
        }
        Map<String, Integer> baseCost =
                TubeEssentiaReleaseRules.baseVisCostCentivis(complexity);
        Map<String, Integer> adjustedCost;
        try {
            adjustedCost = WandVisService.adjustedFractionalCostCentivis(
                    player, wand, baseCost);
        } catch (RuntimeException exception) {
            return;
        }
        if (!WandVisService.consumeCentivis(player, wand, baseCost)) {
            player.displayClientMessage(Component.translatable(
                    "message.thaumcraftmodern.tube_release.no_vis",
                    formatVisRange(adjustedCost)), true);
            return;
        }
        TubeEssentiaReleaseRules.Release release =
                TubeEssentiaReleaseRisk.preview(player, complexity);
        if (!tube.releaseCloggedEssentia(level, release.createsFlux())) return;
        TubeEssentiaReleaseRisk.commit(player, release);
        player.displayClientMessage(Component.translatable(
                release.createsFlux()
                        ? "message.thaumcraftmodern.tube_release.flux"
                        : "message.thaumcraftmodern.tube_release.released",
                Component.translatable("tc.aspect." + aspect),
                complexity.risk(),
                release.createsFlux() ? 0 : release.accumulatedRisk(),
                formatVisRange(adjustedCost)), true);
    }

    private static String formatVisRange(Map<String, Integer> costs) {
        int minimum = costs.values().stream().mapToInt(Integer::intValue)
                .min().orElse(0);
        int maximum = costs.values().stream().mapToInt(Integer::intValue)
                .max().orElse(0);
        return minimum == maximum
                ? formatVis(minimum)
                : formatVis(minimum) + "–" + formatVis(maximum);
    }

    private static String formatVis(int centivis) {
        String value = String.format(Locale.ROOT, "%.2f", centivis / 100.0D);
        return value.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof EssentiaTubeBlockEntity tube
                && tube.policy().filtered() && tube.filter() != null) {
            popResource(level, pos, new ItemStack(ModItems.JAR_LABEL.get()));
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    /**
     * Resolves the actual tube branch under the wand. Unlike relying only on
     * {@link BlockHitResult#getDirection()}, this keeps a click on the long
     * side wall of an arm attached to that arm instead of its perpendicular
     * face. The centre remains TC4's dedicated rotation target (sub-hit 6).
     */
    public static Direction resolveWandSide(BlockHitResult hit, BlockPos pos) {
        double x = hit.getLocation().x - pos.getX();
        double y = hit.getLocation().y - pos.getY();
        double z = hit.getLocation().z - pos.getZ();
        return TubeWandTargetResolver.resolve(x, y, z, hit.getDirection());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaTubeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type,
                        com.thaumcraftmodern.registry.ModBlockEntities.ESSENTIA_TUBE.get(),
                        EssentiaTubeBlockEntity::serverTick)
                : createTickerHelper(type,
                        com.thaumcraftmodern.registry.ModBlockEntities.ESSENTIA_TUBE.get(),
                        EssentiaTubeBlockEntity::clientTick);
    }
}
