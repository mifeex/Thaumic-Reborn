package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.ArcanePressurePlateBlockEntity;
import com.thaumcraftmodern.wand.WandInteractable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Owner-aware three-mode pressure plate from TC4 BlockWoodenDevice metadata 2/3. */
public final class ArcanePressurePlateBlock extends BaseEntityBlock implements WandInteractable {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final IntegerProperty MODE = IntegerProperty.create("mode", 0, 2);
    private static final VoxelShape UP = Block.box(1, 0, 1, 15, 1, 15);
    private static final VoxelShape DOWN = Block.box(1, 0, 1, 15, 0.5, 15);
    private static final AABB TOUCH = new AABB(0.125, 0, 0.125, 0.875, 0.25, 0.875);

    public ArcanePressurePlateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(MODE, 0));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, MODE);
    }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) { return state.getValue(POWERED) ? DOWN : UP; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) { return Shapes.empty(); }
    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportRigidBlock(level, pos.below()) || canSupportCenter(level, pos.below(), Direction.UP);
    }
    @Override public BlockState updateShape(BlockState state, Direction direction, BlockState neighbour,
            LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
    }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player
                && level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate) {
            plate.setOwner(player.getGameProfile().getName());
        }
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate
                && plate.canEdit(player.getGameProfile().getName())) {
            if (!level.isClientSide) {
                int mode = (plate.setting() + 1) % 3;
                plate.setSetting(mode);
                level.setBlock(pos, state.setValue(MODE, mode), 3);
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.BLOCKS, 0.1F, 0.9F);
                player.displayClientMessage(Component.translatable(
                        "message.thaumic_reborn.arcane_pressure_plate.mode_" + mode), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.CONSUME;
    }

    @Override public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && !state.getValue(POWERED)) checkPressed(level, pos, state);
    }
    @Override public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) checkPressed(level, pos, state);
    }

    private void checkPressed(Level level, BlockPos pos, BlockState state) {
        ArcanePressurePlateBlockEntity plate = level.getBlockEntity(pos)
                instanceof ArcanePressurePlateBlockEntity found ? found : null;
        int mode = plate == null ? state.getValue(MODE) : plate.setting();
        AABB box = TOUCH.move(pos);
        List<? extends Entity> entities = mode == 2
                ? level.getEntitiesOfClass(Player.class, box)
                : level.getEntitiesOfClass(Entity.class, box);
        boolean shouldPress = false;
        for (Entity entity : entities) {
            if (entity.isIgnoringBlockTriggers()) continue;
            if (entity instanceof Player player && mode != 0) {
                boolean known = plate != null && plate.isKnown(player.getGameProfile().getName());
                if (mode == 1 && known) continue;
                if (mode == 2 && !known) continue;
            }
            shouldPress = true;
            break;
        }
        boolean pressed = state.getValue(POWERED);
        if (shouldPress != pressed) {
            level.setBlock(pos, state.setValue(POWERED, shouldPress), 2);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS,
                    0.2F, shouldPress ? 0.6F : 0.5F);
        }
        if (shouldPress) level.scheduleTick(pos, this, 20);
    }

    @Override public boolean isSignalSource(BlockState state) { return true; }
    @Override public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP && state.getValue(POWERED) ? 15 : 0;
    }
    @Override public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos,
            Direction direction) { return state.getValue(POWERED) ? 15 : 0; }

    @Override public boolean canConnectRedstone(BlockState state, BlockGetter level,
            BlockPos pos, @Nullable Direction direction) { return true; }

    @Override public boolean canEntityDestroy(BlockState state, BlockGetter level,
            BlockPos pos, Entity entity) { return false; }

    @Override public void onBlockExploded(BlockState state, Level level, BlockPos pos,
            Explosion explosion) { }

    @Override public InteractionResult onWandRightClick(BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePressurePlateBlockEntity plate)
                || !plate.owner().equals(player.getGameProfile().getName())) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(this));
            level.levelEvent(2001, pos, Block.getId(state));
            level.removeBlock(pos, false);
        }
        player.swing(hand);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (state.getBlock() != replacement.getBlock() && state.getValue(POWERED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcanePressurePlateBlockEntity(pos, state);
    }
}
