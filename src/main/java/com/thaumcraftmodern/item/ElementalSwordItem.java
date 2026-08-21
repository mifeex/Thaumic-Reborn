package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ElementalSwordItem extends SwordItem {
    private static final String DEFENDING = "tc4ZephyrDefending";
    public static final float DEFENSIVE_ATTACK_WEAR_MULTIPLIER = 1.3F;
    private static final float EXTRA_DEFENSIVE_ATTACK_WEAR_CHANCE =
            DEFENSIVE_ATTACK_WEAR_MULTIPLIER - 1.0F;
    private static final ThreadLocal<Boolean> SWEEPING = ThreadLocal.withInitial(() -> false);

    public ElementalSwordItem(Properties properties) {
        super(ElementalTier.INSTANCE, 4, -2.4F, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return isDefending(stack) ? UseAnim.BLOCK : UseAnim.NONE;
    }
    @Override public int getUseDuration(ItemStack stack) { return 72_000; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            setDefending(stack, !isDefending(stack));
            if (!isDefending(stack)) {
                player.stopUsingItem();
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
            }
        } else {
            setDefending(stack, false);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!isDefending(stack) || !(entity instanceof Player player)) return;
        if (!selected || player.getMainHandItem() != stack) {
            setDefending(stack, false);
            if (player.getUseItem() == stack) player.stopUsingItem();
            return;
        }
        if (!player.isUsingItem()) player.startUsingItem(InteractionHand.MAIN_HAND);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (!(living instanceof Player player)) return;
        if (isDefending(stack)) {
            renderDefensiveStance(level, player, remaining);
            return;
        }
        int elapsed = getUseDuration(stack) - remaining;
        Vec3 movement = player.getDeltaMovement();
        double y = movement.y;
        if (y < 0.0D) {
            y /= 1.2D;
            player.fallDistance /= 1.2F;
        }
        y += 0.08D;
        if (y > 0.5D) y = 0.2D;
        player.setDeltaMovement(movement.x, y, movement.z);
        AABB range = player.getBoundingBox().inflate(2.5D);
        for (Entity target : level.getEntities(player, range,
                entity -> entity.isAlive() && !(entity instanceof Player) && entity != player.getVehicle())) {
            Vec3 delta = target.position().subtract(player.position());
            double distance = delta.length() + 0.1D;
            target.setDeltaMovement(target.getDeltaMovement().add(delta.scale(1.0D / 2.5D / distance)));
            target.hurtMarked = true;
        }
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + player.getBbHeight() / 2.0D,
                    player.getZ(), 5, 1.0D, player.getBbHeight() / 2.0D, 1.0D, 0.03D);
            if (elapsed == 0 || elapsed % 20 == 0) {
                server.playSound(null, player.blockPosition(), ModSounds.WIND.get(), SoundSource.PLAYERS,
                        0.5F, 0.9F + server.random.nextFloat() * 0.2F);
            }
        }
        if (elapsed % 20 == 0) {
            stack.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(player.getUsedItemHand()));
        }
    }

    public static boolean isDefending(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(DEFENDING);
    }

    public static void setDefending(ItemStack stack, boolean defending) {
        if (defending) {
            stack.getOrCreateTag().putBoolean(DEFENDING, true);
        } else if (stack.hasTag()) {
            stack.getTag().remove(DEFENDING);
        }
    }

    public static boolean toggleDefending(ItemStack stack) {
        boolean defending = !isDefending(stack);
        setDefending(stack, defending);
        return defending;
    }

    private void renderDefensiveStance(Level level, Player player, int remaining) {
        int elapsed = getUseDuration(player.getUseItem()) - remaining;
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.CLOUD, player.getX(),
                    player.getY() + player.getBbHeight() * 0.5D, player.getZ(),
                    4, 0.65D, player.getBbHeight() * 0.35D, 0.65D, 0.015D);
            if (elapsed == 0 || elapsed % 20 == 0) {
                server.playSound(null, player.blockPosition(), ModSounds.WIND.get(),
                        SoundSource.PLAYERS, 0.35F,
                        1.1F + server.random.nextFloat() * 0.1F);
            }
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean defending = isDefending(stack);
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (defending && !attacker.level().isClientSide && !stack.isEmpty()
                && attacker.getRandom().nextFloat() < EXTRA_DEFENSIVE_ATTACK_WEAR_CHANCE) {
            stack.hurtAndBreak(1, attacker,
                    broken -> broken.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        if (!(attacker instanceof Player player) || attacker.level().isClientSide || SWEEPING.get()) return result;
        int hits = 0;
        SWEEPING.set(true);
        try {
            AABB range = target.getBoundingBox().inflate(1.2D, 1.1D, 1.2D);
            for (LivingEntity candidate : attacker.level().getEntitiesOfClass(LivingEntity.class, range,
                    entity -> entity != target && entity != attacker && entity.isAlive())) {
                if (candidate instanceof TamableAnimal tameable && tameable.isOwnedBy(player)) continue;
                if (candidate.isAlliedTo(player)) continue;
                player.attack(candidate);
                hits++;
            }
        } finally {
            SWEEPING.set(false);
        }
        if (hits > 0 && attacker.level() instanceof ServerLevel server) {
            server.playSound(null, target.blockPosition(), ModSounds.SWING.get(), SoundSource.PLAYERS,
                    1.0F, 0.9F + server.random.nextFloat() * 0.2F);
        }
        return result;
    }
}
