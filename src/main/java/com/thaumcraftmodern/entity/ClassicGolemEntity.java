package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.item.GolemCoreItem;
import com.thaumcraftmodern.item.GolemUpgradeItem;
import com.thaumcraftmodern.item.WardedJarItem;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkHooks;
import com.thaumcraftmodern.world.menu.GolemMenu;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

/** Server-authoritative TC4 golem body with synchronized core, cargo, upgrades and configuration. */
public class ClassicGolemEntity extends AbstractGolem implements MenuProvider {
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> INACTIVE = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> CORE = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> UPGRADES = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> TOGGLES = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> CARRYING = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ItemStack> CARRIED_DISPLAY = SynchedEntityData.defineId(
            ClassicGolemEntity.class, EntityDataSerializers.ITEM_STACK);
    private final GolemMaterial material;
    private byte[] upgrades;
    private SimpleContainer inventory;
    private SimpleContainer filters;
    private byte[] filterColors = new byte[]{-1};
    private int regenerationTimer;
    private float bootup = -1F;
    private int actionTimer;
    private int leftArmTimer;
    private int rightArmTimer;
    private boolean menuPaused;
    private final List<GolemMarker> markers = new ArrayList<>();
    private Direction homeFacing = Direction.UP;
    private BlockPos persistentHome;
    private int persistentHomeRadius = 32;
    private FluidStack fluidCarried = FluidStack.EMPTY;
    private String essentiaCarried;
    private int essentiaAmount;
    private BlockPos lumberTreeBase;
    private final List<BlockPos> lumberTreeLogs = new ArrayList<>();

