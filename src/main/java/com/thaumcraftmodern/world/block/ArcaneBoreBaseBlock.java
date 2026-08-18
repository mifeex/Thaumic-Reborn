package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBaseBlockEntity;
import com.thaumcraftmodern.wand.WandInteractable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** TC4 Arcane Bore Base: solid support, Perditio suction and item nozzle. */
public final class ArcaneBoreBaseBlock extends BaseEntityBlock implements WandInteractable {
    public ArcaneBoreBaseBlock(Properties properties) { super(properties); }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof ArcaneBoreBaseBlockEntity base) {
            base.setOutput(placer == null ? Direction.NORTH
                    : placer.getDirection().getOpposite());
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        return player.getItemInHand(hand).getItem() instanceof WandItem
                ? InteractionResult.PASS : InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult onWandRightClick(BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof ArcaneBoreBaseBlockEntity base) {
            base.setOutput(hit.getDirection());
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(),
                    SoundSource.BLOCKS, 0.3F, 1.9F + level.random.nextFloat() * 0.2F);
        }
        player.swing(hand);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcaneBoreBaseBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return level instanceof ServerLevel
                ? createTickerHelper(type, ModBlockEntities.ARCANE_BORE_BASE.get(),
                        ArcaneBoreBaseBlockEntity::serverTick)
                : null;
    }
}
