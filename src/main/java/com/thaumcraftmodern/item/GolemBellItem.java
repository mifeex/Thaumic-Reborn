package com.thaumcraftmodern.item;

import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemMarker;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.GolemBellSyncPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import com.thaumcraftmodern.ThaumcraftModern;

/** Golemancer's Bell selection and marker editing contract from TC4. */
public final class GolemBellItem extends Item {
    private static final String SELECTED = "SelectedGolem";
    private static final String SELECTED_ID = "SelectedGolemId";
    private static final String DIMENSION = "SelectedDimension";
    private static final String HOME = "GolemHome";
    private static final String HOME_FACE = "GolemHomeFace";
    private static final String MARKERS = "Markers";

    public GolemBellItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        if (!(target instanceof ClassicGolemEntity golem) || !canControl(player, golem)) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            selectGolem(stack, serverPlayer, hand, golem);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private static void selectGolem(ItemStack bell, ServerPlayer player,
            InteractionHand hand, ClassicGolemEntity golem) {
        link(bell, golem, player.level());
        synchronizeBell(player, hand, bell);
        player.displayClientMessage(Component.translatable(
                "message.thaumic_reborn.golem_bell.selected",
                golem.getDisplayName()).withStyle(ChatFormatting.DARK_PURPLE), true);
        playBell(player.level(), golem.blockPosition(), SoundSource.NEUTRAL);
    }

