package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * TC4's gravity-free black Eldritch Orb.
 */
public final class EldritchOrbEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Boolean> GOLEM_ORB =
            SynchedEntityData.defineId(
                    EldritchOrbEntity.class,
                    EntityDataSerializers.BOOLEAN
            );
    private static final EntityDataAccessor<Integer> TARGET_ID =
            SynchedEntityData.defineId(
                    EldritchOrbEntity.class,
                    EntityDataSerializers.INT
            );
    private LivingEntity homingTarget;

    public EldritchOrbEntity(
            EntityType<? extends EldritchOrbEntity> type,
            Level level
    ) {
        super(type, level);
    }

    public EldritchOrbEntity(LivingEntity owner, Level level) {
        super(ModEntities.ELDRITCH_ORB.get(), owner, level);
    }

    public EldritchOrbEntity(
            LivingEntity owner,
            LivingEntity target,
            Level level
    ) {
        this(owner, level);
        homingTarget = target;
        entityData.set(GOLEM_ORB, true);
        entityData.set(TARGET_ID, target.getId());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(GOLEM_ORB, false);
        entityData.define(TARGET_ID, -1);
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (isGolemOrb()) {
            tickGolemHoming();
        }
        if (level().isClientSide) {
            level().addParticle(
                    ParticleTypes.SQUID_INK,
                    getX(),
                    getY(),
                    getZ(),
                    0.0D,
                    0.0D,
                    0.0D
                );
        }
        int lifetime = isGolemOrb()
                ? CrimsonCultBehavior.RED_ORB_LIFETIME_TICKS
                : EldritchGuardianBehavior.ORB_LIFETIME_TICKS;
        if (tickCount > lifetime) {
            discard();
        }
    }

    public boolean isCrimsonGolemOrb() {
        return entityData.get(GOLEM_ORB);
    }

    private boolean isGolemOrb() {
        return isCrimsonGolemOrb();
    }

    private void tickGolemHoming() {
        if ((homingTarget == null || !homingTarget.isAlive())
                && entityData.get(TARGET_ID) >= 0
                && level().getEntity(entityData.get(TARGET_ID))
                        instanceof LivingEntity target) {
            homingTarget = target;
        }
        if (homingTarget == null || !homingTarget.isAlive()) {
            return;
        }
        double distanceSquared = distanceToSqr(homingTarget);
        if (distanceSquared <= 0.01D) {
            return;
        }
        double dx = (homingTarget.getX() - getX()) / distanceSquared;
        double dy = (homingTarget.getBoundingBox().minY
                + homingTarget.getBbHeight() * 0.6D - getY())
                / distanceSquared;
        double dz = (homingTarget.getZ() - getZ()) / distanceSquared;
        Vec3 velocity = getDeltaMovement();
        setDeltaMovement(
                Mth.clamp(
                        velocity.x
                                + dx * EldritchConstructBehavior
                                        .ORB_ACCELERATION,
                        -EldritchConstructBehavior.ORB_MAX_AXIS_SPEED,
                        EldritchConstructBehavior.ORB_MAX_AXIS_SPEED
                ),
                Mth.clamp(
                        velocity.y
                                + dy * EldritchConstructBehavior
                                        .ORB_ACCELERATION,
                        -EldritchConstructBehavior.ORB_MAX_AXIS_SPEED,
                        EldritchConstructBehavior.ORB_MAX_AXIS_SPEED
                ),
                Mth.clamp(
                        velocity.z
                                + dz * EldritchConstructBehavior
                                        .ORB_ACCELERATION,
                        -EldritchConstructBehavior.ORB_MAX_AXIS_SPEED,
                        EldritchConstructBehavior.ORB_MAX_AXIS_SPEED
                )
        );
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide || !(getOwner() instanceof LivingEntity owner)) {
            return;
        }

        if (isGolemOrb()) {
            if (result instanceof EntityHitResult entityHit) {
                float damage = (float) owner.getAttributeValue(
                        Attributes.ATTACK_DAMAGE
                ) * CrimsonCultBehavior.RED_ORB_DAMAGE_MULTIPLIER;
                entityHit.getEntity().hurt(
                        damageSources().indirectMagic(this, owner),
                        damage
                );
            }
            level().playSound(
                    null,
                    getX(),
                    getY(),
                    getZ(),
                    ModSounds.SHOCK.get(),
                    SoundSource.HOSTILE,
                    1.0F,
                    1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F
            );
            level().broadcastEntityEvent(this, (byte) 16);
            discard();
            return;
        }

        float damage = (float) owner.getAttributeValue(
                Attributes.ATTACK_DAMAGE
        ) * EldritchGuardianBehavior.ORB_DAMAGE_MULTIPLIER;
        for (Entity entity : level().getEntities(
                owner,
                getBoundingBox().inflate(
                        EldritchGuardianBehavior.ORB_EFFECT_RADIUS
                ),
                candidate -> candidate instanceof LivingEntity living
                        && living.isAlive()
                        && living.getMobType() != MobType.UNDEAD
        )) {
            LivingEntity living = (LivingEntity) entity;
            living.hurt(
                    damageSources().indirectMagic(this, owner),
                    damage
            );
            living.addEffect(new MobEffectInstance(
                    MobEffects.WITHER,
                    EldritchGuardianBehavior.ORB_WITHER_TICKS,
                    0
            ));
        }
        level().playSound(
                null,
                getX(),
                getY(),
                getZ(),
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.HOSTILE,
                0.5F,
                2.6F + (random.nextFloat() - random.nextFloat()) * 0.8F
        );
        level().broadcastEntityEvent(this, (byte) 16);
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isGolemOrb() || isInvulnerableTo(source)) {
            return super.hurt(source, amount);
        }
        Entity attacker = source.getEntity();
        if (attacker == null) {
            return false;
        }
        Vec3 look = attacker.getLookAngle();
        setDeltaMovement(look.scale(0.9D));
        homingTarget = null;
        entityData.set(TARGET_ID, -1);
        hasImpulse = true;
        playSound(
                ModSounds.ZAP.get(),
                1.0F,
                1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F
        );
        return true;
    }

    @Override
    public void handleEntityEvent(byte eventId) {
        if (eventId == 16) {
            for (int index = 0; index < 30; index++) {
                double dx = (random.nextDouble() - random.nextDouble()) * 0.3D;
                double dy = (random.nextDouble() - random.nextDouble()) * 0.3D;
                double dz = (random.nextDouble() - random.nextDouble()) * 0.3D;
                level().addParticle(
                        ParticleTypes.SQUID_INK,
                        getX() + dx,
                        getY() + dy,
                        getZ() + dz,
                        dx,
                        dy,
                        dz
                );
            }
            return;
        }
        super.handleEntityEvent(eventId);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("GolemOrb", isGolemOrb());
        tag.putInt("TargetId", entityData.get(TARGET_ID));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(GOLEM_ORB, tag.getBoolean("GolemOrb"));
        entityData.set(TARGET_ID, tag.getInt("TargetId"));
    }
}
