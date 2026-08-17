package com.thaumcraftmodern.item;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.research.ResearchCompletionService;
import com.thaumcraftmodern.research.ResearchColorResolver;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.WarpFeedbackPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DiscoveryItem extends Item {
    private static final String COPIES_KEY = "copies";
    public DiscoveryItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(String researchId) {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
        ItemStack stack = new ItemStack(com.thaumcraftmodern.registry.ModItems.DISCOVERY.get());
        ResearchCompletionService.writeDiscoveryPayload(stack, researchId);
        return stack;
    }

    public static String researchId(ItemStack stack) {
        return ResearchCompletionService.discoveryResearchId(stack);
    }

    public static boolean hasValidPayload(ItemStack stack) {
        return ResearchCompletionService.hasValidDiscoveryPayload(stack);
    }

    /** Discoveries share TC4's research-note overlay colour contract. */
    public static int color(ItemStack stack) {
        return ResearchColorResolver.color(researchId(stack));
    }

    public static int copies(ItemStack stack) {
        return Math.max(0, stack.getOrCreateTag().getInt(COPIES_KEY));
    }

    public static void setCopies(ItemStack stack, int copies) {
        stack.getOrCreateTag().putInt(COPIES_KEY, Math.max(0, copies));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        String researchId = researchId(stack);
        KnowledgeAccess.get(serverPlayer).ifPresent(knowledge -> {
            ResearchCompletionService.Result result =
                    ResearchCompletionService.complete(knowledge, stack);
            if (result == ResearchCompletionService.Result.ALREADY_COMPLETED) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.thaumic_reborn.research.already_known"),
                        true
                );
                return;
            }
            if (result != ResearchCompletionService.Result.COMPLETED) {
                serverPlayer.displayClientMessage(
                        Component.translatable(
                                "message.thaumic_reborn.research.completion_rejected",
                                result.name().toLowerCase(java.util.Locale.ROOT)
                        ),
                        true
                );
                ThaumcraftModern.LOGGER.warn(
                        "Rejected Discovery use: player={} research={} result={} validPayload={}",
                        serverPlayer.getGameProfile().getName(),
                        researchId,
                        result,
                        hasValidPayload(stack)
                );
                return;
            }

            serverPlayer.getServer().getRecipeManager()
                    .byKey(new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            "knowledge_fragment"
                    ))
                    .ifPresent(recipe -> serverPlayer.awardRecipes(List.of(recipe)));
            KnowledgeSync.send(serverPlayer, "research_completed:" + researchId);
            ResearchRegistry.find(researchId).ifPresent(definition -> {
                int normal = definition.completionWarp() / 2;
                int permanent = definition.completionWarp() - normal;
                if (permanent > 0) {
                    ModNetwork.sendTo(serverPlayer, new WarpFeedbackPacket(
                            WarpFeedbackPacket.PERMANENT,
                            permanent,
                            WarpFeedbackPacket.VISUAL_NONE
                    ));
                }
                if (normal > 0) {
                    ModNetwork.sendTo(serverPlayer, new WarpFeedbackPacket(
                            WarpFeedbackPacket.NORMAL,
                            normal,
                            WarpFeedbackPacket.VISUAL_NONE
                    ));
                }
            });
            level.playSound(
                    null,
                    serverPlayer.blockPosition(),
                    ModSounds.LEARN.get(),
                    SoundSource.MASTER,
                    0.8F,
                    1.0F
            );
            serverPlayer.displayClientMessage(
                    Component.translatable("message.thaumic_reborn.research.completed"),
                    false
            );
            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
        });
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.translatable("tooltip.thaumic_reborn.discovery"));
    }
}
