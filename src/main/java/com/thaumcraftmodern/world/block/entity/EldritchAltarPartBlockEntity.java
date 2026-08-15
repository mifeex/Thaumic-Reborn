package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.entity.EldritchGuardianBehavior;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import org.jetbrains.annotations.Nullable;

public final class EldritchAltarPartBlockEntity extends BlockEntity {
    private static final double CAP_RENDER_MARGIN = 0.25D;
    private static final double OBELISK_RENDER_MARGIN = 0.125D;
    private static final double OBELISK_RENDER_HEIGHT = 4.25D;
    private int spawnCounter;
    private int obeliskEffectCounter;
    private int insertedEyes;

    public EldritchAltarPartBlockEntity(
            BlockPos position,
            BlockState state
    ) {
        super(ModBlockEntities.ELDRITCH_ALTAR_PART.get(), position, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        // LevelRenderer frustum-tests this box even for a BER whose
        // shouldRenderOffScreen() returns true. The animated obelisk extends
        // more than three blocks above its owning block entity.
        int part = getBlockState().hasProperty(EldritchAltarPartBlock.PART)
                ? getBlockState().getValue(EldritchAltarPartBlock.PART)
                : -1;
        return renderBoundingBox(worldPosition, part);
    }

    static AABB renderBoundingBox(BlockPos position, int part) {
        if (part != 1) {
            return new AABB(position).inflate(CAP_RENDER_MARGIN);
        }
        return new AABB(
                position.getX() - OBELISK_RENDER_MARGIN,
                position.getY(),
                position.getZ() - OBELISK_RENDER_MARGIN,
                position.getX() + 1.0D + OBELISK_RENDER_MARGIN,
                position.getY() + OBELISK_RENDER_HEIGHT,
                position.getZ() + 1.0D + OBELISK_RENDER_MARGIN
        );
    }

    public static void serverTick(
            ServerLevel level,
            BlockPos position,
            BlockState state,
            EldritchAltarPartBlockEntity altar
    ) {
        int part = state.getValue(EldritchAltarPartBlock.PART);
        if (part == 1) {
            if (altar.obeliskEffectCounter++
                    % EldritchGuardianBehavior
                            .OBELISK_EFFECT_CHECK_INTERVAL_TICKS == 0) {
                refreshNearbyEldritchMobs(level, position);
            }
            return;
        }
        if (part != 0) {
            return;
        }
        int counterBeforeIncrement = altar.spawnCounter++;
        if (!EldritchGuardianBehavior.isAltarSpawnBoundary(
                counterBeforeIncrement
        ) || !EldritchGuardianBehavior.shouldAttemptAltarSpawn(
                counterBeforeIncrement,
                hasLivingCultists(level, position),
                hasNearbyGuardian(level, position)
        )) {
            return;
        }
        attemptGuardianSpawn(level, position);
    }

    public static void clientTick(
            Level level,
            BlockPos position,
            BlockState state,
            EldritchAltarPartBlockEntity altar
    ) {
        if (state.getValue(EldritchAltarPartBlock.PART) != 1) {
            return;
        }
        AABB area = new AABB(position).inflate(
                EldritchGuardianBehavior.OBELISK_EFFECT_RADIUS
        );
        for (LegacyThaumcraftMob mob : level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                area,
                candidate -> candidate.isAlive()
                        && candidate.kind().eldritch()
                        && candidate.distanceToSqr(
                                position.getX() + 0.5D,
                                position.getY(),
                                position.getZ() + 0.5D
                        ) <= EldritchGuardianBehavior
                                .OBELISK_EFFECT_RADIUS
                                * EldritchGuardianBehavior
                                        .OBELISK_EFFECT_RADIUS
        )) {
            double sourceX = position.getX() + 0.5D;
            double sourceY = position.getY() + 1.0D
                    + level.random.nextFloat() * 3.0D;
            double sourceZ = position.getZ() + 0.5D;
            double targetX = mob.getX();
            double targetY = mob.getY() + mob.getBbHeight() * 0.5D;
            double targetZ = mob.getZ();
            level.addParticle(
                    ModParticles.ELDRITCH_HEAL.get(),
                    sourceX,
                    sourceY,
                    sourceZ,
                    targetX - sourceX,
                    targetY - sourceY,
                    targetZ - sourceZ
            );
        }
    }

    public boolean insertEye(ServerLevel level) {
        if (getBlockState().getValue(EldritchAltarPartBlock.PART) != 0
                || insertedEyes >= 4) {
            return false;
        }
        insertedEyes++;
        setChanged();
        level.sendBlockUpdated(
                worldPosition,
                getBlockState(),
                getBlockState(),
                Block.UPDATE_CLIENTS
        );
        level.playSound(
                null,
                worldPosition,
                ModSounds.CRYSTAL.get(),
                SoundSource.BLOCKS,
                0.65F,
                0.85F + insertedEyes * 0.08F
        );
        level.sendParticles(
                ParticleTypes.PORTAL,
                worldPosition.getX() + 0.5D,
                worldPosition.getY() + 1.1D,
                worldPosition.getZ() + 0.5D,
                12 + insertedEyes * 4,
                0.45D,
                0.35D,
                0.45D,
                0.1D
        );
        if (insertedEyes == 4) {
            level.setBlock(
                    worldPosition.above(),
                    ModBlocks.OUTER_LANDS_PORTAL.get().defaultBlockState(),
                    3
            );
            level.playSound(
                    null,
                    worldPosition,
                    net.minecraft.sounds.SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.BLOCKS,
                    1.0F,
                    0.7F
            );
        }
        return true;
    }

