package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

/** Source-faithful modern projectile for TC4's six primal arrow variants. */
public final class PrimalArrowEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(
            PrimalArrowEntity.class, EntityDataSerializers.INT);
    private static final double TC4_BASE_DAMAGE = 2.1D;
    private static final ResourceKey<DamageType> AIR_ARROW = damageType("air_arrow");
    private static final ResourceKey<DamageType> FIRE_ARROW = damageType("fire_arrow");
    private static final ResourceKey<DamageType> ORDER_ARROW = damageType("order_arrow");

    public PrimalArrowEntity(EntityType<? extends PrimalArrowEntity> type, Level level) {
        super(type, level);
        setBaseDamage(TC4_BASE_DAMAGE);
    }

    public PrimalArrowEntity(Level level, LivingEntity shooter, PrimalArrowType type) {
        super(ModEntities.PRIMAL_ARROW.get(), shooter, level);
        setPrimalType(type);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_TYPE, PrimalArrowType.AER.legacyMetadata());
    }

    public PrimalArrowType primalType() {
        return PrimalArrowType.byLegacyMetadata(entityData.get(DATA_TYPE));
    }

    public void setPrimalType(PrimalArrowType type) {
        entityData.set(DATA_TYPE, type.legacyMetadata());
        setBaseDamage(TC4_BASE_DAMAGE * type.damageMultiplier());
        if (type == PrimalArrowType.TERRA) {
            super.setKnockback(1);
        }
    }

    /** Lets AbstractArrow keep all of its vanilla hit/piercing behavior while selecting TC4's damage class. */
    @Override
    public DamageSources damageSources() {
        PrimalArrowType type = primalType();
        if (type != PrimalArrowType.AER && type != PrimalArrowType.IGNIS
                && type != PrimalArrowType.ORDO) {
            return super.damageSources();
        }
        RegistryAccess access = level().registryAccess();
        return new DamageSources(access) {
            @Override
            public DamageSource arrow(AbstractArrow arrow, Entity owner) {
                ResourceKey<DamageType> key = switch (type) {
                    case AER -> AIR_ARROW;
                    case IGNIS -> FIRE_ARROW;
                    case ORDO -> ORDER_ARROW;
                    default -> throw new IllegalStateException("Unexpected primal arrow type " + type);
                };
                return new DamageSource(access.registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(key), arrow, owner);
            }
        };
    }

    @Override
    public void setKnockback(int strength) {
        super.setKnockback(strength + (primalType() == PrimalArrowType.TERRA ? 1 : 0));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && !inGround && tickCount > 1) {
            int color = primalType().color();
            Vector3f rgb = new Vector3f(
                    ((color >> 16) & 255) / 255.0F,
                    ((color >> 8) & 255) / 255.0F,
                    (color & 255) / 255.0F
            );
            level().addParticle(new DustParticleOptions(rgb, 0.7F),
                    getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        switch (primalType()) {
            case IGNIS -> target.setSecondsOnFire(isOnFire() ? 10 : 5);
            case AQUA -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 4));
            case ORDO -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 4));
            case PERDITIO -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100));
            default -> {
            }
        }
    }

    @Override
    protected ItemStack getPickupItem() {
        String id = switch (primalType()) {
            case AER -> "aer_primal_arrow";
            case IGNIS -> "ignis_primal_arrow";
            case AQUA -> "aqua_primal_arrow";
            case TERRA -> "terra_primal_arrow";
            case ORDO -> "ordo_primal_arrow";
            case PERDITIO -> "perditio_primal_arrow";
        };
        return new ItemStack(ModItems.ARCANE_RECIPE_COMPONENTS.get(id).get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("type", (byte) primalType().legacyMetadata());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPrimalType(PrimalArrowType.byLegacyMetadata(tag.getByte("type")));
    }

    private static ResourceKey<DamageType> damageType(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                new ResourceLocation(ThaumcraftModern.MOD_ID, id));
    }
}
