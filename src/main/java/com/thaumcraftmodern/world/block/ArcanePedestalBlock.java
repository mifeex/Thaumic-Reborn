package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ArcanePedestalBlock extends BaseEntityBlock {
    /** Selection follows every visible section of the pedestal model. */
    private static final VoxelShape OUTLINE = Shapes.or(
            box(0, 0, 0, 16, 4, 16),
            box(4, 4, 4, 12, 12, 12),
            box(2, 12, 2, 14, 16, 14)
    );

    public ArcanePedestalBlock(Properties properties) {
        super(properties);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) { return OUTLINE; }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level,
            BlockPos pos, CollisionContext context) { return Shapes.block(); }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack stored = pedestal.item();
        if (!stored.isEmpty()) {
            ItemStack removed = pedestal.removeItemNoUpdate(0);
            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
            playPickup(level, pos, 1.5F);
            return InteractionResult.CONSUME;
        }
        ItemStack held = player.getItemInHand(hand);
        return placeHeldItem(level, pos, player, pedestal, held);
    }

    /**
     * Shared fallback for placeable items whose own useOn path can otherwise
     * win while the player is using secondary interaction.
     */
    public static InteractionResult placeHeldItem(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand
    ) {
        if (!(level.getBlockEntity(pos)
                instanceof ArcanePedestalBlockEntity pedestal)
                || !pedestal.isEmpty()) {
            return InteractionResult.PASS;
        }
        return placeHeldItem(
                level, pos, player, pedestal,
                player.getItemInHand(hand));
    }

    private static InteractionResult placeHeldItem(
            Level level,
            BlockPos pos,
            Player player,
            ArcanePedestalBlockEntity pedestal,
            ItemStack held
    ) {
        if (held.isEmpty()) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        ItemStack placed = held.copy();
        placed.setCount(1);
        pedestal.setItem(0, placed);
        if (!player.getAbilities().instabuild) held.shrink(1);
        playPickup(level, pos, 1.6F);
        return InteractionResult.CONSUME;
    }

    private static void playPickup(Level level, BlockPos pos, float pitchScale) {
        float pitch = ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F)
                * pitchScale;
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, pitch);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock())
                && level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal) {
            Containers.dropContents(level, pos, pedestal);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override public boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ArcanePedestalBlockEntity pedestal
                && !pedestal.isEmpty() ? 15 : 0;
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcanePedestalBlockEntity(pos, state);
    }
}
