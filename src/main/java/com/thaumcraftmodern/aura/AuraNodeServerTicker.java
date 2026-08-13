package com.thaumcraftmodern.aura;

import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.NodeZapPacket;
import com.thaumcraftmodern.visnet.NodeStabilizerBlockEntity;
import com.thaumcraftmodern.worldgen.ClassicAuraNodeWorldFactory;
import com.thaumcraftmodern.world.block.EerieBiomeService;
import com.thaumcraftmodern.world.block.MagicalForestBiomeService;
import com.thaumcraftmodern.world.block.TaintBiomeService;
import com.thaumcraftmodern.world.block.TaintEcology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-authoritative TC4 node regeneration, discharge and hungry behavior.
 */
final class AuraNodeServerTicker {
    private static final int HUNGRY_RANGE = 15;

    private AuraNodeServerTicker() {
    }

    static void tick(
            ServerLevel level,
            BlockPos position,
            AuraNodeBlockEntity node
    ) {
        ClassicAuraNodeWorldFactory.migrateLegacyUniformDark(
                level,
                position,
                node
        );
        node.advanceClassicTick();
        int ticks = node.classicTicks();
        long nowMillis = System.currentTimeMillis();
        node.initializeLastActive(nowMillis);
        if (node.regenerationWait() > 0) {
            node.decrementRegenerationWait();
        }

        AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
        int stabilizerLock = stabilizerLock(level, position);
        catchUpRegeneration(
                level.random,
                node,
                stabilizerLock,
                nowMillis
        );
        tickTaintNode(level, position, node, snapshot, ticks);
        if (snapshot.type() == AuraNodeType.DARK) {
            tickDarkNode(level, position, ticks);
        }
        if (snapshot.type() == AuraNodeType.PURE) {
            tickPureNode(level, position, ticks);
        }
        if (snapshot.type() == AuraNodeType.HUNGRY) {
            tickHungryEntities(level, position, ticks);
            if (ticks % 50 == 0) {
                hungryBreakBlock(level, position);
            }
        }
        tickNodeStability(
                level,
                position,
                node,
                snapshot,
                stabilizerLock,
                ticks
        );
        if (ticks % 1200 == 0
                && decayEmptyAspects(level, position, node)) {
            return;
        }
        regenerate(
                level.random,
                ticks,
                node,
                stabilizerLock,
                nowMillis
        );
        discharge(level, position, node, stabilizerLock);
    }

    /**
     * TC4 pure nodes choose one triangular [-7, 7] X/Z target every fifty
     * ticks. A node embedded in Silverwood may paint any permitted biome;
     * an ordinary pure node only replaces Tainted Lands.
     */
    private static void tickPureNode(
            ServerLevel level,
            BlockPos position,
            int ticks
    ) {
        if (ticks % PureNodeBiomeSpreadRules.INTERVAL_TICKS != 0
                || level.dimension() == Level.NETHER
                || level.dimension() == Level.END) {
            return;
        }
        BlockPos biomeTarget = position.offset(
                PureNodeBiomeSpreadRules.biomeOffset(
                        level.random.nextInt(
                                PureNodeBiomeSpreadRules.BIOME_OFFSET_BOUND
                        ),
                        level.random.nextInt(
                                PureNodeBiomeSpreadRules.BIOME_OFFSET_BOUND
                        )
                ),
                0,
                PureNodeBiomeSpreadRules.biomeOffset(
                        level.random.nextInt(
                                PureNodeBiomeSpreadRules.BIOME_OFFSET_BOUND
                        ),
                        level.random.nextInt(
                                PureNodeBiomeSpreadRules.BIOME_OFFSET_BOUND
                        )
                )
        );
        if (!level.isLoaded(biomeTarget)
                || MagicalForestBiomeService.isMagicalForest(
                        level,
                        biomeTarget
                )) {
            return;
        }
        boolean embeddedInSilverwood = level.getBlockState(position)
                .is(ModBlocks.SILVERWOOD_NODE.get());
        boolean targetIsTainted = TaintBiomeService.isTainted(
                level,
                biomeTarget
        );
        if (PureNodeBiomeSpreadRules.mayPaint(
                ticks,
                embeddedInSilverwood,
                targetIsTainted
        )) {
            MagicalForestBiomeService.makeColumnMagicalForest(
                    level,
                    biomeTarget
            );
        }
    }