    public ClassicGolemEntity(EntityType<? extends ClassicGolemEntity> type, Level level,
            GolemMaterial material) {
        super(type, level);
        this.material = material;
        this.upgrades = new byte[material.upgradeSlots()];
        Arrays.fill(this.upgrades, (byte) -1);
        syncUpgrades();
        rebuildInventory(null);
        rebuildFilters(null);
        setPersistenceRequired();
        setMaxUpStep(1F);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.BlockPathTypes.WATER,
                material.light() ? 0F : -1F);
        if (getNavigation() instanceof GroundPathNavigation ground) {
            ground.setCanFloat(material.light());
            ground.setCanOpenDoors(true);
            ground.setCanPassDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes(GolemMaterial material) {
        return AbstractGolem.createMobAttributes()
                .add(Attributes.MAX_HEALTH, material.health())
                .add(Attributes.ARMOR, material.armor())
                .add(Attributes.MOVEMENT_SPEED, material.speed())
                .add(Attributes.ATTACK_DAMAGE, material.attackDamage())
                .add(Attributes.FOLLOW_RANGE, 32D);
    }

    @Override protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER, Optional.empty());
        entityData.define(INACTIVE, false);
        entityData.define(CORE, -1);
        entityData.define(UPGRADES, "");
        entityData.define(TOGGLES, (byte) 0);
        entityData.define(CARRYING, false);
        entityData.define(CARRIED_DISPLAY, ItemStack.EMPTY);
    }

    @Override protected void registerGoals() { GolemCoreGoals.register(this); }

    @Override
    public void aiStep() {
        super.aiStep();
        if (actionTimer > 0) actionTimer--;
        if (leftArmTimer > 0) leftArmTimer--;
        if (rightArmTimer > 0) rightArmTimer--;
        boolean fettered = level().getBlockState(blockPosition().below()).is(ModBlocks.GOLEM_FETTER.get())
                && level().getBlockState(blockPosition().below()).getValue(
                        com.thaumcraftmodern.world.block.GolemFetterBlock.ACTIVE);
        if (!level().isClientSide) entityData.set(INACTIVE, fettered);
        if (fettered) {
            getNavigation().stop();
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(0D, motion.y, 0D);
        }
        if (level().isClientSide) {
            if (bootup > 0F && core() != null) {
                bootup *= bootup / 33.1F;
                level().playLocalSound(getX(), getY(), getZ(), ModSounds.CAMERA_TICKS.get(),
                        SoundSource.NEUTRAL, bootup * .2F, bootup, false);
            }
        } else {
            boolean carrying = !fluidCarried.isEmpty() || essentiaAmount > 0;
            ItemStack display = ItemStack.EMPTY;
            for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                if (!inventory.getItem(slot).isEmpty()) {
                    carrying = true;
                    if (display.isEmpty()) display = inventory.getItem(slot).copyWithCount(1);
                }
            }
            if (core() == GolemCoreType.LIQUID) {
                display = fluidCarried.isEmpty() ? new ItemStack(Items.BUCKET)
                        : FluidUtil.getFilledBucket(fluidCarried.copy());
                if (display.isEmpty()) display = new ItemStack(Items.BUCKET);
            } else if (core() == GolemCoreType.ALCHEMY && essentiaAmount > 0 && essentiaCarried != null) {
                CompoundTag jar = new CompoundTag();
                jar.putString("Aspect", essentiaCarried);
                jar.putInt("Amount", essentiaAmount);
                display = WardedJarItem.withContents(
                        (WardedJarItem) ModItems.FILLED_WARDED_JAR.get(), jar);
            }
            if (entityData.get(CARRYING) != carrying) entityData.set(CARRYING, carrying);
            if (!ItemStack.matches(entityData.get(CARRIED_DISPLAY), display)) {
                entityData.set(CARRIED_DISPLAY, display);
            }
            if (regenerationTimer > 0) regenerationTimer--;
            else {
                regenerationTimer = material.regenerationDelay();
                if (getHealth() < getMaxHealth()) heal(1F);
            }
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!isOwnedBy(player)) return InteractionResult.PASS;
        if (held.getItem() instanceof com.thaumcraftmodern.item.GolemBellItem) return InteractionResult.PASS;
        if (held.getItem() instanceof GolemCoreItem coreItem && core() == null) {
            if (!level().isClientSide) {
                setCore(coreItem.type());
                level().broadcastEntityEvent(this, (byte) 7);
                consumeUnlessCreative(player, held);
                playUpgradeSound();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (held.getItem() instanceof GolemUpgradeItem upgradeItem) {
            if (!canInstall(upgradeItem.type())) return InteractionResult.FAIL;
            if (!level().isClientSide) {
                installUpgrade(upgradeItem.type());
                consumeUnlessCreative(player, held);
                playUpgradeSound();
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (held.is(Items.WHEAT)) {
            if (!level().isClientSide) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                heal(5F);
                level().playSound(null, blockPosition(), SoundEvents.GENERIC_EAT,
                        SoundSource.NEUTRAL, .3F, .9F + random.nextFloat() * .2F);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (held.isEmpty() && core() != null && core().hasGui()) {
            if (!level().isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                NetworkHooks.openScreen(serverPlayer, this, buffer -> buffer.writeVarInt(getId()));
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (material.fireResistant() && source.is(DamageTypeTags.IS_FIRE)) return false;
        if (material == GolemMaterial.THAUMIUM
                && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC))) amount *= .5F;
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && upgradeAmount(GolemUpgradeType.PERDITIO) > 0
                && source.getEntity() != null && source.getEntity() != this) {
            source.getEntity().hurt(damageSources().thorns(this), upgradeAmount(GolemUpgradeType.PERDITIO));
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) startWorkAnimation();
        if (hit && upgradeAmount(GolemUpgradeType.IGNIS) > 0) {
            target.setSecondsOnFire(upgradeAmount(GolemUpgradeType.IGNIS) * 4);
        }
        return hit;
    }

    @Override protected int decreaseAirSupply(int air) { return air; }

    public GolemMaterial material() { return material; }
    public boolean isInactive() { return entityData.get(INACTIVE); }
    public void setOwner(@Nullable UUID owner) { entityData.set(OWNER, Optional.ofNullable(owner)); }
    public Optional<UUID> owner() { return entityData.get(OWNER); }
    public int carryLimit() {
        int bonus = Math.min(16, Math.max(4, material.carry()));
        return material.carry() + bonus * upgradeAmount(GolemUpgradeType.TERRA);
    }
    public int fluidCarryLimit() { return Math.max(1000, (int) Math.floor(Math.sqrt(carryLimit())) * 1000); }
    public FluidStack fluidCarried() { return fluidCarried; }
    public void setFluidCarried(FluidStack fluid) { fluidCarried = fluid == null ? FluidStack.EMPTY : fluid; }
    public String essentiaCarried() { return essentiaCarried; }
    public int essentiaAmount() { return essentiaAmount; }
    public void setEssentiaCarried(@Nullable String aspect, int amount) {
        essentiaCarried = amount > 0 ? aspect : null;
        essentiaAmount = Math.max(0, Math.min(carryLimit(), amount));
    }
    public int upgradeSlots() { return material.upgradeSlots(); }
    public int strength() { return material.strength(); }
    public int effectiveStrength() { return material.strength() + upgradeAmount(GolemUpgradeType.TERRA); }
    public int visCost() { return material.visCost(); }
    public ItemStack placerItem() { return ModItems.golem(material).get().getDefaultInstance(); }
    public GolemCoreType core() { return GolemCoreType.byLegacyId(entityData.get(CORE)); }
    public SimpleContainer inventory() { return inventory; }
    public SimpleContainer filters() { return filters; }
    public void ensureConfigurationInventories() {
        int expected = expectedFilterSlots();
        if (filters == null || filters.getContainerSize() != expected) rebuildFilters(filters);
    }
    public boolean hasCoreInventory() { return core() != null && core().hasInventory(); }
    public Direction homeFacing() { return homeFacing; }
    public void setHomeFacing(Direction facing) { homeFacing = facing == null ? Direction.UP : facing; }
    @Override public void restrictTo(BlockPos pos, int radius) {
        super.restrictTo(pos, radius);
        persistentHome = pos == null ? null : pos.immutable();
        persistentHomeRadius = Math.max(1, radius);
    }
    @Override public void clearRestriction() {
        super.clearRestriction();
        persistentHome = null;
    }
    public BlockPos homePos() { return persistentHome != null ? persistentHome : blockPosition(); }
    public BlockPos attachedPos() { return homePos().relative(homeFacing.getOpposite()); }
    public List<GolemMarker> markers() { return Collections.unmodifiableList(markers); }
    public void clearMarkers() { markers.clear(); }
    public void setMarkers(List<GolemMarker> replacement) {
        markers.clear();
        if (replacement != null) markers.addAll(replacement);
    }
    public boolean removeMarker(BlockPos pos, Direction side) {
        return markers.removeIf(marker -> marker.pos().equals(pos) && marker.side() == side);
    }
    public boolean removeMarkersAt(BlockPos pos) {
        return pos != null && markers.removeIf(marker -> marker.pos().equals(pos));
    }
    public byte cycleMarker(BlockPos pos, Direction side) {
        for (int index = 0; index < markers.size(); index++) {
            GolemMarker marker = markers.get(index);
            if (!marker.pos().equals(pos) || marker.side() != side) continue;
            byte next = upgradeAmount(GolemUpgradeType.ORDO) > 0
                    ? (byte) (marker.color() >= 15 ? -1 : marker.color() + 1) : -1;
            markers.set(index, new GolemMarker(pos.immutable(), side, next));
            return next;
        }
        markers.add(new GolemMarker(pos.immutable(), side, (byte) -1));
        return -1;
    }
    /** Exact bell rule: add, remove, or (with Ordo) cycle -1 through the 16 dye colors. */
    public byte changeMarker(BlockPos pos, Direction side, boolean sneaking) {
        for (int index = 0; index < markers.size(); index++) {
            GolemMarker marker = markers.get(index);
            if (!marker.pos().equals(pos) || marker.side() != side) continue;
            if (sneaking || upgradeAmount(GolemUpgradeType.ORDO) == 0 || marker.color() >= 15) {
                markers.remove(index);
                return Byte.MIN_VALUE;
            }
            byte next = (byte) (marker.color() + 1);
            markers.set(index, new GolemMarker(pos.immutable(), side, next));
            return next;
        }
        markers.add(new GolemMarker(pos.immutable(), side, (byte) -1));
        return -1;
    }
    public boolean acceptsFilter(ItemStack stack) {
        if (stack.isEmpty() || filters == null) return true;
        boolean configured = false;
        for (int slot = 0; slot < filters.getContainerSize(); slot++) {
            ItemStack filter = filters.getItem(slot);
            if (filter.isEmpty()) continue;
            configured = true;
            if (filterMatches(filter, stack)) return true;
        }
        return !configured;
    }
    public boolean hasConfiguredFilters() {
        if (filters == null) return false;
        for (int slot = 0; slot < filters.getContainerSize(); slot++) if (!filters.getItem(slot).isEmpty()) return true;
        return false;
    }
    public int configuredAmount(ItemStack stack) {
        int amount = 0;
        if (filters != null) for (int slot = 0; slot < filters.getContainerSize(); slot++) {
            ItemStack filter = filters.getItem(slot);
            if (!filter.isEmpty() && filterMatches(filter, stack)) amount += Math.max(1, filter.getCount());
        }
        return amount;
    }
    public byte filterColor(int slot) {
        return slot >= 0 && slot < filterColors.length ? filterColors[slot] : -1;
    }
    public void setFilterColor(int slot, int color) {
        if (slot >= 0 && slot < filterColors.length) filterColors[slot] = (byte) Math.max(-1, Math.min(15, color));
    }
    public boolean markerAccepts(ItemStack stack, GolemMarker marker) {
        if (upgradeAmount(GolemUpgradeType.ORDO) == 0 || !hasConfiguredFilters()) return marker.color() == -1;
        boolean matched = false;
        for (int slot = 0; slot < filters.getContainerSize(); slot++) {
            ItemStack filter = filters.getItem(slot);
            if (!filter.isEmpty() && filterMatches(filter, stack)) {
                matched = true;
                byte color = filterColor(slot);
                if (color == -1 || marker.color() == color) return true;
            }
        }
        return !matched && marker.color() == -1;
    }
    public boolean filterMatches(ItemStack filter, ItemStack stack) {
        if (filter.isEmpty() || stack.isEmpty()) return false;
        boolean oreMatch = upgradeAmount(GolemUpgradeType.PERDITIO) > 0 && toggle(5)
                && filter.getTags().anyMatch(tag -> stack.is(tag));
        if (!ItemStack.isSameItem(filter, stack) && !oreMatch) return false;
        if (upgradeAmount(GolemUpgradeType.PERDITIO) == 0) return ItemStack.isSameItemSameTags(filter, stack);
        if (!toggle(6) && filter.getDamageValue() != stack.getDamageValue()) return false;
        if (!toggle(7)) {
            CompoundTag left = filter.getTag() == null ? new CompoundTag() : filter.getTag().copy();
            CompoundTag right = stack.getTag() == null ? new CompoundTag() : stack.getTag().copy();
            if (toggle(6)) { left.remove("Damage"); right.remove("Damage"); }
            if (!left.equals(right)) return false;
        }
        return true;
    }
    public boolean toggle(int index) {
        return index >= 0 && index < 8 && (entityData.get(TOGGLES) & (1 << index)) != 0;
    }
    public void setToggle(int index, boolean value) {
        if (index < 0 || index >= 8) return;
        int packed = entityData.get(TOGGLES) & 0xff;
        packed = value ? packed | (1 << index) : packed & ~(1 << index);
        entityData.set(TOGGLES, (byte) packed);
    }
    public boolean canAttackHostiles() { return !toggle(1); }
    public boolean canAttackAnimals() { return upgradeAmount(GolemUpgradeType.ORDO) > 0 && !toggle(2); }
    public boolean canAttackPlayers() { return upgradeAmount(GolemUpgradeType.ORDO) > 0 && !toggle(3); }
    public boolean canAttackCreepers() { return upgradeAmount(GolemUpgradeType.ORDO) > 0 && !toggle(4); }
    public boolean isOperational() { return core() != null && !isInactive() && !menuPaused; }
    public float bootup() { return bootup; }
    public int actionTimer() { return 3 - Math.abs(actionTimer - 3); }
    public int leftArmTimer() { return leftArmTimer; }
    public int rightArmTimer() { return rightArmTimer; }
    public boolean isCarryingForAnimation() {
        if (level().isClientSide) return entityData.get(CARRYING);
        if (!fluidCarried.isEmpty() || essentiaAmount > 0) return true;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) return true;
        }
        return false;
    }
    public ItemStack carriedForDisplay() { return entityData.get(CARRIED_DISPLAY); }
    public void setMenuPaused(boolean paused) {
        menuPaused = paused;
        if (paused) getNavigation().stop();
    }
    public void startWorkAnimation() {
        if (actionTimer == 0 && !level().isClientSide) {
            actionTimer = 6;
            level().broadcastEntityEvent(this, (byte) 4);
        }
    }
    public void startLeftArmAnimation() {
        if (!level().isClientSide) level().broadcastEntityEvent(this, (byte) 6);
    }
    public void startRightArmAnimation() {
        if (!level().isClientSide) level().broadcastEntityEvent(this, (byte) 8);
    }
    public int workRange() { return 16 + upgradeAmount(GolemUpgradeType.AQUA) * 4; }
    @Nullable BlockPos lumberTreeBase() { return lumberTreeBase; }
    List<BlockPos> lumberTreeLogs() { return List.copyOf(lumberTreeLogs); }
    void rememberLumberTree(BlockPos base, List<BlockPos> logs) {
        lumberTreeBase = base == null ? null : base.immutable();
        lumberTreeLogs.clear();
        if (logs != null) {
            logs.stream().map(BlockPos::immutable).distinct().forEach(lumberTreeLogs::add);
        }
    }
    void forgetLumberLog(BlockPos pos) { lumberTreeLogs.remove(pos); }
    void clearLumberTree() {
        lumberTreeBase = null;
        lumberTreeLogs.clear();
    }
    public int upgradeAmount(GolemUpgradeType type) {
        int count = 0;
        for (byte value : upgrades) if (value == type.legacyId()) count++;
        return count;
    }
    public GolemUpgradeType upgrade(int slot) {
        return slot >= 0 && slot < upgrades.length ? GolemUpgradeType.byLegacyId(upgrades[slot]) : null;
    }

    public void setCore(@Nullable GolemCoreType core) {
        entityData.set(CORE, core == null ? -1 : core.legacyId());
        if (core != GolemCoreType.LUMBER) clearLumberTree();
        rebuildInventory(inventory);
        rebuildFilters(filters);
    }

    public boolean canInstall(GolemUpgradeType type) {
        return firstEmptyUpgradeSlot() >= 0 && upgradeAmount(type) < 2;
    }

    public boolean installUpgrade(GolemUpgradeType type) {
        int slot = firstEmptyUpgradeSlot();
        if (slot < 0 || upgradeAmount(type) >= 2) return false;
        upgrades[slot] = (byte) type.legacyId();
        syncUpgrades();
        applyUpgradeAttributes();
        rebuildInventory(inventory);
        rebuildFilters(filters);
        return true;
    }

    public CompoundTag savePortableData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Core", entityData.get(CORE));
        tag.putByte("Toggles", entityData.get(TOGGLES));
        tag.putByteArray("Upgrades", upgrades);
        tag.putFloat("Health", getHealth());
        CompoundTag inventoryTag = new CompoundTag();
        NonNullList<ItemStack> savedInventory = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < savedInventory.size(); slot++) savedInventory.set(slot, inventory.getItem(slot));
        ContainerHelper.saveAllItems(inventoryTag, savedInventory);
        tag.put("GolemInventory", inventoryTag);
        if (!fluidCarried.isEmpty()) {
            CompoundTag fluidTag = new CompoundTag();
            fluidCarried.writeToNBT(fluidTag);
            tag.put("FluidCarried", fluidTag);
        }
        if (essentiaCarried != null && essentiaAmount > 0) {
            tag.putString("EssentiaCarried", essentiaCarried);
            tag.putInt("EssentiaAmount", essentiaAmount);
        }
        NonNullList<ItemStack> savedFilters = NonNullList.withSize(filters.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < savedFilters.size(); slot++) savedFilters.set(slot, filters.getItem(slot));
        ContainerHelper.saveAllItems(tag, savedFilters);
        tag.putByteArray("FilterColors", filterColors);
        net.minecraft.nbt.ListTag markerTags = new net.minecraft.nbt.ListTag();
        for (GolemMarker marker : markers) markerTags.add(marker.save());
        tag.put("Markers", markerTags);
        return tag;
    }

    public void loadPortableData(CompoundTag tag) {
        entityData.set(CORE, tag.getInt("Core"));
        entityData.set(TOGGLES, tag.getByte("Toggles"));
        byte[] restored = tag.getByteArray("Upgrades");
        Arrays.fill(upgrades, (byte) -1);
        System.arraycopy(restored, 0, upgrades, 0, Math.min(restored.length, upgrades.length));
        syncUpgrades();
        applyUpgradeAttributes();
        rebuildInventory(null);
        rebuildFilters(null);
        if (tag.contains("GolemInventory", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            NonNullList<ItemStack> savedInventory = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("GolemInventory"), savedInventory);
            for (int slot = 0; slot < savedInventory.size(); slot++) inventory.setItem(slot, savedInventory.get(slot));
        }
        fluidCarried = tag.contains("FluidCarried", net.minecraft.nbt.Tag.TAG_COMPOUND)
                ? FluidStack.loadFluidStackFromNBT(tag.getCompound("FluidCarried")) : FluidStack.EMPTY;
        setEssentiaCarried(tag.contains("EssentiaCarried") ? tag.getString("EssentiaCarried") : null,
                tag.getInt("EssentiaAmount"));
        if (tag.contains("Health", net.minecraft.nbt.Tag.TAG_FLOAT)) {
            setHealth(net.minecraft.util.Mth.clamp(tag.getFloat("Health"), 1F, getMaxHealth()));
        }
        NonNullList<ItemStack> savedFilters = NonNullList.withSize(filters.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, savedFilters);
        for (int slot = 0; slot < savedFilters.size(); slot++) filters.setItem(slot, savedFilters.get(slot));
        byte[] colors = tag.getByteArray("FilterColors");
        System.arraycopy(colors, 0, filterColors, 0, Math.min(colors.length, filterColors.length));
        markers.clear();
        net.minecraft.nbt.ListTag markerTags = tag.getList("Markers", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int index = 0; index < markerTags.size(); index++) markers.add(GolemMarker.load(markerTags.getCompound(index)));
    }

    @Override protected @Nullable SoundEvent getAmbientSound() { return null; }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CORE.equals(key) && level() != null && level().isClientSide && core() != null && bootup < 0F) {
            bootup = tickCount <= 1 ? 0F : 33F;
        }
        if (CORE.equals(key) && material != null && inventory != null && filters != null) {
            rebuildInventory(inventory);
            rebuildFilters(filters);
        }
        if (UPGRADES.equals(key) && material != null) {
            String encoded = entityData.get(UPGRADES);
            if (upgrades == null || upgrades.length != material.upgradeSlots()) {
                upgrades = new byte[material.upgradeSlots()];
            }
            Arrays.fill(upgrades, (byte) -1);
            for (int slot = 0; slot < Math.min(encoded.length(), upgrades.length); slot++) {
                int value = Character.digit(encoded.charAt(slot), 16);
                upgrades[slot] = (byte) (value == 15 ? -1 : value);
            }
            if (filters != null) rebuildFilters(filters);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) actionTimer = 6;
        else if (event == 6) leftArmTimer = 5;
        else if (event == 8) rightArmTimer = 5;
        else if (event == 7) {
            bootup = 33F;
            for (int index = 0; index < 12; index++) {
                double angle = Math.PI * 2D * index / 12D;
                level().addParticle(index % 2 == 0 ? net.minecraft.core.particles.ParticleTypes.ENCHANT
                                : net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        getX() + Math.cos(angle) * .24D, getY() + .56D + random.nextDouble() * .2D,
                        getZ() + Math.sin(angle) * .24D, 0D, .015D, 0D);
            }
        } else super.handleEntityEvent(event);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GolemMenu(containerId, playerInventory, this);
    }

    @Override
    public Component getDisplayName() {
        return getName();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        owner().ifPresent(uuid -> tag.putUUID("Owner", uuid));
        tag.putInt("RegenTimer", regenerationTimer);
        tag.putInt("Core", entityData.get(CORE));
        tag.putByte("Toggles", entityData.get(TOGGLES));
        tag.putByte("HomeFacing", (byte) homeFacing.get3DDataValue());
        if (persistentHome != null) {
            tag.putLong("GolemHomePos", persistentHome.asLong());
            tag.putInt("GolemHomeRadius", persistentHomeRadius);
        }
        tag.putByteArray("Upgrades", upgrades);
        if (!fluidCarried.isEmpty()) {
            CompoundTag fluidTag = new CompoundTag();
            fluidCarried.writeToNBT(fluidTag);
            tag.put("FluidCarried", fluidTag);
        }
        if (essentiaCarried != null && essentiaAmount > 0) {
            tag.putString("EssentiaCarried", essentiaCarried);
            tag.putInt("EssentiaAmount", essentiaAmount);
        }
        NonNullList<ItemStack> saved = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < saved.size(); slot++) saved.set(slot, inventory.getItem(slot));
        ContainerHelper.saveAllItems(tag, saved);
        CompoundTag filterTag = new CompoundTag();
        NonNullList<ItemStack> savedFilters = NonNullList.withSize(filters.getContainerSize(), ItemStack.EMPTY);
        for (int slot = 0; slot < savedFilters.size(); slot++) savedFilters.set(slot, filters.getItem(slot));
        ContainerHelper.saveAllItems(filterTag, savedFilters);
        tag.put("GolemFilters", filterTag);
        tag.putByteArray("GolemFilterColors", filterColors);
        net.minecraft.nbt.ListTag markerTags = new net.minecraft.nbt.ListTag();
        for (GolemMarker marker : markers) markerTags.add(marker.save());
        tag.put("GolemMarkers", markerTags);
        if (core() == GolemCoreType.LUMBER && lumberTreeBase != null
                && !lumberTreeLogs.isEmpty()) {
            tag.putLong("LumberTreeBase", lumberTreeBase.asLong());
            tag.putLongArray("LumberTreeLogs", lumberTreeLogs.stream()
                    .mapToLong(BlockPos::asLong).toArray());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setOwner(tag.hasUUID("Owner") ? tag.getUUID("Owner") : null);
        regenerationTimer = Math.max(0, Math.min(material.regenerationDelay(), tag.getInt("RegenTimer")));
        byte[] savedUpgrades = tag.getByteArray("Upgrades");
        upgrades = new byte[material.upgradeSlots()];
        Arrays.fill(upgrades, (byte) -1);
        System.arraycopy(savedUpgrades, 0, upgrades, 0, Math.min(savedUpgrades.length, upgrades.length));
        entityData.set(CORE, tag.contains("Core") ? tag.getInt("Core") : -1);
        entityData.set(TOGGLES, tag.getByte("Toggles"));
        homeFacing = Direction.from3DDataValue(tag.getByte("HomeFacing"));
        if (tag.contains("GolemHomePos", net.minecraft.nbt.Tag.TAG_LONG)) {
            restrictTo(BlockPos.of(tag.getLong("GolemHomePos")),
                    tag.contains("GolemHomeRadius", net.minecraft.nbt.Tag.TAG_INT)
                            ? tag.getInt("GolemHomeRadius") : 32);
        } else {
            persistentHome = null;
        }
        fluidCarried = tag.contains("FluidCarried", net.minecraft.nbt.Tag.TAG_COMPOUND)
                ? FluidStack.loadFluidStackFromNBT(tag.getCompound("FluidCarried")) : FluidStack.EMPTY;
        setEssentiaCarried(tag.contains("EssentiaCarried") ? tag.getString("EssentiaCarried") : null,
                tag.getInt("EssentiaAmount"));
        syncUpgrades();
        applyUpgradeAttributes();
        rebuildInventory(null);
        rebuildFilters(null);
        NonNullList<ItemStack> saved = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, saved);
        for (int slot = 0; slot < saved.size(); slot++) inventory.setItem(slot, saved.get(slot));
        if (tag.contains("GolemFilters", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            NonNullList<ItemStack> savedFilters = NonNullList.withSize(filters.getContainerSize(), ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("GolemFilters"), savedFilters);
            for (int slot = 0; slot < savedFilters.size(); slot++) filters.setItem(slot, savedFilters.get(slot));
        }
        byte[] savedColors = tag.getByteArray("GolemFilterColors");
        Arrays.fill(filterColors, (byte) -1);
        System.arraycopy(savedColors, 0, filterColors, 0, Math.min(savedColors.length, filterColors.length));
        markers.clear();
        net.minecraft.nbt.ListTag markerTags = tag.getList("GolemMarkers", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int index = 0; index < markerTags.size(); index++) markers.add(GolemMarker.load(markerTags.getCompound(index)));
        clearLumberTree();
        if (core() == GolemCoreType.LUMBER && tag.contains("LumberTreeBase")
                && tag.contains("LumberTreeLogs", net.minecraft.nbt.Tag.TAG_LONG_ARRAY)) {
            lumberTreeBase = BlockPos.of(tag.getLong("LumberTreeBase"));
            for (long packed : tag.getLongArray("LumberTreeLogs")) {
                lumberTreeLogs.add(BlockPos.of(packed));
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (core() != null) spawnAtLocation(ModItems.golemCore(core()).get());
        for (byte value : upgrades) {
            GolemUpgradeType type = GolemUpgradeType.byLegacyId(value);
            if (type != null) spawnAtLocation(upgradeItem(type));
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty()) spawnAtLocation(stack);
        }
    }

    private boolean isOwnedBy(Player player) { return owner().isEmpty() || owner().get().equals(player.getUUID()); }
    private static void consumeUnlessCreative(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) stack.shrink(1);
    }
    private void playUpgradeSound() {
        level().playSound(null, blockPosition(), ModSounds.UPGRADE.get(), SoundSource.NEUTRAL, .5F, 1F);
    }
    private int firstEmptyUpgradeSlot() {
        for (int slot = 0; slot < upgrades.length; slot++) if (upgrades[slot] < 0) return slot;
        return -1;
    }
    private void syncUpgrades() {
        StringBuilder encoded = new StringBuilder(upgrades.length);
        for (byte value : upgrades) encoded.append(Integer.toHexString(value < 0 ? 15 : value));
        entityData.set(UPGRADES, encoded.toString());
    }
    private void applyUpgradeAttributes() {
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(
                    material.speed() * (1D + upgradeAmount(GolemUpgradeType.AER) * .15D));
        }
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            int earth = upgradeAmount(GolemUpgradeType.TERRA);
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2D + material.strength() + earth * 2D);
        }
        if (getAttribute(Attributes.FOLLOW_RANGE) != null) {
            getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(
                    32D + upgradeAmount(GolemUpgradeType.ORDO) * 8D);
        }
    }
    private void rebuildInventory(@Nullable SimpleContainer previous) {
        // TC4 transports one item stack; Terra changes its count limit, not slot count.
        int size = 1;
        SimpleContainer replacement = new SimpleContainer(size);
        if (previous != null) {
            for (int slot = 0; slot < Math.min(size, previous.getContainerSize()); slot++) {
                replacement.setItem(slot, previous.getItem(slot));
            }
        }
        inventory = replacement;
    }
    private void rebuildFilters(@Nullable SimpleContainer previous) {
        int size = expectedFilterSlots();
        byte[] previousColors = filterColors;
        SimpleContainer replacement = new SimpleContainer(size);
        if (previous != null) {
            for (int slot = 0; slot < Math.min(size, previous.getContainerSize()); slot++) {
                replacement.setItem(slot, previous.getItem(slot));
            }
        }
        filters = replacement;
        filterColors = new byte[size];
        Arrays.fill(filterColors, (byte) -1);
        if (previousColors != null) System.arraycopy(previousColors, 0, filterColors, 0,
                Math.min(previousColors.length, filterColors.length));
    }
    private int expectedFilterSlots() {
        GolemCoreType core = core();
        return core == null ? 1 : core.configurationSlots(upgradeAmount(GolemUpgradeType.IGNIS));
    }
    private static String emptyUpgradeString(int slots) { return "f".repeat(slots); }
    private static net.minecraft.world.item.Item upgradeItem(GolemUpgradeType type) {
        String id = "golem_upgrade_" + type.id();
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(
                        com.thaumcraftmodern.ThaumcraftModern.MOD_ID, id));
    }
}
