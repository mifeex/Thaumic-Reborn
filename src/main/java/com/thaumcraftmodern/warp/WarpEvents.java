package com.thaumcraftmodern.warp;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import com.thaumcraftmodern.registry.ModEffects;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Server-authoritative TC4 warp event table.
 */
public final class WarpEvents {
    public static final int CHECK_INTERVAL_TICKS = 2000;
    private static final List<String> PRIMALS = PrimalAspect.ordered().stream()
            .map(PrimalAspect::id)
            .toList();

    private WarpEvents() {
    }

    public static void check(ServerPlayer player) {
        KnowledgeAccess.get(player).ifPresent(knowledge -> check(player, knowledge));
    }

    static void check(ServerPlayer player, PlayerThaumKnowledge knowledge) {
        RandomSource random = player.getRandom();
        int warp = knowledge.totalWarp() + WarpGearService.equippedWarp(player);
        int actualWarp = knowledge.nonTemporaryWarp();
        int counter = knowledge.warpCounter();
        boolean triggered = false;

        if (counter > 0
                && warp > 0
                && random.nextInt(100) <= Math.sqrt(counter)) {
            triggered = true;
            warp = Math.min(100, (warp + warp + counter) / 3);
            knowledge.setWarpCounter(Math.max(
                    0,
                    (int) (counter - Math.max(5.0D, Math.sqrt(counter) * 2.0D))
            ));
            int effect = random.nextInt(warp);
            ItemStack fortressHelmet = player.getItemBySlot(
                    net.minecraft.world.entity.EquipmentSlot.HEAD);
            if (fortressHelmet.getItem()
                    instanceof com.thaumcraftmodern.item.FortressArmorItem
                    && Integer.valueOf(0).equals(
                            com.thaumcraftmodern.item.FortressArmorItem.mask(
                                    fortressHelmet))) {
                effect -= 2 + random.nextInt(4);
            }
            ModNetwork.sendTo(player, new WarpFeedbackPacket(
                    WarpFeedbackPacket.TEMPORARY,
                    0,
                    WarpFeedbackPacket.VISUAL_EVENT
            ));
            applyEvent(player, knowledge, warp, effect);
        }

        if (triggered) {
            unlockThresholdResearch(player, knowledge, actualWarp);
        }
        int temporary = knowledge.warp(WarpType.TEMPORARY);
        if (temporary > 0) {
            knowledge.setWarp(WarpType.TEMPORARY, temporary - 1);
        }
        KnowledgeSync.send(player, "warp_event_check");
    }

    static void applyEvent(
            ServerPlayer player,
            PlayerThaumKnowledge knowledge,
            int warp,
            int effect
    ) {
        int amplifier = Math.min(3, warp / 15);
        if (effect <= 0) {
            return;
        }
        if (effect <= 4) {
            grantWhisperPrimalPoints(knowledge);
            message(player, "warp.text.3");
        } else if (effect <= 8) {
            return;
        } else if (effect <= 12) {
            message(player, "warp.text.11");
        } else if (effect <= 16) {
            addEffect(player, ModEffects.VIS_EXHAUST.get(), 5000, amplifier);
            message(player, "warp.text.1");
        } else if (effect <= 20) {
            addEffect(
                    player,
                    ModEffects.THAUMARHIA.get(),
                    Math.min(32000, 10 * warp),
                    0
            );
            message(player, "warp.text.15");
        } else if (effect <= 24) {
            addEffect(player, ModEffects.UNNATURAL_HUNGER.get(), 5000, amplifier);
            message(player, "warp.text.2");
        } else if (effect <= 28) {
            message(player, "warp.text.12");
        } else if (effect <= 32) {
            spawnMist(player, 1);
        } else if (effect <= 36) {
            addEffect(
                    player,
                    ModEffects.BLURRED_VISION.get(),
                    Math.min(32000, 10 * warp),
                    0
            );
        } else if (effect <= 40) {
            addEffect(player, ModEffects.SUN_SCORNED.get(), 5000, amplifier);
            message(player, "warp.text.5");
        } else if (effect <= 44) {
            addEffect(player, MobEffects.REGENERATION, 1200, amplifier);
            message(player, "warp.text.9");
        } else if (effect <= 48) {
            addEffect(
                    player,
                    ModEffects.INFECTIOUS_VIS_EXHAUST.get(),
                    6000,
                    amplifier
            );
            message(player, "warp.text.1");
        } else if (effect <= 52) {
            addEffect(
                    player,
                    MobEffects.NIGHT_VISION,
                    Math.min(40 * warp, 6000),
                    0
            );
            message(player, "warp.text.10");
        } else if (effect <= 56) {
            addEffect(player, ModEffects.DEATH_GAZE.get(), 6000, amplifier);
            message(player, "warp.text.4");
        } else if (effect <= 60) {
            suddenlySpiders(player, warp, false);
        } else if (effect <= 64) {
            message(player, "warp.text.13");
        } else if (effect <= 68) {
            spawnMist(player, warp / 30);
        } else if (effect <= 72) {
            addEffect(
                    player,
                    MobEffects.BLINDNESS,
                    Math.min(32000, 5 * warp),
                    0
            );
        } else if (effect <= 75) {
            return;
        } else if (effect == 76) {
            int normal = knowledge.warp(WarpType.NORMAL);
            if (normal > 0) {
                knowledge.setWarp(WarpType.NORMAL, normal - 1);
                ModNetwork.sendTo(player, new WarpFeedbackPacket(
                        WarpFeedbackPacket.NORMAL,
                        -1,
                        WarpFeedbackPacket.VISUAL_NONE
                ));
            }
            message(player, "warp.text.14");
        } else if (effect <= 80) {
            addEffect(player, ModEffects.UNNATURAL_HUNGER.get(), 6000, amplifier);
            message(player, "warp.text.2");
        } else if (effect <= 84) {
            grantWhisperPrimalPoints(knowledge);
            message(player, "warp.text.3");
        } else if (effect <= 88) {
            return;
        } else if (effect <= 92) {
            suddenlySpiders(player, warp, true);
        } else {
            spawnMist(player, Math.min(8, warp / 15));
        }
    }

