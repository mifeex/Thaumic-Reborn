package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlock;
import com.thaumcraftmodern.crystal.CrystalClusterVariant;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlock;
import com.thaumcraftmodern.world.block.ArcaneWorkbenchBlock;
import com.thaumcraftmodern.world.block.ArcaneBellowsBlock;
import com.thaumcraftmodern.world.block.ArcaneDoorBlock;
import com.thaumcraftmodern.world.block.ArcaneLevitatorBlock;
import com.thaumcraftmodern.world.block.ArcaneLampBlock;
import com.thaumcraftmodern.world.block.ArcanePressurePlateBlock;
import com.thaumcraftmodern.world.block.ArcaneLampLightBlock;
import com.thaumcraftmodern.world.block.ArcaneSpaBlock;
import com.thaumcraftmodern.world.block.ArcaneBoreBlock;
import com.thaumcraftmodern.world.block.ArcaneBoreBaseBlock;
import com.thaumcraftmodern.world.block.BrainJarBlock;
import com.thaumcraftmodern.world.block.HungryChestBlock;
import com.thaumcraftmodern.world.block.GolemFetterBlock;
import com.thaumcraftmodern.world.block.FluxScrubberBlock;
import com.thaumcraftmodern.world.block.FocalManipulatorBlock;
import com.thaumcraftmodern.world.block.TemporaryHoleBlock;
import com.thaumcraftmodern.world.block.WardedBlock;
import com.thaumcraftmodern.world.block.ArcanePedestalBlock;
import com.thaumcraftmodern.world.block.WandRechargePedestalBlock;
import com.thaumcraftmodern.world.block.CompoundRechargeFocusBlock;
import com.thaumcraftmodern.world.block.ArcaneEarBlock;
import com.thaumcraftmodern.world.block.AlchemicalFurnaceBlock;
import com.thaumcraftmodern.world.block.AdvancedAlchemicalFurnaceBlock;
import com.thaumcraftmodern.world.block.ArcaneAlembicBlock;
import com.thaumcraftmodern.world.block.ClassicPartBlock;
import com.thaumcraftmodern.world.block.CinderpearlBlock;
import com.thaumcraftmodern.world.block.CrucibleBlock;
import com.thaumcraftmodern.world.block.CrystalClusterBlock;
import com.thaumcraftmodern.world.block.ClassicCrystalSoundType;
import com.thaumcraftmodern.world.block.ClassicJarSoundType;
import com.thaumcraftmodern.world.block.DeconstructionTableBlock;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.world.block.EldritchCapstoneBlock;
import com.thaumcraftmodern.world.block.EldritchLockBlock;
import com.thaumcraftmodern.world.block.EldritchCrabVentBlock;
import com.thaumcraftmodern.world.block.EldritchCrystalBlock;
import com.thaumcraftmodern.world.block.EldritchRunedStoneBlock;
import com.thaumcraftmodern.world.block.EldritchBarrierBlock;
import com.thaumcraftmodern.world.block.EldritchNothingBlock;
import com.thaumcraftmodern.world.block.EldritchNothingAnchorBlock;
import com.thaumcraftmodern.world.block.OuterLandsPortalBlock;
import com.thaumcraftmodern.world.block.AncientStoneBlock;
import com.thaumcraftmodern.world.block.EtherealBloomBlock;
import com.thaumcraftmodern.world.block.EssentiaJarBlock;
import com.thaumcraftmodern.world.block.EssentiaBufferBlock;
import com.thaumcraftmodern.world.block.AdvancedEssentiaBufferBlock;
import com.thaumcraftmodern.world.block.EssentiaCentrifugeBlock;
import com.thaumcraftmodern.world.block.EssentiaCrystallizerBlock;
import com.thaumcraftmodern.world.block.EssentiaReservoirBlock;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import com.thaumcraftmodern.world.block.VoidJarBlock;
import com.thaumcraftmodern.essentia.tube.TubePolicyRegistry;
import com.thaumcraftmodern.world.block.FluxGasBlock;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import com.thaumcraftmodern.world.block.InfusionPillarBlock;
import com.thaumcraftmodern.world.block.InfernalFurnaceBlock;
import com.thaumcraftmodern.world.block.ItemGrateBlock;
import com.thaumcraftmodern.world.block.ManaPodBlock;
import com.thaumcraftmodern.world.block.MagicMirrorBlock;
import com.thaumcraftmodern.world.block.MnemonicMatrixBlock;
import com.thaumcraftmodern.world.block.NitorBlock;
import com.thaumcraftmodern.world.block.LootVesselBlock;
import com.thaumcraftmodern.world.block.ResearchTableBlock;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.TaintFibresBlock;
import com.thaumcraftmodern.world.block.TallowCandleBlock;
import com.thaumcraftmodern.world.block.PurifyingFluidBlock;
import com.thaumcraftmodern.world.block.LiquidDeathBlock;
import com.thaumcraftmodern.world.block.TaintedCaveVineBlock;
import com.thaumcraftmodern.world.block.TaintedGlowBerryVineBlock;
import com.thaumcraftmodern.world.block.TaintedPlantBlock;
import com.thaumcraftmodern.world.block.SpreadingTaintBlock;
import com.thaumcraftmodern.world.block.SpreadingTaintedLeavesBlock;
import com.thaumcraftmodern.world.block.ThaumcraftTableBlock;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import com.thaumcraftmodern.world.block.PavingStoneOfTravelBlock;
import com.thaumcraftmodern.world.block.PavingStoneOfWardingBlock;
import com.thaumcraftmodern.world.block.WardingAuraBlock;
import com.thaumcraftmodern.world.block.VishroomBlock;
import com.thaumcraftmodern.world.tree.MagicalTreeGrower;
import com.thaumcraftmodern.visnet.EnergizedAuraNodeBlock;
import com.thaumcraftmodern.visnet.NodeDeviceBlock;
import com.thaumcraftmodern.visnet.VisRelayBlock;
import com.thaumcraftmodern.worldgen.ModWorldgenKeys;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<Block> RESEARCH_TABLE = BLOCKS.register(
            "research_table",
            () -> new ResearchTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> THAUMCRAFT_TABLE = BLOCKS.register(
            "thaumcraft_table",
            () -> new ThaumcraftTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> ARCANE_WORKBENCH = BLOCKS.register(
            "arcane_workbench",
            () -> new ArcaneWorkbenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> ARCANE_EAR = BLOCKS.register(
            "arcane_ear",
            () -> new ArcaneEarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );
    public static final RegistryObject<Block> ARCANE_PRESSURE_PLATE = BLOCKS.register(
            "arcane_pressure_plate",
            () -> new ArcanePressurePlateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                    .strength(2.0F, 999.0F).noOcclusion())
    );
    public static final RegistryObject<Block> DECONSTRUCTION_TABLE =
            BLOCKS.register(
                    "deconstruction_table",
                    () -> new DeconstructionTableBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.WOOD)
                                    .strength(2.5F)
                                    .sound(SoundType.WOOD)
                                    .noOcclusion()
                    )
            );
    public static final RegistryObject<Block> FOCAL_MANIPULATOR =
            BLOCKS.register("focal_manipulator", () -> new FocalManipulatorBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                            .strength(2.0F, 10.0F).requiresCorrectToolForDrops()
                            .sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> ARCANE_SPA = BLOCKS.register(
            "arcane_spa", () -> new ArcaneSpaBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.0F, 25.0F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.STONE)
                            .noOcclusion()));
    public static final RegistryObject<Block> ARCANE_BORE_BASE = BLOCKS.register(
            "arcane_bore_base", () -> new ArcaneBoreBaseBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                            .strength(2.0F).noOcclusion()));
    public static final RegistryObject<Block> ARCANE_BORE = BLOCKS.register(
            "arcane_bore", () -> new ArcaneBoreBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                            .strength(2.0F).noOcclusion()));
    public static final RegistryObject<Block> TEMPORARY_HOLE = BLOCKS.register(
            "temporary_hole", () -> new TemporaryHoleBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).noCollission().noOcclusion().noLootTable()
                    .lightLevel(state -> 11)
                    .strength(-1.0F, 3600000.0F)));
    public static final RegistryObject<Block> WARDED_BLOCK = BLOCKS.register(
            "warded_block", () -> new WardedBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).noOcclusion().noLootTable()
                    .strength(-1.0F, 3600000.0F)));
    public static final RegistryObject<Block> AIR_CRYSTAL_CLUSTER =
            crystalCluster("air_crystal_cluster", CrystalClusterVariant.AIR);
    public static final RegistryObject<Block> FIRE_CRYSTAL_CLUSTER =
            crystalCluster("fire_crystal_cluster", CrystalClusterVariant.FIRE);
    public static final RegistryObject<Block> WATER_CRYSTAL_CLUSTER =
            crystalCluster("water_crystal_cluster", CrystalClusterVariant.WATER);
    public static final RegistryObject<Block> EARTH_CRYSTAL_CLUSTER =
            crystalCluster("earth_crystal_cluster", CrystalClusterVariant.EARTH);
    public static final RegistryObject<Block> ORDER_CRYSTAL_CLUSTER =
            crystalCluster("order_crystal_cluster", CrystalClusterVariant.ORDER);
    public static final RegistryObject<Block> ENTROPY_CRYSTAL_CLUSTER =
            crystalCluster("entropy_crystal_cluster", CrystalClusterVariant.ENTROPY);
    public static final RegistryObject<Block> BALANCED_CRYSTAL_CLUSTER =
            crystalCluster(
                    "balanced_crystal_cluster",
                    CrystalClusterVariant.BALANCED
            );
    public static final RegistryObject<Block> ELDRITCH_CRYSTAL_CLUSTER =
            BLOCKS.register(
                    "eldritch_crystal_cluster",
                    () -> new EldritchCrystalBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_LIGHT_BLUE)
                                    .strength(0.7F, 1.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(ClassicCrystalSoundType.INSTANCE)
                                    .lightLevel(state -> 8)
                                    .noOcclusion()
                    )
            );
    public static final RegistryObject<Block> ARCANE_STONE = BLOCKS.register(
            "arcane_stone",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(1.5F, 6.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))
    );
    public static final RegistryObject<Block> THAUMIUM_BLOCK = BLOCKS.register(
            "thaumium_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 10.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.STONE))
    );
    public static final RegistryObject<Block> ARCANE_STONE_BRICK =
            BLOCKS.register(
                    "arcane_stone_brick",
                    () -> new Block(arcaneStoneProperties())
            );
    public static final RegistryObject<Block> ARCANE_STONE_SLAB =
            BLOCKS.register(
                    "arcane_stone_slab",
                    () -> new SlabBlock(arcaneStoneProperties())
            );
    public static final RegistryObject<Block> PAVING_STONE_OF_TRAVEL =
            BLOCKS.register(
                    "paving_stone_of_travel",
                    () -> new PavingStoneOfTravelBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(2.0F, 10.0F)
                                    .sound(SoundType.STONE)
                                    .lightLevel(state -> 9)
                                    .isValidSpawn((state, level, pos, type) -> false)
                    )
            );
    public static final RegistryObject<Block> PAVING_STONE_OF_WARDING =
            BLOCKS.register(
                    "paving_stone_of_warding",
                    () -> new PavingStoneOfWardingBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(2.0F, 10.0F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.STONE)
                                    .isValidSpawn(
                                            (state, level, pos, type) -> false
                                    )
                    )
            );
    public static final RegistryObject<Block> WARDING_AURA = BLOCKS.register(
            "warding_aura",
            () -> new WardingAuraBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .replaceable()
                            .instabreak()
                            .noOcclusion()
                            .noLootTable()
            )
    );
    public static final RegistryObject<Block> LOOT_URN = BLOCKS.register(
            "loot_urn",
            () -> new LootVesselBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.TERRACOTTA_BROWN)
                            .strength(0.15F)
                            .sound(SoundType.DECORATED_POT)
                            .noOcclusion(),
                    true
            )
    );
    public static final RegistryObject<Block> LOOT_CRATE = BLOCKS.register(
            "loot_crate",
            () -> new LootVesselBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.WOOD)
                            .strength(0.15F)
                            .sound(SoundType.WOOD)
                            .noOcclusion(),
                    false
            )
    );
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register(
            "crucible",
            () -> new CrucibleBlock(metalDeviceProperties().noOcclusion())
    );
    public static final RegistryObject<Block> NITOR = BLOCKS.register(
            "nitor",
            () -> new NitorBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.FIRE)
                            .strength(0.0F)
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 15)
                            .sound(SoundType.WOOL)
            )
    );
    public static final RegistryObject<Block> ALCHEMICAL_FURNACE =
            BLOCKS.register(
                    "alchemical_furnace",
                    () -> new AlchemicalFurnaceBlock(
                            arcaneStoneProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> RUNIC_MATRIX = BLOCKS.register(
            "runic_matrix",
            () -> new RunicMatrixBlock(
                    arcaneStoneProperties()
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(
                                    RunicMatrixBlock.ACTIVE
                            ) ? 10 : 6)
            )
    );
    public static final RegistryObject<Block> ARCANE_PEDESTAL =
            BLOCKS.register(
                    "arcane_pedestal",
                    () -> new ArcanePedestalBlock(
                            arcaneStoneProperties().noOcclusion())
            );
    public static final RegistryObject<Block> WAND_RECHARGE_PEDESTAL =
            BLOCKS.register(
                    "wand_recharge_pedestal",
                    () -> new WandRechargePedestalBlock(
                            arcaneStoneProperties().noOcclusion())
            );
    public static final RegistryObject<Block> COMPOUND_RECHARGE_FOCUS =
            BLOCKS.register(
                    "compound_recharge_focus",
                    () -> new CompoundRechargeFocusBlock(
                            arcaneStoneProperties().noOcclusion())
            );
    public static final RegistryObject<Block> ARCANE_ALEMBIC =
            BLOCKS.register(
                    "arcane_alembic",
                    () -> new ArcaneAlembicBlock(
                            metalDeviceProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> WARDED_JAR = BLOCKS.register(
            "warded_jar",
            () -> new EssentiaJarBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(0.2F)
                            .sound(ClassicJarSoundType.INSTANCE)
                            .noOcclusion()
                            .noLootTable()
            )
    );
    public static final RegistryObject<Block> ESSENTIA_TUBE = essentiaTube(
            "essentia_tube", TubePolicyRegistry.PLAIN);
    public static final RegistryObject<Block> FILTERED_ESSENTIA_TUBE =
            essentiaTube("filtered_essentia_tube", TubePolicyRegistry.FILTERED);
    public static final RegistryObject<Block> RESTRICTED_ESSENTIA_TUBE =
            essentiaTube("restricted_essentia_tube", TubePolicyRegistry.RESTRICTED);
    public static final RegistryObject<Block> ONE_WAY_ESSENTIA_TUBE =
            essentiaTube("one_way_essentia_tube", TubePolicyRegistry.ONE_WAY);
    public static final RegistryObject<Block> ESSENTIA_VALVE = essentiaTube(
            "essentia_valve", TubePolicyRegistry.VALVE);
    public static final RegistryObject<Block> REVERSIBLE_ESSENTIA_TUBE =
            essentiaTube("reversible_essentia_tube",
                    TubePolicyRegistry.REVERSIBLE);
    public static final RegistryObject<Block> ESSENTIA_BUFFER = BLOCKS.register(
            "essentia_buffer",
            () -> new EssentiaBufferBlock(tubeDeviceProperties().noOcclusion())
    );
    public static final RegistryObject<Block> ADVANCED_ESSENTIA_BUFFER =
            BLOCKS.register("advanced_essentia_buffer",
                    () -> new AdvancedEssentiaBufferBlock(
                            tubeDeviceProperties().noOcclusion()));
    public static final RegistryObject<Block> VOID_JAR = BLOCKS.register(
            "void_jar",
            () -> new VoidJarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NONE).strength(0.2F)
                    .sound(ClassicJarSoundType.INSTANCE).noOcclusion().noLootTable())
    );
    public static final RegistryObject<Block> ESSENTIA_CENTRIFUGE = BLOCKS.register(
            "essentia_centrifuge",
            () -> new EssentiaCentrifugeBlock(tubeDeviceProperties().noOcclusion())
    );
    public static final RegistryObject<Block> ESSENTIA_CRYSTALLIZER = BLOCKS.register(
            "essentia_crystallizer",
            () -> new EssentiaCrystallizerBlock(tubeDeviceProperties().noOcclusion())
    );
    public static final RegistryObject<Block> ESSENTIA_RESERVOIR = BLOCKS.register(
            "essentia_reservoir",
            () -> new EssentiaReservoirBlock(
                    unrestrictedMetalProperties().noOcclusion())
    );
    public static final RegistryObject<Block> MAGIC_MIRROR = BLOCKS.register(
            "magic_mirror",
            () -> new MagicMirrorBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(1.0F, 10.0F).sound(SoundType.GLASS)
                    .noCollission().noOcclusion().noLootTable(), false)
    );
    public static final RegistryObject<Block> ESSENTIA_MIRROR = BLOCKS.register(
            "essentia_mirror",
            () -> new MagicMirrorBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(1.0F, 10.0F).sound(SoundType.GLASS)
                    .noCollission().noOcclusion().noLootTable(), true)
    );
    public static final RegistryObject<Block> MNEMONIC_MATRIX = BLOCKS.register(
            "mnemonic_matrix",
            () -> new MnemonicMatrixBlock(metalDeviceProperties().noOcclusion())
    );
    public static final RegistryObject<Block> ALCHEMICAL_CONSTRUCT =
            BLOCKS.register(
                    "alchemical_construct",
                    () -> new Block(metalDeviceProperties())
            );
    public static final RegistryObject<Block> ARCANE_BELLOWS = BLOCKS.register(
            "arcane_bellows",
            () -> new ArcaneBellowsBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                            .strength(2.5F).noOcclusion()
            )
    );
    public static final RegistryObject<Block> ARCANE_LAMP = BLOCKS.register(
            "arcane_lamp", () -> new ArcaneLampBlock(
                    metalDeviceProperties().noOcclusion()
                            .lightLevel(state -> 15), ArcaneLampBlock.Kind.ARCANE));
    public static final RegistryObject<Block> GROWTH_LAMP = BLOCKS.register(
            "lamp_growth", () -> new ArcaneLampBlock(
                    metalDeviceProperties().noOcclusion().lightLevel(state ->
                            state.getValue(ArcaneLampBlock.LIT) ? 15 : 8),
                    ArcaneLampBlock.Kind.GROWTH));
    public static final RegistryObject<Block> FERTILITY_LAMP = BLOCKS.register(
            "lamp_fertility", () -> new ArcaneLampBlock(
                    metalDeviceProperties().noOcclusion().lightLevel(state ->
                            state.getValue(ArcaneLampBlock.LIT) ? 15 : 8),
                    ArcaneLampBlock.Kind.FERTILITY));
    public static final RegistryObject<Block> ITEM_GRATE = BLOCKS.register(
            "item_grate", () -> new ItemGrateBlock(
                    metalDeviceProperties().noOcclusion()));
    public static final RegistryObject<Block> ARCANE_LAMP_LIGHT = BLOCKS.register(
            "arcane_lamp_light", () -> new ArcaneLampLightBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.NONE)
                            .replaceable().instabreak().noCollission().noOcclusion()
                            .noLootTable().lightLevel(state -> 15)));
    public static final RegistryObject<Block> ARCANE_LEVITATOR = BLOCKS.register(
            "arcane_levitator", () -> new ArcaneLevitatorBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2.5F, 15F).noOcclusion()));
    public static final RegistryObject<Block> ARCANE_DOOR = BLOCKS.register(
            "arcane_door", () -> new ArcaneDoorBlock(
                    BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).strength(15F, 999F).noOcclusion()));
    public static final RegistryObject<Block> BRAIN_JAR = BLOCKS.register(
            "brain_jar", () -> new BrainJarBlock(
                    BlockBehaviour.Properties.copy(Blocks.GLASS).strength(0.2F)
                            .sound(ClassicJarSoundType.INSTANCE).noOcclusion()));
    public static final RegistryObject<Block> HUNGRY_CHEST = BLOCKS.register(
            "hungry_chest", () -> new HungryChestBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(2.5F).noOcclusion()));
    public static final RegistryObject<Block> GOLEM_FETTER = BLOCKS.register(
            "golem_fetter", () -> new GolemFetterBlock(
                    BlockBehaviour.Properties.copy(Blocks.STONE).strength(2F, 10F)));
    public static final RegistryObject<Block> TALLOW_BLOCK = BLOCKS.register(
            "tallow_block", () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.HONEYCOMB_BLOCK).strength(4F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> FLESH_BLOCK = BLOCKS.register(
            "flesh_block", () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.MUD).strength(2F).sound(SoundType.SLIME_BLOCK)));
    public static final RegistryObject<Block> FLUX_SCRUBBER = BLOCKS.register(
            "flux_scrubber", () -> new FluxScrubberBlock(
                    metalDeviceProperties().noOcclusion()));
    public static final RegistryObject<Block> ADVANCED_ALCHEMICAL_CONSTRUCT =
            BLOCKS.register(
                    "advanced_alchemical_construct",
                    () -> new Block(metalDeviceProperties())
            );
    public static final RegistryObject<Block> INFUSION_PILLAR =
            BLOCKS.register(
                    "infusion_pillar",
                    () -> new InfusionPillarBlock(
                            arcaneStoneProperties()
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> INFERNAL_FURNACE =
            BLOCKS.register(
                    "infernal_furnace",
                    () -> new InfernalFurnaceBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.COLOR_BLACK)
                                    .strength(10.0F, 500.0F)
                                    .sound(SoundType.STONE)
                                    .lightLevel(state ->
                                            state.getValue(
                                                    InfernalFurnaceBlock.PART
                                            ) == 0 ? 13 : 3)
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> THAUMATORIUM =
            BLOCKS.register(
                    "thaumatorium",
                    () -> new ThaumatoriumBlock(
                            metalDeviceProperties()
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> ADVANCED_ALCHEMICAL_FURNACE =
            BLOCKS.register(
                    "advanced_alchemical_furnace",
                    () -> new AdvancedAlchemicalFurnaceBlock(
                            metalDeviceProperties()
                                    .noOcclusion()
                                    .lightLevel(state -> state.getValue(
                                            AdvancedAlchemicalFurnaceBlock.LIGHT))
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> AURA_NODE = BLOCKS.register(
            "aura_node",
            () -> new AuraNodeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(2.0F, 200.0F)
                            .sound(SoundType.EMPTY)
                            .noCollission()
                            .noOcclusion()
                            .noLootTable()
                            .lightLevel(state -> 8),
                    () -> ModBlockEntities.AURA_NODE.get()
            )
    );
    public static final RegistryObject<Block> ENERGIZED_AURA_NODE =
            BLOCKS.register(
                    "energized_aura_node",
                    () -> new EnergizedAuraNodeBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.NONE)
                                    .strength(2.0F, 200.0F)
                                    .sound(SoundType.EMPTY)
                                    .noCollission()
                                    .noOcclusion()
                                    .noLootTable()
                                    .lightLevel(state -> 12)
                    )
            );
    public static final RegistryObject<Block> NODE_STABILIZER =
            BLOCKS.register(
                    "node_stabilizer",
                    () -> new NodeDeviceBlock(
                            NodeDeviceBlock.Kind.STABILIZER,
                            arcaneStoneProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> ADVANCED_NODE_STABILIZER =
            BLOCKS.register(
                    "advanced_node_stabilizer",
                    () -> new NodeDeviceBlock(
                            NodeDeviceBlock.Kind.ADVANCED_STABILIZER,
                            arcaneStoneProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> NODE_TRANSDUCER =
            BLOCKS.register(
                    "node_transducer",
                    () -> new NodeDeviceBlock(
                            NodeDeviceBlock.Kind.TRANSDUCER,
                            arcaneStoneProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> VIS_RELAY = BLOCKS.register(
            "vis_relay",
            () -> new VisRelayBlock(
                    false,
                    metalDeviceProperties().noOcclusion()
            )
    );
    public static final RegistryObject<Block> VIS_CHARGE_RELAY =
            BLOCKS.register(
                    "vis_charge_relay",
                    () -> new VisRelayBlock(
                            true,
                            metalDeviceProperties().noOcclusion()
                    )
            );
    public static final RegistryObject<Block> JARRED_AURA_NODE = BLOCKS.register(
            "jarred_aura_node",
            () -> new JarredAuraNodeBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(0.2F)
                            .sound(ClassicJarSoundType.INSTANCE)
                            .noOcclusion()
                            .noLootTable()
                            .lightLevel(state -> 7),
                    () -> ModBlockEntities.JARRED_AURA_NODE.get()
            )
    );
    public static final RegistryObject<Block> TALLOW_CANDLE = BLOCKS.register(
            "tallow_candle",
            () -> new TallowCandleBlock(
                    BlockBehaviour.Properties.copy(Blocks.CANDLE)
                            .noOcclusion()
                            .lightLevel(state -> 14)
            )
    );
    public static final RegistryObject<Block> PURIFYING_FLUID = BLOCKS.register(
            "purifying_fluid",
            () -> new PurifyingFluidBlock(BlockBehaviour.Properties.copy(Blocks.WATER)
                    .noCollission().noLootTable().lightLevel(state -> 6))
    );
    public static final RegistryObject<Block> LIQUID_DEATH = BLOCKS.register(
            "liquid_death",
            () -> new LiquidDeathBlock(BlockBehaviour.Properties.copy(Blocks.WATER)
                    .noCollission().noLootTable().lightLevel(state -> 4))
    );

    public static final RegistryObject<Block> CINNABAR_ORE =
            ore("cinnabar_ore", UniformInt.of(1, 3));
    public static final RegistryObject<Block> AIR_INFUSED_STONE =
            infusedOre("air_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> FIRE_INFUSED_STONE =
            infusedOre("fire_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> WATER_INFUSED_STONE =
            infusedOre("water_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> EARTH_INFUSED_STONE =
            infusedOre("earth_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> ORDER_INFUSED_STONE =
            infusedOre("order_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> ENTROPY_INFUSED_STONE =
            infusedOre("entropy_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> DEEPSLATE_AIR_INFUSED_STONE =
            deepslateInfusedOre("deepslate_air_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> DEEPSLATE_FIRE_INFUSED_STONE =
            deepslateInfusedOre("deepslate_fire_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> DEEPSLATE_WATER_INFUSED_STONE =
            deepslateInfusedOre("deepslate_water_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> DEEPSLATE_EARTH_INFUSED_STONE =
            deepslateInfusedOre("deepslate_earth_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> DEEPSLATE_ORDER_INFUSED_STONE =
            deepslateInfusedOre("deepslate_order_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> DEEPSLATE_ENTROPY_INFUSED_STONE =
            deepslateInfusedOre("deepslate_entropy_infused_stone", UniformInt.of(1, 4));
    public static final RegistryObject<Block> AMBER_ORE =
            ore("amber_ore", UniformInt.of(1, 3));

    public static final RegistryObject<Block> GREATWOOD_LOG = BLOCKS.register(
            "greatwood_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                            .strength(2.0F)
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_LOG = BLOCKS.register(
            "silverwood_log",
            () -> new RotatedPillarBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LOG)
                            .strength(2.0F)
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_NODE = BLOCKS.register(
            "silverwood_node",
            () -> new AuraNodeBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS)
                            .strength(2.0F)
                            .noLootTable()
                            .lightLevel(state -> 7),
                    () -> ModBlockEntities.AURA_NODE.get(),
                    true
            )
    );
    public static final RegistryObject<Block> GREATWOOD_LEAVES = BLOCKS.register(
            "greatwood_leaves",
            () -> new LeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                            .strength(0.2F)
                            .randomTicks()
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_LEAVES = BLOCKS.register(
            "silverwood_leaves",
            () -> new LeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                            .strength(0.2F)
                            .randomTicks()
                            .noOcclusion()
                            .lightLevel(state -> 7)
            )
    );
    public static final RegistryObject<Block> GREATWOOD_PLANKS = BLOCKS.register(
            "greatwood_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS))
    );
    public static final RegistryObject<Block> SILVERWOOD_PLANKS = BLOCKS.register(
            "silverwood_planks",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS))
    );
    public static final RegistryObject<StairBlock> GREATWOOD_STAIRS = BLOCKS.register(
            "greatwood_stairs",
            () -> new StairBlock(
                    GREATWOOD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)
            )
    );
    public static final RegistryObject<StairBlock> SILVERWOOD_STAIRS = BLOCKS.register(
            "silverwood_stairs",
            () -> new StairBlock(
                    SILVERWOOD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS)
            )
    );
    public static final RegistryObject<SlabBlock> GREATWOOD_SLAB = BLOCKS.register(
            "greatwood_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB))
    );
    public static final RegistryObject<SlabBlock> SILVERWOOD_SLAB = BLOCKS.register(
            "silverwood_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SLAB))
    );
    public static final RegistryObject<Block> GREATWOOD_SAPLING = BLOCKS.register(
            "greatwood_sapling",
            () -> new SaplingBlock(
                    new MagicalTreeGrower(ModWorldgenKeys.GREATWOOD_TREE),
                    BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
            )
    );
    public static final RegistryObject<Block> SILVERWOOD_SAPLING = BLOCKS.register(
            "silverwood_sapling",
            () -> new SaplingBlock(
                    new MagicalTreeGrower(ModWorldgenKeys.SILVERWOOD_TREE),
                    BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
            )
    );

    public static final RegistryObject<Block> SHIMMERLEAF =
            flower("shimmerleaf", MobEffects.REGENERATION, 5, 1);
    public static final RegistryObject<Block> CINDERPEARL =
            BLOCKS.register(
                    "cinderpearl",
                    () -> new CinderpearlBlock(
                            MobEffects.FIRE_RESISTANCE,
                            5,
                            flowerProperties(3)
                    )
            );
    public static final RegistryObject<Block> ETHEREAL_BLOOM =
            BLOCKS.register(
                    "ethereal_bloom",
                    () -> new EtherealBloomBlock(
                            MobEffects.REGENERATION,
                            8,
                            flowerProperties(7)
                    )
            );
    public static final RegistryObject<Block> VISHROOM =
            BLOCKS.register(
                    "vishroom",
                    () -> new VishroomBlock(
                            MobEffects.NIGHT_VISION,
                            5,
                            flowerProperties(2)
                    )
            );
    public static final RegistryObject<Block> CRUSTED_TAINT = BLOCKS.register(
            "crusted_taint",
            () -> new SpreadingTaintBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.5F, 3.0F)
                            .sound(SoundType.GRAVEL)
                            .randomTicks()
            )
    );
    public static final RegistryObject<Block> TAINTED_SOIL = BLOCKS.register(
            "tainted_soil",
            () -> new SpreadingTaintBlock(
                    BlockBehaviour.Properties.copy(Blocks.DIRT)
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(1.5F, 3.0F)
                            .sound(SoundType.GRAVEL)
                            .randomTicks()
                    )
    );
    public static final RegistryObject<Block> TAINTED_LEAVES = BLOCKS.register(
            "tainted_leaves",
            () -> new SpreadingTaintedLeavesBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                            .mapColor(MapColor.COLOR_PURPLE)
                            .randomTicks()
            )
    );
    public static final RegistryObject<Block> TAINT_FIBRES = BLOCKS.register(
            "taint_fibres",
            () -> new TaintFibresBlock(
                    BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN)
                            .strength(1.0F, 5.0F)
                            .noCollission()
                            .randomTicks()
            )
    );
    /** Visual-only block for reviewing the second tainted-moss stage in game. */
    public static final RegistryObject<Block> TAINTED_CAVE_MOSS_TEST = BLOCKS.register(
            "tainted_cave_moss_test",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.MOSS_BLOCK))
    );
    /** Visual-only block for reviewing the tainted cave-vine texture in game. */
    public static final RegistryObject<Block> TAINTED_CAVE_VINE_TEST = BLOCKS.register(
            "tainted_cave_vine_test",
            () -> new TaintedCaveVineBlock(
                    BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN)
                            .noCollission()
            )
    );
    /** Visual-only cave-vine head with tainted glow berries. */
    public static final RegistryObject<Block> TAINTED_GLOW_BERRY_VINE_TEST = BLOCKS.register(
            "tainted_glow_berry_vine_test",
            () -> new TaintedGlowBerryVineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .replaceable()
                            .noCollission()
                            .noOcclusion()
                            .instabreak()
                            .sound(SoundType.VINE)
                            .lightLevel(state -> state.getValue(CaveVines.BERRIES) ? 12 : 0)
            )
    );
    public static final RegistryObject<Block> SHORT_TAINTED_GRASS =
            taintedPlant("short_tainted_grass", 0);
    public static final RegistryObject<Block> TALL_TAINTED_GRASS =
            taintedPlant("tall_tainted_grass", 8);
    public static final RegistryObject<Block> SPORE_STALK =
            taintedPlant("spore_stalk", 0);
    public static final RegistryObject<Block> MATURE_SPORE_STALK =
            taintedPlant("mature_spore_stalk", 10);
    public static final RegistryObject<Block> FLUX_GOO = BLOCKS.register(
            "flux_goo",
            () -> new FluxGooBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .strength(0.0F)
                            .noCollission()
                            .noOcclusion()
                            .randomTicks()
                    )
    );
    public static final RegistryObject<Block> FLUX_GAS = BLOCKS.register(
            "flux_gas",
            () -> new FluxGasBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .strength(0.0F)
                            .noCollission()
                            .noOcclusion()
                            .randomTicks()
                    )
    );
    public static final RegistryObject<Block> MANA_POD = BLOCKS.register(
            "mana_pod",
            () -> new ManaPodBlock(
                    BlockBehaviour.Properties.copy(Blocks.WHEAT)
                            .noCollission()
                            .randomTicks()
                            .lightLevel(state -> state.getValue(ManaPodBlock.AGE))
            )
    );

    public static final RegistryObject<Block> OBSIDIAN_TOTEM = BLOCKS.register(
            "obsidian_totem",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .strength(30.0F, 1200.0F)
            )
    );
    public static final RegistryObject<Block> OBSIDIAN_TOTEM_NODE =
            BLOCKS.register(
                    "obsidian_totem_node",
                    () -> new AuraNodeBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(30.0F, 1200.0F)
                                    .noLootTable()
                                    .lightLevel(state -> 9),
                            () -> ModBlockEntities.AURA_NODE.get(),
                            true
                    )
            );
    public static final RegistryObject<Block> OBSIDIAN_TILE = BLOCKS.register(
            "obsidian_tile",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .strength(30.0F, 1200.0F)
            )
    );
    public static final RegistryObject<Block> ELDRITCH_ALTAR_PART =
            BLOCKS.register(
                    "eldritch_altar_part",
                    () -> new EldritchAltarPartBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(30.0F, 1200.0F)
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> ELDRITCH_CAPSTONE =
            BLOCKS.register(
                    "eldritch_capstone",
                    () -> new EldritchCapstoneBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(50.0F, 20000.0F)
                                    .noOcclusion()
                                    .noLootTable()
                    )
            );
    /** TC4 blockEldritch:5, the luminous glyphed library-room stone. */
    public static final RegistryObject<Block> ELDRITCH_GLYPHED_STONE =
            BLOCKS.register(
                    "eldritch_glyphed_stone",
                    () -> new DropExperienceBlock(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(50.0F, 20000.0F)
                                    .lightLevel(state -> 12)
                                    .noOcclusion(),
                            UniformInt.of(1, 4)
                    )
            );
    /** TC4 blockEldritch:4, the animated luminous Outer Lands crust. */
    public static final RegistryObject<Block> ELDRITCH_GLOWING_CRUST =
            BLOCKS.register(
                    "eldritch_glowing_crust",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                                    .strength(2.0F, 30.0F)
                                    .lightLevel(state -> 12)
                                    .noOcclusion()
                    )
            );
    /** TC4 blockCosmeticSolid:15, used as the library's solid supports. */
    public static final RegistryObject<Block> ELDRITCH_PEDESTAL =
            BLOCKS.register(
                    "eldritch_pedestal",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                            Blocks.DEEPSLATE_BRICKS)
                                    .strength(2.0F, 10.0F)
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> ANCIENT_STONE = BLOCKS.register(
            "ancient_stone",
            () -> new AncientStoneBlock(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS)
                            .strength(4.0F, 40.0F)
                            .lightLevel(state -> state.getValue(
                                    AncientStoneBlock.VARIANT
                            ) == 3 ? 11 : 0)
            )
    );
    public static final RegistryObject<Block> ELDRITCH_RUNED_STONE =
            BLOCKS.register(
                    "eldritch_runed_stone",
                    () -> new EldritchRunedStoneBlock(
                            BlockBehaviour.Properties.copy(
                                            Blocks.DEEPSLATE_BRICKS)
                                    .strength(15.0F, 30.0F)
                                    .noLootTable()
                    )
            );
    public static final RegistryObject<Block> ANCIENT_ROCK = BLOCKS.register(
            "ancient_rock",
            () -> new AncientStoneBlock(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS)
                            .strength(4.0F, 40.0F)
            )
    );
    public static final RegistryObject<Block> ANCIENT_STAIRS = BLOCKS.register(
            "ancient_stairs",
            () -> new StairBlock(
                    ANCIENT_STONE.get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICK_STAIRS)
                            .strength(4.0F, 40.0F)
            )
    );
    public static final RegistryObject<Block> ANCIENT_SLAB = BLOCKS.register(
            "ancient_slab",
            () -> new SlabBlock(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICK_SLAB)
                            .strength(4.0F, 40.0F)
            )
    );
    public static final RegistryObject<Block> ANCIENT_CRUST = BLOCKS.register(
            "ancient_crust",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_BRICKS)
                            .strength(3.0F, 20.0F)
            )
    );
    public static final RegistryObject<Block> ANCIENT_SEAL = BLOCKS.register(
            "ancient_seal",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                            .noLootTable()
            )
    );
    public static final RegistryObject<Block> ELDRITCH_NOTHING = BLOCKS.register(
            "eldritch_nothing",
            () -> new EldritchNothingBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.NONE)
                            .noOcclusion()
                            .noLootTable()
                            .strength(-1.0F, 6000000.0F)
                            .sound(SoundType.STONE)
                            .lightLevel(state -> 3)
            )
    );
    public static final RegistryObject<Block> ELDRITCH_NOTHING_ANCHOR =
            BLOCKS.register(
                    "eldritch_nothing_anchor",
                    () -> new EldritchNothingAnchorBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.NONE)
                                    .noOcclusion()
                                    .noLootTable()
                                    .strength(-1.0F, 6000000.0F)
                                    .sound(SoundType.STONE)
                                    .lightLevel(state -> 3)
                    )
            );
    public static final RegistryObject<Block> ELDRITCH_DOOR = BLOCKS.register(
            "eldritch_door",
            () -> new Block(
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                            .noLootTable()
                            .lightLevel(state -> 12)
            )
    );
    public static final RegistryObject<Block> ELDRITCH_BARRIER = BLOCKS.register(
            "eldritch_barrier",
            () -> new EldritchBarrierBlock(
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                            .noLootTable()
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> ELDRITCH_LOCK = BLOCKS.register(
            "eldritch_lock",
            () -> new EldritchLockBlock(
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                            .noLootTable()
                            .lightLevel(state -> 7)
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> ELDRITCH_CRAB_VENT = BLOCKS.register(
            "eldritch_crab_vent",
            () -> new EldritchCrabVentBlock(
                    BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                            .noLootTable()
                            .noOcclusion()
            )
    );
    public static final RegistryObject<Block> OUTER_LANDS_PORTAL = BLOCKS.register(
            "outer_lands_portal",
            () -> new OuterLandsPortalBlock(
                    BlockBehaviour.Properties.of()
                            .strength(-1.0F, 3600000.0F)
                            .noCollission()
                            .noOcclusion()
                            .lightLevel(state -> 11)
                            .noLootTable()
            )
    );

    private ModBlocks() {
    }

    private static RegistryObject<Block> crystalCluster(
            String name,
            CrystalClusterVariant variant
    ) {
        return BLOCKS.register(
                name,
                () -> new CrystalClusterBlock(
                        variant,
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.COLOR_LIGHT_BLUE)
                                .strength(0.7F, 1.0F)
                                .sound(ClassicCrystalSoundType.INSTANCE)
                                .lightLevel(state -> 8)
                                .requiresCorrectToolForDrops()
                                .noOcclusion()
                )
        );
    }

    private static RegistryObject<Block> taintedPlant(
            String name,
            int light
    ) {
        return BLOCKS.register(
                name,
                () -> new TaintedPlantBlock(
                        BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH)
                                .mapColor(MapColor.COLOR_PURPLE)
                                .noCollission()
                                .randomTicks()
                                .lightLevel(state -> light)
                )
        );
    }

    private static BlockBehaviour.Properties arcaneStoneProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(1.5F, 6.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties metalDeviceProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 17.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties tubeDeviceProperties() {
        return unrestrictedMetalProperties();
    }

    /** TC4 {@code BlockTube}: hardness 0.5, resistance 5, no harvest tier. */
    private static BlockBehaviour.Properties classicTubeProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(0.5F, 5.0F)
                .sound(SoundType.METAL);
    }

    private static BlockBehaviour.Properties unrestrictedMetalProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 17.0F)
                .sound(SoundType.METAL);
    }

    private static RegistryObject<Block> ore(
            String name,
            UniformInt experience
    ) {
        return BLOCKS.register(
                name,
                () -> new DropExperienceBlock(
                        BlockBehaviour.Properties.copy(Blocks.STONE)
                                .strength(3.0F, 5.0F)
                                .requiresCorrectToolForDrops(),
                        experience
                )
        );
    }

    private static RegistryObject<Block> infusedOre(
            String name,
            UniformInt experience
    ) {
        return BLOCKS.register(
                name,
                () -> new DropExperienceBlock(
                        BlockBehaviour.Properties.copy(Blocks.STONE)
                                .strength(3.0F, 5.0F)
                                .lightLevel(state -> 4)
                                .requiresCorrectToolForDrops(),
                        experience
                )
        );
    }

    private static RegistryObject<Block> deepslateOre(
            String name,
            UniformInt experience
    ) {
        return BLOCKS.register(
                name,
                () -> new DropExperienceBlock(
                        BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                                .strength(4.5F, 3.0F)
                                .requiresCorrectToolForDrops(),
                        experience
                )
        );
    }

    private static RegistryObject<Block> deepslateInfusedOre(
            String name,
            UniformInt experience
    ) {
        return BLOCKS.register(
                name,
                () -> new DropExperienceBlock(
                        BlockBehaviour.Properties.copy(Blocks.DEEPSLATE)
                                .strength(4.5F, 3.0F)
                                .lightLevel(state -> 4)
                                .requiresCorrectToolForDrops(),
                        experience
                )
        );
    }

    private static RegistryObject<Block> essentiaTube(
            String name,
            net.minecraft.resources.ResourceLocation policy
    ) {
        return BLOCKS.register(
                name,
                () -> new EssentiaTubeBlock(
                        classicTubeProperties().noOcclusion(),
                        policy
                )
        );
    }

    private static RegistryObject<Block> flower(
            String name,
            net.minecraft.world.effect.MobEffect effect,
            int duration,
            int light
    ) {
        return BLOCKS.register(
                name,
                () -> new FlowerBlock(
                        effect,
                        duration,
                        flowerProperties(light)
                )
        );
    }

    private static BlockBehaviour.Properties flowerProperties(int light) {
        return BlockBehaviour.Properties.copy(Blocks.DANDELION)
                .noCollission()
                .instabreak()
                .noOcclusion()
                .lightLevel(state -> light);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