    private static void tickTaintNode(
            ServerLevel level,
            BlockPos position,
            AuraNodeBlockEntity node,
            AuraNodeState.Snapshot snapshot,
            int ticks
    ) {
        if (snapshot.type() == AuraNodeType.TAINTED) {
            if (ticks % 50 != 0) {
                return;
            }
            BlockPos biomeTarget = position.offset(
                    level.random.nextInt(8) - level.random.nextInt(8),
                    0,
                    level.random.nextInt(8) - level.random.nextInt(8)
            );
            TaintBiomeService.taintColumn(level, biomeTarget);
            if (ThaumcraftModernServerConfig.hardNodes()
                    && level.random.nextBoolean()) {
                BlockPos fibresTarget = position.offset(
                        level.random.nextInt(5) - level.random.nextInt(5),
                        level.random.nextInt(5) - level.random.nextInt(5),
                        level.random.nextInt(5) - level.random.nextInt(5)
                );
                if (level.isLoaded(fibresTarget)
                        && (level.isEmptyBlock(fibresTarget)
                        || level.getBlockState(fibresTarget)
                                .canBeReplaced())) {
                    TaintEcology.placeFibres(level, fibresTarget);
                }
            }
            return;
        }
        if (snapshot.type() != AuraNodeType.PURE
                && ticks % 100 == 0
                && TaintBiomeService.isTainted(level, position)
                && level.random.nextInt(500) == 0) {
            node.replaceType(AuraNodeType.TAINTED);
        }
    }

