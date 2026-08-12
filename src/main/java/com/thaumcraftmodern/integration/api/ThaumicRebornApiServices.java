package com.thaumcraftmodern.integration.api;

import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.arcane.ArcaneShapedRecipe;
import com.thaumcraftmodern.arcane.ModArcaneRecipes;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.focus.WandFocusService;
import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.item.RunicShieldService;
import com.thaumcraftmodern.crucible.CrucibleRecipeDefinition;
import com.thaumcraftmodern.crucible.CrucibleRecipeRegistry;
import com.thaumcraftmodern.crucible.ItemAspectRegistry;
import com.thaumcraftmodern.infusion.InfusionRecipeDefinition;
import com.thaumcraftmodern.infusion.InfusionRecipeRegistry;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.research.ResearchDefinition;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.scan.ScanDefinition;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.wand.WandCapDefinition;
import com.thaumcraftmodern.wand.WandRodDefinition;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumicreborn.api.ApiServices;
import com.thaumicreborn.api.aspect.Aspect;
import com.thaumicreborn.api.aspect.AspectApi;
import com.thaumicreborn.api.aura.AuraApi;
import com.thaumicreborn.api.aura.AuraNode;
import com.thaumicreborn.api.crafting.CrucibleRecipe;
import com.thaumicreborn.api.crafting.InfusionRecipe;
import com.thaumicreborn.api.crafting.RecipeApi;
import com.thaumicreborn.api.knowledge.KnowledgeApi;
import com.thaumicreborn.api.knowledge.ResearchStatus;
import com.thaumicreborn.api.knowledge.WarpType;
import com.thaumicreborn.api.equipment.EquipmentApi;
import com.thaumicreborn.api.essentia.EssentiaApi;
import com.thaumicreborn.api.essentia.EssentiaTransport;
import com.thaumicreborn.api.focus.FocusApi;
import com.thaumicreborn.api.focus.FocusBehavior;
import com.thaumicreborn.api.focus.FocusDefinition;
import com.thaumicreborn.api.research.Research;
import com.thaumicreborn.api.research.ResearchApi;
import com.thaumicreborn.api.scan.Scan;
import com.thaumicreborn.api.scan.ScanApi;
import com.thaumicreborn.api.scan.ScanTargetType;
import com.thaumicreborn.api.wand.WandApi;
import com.thaumicreborn.api.wand.WandCap;
import com.thaumicreborn.api.wand.WandForm;
import com.thaumicreborn.api.wand.WandRod;
import com.thaumicreborn.api.wand.WandState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Internal adapter from the stable addon API to current implementation services. */
public final class ThaumicRebornApiServices implements ApiServices {
    private final AspectApi aspects = new AspectApiImpl();
    private final ResearchApi research = new ResearchApiImpl();
    private final KnowledgeApi knowledge = new KnowledgeApiImpl();
    private final ScanApi scans = new ScanApiImpl();
    private final RecipeApi recipes = new RecipeApiImpl();
    private final WandApi wands = new WandApiImpl();
    private final EssentiaApi essentia = new EssentiaApiImpl();
    private final AuraApi aura = new AuraApiImpl();
    private final FocusApi foci = new FocusApiImpl();
    private final EquipmentApi equipment = new EquipmentApiImpl();

    @Override public AspectApi aspects() { return aspects; }
    @Override public ResearchApi research() { return research; }
    @Override public KnowledgeApi knowledge() { return knowledge; }
    @Override public ScanApi scans() { return scans; }
    @Override public RecipeApi recipes() { return recipes; }
    @Override public WandApi wands() { return wands; }
    @Override public EssentiaApi essentia() { return essentia; }
    @Override public AuraApi aura() { return aura; }
    @Override public FocusApi foci() { return foci; }
    @Override public EquipmentApi equipment() { return equipment; }

