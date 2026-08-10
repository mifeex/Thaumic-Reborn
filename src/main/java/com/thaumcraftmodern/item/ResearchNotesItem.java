package com.thaumcraftmodern.item;

import com.thaumcraftmodern.aspect.AspectCatalog;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeSync;
import com.thaumcraftmodern.research.HexResearchPuzzle;
import com.thaumcraftmodern.research.KnowledgeFragmentResearchService;
import com.thaumcraftmodern.research.ResearchPuzzleRegistry;
import com.thaumcraftmodern.research.ResearchColorResolver;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public final class ResearchNotesItem extends Item {
    public static final String FIRST_DISCOVERY = "first_discovery";
    private static final String RESEARCH_KEY = "Research";
    private static final String PLACEMENTS_KEY = "Placements";
    private static final String CELLS_KEY = "Cells";
    private static final String UNKNOWN_DISCOVERY_KEY = "UnknownDiscovery";

    public ResearchNotesItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(String researchId) {
        return create(researchId, RandomSource.create(researchId.hashCode()));
    }

    public static ItemStack create(String researchId, RandomSource random) {
        if (researchId == null || researchId.isBlank()) {
            throw new IllegalArgumentException("researchId must be non-blank");
        }
        ItemStack stack = new ItemStack(ModItems.RESEARCH_NOTES.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(RESEARCH_KEY, researchId);
        writeLayout(stack, researchId, random);
        tag.put(PLACEMENTS_KEY, new ListTag());
        return stack;
    }

    public static ItemStack createUnknownDiscovery() {
        ItemStack stack = new ItemStack(ModItems.RESEARCH_NOTES.get());
        stack.getOrCreateTag().putBoolean(UNKNOWN_DISCOVERY_KEY, true);
        return stack;
    }

    public static boolean isUnknownDiscovery(ItemStack stack) {
        return stack.getItem() instanceof ResearchNotesItem
                && stack.hasTag()
                && stack.getTag().getBoolean(UNKNOWN_DISCOVERY_KEY);
    }

    public static void ensureInitialized(ItemStack stack) {
        if (!(stack.getItem() instanceof ResearchNotesItem)) {
            return;
        }
        if (isUnknownDiscovery(stack)) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(RESEARCH_KEY)) {
            tag.putString(RESEARCH_KEY, FIRST_DISCOVERY);
        }
        if (!tag.contains(PLACEMENTS_KEY)) {
            tag.put(PLACEMENTS_KEY, new ListTag());
        }
    }

    public static String researchId(ItemStack stack) {
        if (isUnknownDiscovery(stack)) {
            return "";
        }
        ensureInitialized(stack);
        return stack.getOrCreateTag().getString(RESEARCH_KEY);
    }

    public static boolean matchesResearch(ItemStack stack, String researchId) {
        return stack.getItem() instanceof ResearchNotesItem
                && researchId != null
                && researchId.equals(researchId(stack));
    }

    /** TC4 colours tint layer 1 with the first aspect of the research tags. */
    public static int color(ItemStack stack) {
        if (isUnknownDiscovery(stack)) {
            return ResearchColorResolver.UNKNOWN_COLOR;
        }
        return ResearchColorResolver.color(researchId(stack));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isUnknownDiscovery(stack)) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        KnowledgeAccess.get(serverPlayer).ifPresent(knowledge -> {
            var candidates = KnowledgeFragmentResearchService.candidates(knowledge);
            if (candidates.isEmpty()) {
                stack.shrink(1);
                ItemStack refund = new ItemStack(
                        ModItems.KNOWLEDGE_FRAGMENT.get(),
                        7 + serverPlayer.getRandom().nextInt(3)
                );
                if (!serverPlayer.getInventory().add(refund)) {
                    serverPlayer.drop(refund, false);
                }
                level.playSound(null, serverPlayer.blockPosition(),
                        ModSounds.ERASE.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
                return;
            }
            var definition = candidates.get(
                    serverPlayer.getRandom().nextInt(candidates.size())
            );
            ItemStack revealed = create(definition.id(), serverPlayer.getRandom());
            knowledge.revealResearch(definition.id());
            serverPlayer.setItemInHand(hand, revealed);
            level.playSound(null, serverPlayer.blockPosition(),
                    ModSounds.WRITE.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            KnowledgeSync.send(serverPlayer, "knowledge_fragment_research");
        });
        return InteractionResultHolder.success(serverPlayer.getItemInHand(hand));
    }

    public static HexResearchPuzzle loadPuzzle(
            ItemStack stack,
            AspectCatalog catalog,
            PlayerThaumKnowledge knowledge
    ) {
        ensureInitialized(stack);
        CompoundTag root = stack.getOrCreateTag();
        if (!root.contains(CELLS_KEY, Tag.TAG_LIST)) {
            String researchId = researchId(stack);
            ResearchRegistry.find(researchId)
                    .filter(research -> !research.researchCost().isEmpty())
                    .ifPresent(research -> writeLayout(
                            stack,
                            researchId,
                            RandomSource.create(researchId.hashCode())
                    ));
        }
        HexResearchPuzzle puzzle;
        if (root.contains(CELLS_KEY, Tag.TAG_LIST)) {
            LinkedHashSet<HexResearchPuzzle.Cell> cells = new LinkedHashSet<>();
            LinkedHashMap<HexResearchPuzzle.Cell, String> anchors = new LinkedHashMap<>();
            ListTag layout = root.getList(CELLS_KEY, Tag.TAG_COMPOUND);
            for (int index = 0; index < layout.size(); index++) {
                CompoundTag cellTag = layout.getCompound(index);
                HexResearchPuzzle.Cell cell = new HexResearchPuzzle.Cell(
                        cellTag.getInt("Q"), cellTag.getInt("R")
                );
                cells.add(cell);
                if (cellTag.contains("Anchor", Tag.TAG_STRING)) {
                    anchors.put(cell, cellTag.getString("Anchor"));
                }
            }
            puzzle = new HexResearchPuzzle(catalog, cells, anchors);
        } else {
            puzzle = new HexResearchPuzzle(catalog);
        }

        if (root.contains(PLACEMENTS_KEY, Tag.TAG_LIST)) {
            ListTag placements = root.getList(PLACEMENTS_KEY, Tag.TAG_COMPOUND);
            for (int index = 0; index < placements.size(); index++) {
                CompoundTag placement = placements.getCompound(index);
                puzzle.restorePlacement(
                        new HexResearchPuzzle.Cell(
                                placement.getInt("Q"), placement.getInt("R")
                        ),
                        placement.getString("Aspect")
                );
            }
        } else if (root.contains(PLACEMENTS_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag placements = root.getCompound(PLACEMENTS_KEY);
            for (int q = -1; q <= 1; q++) {
                String key = Integer.toString(q);
                if (placements.contains(key)) {
                    puzzle.restorePlacement(
                            new HexResearchPuzzle.Cell(q, 0),
                            placements.getString(key)
                    );
                }
            }
        }
        return puzzle;
    }

    public static void savePuzzle(ItemStack stack, HexResearchPuzzle puzzle) {
        ensureInitialized(stack);
        ListTag placements = new ListTag();
        for (Map.Entry<HexResearchPuzzle.Cell, String> placement
                : puzzle.placements().entrySet()) {
            if (!puzzle.isAnchor(placement.getKey())) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Q", placement.getKey().q());
                entry.putInt("R", placement.getKey().r());
                entry.putString("Aspect", placement.getValue());
                placements.add(entry);
            }
        }
        stack.getOrCreateTag().put(PLACEMENTS_KEY, placements);
    }

    private static void writeLayout(
            ItemStack stack,
            String researchId,
            RandomSource random
    ) {
        var research = ResearchRegistry.find(researchId).orElse(null);
        HexResearchPuzzle.Layout layout = research == null
                ? HexResearchPuzzle.classicLayout(1, List.of(), random)
                : HexResearchPuzzle.classicLayout(
                        ResearchPuzzleRegistry.complexity(researchId),
                        research.researchCost(),
                        random
                );
        ListTag cells = new ListTag();
        for (HexResearchPuzzle.Cell cell : layout.cells()) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Q", cell.q());
            entry.putInt("R", cell.r());
            String anchor = layout.anchors().get(cell);
            if (anchor != null) entry.putString("Anchor", anchor);
            cells.add(entry);
        }
        stack.getOrCreateTag().put(CELLS_KEY, cells);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        if (isUnknownDiscovery(stack)) {
            tooltip.add(Component.translatable("item.researchnotes.unknown.1")
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("item.researchnotes.unknown.2")
                    .withStyle(ChatFormatting.BLUE));
            return;
        }
        tooltip.add(Component.translatable("tooltip.thaumcraftmodern.research_notes")
                .withStyle(ChatFormatting.DARK_PURPLE));
        ResearchRegistry.find(researchId(stack)).ifPresent(research ->
                tooltip.add(
                        Component.translatable(research.titleKey())
                                .withStyle(ChatFormatting.GRAY)
                )
        );
    }
}
