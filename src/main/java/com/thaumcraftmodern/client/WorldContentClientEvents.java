package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.client.render.LegacyFlyingMobRenderer;
import com.thaumcraftmodern.client.render.LegacyMobRenderer;
import com.thaumcraftmodern.client.render.BrainyZombieRenderer;
import com.thaumcraftmodern.client.render.InhabitedZombieRenderer;
import com.thaumcraftmodern.client.render.TaintacleModel;
import com.thaumcraftmodern.client.render.TaintacleRenderer;
import com.thaumcraftmodern.client.render.ThaumicSlimeRenderer;
import com.thaumcraftmodern.client.render.CrimsonCultArmorModel;
import com.thaumcraftmodern.client.render.CrimsonCultistModel;
import com.thaumcraftmodern.client.render.EldritchGuardianModel;
import com.thaumcraftmodern.client.render.EldritchGuardianRenderer;
import com.thaumcraftmodern.client.render.EldritchWardenRenderer;
import com.thaumcraftmodern.client.render.EldritchOrbRenderer;
import com.thaumcraftmodern.client.render.EldritchConstructModel;
import com.thaumcraftmodern.client.render.EldritchConstructRenderer;
import com.thaumcraftmodern.client.render.EldritchCrabModel;
import com.thaumcraftmodern.client.render.EldritchCrabRenderer;
import com.thaumcraftmodern.client.render.FireBatModel;
import com.thaumcraftmodern.client.render.FireBatRenderer;
import com.thaumcraftmodern.client.render.MindSpiderRenderer;
import com.thaumcraftmodern.client.render.PechModel;
import com.thaumcraftmodern.client.render.PechRenderer;
import com.thaumcraftmodern.client.render.TaintSporeModel;
import com.thaumcraftmodern.client.render.TaintSporeRenderer;
import com.thaumcraftmodern.client.render.TaintSporeSwarmerModel;
import com.thaumcraftmodern.client.render.TaintSporeSwarmerRenderer;
import com.thaumcraftmodern.client.render.TaintedCrawlerRenderer;
import com.thaumcraftmodern.client.render.TaintedChickenRenderer;
import com.thaumcraftmodern.client.render.TaintedCowRenderer;
import com.thaumcraftmodern.client.render.TaintedCreeperRenderer;
import com.thaumcraftmodern.client.render.TaintedPigRenderer;
import com.thaumcraftmodern.client.render.TaintedSheepModel;
import com.thaumcraftmodern.client.render.TaintedSheepRenderer;
import com.thaumcraftmodern.client.render.TaintedVillagerRenderer;
import com.thaumcraftmodern.client.render.TaintedVillagerModel;
import com.thaumcraftmodern.client.render.TaintSwarmRenderer;
import com.thaumcraftmodern.client.render.WispRenderer;
import com.thaumcraftmodern.client.render.TemporaryHoleBlockEntityRenderer;
import com.thaumcraftmodern.client.render.FacelessWitnessModel;
import com.thaumcraftmodern.client.render.FacelessWitnessRenderer;
import com.thaumcraftmodern.client.render.WingedMantleArmorModel;
import com.thaumcraftmodern.client.render.WingedMantleElytraLayer;
import com.thaumcraftmodern.client.render.ConvertedVillagerModel;
import com.thaumcraftmodern.client.render.ConvertedVillagerRenderer;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModBlockEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public final class WorldContentClientEvents {
    static final int CLASSIC_TAINT_FIBRES_ITEM_COLOR = 0x6D4189;
    static final int CLASSIC_TAINT_FOLIAGE_COLOR = 0x7C6D87;

    private WorldContentClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.TEMPORARY_HOLE.get(),
                TemporaryHoleBlockEntityRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.ELDRITCH_ORB.get(),
                EldritchOrbRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.ALUMENTUM.get(),
                ThrownItemRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.BOTTLED_TAINT.get(),
                ThrownItemRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.PECH_BLAST.get(),
                NoopRenderer::new
        );
        event.registerEntityRenderer(
                ModEntities.FACELESS_WITNESS.get(),
                FacelessWitnessRenderer::new
        );
        for (var entry : ModEntities.entries()) {
            if (entry.getKey() == LegacyMobKind.ELDRITCH_GUARDIAN) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        EldritchGuardianRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.ELDRITCH_WARDEN) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        EldritchWardenRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.THAUMIC_SLIME) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        ThaumicSlimeRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.ANGRY_ZOMBIE
                    || entry.getKey() == LegacyMobKind.FURIOUS_ZOMBIE) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        BrainyZombieRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.INHABITED_ZOMBIE) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        InhabitedZombieRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.WISP) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        WispRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.FIREBAT) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        FireBatRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.MIND_SPIDER) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        MindSpiderRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.PECH) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        PechRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.CONVERTED_VILLAGER) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        ConvertedVillagerRenderer::new
                );
            } else if (entry.getKey()
                    == LegacyMobKind.ELDRITCH_CONSTRUCT) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        EldritchConstructRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.ELDRITCH_CRAB) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        EldritchCrabRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINT_SWARM) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintSwarmRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_CRAWLER) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedCrawlerRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_CHICKEN) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedChickenRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_COW) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedCowRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_CREEPER) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedCreeperRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_PIG) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedPigRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_SHEEP) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedSheepRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINTED_VILLAGER) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintedVillagerRenderer::new
                );
            } else if (entry.getKey() == LegacyMobKind.TAINT_SPORE) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintSporeRenderer::new
                );
            } else if (entry.getKey()
                    == LegacyMobKind.TAINT_SPORE_SWARMER) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        TaintSporeSwarmerRenderer::new
                );
            } else if (entry.getKey().taintacle()) {
                LegacyMobKind kind = entry.getKey();
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        context -> new TaintacleRenderer(context, kind)
                );
            } else if (entry.getKey().flying()) {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        LegacyFlyingMobRenderer::new
                );
            } else {
                event.registerEntityRenderer(
                        entry.getValue().get(),
                        LegacyMobRenderer::new
                );
            }
        }
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new WingedMantleElytraLayer(
                        renderer, event.getEntityModels()));
            }
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {
        event.registerLayerDefinition(
                TaintedVillagerModel.LAYER,
                TaintedVillagerModel::createBodyLayer
        );
        event.registerLayerDefinition(
                TaintedSheepModel.BASE_LAYER,
                TaintedSheepModel::createBaseLayer
        );
        event.registerLayerDefinition(
                TaintedSheepModel.FUR_LAYER,
                TaintedSheepModel::createFurLayer
        );
        event.registerLayerDefinition(
                FacelessWitnessModel.LAYER,
                FacelessWitnessModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ConvertedVillagerModel.LAYER,
                ConvertedVillagerModel::createBodyLayer
        );
        event.registerLayerDefinition(
                EldritchGuardianModel.LAYER,
                EldritchGuardianModel::createBodyLayer
        );
        event.registerLayerDefinition(
                FireBatModel.LAYER,
                FireBatModel::createBodyLayer
        );
        event.registerLayerDefinition(
                PechModel.LAYER,
                PechModel::createBodyLayer
        );
        event.registerLayerDefinition(
                EldritchConstructModel.LAYER,
                EldritchConstructModel::createBodyLayer
        );
        event.registerLayerDefinition(
                EldritchCrabModel.LAYER,
                EldritchCrabModel::createBodyLayer
        );
        event.registerLayerDefinition(
                TaintSporeModel.LAYER,
                TaintSporeModel::createBodyLayer
        );
        event.registerLayerDefinition(
                TaintSporeSwarmerModel.LAYER,
                TaintSporeSwarmerModel::createBodyLayer
        );
        event.registerLayerDefinition(
                TaintacleModel.NORMAL_LAYER,
                TaintacleModel::createNormalLayer
        );
        event.registerLayerDefinition(
                TaintacleModel.TENDRIL_LAYER,
                TaintacleModel::createTendrilLayer
        );
        event.registerLayerDefinition(
                TaintacleModel.GIANT_LAYER,
                TaintacleModel::createGiantLayer
        );
        event.registerLayerDefinition(
                CrimsonCultistModel.LAYER,
                CrimsonCultistModel::createBodyLayer
        );
        event.registerLayerDefinition(
                CrimsonCultArmorModel.KNIGHT_LAYER,
                CrimsonCultArmorModel::createKnightLayer
        );
        event.registerLayerDefinition(
                CrimsonCultArmorModel.CLERIC_LAYER,
                CrimsonCultArmorModel::createClericLayer
        );
        event.registerLayerDefinition(
                CrimsonCultArmorModel.PRAETOR_LAYER,
                CrimsonCultArmorModel::createPraetorLayer
        );
        event.registerLayerDefinition(
                CrimsonCultArmorModel.BOOTS_LAYER,
                CrimsonCultArmorModel::createBootsLayer
        );
        event.registerLayerDefinition(
                WingedMantleArmorModel.LAYER,
                WingedMantleArmorModel::createBodyLayer
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0xFFFF7E : 0xFFFFFF,
                ModBlocks.AIR_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0xFF3C01 : 0xFFFFFF,
                ModBlocks.FIRE_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0x0090FF : 0xFFFFFF,
                ModBlocks.WATER_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0x00A000 : 0xFFFFFF,
                ModBlocks.EARTH_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0xEECCFF : 0xFFFFFF,
                ModBlocks.ORDER_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0x555577 : 0xFFFFFF,
                ModBlocks.ENTROPY_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0xFFFF7E : 0xFFFFFF,
                ModBlocks.DEEPSLATE_AIR_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0xFF3C01 : 0xFFFFFF,
                ModBlocks.DEEPSLATE_FIRE_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0x0090FF : 0xFFFFFF,
                ModBlocks.DEEPSLATE_WATER_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0x00A000 : 0xFFFFFF,
                ModBlocks.DEEPSLATE_EARTH_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0xEECCFF : 0xFFFFFF,
                ModBlocks.DEEPSLATE_ORDER_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0 ? 0x555577 : 0xFFFFFF,
                ModBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE.get());
        event.register((state, level, position, tintIndex) -> {
            if (tintIndex != 0) {
                return 0xFFFFFF;
            }
            return level != null && position != null
                    ? BiomeColors.getAverageFoliageColor(level, position)
                    : FoliageColor.getDefaultColor();
        }, ModBlocks.GREATWOOD_LEAVES.get());
        event.register((state, level, position, tintIndex) ->
                        tintIndex == 0
                                ? CLASSIC_TAINT_FOLIAGE_COLOR
                                : 0xFFFFFF,
                ModBlocks.TAINTED_LEAVES.get());
        event.register((state, level, position, tintIndex) -> {
            if (tintIndex != 0) {
                return 0xFFFFFF;
            }
            return level != null && position != null
                    ? BiomeColors.getAverageGrassColor(level, position)
                    : CLASSIC_TAINT_FIBRES_ITEM_COLOR;
        },
                ModBlocks.TAINT_FIBRES.get(),
                ModBlocks.TAINTED_SOIL.get(),
                ModBlocks.SHORT_TAINTED_GRASS.get(),
                ModBlocks.TALL_TAINTED_GRASS.get(),
                ModBlocks.SPORE_STALK.get(),
                ModBlocks.MATURE_SPORE_STALK.get());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xFFFF7E : 0xFFFFFF,
                ModBlocks.AIR_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xFF3C01 : 0xFFFFFF,
                ModBlocks.FIRE_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0x0090FF : 0xFFFFFF,
                ModBlocks.WATER_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0x00A000 : 0xFFFFFF,
                ModBlocks.EARTH_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xEECCFF : 0xFFFFFF,
                ModBlocks.ORDER_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0x555577 : 0xFFFFFF,
                ModBlocks.ENTROPY_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xFFFF7E : 0xFFFFFF,
                ModBlocks.DEEPSLATE_AIR_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xFF3C01 : 0xFFFFFF,
                ModBlocks.DEEPSLATE_FIRE_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0x0090FF : 0xFFFFFF,
                ModBlocks.DEEPSLATE_WATER_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0x00A000 : 0xFFFFFF,
                ModBlocks.DEEPSLATE_EARTH_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0xEECCFF : 0xFFFFFF,
                ModBlocks.DEEPSLATE_ORDER_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0 ? 0x555577 : 0xFFFFFF,
                ModBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0
                                ? FoliageColor.getDefaultColor()
                                : 0xFFFFFF,
                ModBlocks.GREATWOOD_LEAVES.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0
                                ? CLASSIC_TAINT_FOLIAGE_COLOR
                                : 0xFFFFFF,
                ModBlocks.TAINTED_LEAVES.get());
        event.register((stack, tintIndex) ->
                        tintIndex == 0
                                ? CLASSIC_TAINT_FIBRES_ITEM_COLOR
                                : 0xFFFFFF,
                ModBlocks.TAINT_FIBRES.get(),
                ModBlocks.TAINTED_SOIL.get(),
                ModBlocks.SHORT_TAINTED_GRASS.get(),
                ModBlocks.TALL_TAINTED_GRASS.get(),
                ModBlocks.SPORE_STALK.get(),
                ModBlocks.MATURE_SPORE_STALK.get());
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public static void clientSetup(
            net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event
    ) {
        event.enqueueWork(() -> {
            RenderType cutout = RenderType.cutout();
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.GREATWOOD_LEAVES.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.SILVERWOOD_LEAVES.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.TAINTED_LEAVES.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.GREATWOOD_SAPLING.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.SILVERWOOD_SAPLING.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SHIMMERLEAF.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CINDERPEARL.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ETHEREAL_BLOOM.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VISHROOM.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.TAINT_FIBRES.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.TAINTED_CAVE_VINE_TEST.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.TAINTED_GLOW_BERRY_VINE_TEST.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.SHORT_TAINTED_GRASS.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.TALL_TAINTED_GRASS.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.SPORE_STALK.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.MATURE_SPORE_STALK.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MANA_POD.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.INFERNAL_FURNACE.get(),
                    cutout
            );
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ARCANE_LAMP.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GROWTH_LAMP.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FERTILITY_LAMP.get(), cutout);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ITEM_GRATE.get(), cutout);
            RenderType translucent = RenderType.translucent();
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ESSENTIA_RESERVOIR.get(),
                    translucent
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.FLUX_GOO.get(),
                    translucent
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.FLUX_GAS.get(),
                    translucent
            );
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.PURIFYING_FLUID.get(), translucent);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIQUID_DEATH.get(), translucent);
        });
    }
}
