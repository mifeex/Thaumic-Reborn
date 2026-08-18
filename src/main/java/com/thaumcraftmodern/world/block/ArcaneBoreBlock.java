package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBlockEntity;
import com.thaumcraftmodern.wand.WandInteractable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** TC4 Arcane Bore: attaches exclusively to the top or bottom of its base. */
public final class ArcaneBoreBlock extends BaseEntityBlock implements WandInteractable {
    public ArcaneBoreBlock(Properties properties) { super(properties); }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction baseSide = context.getClickedFace();
        if (baseSide != Direction.UP && baseSide != Direction.DOWN
                || !context.getLevel().getBlockState(context.getClickedPos()
                        .relative(baseSide.getOpposite()))
                        .is(ModBlocks.ARCANE_BORE_BASE.get())) return null;
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer,
            net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            Direction attachment = level.getBlockState(pos.below())
                    .is(ModBlocks.ARCANE_BORE_BASE.get()) ? Direction.UP : Direction.DOWN;
            Direction mining = placer == null ? Direction.SOUTH
                    : Direction.getNearest((float) (placer.getX() - pos.getX() - 0.5D),
                            (float) (placer.getEyeY() - pos.getY() - 0.5D),
                            (float) (placer.getZ() - pos.getZ() - 0.5D));
            bore.configurePlacement(attachment, mining);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.ARCANE_BORE_BASE.get())
                || level.getBlockState(pos.above()).is(ModBlocks.ARCANE_BORE_BASE.get());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbor,
            net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return super.updateShape(state, side, neighbor, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changed,
            BlockPos changedPos, boolean moving) {
        if (!level.isClientSide && !state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, level, pos, changed, changedPos, moving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        Direction direction = level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore
                ? bore.orientation() : Direction.NORTH;
        VoxelShape own = Shapes.block();
        return Shapes.or(own, switch (direction) {
            case DOWN -> box(0, -16, 0, 16, 0, 16);
            case UP -> box(0, 16, 0, 16, 32, 16);
            case NORTH -> box(0, 0, -16, 16, 16, 0);
            case SOUTH -> box(0, 0, 16, 16, 16, 32);
            case WEST -> box(-16, 0, 0, 0, 16, 16);
            case EAST -> box(16, 0, 0, 32, 16, 16);
        });
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof WandItem) return InteractionResult.PASS;
        if (!level.isClientSide && player instanceof ServerPlayer server
                && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            NetworkHooks.openScreen(server, bore, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult onWandRightClick(BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            bore.setOrientation(hit.getDirection());
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS, 0.3F, 1.9F + level.random.nextFloat() * 0.2F);
        }
        player.swing(hand);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof ArcaneBoreBlockEntity bore) {
            Containers.dropContents(level, pos, bore);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? createTickerHelper(type, ModBlockEntities.ARCANE_BORE.get(),
                        ArcaneBoreBlockEntity::clientTick)
                : level instanceof ServerLevel
                        ? createTickerHelper(type, ModBlockEntities.ARCANE_BORE.get(),
                                ArcaneBoreBlockEntity::serverTick)
                        : null;
    }
}
