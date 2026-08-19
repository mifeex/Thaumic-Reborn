package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.block.ArcaneDoorBlock;
import com.thaumcraftmodern.world.block.ArcanePressurePlateBlock;
import com.thaumcraftmodern.world.block.entity.ArcaneDoorBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcanePressurePlateBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** TC4 iron/gold access key for both arcane doors and arcane pressure plates. */
public final class ArcaneDoorKeyItem extends Item {
    private static final byte DOOR = 0;
    private static final byte PRESSURE_PLATE = 1;
    private final boolean gold;

    public ArcaneDoorKeyItem(boolean gold, Properties properties) {
        super(properties);
        this.gold = gold;
    }

    @Override public boolean isFoil(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("location");
    }

    @Override public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        var state = level.getBlockState(clicked);
        BlockPos base = clicked;
        byte type;
        ArcaneDoorBlockEntity door = null;
        ArcanePressurePlateBlockEntity plate = null;
        if (state.getBlock() instanceof ArcaneDoorBlock) {
            type = DOOR;
            base = state.getValue(ArcaneDoorBlock.HALF) == DoubleBlockHalf.UPPER
                    ? clicked.below() : clicked;
            if (!(level.getBlockEntity(base) instanceof ArcaneDoorBlockEntity found))
                return InteractionResult.PASS;
            door = found;
        } else if (state.getBlock() instanceof ArcanePressurePlateBlock) {
            type = PRESSURE_PLATE;
            if (!(level.getBlockEntity(base) instanceof ArcanePressurePlateBlockEntity found))
                return InteractionResult.PASS;
            plate = found;
        } else return InteractionResult.PASS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        String name = player.getGameProfile().getName();
        String location = base.getX() + "," + base.getY() + "," + base.getZ();
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains("location")) {
            if (!level.isClientSide && canLink(name, door, plate)) {
                ItemStack linked = new ItemStack(this);
                linked.getOrCreateTag().putString("location", location);
                linked.getOrCreateTag().putByte("type", type);
                if (!player.getInventory().add(linked)) player.drop(linked, false);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                message(player, type == DOOR ? "key_linked" : "key_plate_linked");
                sound(level, base, 0.9F);
                player.swing(context.getHand());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        boolean same = location.equals(tag.getString("location"))
                && tag.getByte("type") == type;
        boolean owner = name.equals(door != null ? door.owner() : plate.owner());
        boolean already = door != null ? door.hasAccess(name, gold) : plate.hasAccess(name, gold);
        if (same && !owner && !already) {
            if (!level.isClientSide) {
                if (door != null) {
                    door.grant(name, gold);
                    if (level.getBlockEntity(base.above()) instanceof ArcaneDoorBlockEntity upper)
                        upper.grant(name, gold);
                } else plate.grant(name, gold);
                if (!player.getAbilities().instabuild) stack.shrink(1);
                message(player, type == DOOR ? "key_granted"
                        : gold ? "key_plate_granted_gold" : "key_plate_granted_iron");
                sound(level, base, 1.1F);
                player.swing(context.getHand());
            }
        } else if (!level.isClientSide) {
            message(player, same ? "key_already" : "key_wrong");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override public InteractionResult useOn(UseOnContext context) {
        return onItemUseFirst(context.getItemInHand(), context);
    }

    private boolean canLink(String name, @Nullable ArcaneDoorBlockEntity door,
            @Nullable ArcanePressurePlateBlockEntity plate) {
        if (door != null) return name.equals(door.owner()) || !gold && door.canMintIron(name);
        return name.equals(plate.owner()) || !gold && plate.canEdit(name);
    }

    private static void sound(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos, ModSounds.KEY.get(), SoundSource.PLAYERS, 1.0F, pitch);
    }

    private static void message(Player player, String suffix) {
        player.displayClientMessage(Component.translatable(
                "message.thaumic_reborn." + suffix).withStyle(ChatFormatting.DARK_PURPLE,
                ChatFormatting.ITALIC), false);
    }

    @Override public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("location")) return;
        tooltip.add(Component.translatable("message.thaumic_reborn.key_target")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable(tag.getByte("type") == PRESSURE_PLATE
                        ? "message.thaumic_reborn.key_target_plate"
                        : "message.thaumic_reborn.key_target_door")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        String[] parts = tag.getString("location").split(",");
        String location = parts.length == 3
                ? "x " + parts[0] + ", z " + parts[2] + ", y " + parts[1]
                : tag.getString("location");
        tooltip.add(Component.literal(location)
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