    public static void checkDeathGaze(ServerPlayer player) {
        MobEffectInstance gaze = player.getEffect(ModEffects.DEATH_GAZE.get());
        if (gaze == null) {
            return;
        }
        int range = Math.min(8 + gaze.getAmplifier() * 3, 24);
        AABB bounds = player.getBoundingBox().inflate(range);
        for (LivingEntity target : player.level().getEntitiesOfClass(
                LivingEntity.class,
                bounds,
                candidate -> candidate != player && candidate.isAlive()
        )) {
            if (target instanceof ServerPlayer
                    && !player.server.isPvpAllowed()) {
                continue;
            }
            if (!player.hasLineOfSight(target)
                    || target.hasEffect(MobEffects.WITHER)) {
                continue;
            }
            if (target instanceof Mob mob) {
                mob.setTarget(player);
            }
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80));
        }
    }

    private static void unlockThresholdResearch(
            ServerPlayer player,
            PlayerThaumKnowledge knowledge,
            int actualWarp
    ) {
        if (actualWarp > 10 && knowledge.completeResearch("bathsalts")) {
            message(player, "warp.text.8");
        }
        if (actualWarp > 25 && knowledge.completeResearch("eldritchminor")) {
            grantPrimalPoints(player, knowledge, 10);
        }
        if (actualWarp > 50 && knowledge.completeResearch("eldritchmajor")) {
            grantPrimalPoints(player, knowledge, 20);
        }
    }

    private static void grantPrimalPoints(
            ServerPlayer player,
            PlayerThaumKnowledge knowledge,
            int times
    ) {
        int amount = 1 + player.getRandom().nextInt(Math.max(1, times));
        for (int index = 0; index < amount; index++) {
            knowledge.addAspectPoints(
                    PRIMALS.get(player.getRandom().nextInt(PRIMALS.size())),
                    1
            );
        }
    }

    static void grantWhisperPrimalPoints(PlayerThaumKnowledge knowledge) {
        for (String primal : PRIMALS) {
            knowledge.addAspectPoints(primal, 1);
        }
    }

    private static void addEffect(
            LivingEntity entity,
            MobEffect effect,
            int duration,
            int amplifier
    ) {
        entity.addEffect(new MobEffectInstance(
                effect,
                duration,
                amplifier,
                true,
                true
        ));
    }

    private static void spawnMist(ServerPlayer player, int guardians) {
        ModNetwork.sendTo(player, new WarpFeedbackPacket(
                WarpFeedbackPacket.TEMPORARY,
                0,
                WarpFeedbackPacket.VISUAL_MIST
        ));
        for (int index = 0; index < Math.min(8, guardians); index++) {
            spawnNear(player, ModEntities.ELDRITCH_GUARDIAN.get().create(player.level()), true);
        }
        message(player, "warp.text.6");
    }

    private static void suddenlySpiders(
            ServerPlayer player,
            int warp,
            boolean real
    ) {
        for (int index = 0; index < Math.min(50, warp); index++) {
            spawnNear(player, ModEntities.MIND_SPIDER.get().create(player.level()), real);
        }
        message(player, "warp.text.7");
    }

    private static void spawnNear(
            ServerPlayer player,
            LegacyThaumcraftMob mob,
            boolean hostile
    ) {
        if (mob == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        RandomSource random = player.getRandom();
        for (int attempt = 0; attempt < 50; attempt++) {
            int x = player.blockPosition().getX()
                    + random.nextIntBetweenInclusive(7, 24)
                    * random.nextIntBetweenInclusive(-1, 1);
            int z = player.blockPosition().getZ()
                    + random.nextIntBetweenInclusive(7, 24)
                    * random.nextIntBetweenInclusive(-1, 1);
            int y = level.getHeight(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    x,
                    z
            );
            BlockPos position = new BlockPos(x, y, z);
            mob.moveTo(x + 0.5D, y, z + 0.5D, random.nextFloat() * 360.0F, 0);
            if (level.noCollision(mob)
                    && level.getBlockState(position.below()).isFaceSturdy(
                            level,
                            position.below(),
                            net.minecraft.core.Direction.UP
                    )) {
                mob.setTarget(player);
                if (!hostile) {
                    mob.setWarpIllusion(player);
                }
                level.addFreshEntity(mob);
                return;
            }
        }
    }

    private static void message(ServerPlayer player, String key) {
        ModNetwork.sendTo(player, new WarpFeedbackPacket(
                WarpFeedbackPacket.TEMPORARY,
                0,
                WarpFeedbackPacket.VISUAL_NONE,
                key
        ));
    }
}
