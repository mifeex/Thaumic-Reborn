package com.thaumcraftmodern.item;

import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Reading the rites completes TC4's hidden CRIMSON stub on the server.
 */
public final class CrimsonRitesItem extends Item {
    public CrimsonRitesItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            KnowledgeAccess.get(serverPlayer).ifPresent(knowledge -> {
                if (!knowledge.completeResearch("crimson")) {
                    return;
                }
                knowledge.recordResearchCriterion(
                        "thaumic_reborn:legacy_clue/crimson"
                );
                KnowledgeSync.send(serverPlayer, "crimson_rites");
                serverPlayer.displayClientMessage(
                        Component.translatable(
                                "message.thaumic_reborn.research.crimson"
                        ),
                        true
                );
                level.playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS,
                        0.75F,
                        1.0F
                );
            });
        }
        return InteractionResultHolder.sidedSuccess(
                stack,
                level.isClientSide()
        );
    }
}
