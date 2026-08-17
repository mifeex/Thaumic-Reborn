package com.thaumcraftmodern.item;

import com.thaumcraftmodern.mirror.MirrorLink;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.entity.MagicMirrorBlockEntity;
import com.thaumcraftmodern.world.menu.HandMirrorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/** TC4 portable one-slot item sender linked to an ordinary magic mirror. */
public final class HandMirrorItem extends Item {
    private static final String PORTABLE_LINK_ID = "PortableLinkId";
    public HandMirrorItem(Properties properties) {
        super(properties);
    }

    public static @Nullable MirrorLink link(ItemStack stack) {
        return stack.hasTag() ? MirrorLink.load(stack.getTag()) : null;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)
                || !context.getLevel().getBlockState(context.getClickedPos())
                        .is(ModBlocks.MAGIC_MIRROR.get())
                || !(context.getLevel().getBlockEntity(context.getClickedPos())
                        instanceof MagicMirrorBlockEntity)) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        MirrorLink previous = link(stack);
        CompoundTag tag = stack.getOrCreateTag();
        UUID portableId = tag.hasUUID(PORTABLE_LINK_ID)
                ? tag.getUUID(PORTABLE_LINK_ID) : UUID.randomUUID();
        unregister(previous, portableId,
                previous == null ? null : previous.level(level.getServer()));
        MirrorLink.of(level, context.getClickedPos()).save(tag);
        tag.putUUID(PORTABLE_LINK_ID, portableId);
        if (level.getBlockEntity(context.getClickedPos())
                instanceof MagicMirrorBlockEntity mirror) {
            mirror.addPortableLink(portableId);
        }
        Player player = context.getPlayer();
        level.playSound(null, context.getClickedPos(), SoundEvents.BOTTLE_FILL,
                SoundSource.BLOCKS, 1.0F, 2.0F);
        if (player != null) {
            player.displayClientMessage(Component.translatable("tc.handmirrorlinked"), false);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
            InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        MirrorLink link = link(stack);
        ServerLevel remote = link == null ? null : link.level(serverPlayer.server);
        if (remote == null || !remote.hasChunkAt(link.position())
                || !(remote.getBlockEntity(link.position())
                        instanceof MagicMirrorBlockEntity)) {
            if (link != null) clearInvalidLink(stack, serverPlayer);
            return InteractionResultHolder.pass(stack);
        }
        registerPortableLink(stack, remote, link);
        int slot = hand == InteractionHand.OFF_HAND ? -1
                : serverPlayer.getInventory().selected;
        NetworkHooks.openScreen(serverPlayer,
                new SimpleMenuProvider(
                        (id, inventory, ignored) -> new HandMirrorMenu(
                                id, inventory, hand, slot),
                        Component.translatable("container.thaumic_reborn.hand_mirror")),
                buffer -> {
                    buffer.writeEnum(hand);
                    buffer.writeVarInt(slot);
                });
        return InteractionResultHolder.success(stack);
    }

    public static boolean transport(ItemStack mirror, ItemStack items,
            ServerPlayer player) {
        MirrorLink link = link(mirror);
        ServerLevel remote = link == null ? null : link.level(player.server);
        if (remote == null || !remote.hasChunkAt(link.position())
                || !(remote.getBlockEntity(link.position())
                        instanceof MagicMirrorBlockEntity target)) {
            clearInvalidLink(mirror, player);
            return false;
        }
        target.spawnItem(remote, items.copy());
        remote.blockEvent(link.position(), target.getBlockState().getBlock(), 1, 0);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1F, 1.0F);
        return true;
    }

    private static void clearInvalidLink(ItemStack mirror, ServerPlayer player) {
        MirrorLink old = link(mirror);
        UUID portableId = mirror.hasTag()
                && mirror.getTag().hasUUID(PORTABLE_LINK_ID)
                ? mirror.getTag().getUUID(PORTABLE_LINK_ID) : null;
        if (old != null && portableId != null) {
            ServerLevel remote = old.level(player.server);
            unregister(old, portableId, remote);
        }
        mirror.setTag(null);
        player.level().playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS, 0.3F, 1.6F);
        player.displayClientMessage(Component.translatable("tc.handmirrorerror"), false);
    }

    private static void registerPortableLink(ItemStack stack, ServerLevel remote,
            MirrorLink link) {
        CompoundTag tag = stack.getOrCreateTag();
        UUID id = tag.hasUUID(PORTABLE_LINK_ID)
                ? tag.getUUID(PORTABLE_LINK_ID) : UUID.randomUUID();
        tag.putUUID(PORTABLE_LINK_ID, id);
        if (remote.hasChunkAt(link.position())
                && remote.getBlockEntity(link.position())
                        instanceof MagicMirrorBlockEntity mirror) {
            mirror.addPortableLink(id);
        }
    }

    private static void unregister(@Nullable MirrorLink link, UUID id,
            @Nullable ServerLevel fallbackLevel) {
        if (link == null) return;
        ServerLevel remote = fallbackLevel;
        if (remote != null && remote.dimension().location().equals(link.dimension())
                && remote.hasChunkAt(link.position())
                && remote.getBlockEntity(link.position())
                        instanceof MagicMirrorBlockEntity mirror) {
            mirror.removePortableLink(id);
        }
    }

    @Override public boolean isFoil(ItemStack stack) { return link(stack) != null; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
            List<Component> tooltip, TooltipFlag flag) {
        MirrorLink link = link(stack);
        if (link != null) tooltip.add(Component.translatable("tc.handmirrorlinkedto")
                .append(" " + link.display()).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
