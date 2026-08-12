package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.api.runic.RunicArmor;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.RunicShieldFxPacket;
import com.thaumcraftmodern.network.packet.RunicShieldSyncPacket;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative TC4 EventHandlerRunic port. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID)
public final class RunicShieldService {
    public static final String HARDENING_TAG = "RS.HARDEN";
    static final int RECHARGE_TICKS = 40;
    static final int RECHARGE_DELAY_TICKS = 80;
    static final int CHARGED_RING_REDUCTION_TICKS = 10;
    static final int RECHARGE_COST_CENTIVIS = 50;
    private static final Map<UUID, RuntimeState> RUNTIME = new HashMap<>();

    private RunicShieldService() { }

    public static int hardening(ItemStack stack) {
        return stack.hasTag() ? Math.max(0, stack.getTag().getByte(HARDENING_TAG)) : 0;
    }

    public static int finalCharge(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int base = stack.getItem() instanceof
                com.thaumicreborn.api.equipment.RunicArmor armor
                ? armor.baseRunicCharge(stack) : 0;
        return combinedCharge(base, hardening(stack));
    }

    static int combinedCharge(int base, int hardening) {
        return Math.max(0, base + hardening);
    }

    public static boolean isAugmentable(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof
                com.thaumicreborn.api.equipment.RunicArmor
                || stack.is(com.thaumcraftmodern.registry.ModTags.Items.RUNIC_AUGMENTABLE));
    }

    public static ItemStack addHardening(ItemStack stack) {
        ItemStack result = stack.copy();
        result.setCount(1);
        result.getOrCreateTag().putByte(HARDENING_TAG,
                (byte) Math.min(127, hardening(stack) + 1));
        return result;
    }

    /** Exact TC4 ClientProxy runic-charge tooltip line. */
    public static Component chargeTooltip(ItemStack stack) {
        return Component.translatable("item.runic.charge")
                .append(" +" + finalCharge(stack))
                .withStyle(net.minecraft.ChatFormatting.GOLD);
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) return;
        RuntimeState runtime = RUNTIME.computeIfAbsent(player.getUUID(), ignored -> new RuntimeState());
        Loadout loadout = loadout(player);
        int current = current(player);
        if (loadout.maximum == 0) {
            if (current != 0 || runtime.lastMaximum != 0) set(player, 0, 0);
            runtime.lastMaximum = 0;
            return;
        }
        if (current > loadout.maximum) current = loadout.maximum;
        if (runtime.delay > 0) runtime.delay--;
        if (current < loadout.maximum && runtime.delay == 0
                && player.tickCount >= runtime.nextRechargeTick
                && consumeRechargeVis(player)) {
            current++;
            runtime.nextRechargeTick = player.tickCount + Math.max(0,
                    RECHARGE_TICKS - loadout.charged * CHARGED_RING_REDUCTION_TICKS);
        }
        if (current != runtime.lastCharge || loadout.maximum != runtime.lastMaximum) {
            set(player, current, loadout.maximum);
            runtime.lastCharge = current;
            runtime.lastMaximum = loadout.maximum;
        }
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || environmental(event) || event.getAmount() <= 0) return;
        Loadout loadout = loadout(player);
        int charge = current(player);
        if (loadout.maximum <= 0 || charge <= 0) return;
        float damage = event.getAmount();
        event.setAmount(Math.max(0.0F, damage - charge));
        charge = Math.max(0, (int) (charge - damage));
        ModNetwork.sendToTracking(player, new RunicShieldFxPacket(
                player.getId(), event.getSource().getDirectEntity() == null
                        ? -1 : event.getSource().getDirectEntity().getId()));
        player.level().playSound(null, player.blockPosition(),
                ModSounds.RUNIC_SHIELD_EFFECT.get(), SoundSource.PLAYERS,
                0.66F, 1.0F + player.getRandom().nextFloat() * 0.1F);
        RuntimeState runtime = RUNTIME.computeIfAbsent(player.getUUID(), ignored -> new RuntimeState());
        if (charge <= 0) {
            long time = player.level().getGameTime();
            if (loadout.kinetic > 0 && time >= runtime.kineticReady) {
                runtime.kineticReady = time + 400;
                player.level().explode(player, player.getX(), player.getY() + player.getBbHeight() / 2,
                        player.getZ(), 1.5F + loadout.kinetic * 0.5F,
                        net.minecraft.world.level.Level.ExplosionInteraction.NONE);
            }
            if (loadout.healing > 0 && time >= runtime.healingReady) {
                runtime.healingReady = time + 400;
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                        240, loadout.healing));
            }
            if (loadout.emergency > 0 && time >= runtime.emergencyReady) {
                runtime.emergencyReady = time + 1200;
                charge = Math.min(loadout.maximum, 8 * loadout.emergency);
                player.level().playSound(null, player.blockPosition(),
                        ModSounds.RUNIC_SHIELD_CHARGE.get(), SoundSource.PLAYERS, 1, 1);
            }
            if (charge <= 0) runtime.delay = RECHARGE_DELAY_TICKS;
        }
        set(player, charge, loadout.maximum);
        runtime.lastCharge = charge;
    }

    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        RUNTIME.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent public static void tooltip(ItemTooltipEvent event) {
        int charge = finalCharge(event.getItemStack());
        if (charge > 0 && !(event.getItemStack().getItem() instanceof RunicAccessoryItem))
            event.getToolTip().add(chargeTooltip(event.getItemStack()));
    }

    private static boolean environmental(LivingHurtEvent event) {
        return event.getSource().is(DamageTypeTags.IS_FIRE)
                || event.getSource().is(DamageTypeTags.IS_DROWNING)
                || event.getSource().is(DamageTypeTags.IS_FREEZING)
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS);
    }

    private static int current(Player player) {
        return KnowledgeAccess.get(player).map(k -> k.runicCharge()).orElse(0);
    }

    private static void set(ServerPlayer player, int charge, int maximum) {
        int safe = Math.max(0, Math.min(charge, maximum));
        KnowledgeAccess.get(player).ifPresent(k -> k.setRunicCharge(safe));
        ModNetwork.sendTo(player, new RunicShieldSyncPacket(safe, Math.max(0, maximum)));
    }

    private static boolean consumeRechargeVis(ServerPlayer player) {
        Map<String, Integer> cost = Map.of("aer", RECHARGE_COST_CENTIVIS,
                "terra", RECHARGE_COST_CENTIVIS);
        var curios = CuriosApi.getCuriosInventory(player).resolve();
        if (curios.isPresent()) {
            var equipped = curios.get().getEquippedCurios();
            for (int slot = 0; slot < equipped.getSlots(); slot++) {
                ItemStack stack = equipped.getStackInSlot(slot);
                if (stack.getItem() instanceof VisStorageItem storage
                        && storage.visCentivis(stack,
                                com.thaumcraftmodern.aura.PrimalAspect.AER)
                                >= RECHARGE_COST_CENTIVIS
                        && storage.visCentivis(stack,
                                com.thaumcraftmodern.aura.PrimalAspect.TERRA)
                                >= RECHARGE_COST_CENTIVIS) {
                    storage.removeCentivis(stack,
                            com.thaumcraftmodern.aura.PrimalAspect.AER,
                            RECHARGE_COST_CENTIVIS);
                    storage.removeCentivis(stack,
                            com.thaumcraftmodern.aura.PrimalAspect.TERRA,
                            RECHARGE_COST_CENTIVIS);
                    return true;
                }
            }
        }
        for (int slot = player.getInventory().items.size() - 1; slot >= 0; slot--) {
            ItemStack stack = player.getInventory().items.get(slot);
            if (WandVisService.isWand(stack)
                    && WandVisService.consumeCentivis(player, stack, cost)) return true;
        }
        return false;
    }

    private static Loadout loadout(Player player) {
        Loadout result = new Loadout();
        for (ItemStack stack : player.getArmorSlots()) result.accept(stack);
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler -> {
            var curios = handler.getEquippedCurios();
            for (int slot = 0; slot < curios.getSlots(); slot++) result.accept(curios.getStackInSlot(slot));
        });
        return result;
    }

    static final class Loadout {
        int maximum, charged, kinetic, healing, emergency;
        void accept(ItemStack stack) {
            maximum += finalCharge(stack);
            if (stack.getItem() instanceof RunicAccessoryItem item) switch (item.upgrade()) {
                case CHARGED -> charged++;
                case KINETIC -> kinetic++;
                case HEALING -> healing++;
                case EMERGENCY -> emergency++;
                default -> { }
            }
        }
    }

    static final class RuntimeState {
        int delay, lastCharge = -1, lastMaximum = -1;
        long nextRechargeTick, kineticReady, healingReady, emergencyReady;
    }
}
