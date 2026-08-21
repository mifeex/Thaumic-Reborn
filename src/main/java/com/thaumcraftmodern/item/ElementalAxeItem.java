package com.thaumcraftmodern.item;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class ElementalAxeItem extends AxeItem {
    private static final String LUMBERING = "tc4ElementalLumbering";
    static final String ATTRACTED_TO = "tc4ElementalAxeOwner";
    static final String ATTRACTED_UNTIL = "tc4ElementalAxeUntil";
    static final int CHOPPED_DROP_ATTRACTION_TICKS = 200;

    public ElementalAxeItem(Properties properties) {
        super(ElementalTier.INSTANCE, 5.0F, -3.0F, properties);
    }

    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }
    @Override public int getUseDuration(ItemStack stack) { return 72_000; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (!(living instanceof Player player)) return;
        AABB range = player.getBoundingBox().inflate(10.0D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, range, entity -> entity.isAlive())) {
            Vec3 delta = item.position().subtract(player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D));
            double distance = delta.length();
            if (distance < 1.0E-6D) continue;
            Vec3 motion = item.getDeltaMovement().subtract(delta.scale(0.3D / distance));
            item.setDeltaMovement(
                    Mth.clamp(motion.x, -0.35D, 0.35D),
                    Mth.clamp(motion.y, -0.35D, 0.35D),
                    Mth.clamp(motion.z, -0.35D, 0.35D));
            if (level instanceof ServerLevel server && server.random.nextBoolean()) {
                server.sendParticles(ParticleTypes.BUBBLE, item.getX(), item.getY(), item.getZ(),
                        1, 0.125D, 0.125D, 0.125D, 0.0D);
            }
        }
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos position, Player player) {
        if (player.isShiftKeyDown() || stack.getOrCreateTag().getBoolean(LUMBERING)
                || !player.level().getBlockState(position).is(BlockTags.LOGS)) {
            return super.onBlockStartBreak(stack, position, player);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) return true;
        ServerLevel serverLevel = serverPlayer.serverLevel();
        BlockPos furthest = findFurthestLog(serverLevel, position);
        stack.getOrCreateTag().putBoolean(LUMBERING, true);
        boolean destroyed;
        try {
            destroyed = serverPlayer.gameMode.destroyBlock(furthest);
        } finally {
            stack.getOrCreateTag().remove(LUMBERING);
        }
        if (destroyed) {
            serverLevel.sendParticles(ParticleTypes.BUBBLE, furthest.getX() + 0.5D,
                    furthest.getY() + 0.5D, furthest.getZ() + 0.5D, 8, 0.3D, 0.3D, 0.3D, 0.02D);
            serverPlayer.level().playSound(null, position, ModSounds.BUBBLE.get(),
                    SoundSource.PLAYERS, 0.15F, 1.0F);
            pullFreshDrops(serverLevel, furthest, serverPlayer);
        }
        return true;
    }

    static BlockPos findFurthestLog(ServerLevel level, BlockPos origin) {
        BlockState originState = level.getBlockState(origin);
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        pending.add(origin.immutable());
        visited.add(origin.immutable());
        BlockPos furthest = origin;
        double furthestDistance = 0.0D;
        while (!pending.isEmpty() && visited.size() <= 4096) {
            BlockPos current = pending.removeFirst();
            double distance = current.distSqr(origin);
            if (distance > furthestDistance || distance == furthestDistance && current.getY() > furthest.getY()) {
                furthest = current;
                furthestDistance = distance;
            }
            for (int x = -2; x <= 2; x++) for (int y = -2; y <= 2; y++) for (int z = -2; z <= 2; z++) {
                if (x == 0 && y == 0 && z == 0) continue;
                BlockPos next = current.offset(x, y, z);
                if (Math.abs(next.getX() - origin.getX()) > 24 || Math.abs(next.getY() - origin.getY()) > 48
                        || Math.abs(next.getZ() - origin.getZ()) > 24 || visited.contains(next)) continue;
                BlockState state = level.getBlockState(next);
                if (state.getBlock() == originState.getBlock() && state.is(BlockTags.LOGS)) {
                    BlockPos immutable = next.immutable();
                    visited.add(immutable);
                    pending.addLast(immutable);
                }
            }
        }
        return furthest;
    }

    private static void pullFreshDrops(ServerLevel level, BlockPos position, ServerPlayer player) {
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                new AABB(position).inflate(2.5D), entity -> entity.tickCount < 10)) {
            item.getPersistentData().putUUID(ATTRACTED_TO, player.getUUID());
            item.getPersistentData().putLong(
                    ATTRACTED_UNTIL,
                    level.getGameTime() + CHOPPED_DROP_ATTRACTION_TICKS
            );
            item.setPickUpDelay(0);
            pullDrop(item, player);
        }
    }

    static void pullDrop(ItemEntity item, Player player) {
        Vec3 target = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);
        Vec3 delta = target.subtract(item.position());
        double distance = delta.length();
        if (distance < 1.0E-6D) return;
        Vec3 motion = item.getDeltaMovement().scale(0.65D)
                .add(delta.scale(0.3D / distance));
        item.setDeltaMovement(
                Mth.clamp(motion.x, -0.35D, 0.35D),
                Mth.clamp(motion.y, -0.35D, 0.35D),
                Mth.clamp(motion.z, -0.35D, 0.35D)
        );
        item.setNoGravity(distance > 1.25D);
        item.hurtMarked = true;
    }
}