    private static final class AspectApiImpl implements AspectApi {
        @Override
        public Optional<Aspect> find(String id) {
            return AspectRegistryRuntime.find(id).map(ThaumicRebornApiServices::aspect);
        }

        @Override
        public List<Aspect> all() {
            return AspectRegistryRuntime.catalog().definitions().stream()
                    .map(ThaumicRebornApiServices::aspect).toList();
        }

        @Override
        public Map<String, Integer> aspects(ItemStack stack) {
            return ItemAspectRegistry.aspects(stack).orElse(Map.of());
        }
    }

    private static final class ResearchApiImpl implements ResearchApi {
        @Override
        public Optional<Research> find(String id) {
            return ResearchRegistry.find(id).map(ThaumicRebornApiServices::research);
        }

        @Override
        public List<Research> all() {
            return ResearchRegistry.all().stream()
                    .map(ThaumicRebornApiServices::research).toList();
        }
    }

    private static final class KnowledgeApiImpl implements KnowledgeApi {
        @Override
        public ResearchStatus researchStatus(Player player, String researchId) {
            return KnowledgeAccess.get(player).map(knowledge -> {
                if (knowledge.hasCompletedResearch(researchId)) return ResearchStatus.COMPLETE;
                if (knowledge.hasRevealedResearch(researchId)) return ResearchStatus.REVEALED;
                return ResearchStatus.UNKNOWN;
            }).orElse(ResearchStatus.UNKNOWN);
        }

        @Override public boolean knowsResearch(Player player, String id) {
            return researchStatus(player, id) != ResearchStatus.UNKNOWN;
        }

        @Override public boolean hasCompletedResearch(Player player, String id) {
            return researchStatus(player, id) == ResearchStatus.COMPLETE;
        }

        @Override public Set<String> knownAspects(Player player) {
            return KnowledgeAccess.get(player).map(PlayerThaumKnowledge::knownAspects).orElse(Set.of());
        }

        @Override public int aspectAmount(Player player, String id) {
            return KnowledgeAccess.get(player).map(value -> value.aspectAmount(id)).orElse(0);
        }

        @Override public Set<String> completedScans(Player player) {
            return KnowledgeAccess.get(player).map(PlayerThaumKnowledge::scans).orElse(Set.of());
        }

        @Override public int warp(Player player, WarpType type) {
            return KnowledgeAccess.get(player).map(value -> value.warp(warpType(type))).orElse(0);
        }

        @Override public boolean revealResearch(ServerPlayer player, String id) {
            requireResearch(id);
            AtomicBoolean changed = new AtomicBoolean();
            KnowledgeAccess.mutate(player, value -> changed.set(value.revealResearch(id)));
            return changed.get();
        }

        @Override public boolean completeResearch(ServerPlayer player, String id) {
            requireResearch(id);
            AtomicBoolean changed = new AtomicBoolean();
            KnowledgeAccess.mutate(player, value -> {
                changed.set(value.completeResearch(id));
                ResearchProgressService.reconcile(value);
            });
            return changed.get();
        }

        @Override public boolean addAspectPoints(ServerPlayer player, String id, int amount) {
            if (AspectRegistryRuntime.find(id).isEmpty()) {
                throw new IllegalArgumentException("unknown aspect: " + id);
            }
            AtomicBoolean discovered = new AtomicBoolean();
            KnowledgeAccess.mutate(player, value -> discovered.set(value.addAspectPoints(id, amount)));
            return discovered.get();
        }

        @Override public int addWarp(ServerPlayer player, WarpType type, int amount) {
            AtomicInteger result = new AtomicInteger();
            KnowledgeAccess.mutate(player, value -> result.set(value.addWarp(warpType(type), amount)));
            return result.get();
        }

        private static void requireResearch(String id) {
            if (ResearchRegistry.find(id).isEmpty()) {
                throw new IllegalArgumentException("unknown research: " + id);
            }
        }
    }