    public int insertedEyes() {
        return insertedEyes;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("InsertedEyes", insertedEyes);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        insertedEyes = Mth.clamp(tag.getInt("InsertedEyes"), 0, 4);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet
    ) {
        if (packet.getTag() != null) {
            load(packet.getTag());
        }
    }

    private static void refreshNearbyEldritchMobs(
            ServerLevel level,
            BlockPos obelisk
    ) {
        AABB area = new AABB(obelisk).inflate(
                EldritchGuardianBehavior.OBELISK_EFFECT_RADIUS
        );
        for (LegacyThaumcraftMob mob : level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                area,
                candidate -> candidate.isAlive()
                        && candidate.kind().eldritch()
                        && candidate.distanceToSqr(
                                obelisk.getX() + 0.5D,
                                obelisk.getY(),
                                obelisk.getZ() + 0.5D
                        ) <= EldritchGuardianBehavior
                                .OBELISK_EFFECT_RADIUS
                                * EldritchGuardianBehavior
                                        .OBELISK_EFFECT_RADIUS
        )) {
            if (mob.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                continue;
            }
            mob.addEffect(classicObeliskEffect(MobEffects.REGENERATION));
            mob.addEffect(classicObeliskEffect(
                    MobEffects.DAMAGE_RESISTANCE
            ));
            mob.heal(EldritchGuardianBehavior.OBELISK_HEAL_AMOUNT);
        }
    }

    private static MobEffectInstance classicObeliskEffect(
            net.minecraft.world.effect.MobEffect effect
    ) {
        return new MobEffectInstance(
                effect,
                EldritchGuardianBehavior.OBELISK_EFFECT_DURATION_TICKS,
                EldritchGuardianBehavior.OBELISK_EFFECT_AMPLIFIER,
                true,
                true
        );
    }

    private static boolean hasLivingCultists(
            ServerLevel level,
            BlockPos altar
    ) {
        AABB area = new AABB(altar).inflate(
                EldritchGuardianBehavior.CULTIST_SEARCH_HORIZONTAL,
                EldritchGuardianBehavior.CULTIST_SEARCH_VERTICAL,
                EldritchGuardianBehavior.CULTIST_SEARCH_HORIZONTAL
        );
        return !level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                area,
                mob -> mob.isAlive()
                        && (mob.kind() == LegacyMobKind.CRIMSON_CLERIC
                                || mob.kind()
                                        == LegacyMobKind.CRIMSON_KNIGHT
                                || mob.kind()
                                        == LegacyMobKind.CRIMSON_PRAETOR)
        ).isEmpty();
    }

    private static boolean hasNearbyGuardian(
            ServerLevel level,
            BlockPos altar
    ) {
        AABB area = new AABB(altar).inflate(
                EldritchGuardianBehavior.GUARDIAN_SEARCH_HORIZONTAL,
                EldritchGuardianBehavior.GUARDIAN_SEARCH_VERTICAL,
                EldritchGuardianBehavior.GUARDIAN_SEARCH_HORIZONTAL
        );
        return !level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                area,
                mob -> mob.isAlive()
                        && mob.kind()
                                == LegacyMobKind.ELDRITCH_GUARDIAN
        ).isEmpty();
    }

    private static void attemptGuardianSpawn(
            ServerLevel level,
            BlockPos altar
    ) {
        if (level.dimensionType().hasSkyLight() && level.isDay()) {
            return;
        }
        RandomSource random = level.getRandom();
        int spawnX = altar.getX() + signedRange(
                random,
                EldritchGuardianBehavior.SPAWN_MIN_HORIZONTAL,
                EldritchGuardianBehavior.SPAWN_MAX_HORIZONTAL
        );
        int spawnZ = altar.getZ() + signedRange(
                random,
                EldritchGuardianBehavior.SPAWN_MIN_HORIZONTAL,
                EldritchGuardianBehavior.SPAWN_MAX_HORIZONTAL
        );
        int spawnY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                spawnX,
                spawnZ
        );
        BlockPos spawn = new BlockPos(spawnX, spawnY, spawnZ);
        BlockPos floor = spawn.below();
        if (!level.hasChunkAt(spawn)
                || !level.getBlockState(floor)
                        .isFaceSturdy(level, floor, Direction.UP)) {
            return;
        }

        var type = ModEntities.ELDRITCH_GUARDIAN.get();
        LegacyThaumcraftMob guardian = type.create(level);
        if (guardian == null) {
            return;
        }
        guardian.moveTo(
                spawn.getX() + 0.5D,
                spawn.getY(),
                spawn.getZ() + 0.5D,
                random.nextFloat() * 360.0F,
                0.0F
        );
        if (!LegacyThaumcraftMob.checkSpawnRules(
                type,
                level,
                MobSpawnType.STRUCTURE,
                spawn,
                random
        ) || !guardian.checkSpawnObstruction(level)) {
            return;
        }
        guardian.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(spawn),
                MobSpawnType.STRUCTURE,
                null,
                null
        );
        guardian.configureEldritchAltarGuard(altar);
        guardian.setPersistenceRequired();
        level.addFreshEntity(guardian);
    }

    private static int signedRange(
            RandomSource random,
            int minimum,
            int maximum
    ) {
        int magnitude = minimum + random.nextInt(maximum - minimum + 1);
        return magnitude * (random.nextBoolean() ? 1 : -1);
    }
}
