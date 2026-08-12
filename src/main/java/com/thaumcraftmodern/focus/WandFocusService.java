package com.thaumcraftmodern.focus;

import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.world.block.entity.TemporaryHoleBlockEntity;
import com.thaumcraftmodern.world.block.entity.WardedBlockEntity;
import com.thaumcraftmodern.wand.WandVisService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Server authority for equipping and casting unupgraded TC4 foci. */
public final class WandFocusService {
    public static final String FOCUS_KEY = "focus";
    private static final String TRADE_PICK_KEY = "focus_trade_pick";
    private static final double RANGE = 20.0D;
    private static final ConcurrentHashMap<UUID, ExcavationProgress> EXCAVATION =
            new ConcurrentHashMap<>();

    private WandFocusService() {}

    public static Optional<ItemStack> focusStack(ItemStack wand) {
        CompoundTag tag = wand.getTag();
        if (tag == null || !tag.contains(FOCUS_KEY, Tag.TAG_COMPOUND)) return Optional.empty();
        ItemStack focus = ItemStack.of(tag.getCompound(FOCUS_KEY));
        return focus.getItem() instanceof WandFocusItem ? Optional.of(focus) : Optional.empty();
    }

    public static Optional<WandFocusType> type(ItemStack wand) {
        return focusStack(wand).map(stack -> ((WandFocusItem) stack.getItem()).type());
    }

    public static boolean setFocus(ItemStack wand, ItemStack focus) {
        if (!(wand.getItem() instanceof WandItem item) || !item.form().acceptsFocus()
                || !(focus.getItem() instanceof WandFocusItem)) return false;
        wand.getOrCreateTag().put(FOCUS_KEY, focus.copyWithCount(1).save(new CompoundTag()));
        return true;
    }

    public static void clearFocus(ItemStack wand) {
        CompoundTag tag = wand.getTag();
        if (tag != null) tag.remove(FOCUS_KEY);
    }