    /**
     * TC4 dark nodes paint Eerie biome and, in hard-node mode, attempt a
     * Furious Zombie spawn every 50 ticks.
     */
    private static void tickDarkNode(
            ServerLevel level,
            BlockPos position,
            int ticks
    ) {
        boolean interval = ticks
                % MoundGuardianSpawnRules.INTERVAL_TICKS == 0;
        if (!interval
                || level.dimension() == Level.NETHER
                || level.dimension() == Level.END) {
            return;
        }
        BlockPos biomeTarget = position.offset(
                MoundGuardianSpawnRules.biomeOffset(
                        level.random.nextInt(
                                MoundGuardianSpawnRules.BIOME_OFFSET_BOUND
                        ),
                        level.random.nextInt(
                                MoundGuardianSpawnRules.BIOME_OFFSET_BOUND
                        )
                ),
                0,
                MoundGuardianSpawnRules.biomeOffset(
                        level.random.nextInt(
                                MoundGuardianSpawnRules.BIOME_OFFSET_BOUND
                        ),
                        level.random.nextInt(
                                MoundGuardianSpawnRules.BIOME_OFFSET_BOUND
                        )
                )
        );
        EerieBiomeService.makeColumnEerie(level, biomeTarget);

        // Config.spawnAngryZombie gated TC4's biome spawn lists. The direct
        // Giant Brainy Zombie attempt in TileNode was gated only by hardNode.
        boolean enabled = ThaumcraftModernServerConfig.hardNodes();
        if (!enabled) {
            return;
        }
        boolean randomGate = level.random.nextBoolean();
        if (!randomGate) {
            return;
        }
        boolean playerNearby = level.getNearestPlayer(
                        position.getX() + 0.5D,
                        position.getY() + 0.5D,
                        position.getZ() + 0.5D,
                        MoundGuardianSpawnRules.PLAYER_RANGE,
                        false
                ) != null;
        if (!playerNearby) {
            return;
        }

        AABB nearby = new AABB(position).inflate(
                MoundGuardianSpawnRules.HORIZONTAL_CAP_RANGE,
                MoundGuardianSpawnRules.VERTICAL_CAP_RANGE,
                MoundGuardianSpawnRules.HORIZONTAL_CAP_RANGE
        );
        int guardians = level.getEntitiesOfClass(
                LegacyThaumcraftMob.class,
                nearby,
                mob -> mob.kind() == LegacyMobKind.FURIOUS_ZOMBIE
        ).size();
        if (!MoundGuardianSpawnRules.mayAttempt(
                enabled,
                ticks,
                randomGate,
                playerNearby,
                guardians
        )) {
            return;
        }

        LegacyThaumcraftMob guardian =
                ModEntities.FURIOUS_ZOMBIE.get().create(level);
        if (guardian == null) {
            return;
        }
        double spawnX = position.getX()
                + (level.random.nextDouble() - level.random.nextDouble())
                * MoundGuardianSpawnRules.SPAWN_SPREAD;
        double spawnY = position.getY() + level.random.nextInt(3) - 1;
        double spawnZ = position.getZ()
                + (level.random.nextDouble() - level.random.nextDouble())
                * MoundGuardianSpawnRules.SPAWN_SPREAD;
        guardian.moveTo(
                spawnX,
                spawnY,
                spawnZ,
                level.random.nextFloat() * 360.0F,
                0.0F
        );
        if (!Monster.checkMonsterSpawnRules(
                    ModEntities.FURIOUS_ZOMBIE.get(),
                    level,
                    MobSpawnType.NATURAL,
                    guardian.blockPosition(),
                    level.random
                )
                || !level.noCollision(guardian)) {
            return;
        }
        guardian.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(guardian.blockPosition()),
                MobSpawnType.NATURAL,
                null,
                null
        );
        if (level.addFreshEntity(guardian)) {
            level.levelEvent(2004, position, 0);
            guardian.spawnAnim();
        }
    }

    /**
     * TC4 permanently reduced empty pools during every 1200-tick node cleanup
     * and removed the node once no aspect remained.
     *
     * @return true when the node block itself was removed
     */
    private static boolean decayEmptyAspects(
            ServerLevel level,
            BlockPos position,
            AuraNodeBlockEntity node
    ) {
        AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
        Map<String, Integer> current =
                new LinkedHashMap<>(snapshot.aspectsCurrent());
        Map<String, Integer> maximum =
                new LinkedHashMap<>(snapshot.aspectsMaximum());
        boolean changed = false;
        for (String aspect : new ArrayList<>(current.keySet())) {
            if (current.get(aspect) > 0) {
                continue;
            }
            int nextMaximum = Math.max(0, maximum.get(aspect) - 1);
            maximum.put(aspect, nextMaximum);
            changed = true;
            if (nextMaximum <= 0 || level.random.nextInt(20) == 0) {
                current.remove(aspect);
                maximum.remove(aspect);
            }
        }
        if (!changed) {
            return false;
        }
        if (current.isEmpty()) {
            level.playSound(
                    null,
                    position,
                    ModSounds.CRAFT_FAIL.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            level.sendParticles(
                    com.thaumcraftmodern.registry.ModParticles.NODE_BURST.get(),
                    position.getX() + 0.5D,
                    position.getY() + 0.5D,
                    position.getZ() + 0.5D,
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            level.removeBlock(position, false);
            return true;
        }
        node.replaceAspects(snapshot.revision(), current, maximum);
        return false;
    }

    private static void regenerate(
            RandomSource random,
            int ticks,
            AuraNodeBlockEntity node,
            int stabilizerLock,
            long nowMillis
    ) {
        int interval = AuraNodeRegenerationPolicy.interval(
                node.snapshotState().modifier(),
                stabilizerLock
        );
        if (interval <= 0
                || node.regenerationWait() > 0
                || ticks % interval != 0) {
            return;
        }
        node.setLastActiveMillis(nowMillis);
        rechargeOneMissingAspect(random, node);
    }

    private static void catchUpRegeneration(
            RandomSource random,
            AuraNodeBlockEntity node,
            int stabilizerLock,
            long nowMillis
    ) {
        if (!node.consumeCatchUpPending()) {
            return;
        }
        AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
        int interval = AuraNodeRegenerationPolicy.interval(
                snapshot.modifier(),
                stabilizerLock
        );
        int maximumCycles = snapshot.aspectsMaximum().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int cycles = AuraNodeRegenerationPolicy.missedCycles(
                nowMillis,
                node.lastActiveMillis(),
                interval,
                maximumCycles
        );
        for (int cycle = 0; cycle < cycles; cycle++) {
            if (!rechargeOneMissingAspect(random, node)) {
                break;
            }
        }
        if (cycles > 0) {
            node.setLastActiveMillis(
                    AuraNodeRegenerationPolicy.advanceLastActive(
                            node.lastActiveMillis(),
                            interval,
                            cycles
                    )
            );
        }
    }

    private static boolean rechargeOneMissingAspect(
            RandomSource random,
            AuraNodeBlockEntity node
    ) {
        AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
        List<String> missing = snapshot.aspectsCurrent().keySet().stream()
                .filter(aspect -> snapshot.aspectsCurrent().get(aspect)
                        < snapshot.aspectsMaximum().get(aspect))
                .toList();
        if (missing.isEmpty()) {
            return false;
        }
        String aspect = missing.get(random.nextInt(missing.size()));
        Map<String, Integer> current =
                new LinkedHashMap<>(snapshot.aspectsCurrent());
        current.put(aspect, current.get(aspect) + 1);
        return node.replaceAspects(
                snapshot.revision(),
                current,
                snapshot.aspectsMaximum()
        );
    }

    private static void tickNodeStability(
            ServerLevel level,
            BlockPos position,
            AuraNodeBlockEntity node,
            AuraNodeState.Snapshot snapshot,
            int stabilizerLock,
            int ticks
    ) {
        if (ticks % 100 != 0) {
            return;
        }
        if (snapshot.type() == AuraNodeType.UNSTABLE
                && level.random.nextBoolean()) {
            if (stabilizerLock == 0) {
                dischargeUnstableAspect(level, position, node);
            } else if (level.random.nextInt(
                    AuraNodeRegenerationPolicy.unstableImprovementBound(
                            stabilizerLock
                    )
            ) == 42) {
                node.replaceType(AuraNodeType.NORMAL);
            }
        }
        if (snapshot.modifier() == AuraNodeModifier.FADING
                && stabilizerLock > 0
                && level.random.nextInt(
                        AuraNodeRegenerationPolicy.fadingImprovementBound(
                                stabilizerLock
                        )
                ) == 69) {
            node.replaceModifier(AuraNodeModifier.PALE);
        }
    }

    private static void discharge(
            ServerLevel level,
            BlockPos position,
            AuraNodeBlockEntity source,
            int stabilizerLock
    ) {
        if (stabilizerLock == 1) {
            return;
        }
        AuraNodeState.Snapshot sourceSnapshot =
                source.snapshotState().snapshot();
        if (sourceSnapshot.modifier() == AuraNodeModifier.FADING) {
            return;
        }
        boolean shiny = sourceSnapshot.type() == AuraNodeType.HUNGRY
                || sourceSnapshot.modifier() == AuraNodeModifier.BRIGHT;
        int interval = sourceSnapshot.modifier() == AuraNodeModifier.PALE
                ? 3
                : shiny ? 1 : 2;
        int ticks = source.classicTicks();
        if (ticks % interval != 0
                || sourceSnapshot.modifier() == AuraNodeModifier.PALE
                && level.random.nextBoolean()) {
            return;
        }

        int x = level.random.nextInt(5) - level.random.nextInt(5);
        int y = level.random.nextInt(5) - level.random.nextInt(5);
        int z = level.random.nextInt(5) - level.random.nextInt(5);
        if (x == 0 && y == 0 && z == 0) {
            return;
        }
        BlockPos targetPosition = position.offset(x, y, z);
        BlockEntity candidate = level.getBlockEntity(targetPosition);
        if (!(candidate instanceof AuraNodeBlockEntity target)) {
            return;
        }
        if (stabilizerLock(level, targetPosition) > 0) {
            return;
        }

        AuraNodeState.Snapshot targetSnapshot =
                target.snapshotState().snapshot();
        ClassicNodeDischarge.Result transfer =
                ClassicNodeDischarge.tryTransfer(
                        sourceSnapshot,
                        targetSnapshot,
                        level.random,
                        shiny
                ).orElse(null);
        if (transfer == null) {
            return;
        }

        if (!target.replaceAspects(
                targetSnapshot.revision(),
                transfer.victimCurrent(),
                transfer.victimMaximum()
        )) {
            return;
        }
        if (!source.replaceAspects(
                sourceSnapshot.revision(),
                transfer.predatorCurrent(),
                transfer.predatorMaximum()
        )) {
            target.replaceAspects(
                    targetSnapshot.revision() + 1L,
                    targetSnapshot.aspectsCurrent(),
                    targetSnapshot.aspectsMaximum()
            );
            return;
        }
        target.setRegenerationWait(regenerationInterval(targetSnapshot) / 2);
        nodeZap(level, targetPosition, position);
    }

    private static int stabilizerLock(
            ServerLevel level,
            BlockPos nodePosition
    ) {
        BlockPos stabilizerPosition = nodePosition.below();
        if (level.hasNeighborSignal(stabilizerPosition)
                || !(level.getBlockEntity(stabilizerPosition)
                instanceof NodeStabilizerBlockEntity stabilizer)) {
            return 0;
        }
        return stabilizer.advanced() ? 2 : 1;
    }

    private static int averagePool(AuraNodeState.Snapshot snapshot) {
        int current = snapshot.aspectsCurrent().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        int maximum = snapshot.aspectsMaximum().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        return (current + maximum) / 2;
    }

    private static int regenerationInterval(
            AuraNodeState.Snapshot snapshot
    ) {
        return AuraNodeRegenerationPolicy.interval(snapshot.modifier(), 0);
    }

    private static void dischargeUnstableAspect(
            ServerLevel level,
            BlockPos position,
            AuraNodeBlockEntity node
    ) {
        AuraNodeState.Snapshot snapshot = node.snapshotState().snapshot();
        List<String> primals = PrimalAspect.ordered().stream()
                .map(PrimalAspect::id)
                .filter(id -> snapshot.aspectsCurrent()
                        .getOrDefault(id, 0) > 0)
                .toList();
        if (primals.isEmpty()) {
            return;
        }
        String aspect = primals.get(level.random.nextInt(primals.size()));
        Map<String, Integer> current =
                new LinkedHashMap<>(snapshot.aspectsCurrent());
        current.put(aspect, current.get(aspect) - 1);
        if (node.replaceAspects(
                snapshot.revision(),
                current,
                snapshot.aspectsMaximum()
        )) {
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    position.getX() + 0.5D,
                    position.getY() + 0.5D,
                    position.getZ() + 0.5D,
                    8,
                    0.2D,
                    0.2D,
                    0.2D,
                    0.1D
            );
        }
    }

    private static void tickHungryEntities(
            ServerLevel level,
            BlockPos position,
            int ticks
    ) {
        Vec3 center = Vec3.atCenterOf(position);
        AABB range = new AABB(position).inflate(HUNGRY_RANGE);
        for (Entity entity : level.getEntities((Entity) null, range)) {
            if (!entity.isAlive()
                    || entity.isInvulnerable()
                    || entity instanceof Player player
                    && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            Vec3 delta = center.subtract(entity.position());
            double distance = delta.length();
            if (distance <= 0.0001D || distance >= HUNGRY_RANGE) {
                continue;
            }
            if (distance * distance < 2.0D) {
                entity.hurt(level.damageSources().fellOutOfWorld(), 1.0F);
            }

            /*
             * TC4's unequal X/Z and Y pull produces the familiar slingshot
             * around the collision point. Modern entity collision damps that
             * effect much harder, so restore the observed throw explicitly.
             */
            Vec3 velocity = HungryNodePhysics.apply(
                    entity.getDeltaMovement(),
                    entity.position(),
                    center,
                    ticks % 20 == 0
            );
            entity.setDeltaMovement(velocity);
            entity.hurtMarked = true;
        }
    }

    private static void hungryBreakBlock(
            ServerLevel level,
            BlockPos position
    ) {
        int x = position.getX() + level.random.nextInt(16)
                - level.random.nextInt(16);
        int y = position.getY() + level.random.nextInt(16)
                - level.random.nextInt(16);
        int z = position.getZ() + level.random.nextInt(16)
                - level.random.nextInt(16);
        y = Math.min(y, level.getHeight(
                net.minecraft.world.level.levelgen.Heightmap.Types
                        .MOTION_BLOCKING_NO_LEAVES,
                x,
                z
        ));
        Vec3 from = Vec3.atCenterOf(position);
        Vec3 to = Vec3.atCenterOf(new BlockPos(x, y, z));
        BlockHitResult hit = level.clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        ));
        if (hit.getType() != HitResult.Type.BLOCK
                || hit.getBlockPos().distSqr(position) >= 256.0D) {
            return;
        }
        BlockPos target = hit.getBlockPos();
        BlockState state = level.getBlockState(target);
        float hardness = state.getDestroySpeed(level, target);
        if (state.isAir() || hardness < 0.0F || hardness >= 5.0F) {
            return;
        }
        level.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                target.getX() + 0.5D,
                target.getY() + 0.5D,
                target.getZ() + 0.5D,
                20,
                0.4D,
                0.4D,
                0.4D,
                0.08D
        );
        level.destroyBlock(target, true);
    }

    private static void nodeZap(
            ServerLevel level,
            BlockPos from,
            BlockPos to
    ) {
        ModNetwork.sendToTrackingChunk(
                level,
                to,
                new NodeZapPacket(from, to, level.random.nextLong())
        );
        level.playSound(
                null,
                from,
                ModSounds.ZAP.get(),
                SoundSource.BLOCKS,
                0.1F,
                1.0F + level.random.nextFloat() * 0.2F
        );
    }
}
