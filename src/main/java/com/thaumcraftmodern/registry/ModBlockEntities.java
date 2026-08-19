package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneBellowsBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneLevitatorBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneLampBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneSpaBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneBoreBaseBlockEntity;
import com.thaumcraftmodern.world.block.entity.GrowthLampBlockEntity;
import com.thaumcraftmodern.world.block.entity.FertilityLampBlockEntity;
import com.thaumcraftmodern.world.block.entity.ItemGrateBlockEntity;
import com.thaumcraftmodern.world.block.entity.BrainJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.HungryChestBlockEntity;
import com.thaumcraftmodern.world.block.entity.FluxScrubberBlockEntity;
import com.thaumcraftmodern.world.block.entity.FocalManipulatorBlockEntity;
import com.thaumcraftmodern.world.block.entity.TemporaryHoleBlockEntity;
import com.thaumcraftmodern.world.block.entity.WardedBlockEntity;
import com.thaumcraftmodern.world.block.entity.WardedGlassBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneDoorBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcanePressurePlateBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcanePedestalBlockEntity;
import com.thaumcraftmodern.world.block.entity.WandRechargePedestalBlockEntity;
import com.thaumcraftmodern.world.block.entity.RunicMatrixBlockEntity;
import com.thaumcraftmodern.world.block.entity.InfusionPillarBlockEntity;
import com.thaumcraftmodern.world.block.entity.InfernalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneEarBlockEntity;
import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.AdvancedAlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import com.thaumcraftmodern.world.block.entity.CrystalClusterBlockEntity;
import com.thaumcraftmodern.world.block.entity.DeconstructionTableBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.AdvancedEssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaCentrifugeBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaCrystallizerBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaReservoirBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchLockBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchCrabVentBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchRunedStoneBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchNothingBlockEntity;
import com.thaumcraftmodern.world.block.entity.OuterLandsPortalBlockEntity;
import com.thaumcraftmodern.world.block.entity.EtherealBloomBlockEntity;
import com.thaumcraftmodern.world.block.entity.ManaPodBlockEntity;
import com.thaumcraftmodern.world.block.entity.MnemonicMatrixBlockEntity;
import com.thaumcraftmodern.world.block.entity.NitorBlockEntity;
import com.thaumcraftmodern.world.block.entity.MagicMirrorBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaMirrorBlockEntity;
import com.thaumcraftmodern.world.block.entity.PavingStoneOfWardingBlockEntity;
import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import com.thaumcraftmodern.world.block.entity.WardingAuraBlockEntity;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlockEntity;
import com.thaumcraftmodern.visnet.NodeStabilizerBlockEntity;
import com.thaumcraftmodern.visnet.NodeTransducerBlockEntity;
import com.thaumcraftmodern.visnet.VisChargeRelayBlockEntity;
import com.thaumcraftmodern.visnet.VisRelayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<BlockEntityType<ArcaneBellowsBlockEntity>>
            ARCANE_BELLOWS = BLOCK_ENTITIES.register("arcane_bellows",
                    () -> BlockEntityType.Builder.of(ArcaneBellowsBlockEntity::new,
                            ModBlocks.ARCANE_BELLOWS.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneLevitatorBlockEntity>> ARCANE_LEVITATOR =
            BLOCK_ENTITIES.register("arcane_levitator", () -> BlockEntityType.Builder.of(
                    ArcaneLevitatorBlockEntity::new, ModBlocks.ARCANE_LEVITATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneLampBlockEntity>> ARCANE_LAMP =
            BLOCK_ENTITIES.register("arcane_lamp", () -> BlockEntityType.Builder.of(
                    ArcaneLampBlockEntity::new, ModBlocks.ARCANE_LAMP.get()).build(null));
    public static final RegistryObject<BlockEntityType<GrowthLampBlockEntity>> GROWTH_LAMP =
            BLOCK_ENTITIES.register("lamp_growth", () -> BlockEntityType.Builder.of(
                    GrowthLampBlockEntity::new, ModBlocks.GROWTH_LAMP.get()).build(null));
    public static final RegistryObject<BlockEntityType<FertilityLampBlockEntity>> FERTILITY_LAMP =
            BLOCK_ENTITIES.register("lamp_fertility", () -> BlockEntityType.Builder.of(
                    FertilityLampBlockEntity::new, ModBlocks.FERTILITY_LAMP.get()).build(null));
    public static final RegistryObject<BlockEntityType<ItemGrateBlockEntity>> ITEM_GRATE =
            BLOCK_ENTITIES.register("item_grate", () -> BlockEntityType.Builder.of(
                    ItemGrateBlockEntity::new, ModBlocks.ITEM_GRATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<BrainJarBlockEntity>> BRAIN_JAR =
            BLOCK_ENTITIES.register("brain_jar", () -> BlockEntityType.Builder.of(
                    BrainJarBlockEntity::new, ModBlocks.BRAIN_JAR.get()).build(null));
    public static final RegistryObject<BlockEntityType<HungryChestBlockEntity>> HUNGRY_CHEST =
            BLOCK_ENTITIES.register("hungry_chest", () -> BlockEntityType.Builder.of(
                    HungryChestBlockEntity::new, ModBlocks.HUNGRY_CHEST.get()).build(null));
    public static final RegistryObject<BlockEntityType<FluxScrubberBlockEntity>> FLUX_SCRUBBER =
            BLOCK_ENTITIES.register("flux_scrubber", () -> BlockEntityType.Builder.of(
                    FluxScrubberBlockEntity::new, ModBlocks.FLUX_SCRUBBER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneDoorBlockEntity>> ARCANE_DOOR=
            BLOCK_ENTITIES.register("arcane_door",()->BlockEntityType.Builder.of(ArcaneDoorBlockEntity::new,ModBlocks.ARCANE_DOOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcanePressurePlateBlockEntity>> ARCANE_PRESSURE_PLATE =
            BLOCK_ENTITIES.register("arcane_pressure_plate", () -> BlockEntityType.Builder.of(
                    ArcanePressurePlateBlockEntity::new, ModBlocks.ARCANE_PRESSURE_PLATE.get()).build(null));
    public static final RegistryObject<BlockEntityType<WardedGlassBlockEntity>> WARDED_GLASS =
            BLOCK_ENTITIES.register("warded_glass", () -> BlockEntityType.Builder.of(
                    WardedGlassBlockEntity::new, ModBlocks.WARDED_GLASS.get()).build(null));
    public static final RegistryObject<BlockEntityType<MagicMirrorBlockEntity>>
            MAGIC_MIRROR = BLOCK_ENTITIES.register("magic_mirror",
                    () -> BlockEntityType.Builder.of(MagicMirrorBlockEntity::new,
                            ModBlocks.MAGIC_MIRROR.get()).build(null));
    public static final RegistryObject<BlockEntityType<EssentiaMirrorBlockEntity>>
            ESSENTIA_MIRROR = BLOCK_ENTITIES.register("essentia_mirror",
                    () -> BlockEntityType.Builder.of(EssentiaMirrorBlockEntity::new,
                            ModBlocks.ESSENTIA_MIRROR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ResearchTableBlockEntity>> RESEARCH_TABLE =
            BLOCK_ENTITIES.register("research_table",
                    () -> BlockEntityType.Builder.of(
                            ResearchTableBlockEntity::new,
                            ModBlocks.RESEARCH_TABLE.get()
                    ).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneWorkbenchBlockEntity>>
            ARCANE_WORKBENCH = BLOCK_ENTITIES.register(
                    "arcane_workbench",
                    () -> BlockEntityType.Builder.of(
                            ArcaneWorkbenchBlockEntity::new,
                            ModBlocks.ARCANE_WORKBENCH.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<ArcaneEarBlockEntity>>
            ARCANE_EAR = BLOCK_ENTITIES.register(
                    "arcane_ear",
                    () -> BlockEntityType.Builder.of(
                            ArcaneEarBlockEntity::new,
                            ModBlocks.ARCANE_EAR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<DeconstructionTableBlockEntity>>
            DECONSTRUCTION_TABLE = BLOCK_ENTITIES.register(
                    "deconstruction_table",
                    () -> BlockEntityType.Builder.of(
                            DeconstructionTableBlockEntity::new,
                            ModBlocks.DECONSTRUCTION_TABLE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<FocalManipulatorBlockEntity>>
            FOCAL_MANIPULATOR = BLOCK_ENTITIES.register("focal_manipulator",
                    () -> BlockEntityType.Builder.of(FocalManipulatorBlockEntity::new,
                            ModBlocks.FOCAL_MANIPULATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneSpaBlockEntity>> ARCANE_SPA =
            BLOCK_ENTITIES.register("arcane_spa", () -> BlockEntityType.Builder.of(
                    ArcaneSpaBlockEntity::new, ModBlocks.ARCANE_SPA.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneBoreBaseBlockEntity>>
            ARCANE_BORE_BASE = BLOCK_ENTITIES.register("arcane_bore_base",
                    () -> BlockEntityType.Builder.of(ArcaneBoreBaseBlockEntity::new,
                            ModBlocks.ARCANE_BORE_BASE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArcaneBoreBlockEntity>>
            ARCANE_BORE = BLOCK_ENTITIES.register("arcane_bore",
                    () -> BlockEntityType.Builder.of(ArcaneBoreBlockEntity::new,
                            ModBlocks.ARCANE_BORE.get()).build(null));
    public static final RegistryObject<BlockEntityType<TemporaryHoleBlockEntity>> TEMPORARY_HOLE =
            BLOCK_ENTITIES.register("temporary_hole", () -> BlockEntityType.Builder.of(
                    TemporaryHoleBlockEntity::new, ModBlocks.TEMPORARY_HOLE.get()).build(null));
    public static final RegistryObject<BlockEntityType<WardedBlockEntity>> WARDED_BLOCK =
            BLOCK_ENTITIES.register("warded_block", () -> BlockEntityType.Builder.of(
                    WardedBlockEntity::new, ModBlocks.WARDED_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<CrystalClusterBlockEntity>>
            CRYSTAL_CLUSTER = BLOCK_ENTITIES.register(
                    "crystal_cluster",
                    () -> BlockEntityType.Builder.of(
                            CrystalClusterBlockEntity::new,
                            ModBlocks.AIR_CRYSTAL_CLUSTER.get(),
                            ModBlocks.FIRE_CRYSTAL_CLUSTER.get(),
                            ModBlocks.WATER_CRYSTAL_CLUSTER.get(),
                            ModBlocks.EARTH_CRYSTAL_CLUSTER.get(),
                            ModBlocks.ORDER_CRYSTAL_CLUSTER.get(),
                            ModBlocks.ENTROPY_CRYSTAL_CLUSTER.get(),
                            ModBlocks.BALANCED_CRYSTAL_CLUSTER.get(),
                            ModBlocks.ELDRITCH_CRYSTAL_CLUSTER.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>>
            CRUCIBLE = BLOCK_ENTITIES.register(
                    "crucible",
                    () -> BlockEntityType.Builder.of(
                            CrucibleBlockEntity::new,
                            ModBlocks.CRUCIBLE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<ArcanePedestalBlockEntity>>
            ARCANE_PEDESTAL = BLOCK_ENTITIES.register(
                    "arcane_pedestal",
                    () -> BlockEntityType.Builder.of(
                            ArcanePedestalBlockEntity::new,
                            ModBlocks.ARCANE_PEDESTAL.get(),
                            ModBlocks.ELDRITCH_CAPSTONE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<WandRechargePedestalBlockEntity>>
            WAND_RECHARGE_PEDESTAL = BLOCK_ENTITIES.register(
                    "wand_recharge_pedestal",
                    () -> BlockEntityType.Builder.of(
                            WandRechargePedestalBlockEntity::new,
                            ModBlocks.WAND_RECHARGE_PEDESTAL.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<RunicMatrixBlockEntity>>
            RUNIC_MATRIX = BLOCK_ENTITIES.register(
                    "runic_matrix",
                    () -> BlockEntityType.Builder.of(
                            RunicMatrixBlockEntity::new,
                            ModBlocks.RUNIC_MATRIX.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<InfusionPillarBlockEntity>>
            INFUSION_PILLAR = BLOCK_ENTITIES.register(
                    "infusion_pillar",
                    () -> BlockEntityType.Builder.of(
                            InfusionPillarBlockEntity::new,
                            ModBlocks.INFUSION_PILLAR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<InfernalFurnaceBlockEntity>>
            INFERNAL_FURNACE = BLOCK_ENTITIES.register(
                    "infernal_furnace",
                    () -> BlockEntityType.Builder.of(
                            InfernalFurnaceBlockEntity::new,
                            ModBlocks.INFERNAL_FURNACE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<NitorBlockEntity>>
            NITOR = BLOCK_ENTITIES.register(
                    "nitor",
                    () -> BlockEntityType.Builder.of(
                            NitorBlockEntity::new,
                            ModBlocks.NITOR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<PavingStoneOfWardingBlockEntity>>
            PAVING_STONE_OF_WARDING = BLOCK_ENTITIES.register(
                    "paving_stone_of_warding",
                    () -> BlockEntityType.Builder.of(
                            PavingStoneOfWardingBlockEntity::new,
                            ModBlocks.PAVING_STONE_OF_WARDING.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<WardingAuraBlockEntity>>
            WARDING_AURA = BLOCK_ENTITIES.register(
                    "warding_aura",
                    () -> BlockEntityType.Builder.of(
                            WardingAuraBlockEntity::new,
                            ModBlocks.WARDING_AURA.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<AlchemicalFurnaceBlockEntity>>
            ALCHEMICAL_FURNACE = BLOCK_ENTITIES.register(
                    "alchemical_furnace",
                    () -> BlockEntityType.Builder.of(
                            AlchemicalFurnaceBlockEntity::new,
                            ModBlocks.ALCHEMICAL_FURNACE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<AdvancedAlchemicalFurnaceBlockEntity>>
            ADVANCED_ALCHEMICAL_FURNACE = BLOCK_ENTITIES.register(
                    "advanced_alchemical_furnace",
                    () -> BlockEntityType.Builder.of(
                            AdvancedAlchemicalFurnaceBlockEntity::new,
                            ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<ArcaneAlembicBlockEntity>>
            ARCANE_ALEMBIC = BLOCK_ENTITIES.register(
                    "arcane_alembic",
                    () -> BlockEntityType.Builder.of(
                            ArcaneAlembicBlockEntity::new,
                            ModBlocks.ARCANE_ALEMBIC.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaJarBlockEntity>>
            ESSENTIA_JAR = BLOCK_ENTITIES.register(
                    "essentia_jar",
                    () -> BlockEntityType.Builder.of(
                            EssentiaJarBlockEntity::new,
                            ModBlocks.WARDED_JAR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaTubeBlockEntity>>
            ESSENTIA_TUBE = BLOCK_ENTITIES.register(
                    "essentia_tube",
                    () -> BlockEntityType.Builder.of(
                            EssentiaTubeBlockEntity::new,
                            ModBlocks.ESSENTIA_TUBE.get(),
                            ModBlocks.FILTERED_ESSENTIA_TUBE.get(),
                            ModBlocks.RESTRICTED_ESSENTIA_TUBE.get(),
                            ModBlocks.ONE_WAY_ESSENTIA_TUBE.get(),
                            ModBlocks.ESSENTIA_VALVE.get(),
                            ModBlocks.REVERSIBLE_ESSENTIA_TUBE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<AdvancedEssentiaBufferBlockEntity>>
            ADVANCED_ESSENTIA_BUFFER = BLOCK_ENTITIES.register(
                    "advanced_essentia_buffer",
                    () -> BlockEntityType.Builder.of(
                            AdvancedEssentiaBufferBlockEntity::new,
                            ModBlocks.ADVANCED_ESSENTIA_BUFFER.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaBufferBlockEntity>>
            ESSENTIA_BUFFER = BLOCK_ENTITIES.register(
                    "essentia_buffer",
                    () -> BlockEntityType.Builder.of(EssentiaBufferBlockEntity::new,
                            ModBlocks.ESSENTIA_BUFFER.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<VoidJarBlockEntity>>
            VOID_JAR = BLOCK_ENTITIES.register(
                    "void_jar",
                    () -> BlockEntityType.Builder.of(VoidJarBlockEntity::new,
                            ModBlocks.VOID_JAR.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaCentrifugeBlockEntity>>
            ESSENTIA_CENTRIFUGE = BLOCK_ENTITIES.register(
                    "essentia_centrifuge",
                    () -> BlockEntityType.Builder.of(EssentiaCentrifugeBlockEntity::new,
                            ModBlocks.ESSENTIA_CENTRIFUGE.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaCrystallizerBlockEntity>>
            ESSENTIA_CRYSTALLIZER = BLOCK_ENTITIES.register(
                    "essentia_crystallizer",
                    () -> BlockEntityType.Builder.of(EssentiaCrystallizerBlockEntity::new,
                            ModBlocks.ESSENTIA_CRYSTALLIZER.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<EssentiaReservoirBlockEntity>>
            ESSENTIA_RESERVOIR = BLOCK_ENTITIES.register(
                    "essentia_reservoir",
                    () -> BlockEntityType.Builder.of(EssentiaReservoirBlockEntity::new,
                            ModBlocks.ESSENTIA_RESERVOIR.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<MnemonicMatrixBlockEntity>>
            MNEMONIC_MATRIX = BLOCK_ENTITIES.register(
                    "mnemonic_matrix",
                    () -> BlockEntityType.Builder.of(MnemonicMatrixBlockEntity::new,
                            ModBlocks.MNEMONIC_MATRIX.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<ThaumatoriumBlockEntity>>
            THAUMATORIUM = BLOCK_ENTITIES.register(
                    "thaumatorium",
                    () -> BlockEntityType.Builder.of(ThaumatoriumBlockEntity::new,
                            ModBlocks.THAUMATORIUM.get()).build(null)
            );
    public static final RegistryObject<BlockEntityType<AuraNodeBlockEntity>>
            AURA_NODE = BLOCK_ENTITIES.register(
                    "aura_node",
                    () -> BlockEntityType.Builder.of(
                            ModBlockEntities::createAuraNode,
                            ModBlocks.AURA_NODE.get(),
                            ModBlocks.SILVERWOOD_NODE.get(),
                            ModBlocks.OBSIDIAN_TOTEM_NODE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<JarredAuraNodeBlockEntity>>
            JARRED_AURA_NODE = BLOCK_ENTITIES.register(
                    "jarred_aura_node",
                    () -> BlockEntityType.Builder.of(
                            ModBlockEntities::createJarredAuraNode,
                            ModBlocks.JARRED_AURA_NODE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EnergizedAuraNodeBlockEntity>>
            ENERGIZED_AURA_NODE = BLOCK_ENTITIES.register(
                    "energized_aura_node",
                    () -> BlockEntityType.Builder.of(
                            EnergizedAuraNodeBlockEntity::new,
                            ModBlocks.ENERGIZED_AURA_NODE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<NodeStabilizerBlockEntity>>
            NODE_STABILIZER = BLOCK_ENTITIES.register(
                    "node_stabilizer",
                    () -> BlockEntityType.Builder.of(
                            NodeStabilizerBlockEntity::new,
                            ModBlocks.NODE_STABILIZER.get(),
                            ModBlocks.ADVANCED_NODE_STABILIZER.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<NodeTransducerBlockEntity>>
            NODE_TRANSDUCER = BLOCK_ENTITIES.register(
                    "node_transducer",
                    () -> BlockEntityType.Builder.of(
                            NodeTransducerBlockEntity::new,
                            ModBlocks.NODE_TRANSDUCER.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<VisRelayBlockEntity>>
            VIS_RELAY = BLOCK_ENTITIES.register(
                    "vis_relay",
                    () -> BlockEntityType.Builder.of(
                            VisRelayBlockEntity::new,
                            ModBlocks.VIS_RELAY.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<VisChargeRelayBlockEntity>>
            VIS_CHARGE_RELAY = BLOCK_ENTITIES.register(
                    "vis_charge_relay",
                    () -> BlockEntityType.Builder.of(
                            VisChargeRelayBlockEntity::new,
                            ModBlocks.VIS_CHARGE_RELAY.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EldritchAltarPartBlockEntity>>
            ELDRITCH_ALTAR_PART = BLOCK_ENTITIES.register(
                    "eldritch_altar_part",
                    () -> BlockEntityType.Builder.of(
                            EldritchAltarPartBlockEntity::new,
                            ModBlocks.ELDRITCH_ALTAR_PART.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EldritchLockBlockEntity>>
            ELDRITCH_LOCK = BLOCK_ENTITIES.register(
                    "eldritch_lock",
                    () -> BlockEntityType.Builder.of(
                            EldritchLockBlockEntity::new,
                            ModBlocks.ELDRITCH_LOCK.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EldritchCrabVentBlockEntity>>
            ELDRITCH_CRAB_VENT = BLOCK_ENTITIES.register(
                    "eldritch_crab_vent",
                    () -> BlockEntityType.Builder.of(
                            EldritchCrabVentBlockEntity::new,
                            ModBlocks.ELDRITCH_CRAB_VENT.get()
                    ).build(null)
            );
    public static final RegistryObject<
            BlockEntityType<EldritchRunedStoneBlockEntity>>
            ELDRITCH_RUNED_STONE = BLOCK_ENTITIES.register(
                    "eldritch_runed_stone",
                    () -> BlockEntityType.Builder.of(
                            EldritchRunedStoneBlockEntity::new,
                            ModBlocks.ELDRITCH_RUNED_STONE.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EldritchNothingBlockEntity>>
            ELDRITCH_NOTHING = BLOCK_ENTITIES.register(
                    "eldritch_nothing",
                    () -> BlockEntityType.Builder.of(
                            EldritchNothingBlockEntity::new,
                            ModBlocks.ELDRITCH_NOTHING_ANCHOR.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<OuterLandsPortalBlockEntity>>
            OUTER_LANDS_PORTAL = BLOCK_ENTITIES.register(
                    "outer_lands_portal",
                    () -> BlockEntityType.Builder.of(
                            OuterLandsPortalBlockEntity::new,
                            ModBlocks.OUTER_LANDS_PORTAL.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<EtherealBloomBlockEntity>>
            ETHEREAL_BLOOM = BLOCK_ENTITIES.register(
                    "ethereal_bloom",
                    () -> BlockEntityType.Builder.of(
                            EtherealBloomBlockEntity::new,
                            ModBlocks.ETHEREAL_BLOOM.get()
                    ).build(null)
            );
    public static final RegistryObject<BlockEntityType<ManaPodBlockEntity>>
            MANA_POD = BLOCK_ENTITIES.register(
                    "mana_pod",
                    () -> BlockEntityType.Builder.of(
                            ManaPodBlockEntity::new,
                            ModBlocks.MANA_POD.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }

    private static AuraNodeBlockEntity createAuraNode(
            BlockPos position,
            BlockState state
    ) {
        return new AuraNodeBlockEntity(AURA_NODE.get(), position, state);
    }

    private static JarredAuraNodeBlockEntity createJarredAuraNode(
            BlockPos position,
            BlockState state
    ) {
        return new JarredAuraNodeBlockEntity(
                JARRED_AURA_NODE.get(),
                position,
                state
        );
    }
}
