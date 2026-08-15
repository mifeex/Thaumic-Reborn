package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.render.ClassicWandRenderCalibration;
import com.thaumcraftmodern.client.render.ArcaneWorkbenchBlockEntityRenderer;
import com.thaumcraftmodern.client.render.RunicMatrixBlockEntityRenderer;
import com.thaumcraftmodern.client.render.InfusionPillarBlockEntityRenderer;
import com.thaumcraftmodern.client.render.RunicMatrixCubeModel;
import com.thaumcraftmodern.client.render.ArcaneBellowsBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ArcaneBellowsModel;
import com.thaumcraftmodern.client.render.FluxScrubberBlockEntityRenderer;
import com.thaumcraftmodern.client.render.BrainJarBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ClassicBrainJarModel;
import com.thaumcraftmodern.client.render.HungryChestBlockEntityRenderer;
import com.thaumcraftmodern.client.render.HungryChestModel;
import com.thaumcraftmodern.client.render.StrawGolemModel;
import com.thaumcraftmodern.client.render.StrawGolemRenderer;
import com.thaumcraftmodern.client.render.GolemFishingBobberRenderer;
import com.thaumcraftmodern.client.render.TravelingTrunkModel;
import com.thaumcraftmodern.client.render.TravelingTrunkRenderer;
import com.thaumcraftmodern.client.render.AdvancedGolemLayer;
import com.thaumcraftmodern.client.render.ArcanePedestalBlockEntityRenderer;
import com.thaumcraftmodern.client.render.WandRechargePedestalBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ClientNodeRenderers;
import com.thaumcraftmodern.client.render.ResearchTableBlockEntityRenderer;
import com.thaumcraftmodern.client.render.CrucibleBlockEntityRenderer;
import com.thaumcraftmodern.client.render.CrystalClusterModel;
import com.thaumcraftmodern.client.render.CrystalClusterRenderer;
import com.thaumcraftmodern.client.render.DeconstructionTableBlockEntityRenderer;
import com.thaumcraftmodern.client.render.DeconstructionTableModel;
import com.thaumcraftmodern.client.render.ResearchTableModel;
import com.thaumcraftmodern.client.render.ReloadSafeObjLoader;
import com.thaumcraftmodern.client.render.EldritchAltarPartRenderer;
import com.thaumcraftmodern.client.render.OuterLandsPortalRenderer;
import com.thaumcraftmodern.client.render.EldritchLockRenderer;
import com.thaumcraftmodern.client.render.EtherealBloomBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaJarBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaBufferBlockEntityRenderer;
import com.thaumcraftmodern.client.render.AdvancedEssentiaBufferBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaTubeBlockEntityRenderer;
import com.thaumcraftmodern.client.render.VoidJarBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaCentrifugeBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ClassicCentrifugeModel;
import com.thaumcraftmodern.client.render.EssentiaCrystallizerBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EssentiaReservoirBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ThaumatoriumBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ClassicManaPodModel;
import com.thaumcraftmodern.client.render.ThaumaturgeRobeArmorModel;
import com.thaumcraftmodern.client.render.VoidRobeArmorModel;
import com.thaumcraftmodern.client.render.VoidArmorChestModel;
import com.thaumcraftmodern.client.render.FortressArmorModel;
import com.thaumcraftmodern.client.render.MagicMirrorBlockEntityRenderer;
import com.thaumcraftmodern.client.render.ManaPodBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EnergizedAuraNodeBlockEntityRenderer;
import com.thaumcraftmodern.client.render.NodeDeviceBlockEntityRenderer;
import com.thaumcraftmodern.client.render.VisRelayBlockEntityRenderer;
import com.thaumcraftmodern.item.EssentiaCrystalItem;
import com.thaumcraftmodern.client.render.ArcaneAlembicBlockEntityRenderer;
import com.thaumcraftmodern.client.render.AdvancedAlchemicalFurnaceBlockEntityRenderer;
import com.thaumcraftmodern.client.render.EtherealBloomCrystalModel;
import com.thaumcraftmodern.client.screen.ArcaneWorkbenchScreen;
import com.thaumcraftmodern.client.screen.AlchemicalFurnaceScreen;
import com.thaumcraftmodern.client.screen.DeconstructionTableScreen;
import com.thaumcraftmodern.client.screen.ResearchTableScreen;
import com.thaumcraftmodern.client.screen.PechScreen;
import com.thaumcraftmodern.client.screen.ThaumatoriumScreen;
import com.thaumcraftmodern.client.screen.HandMirrorScreen;
import com.thaumcraftmodern.client.screen.GolemScreen;
import com.thaumcraftmodern.client.screen.TravelingTrunkScreen;
import com.thaumcraftmodern.item.AspectShardItem;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.EssentiaPhialItem;
import com.thaumcraftmodern.item.ManaBeanItem;
import com.thaumcraftmodern.item.ThaumaturgeRobeItem;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.client.particle.NodeBurstParticle;
import com.thaumcraftmodern.client.particle.NitorWispParticle;
import com.thaumcraftmodern.client.particle.EldritchHealParticle;
import com.thaumcraftmodern.client.particle.TravelSparkleParticle;
import com.thaumcraftmodern.client.particle.WardingRuneParticle;
import com.thaumcraftmodern.client.particle.CrucibleBubbleParticle;
import com.thaumcraftmodern.client.particle.TubeVentParticle;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import com.thaumcraftmodern.aspect.AspectDefinition;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.crystal.CrystalClusterVariant;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.ModelEvent;
import com.thaumcraftmodern.client.render.InfernalFurnaceBakedModel;
import com.thaumcraftmodern.client.render.LegacyObjMesh;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerDimensionEffects(
            RegisterDimensionSpecialEffectsEvent event
    ) {
        event.register(
                new ResourceLocation(
                        ThaumcraftModern.MOD_ID,
                        "outer_lands"
                ),
                new OuterLandsDimensionEffects()
        );
    }

    @SubscribeEvent
    public static void registerScreens(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        if (!com.thaumicreborn.api.client.ThaumicRebornClientApi.isAvailable()) {
            com.thaumicreborn.api.client.ThaumicRebornClientApi.install(
                    new com.thaumcraftmodern.integration.api.ThaumicRebornClientApiServices());
        }
        event.enqueueWork(() -> {
            ClientSinisterNodeTracker.installLifecycleListener();
            MenuScreens.register(ModMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
            MenuScreens.register(ModMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
            MenuScreens.register(
                    ModMenus.DECONSTRUCTION_TABLE.get(),
                    DeconstructionTableScreen::new
            );
            MenuScreens.register(ModMenus.PECH.get(), PechScreen::new);
            MenuScreens.register(
                    ModMenus.ALCHEMICAL_FURNACE.get(),
                    AlchemicalFurnaceScreen::new
            );
            MenuScreens.register(ModMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
            MenuScreens.register(ModMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
            MenuScreens.register(ModMenus.FOCAL_MANIPULATOR.get(),
                    com.thaumcraftmodern.client.screen.FocalManipulatorScreen::new);
            MenuScreens.register(ModMenus.GOLEM.get(), GolemScreen::new);
            MenuScreens.register(ModMenus.TRAVELING_TRUNK.get(), TravelingTrunkScreen::new);
            ItemProperties.register(
                    ModItems.ESSENTIA_PHIAL.get(),
                    new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            "filled"
                    ),
                    (stack, level, entity, seed) ->
                            EssentiaPhialItem.aspect(stack).isPresent()
                                    ? 1.0F : 0.0F
            );
            ItemProperties.register(ModItems.SINISTER_LODESTONE.get(),
                    new ResourceLocation(ThaumcraftModern.MOD_ID, "active"),
                    (stack, level, entity, seed) -> level != null && entity != null
                            && ClientSinisterNodeTracker.pointsAt(level,entity) ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(
            ModelEvent.RegisterGeometryLoaders event
    ) {
        event.register("reload_safe_obj", ReloadSafeObjLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(EssentiaCrystallizerBlockEntityRenderer.CRYSTAL_MODEL);
    }

    @SubscribeEvent
    public static void modifyBakedModels(ModelEvent.ModifyBakingResult event) {
        InfernalFurnaceBakedModel.wrapModels(event);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> stack.getItem()
                        instanceof com.thaumcraftmodern.item.AspectRingItem ring
                        ? 0xFF000000 | ring.color()
                        : 0xFFFFFFFF,
                ModItems.ASPECT_RINGS.values().stream()
                        .map(net.minecraftforge.registries.RegistryObject::get)
                        .toArray(net.minecraft.world.item.Item[]::new)
        );
        event.register(
                (stack, tintIndex) -> stack.getItem() instanceof AspectShardItem shard
                        ? 0xFF000000 | shard.color()
                        : 0xFFFFFFFF,
                ModItems.AIR_SHARD.get(),
                ModItems.FIRE_SHARD.get(),
                ModItems.WATER_SHARD.get(),
                ModItems.EARTH_SHARD.get(),
                ModItems.ORDER_SHARD.get(),
                ModItems.ENTROPY_SHARD.get()
        );
        event.register(
                (stack, tintIndex) -> 0xFF000000
                        | EtherealEssenceItem.color(stack),
                ModItems.ETHEREAL_ESSENCE.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? 0xFF000000 | EssentiaPhialItem.color(stack)
                        : 0xFFFFFFFF,
                ModItems.ESSENTIA_PHIAL.get()
        );
        event.register(
                (stack, tintIndex) -> 0xFF000000
                        | EssentiaCrystalItem.color(stack),
                ModItems.ESSENTIA_CRYSTAL.get()
        );
        event.register(
                (stack, tintIndex) -> 0xFF000000
                        | ManaBeanItem.color(stack),
                ModItems.MANA_BEAN.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 0
                        && stack.getItem() instanceof ThaumaturgeRobeItem robe
                        ? 0xFF000000 | robe.getColor(stack)
                        : 0xFFFFFFFF,
                ModItems.THAUMATURGE_ROBE.get(),
                ModItems.THAUMATURGE_LEGGINGS.get(),
                ModItems.THAUMATURGE_BOOTS.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? 0xFF000000
                                | com.thaumcraftmodern.item.ResearchNotesItem
                                        .color(stack)
                        : 0xFFFFFFFF,
                ModItems.RESEARCH_NOTES.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 1
                        ? 0xFF000000
                                | com.thaumcraftmodern.item.DiscoveryItem
                                        .color(stack)
                        : 0xFFFFFFFF,
                ModItems.DISCOVERY.get()
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(
            RegisterColorHandlersEvent.Block event
    ) {
        event.register(
                (state, level, position, tintIndex) -> {
                    if (!(state.getBlock()
                            instanceof CrystalClusterBlock cluster)) {
                        return 0xFFFFFF;
                    }
                    CrystalClusterVariant variant = cluster.variant();
                    int crystalIndex = variant
                            == CrystalClusterVariant.BALANCED
                            ? ThreadLocalRandom.current().nextInt(1, 7)
                            : 0;
                    return variant.crystalColor(crystalIndex);
                },
                ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
                ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
                ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
                ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
                ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
                ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(),
                ModBlocks.BALANCED_CRYSTAL_CLUSTER.get()
        );
        event.register(
                (state, level, position, tintIndex) ->
                        level != null && position != null
                                ? BiomeColors.getAverageWaterColor(
                                        level,
                                        position
                                )
                                : 0x3F76E4,
                ModBlocks.CRUCIBLE.get()
        );
        event.register(
                (state, level, position, tintIndex) -> {
                    if (tintIndex != 0 || level == null || position == null
                            || !(level.getBlockEntity(position)
                            instanceof EssentiaTubeBlockEntity tube)
                            || tube.filter() == null) {
                        return 0xFFFFFF;
                    }
                    return AspectRegistryRuntime.find(tube.filter())
                            .map(AspectDefinition::color).orElse(0xFFFFFF);
                },
                ModBlocks.FILTERED_ESSENTIA_TUBE.get()
        );
        event.register(
                (state, level, position, tintIndex) -> {
                    if (tintIndex < 0 || tintIndex >= net.minecraft.core.Direction.values().length
                            || level == null || position == null
                            || !(level.getBlockEntity(position)
                            instanceof AdvancedEssentiaBufferBlockEntity buffer)) {
                        return 0xFFFFFF;
                    }
                    return buffer.role(net.minecraft.core.Direction.values()[tintIndex])
                            .indicatorColor();
                },
                ModBlocks.ADVANCED_ESSENTIA_BUFFER.get()
        );
    }

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerEntityRenderer(ModEntities.FROST_SHARD.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.PRIMAL_ORB.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FOCUS_EMBER.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.GOLEM_FISHING_BOBBER.get(), GolemFishingBobberRenderer::new);
        event.registerEntityRenderer(ModEntities.STRAW_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.WOOD_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.TALLOW_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.CLAY_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.FLESH_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.STONE_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.IRON_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.THAUMIUM_GOLEM.get(), StrawGolemRenderer::new);
        event.registerEntityRenderer(ModEntities.TRAVELING_TRUNK.get(), TravelingTrunkRenderer::new);
        ClientNodeRenderers.register(
                event,
                ModBlockEntities.AURA_NODE.get(),
                ModBlockEntities.JARRED_AURA_NODE.get(),
                ModItems.THAUMOMETER.get(),
                ModItems.GOGGLES_OF_REVEALING.get()
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ENERGIZED_AURA_NODE.get(),
                EnergizedAuraNodeBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.NODE_STABILIZER.get(),
                NodeDeviceBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.NODE_TRANSDUCER.get(),
                NodeDeviceBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.VIS_RELAY.get(),
                VisRelayBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.VIS_CHARGE_RELAY.get(),
                VisRelayBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ARCANE_WORKBENCH.get(),
                ArcaneWorkbenchBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ARCANE_PEDESTAL.get(),
                ArcanePedestalBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.WAND_RECHARGE_PEDESTAL.get(),
                WandRechargePedestalBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.RUNIC_MATRIX.get(),
                RunicMatrixBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.INFUSION_PILLAR.get(),
                InfusionPillarBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ARCANE_BELLOWS.get(),
                ArcaneBellowsBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(ModBlockEntities.FLUX_SCRUBBER.get(),
                FluxScrubberBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BRAIN_JAR.get(),BrainJarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HUNGRY_CHEST.get(), HungryChestBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.RESEARCH_TABLE.get(),
                ResearchTableBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.DECONSTRUCTION_TABLE.get(),
                DeconstructionTableBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(ModBlockEntities.WARDED_BLOCK.get(),
                com.thaumcraftmodern.client.render.WardedBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(
                ModBlockEntities.CRYSTAL_CLUSTER.get(),
                CrystalClusterRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.CRUCIBLE.get(),
                CrucibleBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_JAR.get(),
                EssentiaJarBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_BUFFER.get(),
                EssentiaBufferBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ADVANCED_ESSENTIA_BUFFER.get(),
                AdvancedEssentiaBufferBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.VOID_JAR.get(),
                VoidJarBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_CENTRIFUGE.get(),
                EssentiaCentrifugeBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_CRYSTALLIZER.get(),
                EssentiaCrystallizerBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_RESERVOIR.get(),
                EssentiaReservoirBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.THAUMATORIUM.get(),
                ThaumatoriumBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_TUBE.get(),
                EssentiaTubeBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ARCANE_ALEMBIC.get(),
                ArcaneAlembicBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(),
                AdvancedAlchemicalFurnaceBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ELDRITCH_ALTAR_PART.get(),
                EldritchAltarPartRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.OUTER_LANDS_PORTAL.get(),
                OuterLandsPortalRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ELDRITCH_LOCK.get(),
                EldritchLockRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ETHEREAL_BLOOM.get(),
                EtherealBloomBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.MANA_POD.get(),
                ManaPodBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.MAGIC_MIRROR.get(),
                MagicMirrorBlockEntityRenderer::new
        );
        event.registerBlockEntityRenderer(
                ModBlockEntities.ESSENTIA_MIRROR.get(),
                MagicMirrorBlockEntityRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {
        event.registerLayerDefinition(
                RunicMatrixCubeModel.LAYER,
                RunicMatrixCubeModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ArcaneBellowsModel.LAYER,
                ArcaneBellowsModel::createBodyLayer
        );
        event.registerLayerDefinition(ClassicBrainJarModel.LAYER,ClassicBrainJarModel::createBodyLayer);
        event.registerLayerDefinition(HungryChestModel.LAYER,HungryChestModel::createBodyLayer);
        event.registerLayerDefinition(StrawGolemModel.LAYER,StrawGolemModel::createBodyLayer);
        event.registerLayerDefinition(TravelingTrunkModel.LAYER, TravelingTrunkModel::createBodyLayer);
        event.registerLayerDefinition(AdvancedGolemLayer.LAYER, AdvancedGolemLayer::createBodyLayer);
        event.registerLayerDefinition(
                ClassicManaPodModel.LAYER,
                ClassicManaPodModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ResearchTableBlockEntityRenderer.LAYER,
                ResearchTableModel::createBodyLayer
        );
        event.registerLayerDefinition(
                DeconstructionTableBlockEntityRenderer.LAYER,
                DeconstructionTableModel::createBodyLayer
        );
        event.registerLayerDefinition(
                CrystalClusterModel.LAYER,
                CrystalClusterModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ClassicCentrifugeModel.LAYER,
                ClassicCentrifugeModel::createBodyLayer
        );
        event.registerLayerDefinition(
                EtherealBloomCrystalModel.LAYER,
                EtherealBloomCrystalModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ThaumaturgeRobeArmorModel.OUTER_LAYER,
                ThaumaturgeRobeArmorModel::createOuterLayer
        );
        event.registerLayerDefinition(
                ThaumaturgeRobeArmorModel.BOOTS_LAYER,
                ThaumaturgeRobeArmorModel::createBootsLayer
        );
        event.registerLayerDefinition(
                VoidRobeArmorModel.OUTER_LAYER,
                VoidRobeArmorModel::createOuterLayer
        );
        event.registerLayerDefinition(
                VoidRobeArmorModel.INNER_LAYER,
                VoidRobeArmorModel::createInnerLayer
        );
        event.registerLayerDefinition(
                VoidArmorChestModel.LAYER,
                VoidArmorChestModel::createLayer
        );
        event.registerLayerDefinition(
                FortressArmorModel.LAYER,
                FortressArmorModel::createLayer
        );
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(
                "wand_vis",
                ClientWandVisOverlay::render
        );
        event.registerAboveAll("thaumometer_view", ClientThaumometerOverlay::render);
        event.registerAboveAll(
                "goggles_node_aspects",
                ClientGogglesNodeOverlay::render
        );
        event.registerAboveAll(
                "scan_notifications",
                ClientScanOverlay::renderNotification
        );
        event.registerAboveAll(
                "research_table_notifications",
                ClientResearchTableOverlay::render
        );
        event.registerAboveAll("warp", ClientWarpOverlay::render);
        event.registerAboveAll("runic_shield", ClientRunicShieldOverlay::render);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ModParticles.NITOR_WISP_LARGE.get(),
                sprites -> new NitorWispParticle.Provider(sprites, true)
        );
        event.registerSpriteSet(
                ModParticles.NITOR_WISP_SMALL.get(),
                sprites -> new NitorWispParticle.Provider(sprites, false)
        );
        event.registerSpriteSet(
                ModParticles.NODE_BURST.get(),
                NodeBurstParticle.Provider::new
        );
        event.registerSpriteSet(
                ModParticles.ELDRITCH_HEAL.get(),
                EldritchHealParticle.Provider::new
        );
        event.registerSpriteSet(
                ModParticles.TRAVEL_SPARKLE.get(),
                TravelSparkleParticle.Provider::new
        );
        event.registerSpriteSet(
                ModParticles.WARDING_RUNE_ACTIVE.get(),
                sprites -> new WardingRuneParticle.Provider(
                        sprites,
                        WardingRuneParticle.State.ACTIVE
                )
        );
        event.registerSpriteSet(
                ModParticles.WARDING_RUNE_DISABLED.get(),
                sprites -> new WardingRuneParticle.Provider(
                        sprites,
                        WardingRuneParticle.State.DISABLED
                )
        );
        event.registerSpriteSet(
                ModParticles.WARDING_RUNE_BLOCKED.get(),
                sprites -> new WardingRuneParticle.Provider(
                        sprites,
                        WardingRuneParticle.State.BLOCKED
                )
        );
        event.registerSpriteSet(
                ModParticles.CRUCIBLE_BUBBLE.get(),
                sprites -> new CrucibleBubbleParticle.Provider(
                        sprites,
                        false
                )
        );
        event.registerSpriteSet(
                ModParticles.CRUCIBLE_FROTH.get(),
                sprites -> new CrucibleBubbleParticle.Provider(
                        sprites,
                        true
                )
        );
        event.registerSpriteSet(
                ModParticles.TUBE_VENT.get(),
                TubeVentParticle.Provider::new
        );
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(
            RegisterClientReloadListenersEvent event
    ) {
        ThaumometerHudLayout.registerReloadListener(event);
        ClassicWandRenderCalibration.registerReloadListener(event);
        LegacyObjMesh.registerReloadListener(event);
    }
}