    public static void changeFocus(ServerPlayer player, String requestedId) {
        ItemStack wand = player.getMainHandItem();
        if (!(wand.getItem() instanceof WandItem item) || !item.form().acceptsFocus()) return;
        ItemStack previous = focusStack(wand).orElse(ItemStack.EMPTY);
        if (requestedId.equals("remove")) {
            if (!previous.isEmpty()) giveOrDrop(player, previous);
            clearFocus(wand);
            player.getInventory().setChanged();
            playEquip(player, 0.9F);
            return;
        }
        int selectedSlot = -1;
        for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
            ItemStack candidate = player.getInventory().items.get(slot);
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(candidate.getItem());
            if (candidate.getItem() instanceof WandFocusItem && key != null
                    && key.getPath().equals(requestedId)) { selectedSlot = slot; break; }
        }
        if (selectedSlot < 0) return;
        ItemStack selected = player.getInventory().items.get(selectedSlot).split(1);
        if (!previous.isEmpty() && !player.getInventory().add(previous.copy())) {
            player.drop(previous.copy(), false);
        }
        setFocus(wand, selected);
        player.getInventory().setChanged();
        playEquip(player, 1.0F);
    }

    public static InteractionResult cast(ItemStack wand, Level level, ServerPlayer player,
                                         InteractionHand hand, BlockHitResult blockHit) {
        WandFocusType type = type(wand).orElse(null);
        if (type == null) return InteractionResult.PASS;
        if (type == WandFocusType.TRADE) return blockHit != null
                && blockHit.getType() == HitResult.Type.BLOCK
                ? trade(wand, player, blockHit, true) : InteractionResult.CONSUME;
        if (type == WandFocusType.SHOCK
                && upgradeLevel(wand, FocusUpgradeType.EARTH_SHOCK) > 0) {
            if (!coolingDown(player, type) && castEarthShock(wand, player)) setCooldown(player, type);
            return InteractionResult.CONSUME;
        }
        if (type.continuous()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (coolingDown(player, type)) return InteractionResult.CONSUME;
        boolean cast = switch (type) {
            case FROST -> castFrost(wand, player);
            case PRIMAL -> castPrimal(wand, player);
            case HELLBAT -> castHellbat(wand, player);
            case PORTABLE_HOLE -> castPortableHole(wand, player);
            case WARDING -> castWarding(wand, player);
            default -> false;
        };
        if (cast) setCooldown(player, type);
        return InteractionResult.CONSUME;
    }

    public static void tick(ItemStack wand, Level level, LivingEntity living) {
        if (!(living instanceof ServerPlayer player)) return;
        WandFocusType type = type(wand).orElse(null);
        if (type == null || !type.continuous()) return;
        if (type == WandFocusType.SHOCK && player.tickCount % 5 != 0) return;
        boolean active = switch (type) {
            case FIRE -> castFire(wand, player);
            case SHOCK -> castShock(wand, player);
            case EXCAVATION -> castExcavation(wand, player);
            default -> false;
        };
        if (!active) player.stopUsingItem();
    }

    public static void stopped(LivingEntity living) {
        EXCAVATION.remove(living.getUUID());
    }

    public static InteractionResult tradeLeftClick(ServerPlayer player, BlockPos position) {
        ItemStack wand = player.getMainHandItem();
        if (type(wand).orElse(null) != WandFocusType.TRADE) return InteractionResult.PASS;
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(position), Direction.UP, position, false);
        return trade(wand, player, hit, false);
    }

    private static boolean castFire(ItemStack wand, ServerPlayer player) {
        if (!pay(player, wand, WandFocusType.FIRE.centivisCost())) return false;
        ServerLevel level = player.serverLevel();
        int potency = upgradeLevel(wand, FocusUpgradeType.POTENCY);
        int count = upgradeLevel(wand, FocusUpgradeType.FIREBALL) > 0 ? 1 : 2;
        for (int index = 0; index < count; index++) {
            var ember = new com.thaumcraftmodern.entity.FocusEmberEntity(
                    ModEntities.FOCUS_EMBER.get(), level);
            ember.setOwner(player);
            ember.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            ember.configure(2.0F + potency, 3 + potency
                    + upgradeLevel(wand, FocusUpgradeType.ALCHEMISTS_FIRE) * 2);
            ember.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, upgradeLevel(wand, FocusUpgradeType.FIRE_BEAM) > 0 ? 2.0F : 1.0F,
                    upgradeLevel(wand, FocusUpgradeType.FIRE_BEAM) > 0 ? 0.0F : 15.0F);
            level.addFreshEntity(ember);
        }
        if (player.tickCount % 10 == 0) level.playSound(null, player.blockPosition(),
                SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.33F, 2.0F);
        return true;
    }

    private static boolean castShock(ItemStack wand, ServerPlayer player) {
        java.util.Map<String, Integer> shockCost = upgradeLevel(wand, FocusUpgradeType.CHAIN_LIGHTNING) > 0
                ? java.util.Map.of("aer", 40, "aqua", 10) : WandFocusType.SHOCK.centivisCost();
        if (!pay(player, wand, shockCost)) return false;
        ServerLevel level = player.serverLevel();
        Vec3 eyes = player.getEyePosition();
        Vec3 end = eyes.add(player.getLookAngle().scale(RANGE));
        LivingEntity target = nearestLiving(player, RANGE, 1.0D).orElse(null);
        if (target != null) {
            end = target.getBoundingBox().getCenter();
            float damage = 4.0F + upgradeLevel(wand, FocusUpgradeType.POTENCY) * 1.5F;
            target.hurt(player.damageSources().playerAttack(player), damage);
            if (upgradeLevel(wand, FocusUpgradeType.CHAIN_LIGHTNING) > 0) {
                LivingEntity previous = target;
                int chains = upgradeLevel(wand, FocusUpgradeType.CHAIN_LIGHTNING) * 2
                        + upgradeLevel(wand, FocusUpgradeType.ENLARGE) * 2;
                for (int chain = 0; chain < chains; chain++) {
                    LivingEntity from = previous;
                    LivingEntity next = level.getEntitiesOfClass(LivingEntity.class,
                            from.getBoundingBox().inflate(5.0D), entity -> entity != player
                                    && entity != from && entity.isAlive()).stream()
                            .min(java.util.Comparator.comparingDouble(from::distanceToSqr)).orElse(null);
                    if (next == null) break;
                    next.hurt(player.damageSources().playerAttack(player), damage * 0.65F);
                    previous = next;
                }
            }
        }
        Vector3f color = new Vector3f(1.0F, 1.0F, 0.49F);
        for (int step = 1; step <= 18; step++) {
            Vec3 point = eyes.lerp(end, step / 18.0D);
            level.sendParticles(new DustParticleOptions(color, 0.7F), point.x, point.y, point.z,
                    1, 0.025D, 0.025D, 0.025D, 0.0D);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 0.25F, 1.4F);
        return true;
    }

    private static boolean castFrost(ItemStack wand, ServerPlayer player) {
        if (!pay(player, wand, WandFocusType.FROST.centivisCost())) return false;
        var shard = new com.thaumcraftmodern.entity.FrostShardEntity(
                ModEntities.FROST_SHARD.get(), player.serverLevel());
        shard.setOwner(player);
        shard.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        boolean boulder = upgradeLevel(wand, FocusUpgradeType.ICE_BOULDER) > 0;
        shard.configure((boulder ? 8.0F : 3.0F) + upgradeLevel(wand, FocusUpgradeType.POTENCY),
                boulder ? 0 : 3 + upgradeLevel(wand, FocusUpgradeType.ALCHEMISTS_FROST));
        shard.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                boulder ? 0.85F : 1.5F, 1.0F);
        player.serverLevel().addFreshEntity(shard);
        if (upgradeLevel(wand, FocusUpgradeType.SCATTERSHOT) > 0) {
            for (float offset : new float[]{-8.0F, 8.0F}) {
                var extra = new com.thaumcraftmodern.entity.FrostShardEntity(
                        ModEntities.FROST_SHARD.get(), player.serverLevel());
                extra.setOwner(player); extra.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
                extra.configure(2.0F + upgradeLevel(wand, FocusUpgradeType.POTENCY), 2);
                extra.shootFromRotation(player, player.getXRot(), player.getYRot() + offset, 0.0F, 1.35F, 0.5F);
                player.serverLevel().addFreshEntity(extra);
            }
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK,
                SoundSource.PLAYERS, 0.4F, 1.1F);
        return true;
    }

    private static boolean castPrimal(ItemStack wand, ServerPlayer player) {
        Random random = new Random(System.currentTimeMillis() / 200L);
        java.util.LinkedHashMap<String, Integer> cost = new java.util.LinkedHashMap<>();
        for (String aspect : List.of("aqua", "aer", "terra", "ignis", "ordo", "perditio"))
            cost.put(aspect, 50 + random.nextInt(5) * 50);
        if (!pay(player, wand, cost)) return false;
        var orb = new com.thaumcraftmodern.entity.PrimalOrbEntity(
                ModEntities.PRIMAL_ORB.get(), player.serverLevel());
        orb.setOwner(player);
        orb.setSeeker(upgradeLevel(wand, FocusUpgradeType.SEEKER) > 0);
        orb.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
        orb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.55F, 0.0F);
        player.serverLevel().addFreshEntity(orb);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.PLAYERS, 0.3F, 0.85F);
        return true;
    }

    private static boolean castExcavation(ItemStack wand, ServerPlayer player) {
        BlockHitResult hit = rayBlock(player, 10.0D);
        if (hit.getType() != HitResult.Type.BLOCK) { EXCAVATION.remove(player.getUUID()); return true; }
        BlockPos pos = hit.getBlockPos();
        BlockState state = player.level().getBlockState(pos);
        float hardness = state.getDestroySpeed(player.level(), pos);
        if (state.isAir() || hardness < 0.0F || !player.mayInteract(player.level(), pos)) {
            EXCAVATION.remove(player.getUUID()); return true;
        }
        if (!pay(player, wand, WandFocusType.EXCAVATION.centivisCost())) return false;
        ExcavationProgress previous = EXCAVATION.get(player.getUUID());
        float progress = previous != null && previous.position.equals(pos) ? previous.progress : 0.0F;
        progress += excavationSpeed(state) * (1.0F
                + upgradeLevel(wand, FocusUpgradeType.POTENCY) * 0.35F);
        int stage = Math.min(9, (int) (progress / Math.max(0.01F, hardness) * 9.0F));
        player.serverLevel().destroyBlockProgress(player.getId(), pos, stage);
        if (progress >= hardness) {
            breakWithFocus(player, wand, pos, state);
            int enlarge = upgradeLevel(wand, FocusUpgradeType.ENLARGE);
            for (Direction direction : Direction.values()) {
                if (enlarge <= 0) break;
                BlockPos neighbour = pos.relative(direction);
                BlockState neighbourState = player.level().getBlockState(neighbour);
                if (neighbourState.getBlock() == state.getBlock()
                        && swappable(player, neighbour, neighbourState)
                        && pay(player, wand, WandFocusType.EXCAVATION.centivisCost())) {
                    breakWithFocus(player, wand, neighbour, neighbourState);
                    enlarge--;
                }
            }
            player.serverLevel().destroyBlockProgress(player.getId(), pos, -1);
            EXCAVATION.remove(player.getUUID());
        } else EXCAVATION.put(player.getUUID(), new ExcavationProgress(pos, progress));
        if (player.tickCount % 24 == 0) player.serverLevel().playSound(null, pos,
                SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.3F, 1.0F);
        if (upgradeLevel(wand, FocusUpgradeType.DOWSING) > 0 && player.tickCount % 20 == 0)
            dowsingPulse(player, pos);
        return true;
    }

    private static float excavationSpeed(BlockState state) {
        var material = state.getSoundType();
        return material == net.minecraft.world.level.block.SoundType.STONE
                || material == net.minecraft.world.level.block.SoundType.METAL
                || material == net.minecraft.world.level.block.SoundType.GLASS
                ? 0.25F : 0.05F;
    }

    private static InteractionResult trade(ItemStack wand, ServerPlayer player,
                                           BlockHitResult hit, boolean area) {
        BlockPos origin = hit.getBlockPos();
        BlockState source = player.level().getBlockState(origin);
        if (player.isShiftKeyDown()) {
            if (!source.isAir() && !source.hasBlockEntity()) {
                ItemStack picked = source.getBlock().asItem().getDefaultInstance();
                if (!picked.isEmpty()) wand.getOrCreateTag().put(TRADE_PICK_KEY,
                        picked.save(new CompoundTag()));
            }
            return InteractionResult.CONSUME;
        }
        CompoundTag tag = wand.getTag();
        if (tag == null || !tag.contains(TRADE_PICK_KEY, Tag.TAG_COMPOUND)) return InteractionResult.CONSUME;
        ItemStack picked = ItemStack.of(tag.getCompound(TRADE_PICK_KEY));
        if (!(picked.getItem() instanceof BlockItem blockItem)) return InteractionResult.CONSUME;
        List<BlockPos> positions = area ? exposedConnected(player, origin, source, hit.getDirection(),
                3 + upgradeLevel(wand, FocusUpgradeType.ENLARGE))
                : List.of(origin);
        for (BlockPos pos : positions) {
            int slot = findMatching(player.getInventory(), picked);
            if (!player.getAbilities().instabuild && slot < 0) break;
            BlockState old = player.level().getBlockState(pos);
            if (!swappable(player, pos, old)) continue;
            if (!pay(player, wand, WandFocusType.TRADE.centivisCost())) break;
            List<ItemStack> drops = Block.getDrops(old, player.serverLevel(), pos, null, player,
                    lootTool(wand));
            if (!player.getAbilities().instabuild) player.getInventory().items.get(slot).shrink(1);
            player.level().setBlock(pos, blockItem.getBlock().defaultBlockState(), 3);
            drops.forEach(drop -> giveOrDrop(player, drop));
            player.serverLevel().levelEvent(2001, pos, Block.getId(old));
        }
        return InteractionResult.CONSUME;
    }

    private static List<BlockPos> exposedConnected(ServerPlayer player, BlockPos origin,
                                                    BlockState source, Direction face, int radius) {
        List<BlockPos> result = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> checked = new HashSet<>();
        queue.add(origin);
        int cap = (radius * 2 + 1) * (radius * 2 + 1);
        while (!queue.isEmpty() && result.size() < cap) {
            BlockPos pos = queue.removeFirst();
            if (!checked.add(pos) || coordinateDistance(origin, pos, face) > radius) continue;
            BlockState state = player.level().getBlockState(pos);
            if (state.getBlock() != source.getBlock() || state.hasBlockEntity()
                    || !swappable(player, pos, state) || !isExposed(player.level(), pos)) continue;
            result.add(pos.immutable());
            for (Direction direction : Direction.values())
                if (direction != face && direction != face.getOpposite()) queue.add(pos.relative(direction));
        }
        return result;
    }

    private static int coordinateDistance(BlockPos a, BlockPos b, Direction face) {
        return switch (face.getAxis()) {
            case X -> Math.max(Math.abs(a.getY() - b.getY()), Math.abs(a.getZ() - b.getZ()));
            case Y -> Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getZ() - b.getZ()));
            case Z -> Math.max(Math.abs(a.getX() - b.getX()), Math.abs(a.getY() - b.getY()));
        };
    }

    private static boolean isExposed(Level level, BlockPos pos) {
        for (Direction direction : Direction.values())
            if (!level.getBlockState(pos.relative(direction)).isSolidRender(level, pos.relative(direction))) return true;
        return false;
    }

    private static boolean swappable(ServerPlayer player, BlockPos pos, BlockState state) {
        return !state.isAir() && !state.hasBlockEntity() && state.getDestroySpeed(player.level(), pos) >= 0.0F
                && player.mayInteract(player.level(), pos);
    }

    private static boolean castHellbat(ItemStack wand, ServerPlayer player) {
        LivingEntity target = nearestLiving(player, 32.0D, 1.5D).orElse(null);
        if (target == null || target == player
                || target instanceof net.minecraft.world.entity.player.Player
                && !player.server.isPvpAllowed()) return false;
        java.util.Map<String, Integer> batCost = upgradeLevel(wand, FocusUpgradeType.BAT_BOMBS) > 0
                ? java.util.Map.of("ignis", 100, "perditio", 200, "aer", 100)
                : upgradeLevel(wand, FocusUpgradeType.DEVIL_BATS) > 0
                ? java.util.Map.of("ignis", 100, "perditio", 100, "aer", 100, "terra", 100)
                : WandFocusType.HELLBAT.centivisCost();
        if (!pay(player, wand, batCost)) return false;
        LegacyThaumcraftMob bat = ModEntities.FIREBAT.get().create(player.serverLevel());
        if (bat == null) return false;
        bat.setPos(player.getX(), player.getEyeY(), player.getZ());
        bat.setTarget(target);
        int potency = upgradeLevel(wand, FocusUpgradeType.POTENCY);
        bat.configureFocusBat(player.getUUID(),
                upgradeLevel(wand, FocusUpgradeType.DEVIL_BATS) > 0,
                upgradeLevel(wand, FocusUpgradeType.BAT_BOMBS) > 0,
                upgradeLevel(wand, FocusUpgradeType.VAMPIRE_BATS) > 0, potency);
        player.serverLevel().addFreshEntity(bat);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BAT_TAKEOFF,
                SoundSource.PLAYERS, 0.6F, 0.8F);
        return true;
    }

    private static boolean castEarthShock(ItemStack wand, ServerPlayer player) {
        if (!pay(player, wand, java.util.Map.of("aer", 75, "terra", 25))) return false;
        BlockHitResult hit = rayBlock(player, 20.0D);
        Vec3 center = hit.getType() == HitResult.Type.BLOCK
                ? Vec3.atCenterOf(hit.getBlockPos()) : player.position().add(player.getLookAngle().scale(6.0D));
        double radius = 2.5D + upgradeLevel(wand, FocusUpgradeType.ENLARGE) * 2.0D;
        float damage = 5.0F + upgradeLevel(wand, FocusUpgradeType.POTENCY) * 1.33F;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(center, center).inflate(radius), entity -> entity != player && entity.isAlive())) {
            target.hurt(player.damageSources().playerAttack(player), damage);
            Vec3 push = target.position().subtract(center).normalize().scale(0.8D);
            target.push(push.x, 0.35D, push.z);
        }
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                center.x, center.y, center.z, 40, radius * .4D, .3D, radius * .4D, .05D);
        player.serverLevel().playSound(null, BlockPos.containing(center), SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS, 0.6F, 0.8F);
        return true;
    }

    private static boolean castPortableHole(ItemStack wand, ServerPlayer player) {
        BlockHitResult hit = rayBlock(player, 32.0D);
        if (hit.getType() != HitResult.Type.BLOCK) {
            playPortableHoleFailure(player, player.blockPosition());
            return false;
        }
        Direction direction = hit.getDirection().getOpposite();
        int maximum = 33 + upgradeLevel(wand, FocusUpgradeType.ENLARGE) * 8;
        int duration = 120 + upgradeLevel(wand, FocusUpgradeType.EXTEND) * 60;
        int depth = 0;
        for (; depth < maximum; depth++) {
            BlockPos center = hit.getBlockPos().relative(direction, depth);
            if (!canOpenPortableHole(player, center)) break;
        }
        if (depth == 0) {
            playPortableHoleFailure(player, hit.getBlockPos());
            return false;
        }
        java.util.Map<String, Integer> total = java.util.Map.of(
                "perditio", 10 * depth,
                "aer", 10 * depth
        );
        if (!pay(player, wand, total)) {
            playPortableHoleFailure(player, hit.getBlockPos());
            return false;
        }
        if (!TemporaryHoleBlockEntity.createTunnelCell(
                player.serverLevel(),
                hit.getBlockPos(),
                duration,
                direction,
                depth,
                player.getUUID()
        )) {
            playPortableHoleFailure(player, hit.getBlockPos());
            return false;
        }
        player.serverLevel().playSound(null, hit.getBlockPos(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static boolean canOpenPortableHole(
            ServerPlayer player,
            BlockPos position
    ) {
        return TemporaryHoleBlockEntity.canCreateTunnelCell(
                player.level(),
                position,
                player
        );
    }

    private static void playPortableHoleFailure(
            ServerPlayer player,
            BlockPos position
    ) {
        player.serverLevel().playSound(
                null,
                position,
                com.thaumcraftmodern.registry.ModSounds.WAND_FAIL.get(),
                SoundSource.PLAYERS,
                0.2F,
                1.0F
        );
    }

    private static boolean castWarding(ItemStack wand, ServerPlayer player) {
        BlockHitResult hit = rayBlock(player, 20.0D);
        if (hit.getType() != HitResult.Type.BLOCK) return false;
        BlockPos origin = hit.getBlockPos();
        if (player.level().getBlockEntity(origin) instanceof WardedBlockEntity ward) {
            if (!ward.ownedBy(player.getUUID())) return false;
            int unwardRadius = upgradeLevel(wand, FocusUpgradeType.ARCHITECT) > 0
                    ? upgradeLevel(wand, FocusUpgradeType.ENLARGE) : 0;
            for (int first = -unwardRadius; first <= unwardRadius; first++)
                for (int second = -unwardRadius; second <= unwardRadius; second++) {
                    BlockPos pos = planeOffset(origin, hit.getDirection(), first, second);
                    if (player.level().getBlockEntity(pos) instanceof WardedBlockEntity found
                            && found.ownedBy(player.getUUID())) found.restore();
                }
            return true;
        }
        int radius = upgradeLevel(wand, FocusUpgradeType.ARCHITECT) > 0
                ? upgradeLevel(wand, FocusUpgradeType.ENLARGE) : 0;
        Direction face = hit.getDirection();
        int changed = 0;
        for (int first = -radius; first <= radius; first++) {
            for (int second = -radius; second <= radius; second++) {
                BlockPos pos = planeOffset(origin, face, first, second);
                BlockState old = player.level().getBlockState(pos);
                if (old.isAir() || old.hasBlockEntity() || old.getDestroySpeed(player.level(), pos) < 0.0F
                        || !player.mayInteract(player.level(), pos)) continue;
                if (!pay(player, wand, WandFocusType.WARDING.centivisCost())) return changed > 0;
                player.level().setBlock(pos, ModBlocks.WARDED_BLOCK.get().defaultBlockState(), 3);
                if (player.level().getBlockEntity(pos) instanceof WardedBlockEntity created)
                    created.configure(old, player.getUUID());
                changed++;
            }
        }
        return changed > 0;
    }

    private static BlockPos planeOffset(BlockPos origin, Direction face, int first, int second) {
        return switch (face.getAxis()) {
            case X -> origin.offset(0, first, second);
            case Y -> origin.offset(first, 0, second);
            case Z -> origin.offset(first, second, 0);
        };
    }

    private static void breakWithFocus(ServerPlayer player, ItemStack wand,
                                       BlockPos pos, BlockState state) {
        Block.getDrops(state, player.serverLevel(), pos, null, player, lootTool(wand))
                .forEach(drop -> giveOrDrop(player, drop));
        player.level().setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        player.serverLevel().levelEvent(2001, pos, Block.getId(state));
    }

    private static ItemStack lootTool(ItemStack wand) {
        ItemStack tool = wand.copy();
        if (upgradeLevel(wand, FocusUpgradeType.SILK_TOUCH) > 0)
            tool.enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, 1);
        else {
            int fortune = upgradeLevel(wand, FocusUpgradeType.TREASURE);
            if (fortune > 0) tool.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE,
                    fortune);
        }
        return tool;
    }

    private static void dowsingPulse(ServerPlayer player, BlockPos center) {
        for (BlockPos cursor : BlockPos.betweenClosed(center.offset(-4, -4, -4),
                center.offset(4, 4, 4))) {
            if (!player.level().getBlockState(cursor).is(net.minecraftforge.common.Tags.Blocks.ORES)) continue;
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    cursor.getX() + .5D, cursor.getY() + .5D, cursor.getZ() + .5D,
                    1, .15D, .15D, .15D, 0.0D);
        }
    }

    private static Optional<LivingEntity> nearestLiving(ServerPlayer player, double range,
                                                         double aimRadius) {
        Vec3 eyes = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eyes.add(look.scale(range));
        AABB search = player.getBoundingBox().expandTowards(look.scale(range)).inflate(aimRadius);
        EntityHitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(
                player.level(), player, eyes, end, search,
                entity -> entity instanceof LivingEntity && entity.isPickable(), (float) (range * range));
        return hit != null && hit.getEntity() instanceof LivingEntity living
                ? Optional.of(living) : Optional.empty();
    }

    private static BlockHitResult rayBlock(ServerPlayer player, double range) {
        Vec3 eyes = player.getEyePosition();
        return player.level().clip(new ClipContext(eyes, eyes.add(player.getLookAngle().scale(range)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private static boolean pay(ServerPlayer player, ItemStack wand, java.util.Map<String, Integer> cost) {
        int frugal = upgradeLevel(wand, FocusUpgradeType.FRUGAL);
        java.util.LinkedHashMap<String, Integer> discounted = new java.util.LinkedHashMap<>();
        cost.forEach((aspect, amount) -> discounted.put(aspect,
                Math.max(1, (int) Math.ceil(amount * Math.max(0.1D, 1.0D - frugal * 0.1D)))));
        return player.getAbilities().instabuild || WandVisService.consumeCentivis(player, wand, discounted);
    }

    private static int upgradeLevel(ItemStack wand, FocusUpgradeType upgrade) {
        return focusStack(wand).map(stack -> WandFocusItem.upgradeLevel(stack, upgrade)).orElse(0);
    }

    private static int findMatching(Inventory inventory, ItemStack picked) {
        for (int slot = 0; slot < inventory.items.size(); slot++)
            if (ItemStack.isSameItemSameTags(inventory.items.get(slot), picked)) return slot;
        return -1;
    }

    private static boolean coolingDown(ServerPlayer player, WandFocusType type) {
        return player.getCooldowns().isOnCooldown(focusStack(player.getMainHandItem())
                .map(ItemStack::getItem).orElse(net.minecraft.world.item.Items.AIR));
    }

    private static void setCooldown(ServerPlayer player, WandFocusType type) {
        focusStack(player.getMainHandItem()).ifPresent(stack ->
                player.getCooldowns().addCooldown(stack.getItem(),
                        type == WandFocusType.SHOCK
                                ? WandFocusItem.upgradeLevel(stack, FocusUpgradeType.EARTH_SHOCK) > 0 ? 20
                                : WandFocusItem.upgradeLevel(stack, FocusUpgradeType.CHAIN_LIGHTNING) > 0 ? 10 : 5
                                : type.cooldownTicks()));
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }

    private static void playEquip(ServerPlayer player, float pitch) {
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL,
                SoundSource.PLAYERS, 0.25F, pitch);
    }

    private record ExcavationProgress(BlockPos position, float progress) {}
}