    private static final class ScanApiImpl implements ScanApi {
        @Override
        public Optional<Scan> find(ScanTargetType type, ResourceLocation targetId) {
            return ScanRegistry.find(internal(type), targetId.toString())
                    .map(ThaumicRebornApiServices::scan);
        }

        @Override
        public Optional<Scan> findForItem(ItemStack stack) {
            return ScanRegistry.findForItem(stack).map(ThaumicRebornApiServices::scan);
        }

        @Override
        public List<Scan> all() {
            return ScanRegistry.all().stream().map(ThaumicRebornApiServices::scan).toList();
        }
    }

    private static final class RecipeApiImpl implements RecipeApi {
        @Override public List<com.thaumicreborn.api.crafting.ArcaneRecipe> arcaneRecipes(
                MinecraftServer server
        ) {
            return server.getRecipeManager()
                    .getAllRecipesFor(ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get())
                    .stream()
                    .map(value -> {
                        boolean shaped = value instanceof ArcaneShapedRecipe;
                        int width = shaped ? ((ArcaneShapedRecipe) value).width() : 0;
                        int height = shaped
                                ? value.getIngredients().size() / Math.max(1, width)
                                : 0;
                        return new com.thaumicreborn.api.crafting.ArcaneRecipe(
                                value.getId(),
                                value.researchId(),
                                value.getIngredients(),
                                value.getResultItem(server.registryAccess()),
                                value.visCost().amounts(),
                                shaped,
                                width,
                                height
                        );
                    }).toList();
        }

        @Override public List<CrucibleRecipe> crucibleRecipes() {
            return CrucibleRecipeRegistry.all().stream()
                    .map(ThaumicRebornApiServices::crucible).toList();
        }

        @Override public List<InfusionRecipe> infusionRecipes() {
            return InfusionRecipeRegistry.all().stream()
                    .map(ThaumicRebornApiServices::infusion).toList();
        }

        @Override public Optional<InfusionRecipe> infusionRecipe(ResourceLocation id) {
            return InfusionRecipeRegistry.find(id).map(ThaumicRebornApiServices::infusion);
        }
    }

    private static final class WandApiImpl implements WandApi {
        @Override public List<WandRod> rods() {
            return com.thaumcraftmodern.wand.WandComponentRegistry.catalog().rods().stream()
                    .map(ThaumicRebornApiServices::rod).toList();
        }

        @Override public List<WandCap> caps() {
            return com.thaumcraftmodern.wand.WandComponentRegistry.catalog().caps().stream()
                    .map(ThaumicRebornApiServices::cap).toList();
        }

        @Override public ItemStack createWand(String rod, String cap, boolean filled) {
            return com.thaumcraftmodern.api.wand.WandApi.createWand(rod, cap, filled);
        }

        @Override public ItemStack createSceptre(String rod, String cap, boolean filled) {
            return com.thaumcraftmodern.api.wand.WandApi.createSceptre(rod, cap, filled);
        }

        @Override public ItemStack createStaff(String rod, String cap, boolean filled) {
            return com.thaumcraftmodern.api.wand.WandApi.createStaff(rod, cap, filled);
        }

        @Override public Optional<WandState> state(ItemStack stack) {
            return WandVisService.state(stack).map(value -> {
                Map<String, Integer> vis = new LinkedHashMap<>();
                PrimalAspect.ordered().forEach(aspect ->
                        vis.put(aspect.id(), value.visCentivis(aspect)));
                com.thaumcraftmodern.wand.WandForm form =
                        com.thaumcraftmodern.api.wand.WandApi.form(stack);
                return new WandState(value.rodId(), value.capId(), vis,
                        WandVisService.capacity(stack), WandForm.valueOf(form.name()));
            });
        }

        @Override public boolean isCraftingTool(ItemStack stack) {
            return WandVisService.isCraftingTool(stack);
        }

