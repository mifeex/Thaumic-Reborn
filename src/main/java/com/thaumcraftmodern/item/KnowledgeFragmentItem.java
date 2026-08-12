package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

/** TC4 knowledge fragment: one use grants 1-2 points of every primal aspect. */
public final class KnowledgeFragmentItem extends Item {
    public KnowledgeFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        KnowledgeAccess.get(serverPlayer).ifPresent(knowledge -> {
            ArrayList<ScanFeedbackPacket.AspectGain> gains = new ArrayList<>();
            for (PrimalAspect aspect : PrimalAspect.ordered()) {
                int amount = serverPlayer.getRandom().nextInt(2) + 1;
                boolean newlyDiscovered = !knowledge.knowsAspect(aspect.id());
                knowledge.addAspectPoints(aspect.id(), amount);
                gains.add(new ScanFeedbackPacket.AspectGain(
                        aspect.id(),
                        amount,
                        knowledge.aspectAmount(aspect.id()),
                        newlyDiscovered
                ));
            }
            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
            KnowledgeSync.send(serverPlayer, "knowledge_fragment_used");
            ModNetwork.sendTo(serverPlayer, new ScanFeedbackPacket(
                    true,
                    "tc.addaspectpool",
                    "",
                    gains
            ));
        });
        return InteractionResultHolder.success(stack);
    }
}
