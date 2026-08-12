package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.api.wand.WandApi;
import com.thaumcraftmodern.essentia.tube.TubeFacingRules;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.EssentiaReservoirBlockEntity;
import com.thaumcraftmodern.wand.WandInteractable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** Full-block TC4 reservoir with one wand-selectable transport port. */
public final class EssentiaReservoirBlock extends BaseEntityBlock
        implements WandInteractable {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape FULL_CUBE = box(0, 0, 0, 16, 16, 16);

    public EssentiaReservoirBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.DOWN));
    }

    @Override protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING,
                context.getClickedFace().getOpposite());
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (WandApi.state(player.getItemInHand(hand)).isEmpty()) return InteractionResult.PASS;
        return rotatePortTowardLook(state, level, pos, player, hand, hit);
    }

    @Override public InteractionResult onWandRightClick(BlockState state,
            Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        return rotatePortTowardLook(state, level, pos, player, hand, hit);
    }

    private static InteractionResult rotatePortTowardLook(BlockState state,
            Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (!level.isClientSide) {
            // TC4 TileEssentiaReservoir.onWandRightClick uses the face reached
            // by the player's view ray. Normal use points the port through
            // that face; sneaking selects the near face itself.
            Direction preferred = player.isShiftKeyDown()
                    ? hit.getDirection() : hit.getDirection().getOpposite();
            Direction facing = TubeFacingRules.toggleFacing(
                    state.getValue(FACING), preferred);
            level.setBlock(pos, state.setValue(FACING, facing), UPDATE_ALL);
        }
        player.swing(hand, true);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof EssentiaReservoirBlockEntity reservoir)) return 0;
        int amount = reservoir.totalAmount();
        return Mth.floor((float) amount / EssentiaReservoirBlockEntity.CAPACITY * 14.0F)
                + (amount > 0 ? 1 : 0);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof EssentiaReservoirBlockEntity reservoir
                && reservoir.totalAmount() > 0) {
            int spills = Math.min(50, Math.max(1, reservoir.totalAmount() / 16));
            for (int i = 0; i < spills; i++) {
                BlockPos target = pos.offset(
                        level.random.nextInt(5) - level.random.nextInt(5),
                        level.random.nextInt(5) - level.random.nextInt(5),
                        level.random.nextInt(5) - level.random.nextInt(5));
                if (!level.isEmptyBlock(target)) continue;
                level.setBlock(target, (target.getY() < pos.getY()
                        ? ModBlocks.FLUX_GOO.get() : ModBlocks.FLUX_GAS.get())
                        .defaultBlockState(), UPDATE_ALL);
            }
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) { return FULL_CUBE; }
    @Override public VoxelShape getCollisionShape(BlockState state,
            BlockGetter level, BlockPos pos, CollisionContext context) {
        return FULL_CUBE;
    }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssentiaReservoirBlockEntity(pos, state);
    }
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ESSENTIA_RESERVOIR.get(),
                        EssentiaReservoirBlockEntity::serverTick)
                : createTickerHelper(type, ModBlockEntities.ESSENTIA_RESERVOIR.get(),
                        EssentiaReservoirBlockEntity::clientTick);
    }
}