        @Override public boolean acceptsFocus(ItemStack stack) {
            return com.thaumcraftmodern.api.wand.WandApi.acceptsFocus(stack);
        }
    }

    private static final class EssentiaApiImpl implements EssentiaApi {
        @Override
        public Optional<EssentiaTransport> transport(Level level, BlockPos position) {
            Object entity = level.getBlockEntity(position);
            return entity instanceof EssentiaTransport transport
                    ? Optional.of(transport) : Optional.empty();
        }

        @Override
        public Optional<EssentiaTransport> neighbour(
                Level level, BlockPos position, Direction side) {
            return com.thaumcraftmodern.essentia.EssentiaConnections
                    .neighbour(level, position, side);
        }

        @Override
        public boolean connected(Level level, BlockPos position, Direction side,
                                 EssentiaTransport local) {
            return com.thaumcraftmodern.essentia.EssentiaConnections
                    .connected(level, position, side, local);
        }
    }

    private static final class AuraApiImpl implements AuraApi {
        @Override
        public Optional<AuraNode> node(Level level, BlockPos position) {
            Object entity = level.getBlockEntity(position);
            if (!(entity instanceof AuraNodeBlockEntity node)) return Optional.empty();
            return Optional.of(auraNode(node.snapshotState().snapshot()));
        }

        @Override
        public boolean initialize(ServerLevel level, BlockPos position, AuraNode node) {
            Object entity = level.getBlockEntity(position);
            return entity instanceof AuraNodeBlockEntity blockEntity
                    && blockEntity.initializeOnce(AuraNodeState.withAspects(
                    node.id(),
                    com.thaumcraftmodern.aura.AuraNodeType.valueOf(node.type().name()),
                    com.thaumcraftmodern.aura.AuraNodeModifier.valueOf(node.modifier().name()),
                    node.current(), node.maximum(), node.revision()));
        }

        @Override
        public boolean replaceAspects(ServerLevel level, BlockPos position,
                                      long expectedRevision,
                                      Map<String, Integer> current,
                                      Map<String, Integer> maximum) {
            Object entity = level.getBlockEntity(position);
            return entity instanceof AuraNodeBlockEntity node
                    && node.replaceAspects(expectedRevision, current, maximum);
        }
    }

    private static final class FocusApiImpl implements FocusApi {
        @Override
        public void register(FocusDefinition definition, FocusBehavior behavior) {
            AddonFocusRegistry.register(definition, behavior);
        }

        @Override
        public Optional<FocusDefinition> find(ResourceLocation id) {
            Optional<FocusDefinition> addon = AddonFocusRegistry.find(id)
                    .map(AddonFocusRegistry.Entry::definition);
            if (addon.isPresent()) return addon;
            return java.util.Arrays.stream(WandFocusType.values())
                    .map(ThaumicRebornApiServices::focus)
                    .filter(value -> value.id().equals(id)).findFirst();
        }

        @Override
        public List<FocusDefinition> all() {
            java.util.ArrayList<FocusDefinition> result = new java.util.ArrayList<>();
            java.util.Arrays.stream(WandFocusType.values())
                    .map(ThaumicRebornApiServices::focus).forEach(result::add);
            result.addAll(AddonFocusRegistry.all());
            return List.copyOf(result);
        }

        @Override public Optional<ItemStack> equipped(ItemStack wand) {
            return WandFocusService.focusStack(wand);
        }
        @Override public boolean equip(ItemStack wand, ItemStack focus) {
            return WandFocusService.setFocus(wand, focus);
        }
        @Override public void clear(ItemStack wand) { WandFocusService.clearFocus(wand); }
        @Override public boolean consumeVis(ServerPlayer player, ItemStack wand,
                                            Map<String, Integer> centivisCost) {
            return player.getAbilities().instabuild
                    || WandVisService.consumeCentivis(player, wand, centivisCost);
        }
    }

