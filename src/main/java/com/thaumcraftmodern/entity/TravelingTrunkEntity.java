package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.item.GolemUpgradeItem;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.world.menu.TravelingTrunkMenu;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** TC4 traveling trunk: owned mobile storage with one elemental upgrade. */
public final class TravelingTrunkEntity extends PathfinderMob implements MenuProvider {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(
            TravelingTrunkEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> STAY = SynchedEntityData.defineId(
            TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> UPGRADE = SynchedEntityData.defineId(
            TravelingTrunkEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> OPEN = SynchedEntityData.defineId(
            TravelingTrunkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ANGER = SynchedEntityData.defineId(
            TravelingTrunkEntity.class, EntityDataSerializers.INT);

    private final SimpleContainer inventory = new SimpleContainer(36);
    public float lidAngle;
    public float previousLidAngle;
    public float squish;
    public float previousSquish;
    private int healTimer;
    private int jumpDelay;
    private boolean previousOnGround;

    public TravelingTrunkEntity(EntityType<? extends TravelingTrunkEntity> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setMaxUpStep(1F);
        jumpDelay = random.nextInt(20) + 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 75D)
                .add(Attributes.MOVEMENT_SPEED, .5D).add(Attributes.ATTACK_DAMAGE, 4D)
                .add(Attributes.FOLLOW_RANGE, 32D);
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER, Optional.empty());
        entityData.define(STAY, false);
        entityData.define(UPGRADE, -1);
        entityData.define(OPEN, false);
        entityData.define(ANGER, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, .6D, true) {
            @Override public boolean canUse() { return upgrade() == GolemUpgradeType.IGNIS && super.canUse(); }
            @Override public boolean canContinueToUse() { return upgrade() == GolemUpgradeType.IGNIS && super.canContinueToUse(); }
        });
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override public void aiStep() {
        super.aiStep();
        previousLidAngle = lidAngle;
        previousSquish += (squish - previousSquish) * .5F;
        if (level().isClientSide) {
            if (!onGround() && getDeltaMovement().y < 0D) lidAngle += .015F;
            if ((onGround() || isInWater()) && !isOpen()) lidAngle = Math.max(0F, lidAngle - .1F);
            if (isOpen()) lidAngle += .035F;
            lidAngle = Math.min(isOpen() ? .5F : .2F, lidAngle);
        }
        if (onGround() && !previousOnGround) squish = -.5F;
        else if (!onGround() && previousOnGround) squish = 1F;
        else if (onGround() && getDeltaMovement().horizontalDistanceSqr() > .0025D
                && jumpDelay-- <= 0) {
            squish = .35F;
            jumpDelay = random.nextInt(10) + 5;
        }
        squish *= .6F;
        previousOnGround = onGround();
        if (!level().isClientSide) serverTick();
    }

    private void serverTick() {
        if (anger() > 0) entityData.set(ANGER, anger() - 1);
        if (++healTimer >= (upgrade() == GolemUpgradeType.AQUA ? 1 : 50)) {
            healTimer = 0;
            if (getHealth() < getMaxHealth()) heal(1F);
        }
        Player owner = ownerPlayer();
        if (!isStaying() && upgrade() == GolemUpgradeType.IGNIS && owner != null
                && anger() == 0 && getTarget() == null) {
            LivingEntity attacker = owner.getLastHurtByMob();
            if (attacker == null) attacker = owner.getLastHurtMob();
            if (attacker != null && attacker.isAlive() && attacker != this) {
                setTarget(attacker);
                entityData.set(ANGER, 600);
            }
        } else if (getTarget() != null) setTarget(null);

        if (upgrade() == GolemUpgradeType.PERDITIO) {
            for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class,
                    getBoundingBox().inflate(3D), ItemEntity::isAlive)) {
                double dx = item.getX() - getX();
                double dy = item.getY() - getY() + getBbHeight() * .8D;
                double dz = item.getZ() - getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 0D) item.setDeltaMovement(item.getDeltaMovement().subtract(
                        dx / distance * .075D, dy / distance * .075D, dz / distance * .075D));
                if (!item.getBoundingBox().intersects(getBoundingBox().inflate(.5D))) continue;
                int before = item.getItem().getCount();
                ItemStack remainder = inventory.addItem(item.getItem());
                if (remainder.isEmpty()) item.discard(); else item.setItem(remainder);
                if (remainder.getCount() != before) {
                    level().playSound(null, blockPosition(), SoundEvents.GENERIC_EAT,
                            SoundSource.NEUTRAL, .5F, random.nextFloat() * .5F + .5F);
                    level().broadcastEntityEvent(this, (byte) 17);
                }
            }
        }
        if (owner == null || isStaying() || isOpen()) return;
        double distance = distanceToSqr(owner);
        if (distance > 400D) {
            tryTeleportToOwner(owner);
        } else if (distance > 16D) {
            getNavigation().moveTo(owner, upgrade() == GolemUpgradeType.AER ? .65D : .5D);
        } else getNavigation().stop();
    }

    @Nullable private Player ownerPlayer() {
        return owner().map(id -> level().getPlayerByUUID(id)).orElse(null);
    }

    private boolean tryTeleportToOwner(Player owner) {
        BlockPos center = owner.blockPosition();
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (dx >= -1 && dx <= 1 && dz >= -1 && dz <= 1) continue;
            BlockPos candidate = center.offset(dx, 0, dz);
            BlockPos floor = candidate.below();
            if (!level().getBlockState(floor).isFaceSturdy(level(), floor, Direction.UP)
                    || !level().getBlockState(candidate).getCollisionShape(level(), candidate).isEmpty()
                    || !level().getBlockState(candidate.above()).getCollisionShape(level(), candidate.above()).isEmpty()) {
                continue;
            }
            getNavigation().stop();
            moveTo(candidate.getX() + .5D, candidate.getY(), candidate.getZ() + .5D,
                    getYRot(), getXRot());
            setTarget(null);
            level().playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.NEUTRAL, .5F, 1F);
            return true;
        }
        return false;
    }

    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof com.thaumcraftmodern.item.GolemBellItem) return InteractionResult.PASS;
        boolean owner = canControl(player);
        if (held.getItem() instanceof GolemUpgradeItem upgradeItem && upgrade() == null) {
            if (!level().isClientSide) {
                setUpgrade(upgradeItem.type());
                if (!player.getAbilities().instabuild) held.shrink(1);
                level().playSound(null, blockPosition(), ModSounds.UPGRADE.get(),
                        SoundSource.NEUTRAL, .5F, 1F);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (held.getFoodProperties(player) != null && getHealth() < getMaxHealth()) {
            if (!level().isClientSide) {
                heal(held.getFoodProperties(player).getNutrition());
                if (!player.getAbilities().instabuild) held.shrink(1);
                level().playSound(null, blockPosition(), getHealth() >= getMaxHealth()
                                ? SoundEvents.PLAYER_BURP : SoundEvents.GENERIC_EAT,
                        SoundSource.NEUTRAL, .5F, .9F + random.nextFloat() * .2F);
                level().broadcastEntityEvent(this, (byte) 18);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (upgrade() == GolemUpgradeType.AQUA && !owner) return InteractionResult.FAIL;
        if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, this, buffer -> buffer.writeVarInt(getId()));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) return false;
        if (upgrade() == GolemUpgradeType.AQUA) return false;
        boolean hurt = super.hurt(source, source.is(DamageTypeTags.IS_FIRE) ? amount * .1F : amount);
        if (hurt) entityData.set(ANGER, 600);
        return hurt;
    }

    @Override public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result) {
            entityData.set(ANGER, 600);
            level().broadcastEntityEvent(this, (byte) 17);
            playSound(SoundEvents.BLAZE_HURT, .5F, random.nextFloat() * .1F + .9F);
        }
        return result;
    }

    @Override public void handleEntityEvent(byte id) {
        if (id == 17 || id == 18) {
            lidAngle = .15F;
            if (id == 18) for (int i = 0; i < 7; i++) {
                level().addParticle(net.minecraft.core.particles.ParticleTypes.HEART,
                        getRandomX(1D), getY() + .5D + random.nextFloat() * getBbHeight(),
                        getRandomZ(1D), random.nextGaussian() * .02D,
                        random.nextGaussian() * .02D, random.nextGaussian() * .02D);
            }
        } else super.handleEntityEvent(id);
    }

    public Optional<UUID> owner() { return entityData.get(OWNER); }
    public void setOwner(@Nullable UUID owner) { entityData.set(OWNER, Optional.ofNullable(owner)); }
    public boolean canControl(Player player) { return owner().isEmpty() || owner().get().equals(player.getUUID()); }
    public boolean isStaying() { return entityData.get(STAY); }
    public void setStaying(boolean stay) { entityData.set(STAY, stay); if (stay) getNavigation().stop(); }
    public boolean isOpen() { return entityData.get(OPEN); }
    public void setOpen(boolean open) {
        boolean previous = isOpen();
        entityData.set(OPEN, open);
        if (!level().isClientSide && previous != open) {
            level().playSound(
                    null,
                    blockPosition(),
                    open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE,
                    SoundSource.NEUTRAL,
                    .5F,
                    .9F + random.nextFloat() * .1F
            );
        }
    }
    public int anger() { return entityData.get(ANGER); }
    @Nullable public GolemUpgradeType upgrade() { return GolemUpgradeType.byLegacyId(entityData.get(UPGRADE)); }
    public void setUpgrade(@Nullable GolemUpgradeType upgrade) {
        entityData.set(UPGRADE, upgrade == null ? -1 : upgrade.legacyId());
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(SoundEvents.WOOD_STEP, .18F,
                .85F + random.nextFloat() * .15F);
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WOOD_HIT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOOD_BREAK;
    }

    @Override
    protected float getSoundVolume() {
        return .7F;
    }
    public int rows() { return upgrade() == GolemUpgradeType.TERRA ? 4 : 3; }
    public SimpleContainer inventory() { return inventory; }

    public ItemStack createSpawner(boolean preserveInventory) {
        ItemStack result = ModItems.TRAVELING_TRUNK.get().getDefaultInstance();
        CompoundTag data = savePortableData(preserveInventory);
        result.getOrCreateTag().put("TrunkData", data);
        if (hasCustomName()) result.setHoverName(getCustomName());
        return result;
    }

    public CompoundTag savePortableData(boolean preserveInventory) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Upgrade", entityData.get(UPGRADE));
        tag.putBoolean("Stay", isStaying());
        tag.putFloat("Health", getHealth());
        if (preserveInventory) saveInventory(tag);
        return tag;
    }

    public void loadPortableData(CompoundTag tag) {
        setUpgrade(GolemUpgradeType.byLegacyId(tag.contains("Upgrade") ? tag.getInt("Upgrade") : -1));
        setStaying(tag.getBoolean("Stay"));
        if (tag.contains("Health")) setHealth(Math.max(1F, Math.min(getMaxHealth(), tag.getFloat("Health"))));
        loadInventory(tag);
    }

    public void dropContents() {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.removeItemNoUpdate(slot);
            if (!stack.isEmpty()) spawnAtLocation(stack);
        }
    }

    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        dropContents();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        owner().ifPresent(id -> tag.putUUID("Owner", id));
        tag.putBoolean("Stay", isStaying());
        tag.putInt("Upgrade", entityData.get(UPGRADE));
        saveInventory(tag);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOwner(tag.hasUUID("Owner") ? tag.getUUID("Owner") : null);
        setStaying(tag.getBoolean("Stay"));
        setUpgrade(GolemUpgradeType.byLegacyId(tag.contains("Upgrade") ? tag.getInt("Upgrade") : -1));
        loadInventory(tag);
    }

    private void saveInventory(CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
        for (int i = 0; i < 36; i++) items.set(i, inventory.getItem(i));
        ContainerHelper.saveAllItems(tag, items);
    }

    private void loadInventory(CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
        for (int i = 0; i < 36; i++) inventory.setItem(i, items.get(i));
    }

    @Override public Component getDisplayName() { return getName(); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TravelingTrunkMenu(id, inventory, this);
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    /** TC4 trunks follow their owner through dimensions unless the stay button is active. */
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = com.thaumcraftmodern.ThaumcraftModern.MOD_ID)
    public static final class DimensionFollower {
        private DimensionFollower() {}

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onOwnerChangedDimension(
                net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
            if (!(event.getEntity() instanceof ServerPlayer owner) || owner.getServer() == null) return;
            ServerLevel destination = owner.getServer().getLevel(event.getTo());
            if (destination == null) return;
            ArrayList<TravelingTrunkEntity> followers = new ArrayList<>();
            for (ServerLevel level : owner.getServer().getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof TravelingTrunkEntity trunk && !trunk.isStaying()
                            && trunk.owner().map(owner.getUUID()::equals).orElse(false)) followers.add(trunk);
                }
            }
            for (TravelingTrunkEntity trunk : followers) {
                if (trunk.level() == destination) continue;
                Entity moved = trunk.changeDimension(destination);
                if (moved instanceof TravelingTrunkEntity movedTrunk) {
                    movedTrunk.moveTo(owner.getX(), owner.getY(), owner.getZ(),
                            movedTrunk.getYRot(), movedTrunk.getXRot());
                }
            }
        }
    }
}