    private static boolean canControl(Player player, ClassicGolemEntity golem) {
        return golem.owner().isEmpty() || golem.owner().get().equals(player.getUUID());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack bell, Player player, net.minecraft.world.entity.Entity target) {
        if (target instanceof com.thaumcraftmodern.entity.TravelingTrunkEntity trunk) {
            if (trunk.upgrade() == com.thaumcraftmodern.entity.GolemUpgradeType.AQUA
                    && !trunk.canControl(player)) return false;
            if (!player.level().isClientSide) {
                boolean dismantling = player.isShiftKeyDown();
                boolean preserve = !dismantling
                        && trunk.upgrade() == com.thaumcraftmodern.entity.GolemUpgradeType.ORDO;
                if (!preserve) trunk.dropContents();
                if (dismantling && trunk.upgrade() != null && trunk.getRandom().nextBoolean()) {
                    trunk.spawnAtLocation(com.thaumcraftmodern.registry.ModItems
                            .golemUpgrade(trunk.upgrade()).get());
                }
                if (dismantling) trunk.setUpgrade(null);
                trunk.spawnAtLocation(trunk.createSpawner(preserve));
                player.level().playSound(null, trunk.blockPosition(), ModSounds.ZAP.get(),
                        SoundSource.NEUTRAL, .5F, 1F);
                trunk.discard();
            }
            return true;
        }
        if (!(target instanceof ClassicGolemEntity golem)
                || !golem.owner().map(player.getUUID()::equals).orElse(true)) return false;
        if (!player.level().isClientSide) {
            ItemStack placer = golem.placerItem();
            if (golem.hasCustomName()) placer.setHoverName(golem.getCustomName());
            boolean dismantling = player.isShiftKeyDown();
            if (dismantling) {
                if (golem.core() != null) golem.spawnAtLocation(com.thaumcraftmodern.registry.ModItems.golemCore(golem.core()).get());
                for (int slot = 0; slot < golem.upgradeSlots(); slot++) {
                    var upgrade = golem.upgrade(slot);
                    if (upgrade != null && golem.getRandom().nextBoolean()) {
                        net.minecraft.world.item.Item upgradeItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                                new net.minecraft.resources.ResourceLocation(ThaumcraftModern.MOD_ID,
                                        "golem_upgrade_" + upgrade.id()));
                        if (upgradeItem != null) golem.spawnAtLocation(upgradeItem);
                    }
                }
            } else {
                placer.getOrCreateTag().put("GolemData", golem.savePortableData());
            }
            ItemStack carried = golem.inventory().getItem(0);
            if (dismantling && !carried.isEmpty()) golem.spawnAtLocation(carried.copy());
            golem.spawnAtLocation(placer);
            player.level().playSound(null, golem.blockPosition(), ModSounds.ZAP.get(),
                    SoundSource.NEUTRAL, .5F, 1F);
            golem.discard();
        }
        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        // TC4 deliberately handles the bell before the clicked block.  Doing
        // this only in useOn means containers consume the interaction first,
        // so a chest opens and never becomes a golem marker.
        if (context.getLevel().isClientSide) return InteractionResult.PASS;
        if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        return editMarker(stack, player, context.getHand(), level,
                context.getClickedPos(), context.getClickedFace());
    }

    private static InteractionResult editMarker(
            ItemStack stack,
            ServerPlayer player,
            InteractionHand hand,
            ServerLevel level,
            BlockPos position,
            net.minecraft.core.Direction face
    ) {
        ClassicGolemEntity golem = linkedGolem(stack, level);
        if (golem == null) {
            clearLink(stack);
            synchronizeBell(player, hand, stack);
            player.displayClientMessage(Component.translatable(
                    "message.thaumic_reborn.golem_bell.unlinked"), true);
            return InteractionResult.FAIL;
        }
        byte result = golem.changeMarker(position, face, player.isShiftKeyDown());
        writeMarkers(stack, golem.markers());
        synchronizeBell(player, hand, stack);
        Component feedback = result == Byte.MIN_VALUE
                ? Component.translatable("message.thaumic_reborn.golem_bell.marker_removed")
                : result < 0
                        ? Component.translatable("message.thaumic_reborn.golem_bell.marker_added")
                        : Component.translatable(
                                "message.thaumic_reborn.golem_bell.marker_color", result);
        player.displayClientMessage(feedback.copy().withStyle(ChatFormatting.DARK_PURPLE), true);
        playBell(level, position, SoundSource.BLOCKS);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // onItemUseFirst is the authoritative path. This fallback preserves
        // marker editing for callers that invoke Item#useOn directly.
        return context.getLevel().isClientSide
                ? InteractionResult.SUCCESS
                : onItemUseFirst(context.getItemInHand(), context);
    }

    public static int selectedEntityId(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(SELECTED_ID) : -1;
    }

    public static UUID selectedUuid(ItemStack stack) {
        return stack.hasTag() && stack.getTag().hasUUID(SELECTED) ? stack.getTag().getUUID(SELECTED) : null;
    }

    public static BlockPos home(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(HOME) ? BlockPos.of(stack.getTag().getLong(HOME)) : null;
    }

    public static net.minecraft.core.Direction homeFace(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(HOME_FACE)
                ? net.minecraft.core.Direction.from3DDataValue(stack.getTag().getByte(HOME_FACE)) : null;
    }

    public static List<GolemMarker> markers(ItemStack stack) {
        List<GolemMarker> result = new ArrayList<>();
        if (!stack.hasTag()) return result;
        net.minecraft.nbt.ListTag tags = stack.getTag().getList(MARKERS, net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int index = 0; index < tags.size(); index++) result.add(GolemMarker.load(tags.getCompound(index)));
        return result;
    }

    private static void link(ItemStack stack, ClassicGolemEntity golem, net.minecraft.world.level.Level level) {
        clearLink(stack);
        var tag = stack.getOrCreateTag();
        tag.putUUID(SELECTED, golem.getUUID());
        tag.putInt(SELECTED_ID, golem.getId());
        tag.putString(DIMENSION, level.dimension().location().toString());
        tag.putLong(HOME, golem.homePos().asLong());
        tag.putByte(HOME_FACE, (byte) golem.homeFacing().get3DDataValue());
        writeMarkers(stack, golem.markers());
    }

    private static ClassicGolemEntity linkedGolem(ItemStack stack, ServerLevel level) {
        if (!stack.hasTag() || !stack.getTag().hasUUID(SELECTED)
                || !level.dimension().location().toString().equals(stack.getTag().getString(DIMENSION))) return null;
        var entity = level.getEntity(stack.getTag().getUUID(SELECTED));
        return entity instanceof ClassicGolemEntity golem && golem.isAlive() ? golem : null;
    }

    private static void writeMarkers(ItemStack stack, List<GolemMarker> markers) {
        net.minecraft.nbt.ListTag tags = new net.minecraft.nbt.ListTag();
        for (GolemMarker marker : markers) tags.add(marker.save());
        stack.getOrCreateTag().put(MARKERS, tags);
    }

    private static void clearLink(ItemStack stack) {
        if (!stack.hasTag()) return;
        var tag = stack.getTag();
        tag.remove(SELECTED); tag.remove(SELECTED_ID); tag.remove(DIMENSION);
        tag.remove(HOME); tag.remove(HOME_FACE); tag.remove(MARKERS);
    }

    private static void playBell(net.minecraft.world.level.Level level, BlockPos pos, SoundSource source) {
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, source,
                .7F, 1F + level.random.nextFloat() * .1F);
    }

    /**
     * A bell changes only stack NBT, not its count. Vanilla therefore may not
     * notice the held slot changed; explicitly diff and resend it so TC4's
     * client marker renderer sees the selected golem, block and face at once.
     */
    private static void synchronizeBell(
            ServerPlayer player,
            InteractionHand hand,
            ItemStack bell
    ) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.broadcastChanges();
        }
        ModNetwork.sendTo(player, new GolemBellSyncPacket(hand, bell));
    }

    /** TC4's F binding clears every marker and must not swap the bell to the offhand. */
    @Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
    public static final class SwapKeyHandler {
        private SwapKeyHandler() {}

        @SubscribeEvent
        public static void onSwap(net.minecraftforge.event.entity.living.LivingSwapItemsEvent.Hands event) {
            if (!(event.getEntity() instanceof Player player)) return;
            ItemStack bell = event.getItemSwappedToOffHand().getItem() instanceof GolemBellItem
                    ? event.getItemSwappedToOffHand()
                    : event.getItemSwappedToMainHand().getItem() instanceof GolemBellItem
                            ? event.getItemSwappedToMainHand() : ItemStack.EMPTY;
            if (bell.isEmpty() || !bell.hasTag() || !bell.getTag().hasUUID(SELECTED)) return;
            event.setCanceled(true);
            if (player.level() instanceof ServerLevel level
                    && level.getEntity(bell.getTag().getUUID(SELECTED)) instanceof ClassicGolemEntity golem) {
                golem.clearMarkers();
                writeMarkers(bell, golem.markers());
                if (player instanceof ServerPlayer serverPlayer) {
                    InteractionHand hand = player.getMainHandItem() == bell
                            ? InteractionHand.MAIN_HAND
                            : InteractionHand.OFF_HAND;
                    synchronizeBell(serverPlayer, hand, bell);
                }
                playBell(level, player.blockPosition(), SoundSource.PLAYERS);
                player.displayClientMessage(Component.translatable(
                        "message.thaumic_reborn.golem_bell.markers_cleared")
                        .withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }

        /**
         * Forge calls this before the clicked block.  Handling the bell here
         * on the logical server prevents chests, furnaces and other menus from
         * consuming the click before a marker can be changed.
         */
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (!(event.getLevel() instanceof ServerLevel level)
                    || !(event.getEntity() instanceof ServerPlayer player)
                    || !(event.getItemStack().getItem() instanceof GolemBellItem)
                    || event.getFace() == null) {
                return;
            }
            InteractionResult result = editMarker(
                    event.getItemStack(), player, event.getHand(), level,
                    event.getPos(), event.getFace());
            event.setCanceled(true);
            event.setCancellationResult(result);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
            selectFromEvent(event, event.getTarget());
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onEntityInteractSpecific(
                PlayerInteractEvent.EntityInteractSpecific event
        ) {
            selectFromEvent(event, event.getTarget());
        }

        /** A real player break invalidates every face/color marker on that block. */
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onMarkedBlockBroken(BlockEvent.BreakEvent event) {
            if (!(event.getLevel() instanceof ServerLevel level)) return;
            BlockPos broken = event.getPos();
            java.util.ArrayList<ClassicGolemEntity> changed = new java.util.ArrayList<>();
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (entity instanceof ClassicGolemEntity golem && golem.removeMarkersAt(broken)) {
                    changed.add(golem);
                }
            }
            if (changed.isEmpty()) return;
            for (ServerPlayer player : level.players()) {
                boolean inventoryChanged = false;
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
                    ItemStack bell = player.getInventory().getItem(slot);
                    if (!(bell.getItem() instanceof GolemBellItem)) continue;
                    UUID selected = selectedUuid(bell);
                    ClassicGolemEntity selectedGolem = changed.stream()
                            .filter(golem -> golem.getUUID().equals(selected)).findFirst().orElse(null);
                    if (selectedGolem == null) continue;
                    writeMarkers(bell, selectedGolem.markers());
                    inventoryChanged = true;
                    if (bell == player.getMainHandItem()) {
                        ModNetwork.sendTo(player, new GolemBellSyncPacket(InteractionHand.MAIN_HAND, bell));
                    }
                    if (bell == player.getOffhandItem()) {
                        ModNetwork.sendTo(player, new GolemBellSyncPacket(InteractionHand.OFF_HAND, bell));
                    }
                }
                if (inventoryChanged) {
                    player.getInventory().setChanged();
                    player.inventoryMenu.broadcastChanges();
                    if (player.containerMenu != player.inventoryMenu) player.containerMenu.broadcastChanges();
                }
            }
        }

        /** Server-authoritative selection before the golem's own interaction path. */
        private static void selectFromEvent(
                PlayerInteractEvent event,
                net.minecraft.world.entity.Entity target
        ) {
            if (!(event.getEntity() instanceof ServerPlayer player)
                    || !(event.getItemStack().getItem() instanceof GolemBellItem)
                    || !(target instanceof ClassicGolemEntity golem)
                    || !canControl(player, golem)) {
                return;
            }
            selectGolem(event.getItemStack(), player, event.getHand(), golem);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }
}
