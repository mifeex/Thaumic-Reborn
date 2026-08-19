package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.wand.WandInteractable;
import com.thaumcraftmodern.world.block.entity.WardedGlassBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** TC4 warded glass: owner-bound, transparent and removable with its owner's wand. */
public final class WardedGlassBlock extends GlassBlock
        implements EntityBlock, WandInteractable {
    public WardedGlassBlock(Properties properties) { super(properties); }

    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player
                && level.getBlockEntity(pos) instanceof WardedGlassBlockEntity glass)
            glass.setOwner(player.getGameProfile().getName());
    }

    @Override public boolean canEntityDestroy(BlockState state, BlockGetter level,
            BlockPos pos, Entity entity) { return false; }

    @Override public void onBlockExploded(BlockState state, Level level, BlockPos pos,
            Explosion explosion) { }

    @Override public InteractionResult onWandRightClick(BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof WardedGlassBlockEntity glass)
                || !glass.owner().equals(player.getGameProfile().getName()))
            return InteractionResult.CONSUME;
        if (!level.isClientSide) {
            popResource(level, pos, new ItemStack(this));
            level.levelEvent(2001, pos, Block.getId(state));
            level.removeBlock(pos, false);
        }
        player.swing(hand);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WardedGlassBlockEntity(pos, state);
    }
}
