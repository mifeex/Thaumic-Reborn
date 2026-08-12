package com.thaumcraftmodern.item;

import com.thaumcraftmodern.api.enchantment.ThaumcraftRepairable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class PrimalCrusherItem extends DiggerItem
        implements ThaumcraftRepairable {
    private static final String BREAK_FACE = "tc4BreakFace";
    private static final String AREA_BREAKING = "tc4AreaBreaking";

    public PrimalCrusherItem(Properties properties) {
        super(
                3.5F,
                -2.8F,
                PrimalCrusherTier.INSTANCE,
                PrimalCrusherTier.INSTANCE.getTag(),
                properties
        );
    }

    @Override
    public boolean onBlockStartBreak(
            ItemStack stack,
            BlockPos position,
            net.minecraft.world.entity.player.Player player
    ) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.getBoolean(AREA_BREAKING)) {
            BlockHitResult hit = getPlayerPOVHitResult(
                    player.level(),
                    player,
                    ClipContext.Fluid.NONE
            );
            tag.putByte(
                    BREAK_FACE,
                    (byte) hit.getDirection().get3DDataValue()
            );
        }
        return super.onBlockStartBreak(stack, position, player);
    }

    @Override
    public boolean mineBlock(
            ItemStack stack,
            Level level,
            BlockState state,
            BlockPos position,
            LivingEntity miner
    ) {
        boolean result = super.mineBlock(stack, level, state, position, miner);
        CompoundTag tag = stack.getOrCreateTag();
        if (miner.isShiftKeyDown()
                || tag.getBoolean(AREA_BREAKING)
                || !(miner instanceof ServerPlayer player)
                || !isEffective(state)) {
            return result;
        }

        Direction face = Direction.from3DDataValue(tag.getByte(BREAK_FACE));
        tag.putBoolean(AREA_BREAKING, true);
        try {
            for (int first = -1; first <= 1; first++) {
                for (int second = -1; second <= 1; second++) {
                    BlockPos target = position.offset(
                            planeOffset(first, second, face)
                    );
                    if (target.equals(position)
                            || !level.mayInteract(player, target)
                            || !player.mayUseItemAt(target, face, stack)) {
                        continue;
                    }
                    BlockState targetState = level.getBlockState(target);
                    if (targetState.getDestroySpeed(level, target) >= 0.0F
                            && isEffective(targetState)) {
                        player.gameMode.destroyBlock(target);
                    }
                }
            }
        } finally {
            tag.remove(AREA_BREAKING);
        }
        return result;
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slot,
            boolean selected
    ) {
        super.inventoryTick(stack, level, entity, slot, selected);
        VoidItemMechanics.repairOnePerSecond(stack, level, entity);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return VoidItemMechanics.isPrimalCharm(repair)
                || super.isValidRepairItem(stack, repair);
    }

    private boolean isEffective(BlockState state) {
        return state.is(PrimalCrusherTier.INSTANCE.getTag());
    }

    static BlockPos planeOffset(int first, int second, Direction face) {
        return switch (face.getAxis()) {
            case Y -> new BlockPos(first, 0, second);
            case Z -> new BlockPos(first, second, 0);
            case X -> new BlockPos(0, second, first);
        };
    }
}
