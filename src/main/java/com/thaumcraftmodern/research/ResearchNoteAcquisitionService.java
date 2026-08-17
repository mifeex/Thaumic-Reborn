package com.thaumcraftmodern.research;

import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Server-owned transaction that exchanges paper and ink for research notes.
 */
public final class ResearchNoteAcquisitionService {
    private ResearchNoteAcquisitionService() {
    }

    public static Result request(ServerPlayer player, String researchId) {
        ResearchDefinition research = ResearchRegistry.find(researchId).orElse(null);
        if (research == null) {
            return reject(
                    player,
                    researchId,
                    Result.UNKNOWN_RESEARCH,
                    Component.translatable("screen.thaumic_reborn.thaumonomicon.locked")
            );
        }

        return KnowledgeAccess.get(player)
                .map(knowledge -> {
                    if (knowledge.hasCompletedResearch(research.id())) {
                        return reject(
                                player,
                                research.id(),
                                Result.ALREADY_COMPLETED,
                                Component.translatable(
                                        "message.thaumic_reborn.research.already_known"
                                )
                        );
                    }
                    if (!ResearchProgressService.canCreateNotes(research, knowledge)) {
                        return reject(
                                player,
                                research.id(),
                                Result.UNAVAILABLE,
                                Component.translatable(
                                        "screen.thaumic_reborn.thaumonomicon.locked"
                                )
                        );
                    }
                    if (hasResearchNotes(player.getInventory(), research.id())) {
                        return reject(
                                player,
                                research.id(),
                                Result.ALREADY_HAS_NOTES,
                                Component.translatable("tc.research.hasnote")
                        );
                    }

                    int paperSlot = findPaper(player.getInventory());
                    int toolsSlot = findScribingTools(player.getInventory());
                    if (paperSlot < 0 || toolsSlot < 0) {
                        return reject(
                                player,
                                research.id(),
                                Result.MISSING_MATERIALS,
                                Component.translatable("tc.research.shortprim")
                        );
                    }

                    consumeMaterials(player.getInventory(), paperSlot, toolsSlot);
                    ItemStack notes = ResearchNotesItem.create(
                            research.id(),
                            player.getRandom()
                    );
                    if (!player.getInventory().add(notes)) {
                        player.drop(notes, false);
                    }
                    player.containerMenu.broadcastChanges();
                    player.level().playSound(
                            null,
                            player.blockPosition(),
                            ModSounds.LEARN.get(),
                            SoundSource.PLAYERS,
                            0.75F,
                            1.0F
                    );
                    player.displayClientMessage(
                            Component.translatable(
                                    "tc.research.popup",
                                    Component.translatable(research.titleKey())
                            ),
                            true
                    );
                    ResearchDiagnostics.log(
                            "SERVER_RESEARCH_NOTES_CREATED",
                            "player={} research={} paperSlot={} toolsSlot={} notes={}",
                            player.getGameProfile().getName(),
                            research.id(),
                            paperSlot,
                            toolsSlot,
                            notes
                    );
                    return Result.CREATED;
                })
                .orElseGet(() -> reject(
                        player,
                        research.id(),
                        Result.MISSING_KNOWLEDGE,
                        Component.translatable(
                                "screen.thaumic_reborn.thaumonomicon.locked"
                        )
                ));
    }

    public static boolean hasResearchNotes(Inventory inventory, String researchId) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ResearchNotesItem.matchesResearch(stack, researchId)) {
                return true;
            }
        }
        return false;
    }

    public static int findPaper(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(Items.PAPER)) {
                return slot;
            }
        }
        return -1;
    }

    public static int findScribingTools(Inventory inventory) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (ScribingToolsItem.hasInk(inventory.getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private static void consumeMaterials(
            Inventory inventory,
            int paperSlot,
            int toolsSlot
    ) {
        inventory.getItem(paperSlot).shrink(1);
        ScribingToolsItem.consumeInk(inventory.getItem(toolsSlot));
        inventory.setChanged();
    }

    private static Result reject(
            ServerPlayer player,
            String researchId,
            Result result,
            Component message
    ) {
        ResearchDiagnostics.log(
                "SERVER_RESEARCH_NOTES_REJECTED",
                "player={} research={} result={}",
                player.getGameProfile().getName(),
                researchId,
                result
        );
        player.displayClientMessage(message, true);
        return result;
    }

    public enum Result {
        CREATED,
        UNKNOWN_RESEARCH,
        MISSING_KNOWLEDGE,
        UNAVAILABLE,
        ALREADY_COMPLETED,
        ALREADY_HAS_NOTES,
        MISSING_MATERIALS
    }
}
