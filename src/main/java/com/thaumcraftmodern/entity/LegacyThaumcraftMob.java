package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.ManaBeanItem;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.WispZapPacket;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsSpawnRules;
import com.thaumcraftmodern.world.menu.PechMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Shared server-authoritative base for the world-content entity roster. Each
 * registered EntityType fixes one legacy kind and therefore cannot be changed
 * by client NBT.
 */
public final class LegacyThaumcraftMob extends Monster
        implements RangedAttackMob {
    private static final int PECH_POPULATION_RANGE = 16;
    private static final int WISP_POPULATION_RANGE = 16;
    private static final int TAINTACLE_POPULATION_HORIZONTAL_RANGE = 24;
    private static final int TAINTACLE_POPULATION_VERTICAL_RANGE = 8;

    private static final EntityDataAccessor<Integer> PECH_TYPE =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Integer> PECH_ANGER =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Boolean> PECH_TAMED =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.BOOLEAN
            );
    private static final EntityDataAccessor<String> WISP_ASPECT =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.STRING
            );
    private static final EntityDataAccessor<Boolean> CRIMSON_RITUALIST =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.BOOLEAN
            );
    private static final EntityDataAccessor<Optional<BlockPos>> CRIMSON_ALTAR =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.OPTIONAL_BLOCK_POS
            );
    private static final EntityDataAccessor<Float> FURIOUS_ANGER =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.FLOAT
            );
    private static final EntityDataAccessor<Boolean> HARMLESS =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.BOOLEAN
            );
    private static final EntityDataAccessor<Optional<UUID>> WARP_VIEWER =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.OPTIONAL_UUID
            );
    private static final EntityDataAccessor<Integer> THAUMIC_SLIME_SIZE =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Boolean> FIREBAT_HANGING =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.BOOLEAN
            );
    private static final EntityDataAccessor<Boolean> CONSTRUCT_HEADLESS =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.BOOLEAN
            );
    private static final EntityDataAccessor<Integer> CONSTRUCT_ATTACK_TIMER =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Integer> CONSTRUCT_RECOVERY_TIMER =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Integer> CONSTRUCT_BEAM_CHARGE =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.INT
            );
    private static final EntityDataAccessor<Boolean> CRAB_HELM =
            SynchedEntityData.defineId(
                    LegacyThaumcraftMob.class,
                    EntityDataSerializers.BOOLEAN
            );

    private final LegacyMobKind kind;
    private final @Nullable ServerBossEvent constructBossEvent;
    private final Map<Integer, Integer> constructAggro = new HashMap<>();
    private final ItemStackHandler pechPack =
            new ItemStackHandler(PechBehavior.PACK_SLOTS);
    private boolean pechTrading;
    private BlockPos eldritchAltarHome;
    private int slimeJumpDelay;
    private int slimeSpitCooldown = 100;
    private int slimeLaunchedTicks;
    private int slimeAttackCooldown;
    private boolean slimeSplit;
    private boolean slimeWasOnGround;
    private LegacyThaumcraftMob slimeMergeTarget;
    private int wispAttackCounter;
    private int wispAggroCooldown;
    private int wispCourseChangeCooldown;
    private Vec3 wispWaypoint = Vec3.ZERO;
    private int firebatAttackCooldown;
    private BlockPos firebatFlightTarget;
    private UUID focusBatOwner;
    private boolean focusBatSummoned;
    private boolean focusBatExplosive;
    private boolean focusBatVampire;
    private boolean constructChargingBeam;
    private int constructAngerTicks;
    private int constructArcTicks;
    private BlockPos constructArcTarget;
    private int taintSporeSize;
    private int taintSporeGrowth;
    private int taintSwarmSpawnCounter = 500;
    private final TaintedChickenFlapAnimation taintedChickenFlap =
            new TaintedChickenFlapAnimation();

    public LegacyThaumcraftMob(
            EntityType<? extends Monster> entityType,
            Level level,
            LegacyMobKind kind
    ) {
        super(entityType, level);
        this.kind = Objects.requireNonNull(kind, "kind");
        taintSporeSize = kind == LegacyMobKind.TAINT_SPORE_SWARMER ? 10 : 2;
        constructBossEvent = isOuterLandsBossKind(kind)
                ? new ServerBossEvent(
                        getDisplayName(),
                        BossEvent.BossBarColor.PURPLE,
                        BossEvent.BossBarOverlay.PROGRESS
                )
                : null;
        equipCrimsonWeapon();
        registerKindGoals();
        if (kind.flying()) {
            moveControl = new FlyingMoveControl(this, 12, true);
            setNoGravity(true);
        }
        if (kind == LegacyMobKind.FIREBAT) {
            entityData.set(FIREBAT_HANGING, true);
        }
        if (kind == LegacyMobKind.TAINTACLE
                || kind == LegacyMobKind.TAINT_TENDRIL
                || kind == LegacyMobKind.GIANT_TAINTACLE) {
            setNoAi(false);
        }
        xpReward = Math.max(3, (int) Math.round(kind.health() / 8.0D));
        if (kind == LegacyMobKind.MIND_SPIDER) {
            xpReward = 1;
        } else if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            xpReward = 50;
        }
    }

    public LegacyMobKind kind() {
        return kind;
    }

    private static boolean isOuterLandsBossKind(LegacyMobKind kind) {
        return kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                || kind == LegacyMobKind.ELDRITCH_WARDEN
                || kind == LegacyMobKind.CRIMSON_PRAETOR
                || kind == LegacyMobKind.GIANT_TAINTACLE;
    }

    @Override
    public Component getName() {
        if (kind == LegacyMobKind.PECH && !hasCustomName()) {
            return Component.translatable(switch (pechType()) {
                case PechBehavior.MAGE ->
                        "entity.thaumic_reborn.pech_mage";
                case PechBehavior.STALKER ->
                        "entity.thaumic_reborn.pech_stalker";
                default -> "entity.thaumic_reborn.pech_forager";
            });
        }
        return super.getName();
    }

    private void equipCrimsonWeapon() {
        if (kind == LegacyMobKind.CRIMSON_KNIGHT
                || kind == LegacyMobKind.CRIMSON_PRAETOR) {
            setItemSlot(
                    EquipmentSlot.MAINHAND,
                    new ItemStack(Items.IRON_SWORD)
            );
        } else if (kind == LegacyMobKind.CRIMSON_INQUISITOR) {
            setItemSlot(
                    EquipmentSlot.MAINHAND,
                    new ItemStack(Items.IRON_AXE)
            );
        }
    }

    /** Restores TC4's real server-side cultist equipment and its drop rolls. */
    private void equipCrimsonArmor(boolean rollClericBoots) {
        switch (kind) {
            case CRIMSON_KNIGHT, CRIMSON_INQUISITOR -> {
                equipCultistArmorSlot(EquipmentSlot.HEAD,
                        ModItems.CULTIST_KNIGHT_HELMET.get());
                equipCultistArmorSlot(EquipmentSlot.CHEST,
                        ModItems.CULTIST_KNIGHT_CHESTPLATE.get());
                equipCultistArmorSlot(EquipmentSlot.LEGS,
                        ModItems.CULTIST_KNIGHT_LEGGINGS.get());
                equipCultistArmorSlot(EquipmentSlot.FEET,
                        ModItems.CULTIST_BOOTS.get());
            }
            case CRIMSON_CLERIC -> {
                equipCultistArmorSlot(EquipmentSlot.HEAD,
                        ModItems.CULTIST_CLERIC_HOOD.get());
                equipCultistArmorSlot(EquipmentSlot.CHEST,
                        ModItems.CULTIST_CLERIC_ROBE.get());
                equipCultistArmorSlot(EquipmentSlot.LEGS,
                        ModItems.CULTIST_CLERIC_LEGGINGS.get());
                if (rollClericBoots
                        && getRandom().nextFloat() < (level().getDifficulty()
                                == net.minecraft.world.Difficulty.HARD
                                ? 0.3F : 0.1F)) {
                    equipCultistArmorSlot(EquipmentSlot.FEET,
                            ModItems.CULTIST_BOOTS.get());
                }
            }
            case CRIMSON_PRAETOR -> {
                equipCultistArmorSlot(EquipmentSlot.HEAD,
                        ModItems.CULTIST_PRAETOR_HELMET.get());
                equipCultistArmorSlot(EquipmentSlot.CHEST,
                        ModItems.CULTIST_PRAETOR_CHESTPLATE.get());
                equipCultistArmorSlot(EquipmentSlot.LEGS,
                        ModItems.CULTIST_PRAETOR_LEGGINGS.get());
                equipCultistArmorSlot(EquipmentSlot.FEET,
                        ModItems.CULTIST_BOOTS.get());
            }
            default -> {
            }
        }
    }

    /** TC4 EntityInhabitedZombie.onInitialSpawn equipment, unchanged. */
    private void equipInhabitedZombieArmor() {
        if (kind != LegacyMobKind.INHABITED_ZOMBIE) {
            return;
        }
        equipInhabitedZombieHelmet();
        float armorChance = level().getDifficulty()
                == net.minecraft.world.Difficulty.HARD ? 0.9F : 0.6F;
        if (getRandom().nextFloat() <= armorChance) {
            equipCultistArmorSlot(
                    EquipmentSlot.CHEST,
                    ModItems.CULTIST_KNIGHT_CHESTPLATE.get()
            );
        }
        if (getRandom().nextFloat() <= armorChance) {
            equipCultistArmorSlot(
                    EquipmentSlot.LEGS,
                    ModItems.CULTIST_KNIGHT_LEGGINGS.get()
            );
        }
    }

    private void equipInhabitedZombieHelmet() {
        if (kind != LegacyMobKind.INHABITED_ZOMBIE) {
            return;
        }
        equipCultistArmorSlot(
                EquipmentSlot.HEAD,
                ModItems.CULTIST_KNIGHT_HELMET.get()
        );
    }

    private void equipCultistArmorSlot(EquipmentSlot slot, Item item) {
        if (getItemBySlot(slot).isEmpty()) {
            setItemSlot(slot, new ItemStack(item));
            // Vanilla's original mob-equipment base chance is 8.5%; Looting
            // is applied later by LivingEntity's standard equipment drop path.
            setDropChance(slot, 0.085F);
        }
    }

    public int pechType() {
        return entityData.get(PECH_TYPE);
    }

    public void setPechType(int type) {
        if (kind == LegacyMobKind.PECH) {
            entityData.set(
                    PECH_TYPE,
                    Math.max(
                            PechBehavior.FORAGER,
                            Math.min(PechBehavior.STALKER, type)
                    )
            );
        }
    }

    public int pechAnger() {
        return entityData.get(PECH_ANGER);
    }

    public boolean isPechTamed() {
        return kind == LegacyMobKind.PECH && entityData.get(PECH_TAMED);
    }

    public void setPechTamed(boolean tamed) {
        if (kind != LegacyMobKind.PECH) {
            return;
        }
        entityData.set(PECH_TAMED, tamed);
        if (!tamed) {
            pechTrading = false;
        }
    }

    public ItemStackHandler pechPack() {
        return pechPack;
    }

    public void setPechTrading(boolean trading) {
        pechTrading = kind == LegacyMobKind.PECH && trading;
        if (pechTrading) {
            getNavigation().stop();
        }
    }

    public boolean isConstructHeadless() {
        return entityData.get(CONSTRUCT_HEADLESS);
    }

    public int constructAttackTimer() {
        return entityData.get(CONSTRUCT_ATTACK_TIMER);
    }

    public int constructRecoveryTimer() {
        return entityData.get(CONSTRUCT_RECOVERY_TIMER);
    }

    public int constructBeamCharge() {
        return entityData.get(CONSTRUCT_BEAM_CHARGE);
    }

    public boolean hasCrabHelm() {
        return entityData.get(CRAB_HELM);
    }

    private void setCrabHelm(boolean helm) {
        entityData.set(CRAB_HELM, helm);
        restoreLegacyBaseAttribute(Attributes.ARMOR, helm ? 5.0D : 0.0D);
        restoreLegacyBaseAttribute(
                Attributes.MOVEMENT_SPEED,
                helm ? 0.275D : 0.3D
        );
    }

    public String wispAspect() {
        return entityData.get(WISP_ASPECT);
    }

    public float furiousAnger() {
        return entityData.get(FURIOUS_ANGER);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(PECH_TYPE, 0);
        entityData.define(PECH_ANGER, 0);
        entityData.define(PECH_TAMED, false);
        entityData.define(WISP_ASPECT, PrimalAspect.AER.id());
        entityData.define(CRIMSON_RITUALIST, false);
        entityData.define(CRIMSON_ALTAR, Optional.empty());
        entityData.define(
                FURIOUS_ANGER,
                FuriousZombieBehavior.INITIAL_ANGER
        );
        entityData.define(HARMLESS, false);
        entityData.define(WARP_VIEWER, Optional.empty());
        entityData.define(THAUMIC_SLIME_SIZE, 1);
        entityData.define(FIREBAT_HANGING, false);
        entityData.define(CONSTRUCT_HEADLESS, false);
        entityData.define(CONSTRUCT_ATTACK_TIMER, 0);
        entityData.define(CONSTRUCT_RECOVERY_TIMER, 0);
        entityData.define(CONSTRUCT_BEAM_CHARGE, 0);
        entityData.define(CRAB_HELM, false);
    }

    public void setWarpIllusion(Player viewer) {
        entityData.set(HARMLESS, true);
        entityData.set(WARP_VIEWER, Optional.of(viewer.getUUID()));
        xpReward = 0;
    }

    public boolean isWarpIllusion() {
        return entityData.get(HARMLESS);
    }

    public int thaumicSlimeSize() {
        return entityData.get(THAUMIC_SLIME_SIZE);
    }

    public void setThaumicSlimeSize(int size) {
        if (kind != LegacyMobKind.THAUMIC_SLIME) {
            return;
        }
        int clamped = Mth.clamp(size, 1, 100);
        entityData.set(THAUMIC_SLIME_SIZE, clamped);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) {
            health.setBaseValue(clamped);
        }
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) {
            damage.setBaseValue(clamped);
        }
        setHealth(clamped);
        xpReward = Math.max(1, (int) Math.sqrt(clamped));
        refreshDimensions();
    }

    public boolean isFirebatHanging() {
        return entityData.get(FIREBAT_HANGING);
    }

    public void configureFocusBat(UUID owner, boolean devil, boolean explosive,
                                  boolean vampire, int potency) {
        if (kind != LegacyMobKind.FIREBAT) return;
        focusBatOwner = owner;
        focusBatSummoned = true;
        focusBatExplosive = explosive;
        focusBatVampire = vampire;
        setFirebatHanging(false);
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.setBaseValue((devil ? 5.0D : 2.0D) + potency);
        if (devil) {
            var health = getAttribute(Attributes.MAX_HEALTH);
            if (health != null) health.setBaseValue(20.0D);
            setHealth(20.0F);
        }
    }

    private void setFirebatHanging(boolean hanging) {
        entityData.set(FIREBAT_HANGING, hanging);
        if (hanging) {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (entityData.get(HARMLESS)) {
            return false;
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            if (constructRecoveryTimer() > 0
                    || constructAttackTimer() > 0) {
                return false;
            }
            entityData.set(
                    CONSTRUCT_ATTACK_TIMER,
                    EldritchConstructBehavior.MELEE_COOLDOWN_TICKS
            );
            level().broadcastEntityEvent(this, (byte) 4);
            boolean hit = target.hurt(
                    damageSources().mobAttack(this),
                    (float) getAttributeValue(Attributes.ATTACK_DAMAGE)
                            * EldritchConstructBehavior
                                    .MELEE_DAMAGE_MULTIPLIER
            );
            if (hit) {
                target.setDeltaMovement(
                        target.getDeltaMovement().add(
                                0.0D,
                                EldritchConstructBehavior
                                        .MELEE_VERTICAL_LIFT,
                                0.0D
                        )
                );
                if (isConstructHeadless()) {
                    float yaw = getYRot() * Mth.DEG_TO_RAD;
                    target.push(
                            -Mth.sin(yaw)
                                    * EldritchConstructBehavior
                                            .HEADLESS_KNOCKBACK,
                            EldritchConstructBehavior
                                    .HEADLESS_KNOCKBACK_Y,
                            Mth.cos(yaw)
                                    * EldritchConstructBehavior
                                            .HEADLESS_KNOCKBACK
                    );
                }
            }
            return hit;
        }
        boolean hit = super.doHurtTarget(target);
        if (hit && kind == LegacyMobKind.FIREBAT && focusBatVampire
                && focusBatOwner != null && level() instanceof ServerLevel server) {
            Player found = server.getPlayerByUUID(focusBatOwner);
            if (found instanceof ServerPlayer owner) owner.heal(1.0F);
        }
        if (kind == LegacyMobKind.CRIMSON_INQUISITOR
                && target instanceof Player player
                && player.isBlocking()
                && player.getUseItem().is(Items.SHIELD)) {
            player.disableShield(true);
        }
        if (hit && kind == LegacyMobKind.ELDRITCH_CRAB) {
            playSound(ModSounds.CRAB_CLAW.get(), 1.0F, getVoicePitch());
        } else if (hit && kind == LegacyMobKind.TAINT_SWARM) {
            playSound(
                    ModSounds.SWARM_ATTACK.get(),
                    0.3F,
                    0.9F + getRandom().nextFloat() * 0.2F
            );
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(
                        MobEffects.CONFUSION,
                        100,
                        0
                ));
            }
        }
        return hit;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (kind == LegacyMobKind.PECH && (event == 18 || event == 19)) {
            ParticleOptions particle = event == 18
                    ? ParticleTypes.HAPPY_VILLAGER
                    : ParticleTypes.ANGRY_VILLAGER;
            for (int index = 0; index < 5; index++) {
                level().addParticle(
                        particle,
                        getRandomX(1.0D),
                        getRandomY(),
                        getRandomZ(1.0D),
                        getRandom().nextGaussian() * 0.02D,
                        getRandom().nextGaussian() * 0.02D,
                        getRandom().nextGaussian() * 0.02D
                );
            }
            return;
        }
        if (event == 4 && kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            entityData.set(
                    CONSTRUCT_ATTACK_TIMER,
                    EldritchConstructBehavior.MELEE_COOLDOWN_TICKS
            );
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
            return;
        }
        if (event == 19 && kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            spawnConstructChargeArc();
            return;
        }
        if (event == 61) {
            for (int index = 0; index < 32; index++) {
                level().addParticle(
                        ParticleTypes.POOF,
                        getX() + (getRandom().nextDouble() - 0.5D) * 1.5D,
                        getY() + 2.5D + getRandom().nextDouble(),
                        getZ() + (getRandom().nextDouble() - 0.5D) * 1.5D,
                        0.0D,
                        0.05D,
                        0.0D
                );
            }
            return;
        }
        super.handleEntityEvent(event);
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return kind == LegacyMobKind.MIND_SPIDER
                || kind == LegacyMobKind.FIREBAT
                || super.isIgnoringBlockTriggers();
    }

    @Override
    public boolean isPushable() {
        return kind != LegacyMobKind.FIREBAT
                && (kind != LegacyMobKind.ELDRITCH_CONSTRUCT
                        || constructRecoveryTimer() <= 0)
                && super.isPushable();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (constructBossEvent != null && shouldDisplayBossBar()) {
            constructBossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (constructBossEvent != null) {
            constructBossEvent.removePlayer(player);
        }
    }

    private boolean shouldDisplayBossBar() {
        return kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                || getPersistentData().getBoolean("OuterLandsBoss");
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return kind != LegacyMobKind.ELDRITCH_CONSTRUCT
                && super.removeWhenFarAway(distanceSquared);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                ? air
                : super.decreaseAirSupply(air);
    }

    @Override
    public boolean isInvisibleTo(Player viewer) {
        Optional<UUID> warpViewer = entityData.get(WARP_VIEWER);
        if (entityData.get(HARMLESS)
                && warpViewer.isPresent()
                && !warpViewer.get().equals(viewer.getUUID())) {
            return true;
        }
        return super.isInvisibleTo(viewer);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    /**
     * Mob invokes registerGoals from its constructor, before this subclass can
     * assign kind. Install kind-dependent goals only after super returns.
     */
    private void registerKindGoals() {
        if (kind == LegacyMobKind.TAINT_SPORE
                || kind == LegacyMobKind.TAINT_SPORE_SWARMER) {
            // TC4 spores are rooted entities: no navigation, melee or target
            // selection is installed for either the growing spore or swarmer.
            return;
        }
        if (kind.flying()) {
            if (kind == LegacyMobKind.WISP) {
                goalSelector.addGoal(1, new WispZapGoal(this));
            } else if (kind == LegacyMobKind.FIREBAT) {
                goalSelector.addGoal(1, new FirebatAttackGoal(this));
            } else {
                goalSelector.addGoal(
                        6,
                        new WaterAvoidingRandomFlyingGoal(this, 1.0D)
                );
            }
        } else if (kind.speed() > 0.0D) {
            if (kind == LegacyMobKind.FURIOUS_ZOMBIE) {
                goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
            }
            if (kind == LegacyMobKind.MIND_SPIDER) {
                goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
            }
            if (kind == LegacyMobKind.ELDRITCH_CRAB) {
                goalSelector.addGoal(
                        2,
                        new LeapAtTargetGoal(this, 0.63F)
                );
            }
            if (kind == LegacyMobKind.CRIMSON_CLERIC) {
                goalSelector.addGoal(1, new CrimsonAltarFocusGoal(this));
            }
            if (kind == LegacyMobKind.ELDRITCH_GUARDIAN) {
                goalSelector.addGoal(
                        2,
                        new EldritchGuardianRangedGoal(this)
                );
            }
            if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
                goalSelector.addGoal(
                        2,
                        new EldritchConstructRangedGoal(this)
                );
            }
            if (kind == LegacyMobKind.PECH) {
                goalSelector.addGoal(1, new PechTradeGoal(this));
                goalSelector.addGoal(2, new PechRangedGoal(this));
                goalSelector.addGoal(2, new PechMeleeGoal(this));
                goalSelector.addGoal(3, new PechPickupGoal(this));
                goalSelector.addGoal(
                        4,
                        new AvoidEntityGoal<>(
                                this,
                                Player.class,
                                8.0F,
                                0.5D,
                                0.6D,
                                player -> !isPechTamed()
                        )
                );
            }
            if (kind != LegacyMobKind.THAUMIC_SLIME
                    && kind != LegacyMobKind.PECH
                    && kind != LegacyMobKind.CONVERTED_VILLAGER) {
                goalSelector.addGoal(
                        3,
                        new MeleeAttackGoal(
                                this,
                                HostileAiBehavior.MELEE_PURSUIT_SPEED,
                                false
                        )
                );
            }
            if (kind == LegacyMobKind.CRIMSON_KNIGHT
                    || kind == LegacyMobKind.CRIMSON_INQUISITOR
                    || kind == LegacyMobKind.CRIMSON_CLERIC
                    || kind == LegacyMobKind.ELDRITCH_GUARDIAN) {
                goalSelector.addGoal(
                        kind == LegacyMobKind.ELDRITCH_GUARDIAN ? 5 : 6,
                        new ReturnToCombatHomeGoal(this)
                );
            }
            goalSelector.addGoal(
                    7,
                    new WaterAvoidingRandomStrollGoal(this, 0.8D)
            );
        }
        if (kind != LegacyMobKind.PECH
                && kind != LegacyMobKind.WISP
                && kind != LegacyMobKind.CONVERTED_VILLAGER) {
            NearestAttackableTargetGoal<Player> playerTargetGoal =
                    new NearestAttackableTargetGoal<>(
                            this,
                            Player.class,
                            kind == LegacyMobKind.MIND_SPIDER
                                    ? 0
                                    : HostileAiBehavior
                                            .TARGET_CHECK_INTERVAL_TICKS,
                            true,
                            false,
                            null
                    );
            playerTargetGoal.setUnseenMemoryTicks(
                    HostileAiBehavior.UNSEEN_MEMORY_TICKS
            );
            targetSelector.addGoal(2, playerTargetGoal);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (kind == LegacyMobKind.TAINTED_CHICKEN) {
            taintedChickenFlap.tick(onGround());
        }
        if (!level().isClientSide && constructBossEvent != null) {
            constructBossEvent.setProgress(Mth.clamp(
                    getHealth() / getMaxHealth(),
                    0.0F,
                    1.0F
            ));
            constructBossEvent.setName(getDisplayName());
        }
        tickFuriousZombie();
        tickPech();
        tickFirebatParticles();
        tickFirebatState();
        tickWispFlight();
        tickThaumicSlime();
        tickMindSpider();
        tickEldritchConstruct();
        tickSwarmBuzz();
        tickTaintEcologyMob();
        if (level().isClientSide
                || tickCount
                        % CrimsonCultBehavior.RITUAL_CHECK_INTERVAL_TICKS != 0
                || crimsonAltarPosition().isPresent()
                || (kind != LegacyMobKind.CRIMSON_CLERIC
                        && kind != LegacyMobKind.CRIMSON_KNIGHT)) {
            return;
        }
        findNearbyCrimsonAltar().ifPresent(altar -> configureCrimsonAltar(
                altar,
                kind == LegacyMobKind.CRIMSON_CLERIC
        ));
    }

    /**
     * Interpolated TC4 wing roll input. On the ground its spread decays to
     * zero, so a resting tainted chicken does not flap forever.
     */
    public float taintedChickenWingBob(float partialTick) {
        return taintedChickenFlap.sample(partialTick);
    }

    private void tickPech() {
        if (kind != LegacyMobKind.PECH) {
            return;
        }
        if (!level().isClientSide) {
            int anger = pechAnger();
            if (anger > 0) {
                entityData.set(PECH_ANGER, anger - 1);
                if (anger == 1 && getTarget() instanceof Player) {
                    setTarget(null);
                }
            }
            if (tickCount % 40 == 0 && getHealth() < getMaxHealth()) {
                heal(1.0F);
            }
            if (pechTrading) {
                getNavigation().stop();
            }
            return;
        }
        if (pechAnger() > 0 && getRandom().nextInt(15) == 0) {
            level().addParticle(
                    ParticleTypes.ANGRY_VILLAGER,
                    getRandomX(1.0D),
                    getRandomY(),
                    getRandomZ(1.0D),
                    0.0D,
                    0.0D,
                    0.0D
            );
        } else if (isPechTamed() && getRandom().nextInt(25) == 0) {
            level().addParticle(
                    ParticleTypes.HAPPY_VILLAGER,
                    getRandomX(1.0D),
                    getRandomY(),
                    getRandomZ(1.0D),
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private void tickEldritchConstruct() {
        if (kind != LegacyMobKind.ELDRITCH_CONSTRUCT) {
            return;
        }
        if (constructAttackTimer() > 0) {
            entityData.set(
                    CONSTRUCT_ATTACK_TIMER,
                    constructAttackTimer() - 1
            );
        }
        int recovery = constructRecoveryTimer();
        if (recovery > 0) {
            entityData.set(CONSTRUCT_RECOVERY_TIMER, recovery - 1);
            getNavigation().stop();
            if (!level().isClientSide) {
                heal(EldritchConstructBehavior.SPAWN_HEAL_PER_TICK);
            }
        }
        if (level().isClientSide) {
            spawnConstructWalkingDebris();
            tickConstructClientEffects();
            return;
        }
        breakConstructObstruction();
        if (constructAngerTicks > 0) {
            constructAngerTicks--;
        }
        if (constructBossEvent != null) {
            constructBossEvent.setProgress(Mth.clamp(
                    getHealth() / getMaxHealth(),
                    0.0F,
                    1.0F
            ));
            constructBossEvent.setName(getDisplayName());
        }
        if (tickCount
                % EldritchConstructBehavior
                        .PASSIVE_HEAL_INTERVAL_TICKS == 0) {
            heal(EldritchConstructBehavior.PASSIVE_HEAL);
        }
        if (getTarget() != null && tickCount % 20 == 0) {
            updateConstructAggroAndScaling();
        }
        if (!isConstructHeadless()) {
            return;
        }
        if (constructBeamCharge() <= 0) {
            constructChargingBeam = true;
        }
        if (constructChargingBeam) {
            int nextCharge = constructBeamCharge() + 1;
            entityData.set(CONSTRUCT_BEAM_CHARGE, nextCharge);
            level().broadcastEntityEvent(this, (byte) 19);
            if (nextCharge >= EldritchConstructBehavior.BEAM_MAX_CHARGE) {
                constructChargingBeam = false;
            }
        }
    }

    private void spawnConstructWalkingDebris() {
        Vec3 movement = getDeltaMovement();
        if (movement.x * movement.x + movement.z * movement.z
                <= 2.500000277905201E-7D
                || getRandom().nextInt(5) != 0) {
            return;
        }
        BlockPos foot = BlockPos.containing(
                getX(),
                getBoundingBox().minY - 0.2D,
                getZ()
        );
        BlockState state = level().getBlockState(foot);
        if (state.isAir()) {
            return;
        }
        level().addParticle(
                new BlockParticleOption(
                        ParticleTypes.BLOCK,
                        state
                ),
                getX() + (getRandom().nextFloat() - 0.5D) * getBbWidth(),
                getBoundingBox().minY + 0.1D,
                getZ() + (getRandom().nextFloat() - 0.5D) * getBbWidth(),
                4.0D * (getRandom().nextFloat() - 0.5D),
                0.5D,
                4.0D * (getRandom().nextFloat() - 0.5D)
        );
    }

    private void breakConstructObstruction() {
        Vec3 movement = getDeltaMovement();
        BlockPos ahead = BlockPos.containing(
                getX() + movement.x,
                getBoundingBox().minY,
                getZ() + movement.z
        );
        BlockState state = level().getBlockState(ahead);
        float hardness = state.getDestroySpeed(level(), ahead);
        if (!state.isAir() && hardness >= 0.0F && hardness <= 0.15F) {
            level().destroyBlock(ahead, true, this);
        }
    }

    private void updateConstructAggroAndScaling() {
        LivingEntity currentTarget = getTarget();
        if (currentTarget == null) {
            return;
        }
        int currentAggro = constructAggro.getOrDefault(
                currentTarget.getId(),
                0
        );
        int highestAggro = currentAggro;
        int players = 0;
        LivingEntity newTarget = null;
        List<Integer> stale = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : constructAggro.entrySet()) {
            Entity candidate = level().getEntity(entry.getKey());
            if (!(candidate instanceof LivingEntity living)
                    || !living.isAlive()
                    || distanceToSqr(living) > 16384.0D) {
                stale.add(entry.getKey());
                continue;
            }
            if (living instanceof Player) {
                players++;
            }
            int candidateAggro = entry.getValue();
            if (candidateAggro > currentAggro + 25
                    && candidateAggro > currentAggro * 1.1D
                    && candidateAggro > highestAggro) {
                highestAggro = candidateAggro;
                newTarget = living;
            }
        }
        stale.forEach(constructAggro::remove);
        if (newTarget != null && newTarget != currentTarget) {
            setTarget(newTarget);
        }
        int extraPlayers = Mth.clamp(players - 1, 0, 5);
        double desiredMaxHealth = kind.health() + extraPlayers * 50.0D;
        double desiredDamage = kind.damage() + extraPlayers * 0.5D;
        if (getMaxHealth() == (float) desiredMaxHealth
                && getAttributeValue(Attributes.ATTACK_DAMAGE)
                        == desiredDamage) {
            return;
        }
        float oldMaxHealth = getMaxHealth();
        float healthRatio = oldMaxHealth > 0.0F
                ? getHealth() / oldMaxHealth
                : 1.0F;
        restoreLegacyBaseAttribute(Attributes.MAX_HEALTH, desiredMaxHealth);
        restoreLegacyBaseAttribute(Attributes.ATTACK_DAMAGE, desiredDamage);
        setHealth(Mth.clamp(
                getMaxHealth() * healthRatio,
                1.0F,
                getMaxHealth()
        ));
    }

    private void tickConstructClientEffects() {
        if (!isConstructHeadless()) {
            return;
        }
        float yaw = -yBodyRot * Mth.DEG_TO_RAD - Mth.PI;
        Vec3 forward = new Vec3(
                Mth.sin(yaw),
                0.0D,
                Mth.cos(yaw)
        );
        double ventX = getX() + forward.x * 0.66D;
        double ventY = getY() + getEyeHeight() - 0.75D;
        double ventZ = getZ() + forward.z * 0.66D;
        level().addParticle(
                ParticleTypes.SMOKE,
                ventX,
                ventY,
                ventZ,
                0.0D,
                0.001D,
                0.0D
        );
        if (getRandom().nextInt(20) == 0) {
            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    getX() + forward.x
                            + (getRandom().nextFloat()
                                    - getRandom().nextFloat()) * 0.5D,
                    getY() + getEyeHeight() - 0.25D,
                    getZ() + forward.z
                            + (getRandom().nextFloat()
                                    - getRandom().nextFloat()) * 0.5D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
        if (constructArcTicks <= 0 || constructArcTarget == null) {
            return;
        }
        constructArcTicks--;
        Vec3 start = position().add(0.0D, getBbHeight() * 0.5D, 0.0D);
        Vec3 end = Vec3.atCenterOf(constructArcTarget)
                .add(0.0D, 0.5D, 0.0D);
        for (int step = 0; step <= 12; step++) {
            double progress = step / 12.0D;
            Vec3 point = start.lerp(end, progress);
            double jitter = step == 0 || step == 12
                    ? 0.0D
                    : 0.12D;
            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    point.x + (getRandom().nextDouble() - 0.5D) * jitter,
                    point.y + (getRandom().nextDouble() - 0.5D) * jitter,
                    point.z + (getRandom().nextDouble() - 0.5D) * jitter,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    private void spawnConstructChargeArc() {
        if (constructArcTicks > 0) {
            return;
        }
        float radius = 2.0F + getRandom().nextFloat() * 2.0F;
        double radians = Math.toRadians(getRandom().nextInt(360));
        BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos(
                Mth.floor(getX() + radius * Math.cos(radians)),
                Mth.floor(getY()),
                Mth.floor(getZ() + radius * Math.sin(radians))
        );
        for (int depth = 0;
                depth < 5 && level().getBlockState(target).isAir();
                depth++) {
            target.move(0, -1, 0);
        }
        constructArcTarget = target.immutable();
        constructArcTicks = 8 + getRandom().nextInt(5);
        playSound(
                ModSounds.JACOBS.get(),
                0.8F,
                1.0F + (getRandom().nextFloat() - getRandom().nextFloat())
                        * 0.05F
        );
    }

    private void tickSwarmBuzz() {
        if (kind == LegacyMobKind.TAINT_SWARM
                && !level().isClientSide
                && getRandom().nextInt(50) == 0) {
            playSound(
                    ModSounds.FLY.get(),
                    0.03F,
                    0.5F + getRandom().nextFloat() * 0.4F
            );
        }
    }

    private void tickTaintEcologyMob() {
        if (level().isClientSide) {
            return;
        }
        if (kind == LegacyMobKind.TAINT_SPORE_SWARMER) {
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            if (taintSwarmSpawnCounter > 0) {
                taintSwarmSpawnCounter--;
            }
            if (taintSwarmSpawnCounter <= 0
                    && level().getNearestPlayer(this, 16.0D) != null) {
                taintSwarmSpawnCounter = 500;
                spawnTaintSwarm();
            }
            return;
        }
        if (kind != LegacyMobKind.TAINT_SPORE) {
            return;
        }
        getNavigation().stop();
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        if (tickCount % 20 == 0
                && !level().getBiome(blockPosition()).is(
                        ModWorldgenKeys.TAINTED_LANDS)) {
            hurt(damageSources().drown(), 1.0F);
            return;
        }
        if (taintSporeSize < 10 && ++taintSporeGrowth >= 1200) {
            taintSporeSize++;
            taintSporeGrowth = 0;
        }
        if (!level().getBlockState(blockPosition().below())
                .is(com.thaumcraftmodern.registry.ModBlocks
                        .MATURE_SPORE_STALK.get())) {
            burstSporeIntoCrawlers();
        }
    }

    private void spawnTaintSwarm() {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        LegacyThaumcraftMob swarm = ModEntities.TAINT_SWARM.get()
                .create(server);
        if (swarm == null) {
            return;
        }
        swarm.moveTo(getX(), getY() + 0.5D, getZ(),
                getRandom().nextFloat() * 360.0F, 0.0F);
        swarm.finalizeSpawn(server, server.getCurrentDifficultyAt(
                blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        server.addFreshEntity(swarm);
        playSound(ModSounds.GORE.get(), 1.0F,
                0.9F + getRandom().nextFloat() * 0.1F);
    }

    private void burstSporeIntoCrawlers() {
        if (!(level() instanceof ServerLevel server) || isRemoved()) {
            return;
        }
        int amount = taintSporeSize / 3
                + getRandom().nextInt(taintSporeSize / 2 + 1);
        for (int index = 0; index < amount; index++) {
            LegacyThaumcraftMob crawler = ModEntities.TAINTED_CRAWLER.get()
                    .create(server);
            if (crawler == null) {
                continue;
            }
            crawler.moveTo(
                    getX() + getRandom().nextFloat()
                            - getRandom().nextFloat(),
                    getY() + getRandom().nextFloat(),
                    getZ() + getRandom().nextFloat()
                            - getRandom().nextFloat(),
                    getRandom().nextFloat() * 360.0F,
                    0.0F
            );
            crawler.finalizeSpawn(server, server.getCurrentDifficultyAt(
                    blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            server.addFreshEntity(crawler);
        }
        BlockPos below = blockPosition().below();
        if (server.getBlockState(below).is(
                com.thaumcraftmodern.registry.ModBlocks
                        .MATURE_SPORE_STALK.get())) {
            server.setBlock(below,
                    com.thaumcraftmodern.registry.ModBlocks.SPORE_STALK.get()
                            .defaultBlockState(), 3);
        }
        playSound(ModSounds.GORE.get(), 1.0F,
                0.9F + getRandom().nextFloat() * 0.1F);
        discard();
    }

    private void tickFuriousZombie() {
        if (kind != LegacyMobKind.FURIOUS_ZOMBIE
                || level().isClientSide) {
            return;
        }
        if (furiousAnger() > FuriousZombieBehavior.INITIAL_ANGER) {
            setFuriousAnger(FuriousZombieBehavior.afterTick(
                    furiousAnger()
            ));
        }
        var attackDamage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null) {
            attackDamage.setBaseValue(
                    FuriousZombieBehavior.attackDamage(furiousAnger())
            );
        }
    }

    private void tickFirebatParticles() {
        if (kind != LegacyMobKind.FIREBAT || !level().isClientSide) {
            return;
        }
        spawnFirebatParticle(ParticleTypes.SMOKE);
        spawnFirebatParticle(ParticleTypes.FLAME);
    }

    private void tickFirebatState() {
        if (kind != LegacyMobKind.FIREBAT) {
            return;
        }
        if (firebatAttackCooldown > 0) {
            firebatAttackCooldown--;
        }
        if (level().isClientSide) {
            return;
        }
        if (focusBatSummoned && (getTarget() == null || !getTarget().isAlive())) {
            discard();
            return;
        }
        BlockPos above = BlockPos.containing(
                getX(),
                getBoundingBox().maxY + 0.1D,
                getZ()
        );
        if (isFirebatHanging()) {
            setDeltaMovement(Vec3.ZERO);
            setPos(
                    getX(),
                    Mth.floor(getY()) + 1.0D - getBbHeight(),
                    getZ()
            );
            if (level().getBlockState(above).isAir()
                    || !level().getFluidState(above).isEmpty()
                    || level().getNearestPlayer(this, 4.0D) != null) {
                setFirebatHanging(false);
                level().levelEvent(null, 1015, blockPosition(), 0);
            } else if (getRandom().nextInt(200) == 0) {
                setYRot(getRandom().nextFloat() * 360.0F);
            }
        } else if (getTarget() == null
                && getRandom().nextInt(100) == 0
                && !level().getBlockState(above).isAir()
                && level().getFluidState(above).isEmpty()) {
            setFirebatHanging(true);
        } else if (!isFirebatHanging() && getTarget() == null) {
            if (firebatFlightTarget == null
                    || getRandom().nextInt(30) == 0
                    || firebatFlightTarget.distToCenterSqr(
                            getX(),
                            getY(),
                            getZ()
                    ) < 4.0D) {
                firebatFlightTarget = BlockPos.containing(
                        getX() + getRandom().nextInt(7)
                                - getRandom().nextInt(7),
                        getY() + getRandom().nextInt(6) - 2,
                        getZ() + getRandom().nextInt(7)
                                - getRandom().nextInt(7)
                );
            }
            steerFirebatToward(
                    firebatFlightTarget.getX() + 0.5D,
                    firebatFlightTarget.getY() + 0.1D,
                    firebatFlightTarget.getZ() + 0.5D
            );
        }
        if (!isFirebatHanging()) {
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x, motion.y * 0.6D, motion.z);
        }
    }

    private void steerFirebatToward(double x, double y, double z) {
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(
                motion.x
                        + (Math.signum(x - getX()) * 0.5D - motion.x)
                                * 0.1D,
                motion.y
                        + (Math.signum(y - getY()) * 0.7D - motion.y)
                                * 0.1D,
                motion.z
                        + (Math.signum(z - getZ()) * 0.5D - motion.z)
                                * 0.1D
        );
    }

    private void tickMindSpider() {
        if (kind == LegacyMobKind.MIND_SPIDER
                && !level().isClientSide
                && isWarpIllusion()
                && tickCount > 1200) {
            discard();
        }
    }

    private void tickWispFlight() {
        if (kind != LegacyMobKind.WISP || level().isClientSide) {
            return;
        }
        Vec3 offset = wispWaypoint.subtract(position());
        double distanceSquared = offset.lengthSqr();
        if (distanceSquared < 1.0D || distanceSquared > 3600.0D) {
            wispWaypoint = position().add(
                    (getRandom().nextFloat() * 2.0F - 1.0F) * 16.0F,
                    (getRandom().nextFloat() * 2.0F - 1.0F) * 16.0F,
                    (getRandom().nextFloat() * 2.0F - 1.0F) * 16.0F
            );
            offset = wispWaypoint.subtract(position());
            distanceSquared = offset.lengthSqr();
        }
        if (wispCourseChangeCooldown-- > 0) {
            return;
        }
        wispCourseChangeCooldown += getRandom().nextInt(5) + 2;
        double distance = Math.sqrt(distanceSquared);
        if (distance <= 0.01D
                || !isWispCourseTraversable(offset, distance)) {
            wispWaypoint = position();
            return;
        }
        setDeltaMovement(getDeltaMovement().add(
                offset.x / distance * 0.1D,
                offset.y / distance * 0.1D,
                offset.z / distance * 0.1D
        ));
    }

    private boolean isWispCourseTraversable(
            Vec3 offset,
            double distance
    ) {
        AABB moved = getBoundingBox();
        Vec3 step = offset.scale(1.0D / distance);
        for (int index = 1; index < distance; index++) {
            moved = moved.move(step);
            if (!level().noCollision(this, moved)) {
                return false;
            }
        }
        return true;
    }

    private void tickThaumicSlime() {
        if (kind != LegacyMobKind.THAUMIC_SLIME
                || level().isClientSide) {
            return;
        }
        if (slimeLaunchedTicks > 0) {
            slimeLaunchedTicks--;
        }
        if (slimeAttackCooldown > 0) {
            slimeAttackCooldown--;
        }
        boolean grounded = onGround();
        if (grounded && !slimeWasOnGround && thaumicSlimeSize() > 5) {
            playSound(
                    SoundEvents.SLIME_SQUISH,
                    getSoundVolume(),
                    slimePitch(0.8F)
            );
        }
        slimeWasOnGround = grounded;
        LivingEntity target = getTarget();
        if (target == null) {
            Player nearby = level().getNearestPlayer(this, 16.0D);
            if (nearby != null && !nearby.getAbilities().invulnerable) {
                setTarget(nearby);
                target = nearby;
            }
        }
        if (target != null && target.isAlive()) {
            getLookControl().setLookAt(target, 10.0F, 20.0F);
            if (slimeSpitCooldown > 0) {
                slimeSpitCooldown--;
            }
            if (thaumicSlimeSize() > 3
                    && distanceTo(target) > 4.0F
                    && slimeSpitCooldown <= 0) {
                spitThaumicSlime(target);
                slimeSpitCooldown = 101;
            }
            double contact = 0.8D * Math.max(
                    1.0D,
                    Math.sqrt(thaumicSlimeSize())
            );
            if (slimeAttackCooldown <= 0
                    && distanceToSqr(target) < contact * contact) {
                doHurtTarget(target);
                slimeAttackCooldown = 10;
            }
        } else if (tickCount > 100) {
            mergeWithNearbySlime();
        }
        if (grounded && slimeJumpDelay-- <= 0) {
            slimeJumpDelay = getRandom().nextInt(16) + 8;
            if (target != null) {
                slimeJumpDelay = Math.max(8, slimeJumpDelay / 2);
            }
            Vec3 direction;
            if (target != null) {
                direction = target.position().subtract(position());
            } else if (slimeMergeTarget != null
                    && slimeMergeTarget.isAlive()) {
                direction = slimeMergeTarget.position()
                        .subtract(position());
            } else {
                direction = new Vec3(
                        getRandom().nextDouble() - 0.5D,
                        0.0D,
                        getRandom().nextDouble() - 0.5D
                );
            }
            Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
            if (horizontal.lengthSqr() > 1.0E-6D) {
                horizontal = horizontal.normalize().scale(
                        0.18D * Math.sqrt(thaumicSlimeSize())
                );
            }
            setDeltaMovement(horizontal.x, 0.42D, horizontal.z);
            hasImpulse = true;
            if (thaumicSlimeSize() > 3) {
                playSound(
                        SoundEvents.SLIME_SQUISH,
                        getSoundVolume(),
                        slimePitch(0.8F)
                );
            }
        }
    }

    private void spitThaumicSlime(LivingEntity target) {
        LegacyThaumcraftMob spit =
                ModEntities.THAUMIC_SLIME.get().create(level());
        if (spit == null) {
            return;
        }
        spit.setThaumicSlimeSize(1);
        spit.slimeLaunchedTicks = 10;
        Vec3 origin = new Vec3(
                getX(),
                getBoundingBox().getCenter().y,
                getZ()
        );
        Vec3 aim = target.position()
                .add(0.0D, target.getBbHeight() / 3.0D, 0.0D)
                .subtract(origin);
        Vec3 horizontal = new Vec3(aim.x, 0.0D, aim.z);
        if (horizontal.lengthSqr() > 1.0E-6D) {
            horizontal = horizontal.normalize();
        }
        spit.moveTo(
                origin.x + horizontal.x,
                origin.y,
                origin.z + horizontal.z,
                getYRot(),
                0.0F
        );
        Vec3 velocity = aim.normalize().add(
                0.0D,
                Math.sqrt(aim.x * aim.x + aim.z * aim.z) * 0.02D,
                0.0D
        ).normalize().scale(1.5D);
        spit.setDeltaMovement(velocity);
        level().addFreshEntity(spit);
        playSound(SoundEvents.SLIME_SQUISH, 1.0F, slimePitch(0.8F));
        setThaumicSlimeSize(thaumicSlimeSize() - 1);
    }

    private void mergeWithNearbySlime() {
        LegacyThaumcraftMob closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (LegacyThaumcraftMob candidate : level().getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                mob -> mob != this
                        && mob.kind == LegacyMobKind.THAUMIC_SLIME
                        && mob.tickCount > 100
                        && mob.thaumicSlimeSize() < 100
        )) {
            double distance = distanceToSqr(candidate);
            if (distance < closestDistance) {
                closest = candidate;
                closestDistance = distance;
            }
        }
        if (closest == null) {
            slimeMergeTarget = null;
            return;
        }
        slimeMergeTarget = closest;
        if (getBoundingBox().intersects(closest.getBoundingBox())) {
            closest.setThaumicSlimeSize(Math.min(
                    100,
                    closest.thaumicSlimeSize() + thaumicSlimeSize()
            ));
            discard();
        }
    }

    private float slimePitch(float multiplier) {
        return ((getRandom().nextFloat() - getRandom().nextFloat())
                * 0.2F + 1.0F) * multiplier;
    }

    private void renderWispZap(LivingEntity target) {
        if (!(level() instanceof ServerLevel)) {
            return;
        }
        ModNetwork.sendToTracking(
                this,
                new WispZapPacket(
                        getId(),
                        target.getId(),
                        getRandom().nextLong()
                )
        );
    }

    private void spawnFirebatParticle(ParticleOptions particle) {
        double x = getX() + (getRandom().nextFloat()
                - getRandom().nextFloat()) * 0.2D;
        double y = getY() + getBbHeight() * 0.5D;
        double z = getZ() + (getRandom().nextFloat()
                - getRandom().nextFloat()) * 0.2D;
        level().addParticle(
                particle,
                x,
                y,
                z,
                0.0D,
                0.0D,
                0.0D
        );
    }

    private Optional<BlockPos> findNearbyCrimsonAltar() {
        BlockPos origin = blockPosition();
        BlockPos closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (BlockPos candidate : BlockPos.betweenClosed(
                origin.offset(-6, -3, -6),
                origin.offset(6, 3, 6)
        )) {
            var state = level().getBlockState(candidate);
            if (!state.is(com.thaumcraftmodern.registry.ModBlocks
                    .ELDRITCH_ALTAR_PART.get())
                    || state.getValue(EldritchAltarPartBlock.PART) != 0) {
                continue;
            }
            double distance = candidate.distSqr(origin);
            if (distance < closestDistance) {
                closest = candidate.immutable();
                closestDistance = distance;
            }
        }
        return Optional.ofNullable(closest);
    }

    public void configureCrimsonAltar(
            BlockPos altarPosition,
            boolean ritualist
    ) {
        if (kind != LegacyMobKind.CRIMSON_CLERIC
                && kind != LegacyMobKind.CRIMSON_KNIGHT) {
            return;
        }
        BlockPos altar = Objects.requireNonNull(
                altarPosition,
                "altarPosition"
        ).immutable();
        entityData.set(CRIMSON_ALTAR, Optional.of(altar));
        entityData.set(
                CRIMSON_RITUALIST,
                kind == LegacyMobKind.CRIMSON_CLERIC && ritualist
        );
    }

    public void configureEldritchAltarGuard(BlockPos altarPosition) {
        if (kind != LegacyMobKind.ELDRITCH_GUARDIAN) {
            return;
        }
        eldritchAltarHome = Objects.requireNonNull(
                altarPosition,
                "altarPosition"
        ).immutable();
    }

    @Override
    public void performRangedAttack(
            LivingEntity target,
            float distanceFactor
    ) {
        if (level().isClientSide) {
            return;
        }
        if (kind == LegacyMobKind.PECH) {
            performPechRangedAttack(target, distanceFactor);
            return;
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            performConstructRangedAttack(target);
            return;
        }
        if (kind != LegacyMobKind.ELDRITCH_GUARDIAN) {
            return;
        }
        if (getRandom().nextFloat()
                < EldritchGuardianBehavior.ORB_ATTACK_CHANCE) {
            EldritchOrbEntity orb = new EldritchOrbEntity(this, level());
            double side = getRandom().nextBoolean() ? -0.5D : 0.5D;
            double yaw = Math.toRadians(getYRot());
            orb.setPos(
                    getX() + Math.cos(yaw) * side,
                    getY() + getBbHeight() * 0.44D,
                    getZ() + Math.sin(yaw) * side
            );
            double dx = target.getX() + target.getDeltaMovement().x
                    - orb.getX();
            double dy = target.getY() + target.getBbHeight() * 0.5D
                    - orb.getY();
            double dz = target.getZ() + target.getDeltaMovement().z
                    - orb.getZ();
            orb.shoot(dx, dy, dz, 1.0F, 2.0F);
            playSound(
                    ModSounds.EG_ATTACK.get(),
                    2.0F,
                    1.0F + getRandom().nextFloat() * 0.1F
            );
            level().addFreshEntity(orb);
            return;
        }
        if (!getSensing().hasLineOfSight(target)) {
            return;
        }
        target.addEffect(new MobEffectInstance(
                MobEffects.BLINDNESS,
                EldritchGuardianBehavior.SCREECH_BLINDNESS_TICKS,
                0
        ));
        if (target instanceof ServerPlayer player) {
            int range = EldritchGuardianBehavior
                    .SCREECH_MAX_TEMPORARY_WARP
                    - EldritchGuardianBehavior
                            .SCREECH_MIN_TEMPORARY_WARP
                    + 1;
            ResearchProgressService.addWarp(
                    player,
                    WarpType.TEMPORARY,
                    EldritchGuardianBehavior
                            .SCREECH_MIN_TEMPORARY_WARP
                            + getRandom().nextInt(range),
                    "eldritch_guardian_screech"
            );
        }
        playSound(
                ModSounds.EG_SCREECH.get(),
                3.0F,
                1.0F + getRandom().nextFloat() * 0.1F
        );
    }

    private void performPechRangedAttack(
            LivingEntity target,
            float distanceFactor
    ) {
        if (pechType() == PechBehavior.STALKER) {
            Arrow arrow = new Arrow(level(), this);
            double dx = target.getX() - getX();
            double dy = target.getY() + target.getBbHeight() / 3.0D
                    - arrow.getY();
            double dz = target.getZ() - getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(
                    dx,
                    dy + horizontal * 0.2D,
                    dz,
                    1.6F,
                    14.0F - level().getDifficulty().getId() * 4.0F
            );
            arrow.setBaseDamage(
                    distanceFactor * 2.0F
                            + getRandom().nextGaussian() * 0.25D
                            + level().getDifficulty().getId() * 0.11F
            );
            playSound(
                    SoundEvents.ARROW_SHOOT,
                    1.0F,
                    1.0F / (getRandom().nextFloat() * 0.4F + 0.8F)
            );
            level().addFreshEntity(arrow);
            swing(InteractionHand.MAIN_HAND);
            return;
        }
        if (pechType() == PechBehavior.MAGE) {
            PechBlastEntity blast = new PechBlastEntity(
                    this,
                    getRandom().nextFloat() < 0.1F,
                    level()
            );
            double dx = target.getX() + target.getDeltaMovement().x - getX();
            double dy = target.getY() + target.getEyeHeight()
                    - 1.500000023841858D - getY();
            double dz = target.getZ() + target.getDeltaMovement().z - getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            blast.shoot(
                    dx,
                    dy + horizontal * 0.1D,
                    dz,
                    1.5F,
                    4.0F
            );
            playSound(
                    SoundEvents.GLASS_BREAK,
                    0.4F,
                    1.0F + getRandom().nextFloat() * 0.1F
            );
            level().addFreshEntity(blast);
            swing(InteractionHand.MAIN_HAND);
        }
    }

    private void performConstructRangedAttack(LivingEntity target) {
        if (!isConstructHeadless()
                || constructRecoveryTimer() > 0
                || constructChargingBeam
                || constructBeamCharge() <= 0
                || !getSensing().hasLineOfSight(target)) {
            return;
        }
        entityData.set(
                CONSTRUCT_BEAM_CHARGE,
                constructBeamCharge()
                        - EldritchConstructBehavior.BEAM_SHOT_COST_MIN
                        - getRandom().nextInt(
                                EldritchConstructBehavior
                                        .BEAM_SHOT_COST_VARIANCE
                        )
        );
        getLookControl().setLookAt(target, 30.0F, 30.0F);
        Vec3 look = getLookAngle();
        EldritchOrbEntity orb = new EldritchOrbEntity(
                this,
                target,
                level()
        );
        orb.setPos(
                orb.getX() + look.x,
                orb.getY(),
                orb.getZ() + look.z
        );
        double dx = target.getX() + target.getDeltaMovement().x - getX();
        double dy = target.getY() - getY()
                - target.getBbHeight() * 0.5D;
        double dz = target.getZ() + target.getDeltaMovement().z - getZ();
        orb.shoot(
                dx,
                dy,
                dz,
                EldritchConstructBehavior.ORB_VELOCITY,
                EldritchConstructBehavior.ORB_INACCURACY
        );
        playSound(
                ModSounds.EG_ATTACK.get(),
                1.0F,
                1.0F + getRandom().nextFloat() * 0.1F
        );
        level().addFreshEntity(orb);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return switch (kind) {
            case CONVERTED_VILLAGER -> SoundEvents.VILLAGER_AMBIENT;
            case ELDRITCH_GUARDIAN -> ModSounds.EG_IDLE.get();
            case CRIMSON_CLERIC -> ModSounds.CULTIST_CHANT.get();
            case WISP -> ModSounds.WISP_LIVE.get();
            case FIREBAT -> SoundEvents.BAT_AMBIENT;
            case PECH -> ModSounds.PECH_IDLE.get();
            case ELDRITCH_CRAB -> ModSounds.CRAB_TALK.get();
            case THAUMIC_SLIME -> SoundEvents.SLIME_SQUISH;
            case MIND_SPIDER, TAINTED_CRAWLER ->
                    SoundEvents.SPIDER_AMBIENT;
            case TAINT_SPORE -> ModSounds.SWARM.get();
            case TAINT_SPORE_SWARMER -> ModSounds.ROOTS.get();
            default -> super.getAmbientSound();
        };
    }

    @Override
    public void playAmbientSound() {
        if (kind == LegacyMobKind.PECH && !level().isClientSide) {
            if (getRandom().nextInt(3) == 0
                    && !level().getEntitiesOfClass(
                            LegacyThaumcraftMob.class,
                            getBoundingBox().inflate(4.0D, 2.0D, 4.0D),
                            mob -> mob != this
                                    && mob.kind == LegacyMobKind.PECH
                    ).isEmpty()) {
                level().broadcastEntityEvent(this, (byte) 17);
                playSound(
                        ModSounds.PECH_TRADE.get(),
                        getSoundVolume(),
                        getVoicePitch()
                );
                return;
            }
            level().broadcastEntityEvent(this, (byte) 16);
        }
        super.playAmbientSound();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return switch (kind) {
            case CONVERTED_VILLAGER -> SoundEvents.VILLAGER_HURT;
            case WISP -> SoundEvents.FIRE_EXTINGUISH;
            case FIREBAT -> SoundEvents.BAT_HURT;
            case PECH -> ModSounds.PECH_HIT.get();
            case ELDRITCH_CONSTRUCT -> SoundEvents.IRON_GOLEM_HURT;
            case ELDRITCH_CRAB -> SoundEvents.GUARDIAN_HURT;
            case TAINT_SWARM -> ModSounds.SWARM_ATTACK.get();
            case THAUMIC_SLIME -> SoundEvents.SLIME_HURT;
            case MIND_SPIDER, TAINTED_CRAWLER ->
                    SoundEvents.SPIDER_HURT;
            case TAINT_SPORE, TAINT_SPORE_SWARMER ->
                    ModSounds.GORE.get();
            default -> super.getHurtSound(source);
        };
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return switch (kind) {
            case CONVERTED_VILLAGER -> SoundEvents.VILLAGER_DEATH;
            case ELDRITCH_GUARDIAN -> ModSounds.EG_DEATH.get();
            case WISP -> ModSounds.WISP_DEAD.get();
            case FIREBAT -> SoundEvents.BAT_DEATH;
            case PECH -> ModSounds.PECH_DEATH.get();
            case ELDRITCH_CONSTRUCT -> SoundEvents.IRON_GOLEM_DEATH;
            case ELDRITCH_CRAB -> ModSounds.CRAB_DEATH.get();
            case TAINT_SWARM -> ModSounds.SWARM_ATTACK.get();
            case THAUMIC_SLIME -> SoundEvents.SLIME_DEATH;
            case MIND_SPIDER, TAINTED_CRAWLER ->
                    SoundEvents.SPIDER_DEATH;
            case TAINT_SPORE, TAINT_SPORE_SWARMER ->
                    ModSounds.GORE.get();
            default -> super.getDeathSound();
        };
    }

    @Override
    protected float getSoundVolume() {
        return switch (kind) {
            case ELDRITCH_GUARDIAN -> 1.5F;
            case FIREBAT, TAINT_SWARM -> 0.1F;
            case TAINT_SPORE, TAINT_SPORE_SWARMER -> 0.1F;
            case THAUMIC_SLIME ->
                    0.1F * (float) Math.sqrt(thaumicSlimeSize());
            default -> super.getSoundVolume();
        };
    }

    @Override
    public float getVoicePitch() {
        return switch (kind) {
            case CONVERTED_VILLAGER -> 0.72F;
            case WISP -> 0.25F;
            case MIND_SPIDER, TAINTED_CRAWLER -> 0.7F;
            case FIREBAT -> (getRandom().nextFloat()
                    - getRandom().nextFloat()) * 0.2F + 1.0F;
            default -> super.getVoicePitch();
        };
    }

    public boolean isCrimsonRitualist() {
        return kind == LegacyMobKind.CRIMSON_CLERIC
                && entityData.get(CRIMSON_RITUALIST);
    }

    public Optional<BlockPos> crimsonAltarPosition() {
        return entityData.get(CRIMSON_ALTAR);
    }

    private void stopCrimsonRitual() {
        if (isCrimsonRitualist()) {
            entityData.set(CRIMSON_RITUALIST, false);
        }
    }

    @Override
    protected InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {
        if (kind == LegacyMobKind.CONVERTED_VILLAGER) {
            return InteractionResult.PASS;
        }
        if (kind != LegacyMobKind.PECH
                || player.isShiftKeyDown()
                || !isPechTamed()) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide && player instanceof ServerPlayer server) {
            NetworkHooks.openScreen(
                    server,
                    new SimpleMenuProvider(
                            (containerId, inventory, ignored) ->
                                    new PechMenu(
                                            containerId,
                                            inventory,
                                            this
                                    ),
                            getDisplayName()
                    ),
                    buffer -> buffer.writeVarInt(getId())
            );
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private boolean canPechPickup(ItemEntity item) {
        if (kind != LegacyMobKind.PECH
                || !isAlive()
                || pechTrading
                || PechMenu.isTradeDrop(item)) {
            return false;
        }
        ItemStack stack = item.getItem();
        return isPechTamed()
                ? PechBehavior.canInsertPack(pechPack, stack)
                : PechBehavior.value(stack) > 0;
    }

    private void pickupForPech(ItemEntity item) {
        ItemStack stack = item.getItem();
        int before = stack.getCount();
        if (!isPechTamed()) {
            int value = PechBehavior.value(stack);
            if (value <= 0) {
                return;
            }
            stack.shrink(1);
            if (PechBehavior.tames(value, getRandom().nextInt(10))) {
                setPechTamed(true);
                level().broadcastEntityEvent(this, (byte) 18);
            }
        } else {
            ItemStack remainder = PechBehavior.insertPack(pechPack, stack);
            item.setItem(remainder);
            stack = remainder;
        }
        if (stack.isEmpty()) {
            item.discard();
        }
        if (stack.getCount() != before) {
            playSound(
                    SoundEvents.ITEM_PICKUP,
                    0.2F,
                    ((getRandom().nextFloat() - getRandom().nextFloat())
                            * 0.7F + 1.0F) * 2.0F
            );
        }
    }

    private void becomePechAngryAt(LivingEntity attacker) {
        if (kind != LegacyMobKind.PECH) {
            return;
        }
        if (pechAnger() <= 0) {
            level().broadcastEntityEvent(this, (byte) 19);
            playSound(
                    ModSounds.PECH_CHARGE.get(),
                    getSoundVolume(),
                    getVoicePitch()
            );
        }
        setLastHurtByMob(attacker);
        setTarget(attacker);
        entityData.set(
                PECH_ANGER,
                PechBehavior.angerTicks(getRandom())
        );
        setPechTamed(false);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (kind == LegacyMobKind.PECH
                && !level().isClientSide
                && source.getEntity() instanceof Player player) {
            for (LegacyThaumcraftMob nearby : level().getEntitiesOfClass(
                    LegacyThaumcraftMob.class,
                    getBoundingBox().inflate(
                            PechBehavior.ANGER_HORIZONTAL_RANGE,
                            PechBehavior.ANGER_VERTICAL_RANGE,
                            PechBehavior.ANGER_HORIZONTAL_RANGE
                    ),
                    mob -> mob.kind == LegacyMobKind.PECH
            )) {
                nearby.becomePechAngryAt(player);
            }
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                && !level().isClientSide
                && !isConstructHeadless()
                && EldritchConstructBehavior.breaksHead(
                        amount,
                        getHealth()
                )) {
            entityData.set(CONSTRUCT_HEADLESS, true);
            entityData.set(
                    CONSTRUCT_RECOVERY_TIMER,
                    EldritchConstructBehavior.SPAWN_RECOVERY_TICKS
            );
            float yaw = getYRot() % 360.0F * Mth.DEG_TO_RAD;
            double offsetX = Mth.cos(yaw) * 0.75F;
            double offsetZ = Mth.sin(yaw) * 0.75F;
            level().explode(
                    this,
                    getX() + offsetX,
                    getY() + getEyeHeight(),
                    getZ() + offsetZ,
                    2.0F,
                    Level.ExplosionInteraction.NONE
            );
            return false;
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                && constructRecoveryTimer() > 0) {
            return false;
        }
        if (kind == LegacyMobKind.FIREBAT
                && (source.is(DamageTypeTags.IS_FIRE)
                        || source.is(DamageTypeTags.IS_EXPLOSION))) {
            return false;
        }
        if (kind == LegacyMobKind.FIREBAT
                && !level().isClientSide
                && isFirebatHanging()) {
            setFirebatHanging(false);
        }
        if (kind == LegacyMobKind.FURIOUS_ZOMBIE
                && !level().isClientSide) {
            setFuriousAnger(FuriousZombieBehavior.afterHit(
                    furiousAnger()
            ));
        }
        float appliedAmount = kind == LegacyMobKind.ELDRITCH_GUARDIAN
                && source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                ? amount * 0.5F
                : amount;
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                && !level().isClientSide) {
            if (source.getEntity() instanceof LivingEntity attacker) {
                constructAggro.merge(
                        attacker.getId(),
                        (int) appliedAmount,
                        Integer::sum
                );
            }
            if (appliedAmount
                    > EldritchConstructBehavior.MAX_DAMAGE_PER_HIT
                    && constructAngerTicks == 0) {
                addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        EldritchConstructBehavior.ENRAGE_TICKS,
                        (int) (appliedAmount / 15.0F)
                ));
                addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        EldritchConstructBehavior.ENRAGE_TICKS,
                        (int) (appliedAmount / 40.0F)
                ));
                addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        EldritchConstructBehavior.ENRAGE_TICKS,
                        (int) (appliedAmount / 40.0F)
                ));
                constructAngerTicks = EldritchConstructBehavior.ENRAGE_TICKS;
                if (source.getEntity() instanceof Player player) {
                    player.sendSystemMessage(
                            Component.empty()
                                    .append(getDisplayName())
                                    .append(" ")
                                    .append(Component.translatable(
                                            "tc.boss.enrage"
                                    ))
                    );
                }
            }
            appliedAmount = EldritchConstructBehavior.cappedDamage(
                    appliedAmount
            );
        }
        boolean hurt = super.hurt(source, appliedAmount);
        if (hurt && !level().isClientSide
                && kind == LegacyMobKind.TAINT_SPORE) {
            burstSporeIntoCrawlers();
        } else if (hurt && !level().isClientSide
                && kind == LegacyMobKind.TAINT_SPORE_SWARMER) {
            spawnTaintSwarm();
            taintSwarmSpawnCounter = 500;
        }
        if (hurt
                && !level().isClientSide
                && kind == LegacyMobKind.ELDRITCH_CRAB
                && hasCrabHelm()
                && getHealth() / getMaxHealth() <= 0.5F) {
            setCrabHelm(false);
        }
        if (hurt
                && !level().isClientSide
                && kind == LegacyMobKind.WISP) {
            Entity directAttacker = source.getDirectEntity();
            Entity trueAttacker = source.getEntity();
            LivingEntity attacker = trueAttacker instanceof LivingEntity living
                    ? living
                    : directAttacker instanceof LivingEntity living
                            ? living
                            : null;
            if (attacker != null) {
                setTarget(attacker);
                wispAggroCooldown = 200;
                wispAttackCounter = 0;
            }
        }
        if (!hurt || level().isClientSide || !isCrimsonCultist()) {
            return hurt;
        }
        stopCrimsonRitual();
        if (source.getEntity() instanceof LivingEntity attacker
                && !isCrimsonCultist(attacker)) {
            alertNearbyCrimsonCultists(attacker);
        }
        return true;
    }

    private void setFuriousAnger(float anger) {
        float clamped = Mth.clamp(
                anger,
                FuriousZombieBehavior.INITIAL_ANGER,
                FuriousZombieBehavior.MAX_ANGER
        );
        if (Float.compare(entityData.get(FURIOUS_ANGER), clamped) != 0) {
            entityData.set(FURIOUS_ANGER, clamped);
            refreshDimensions();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (FURIOUS_ANGER.equals(accessor)) {
            refreshDimensions();
        }
        if (THAUMIC_SLIME_SIZE.equals(accessor)) {
            refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDimensions(pose);
        if (kind == LegacyMobKind.FURIOUS_ZOMBIE) {
            return dimensions.scale(furiousAnger());
        }
        if (kind == LegacyMobKind.THAUMIC_SLIME) {
            float side = 0.25F
                    * (float) Math.sqrt(thaumicSlimeSize()) + 0.25F;
            return EntityDimensions.scalable(side, side);
        }
        return dimensions;
    }

    private void alertNearbyCrimsonCultists(LivingEntity attacker) {
        double range = CrimsonCultBehavior.FOLLOW_RANGE;
        List<LegacyThaumcraftMob> allies = level().getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                getBoundingBox().inflate(
                        range,
                        CrimsonCultBehavior.ALERT_VERTICAL_RANGE,
                        range
                ),
                ally -> ally.isCrimsonCultist()
                        && ally != this
                        && ally.getTarget() == null
                        && !isCrimsonCultist(attacker)
        );
        for (LegacyThaumcraftMob ally : allies) {
            if (ally.isCrimsonRitualist()) {
                int legacyChanceRoll = getRandom().nextInt(
                        CrimsonCultBehavior.RITUALIST_ALERT_CHANCE
                );
                if (!CrimsonCultBehavior.shouldAlertRitualist(
                        legacyChanceRoll
                )) {
                    continue;
                }
                ally.stopCrimsonRitual();
            }
            ally.setTarget(attacker);
        }
    }

    private boolean isCrimsonCultist() {
        return kind == LegacyMobKind.CRIMSON_CLERIC
                || kind == LegacyMobKind.CRIMSON_KNIGHT
                || kind == LegacyMobKind.CRIMSON_INQUISITOR
                || kind == LegacyMobKind.CRIMSON_PRAETOR;
    }

    private static boolean isCrimsonCultist(LivingEntity entity) {
        return entity instanceof LegacyThaumcraftMob mob
                && mob.isCrimsonCultist();
    }

    @Override
    public boolean isNoGravity() {
        return kind != null && kind.flying() || super.isNoGravity();
    }

    @Override
    public MobType getMobType() {
        return switch (kind) {
            case ANGRY_ZOMBIE, FURIOUS_ZOMBIE, INHABITED_ZOMBIE,
                    TAINTED_VILLAGER, ELDRITCH_GUARDIAN ->
                    MobType.UNDEAD;
            default -> MobType.UNDEFINED;
        };
    }

    @Override
    protected float getStandingEyeHeight(
            Pose pose,
            EntityDimensions dimensions
    ) {
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            return isConstructHeadless() ? 3.33F : 3.0F;
        }
        return super.getStandingEyeHeight(pose, dimensions);
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            playSound(SoundEvents.IRON_GOLEM_STEP, 1.0F, 1.0F);
            return;
        }
        super.playStepSound(position, state);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag data
    ) {
        SpawnGroupData result = super.finalizeSpawn(
                level,
                difficulty,
                reason,
                spawnData,
                data
        );
        equipCrimsonArmor(true);
        equipInhabitedZombieArmor();
        if (kind == LegacyMobKind.PECH) {
            PechBehavior.HeldItemRoll roll = PechBehavior.heldItemRoll(
                    getRandom().nextInt(20)
            );
            entityData.set(PECH_TYPE, PechBehavior.typeFor(roll));
            ItemStack held = switch (roll) {
                case WAND -> ModItems.CASTING_WAND.get()
                        .getDefaultInstance();
                case BOW -> new ItemStack(Items.BOW);
                case STONE_SWORD -> new ItemStack(Items.STONE_SWORD);
                case STONE_AXE -> new ItemStack(Items.STONE_AXE);
                case IRON_SWORD -> new ItemStack(Items.IRON_SWORD);
                case IRON_AXE -> new ItemStack(Items.IRON_AXE);
                case FISHING_ROD -> new ItemStack(Items.FISHING_ROD);
                case STONE_PICKAXE -> new ItemStack(Items.STONE_PICKAXE);
                case IRON_PICKAXE -> new ItemStack(Items.IRON_PICKAXE);
                case EMPTY -> ItemStack.EMPTY;
            };
            if (roll == PechBehavior.HeldItemRoll.WAND) {
                PechBehavior.configureMageWand(held, getRandom());
                setDropChance(EquipmentSlot.MAINHAND, 0.1F);
            }
            setItemSlot(EquipmentSlot.MAINHAND, held);
            setCanPickUpLoot(
                    getRandom().nextFloat()
                            < 0.75F * difficulty.getSpecialMultiplier()
            );
        }
        if (kind == LegacyMobKind.WISP) {
            PrimalAspect[] aspects = PrimalAspect.ordered()
                    .toArray(PrimalAspect[]::new);
            entityData.set(
                    WISP_ASPECT,
                    aspects[getRandom().nextInt(aspects.length)].id()
            );
        }
        if (kind == LegacyMobKind.THAUMIC_SLIME) {
            setThaumicSlimeSize(1 << getRandom().nextInt(3));
            slimeJumpDelay = getRandom().nextInt(20) + 10;
        }
        if (kind == LegacyMobKind.ELDRITCH_CRAB) {
            setCrabHelm(
                    level.getDifficulty()
                                    == net.minecraft.world.Difficulty.HARD
                            || getRandom().nextFloat() < 0.33F
            );
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            entityData.set(
                    CONSTRUCT_RECOVERY_TIMER,
                    EldritchConstructBehavior.SPAWN_RECOVERY_TICKS
            );
            restrictTo(blockPosition(), 24);
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        if (kind == LegacyMobKind.FIREBAT && focusBatExplosive && !level().isClientSide)
            level().explode(this, getX(), getY(), getZ(), 1.5F,
                    Level.ExplosionInteraction.NONE);
        if (kind == LegacyMobKind.THAUMIC_SLIME
                && !level().isClientSide
                && !slimeSplit) {
            slimeSplit = true;
            int children = (int) Math.sqrt(thaumicSlimeSize());
            if (children > 1) {
                for (int index = 0; index < children; index++) {
                    LegacyThaumcraftMob child =
                            ModEntities.THAUMIC_SLIME.get().create(level());
                    if (child == null) {
                        continue;
                    }
                    child.setThaumicSlimeSize(1);
                    float xOffset = (index % 2 - 0.5F) * 0.5F;
                    float zOffset = (index / 2 - 0.5F) * 0.5F;
                    child.moveTo(
                            getX() + xOffset,
                            getY() + 0.5D,
                            getZ() + zOffset,
                            getRandom().nextFloat() * 360.0F,
                            0.0F
                    );
                    level().addFreshEntity(child);
                }
            }
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(
            DamageSource source,
            int looting,
            boolean recentlyHit
    ) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (kind == LegacyMobKind.WISP) {
            PrimalAspect aspect;
            try {
                aspect = PrimalAspect.fromId(wispAspect());
            } catch (RuntimeException invalidSavedAspect) {
                aspect = PrimalAspect.AER;
            }
            ItemStack essence = EtherealEssenceItem.create(
                    ModItems.ETHEREAL_ESSENCE.get(),
                    aspect,
                    2
            );
            spawnAtLocation(essence);
        }
        if (kind == LegacyMobKind.ELDRITCH_GUARDIAN) {
            if (getRandom().nextBoolean()) {
                spawnAtLocation(EtherealEssenceItem.create(
                        ModItems.ETHEREAL_ESSENCE.get(), "exanimis", 2));
            }
            if (getRandom().nextBoolean()) {
                spawnAtLocation(EtherealEssenceItem.create(
                        ModItems.ETHEREAL_ESSENCE.get(), "alienis", 2));
            }
        }
        if (kind == LegacyMobKind.THAUMIC_SLIME
                && thaumicSlimeSize() < 3
                && getRandom().nextInt(3) == 0) {
            spawnAtLocation(new ItemStack(ModItems.TAINTED_GOO.get()), 1.5F);
        }
        if (kind == LegacyMobKind.CRIMSON_KNIGHT
                || kind == LegacyMobKind.CRIMSON_INQUISITOR
                || kind == LegacyMobKind.CRIMSON_CLERIC) {
            int roll = getRandom().nextInt(10);
            if (roll == 0) {
                spawnAtLocation(new ItemStack(ModItems.KNOWLEDGE_FRAGMENT.get()), 1.5F);
            } else if (roll == 1) {
                spawnAtLocation(new ItemStack(ModItems.VOID_SEED.get()), 1.5F);
            } else if (roll <= 3 + looting) {
                spawnAtLocation(new ItemStack(ModItems.GOLD_COIN.get()), 1.5F);
            }
            if (recentlyHit && getRandom().nextInt(200) - looting < 5) {
                spawnAtLocation(new ItemStack(ModItems.CRIMSON_RITES.get()), 1.0F);
            }
        }
        if (kind == LegacyMobKind.PECH) {
            for (int slot = 0; slot < pechPack.getSlots(); slot++) {
                ItemStack stack = pechPack.getStackInSlot(slot);
                if (!stack.isEmpty() && getRandom().nextFloat() < 0.88F) {
                    spawnAtLocation(stack.copy(), 1.5F);
                }
            }
            PrimalAspect[] aspects = PrimalAspect.ordered()
                    .toArray(PrimalAspect[]::new);
            for (int roll = 0; roll < 1 + looting; roll++) {
                if (aspects.length > 0 && getRandom().nextBoolean()) {
                    ItemStack bean = ModItems.MANA_BEAN.get()
                            .getDefaultInstance();
                    ManaBeanItem.setAspect(bean, aspects[
                            getRandom().nextInt(aspects.length)
                    ].id());
                    spawnAtLocation(bean, 1.5F);
                }
            }
        }
        if (kind == LegacyMobKind.CRIMSON_PRAETOR
                && getPersistentData().getBoolean("OuterLandsPearlReward")) {
            spawnAtLocation(
                    new ItemStack(ModItems.PRIMORDIAL_PEARL.get()),
                    1.5F
            );
        }
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        String table = switch (kind) {
            case ANGRY_ZOMBIE -> "brainy_zombie";
            case FURIOUS_ZOMBIE -> "furious_zombie";
            case WISP, CONVERTED_VILLAGER -> "empty";
            case FIREBAT -> "firebat";
            case PECH -> "pech";
            case THAUMIC_SLIME -> "empty";
            case TAINTED_CRAWLER -> "tainted_crawler";
            case TAINTACLE -> "taintacle";
            case TAINT_TENDRIL -> "empty";
            case TAINT_SPORE -> "taint_spore";
            case TAINT_SPORE_SWARMER -> "taint_spore_swarmer";
            case TAINT_SWARM -> "taint_swarm";
            case TAINTED_CHICKEN -> "tainted_chicken";
            case TAINTED_COW, TAINTED_CREEPER -> "tainted_even";
            case TAINTED_PIG -> "tainted_pig";
            case TAINTED_SHEEP -> "tainted_sheep";
            case TAINTED_VILLAGER -> "tainted_villager";
            case GIANT_TAINTACLE -> "empty";
            case ELDRITCH_GUARDIAN -> "eldritch_guardian";
            case CRIMSON_KNIGHT, CRIMSON_INQUISITOR, CRIMSON_CLERIC -> "empty";
            case CRIMSON_PRAETOR -> "crimson_boss";
            case ELDRITCH_WARDEN, ELDRITCH_CONSTRUCT -> "eldritch_boss";
            case ELDRITCH_CRAB, INHABITED_ZOMBIE -> "empty";
            case MIND_SPIDER -> "mind_spider";
        };
        return new ResourceLocation(
                "thaumic_reborn",
                "entities/legacy/" + table
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("WarpHarmless", entityData.get(HARMLESS));
        entityData.get(WARP_VIEWER).ifPresent(
                viewer -> tag.putUUID("WarpViewer", viewer)
        );
        tag.putInt("PechType", pechType());
        if (kind == LegacyMobKind.PECH) {
            tag.putInt("PechAnger", pechAnger());
            tag.putBoolean("PechTamed", isPechTamed());
            tag.putBoolean("PechTrading", pechTrading);
            tag.put("PechLoot", pechPack.serializeNBT());
        }
        tag.putString("WispAspect", wispAspect());
        crimsonAltarPosition().ifPresent(position -> {
            tag.putLong("CrimsonAltar", position.asLong());
            tag.putBoolean("CrimsonRitualist", isCrimsonRitualist());
        });
        if (eldritchAltarHome != null) {
            tag.putLong(
                    "EldritchAltarHome",
                    eldritchAltarHome.asLong()
            );
        }
        if (kind == LegacyMobKind.FURIOUS_ZOMBIE) {
            tag.putFloat("Anger", furiousAnger());
        }
        if (kind == LegacyMobKind.THAUMIC_SLIME) {
            tag.putInt("ThaumicSlimeSize", thaumicSlimeSize());
        }
        if (kind == LegacyMobKind.FIREBAT) {
            tag.putBoolean("FirebatHanging", isFirebatHanging());
            tag.putBoolean("FocusSummoned", focusBatSummoned);
            tag.putBoolean("FocusExplosive", focusBatExplosive);
            tag.putBoolean("FocusVampire", focusBatVampire);
            if (focusBatOwner != null) tag.putUUID("FocusOwner", focusBatOwner);
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            tag.putBoolean("ConstructHeadless", isConstructHeadless());
            tag.putInt(
                    "ConstructRecoveryTicks",
                    constructRecoveryTimer()
            );
            tag.putInt("ConstructBeamCharge", constructBeamCharge());
            tag.putInt("ConstructAngerTicks", constructAngerTicks);
        }
        if (kind == LegacyMobKind.ELDRITCH_CRAB) {
            tag.putBoolean("CrabHelm", hasCrabHelm());
        }
        if (kind == LegacyMobKind.TAINT_SPORE) {
            tag.putInt("Size", Math.max(0, taintSporeSize - 1));
            tag.putInt("Growth", taintSporeGrowth);
        }
        if (kind == LegacyMobKind.TAINT_SPORE_SWARMER) {
            tag.putInt("SpawnCounter", taintSwarmSpawnCounter);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        equipCrimsonWeapon();
        // Migrate cultists saved before armor became real server equipment.
        equipCrimsonArmor(false);
        // Migrate inhabited zombies saved before their guaranteed TC4 helm.
        equipInhabitedZombieHelmet();
        entityData.set(HARMLESS, tag.getBoolean("WarpHarmless"));
        entityData.set(
                WARP_VIEWER,
                tag.hasUUID("WarpViewer")
                        ? Optional.of(tag.getUUID("WarpViewer"))
                        : Optional.empty()
        );
        if (entityData.get(HARMLESS)) {
            xpReward = 0;
        }
        var followRange = getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.setBaseValue(HostileAiBehavior.followRange(kind));
        }
        restoreLegacyBaseAttribute(Attributes.MAX_HEALTH, kind.health());
        restoreLegacyBaseAttribute(Attributes.ATTACK_DAMAGE, kind.damage());
        restoreLegacyBaseAttribute(
                Attributes.MOVEMENT_SPEED,
                Math.max(0.05D, kind.speed())
        );
        restoreLegacyBaseAttribute(
                Attributes.ARMOR,
                kind == LegacyMobKind.ELDRITCH_CONSTRUCT ? 6.0D : 0.0D
        );
        restoreLegacyBaseAttribute(
                Attributes.KNOCKBACK_RESISTANCE,
                kind == LegacyMobKind.ELDRITCH_CONSTRUCT ? 0.95D : 0.0D
        );
        if (tag.contains("PechType")) {
            entityData.set(
                    PECH_TYPE,
                    Math.max(0, Math.min(2, tag.getInt("PechType")))
            );
        }
        if (kind == LegacyMobKind.PECH) {
            entityData.set(
                    PECH_ANGER,
                    Math.max(
                            0,
                            tag.contains("PechAnger")
                                    ? tag.getInt("PechAnger")
                                    : tag.getShort("Anger")
                    )
            );
            setPechTamed(
                    tag.contains("PechTamed")
                            ? tag.getBoolean("PechTamed")
                            : tag.getBoolean("Tamed")
            );
            pechTrading = false;
            if (tag.contains("PechLoot", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
                pechPack.deserializeNBT(tag.getCompound("PechLoot"));
            } else if (tag.contains(
                    "Loot",
                    net.minecraft.nbt.Tag.TAG_LIST
            )) {
                var legacyLoot = tag.getList(
                        "Loot",
                        net.minecraft.nbt.Tag.TAG_COMPOUND
                );
                for (int slot = 0;
                     slot < Math.min(legacyLoot.size(), pechPack.getSlots());
                     slot++) {
                    pechPack.setStackInSlot(
                            slot,
                            ItemStack.of(legacyLoot.getCompound(slot))
                    );
                }
            }
        }
        if (tag.contains("WispAspect")) {
            entityData.set(WISP_ASPECT, tag.getString("WispAspect"));
        }
        if (tag.contains("CrimsonAltar")) {
            configureCrimsonAltar(
                    BlockPos.of(tag.getLong("CrimsonAltar")),
                    tag.getBoolean("CrimsonRitualist")
            );
        }
        if (tag.contains("EldritchAltarHome")) {
            configureEldritchAltarGuard(
                    BlockPos.of(tag.getLong("EldritchAltarHome"))
            );
        }
        if (kind == LegacyMobKind.FURIOUS_ZOMBIE
                && tag.contains("Anger")) {
            setFuriousAnger(tag.getFloat("Anger"));
        }
        if (kind == LegacyMobKind.THAUMIC_SLIME) {
            setThaumicSlimeSize(
                    tag.contains("ThaumicSlimeSize")
                            ? tag.getInt("ThaumicSlimeSize")
                            : 1
            );
        }
        if (kind == LegacyMobKind.FIREBAT) {
            setFirebatHanging(
                    !tag.contains("FirebatHanging")
                            || tag.getBoolean("FirebatHanging")
            );
            focusBatSummoned = tag.getBoolean("FocusSummoned");
            focusBatExplosive = tag.getBoolean("FocusExplosive");
            focusBatVampire = tag.getBoolean("FocusVampire");
            focusBatOwner = tag.hasUUID("FocusOwner") ? tag.getUUID("FocusOwner") : null;
        }
        if (kind == LegacyMobKind.ELDRITCH_CONSTRUCT) {
            entityData.set(
                    CONSTRUCT_HEADLESS,
                    tag.getBoolean("ConstructHeadless")
            );
            entityData.set(
                    CONSTRUCT_RECOVERY_TIMER,
                    Math.max(0, tag.getInt("ConstructRecoveryTicks"))
            );
            entityData.set(
                    CONSTRUCT_BEAM_CHARGE,
                    Math.max(0, tag.getInt("ConstructBeamCharge"))
            );
            constructAngerTicks = Math.max(
                    0,
                    tag.getInt("ConstructAngerTicks")
            );
        }
        if (kind == LegacyMobKind.ELDRITCH_CRAB) {
            setCrabHelm(tag.getBoolean("CrabHelm"));
        }
        if (kind == LegacyMobKind.TAINT_SPORE) {
            taintSporeSize = tag.contains("Size")
                    ? Mth.clamp(tag.getInt("Size") + 1, 1, 10) : 2;
            taintSporeGrowth = Math.max(0, tag.getInt("Growth"));
        }
        if (kind == LegacyMobKind.TAINT_SPORE_SWARMER) {
            taintSporeSize = 10;
            taintSwarmSpawnCounter = tag.contains("SpawnCounter")
                    ? Math.max(0, tag.getInt("SpawnCounter")) : 500;
        }
    }

    private void restoreLegacyBaseAttribute(
            net.minecraft.world.entity.ai.attributes.Attribute attribute,
            double value
    ) {
        var instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    public static AttributeSupplier.Builder createAttributes(LegacyMobKind kind) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, kind.health())
                .add(Attributes.ATTACK_DAMAGE, kind.damage())
                .add(Attributes.MOVEMENT_SPEED, Math.max(0.05D, kind.speed()))
                .add(
                        Attributes.ARMOR,
                        kind == LegacyMobKind.ELDRITCH_CONSTRUCT ? 6.0D : 0.0D
                )
                .add(
                        Attributes.KNOCKBACK_RESISTANCE,
                        kind == LegacyMobKind.ELDRITCH_CONSTRUCT
                                ? 0.95D
                                : 0.0D
                )
                .add(
                        Attributes.FOLLOW_RANGE,
                        HostileAiBehavior.followRange(kind)
                );
    }

    public static boolean checkSpawnRules(
            EntityType<LegacyThaumcraftMob> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos position,
            net.minecraft.util.RandomSource random
    ) {
        LegacyThaumcraftMob sample = type.create(level.getLevel());
        if (sample == null || !spawnEnabled(sample.kind)) {
            return false;
        }
        if (!allowsClassicBiomePopulation(
                sample.kind,
                level,
                position
        )) {
            return false;
        }
        if (sample.kind == LegacyMobKind.WISP) {
            /*
             * TC4 EntityWisp#getCanSpawnHere delegates to EntityLiving rather
             * than EntityMob: wisps require a non-peaceful world, but do not
             * require darkness. This is also essential for the illuminated
             * hilltop wisp spawner.
             */
            return WispSpawnPolicy.allows(level.getLevel().getDifficulty());
        }
        if (sample.kind == LegacyMobKind.ELDRITCH_GUARDIAN
                && !OuterLandsSpawnRules.isOuterLands(level)
                && EldritchGuardianBehavior.usesSurfaceNightRules(reason)) {
            int surfaceY = level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types
                            .MOTION_BLOCKING_NO_LEAVES,
                    position.getX(),
                    position.getZ()
            );
            if (!EldritchGuardianBehavior.isSurfaceNightSpawn(
                    level.getLevel().dimensionType().hasSkyLight(),
                    level.getLevel().isDay(),
                    position.getY(),
                    surfaceY
            )) {
                return false;
            }
        }
        if (sample.kind == LegacyMobKind.ELDRITCH_CRAB
                && !level.getBiome(position).is(ModWorldgenKeys.ELDRITCH)) {
            return false;
        }
        boolean taintedBiome = level.getBiome(position).is(
                ModWorldgenKeys.TAINTED_LANDS
        );
        if (sample.kind.tainted() && taintedBiome) {
            if (TaintedBiomeSpawnPolicy.requiresNearbyVillage(sample.kind)
                    && !level.getLevel().isCloseToVillage(
                            position,
                            TaintedBiomeSpawnPolicy
                                    .VILLAGE_PROXIMITY_SECTIONS
                    )) {
                return false;
            }
            boolean sturdyGround = sample.kind == LegacyMobKind.TAINTACLE
                    ? TaintedBiomeSpawnPolicy.validTaintacleGround(
                            level.getBlockState(position),
                            level.getBlockState(position.below()))
                    : level.getBlockState(position.below()).isFaceSturdy(
                            level,
                            position.below(),
                            net.minecraft.core.Direction.UP);
            return TaintedBiomeSpawnPolicy.allows(
                    sample.kind,
                    true,
                    TaintedBiomeSpawnPolicy.usesEcologyLifecycleRules(
                            sample.kind
                    )
                            ? level.getLevel().getDifficulty()
                                    != Difficulty.PEACEFUL
                            : Monster.checkMonsterSpawnRules(
                                    type,
                                    level,
                                    reason,
                                    position,
                                    random
                            ),
                    sturdyGround
            );
        }
        return Monster.checkMonsterSpawnRules(
                type,
                level,
                reason,
                position,
                random
        );
    }

    private static boolean allowsClassicBiomePopulation(
            LegacyMobKind kind,
            ServerLevelAccessor level,
            BlockPos position
    ) {
        int horizontalRange;
        int verticalRange;
        if (kind == LegacyMobKind.PECH) {
            horizontalRange = PECH_POPULATION_RANGE;
            verticalRange = PECH_POPULATION_RANGE;
        } else if (kind == LegacyMobKind.WISP) {
            horizontalRange = WISP_POPULATION_RANGE;
            verticalRange = WISP_POPULATION_RANGE;
        } else if (kind == LegacyMobKind.TAINTACLE) {
            horizontalRange = TAINTACLE_POPULATION_HORIZONTAL_RANGE;
            verticalRange = TAINTACLE_POPULATION_VERTICAL_RANGE;
        } else {
            return true;
        }
        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
                position.getX() - horizontalRange,
                position.getY() - verticalRange,
                position.getZ() - horizontalRange,
                position.getX() + horizontalRange + 1,
                position.getY() + verticalRange + 1,
                position.getZ() + horizontalRange + 1
        );
        int nearby = level.getLevel().getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                area,
                mob -> mob.kind() == kind
        ).size();
        return kind.allowsClassicBiomePopulation(nearby);
    }

    private static boolean spawnEnabled(LegacyMobKind kind) {
        if (kind == LegacyMobKind.ANGRY_ZOMBIE
                || kind == LegacyMobKind.FURIOUS_ZOMBIE) {
            return ThaumcraftModernServerConfig.spawnAngryZombies();
        }
        if (kind == LegacyMobKind.FIREBAT) {
            return ThaumcraftModernServerConfig.spawnFirebats();
        }
        if (kind == LegacyMobKind.WISP) {
            return ThaumcraftModernServerConfig.spawnWisps();
        }
        if (kind == LegacyMobKind.PECH) {
            return ThaumcraftModernServerConfig.spawnPech();
        }
        if (kind.tainted() || kind == LegacyMobKind.THAUMIC_SLIME) {
            return ThaumcraftModernServerConfig.spawnTaintCreatures();
        }
        return !kind.eldritch()
                || ThaumcraftModernServerConfig.spawnEldritchCreatures();
    }

    private static final class PechTradeGoal extends Goal {
        private final LegacyThaumcraftMob pech;

        private PechTradeGoal(LegacyThaumcraftMob pech) {
            this.pech = pech;
            setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return pech.pechTrading
                    && pech.isPechTamed()
                    && pech.isAlive()
                    && pech.onGround()
                    && !pech.isInWater()
                    && !pech.isPassenger();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            pech.getNavigation().stop();
        }

        @Override
        public void stop() {
            pech.pechTrading = false;
        }
    }

    private static final class PechRangedGoal extends RangedAttackGoal {
        private final LegacyThaumcraftMob pech;

        private PechRangedGoal(LegacyThaumcraftMob pech) {
            super(pech, 0.6D, 20, 50, 15.0F);
            this.pech = pech;
        }

        @Override
        public boolean canUse() {
            return pech.pechType() != PechBehavior.FORAGER
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return pech.pechType() != PechBehavior.FORAGER
                    && super.canContinueToUse();
        }
    }

    private static final class PechMeleeGoal extends MeleeAttackGoal {
        private final LegacyThaumcraftMob pech;

        private PechMeleeGoal(LegacyThaumcraftMob pech) {
            super(pech, 0.6D, false);
            this.pech = pech;
        }

        @Override
        public boolean canUse() {
            return pech.pechType() == PechBehavior.FORAGER
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return pech.pechType() == PechBehavior.FORAGER
                    && super.canContinueToUse();
        }
    }

    private static final class PechPickupGoal extends Goal {
        private final LegacyThaumcraftMob pech;
        private ItemEntity target;

        private PechPickupGoal(LegacyThaumcraftMob pech) {
            this.pech = pech;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (pech.pechTrading || pech.tickCount % 10 != 0) {
                return false;
            }
            target = pech.level().getEntitiesOfClass(
                    ItemEntity.class,
                    pech.getBoundingBox().inflate(
                            PechBehavior.ITEM_SEARCH_RANGE
                    ),
                    pech::canPechPickup
            ).stream().min(
                    java.util.Comparator.comparingDouble(pech::distanceToSqr)
            ).orElse(null);
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null
                    && target.isAlive()
                    && pech.canPechPickup(target)
                    && pech.distanceToSqr(target)
                            < PechBehavior.ITEM_SEARCH_RANGE
                                    * PechBehavior.ITEM_SEARCH_RANGE;
        }

        @Override
        public void start() {
            pech.getNavigation().moveTo(
                    target,
                    pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5D
            );
        }

        @Override
        public void tick() {
            if (target == null) {
                return;
            }
            pech.getLookControl().setLookAt(target, 30.0F, 30.0F);
            pech.getNavigation().moveTo(
                    target,
                    pech.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.5D
            );
            if (pech.distanceToSqr(
                    target.getX(),
                    target.getBoundingBox().minY,
                    target.getZ()
            ) <= 1.5D) {
                pech.pickupForPech(target);
                target = null;
            }
        }

        @Override
        public void stop() {
            target = null;
        }
    }

    private static final class WispZapGoal extends Goal {
        private static final double TARGET_RANGE = 16.0D;
        private final LegacyThaumcraftMob wisp;

        private WispZapGoal(LegacyThaumcraftMob wisp) {
            this.wisp = wisp;
            setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = wisp.getTarget();
            if (target != null && !target.isAlive()) {
                wisp.setTarget(null);
                target = null;
            }
            wisp.wispAggroCooldown--;
            if (wisp.getRandom().nextInt(1000) == 0
                    && (target == null || wisp.wispAggroCooldown-- <= 0)) {
                Player player = wisp.level().getNearestPlayer(
                        wisp,
                        TARGET_RANGE
                );
                if (player != null && !player.getAbilities().invulnerable) {
                    wisp.setTarget(player);
                    target = player;
                    wisp.wispAggroCooldown = 50;
                }
            }
            if (target == null
                    || wisp.distanceToSqr(target)
                            >= TARGET_RANGE * TARGET_RANGE) {
                if (wisp.wispAttackCounter > 0) {
                    wisp.wispAttackCounter--;
                }
                return;
            }
            wisp.getLookControl().setLookAt(target, 10.0F, 10.0F);
            if (!wisp.getSensing().hasLineOfSight(target)) {
                if (wisp.wispAttackCounter > 0) {
                    wisp.wispAttackCounter--;
                }
                return;
            }
            if (++wisp.wispAttackCounter != 20) {
                return;
            }
            wisp.playSound(ModSounds.ZAP.get(), 1.0F, 1.1F);
            wisp.renderWispZap(target);
            Vec3 movement = target.getDeltaMovement();
            boolean moving = Math.abs(movement.x) > 0.1D
                    || Math.abs(movement.y) > 0.1D
                    || Math.abs(movement.z) > 0.1D;
            float chance = moving ? 0.4F : 0.66F;
            if (wisp.getRandom().nextFloat() < chance) {
                float damage = (float) wisp.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                );
                target.hurt(
                        wisp.damageSources().indirectMagic(wisp, wisp),
                        moving ? damage : damage + 1.0F
                );
            }
            wisp.wispAttackCounter =
                    -20 + wisp.getRandom().nextInt(20);
        }
    }

    private static final class FirebatAttackGoal extends Goal {
        private final LegacyThaumcraftMob firebat;

        private FirebatAttackGoal(LegacyThaumcraftMob firebat) {
            this.firebat = firebat;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !firebat.isFirebatHanging()
                    && firebat.getTarget() != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = firebat.getTarget();
            return !firebat.isFirebatHanging()
                    && target != null
                    && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = firebat.getTarget();
            if (target == null) {
                return;
            }
            if (target instanceof Player player
                    && player.getAbilities().invulnerable) {
                firebat.setTarget(null);
                return;
            }
            Vec3 motion = firebat.getDeltaMovement();
            double dx = target.getX() - firebat.getX();
            double dy = target.getY()
                    + target.getEyeHeight() * 0.66D - firebat.getY();
            double dz = target.getZ() - firebat.getZ();
            firebat.setDeltaMovement(
                    motion.x + (Math.signum(dx) * 0.5D - motion.x) * 0.1D,
                    motion.y + (Math.signum(dy) * 0.7D - motion.y) * 0.1D,
                    motion.z + (Math.signum(dz) * 0.5D - motion.z) * 0.1D
            );
            firebat.getLookControl().setLookAt(target, 10.0F, 10.0F);
            double reach = Math.max(
                    2.5D,
                    target.getBbWidth() * 1.1D
            );
            boolean verticalOverlap =
                    firebat.getBoundingBox().maxY
                                    >= target.getBoundingBox().minY
                            && firebat.getBoundingBox().minY
                                    <= target.getBoundingBox().maxY;
            if (firebat.firebatAttackCooldown > 0
                    || !verticalOverlap
                    || firebat.distanceToSqr(target) > reach * reach) {
                return;
            }
            if (firebat.doHurtTarget(target)) {
                if (firebat.getRandom().nextBoolean()) {
                    target.setSecondsOnFire(2);
                }
                firebat.playSound(
                        SoundEvents.BAT_HURT,
                        0.5F,
                        0.9F + firebat.getRandom().nextFloat() * 0.2F
                );
            }
            firebat.firebatAttackCooldown = 20;
        }
    }

    private static final class CrimsonAltarFocusGoal extends Goal {
        private final LegacyThaumcraftMob cleric;

        private CrimsonAltarFocusGoal(LegacyThaumcraftMob cleric) {
            this.cleric = cleric;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return cleric.isCrimsonRitualist()
                    && cleric.crimsonAltarPosition().isPresent()
                    && cleric.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            cleric.getNavigation().stop();
        }

        @Override
        public void tick() {
            BlockPos altar = cleric.crimsonAltarPosition().orElse(null);
            if (altar == null) {
                return;
            }
            cleric.getNavigation().stop();
            cleric.getLookControl().setLookAt(
                    altar.getX() + 0.5D,
                    altar.getY() + 1.5D,
                    altar.getZ() + 0.5D,
                    30.0F,
                    10.0F
            );
            if (cleric.tickCount
                    % CrimsonCultBehavior.RITUAL_CHECK_INTERVAL_TICKS != 0) {
                return;
            }
            if (cleric.distanceToSqr(
                    altar.getX(),
                    altar.getY(),
                    altar.getZ()
            ) > CrimsonCultBehavior.RITUAL_MAX_DISTANCE_SQUARED
                    || !cleric.level().getBlockState(altar)
                            .is(com.thaumcraftmodern.registry.ModBlocks
                                    .ELDRITCH_ALTAR_PART.get())) {
                cleric.stopCrimsonRitual();
            }
        }
    }

    /**
     * Altar guards return home only while idle. Vanilla restrictions also
     * reject combat targets outside the home radius, which made cultists drop
     * players after only 8-16 blocks.
     */
    private static final class ReturnToCombatHomeGoal extends Goal {
        private final LegacyThaumcraftMob mob;

        private ReturnToCombatHomeGoal(LegacyThaumcraftMob mob) {
            this.mob = mob;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() == null
                    && homePosition() != null
                    && outsideHomeRadius();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getTarget() == null
                    && homePosition() != null
                    && outsideHomeRadius()
                    && !mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            moveHome();
        }

        @Override
        public void tick() {
            if (mob.getNavigation().isDone()) {
                moveHome();
            }
        }

        private void moveHome() {
            BlockPos home = homePosition();
            if (home != null) {
                mob.getNavigation().moveTo(
                        home.getX() + 0.5D,
                        home.getY(),
                        home.getZ() + 0.5D,
                        HostileAiBehavior.IDLE_RETURN_SPEED
                );
            }
        }

        private boolean outsideHomeRadius() {
            BlockPos home = homePosition();
            if (home == null) {
                return false;
            }
            int radius = mob.kind == LegacyMobKind.ELDRITCH_GUARDIAN
                    ? EldritchGuardianBehavior.HOME_RADIUS
                    : mob.kind == LegacyMobKind.CRIMSON_CLERIC
                            ? CrimsonCultBehavior.CLERIC_HOME_RADIUS
                            : CrimsonCultBehavior.KNIGHT_HOME_RADIUS;
            return mob.distanceToSqr(
                    home.getX() + 0.5D,
                    home.getY(),
                    home.getZ() + 0.5D
            ) > radius * radius;
        }

        private BlockPos homePosition() {
            return mob.kind == LegacyMobKind.ELDRITCH_GUARDIAN
                    ? mob.eldritchAltarHome
                    : mob.crimsonAltarPosition().orElse(null);
        }
    }

    private static final class EldritchGuardianRangedGoal
            extends RangedAttackGoal {
        private final LegacyThaumcraftMob guardian;

        private EldritchGuardianRangedGoal(
                LegacyThaumcraftMob guardian
        ) {
            super(
                    guardian,
                    1.0D,
                    EldritchGuardianBehavior
                            .RANGED_MIN_COOLDOWN_TICKS,
                    EldritchGuardianBehavior
                            .RANGED_MAX_COOLDOWN_TICKS,
                    EldritchGuardianBehavior.RANGED_MAX_DISTANCE
            );
            this.guardian = guardian;
        }

        @Override
        public boolean canUse() {
            return inRangedBand() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return inRangedBand() && super.canContinueToUse();
        }

        private boolean inRangedBand() {
            LivingEntity target = guardian.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            double distance = guardian.distanceToSqr(target);
            return distance
                            >= EldritchGuardianBehavior
                                    .RANGED_MIN_DISTANCE
                                    * EldritchGuardianBehavior
                                            .RANGED_MIN_DISTANCE
                    && distance
                            <= EldritchGuardianBehavior
                                    .RANGED_MAX_DISTANCE
                                    * EldritchGuardianBehavior
                                            .RANGED_MAX_DISTANCE;
        }
    }

    private static final class EldritchConstructRangedGoal
            extends RangedAttackGoal {
        private final LegacyThaumcraftMob construct;

        private EldritchConstructRangedGoal(
                LegacyThaumcraftMob construct
        ) {
            super(
                    construct,
                    EldritchConstructBehavior.RANGED_MOVE_SPEED,
                    EldritchConstructBehavior.RANGED_INTERVAL_TICKS,
                    EldritchConstructBehavior.RANGED_INTERVAL_TICKS,
                    EldritchConstructBehavior.RANGED_RANGE
            );
            this.construct = construct;
        }

        @Override
        public boolean canUse() {
            return combatReady() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return combatReady() && super.canContinueToUse();
        }

        private boolean combatReady() {
            LivingEntity target = construct.getTarget();
            return construct.isConstructHeadless()
                    && construct.constructRecoveryTimer() <= 0
                    && target != null
                    && target.isAlive()
                    && construct.distanceToSqr(target)
                            <= EldritchConstructBehavior.RANGED_RANGE
                                    * EldritchConstructBehavior.RANGED_RANGE;
        }
    }
}
