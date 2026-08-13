package com.thaumcraftmodern.world.menu;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectCombinationService;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ResearchTableFeedbackPacket;
import com.thaumcraftmodern.network.packet.ScanFeedbackPacket;
import com.thaumcraftmodern.research.HexResearchPuzzle;
import com.thaumcraftmodern.research.ResearchDiagnostics;
import com.thaumcraftmodern.research.ResearchCompletionService;
import com.thaumcraftmodern.research.ResearchExpertiseService;
import com.thaumcraftmodern.research.ResearchDuplicationService;
import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import java.util.List;
import java.util.Locale;

public final class ResearchTableMenu extends AbstractContainerMenu {
    public static final int DUPLICATE_BUTTON = 5;
    private static final int COMBINE_BASE = 10;
    private static final int MASTERY_COMBINE_BASE = 5_000;
    private static final int PLACE_BASE = 10_000;
    private static final int ERASE_BASE = 20_000;
    private static final int PUZZLE_DIAMETER = HexResearchPuzzle.MAX_RADIUS * 2 + 1;

    private final ResearchTableBlockEntity table;
    private final ContainerLevelAccess access;

    public ResearchTableMenu(int containerId, Inventory playerInventory, ResearchTableBlockEntity table) {
        super(ModMenus.RESEARCH_TABLE.get(), containerId);
        this.table = table;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), table.getBlockPos());

        addSlot(new SlotItemHandler(table.items(), ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT, 14, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ScribingToolsItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        addSlot(new SlotItemHandler(table.items(), ResearchTableBlockEntity.NOTES_SLOT, 70, 10) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ResearchNotesItem
                        || stack.getItem() instanceof DiscoveryItem;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 48 + column * 18, 175 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 48 + column * 18, 233));
        }
    }

    public static ResearchTableMenu fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos position = buffer.readBlockPos();
        if (inventory.player.level().getBlockEntity(position) instanceof ResearchTableBlockEntity table) {
            return new ResearchTableMenu(containerId, inventory, table);
        }
        ThaumcraftModern.LOGGER.warn(
                "Research Table menu opened before its block entity was available at {}; "
                        + "using a non-valid client placeholder",
                position
        );
        ResearchTableBlockEntity placeholder =
                new ResearchTableBlockEntity(position, ModBlocks.RESEARCH_TABLE.get().defaultBlockState());
        return new ResearchTableMenu(containerId, inventory, placeholder);
    }

    public ItemStack notes() {
        return table.items().getStackInSlot(ResearchTableBlockEntity.NOTES_SLOT);
    }

    public ItemStack scribingTools() {
        return table.items().getStackInSlot(ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT);
    }

    public HexResearchPuzzle puzzle(Player player) {
        ItemStack notes = notes();
        if (!(notes.getItem() instanceof ResearchNotesItem)) {
            return new HexResearchPuzzle(AspectRegistryRuntime.catalog());
        }
        PlayerThaumKnowledge knowledge = KnowledgeAccess.get(player).orElseGet(PlayerThaumKnowledge::new);
        return ResearchNotesItem.loadPuzzle(notes, AspectRegistryRuntime.catalog(), knowledge);
    }

    public static int encodePlacement(
            HexResearchPuzzle.Cell cell,
            int paletteIndex
    ) {
        return PLACE_BASE + encodeCell(cell) * palette().size() + paletteIndex;
    }

    public static int encodeErase(HexResearchPuzzle.Cell cell) {
        return ERASE_BASE + encodeCell(cell);
    }

    public static int encodeCombination(int firstPaletteIndex, int secondPaletteIndex) {
        List<String> palette = palette();
        if (firstPaletteIndex < 0 || firstPaletteIndex >= palette.size()
                || secondPaletteIndex < 0 || secondPaletteIndex >= palette.size()) {
            throw new IllegalArgumentException("aspect palette index is out of bounds");
        }
        return COMBINE_BASE + firstPaletteIndex * palette.size() + secondPaletteIndex;
    }

    public static int encodeMasteryCombination(int resultPaletteIndex) {
        if (resultPaletteIndex < 0 || resultPaletteIndex >= palette().size()) {
            throw new IllegalArgumentException(
                    "aspect palette index is out of bounds"
            );
        }
        return MASTERY_COMBINE_BASE + resultPaletteIndex;
    }

    public static List<String> palette() {
        return AspectRegistryRuntime.catalog().definitions().stream()
                .map(com.thaumcraftmodern.aspect.AspectDefinition::id)
                // TC4 AspectList.getAspectsSorted() compares Aspect.getTag()
                // with String.compareTo; modern aspect IDs are those tags.
                .sorted()
                .toList();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || !stillValid(player)) {
            return false;
        }
        PlayerThaumKnowledge knowledge = KnowledgeAccess.get(serverPlayer).orElse(null);
        if (knowledge == null) {
            ResearchDiagnostics.log(
                    "SERVER_BUTTON",
                    "player={} container={} button={} rejected=no_knowledge",
                    serverPlayer.getGameProfile().getName(),
                    containerId,
                    id
            );
            return false;
        }
        List<String> palette = palette();
        ResearchDiagnostics.log(
                "SERVER_BUTTON",
                "player={} container={} button={} paletteSize={} amounts={} notes={}",
                serverPlayer.getGameProfile().getName(),
                containerId,
                id,
                palette.size(),
                knowledge.aspectAmounts(),
                notes().getTag()
        );
        if (id == DUPLICATE_BUTTON) {
            ItemStack discovery = table.items().getStackInSlot(
                    ResearchTableBlockEntity.NOTES_SLOT
            );
            ResearchDuplicationService.Result result =
                    ResearchDuplicationService.duplicate(
                            serverPlayer,
                            knowledge,
                            discovery
                    );
            ResearchDiagnostics.log(
                    "SERVER_RESEARCH_DUPLICATE",
                    "player={} research={} copies={} result={}",
                    serverPlayer.getGameProfile().getName(),
                    discovery.getItem() instanceof DiscoveryItem
                            ? DiscoveryItem.researchId(discovery)
                            : "",
                    discovery.getItem() instanceof DiscoveryItem
                            ? DiscoveryItem.copies(discovery)
                            : 0,
                    result
            );
            if (result == ResearchDuplicationService.Result.CREATED) {
                table.items().setStackInSlot(
                        ResearchTableBlockEntity.NOTES_SLOT,
                        discovery
                );
                table.setChanged();
                broadcastChanges();
                KnowledgeSync.send(serverPlayer, "research_table.duplicate");
                serverPlayer.level().playSound(
                        null,
                        table.getBlockPos(),
                        ModSounds.LEARN.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                );
                return true;
            }
            return false;
        }
        int combineLimit = COMBINE_BASE + palette.size() * palette.size();
        if (id >= COMBINE_BASE && id < combineLimit) {
            int encoded = id - COMBINE_BASE;
            int firstIndex = encoded / palette.size();
            int secondIndex = encoded % palette.size();
            String firstAspect = palette.get(firstIndex);
            String secondAspect = palette.get(secondIndex);
            return combineAspects(
                    serverPlayer,
                    knowledge,
                    firstAspect,
                    secondAspect,
                    "research_table.combine"
            );
        }

        int masteryLimit = MASTERY_COMBINE_BASE + palette.size();
        if (id >= MASTERY_COMBINE_BASE && id < masteryLimit) {
            if (!ResearchExpertiseService.canCombineFromPalette(knowledge)) {
                ResearchDiagnostics.log(
                        "SERVER_MASTERY_COMBINE_REJECTED",
                        "player={} button={} reason=research_incomplete",
                        serverPlayer.getGameProfile().getName(),
                        id
                );
                return false;
            }
            String resultAspect = palette.get(id - MASTERY_COMBINE_BASE);
            var definition = AspectRegistryRuntime.find(resultAspect)
                    .orElse(null);
            if (definition == null || !definition.isCompound()) {
                return false;
            }
            return combineAspects(
                    serverPlayer,
                    knowledge,
                    definition.components().get(0),
                    definition.components().get(1),
                    "research_table.mastery_combine"
            );
        }

        ItemStack tools = table.items().getStackInSlot(ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT);
        ItemStack notes = table.items().getStackInSlot(ResearchTableBlockEntity.NOTES_SLOT);
        if (!(tools.getItem() instanceof ScribingToolsItem)
                || !(notes.getItem() instanceof ResearchNotesItem)) {
            ResearchDiagnostics.log(
                    "SERVER_WORKSPACE_REJECTED",
                    "player={} tools={} notes={}",
                    serverPlayer.getGameProfile().getName(),
                    tools,
                    notes
            );
            sendFeedback(serverPlayer,
                    Component.translatable(
                            tools.getItem() instanceof ScribingToolsItem
                                    ? "screen.thaumcraftmodern.research_table.no_notes"
                                    : "screen.thaumcraftmodern.research_table.no_ink"
                    ), false
            );
            return false;
        }

        HexResearchPuzzle puzzle = ResearchNotesItem.loadPuzzle(
                notes,
                AspectRegistryRuntime.catalog(),
                knowledge
        );
        ResearchDiagnostics.log(
                "SERVER_NOTES_LOADED",
                "player={} research={} persistedTag={} restoredPlacements={} amounts={}",
                serverPlayer.getGameProfile().getName(),
                ResearchNotesItem.researchId(notes),
                notes.getTag(),
                puzzle.placements(),
                knowledge.aspectAmounts()
        );

        if (id >= PLACE_BASE && id < ERASE_BASE) {
            int encoded = id - PLACE_BASE;
            HexResearchPuzzle.Cell cell = decodeCell(encoded / palette.size());
            int paletteIndex = encoded % palette.size();
            if (paletteIndex < 0 || paletteIndex >= palette.size()) {
                ResearchDiagnostics.log(
                        "SERVER_PLACE_REJECTED",
                        "player={} reason=palette_index index={} size={} encoded={}",
                        serverPlayer.getGameProfile().getName(),
                        paletteIndex,
                        palette.size(),
                        id
                );
                return false;
            }
            String aspectId = palette.get(paletteIndex);
            int amountBefore = knowledge.aspectAmount(aspectId);
            ResearchDiagnostics.log(
                    "SERVER_PLACE_BEGIN",
                    "player={} cell={} aspect={} amountBefore={} placementsBefore={} encoded={}",
                    serverPlayer.getGameProfile().getName(),
                    cell,
                    aspectId,
                    amountBefore,
                    puzzle.placements(),
                    id
            );
            boolean consumeAspect =
                    ResearchExpertiseService.placementCostsAspect(
                            knowledge,
                            serverPlayer.getRandom().nextFloat()
                    );
            HexResearchPuzzle.PlacementResult result =
                    puzzle.place(cell, aspectId, knowledge, consumeAspect);
            ResearchDiagnostics.log(
                    "SERVER_PLACE_RESULT",
                    "player={} cell={} aspect={} result={} amountBefore={} amountAfter={} placementsAfter={}",
                    serverPlayer.getGameProfile().getName(),
                    cell,
                    aspectId,
                    result,
                    amountBefore,
                    knowledge.aspectAmount(aspectId),
                    puzzle.placements()
            );
            if (result == HexResearchPuzzle.PlacementResult.PLACED
                    || result == HexResearchPuzzle.PlacementResult.PLACED_AND_COMPLETED) {
                if (!consumeAspect) {
                    playExpertiseRewardSound(serverPlayer);
                }
                consumeInk(tools);
                ResearchNotesItem.savePuzzle(notes, puzzle);
                table.items().setStackInSlot(ResearchTableBlockEntity.NOTES_SLOT, notes);
                if (result == HexResearchPuzzle.PlacementResult.PLACED_AND_COMPLETED) {
                    String completedResearchId = ResearchNotesItem.researchId(notes);
                    ResearchCompletionService.markDiscoveryReady(
                            knowledge,
                            completedResearchId
                    );
                    table.items().setStackInSlot(
                            ResearchTableBlockEntity.NOTES_SLOT,
                            DiscoveryItem.create(completedResearchId)
                    );
                    sendFeedback(
                            serverPlayer,
                            Component.translatable(
                                    "puzzle.thaumcraftmodern.first_discovery.complete"
                            ),
                            true
                    );
                    serverPlayer.level().playSound(
                            null,
                            table.getBlockPos(),
                            ModSounds.LEARN.get(),
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F
                    );
                }
                table.setChanged();
                broadcastChanges();
                ResearchDiagnostics.log(
                        "SERVER_NOTES_SAVED",
                        "player={} research={} notesAfter={} finalAmounts={}",
                        serverPlayer.getGameProfile().getName(),
                        ResearchNotesItem.researchId(notes),
                        notes.getTag(),
                        knowledge.aspectAmounts()
                );
                KnowledgeSync.send(serverPlayer, "research_table.place");
                return true;
            }
            sendPuzzleResult(serverPlayer, result.name());
            return false;
        }

        HexResearchPuzzle.Cell cell = decodeCell(id - ERASE_BASE);
        String erasedAspect = puzzle.aspectAt(cell).orElse("");
        HexResearchPuzzle.EraseResult result = puzzle.erase(cell);
        ResearchDiagnostics.log(
                "SERVER_ERASE_RESULT",
                "player={} cell={} result={} placementsAfter={}",
                serverPlayer.getGameProfile().getName(),
                cell,
                result,
                puzzle.placements()
        );
        if (result == HexResearchPuzzle.EraseResult.ERASED) {
            boolean refunded = !erasedAspect.isBlank()
                    && ResearchExpertiseService.refundsErasedAspect(
                            knowledge,
                            serverPlayer.getRandom().nextFloat()
                    );
            if (refunded) {
                knowledge.addAspectPoints(erasedAspect, 1);
                playExpertiseRewardSound(serverPlayer);
            }
            consumeInk(tools);
            ResearchNotesItem.savePuzzle(notes, puzzle);
            table.items().setStackInSlot(ResearchTableBlockEntity.NOTES_SLOT, notes);
            table.setChanged();
            broadcastChanges();
            if (refunded) {
                KnowledgeSync.send(serverPlayer, "research_table.erase_refund");
            }
            return true;
        }
        sendPuzzleResult(serverPlayer, result.name());
        return false;
    }

    private static int encodeCell(HexResearchPuzzle.Cell cell) {
        if (Math.abs(cell.q()) > HexResearchPuzzle.MAX_RADIUS
                || Math.abs(cell.r()) > HexResearchPuzzle.MAX_RADIUS) {
            throw new IllegalArgumentException("puzzle cell is outside encoding bounds");
        }
        return (cell.q() + HexResearchPuzzle.MAX_RADIUS) * PUZZLE_DIAMETER
                + cell.r() + HexResearchPuzzle.MAX_RADIUS;
    }

    private static HexResearchPuzzle.Cell decodeCell(int encoded) {
        int q = encoded / PUZZLE_DIAMETER - HexResearchPuzzle.MAX_RADIUS;
        int r = encoded % PUZZLE_DIAMETER - HexResearchPuzzle.MAX_RADIUS;
        return new HexResearchPuzzle.Cell(q, r);
    }

    private boolean combineAspects(
            ServerPlayer player,
            PlayerThaumKnowledge knowledge,
            String firstAspect,
            String secondAspect,
            String syncReason
    ) {
        int firstBefore = knowledge.aspectAmount(firstAspect);
        int secondBefore = knowledge.aspectAmount(secondAspect);
        ResearchDiagnostics.log(
                "SERVER_COMBINE_BEGIN",
                "player={} first={}({}) second={}({})",
                player.getGameProfile().getName(),
                firstAspect,
                firstBefore,
                secondAspect,
                secondBefore
        );
        AspectCombinationService.Result result = AspectCombinationService.combine(
                AspectRegistryRuntime.catalog(),
                knowledge,
                firstAspect,
                secondAspect
        );
        ResearchDiagnostics.log(
                "SERVER_COMBINE_RESULT",
                "player={} status={} result={} created={} newlyDiscovered={} firstAfter={} secondAfter={} amounts={}",
                player.getGameProfile().getName(),
                result.status(),
                result.resultAspectId(),
                result.createdAmount(),
                result.newlyDiscovered(),
                knowledge.aspectAmount(firstAspect),
                knowledge.aspectAmount(secondAspect),
                knowledge.aspectAmounts()
        );
        if (!result.combined()) {
            playCombinationResultSound(player, false);
            if (result.status() == AspectCombinationService.Status.NO_COMBINATION) {
                KnowledgeSync.send(player, syncReason + ":failed_combination");
            }
            return true;
        }

        playCombinationResultSound(player, true);
        KnowledgeSync.send(player, syncReason);
        ModNetwork.sendTo(player, new ScanFeedbackPacket(
                true,
                "tc.addaspectpool",
                "",
                List.of(new ScanFeedbackPacket.AspectGain(
                        result.resultAspectId(),
                        result.createdAmount(),
                        knowledge.aspectAmount(result.resultAspectId()),
                        result.newlyDiscovered()
                ))
        ));
        if (result.newlyDiscovered()) {
            player.displayClientMessage(
                    Component.translatable(
                            "message.thaumcraftmodern.scan.aspect_discovered",
                            Component.translatable(
                                    "aspect.thaumcraftmodern."
                                            + result.resultAspectId()
                            )
                    ),
                    false
            );
        }
        return true;
    }

    private static void playCombinationResultSound(
            ServerPlayer player,
            boolean successful
    ) {
        player.playNotifySound(
                successful ? ModSounds.HH_ON.get() : ModSounds.HH_OFF.get(),
                SoundSource.PLAYERS,
                successful ? 0.3F : 0.2F,
                successful
                        ? 1.0F
                        : 1.0F + player.getRandom().nextFloat() * 0.1F
        );
    }

    private void playExpertiseRewardSound(ServerPlayer player) {
        player.level().playSound(
                null,
                table.getBlockPos(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.2F,
                0.9F + player.getRandom().nextFloat() * 0.2F
        );
    }

    private static void consumeInk(ItemStack tools) {
        com.thaumcraftmodern.item.ScribingToolsItem.consumeInk(tools);
    }

    private static void sendPuzzleResult(ServerPlayer player, String resultName) {
        sendFeedback(player,
                Component.translatable(
                        "puzzle.thaumcraftmodern.first_discovery.error."
                                + resultName.toLowerCase(Locale.ROOT)
                ), false
        );
    }

    private static void sendFeedback(
            ServerPlayer player,
            Component message,
            boolean success
    ) {
        ModNetwork.sendTo(
                player,
                new ResearchTableFeedbackPacket(message, success)
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        int tableSlots = 2;
        if (index < tableSlots) {
            if (!moveItemStackTo(source, tableSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ScribingToolsItem) {
            if (!moveItemStackTo(source, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (source.getItem() instanceof ResearchNotesItem
                || source.getItem() instanceof DiscoveryItem) {
            if (!moveItemStackTo(source, 1, 2, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
        if (source.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RESEARCH_TABLE.get());
    }
}