    private static final class EquipmentApiImpl implements EquipmentApi {
        @Override
        public int visDiscountPercent(ItemStack stack, Player player, String primalAspect) {
            PrimalAspect aspect = PrimalAspect.fromId(primalAspect);
            if (stack.getItem() instanceof com.thaumcraftmodern.api.wand.VisDiscountGear gear) {
                return gear.visDiscountPercent(stack, player, aspect);
            }
            if (stack.getItem() instanceof
                    com.thaumicreborn.api.equipment.VisDiscountGear gear) {
                return gear.visDiscountPercent(stack, player, primalAspect);
            }
            return 0;
        }
        @Override public boolean reveals(ItemStack stack) {
            return com.thaumcraftmodern.item.RevealingGear.equipped(stack);
        }
        @Override public int runicCharge(ItemStack stack) {
            return RunicShieldService.finalCharge(stack);
        }
        @Override public boolean repairable(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof
                    com.thaumicreborn.api.equipment.ThaumicRepairable;
        }
    }

    private static Aspect aspect(AspectDefinition value) {
        ResourceLocation icon = ResourceLocation.tryParse(value.icon());
        if (icon == null) throw new IllegalStateException("invalid aspect icon: " + value.icon());
        return new Aspect(value.id(), value.color(), icon, value.components(), value.order());
    }

    private static AuraNode auraNode(AuraNodeState.Snapshot value) {
        return new AuraNode(value.nodeId(),
                com.thaumicreborn.api.aura.AuraNodeType.valueOf(value.type().name()),
                com.thaumicreborn.api.aura.AuraNodeModifier.valueOf(value.modifier().name()),
                value.aspectsCurrent(), value.aspectsMaximum(), value.revision());
    }

    private static FocusDefinition focus(WandFocusType value) {
        return new FocusDefinition(new ResourceLocation("thaumcraftmodern", value.itemId()),
                value.color(), value.continuous(), value.cooldownTicks(),
                value.centivisCost());
    }

    private static Research research(ResearchDefinition value) {
        return new Research(value.id(), value.categoryId(), value.titleKey(),
                value.subtitleKey(), value.iconItem(), value.parents(), value.concealed(),
                value.inactive(), value.virtual());
    }

    private static Scan scan(ScanDefinition value) {
        ResourceLocation target = ResourceLocation.tryParse(value.targetId());
        if (target == null) throw new IllegalStateException("invalid scan target: " + value.targetId());
        return new Scan(ScanTargetType.valueOf(value.type().name()), target,
                value.displayKey(), value.aspects().stream()
                .map(reward -> new com.thaumicreborn.api.scan.AspectReward(
                        reward.aspectId(), reward.amount())).toList(), value.knowledgeKey());
    }

    private static CrucibleRecipe crucible(CrucibleRecipeDefinition value) {
        return new CrucibleRecipe(value.id(), value.research(), value.catalyst(),
                value.catalystAspect(), value.output(), value.aspects());
    }

    private static InfusionRecipe infusion(InfusionRecipeDefinition value) {
        return new InfusionRecipe(value.id(), value.research(), value.instability(),
                value.central(), value.components(), value.output(), value.essentia());
    }

    private static WandRod rod(WandRodDefinition value) {
        return new WandRod(value.id(), value.capacityVis(), value.translationKey(),
                value.researchId(), value.rechargeAspects(), value.staff(), value.runes());
    }

    private static WandCap cap(WandCapDefinition value) {
        return new WandCap(value.id(), value.costModifier(), value.translationKey(),
                value.researchId(), value.specialAspects(), value.specialCostModifier());
    }

    private static com.thaumcraftmodern.scan.ScanTargetType internal(ScanTargetType value) {
        return com.thaumcraftmodern.scan.ScanTargetType.valueOf(value.name());
    }

    private static com.thaumcraftmodern.knowledge.WarpType warpType(WarpType value) {
        return com.thaumcraftmodern.knowledge.WarpType.valueOf(value.name());
    }
}
