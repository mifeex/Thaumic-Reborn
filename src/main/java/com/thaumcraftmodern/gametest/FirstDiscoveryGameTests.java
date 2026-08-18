package com.thaumcraftmodern.gametest;

import com.mojang.authlib.GameProfile;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBlockEntity;
import com.thaumcraftmodern.aura.AuraNodeFactory;
import com.thaumcraftmodern.aura.AuraNodeModifier;
import com.thaumcraftmodern.aura.AuraNodeState;
import com.thaumcraftmodern.aura.AuraNodeType;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.arcane.ModArcaneRecipes;
import com.thaumcraftmodern.construction.ConstructionDefinition;
import com.thaumcraftmodern.construction.ConstructionRegistry;
import com.thaumcraftmodern.construction.ClassicStructureConstructionEvents;
import com.thaumcraftmodern.crucible.CrucibleItemTossEvents;
import com.thaumcraftmodern.crucible.EssentiaStore;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import com.thaumcraftmodern.entity.PechBehavior;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.EssentiaPhialItem;
import com.thaumcraftmodern.item.EssentiaCrystalItem;
import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.WardedJarItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.knowledge.KnowledgeCapabilities;
import com.thaumcraftmodern.knowledge.PlayerThaumKnowledge;
import com.thaumcraftmodern.nodejar.JarredAuraNodeBlockEntity;
import com.thaumcraftmodern.nodejar.NodeJarCodec;
import com.thaumcraftmodern.nodejar.NodeJarData;
import com.thaumcraftmodern.nodejar.NodeJarFactory;
import com.thaumcraftmodern.nodejar.NodeJarSavedData;
import com.thaumcraftmodern.nodejar.ServerNodeJarWorld;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModEffects;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.research.ResearchRegistry;
import com.thaumcraftmodern.research.ResearchCategoryRegistry;
import com.thaumcraftmodern.research.ResearchCompletionService;
import com.thaumcraftmodern.research.ResearchProgressService;
import com.thaumcraftmodern.research.ResearchDuplicationService;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.EntityScanIdentity;
import com.thaumcraftmodern.scan.ScanService;
import com.thaumcraftmodern.scan.ScanSessionManager;
import com.thaumcraftmodern.scan.ScanTargetType;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import com.thaumcraftmodern.wand.WandVisService;
import com.thaumcraftmodern.world.block.ResearchTableBlock;
import com.thaumcraftmodern.world.block.ResearchTablePart;
import com.thaumcraftmodern.world.block.ClassicPartBlock;
import com.thaumcraftmodern.world.block.InfernalFurnaceBlock;
import com.thaumcraftmodern.world.block.CrucibleBlock;
import com.thaumcraftmodern.world.block.EerieBiomeService;
import com.thaumcraftmodern.world.block.EssentiaTubeBlock;
import com.thaumcraftmodern.world.block.EssentiaCrystallizerBlock;
import com.thaumcraftmodern.world.block.EssentiaReservoirBlock;
import com.thaumcraftmodern.world.block.MagicalForestBiomeService;
import com.thaumcraftmodern.world.block.MnemonicMatrixBlock;
import com.thaumcraftmodern.world.block.RunicMatrixBlock;
import com.thaumcraftmodern.world.block.ThaumatoriumBlock;
import com.thaumcraftmodern.world.block.TaintBiomeService;
import com.thaumcraftmodern.world.block.EldritchAltarPartBlock;
import com.thaumcraftmodern.world.block.FluxGasBlock;
import com.thaumcraftmodern.world.block.FluxGooBlock;
import com.thaumcraftmodern.world.block.entity.ArcaneWorkbenchBlockEntity;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import com.thaumcraftmodern.world.block.entity.AlchemicalFurnaceBlockEntity;
import com.thaumcraftmodern.world.block.entity.ArcaneAlembicBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaTubeBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaBufferBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaCentrifugeBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaCrystallizerBlockEntity;
import com.thaumcraftmodern.world.block.entity.EssentiaReservoirBlockEntity;
import com.thaumcraftmodern.world.block.entity.MnemonicMatrixBlockEntity;
import com.thaumcraftmodern.world.block.entity.ThaumatoriumBlockEntity;
import com.thaumcraftmodern.world.block.entity.VoidJarBlockEntity;
import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import com.thaumcraftmodern.world.block.entity.EldritchAltarPartBlockEntity;
import com.thaumcraftmodern.world.menu.ArcaneWorkbenchMenu;
import com.thaumcraftmodern.worldgen.GreatwoodTreeFeature;
import com.thaumcraftmodern.worldgen.LegacyStructureKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashSet;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@GameTestHolder(ThaumcraftModern.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FirstDiscoveryGameTests {
    private static final BlockPos TABLE_POSITION = new BlockPos(2, 1, 2);
    private static final BlockPos BOOKSHELF_POSITION = new BlockPos(2, 1, 2);
    private static final BlockPos INVALID_WAND_TARGET = new BlockPos(4, 1, 2);

    @GameTest(template = "empty")
    public static void pechVariantPackAndTrustSurviveSaveReload(
            GameTestHelper helper
    ) {
        LegacyThaumcraftMob source = ModEntities.PECH.get().create(
                helper.getLevel()
        );
        LegacyThaumcraftMob restored = ModEntities.PECH.get().create(
                helper.getLevel()
        );
        helper.assertTrue(
                source != null && restored != null,
                "Pech entity type could not create save/reload fixtures"
        );
        source.setPechType(PechBehavior.STALKER);
        source.setPechTamed(true);
        source.pechPack().setStackInSlot(
                3,
                new ItemStack(Items.DIAMOND, 2)
        );
        CompoundTag saved = new CompoundTag();
        source.addAdditionalSaveData(saved);
        restored.readAdditionalSaveData(saved);

        helper.assertTrue(
                restored.pechType() == PechBehavior.STALKER
                        && restored.isPechTamed(),
                "Pech variant or trust did not survive save/reload"
        );
        helper.assertTrue(
                restored.pechPack().getStackInSlot(3).is(Items.DIAMOND)
                        && restored.pechPack().getStackInSlot(3).getCount() == 2,
                "Pech nine-slot pack did not survive save/reload"
        );
        helper.assertTrue(
                EntityScanIdentity.targetId(restored).equals(
                        "thaumic_reborn:pech/stalker"
                ),
                "Pech variant did not expose its distinct scan identity"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void taintBiomeColumnCanSpreadAndBePurified(
            GameTestHelper helper
    ) {
        BlockPos position = helper.absolutePos(new BlockPos(2, 1, 2));
        helper.assertTrue(
                TaintBiomeService.spreadChanceBound() == 1000,
                "TC4 default taint biome chance is not 1/1000"
        );
        if (TaintBiomeService.isTainted(helper.getLevel(), position)) {
            TaintBiomeService.purifyColumn(helper.getLevel(), position);
        }
        helper.assertTrue(
                TaintBiomeService.taintColumn(helper.getLevel(), position),
                "Taint could not mutate a biome column"
        );
        helper.assertTrue(
                TaintBiomeService.isTainted(helper.getLevel(), position),
                "Mutated biome column is not Tainted Lands"
        );
        helper.assertTrue(
                TaintBiomeService.purifyColumn(helper.getLevel(), position),
                "Ethereal Bloom purification could not restore a column"
        );
        helper.assertTrue(
                !TaintBiomeService.isTainted(helper.getLevel(), position),
                "Purified biome column remained Tainted Lands"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void sinisterNodePaintsEerieBiomeAtClassicInterval(
            GameTestHelper helper
    ) {
        BlockPos relativeNode = new BlockPos(2, 2, 2);
        BlockPos absoluteNode = helper.absolutePos(relativeNode);
        helper.setBlock(relativeNode, ModBlocks.AURA_NODE.get());
        helper.assertTrue(
                helper.getBlockEntity(relativeNode)
                        instanceof AuraNodeBlockEntity,
                "Sinister test node block entity was not created"
        );
        AuraNodeBlockEntity node = (AuraNodeBlockEntity)
                helper.getBlockEntity(relativeNode);
        helper.assertTrue(
                node.initializeOnce(AuraNodeFactory.structureNode(
                        absoluteNode,
                        AuraNodeType.DARK
                )),
                "Sinister test node rejected its state"
        );

        helper.runAfterDelay(55, () -> {
            boolean foundEerie = false;
            for (int x = -11; x <= 11 && !foundEerie; x++) {
                for (int z = -11; z <= 11; z++) {
                    BlockPos sample = absoluteNode.offset(x, 0, z);
                    if (EerieBiomeService.isEerie(
                            helper.getLevel(),
                            sample
                    )) {
                        foundEerie = true;
                        break;
                    }
                }
            }
            helper.assertTrue(
                    foundEerie,
                    "Sinister node did not paint Eerie biome after 50 ticks"
            );
            helper.succeed();
        });
    }

    @GameTest(
            template = "empty",
            batch = "pureNodeBiome",
            timeoutTicks = 100
    )
    public static void silverwoodNodePaintsMagicalForestAtClassicInterval(
            GameTestHelper helper
    ) {
        BlockPos relativeNode = new BlockPos(8, 2, 8);
        BlockPos absoluteNode = helper.absolutePos(relativeNode);
        helper.setBlock(relativeNode, ModBlocks.SILVERWOOD_NODE.get());
        helper.assertTrue(
                helper.getBlockEntity(relativeNode)
                        instanceof AuraNodeBlockEntity,
                "Silverwood node block entity was not created"
        );
        AuraNodeBlockEntity node = (AuraNodeBlockEntity)
                helper.getBlockEntity(relativeNode);
        helper.assertTrue(
                node.initializeOnce(AuraNodeFactory.structureNode(
                        absoluteNode,
                        AuraNodeType.PURE
                )),
                "Silverwood test node rejected its pure state"
        );

        helper.runAfterDelay(55, () -> {
            boolean foundMagicalForest = false;
            for (int x = -7; x <= 7 && !foundMagicalForest; x++) {
                for (int z = -7; z <= 7; z++) {
                    if (MagicalForestBiomeService.isMagicalForest(
                            helper.getLevel(),
                            absoluteNode.offset(x, 0, z)
                    )) {
                        foundMagicalForest = true;
                        break;
                    }
                }
            }
            helper.assertTrue(
                    foundMagicalForest,
                    "Silverwood node did not paint Magical Forest "
                            + "after 50 ticks"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void finiteFluxGooSpreadsAndFluxGasRises(
            GameTestHelper helper
    ) {
        BlockPos goo = new BlockPos(2, 2, 2);
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
            }
        }
        helper.setBlock(
                goo,
                ModBlocks.FLUX_GOO.get().defaultBlockState()
                        .setValue(FluxGooBlock.LEVEL, 7)
        );
        BlockPos gas = new BlockPos(6, 1, 2);
        helper.setBlock(
                gas,
                ModBlocks.FLUX_GAS.get().defaultBlockState()
                        .setValue(FluxGasBlock.LEVEL, 0)
        );
        helper.assertTrue(
                FluxGooBlock.isReplaceableLevel(0),
                "One-quantum Flux Goo should be replaceable"
        );
        helper.assertTrue(
                !FluxGooBlock.isReplaceableLevel(7),
                "Full Flux Goo should not be replaceable"
        );
        BlockPos thinGoo = new BlockPos(8, 2, 2);
        helper.setBlock(thinGoo.below(), Blocks.STONE);
        helper.setBlock(
                thinGoo,
                ModBlocks.FLUX_GOO.get().defaultBlockState()
                        .setValue(FluxGooBlock.LEVEL, 0)
        );
        BlockState thinState = helper.getBlockState(thinGoo);
        BlockPos thinAbsolute = helper.absolutePos(thinGoo);
        helper.assertTrue(
                thinState.getShape(
                        helper.getLevel(),
                        thinAbsolute,
                        CollisionContext.empty()
                ).isEmpty(),
                "Thin Flux Goo still has a selection outline"
        );
        LiquidBlockContainer container =
                (LiquidBlockContainer) ModBlocks.FLUX_GOO.get();
        helper.assertTrue(
                container.canPlaceLiquid(
                        helper.getLevel(),
                        thinAbsolute,
                        thinState,
                        Fluids.WATER
                ),
                "Thin Flux Goo rejected water"
        );
        helper.assertTrue(
                container.placeLiquid(
                        helper.getLevel(),
                        thinAbsolute,
                        thinState,
                        Fluids.WATER.getSource(false)
                ),
                "Water did not replace thin Flux Goo"
        );
        helper.assertBlockPresent(Blocks.WATER, thinGoo);
        helper.setBlock(thinGoo, Blocks.AIR);
        helper.runAfterDelay(35, () -> {
            int gooCells = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (helper.getBlockState(goo.relative(direction))
                        .is(ModBlocks.FLUX_GOO.get())) {
                    gooCells++;
                }
            }
            if (helper.getBlockState(goo).is(ModBlocks.FLUX_GOO.get())) {
                gooCells++;
            }
            helper.assertTrue(
                    gooCells >= 2,
                    "Full Flux Goo did not spread across its surface"
            );
            helper.assertBlockPresent(Blocks.AIR, gas);
            boolean rose = false;
            for (int y = 2; y <= 5; y++) {
                if (helper.getBlockState(new BlockPos(6, y, 2))
                        .is(ModBlocks.FLUX_GAS.get())) {
                    rose = true;
                    break;
                }
            }
            helper.assertTrue(rose, "Flux Gas did not rise through open air");
            helper.succeed();
        });
    }

    @GameTest(
            template = "empty",
            batch = "fluxWaterWash",
            timeoutTicks = 100
    )
    public static void flowingAndSourceWaterWashGooAndGasEqually(
            GameTestHelper helper
    ) {
        BlockState flowingWater = Blocks.WATER.defaultBlockState()
                .setValue(LiquidBlock.LEVEL, 3);

        BlockPos weakGoo = new BlockPos(2, 2, 2);
        BlockPos weakGas = new BlockPos(2, 2, 7);
        helper.setBlock(
                weakGoo,
                ModBlocks.FLUX_GOO.get().defaultBlockState()
                        .setValue(FluxGooBlock.LEVEL, 4)
        );
        helper.setBlock(
                weakGas,
                ModBlocks.FLUX_GAS.get().defaultBlockState()
                        .setValue(FluxGasBlock.LEVEL, 4)
        );
        helper.setBlock(weakGoo.east(), flowingWater);
        helper.setBlock(weakGas.east(), flowingWater);
        helper.assertBlockNotPresent(ModBlocks.FLUX_GOO.get(), weakGoo);
        helper.assertBlockNotPresent(ModBlocks.FLUX_GAS.get(), weakGas);

        BlockPos strongGoo = new BlockPos(7, 2, 2);
        BlockPos strongGas = new BlockPos(7, 2, 7);
        helper.setBlock(
                strongGoo,
                ModBlocks.FLUX_GOO.get().defaultBlockState()
                        .setValue(FluxGooBlock.LEVEL, 5)
        );
        helper.setBlock(
                strongGas,
                ModBlocks.FLUX_GAS.get().defaultBlockState()
                        .setValue(FluxGasBlock.LEVEL, 5)
        );
        helper.setBlock(strongGoo.east(), flowingWater);
        helper.setBlock(strongGas.east(), flowingWater);
        helper.assertBlockPresent(ModBlocks.FLUX_GOO.get(), strongGoo);
        helper.assertBlockPresent(ModBlocks.FLUX_GAS.get(), strongGas);

        BlockPos fullGoo = new BlockPos(12, 2, 2);
        BlockPos fullGas = new BlockPos(12, 2, 7);
        helper.setBlock(
                fullGoo,
                ModBlocks.FLUX_GOO.get().defaultBlockState()
                        .setValue(FluxGooBlock.LEVEL, 7)
        );
        helper.setBlock(
                fullGas,
                ModBlocks.FLUX_GAS.get().defaultBlockState()
                        .setValue(FluxGasBlock.LEVEL, 7)
        );
        helper.setBlock(fullGoo.east(), Blocks.WATER);
        helper.setBlock(fullGas.east(), Blocks.WATER);
        helper.assertBlockNotPresent(ModBlocks.FLUX_GOO.get(), fullGoo);
        helper.assertBlockNotPresent(ModBlocks.FLUX_GAS.get(), fullGas);

        BlockPos bucketGoo = new BlockPos(17, 2, 2);
        BlockPos bucketGas = new BlockPos(17, 2, 7);
        helper.setBlock(
                bucketGoo,
                ModBlocks.FLUX_GOO.get().defaultBlockState()
                        .setValue(FluxGooBlock.LEVEL, 7)
        );
        helper.setBlock(
                bucketGas,
                ModBlocks.FLUX_GAS.get().defaultBlockState()
                        .setValue(FluxGasBlock.LEVEL, 7)
        );
        LiquidBlockContainer gooContainer =
                (LiquidBlockContainer) ModBlocks.FLUX_GOO.get();
        LiquidBlockContainer gasContainer =
                (LiquidBlockContainer) ModBlocks.FLUX_GAS.get();
        BlockPos absoluteBucketGoo = helper.absolutePos(bucketGoo);
        BlockPos absoluteBucketGas = helper.absolutePos(bucketGas);
        helper.assertTrue(
                gooContainer.placeLiquid(
                        helper.getLevel(),
                        absoluteBucketGoo,
                        helper.getBlockState(bucketGoo),
                        Fluids.WATER.getSource(false)
                ),
                "A water source could not replace full Flux Goo"
        );
        helper.assertTrue(
                gasContainer.placeLiquid(
                        helper.getLevel(),
                        absoluteBucketGas,
                        helper.getBlockState(bucketGas),
                        Fluids.WATER.getSource(false)
                ),
                "A water source could not replace full Flux Gas"
        );
        helper.assertBlockPresent(Blocks.WATER, bucketGoo);
        helper.assertBlockPresent(Blocks.WATER, bucketGas);
        for (BlockPos cleanup : new BlockPos[]{
                weakGoo, weakGoo.east(),
                weakGas, weakGas.east(),
                strongGoo, strongGoo.east(),
                strongGas, strongGas.east(),
                fullGoo, fullGoo.east(),
                fullGas, fullGas.east(),
                bucketGoo, bucketGas
        }) {
            helper.getLevel().removeBlock(
                    helper.absolutePos(cleanup),
                    false
            );
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legacySitesAreRegisteredForLocate(
            GameTestHelper helper
    ) {
        var structures = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.STRUCTURE);
        for (LegacyStructureKind kind : LegacyStructureKind.values()) {
            ResourceLocation id = new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    kind.serializedName()
            );
            helper.assertTrue(
                    structures.containsKey(id),
                    "Missing /locate structure entry: " + id
            );
        }
        helper.succeed();
    }
    private static final List<String> REQUIRED_RECIPES = List.of(
            "air_shard",
            "alchemical_furnace",
            "arcane_stone",
            "arcane_stone_brick",
            "arcane_pedestal",
            "blaze_powder_from_cinderpearl",
            "earth_shard",
            "entropy_shard",
            "fire_shard",
            "goggles_of_revealing",
            "knowledge_fragment",
            "iron_wand_cap",
            "basic_wand",
            "order_shard",
            "quicksilver_from_shimmerleaf",
            "research_notes",
            "runic_matrix",
            "scribing_tools",
            "thaumcraft_table",
            "thaumometer",
            "water_shard"
    );
    private static final Set<String> REQUIRED_RECIPE_DERIVED_BLOCK_SCANS =
            Set.of(
                    "minecraft:activator_rail",
                    "minecraft:beacon",
                    "minecraft:bookshelf",
                    "minecraft:chest",
                    "minecraft:daylight_detector",
                    "minecraft:detector_rail",
                    "minecraft:dispenser",
                    "minecraft:dropper",
                    "minecraft:enchanting_table",
                    "minecraft:ender_chest",
                    "minecraft:furnace",
                    "minecraft:heavy_weighted_pressure_plate",
                    "minecraft:hopper",
                    "minecraft:jukebox",
                    "minecraft:lever",
                    "minecraft:light_weighted_pressure_plate",
                    "minecraft:melon",
                    "minecraft:note_block",
                    "minecraft:oak_button",
                    "minecraft:oak_fence_gate",
                    "minecraft:oak_pressure_plate",
                    "minecraft:oak_trapdoor",
                    "minecraft:piston",
                    "minecraft:powered_rail",
                    "minecraft:rail",
                    "minecraft:redstone_torch",
                    "minecraft:sandstone",
                    "minecraft:sticky_piston",
                    "minecraft:stone_button",
                    "minecraft:stone_pressure_plate"
            );

    private FirstDiscoveryGameTests() {
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void eldritchObeliskRestoresNearbyGuardian(
            GameTestHelper helper
    ) {
        helper.getLevel().getServer().setDifficulty(
                Difficulty.NORMAL,
                true
        );
        BlockPos obelisk = new BlockPos(2, 2, 2);
        helper.setBlock(
                obelisk,
                ModBlocks.ELDRITCH_ALTAR_PART.get()
                        .defaultBlockState()
                        .setValue(EldritchAltarPartBlock.PART, 1)
        );
        helper.assertTrue(
                helper.getBlockEntity(obelisk)
                        instanceof EldritchAltarPartBlockEntity,
                "Obelisk block entity was not created"
        );
        var guardian = ModEntities.ELDRITCH_GUARDIAN.get().create(
                helper.getLevel()
        );
        helper.assertTrue(guardian != null, "Guardian could not be created");
        BlockPos spawn = helper.absolutePos(new BlockPos(4, 2, 2));
        guardian.moveTo(
                spawn.getX() + 0.5D,
                spawn.getY(),
                spawn.getZ() + 0.5D
        );
        guardian.setNoGravity(true);
        guardian.setPersistenceRequired();
        guardian.setHealth(20.0F);
        helper.getLevel().addFreshEntity(guardian);
        BlockPos absoluteObelisk = helper.absolutePos(obelisk);
        helper.assertTrue(
                guardian.kind() == LegacyMobKind.ELDRITCH_GUARDIAN,
                "Spawned test entity was not an Eldritch Guardian"
        );
        helper.assertTrue(
                guardian.distanceToSqr(
                        absoluteObelisk.getX() + 0.5D,
                        absoluteObelisk.getY(),
                        absoluteObelisk.getZ() + 0.5D
                ) <= 36.0D,
                "Guardian was outside the classic six-block obelisk radius"
        );

        helper.succeedWhen(() -> {
            helper.assertTrue(
                    guardian.hasEffect(MobEffects.DAMAGE_RESISTANCE),
                    "Obelisk did not apply classic Resistance I"
            );
            helper.assertTrue(
                    guardian.getHealth() > 20.0F,
                    "Obelisk particles/effects did not restore health"
            );
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void greatwoodUsesTheFullClassicTwoPassHeight(
            GameTestHelper helper
    ) {
        BlockPos origin = helper.absolutePos(new BlockPos(16, 2, 16));
        for (int x = -16; x <= 16; x++) {
            for (int y = 0; y <= 40; y++) {
                for (int z = -16; z <= 16; z++) {
                    helper.getLevel().setBlock(
                            origin.offset(x, y, z),
                            Blocks.AIR.defaultBlockState(),
                            2
                    );
                }
            }
        }
        for (int x = -2; x <= 3; x++) {
            for (int z = -2; z <= 3; z++) {
                helper.getLevel().setBlock(
                        origin.offset(x, -1, z),
                        Blocks.DIRT.defaultBlockState(),
                        3
                );
                helper.getLevel().setBlock(
                        origin.offset(x, -2, z),
                        Blocks.STONE.defaultBlockState(),
                        3
                );
            }
        }
        helper.assertTrue(
                GreatwoodTreeFeature.placeTree(
                        helper.getLevel(),
                        origin,
                        RandomSource.create(0x4752454154574F4FL),
                        true
                ),
                "Classic Greatwood generator rejected an empty 2x2 dirt site"
        );

        int maximumY = origin.getY();
        int logCount = 0;
        for (int x = -16; x <= 16; x++) {
            for (int y = 0; y <= 40; y++) {
                for (int z = -16; z <= 16; z++) {
                    BlockState state = helper.getLevel().getBlockState(
                            origin.offset(x, y, z)
                    );
                    if (state.is(ModBlocks.GREATWOOD_LOG.get())) {
                        logCount++;
                        maximumY = Math.max(maximumY, origin.getY() + y);
                    } else if (state.is(ModBlocks.GREATWOOD_LEAVES.get())) {
                        maximumY = Math.max(maximumY, origin.getY() + y);
                    }
                }
            }
        }
        helper.assertTrue(
                maximumY >= origin.getY() + 16,
                "Greatwood canopy did not reach the TC4 two-pass minimum height"
        );
        helper.assertTrue(
                logCount >= 40,
                "Greatwood did not produce the classic 2x2 trunks and branches"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dataDefinitionsAreLoaded(GameTestHelper helper) {
        helper.assertTrue(AspectRegistryRuntime.find("aer").isPresent(), "Aer aspect was not loaded");
        helper.assertTrue(AspectRegistryRuntime.find("lux").isPresent(), "Lux aspect was not loaded");
        helper.assertTrue(
                ScanRegistry.find(ScanTargetType.BLOCK, "minecraft:redstone_block").isPresent(),
                "Redstone block scan was not loaded"
        );
        helper.assertTrue(
                ScanRegistry.find(
                        ScanTargetType.BLOCK,
                        "minecraft:grass_block"
                ).isPresent(),
                "Legacy Blocks.grass was not loaded as the modern grass block"
        );
        helper.assertTrue(
                "block.minecraft.grass_block".equals(
                        ScanService.displayKey(
                                ScanRegistry.find(
                                        ScanTargetType.BLOCK,
                                        "minecraft:grass_block"
                                ).orElseThrow(),
                                new ScanSessionManager.BlockTarget(
                                        helper.getLevel().dimension(),
                                        BlockPos.ZERO,
                                        "minecraft:grass_block"
                                )
                        )
                ),
                "Blank legacy display key did not resolve through the runtime "
                        + "block registry"
        );
        for (String target : REQUIRED_RECIPE_DERIVED_BLOCK_SCANS) {
            helper.assertTrue(
                    ScanRegistry.find(ScanTargetType.BLOCK, target).isPresent(),
                    "Recipe-derived vanilla block scan was not loaded: "
                            + target
            );
        }
        helper.assertTrue(
                ScanRegistry.find(ScanTargetType.BLOCK, "minecraft:redstone_block")
                        .orElseThrow()
                        .aspects()
                        .get(0)
                        .amount() == 2,
                "Redstone block aspect amount was not loaded"
        );
        helper.assertTrue(
                ScanRegistry.find(
                        ScanTargetType.ITEM,
                        "minecraft:copper_ingot"
                ).isPresent()
                        && ScanRegistry.find(
                                ScanTargetType.ITEM,
                                "minecraft:raw_copper"
                        ).isPresent()
                        && ScanRegistry.find(
                                ScanTargetType.BLOCK,
                                "minecraft:copper_ore"
                        ).isPresent()
                        && ScanRegistry.find(
                                ScanTargetType.BLOCK,
                                "minecraft:deepslate_copper_ore"
                        ).isPresent(),
                "Modern copper scan identities were not loaded"
        );
        helper.assertTrue(
                ScanRegistry.find(
                        ScanTargetType.BLOCK,
                        "minecraft:spruce_log"
                ).filter(definition ->
                        definition.type() == ScanTargetType.BLOCK_TAG
                                && definition.targetId().equals("minecraft:logs"))
                        .isPresent()
                        && ScanRegistry.find(
                                ScanTargetType.ITEM,
                                "minecraft:red_dye"
                        ).filter(definition ->
                                definition.type() == ScanTargetType.ITEM_TAG
                                        && definition.targetId().equals("forge:dyes"))
                        .isPresent(),
                "Modern tag-backed replacements for Ore Dictionary scans did not resolve"
        );
        helper.assertTrue(
                ScanRegistry.find(
                        ScanTargetType.BLOCK,
                        "thaumic_reborn:ancient_stone"
                ).isPresent(),
                "Ancient Stone scan was not loaded after the resource reload"
        );
        long ancientStoneScans = ScanRegistry.all().stream()
                .filter(definition ->
                        definition.type() == ScanTargetType.BLOCK
                                && definition.targetId().equals(
                                        "thaumic_reborn:ancient_stone"
                                ))
                .count();
        helper.assertTrue(
                ancientStoneScans == 1,
                "Ancient Stone scan identity was loaded more than once"
        );
        ScanRegistry.all().stream()
                .filter(definition ->
                        definition.type() == ScanTargetType.BLOCK
                                || definition.type() == ScanTargetType.ITEM
                                || definition.type() == ScanTargetType.ENTITY)
                .forEach(definition -> {
                    ResourceLocation id = new ResourceLocation(
                            definition.targetId()
                    );
                    boolean exists = switch (definition.type()) {
                        case BLOCK -> BuiltInRegistries.BLOCK.containsKey(id);
                        case ITEM -> BuiltInRegistries.ITEM.containsKey(id);
                        case ENTITY ->
                                com.thaumcraftmodern.scan.EntityScanIdentity
                                        .isRegisteredTarget(
                                                definition.targetId()
                                        );
                        default -> true;
                    };
                    helper.assertTrue(
                            exists,
                            "Active scan points to a missing 1.20.1 registry ID: "
                                    + definition.scanKey()
                    );
                });
        helper.assertTrue(ResearchRegistry.find("basics").isPresent(), "Basics research was not loaded");
        helper.assertTrue(
                !ResearchRegistry.find("cap_gold").orElseThrow().inactive()
                        && !ResearchRegistry.find("cap_copper")
                        .orElseThrow()
                        .inactive(),
                "Materialized gold or copper cap recipe is still inactive"
        );
        PlayerThaumKnowledge capKnowledge = new PlayerThaumKnowledge();
        capKnowledge.revealResearch("basicthaumaturgy");
        capKnowledge.completeResearch("basicthaumaturgy");
        capKnowledge.revealResearch("cap_gold");
        capKnowledge.revealResearch("cap_copper");
        helper.assertTrue(
                ResearchProgressService.isAvailable(
                        ResearchRegistry.find("cap_gold").orElseThrow(),
                        capKnowledge
                ) && ResearchProgressService.isAvailable(
                        ResearchRegistry.find("cap_copper").orElseThrow(),
                        capKnowledge
                ),
                "Gold or copper cap research did not become available after its parent"
        );
        helper.assertTrue(
                WandComponentRegistry.rod("wood")
                        .orElseThrow()
                        .capacityVis() == 25,
                "Classic wooden wand capacity was not loaded"
        );
        helper.assertTrue(
                WandComponentRegistry.rod("silverwood")
                        .orElseThrow()
                        .capacityVis() == 100,
                "Classic Silverwood wand capacity was not loaded"
        );
        helper.assertTrue(
                WandComponentRegistry.cap("iron")
                        .orElseThrow()
                        .costModifier() == 1.1F,
                "Classic iron cap modifier was not loaded"
        );
        helper.assertTrue(
                WandVisService.capacity(
                        ModItems.SILVERWOOD_WAND.get().getDefaultInstance()
                ) == 100,
                "Ready Silverwood wand is missing its configured capacity"
        );
        helper.assertTrue(
                WandComponentRegistry.catalog().rods().size() == 19
                        && WandComponentRegistry.catalog().caps().size() == 6,
                "The complete classic rod/cap catalog was not loaded"
        );
        helper.assertTrue(
                WandVisService.capacity(
                        ModItems.CODEX_WAND.get().getDefaultInstance()
                ) == 1000
                        && WandVisService.state(
                                ModItems.CODEX_WAND.get().getDefaultInstance()
                        ).orElseThrow().visCentivis().values().stream()
                        .allMatch(amount -> amount == 100000),
                "Codex wand was not created fully charged at 1000 vis"
        );
        helper.assertTrue(
                WandVisService.isCraftingTool(
                        ModItems.BASIC_WAND.get().getDefaultInstance()
                )
                        && WandVisService.isCraftingTool(
                                ModItems.CRAFTING_SCEPTRE.get()
                                        .getDefaultInstance()
                        )
                        && !WandVisService.isCraftingTool(
                                ModItems.GREATWOOD_STAFF.get()
                                        .getDefaultInstance()
                        ),
                "Classic Arcane Workbench wand/sceptre/staff restrictions differ"
        );
        helper.assertTrue(
                ConstructionRegistry.find(
                        ConstructionDefinition.Handler.RESEARCH_TABLE_PAIR
                ).orElseThrow().trigger().item().toString()
                        .equals("thaumic_reborn:scribing_tools"),
                "Research Table construction did not load its explicit item trigger"
        );
        helper.assertTrue(
                ConstructionRegistry.find(
                        ConstructionDefinition.Handler.INFUSION_ALTAR
                ).orElseThrow().vis().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum() == 150,
                "Infusion Altar did not load its classic six-primal vis cost"
        );
        helper.assertTrue(
                ResearchCategoryRegistry.find("basics").isPresent(),
                "Basics research category was not loaded"
        );
        helper.assertTrue(
                ResearchCategoryRegistry.find("research").isEmpty(),
                "The development-only research category leaked into production"
        );
        helper.assertTrue(
                ResearchCategoryRegistry.find("infusion_layout_test").isEmpty()
                        && ResearchRegistry.find("infusion_layout_test").isEmpty(),
                "The development-only infusion layout category leaked into production"
        );
        helper.assertTrue(
                ResearchRegistry.find("first_discovery").isPresent(),
                "First Discovery research was not loaded"
        );
        helper.assertTrue(
                ResearchRegistry.find("first_discovery")
                        .orElseThrow()
                        .categoryId()
                        .equals("basics"),
                "First Discovery was not moved into the classic Basics category"
        );
        for (String productionResearch : new String[]{
                "basics",
                "first_discovery",
                "world_structures",
                "world_inhabitants"
        }) {
            helper.assertTrue(
                    ResearchRegistry.find(productionResearch).isPresent(),
                    "Missing production research " + productionResearch
            );
        }
        helper.assertTrue(
                ResearchRegistry.find("rod_wood").orElseThrow().virtual()
                        && ResearchRegistry.find("cap_iron").orElseThrow().virtual(),
                "Classic two-argument wand component research was not virtual"
        );
        helper.assertTrue(
                !ResearchRegistry.find("arctable").orElseThrow().concealed()
                        && !ResearchRegistry.find("nodetapper1").orElseThrow().concealed()
                        && !ResearchRegistry.find("nodepreserve").orElseThrow().concealed(),
                "Classic non-concealed progression research was hidden"
        );
        helper.assertTrue(
                ResearchRegistry.find("goggles").orElseThrow().concealed()
                        && ResearchRegistry.find("nodejar").orElseThrow().concealed(),
                "Classic concealed research lost its reveal flag"
        );
        Set<String> activeResearchPositions = new HashSet<>();
        ResearchRegistry.all().stream()
                .filter(research -> !research.inactive() && !research.virtual())
                .forEach(research -> {
                    String position = research.categoryId()
                            + ":" + research.x()
                            + ":" + research.y();
                    helper.assertTrue(
                            activeResearchPositions.add(position),
                            "Active research nodes overlap at " + position
                    );
                });
        helper.assertTrue(
                List.of(
                                "test_research_root",
                                "test_research_theory",
                                "test_research_direct",
                                "test_research_method",
                                "test_research_final"
                        )
                        .stream()
                        .noneMatch(id -> ResearchRegistry.find(id).isPresent()),
                "One or more development research fixtures leaked into production"
        );
        helper.assertTrue(
                ResearchRegistry.find("aspects").isPresent()
                        && ResearchRegistry.find("basicthaumaturgy").isPresent()
                        && ResearchRegistry.find("crucible").isPresent()
                        && ResearchRegistry.find("basicartiface").isPresent()
                        && ResearchRegistry.find("hungrychest").isPresent()
                        && ResearchRegistry.find("eldritchminor").isPresent(),
                "One or more classic Thaumonomicon branches were not loaded"
        );
        helper.assertTrue(
                ResearchCategoryRegistry.find("thaumaturgy").isPresent()
                        && ResearchCategoryRegistry.find("alchemy").isPresent()
                        && ResearchCategoryRegistry.find("artifice").isPresent()
                        && ResearchCategoryRegistry.find("golemancy").isPresent()
                        && ResearchCategoryRegistry.find("eldritch").isPresent(),
                "One or more classic Thaumonomicon categories were not loaded"
        );
        helper.assertTrue(
                ScanRegistry.identityForItem(new ItemStack(Items.STONE)).type()
                        == ScanTargetType.BLOCK,
                "Dropped stone did not reuse the registered block scan"
        );
        helper.assertTrue(
                ScanRegistry.identityForItem(new ItemStack(Items.DIRT)).type()
                        == ScanTargetType.BLOCK,
                "A BlockItem did not reuse its block identity"
        );
        helper.assertTrue(
                ScanRegistry.find(ScanTargetType.BLOCK, "minecraft:water")
                        .orElseThrow()
                        .aspects()
                        .stream()
                        .anyMatch(reward -> reward.aspectId().equals("aqua")
                                && reward.amount() == 4),
                "Water did not receive its explicit TC4-derived Aqua composition"
        );
        helper.assertTrue(
                ScanRegistry.find(ScanTargetType.BLOCK, "minecraft:lava")
                        .orElseThrow()
                        .aspects()
                        .stream()
                        .anyMatch(reward -> reward.aspectId().equals("ignis")
                                && reward.amount() == 3),
                "Lava did not receive its classic explicit Ignis composition"
        );
        helper.assertTrue(
                ScanRegistry.find(ScanTargetType.ENTITY, "minecraft:pig")
                        .orElseThrow()
                        .aspects()
                        .stream()
                        .anyMatch(reward -> reward.aspectId().equals("bestia")
                                && reward.amount() == 2),
                "Pig did not receive its classic explicit Bestia composition"
        );
        helper.assertTrue(
                ScanRegistry.find(ScanTargetType.ITEM, "minecraft:feather").isPresent(),
                "The explicit classic feather scan was not loaded"
        );
        helper.assertTrue(
                ScanRegistry.find(
                        ScanTargetType.ITEM,
                        "minecraft:structure_void"
                ).isEmpty(),
                "Fidelity mode invented a scan definition for an unknown target"
        );
        helper.assertTrue(
                ScanRegistry.findHistorical(
                        ScanTargetType.ITEM,
                        "minecraft:structure_void"
                ).isPresent(),
                "Historical lookup could not interpret a previously saved inferred scan"
        );
        for (String recipeId : REQUIRED_RECIPES) {
            helper.assertTrue(
                    helper.getLevel().getRecipeManager()
                            .byKey(new ResourceLocation(
                                    ThaumcraftModern.MOD_ID,
                                    recipeId
                            ))
                            .isPresent(),
                    "Required recipe was not loaded: " + recipeId
            );
        }
        var thaumometerRecipe = helper.getLevel().getRecipeManager()
                .byKey(new ResourceLocation(
                        ThaumcraftModern.MOD_ID,
                        "thaumometer"
                ))
                .orElseThrow();
        var anyPrimalShard = thaumometerRecipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getItems().length == 6)
                .findFirst()
                .orElseThrow();
        for (var shard : List.of(
                ModItems.AIR_SHARD.get(),
                ModItems.FIRE_SHARD.get(),
                ModItems.WATER_SHARD.get(),
                ModItems.EARTH_SHARD.get(),
                ModItems.ORDER_SHARD.get(),
                ModItems.ENTROPY_SHARD.get()
        )) {
            helper.assertTrue(
                    anyPrimalShard.test(new ItemStack(shard)),
                    "Thaumometer recipe rejected a primal shard: " + shard
            );
        }
        for (var arrowVariant : Map.of(
                "primal_arrow_aer", ModItems.AIR_SHARD.get(),
                "primal_arrow_ignis", ModItems.FIRE_SHARD.get(),
                "primal_arrow_aqua", ModItems.WATER_SHARD.get(),
                "primal_arrow_terra", ModItems.EARTH_SHARD.get(),
                "primal_arrow_ordo", ModItems.ORDER_SHARD.get(),
                "primal_arrow_perditio", ModItems.ENTROPY_SHARD.get()
        ).entrySet()) {
            var arrowRecipe = helper.getLevel().getRecipeManager()
                    .byKey(new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            arrowVariant.getKey()
                    ))
                    .orElseThrow();
            helper.assertTrue(
                    arrowRecipe.getIngredients().stream().anyMatch(ingredient ->
                            ingredient.test(new ItemStack(arrowVariant.getValue()))),
                    "Primal arrow recipe lost its matching shard: "
                            + arrowVariant.getKey()
            );
        }
        helper.assertTrue(
                helper.getLevel().getRecipeManager()
                        .getAllRecipesFor(ModArcaneRecipes.ARCANE_CRAFTING_TYPE.get())
                        .size() >= 113,
                "The complete migrated TC4 arcane recipe catalog was not loaded"
        );
        for (String migratedRecipe : List.of(
                "banner_white",
                "primal_arrow_aer",
                "wand_cap_void_inert",
                "wand_rod_silverwood_staff",
                "focus_primal",
                "tube_buffer",
                "mnemonic_matrix"
        )) {
            helper.assertTrue(
                    helper.getLevel().getRecipeManager()
                            .byKey(new ResourceLocation(
                                    ThaumcraftModern.MOD_ID,
                                    migratedRecipe
                            ))
                            .isPresent(),
                    "Migrated arcane recipe was not loaded: " + migratedRecipe
            );
        }
        helper.assertTrue(
                helper.getLevel().getRecipeManager()
                        .byKey(new ResourceLocation(
                                ThaumcraftModern.MOD_ID,
                                "silverwood_wand"
                        ))
                        .isEmpty(),
                "Silverwood wand recipe was added before its permitted vertical"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void researchTableAndPlayerCapabilityAreReady(GameTestHelper helper) {
        helper.assertTrue(
                KnowledgeCapabilities.PLAYER.isRegistered(),
                "Player thaumaturgy capability type was not registered"
        );
        helper.setBlock(TABLE_POSITION, ModBlocks.RESEARCH_TABLE.get());
        helper.assertTrue(
                helper.getBlockEntity(TABLE_POSITION) instanceof ResearchTableBlockEntity,
                "Research Table block entity was not created"
        );

        ResearchTableBlockEntity table =
                (ResearchTableBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        ItemStack tools = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        ItemStack notes = new ItemStack(ModItems.RESEARCH_NOTES.get());
        ResearchNotesItem.ensureInitialized(notes);
        helper.assertTrue(
                table.items().insertItem(ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT, tools, false).isEmpty(),
                "Scribing Tools were rejected by slot 0"
        );
        helper.assertTrue(
                table.items().insertItem(ResearchTableBlockEntity.NOTES_SLOT, notes, false).isEmpty(),
                "Research Notes were rejected by slot 1"
        );
        helper.assertTrue(
                table.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(),
                "Research Table item capability was not exposed"
        );
        table.invalidateCaps();
        table.reviveCaps();
        helper.assertTrue(
                table.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent(),
                "Research Table item capability did not revive"
        );

        Player player = helper.makeMockSurvivalPlayer();
        helper.assertTrue(KnowledgeAccess.get(player).isPresent(), "Player knowledge capability was not attached");
        helper.assertTrue(
                KnowledgeAccess.get(player).orElseThrow().knowsAspect("aer"),
                "New player did not receive primal aspect knowledge"
        );
        helper.assertTrue(
                KnowledgeAccess.get(player).orElseThrow().aspectAmount("aer") == 5,
                "New player did not receive the first-vertical primal aspect pool"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void scribingToolsCreateTwoBlockResearchTableWithoutWand(
            GameTestHelper helper
    ) {
        BlockPos companionPosition = TABLE_POSITION.east();
        helper.setBlock(TABLE_POSITION, ModBlocks.THAUMCRAFT_TABLE.get());
        helper.setBlock(
                companionPosition,
                ModBlocks.THAUMCRAFT_TABLE.get()
        );
        ServerPlayer player = fakePlayer(helper, "research-table-construction");
        ItemStack tools = new ItemStack(ModItems.SCRIBING_TOOLS.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, tools);

        InteractionResult result = useOn(helper, player, TABLE_POSITION);
        helper.assertTrue(
                result.consumesAction(),
                "Scribing Tools did not assemble two adjacent tables"
        );
        BlockState main = helper.getBlockState(TABLE_POSITION);
        BlockState companion = helper.getBlockState(companionPosition);
        helper.assertTrue(
                main.is(ModBlocks.RESEARCH_TABLE.get())
                        && main.getValue(ResearchTableBlock.PART)
                        == ResearchTablePart.MAIN
                        && main.getValue(ResearchTableBlock.FACING)
                        == Direction.EAST,
                "Clicked table did not become the main Research Table half"
        );
        helper.assertTrue(
                companion.is(ModBlocks.RESEARCH_TABLE.get())
                        && companion.getValue(ResearchTableBlock.PART)
                        == ResearchTablePart.COMPANION,
                "Adjacent table did not become the companion Research Table half"
        );
        helper.assertTrue(
                helper.getBlockEntity(TABLE_POSITION)
                        instanceof ResearchTableBlockEntity,
                "Main Research Table half has no block entity"
        );
        ResearchTableBlockEntity table =
                (ResearchTableBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        helper.assertTrue(
                table.items().getStackInSlot(
                        ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT
                ).is(ModItems.SCRIBING_TOOLS.get()),
                "Scribing Tools were not installed into the assembled table"
        );
        helper.assertTrue(
                player.getMainHandItem().isEmpty(),
                "Survival construction did not move the Scribing Tools"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wandConvertsCauldronToCrucibleWithoutVis(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, Blocks.CAULDRON);
        ServerPlayer player = fakePlayer(helper, "crucible-construction");
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        CompoundTag before = wand.getTag().copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);

        ClassicStructureConstructionEvents.ConstructionResult result =
                ClassicStructureConstructionEvents.tryConstruct(
                        helper.getLevel(),
                        player,
                        wand,
                        helper.absolutePos(TABLE_POSITION),
                        Direction.UP
                );
        helper.assertTrue(
                result == ClassicStructureConstructionEvents
                        .ConstructionResult.CONSTRUCTED,
                "Wand did not convert a cauldron into a Crucible"
        );
        helper.assertBlockPresent(ModBlocks.CRUCIBLE.get(), TABLE_POSITION);
        helper.assertTrue(
                before.equals(wand.getTag()),
                "Crucible construction consumed vis"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crucibleSupportsCauldronBottlesAndBucketTopUp(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, ModBlocks.CRUCIBLE.get());
        ServerPlayer player = fakePlayer(helper, "crucible-water");
        CrucibleBlockEntity crucible = (CrucibleBlockEntity)
                helper.getBlockEntity(TABLE_POSITION);
        helper.assertTrue(crucible != null, "Crucible block entity missing");
        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                PotionUtils.setPotion(
                        new ItemStack(Items.POTION),
                        Potions.WATER
                )
        );
        BlockPos absolute = helper.absolutePos(TABLE_POSITION);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolute),
                Direction.UP,
                absolute,
                false
        );

        InteractionResult result = ModBlocks.CRUCIBLE.get().use(
                helper.getBlockState(TABLE_POSITION),
                helper.getLevel(),
                absolute,
                player,
                InteractionHand.MAIN_HAND,
                hit
        );

        helper.assertTrue(
                result.consumesAction(),
                "Crucible rejected a water bottle"
        );
        helper.assertTrue(
                crucible.water() == 334,
                "One bottle did not add one cauldron portion; water="
                        + crucible.water()
        );
        helper.assertTrue(
                player.getMainHandItem().is(Items.GLASS_BOTTLE),
                "Filling the Crucible did not return an empty bottle"
        );

        CompoundTag partial = crucible.saveWithFullMetadata();
        partial.putInt("Water", 450);
        crucible.load(partial);
        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(Items.WATER_BUCKET)
        );
        ModBlocks.CRUCIBLE.get().use(
                helper.getBlockState(TABLE_POSITION),
                helper.getLevel(),
                absolute,
                player,
                InteractionHand.MAIN_HAND,
                hit
        );
        helper.assertTrue(
                crucible.water() == CrucibleBlockEntity.FLUID_CAPACITY_MB,
                "Water bucket did not top a partial Crucible up to full"
        );
        helper.assertTrue(
                player.getMainHandItem().is(Items.BUCKET),
                "Topping up the Crucible did not return the empty bucket"
        );

        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                new ItemStack(Items.WATER_BUCKET)
        );
        ModBlocks.CRUCIBLE.get().use(
                helper.getBlockState(TABLE_POSITION),
                helper.getLevel(),
                absolute,
                player,
                InteractionHand.MAIN_HAND,
                hit
        );
        helper.assertTrue(
                player.getMainHandItem().is(Items.WATER_BUCKET),
                "A full Crucible consumed a second water bucket"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void destroyingCruciblesRemovesEmptyAndFilledInstances(
            GameTestHelper helper
    ) {
        BlockPos emptyPosition = new BlockPos(1, 1, 1);
        BlockPos filledPosition = new BlockPos(3, 1, 1);
        helper.setBlock(emptyPosition, ModBlocks.CRUCIBLE.get());
        helper.setBlock(filledPosition, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity filled =
                (CrucibleBlockEntity) helper.getBlockEntity(filledPosition);
        helper.assertTrue(
                filled != null && filled.fillWater(),
                "Filled Crucible test fixture could not accept water"
        );

        helper.getLevel().destroyBlock(
                helper.absolutePos(emptyPosition),
                false
        );
        helper.getLevel().destroyBlock(
                helper.absolutePos(filledPosition),
                false
        );

        helper.assertBlockPresent(Blocks.AIR, emptyPosition);
        helper.assertBlockPresent(Blocks.AIR, filledPosition);
        helper.assertTrue(
                helper.getBlockEntity(emptyPosition) == null,
                "Destroyed empty Crucible left a block entity instance"
        );
        helper.assertTrue(
                helper.getBlockEntity(filledPosition) == null,
                "Destroyed filled Crucible left a block entity instance"
        );
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 230)
    public static void crucibleCraftsAtomicallyAndSurvivesReload(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION.below(), Blocks.FIRE);
        helper.setBlock(TABLE_POSITION, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible =
                (CrucibleBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        helper.assertTrue(crucible != null, "Crucible block entity missing");
        helper.assertTrue(crucible.fillWater(), "Crucible did not accept water");

        ServerPlayer player = fakePlayer(helper, "crucible-alchemy");
        KnowledgeAccess.get(player).orElseThrow()
                .completeResearch("crucible");
        helper.runAfterDelay(160, () -> {
            helper.assertTrue(
                    crucible.heat() > CrucibleBlockEntity.BOILING_HEAT,
                    "TC4 heat threshold was not reached"
            );
            for (ItemStack ingredient : List.of(
                    new ItemStack(ModItems.FIRE_SHARD.get()),
                    new ItemStack(ModItems.WATER_SHARD.get()),
                    new ItemStack(ModItems.EARTH_SHARD.get()),
                    new ItemStack(ModItems.ORDER_SHARD.get()),
                    new ItemStack(ModItems.ENTROPY_SHARD.get())
            )) {
                ItemEntity entity = new ItemEntity(
                        helper.getLevel(),
                        helper.absolutePos(TABLE_POSITION).getX() + 0.5D,
                        helper.absolutePos(TABLE_POSITION).getY() + 0.75D,
                        helper.absolutePos(TABLE_POSITION).getZ() + 0.5D,
                        ingredient
                );
                entity.setThrower(player.getUUID());
                entity.setNeverPickUp();
                helper.getLevel().addFreshEntity(entity);
            }
        });

        helper.runAfterDelay(165, () -> {
            helper.assertTrue(
                    crucible.essentiaAmount() == 20,
                    "Explicit shard aspects were not dissolved; essentia="
                            + crucible.essentia()
            );
            helper.assertTrue(
                    KnowledgeAccess.get(player).orElseThrow()
                            .hasCompletedResearch("crucible"),
                    "Crucible research was not retained by catalyst owner"
            );
            ItemEntity catalyst = new ItemEntity(
                    helper.getLevel(),
                    helper.absolutePos(TABLE_POSITION).getX() + 0.5D,
                    helper.absolutePos(TABLE_POSITION).getY() + 0.75D,
                    helper.absolutePos(TABLE_POSITION).getZ() + 0.5D,
                    new ItemStack(ModItems.AIR_SHARD.get())
            );
            catalyst.setThrower(player.getUUID());
            catalyst.setNeverPickUp();
            CrucibleItemTossEvents.attachResearch(player, catalyst);
            helper.getLevel().addFreshEntity(catalyst);
            helper.assertTrue(
                    CrucibleItemTossEvents.hasResearch(
                            catalyst,
                            "crucible"
                    ),
                    "Catalyst did not retain server-authoritative research"
            );
        });

        helper.runAfterDelay(175, () -> {
            helper.assertTrue(
                    crucible.water() == 950,
                    "Crucible recipe did not consume exactly 50 mB; "
                            + "water=" + crucible.water()
            );
            helper.assertTrue(
                    crucible.essentiaAmount() == 10,
                    "Catalyst transaction consumed non-required essentia; "
                            + "essentia=" + crucible.essentia()
            );
            helper.assertTrue(
                    helper.getEntities(
                                    EntityType.ITEM,
                                    TABLE_POSITION,
                                    3.0D
                            ).stream()
                            .anyMatch(entity -> entity.getItem().is(
                                    ModItems.BALANCED_SHARD.get()
                            )),
                    "Balanced Shard was not ejected"
            );

            CompoundTag saved = crucible.saveWithFullMetadata();
            short savedHeat = crucible.heat();
            Map<String, Integer> savedEssentia =
                    Map.copyOf(crucible.essentia());
            helper.setBlock(TABLE_POSITION, Blocks.AIR);
            helper.setBlock(
                    TABLE_POSITION,
                    ModBlocks.CRUCIBLE.get().defaultBlockState()
                            .setValue(CrucibleBlock.FILLED, true)
            );
            CrucibleBlockEntity loaded =
                    (CrucibleBlockEntity) helper.getBlockEntity(TABLE_POSITION);
            helper.assertTrue(loaded != null, "Reloaded Crucible missing");
            loaded.load(saved);
            helper.assertTrue(
                    loaded.water() == 950
                            && loaded.heat() == savedHeat
                            && loaded.essentia().equals(savedEssentia),
                    "Crucible water/heat/essentia did not survive NBT reload"
            );

            player.setShiftKeyDown(true);
            player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    ModItems.BASIC_WAND.get().getDefaultInstance()
            );
            InteractionResult cleanup = useOn(
                    helper,
                    player,
                    TABLE_POSITION
            );
            helper.assertTrue(
                    cleanup.consumesAction()
                            && loaded.water() == 0
                            && loaded.essentiaAmount() == 0
                            && !helper.getBlockState(TABLE_POSITION)
                            .getValue(CrucibleBlock.FILLED),
                    "Shift-wand cleanup did not spill all Crucible remnants"
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 180)
    public static void crucibleDissolvesEveryExplicitlyAspectedItem(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION.below(), Blocks.FIRE);
        helper.setBlock(TABLE_POSITION, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible =
                (CrucibleBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        helper.assertTrue(crucible != null, "Crucible block entity missing");
        helper.assertTrue(crucible.fillWater(), "Crucible did not accept water");

        helper.runAfterDelay(160, () -> {
            helper.assertTrue(
                    crucible.heat() > CrucibleBlockEntity.BOILING_HEAT,
                    "TC4 heat threshold was not reached"
            );
            ItemEntity feather = new ItemEntity(
                    helper.getLevel(),
                    helper.absolutePos(TABLE_POSITION).getX() + 0.5D,
                    helper.absolutePos(TABLE_POSITION).getY() + 0.75D,
                    helper.absolutePos(TABLE_POSITION).getZ() + 0.5D,
                    new ItemStack(Items.FEATHER)
            );
            feather.setNeverPickUp();
            helper.getLevel().addFreshEntity(feather);
        });

        helper.runAfterDelay(165, () -> {
            helper.assertTrue(
                    crucible.essentiaAmount() == 3
                            && crucible.essentia()
                            .getOrDefault("volatus", 0) == 2
                            && crucible.essentia()
                            .getOrDefault("aer", 0) == 1,
                    "Explicit feather aspects were not dissolved; essentia="
                            + crucible.essentia()
            );
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void crucibleOverflowAndDegradationMatchTc4AndReload(
            GameTestHelper helper
    ) {
        BlockPos overflowPosition = TABLE_POSITION;
        BlockPos degradationPosition = TABLE_POSITION.east();
        helper.setBlock(overflowPosition, ModBlocks.CRUCIBLE.get());
        helper.setBlock(degradationPosition.below(), Blocks.FIRE);
        helper.setBlock(degradationPosition, ModBlocks.CRUCIBLE.get());

        CrucibleBlockEntity overflow =
                (CrucibleBlockEntity) helper.getBlockEntity(
                        overflowPosition
                );
        CrucibleBlockEntity degradation =
                (CrucibleBlockEntity) helper.getBlockEntity(
                        degradationPosition
                );
        helper.assertTrue(
                overflow != null && degradation != null,
                "Crucible fixtures were not created"
        );
        seedCrucible(overflow, 1000, (short) 0, "ignis", 101);
        seedCrucible(
                degradation,
                1000,
                (short) CrucibleBlockEntity.MAX_HEAT,
                "potentia",
                1
        );

        for (int tick = 0; tick < 5; tick++) {
            CrucibleBlockEntity.serverTick(
                    helper.getLevel(),
                    overflow.getBlockPos(),
                    overflow.getBlockState(),
                    overflow
            );
        }
        helper.assertTrue(
                overflow.essentiaAmount() == 100,
                "Overflow did not remove exactly one essentia in five ticks; "
                        + "essentia=" + overflow.essentiaAmount()
        );

        for (int tick = 0; tick < 201; tick++) {
            CrucibleBlockEntity.serverTick(
                    helper.getLevel(),
                    degradation.getBlockPos(),
                    degradation.getBlockState(),
                    degradation
            );
        }
        helper.assertTrue(
                degradation.water() == 998,
                "Degradation did not consume the original 2 mB; water="
                        + degradation.water()
        );
        helper.assertTrue(
                degradation.essentiaAmount() == 1
                        && degradation.essentia()
                        .getOrDefault("potentia", 0) == 0
                        && (degradation.essentia()
                        .getOrDefault("ordo", 0) == 1
                        || degradation.essentia()
                        .getOrDefault("ignis", 0) == 1),
                "Potentia did not become one direct TC4 component; essentia="
                        + degradation.essentia()
        );

        CompoundTag overflowSaved = overflow.saveWithFullMetadata();
        CompoundTag degradationSaved =
                degradation.saveWithFullMetadata();
        Map<String, Integer> degradationEssentia =
                Map.copyOf(degradation.essentia());
        helper.setBlock(overflowPosition, Blocks.AIR);
        helper.setBlock(degradationPosition, Blocks.AIR);
        helper.setBlock(overflowPosition, ModBlocks.CRUCIBLE.get());
        helper.setBlock(degradationPosition, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity overflowReloaded =
                (CrucibleBlockEntity) helper.getBlockEntity(
                        overflowPosition
                );
        CrucibleBlockEntity degradationReloaded =
                (CrucibleBlockEntity) helper.getBlockEntity(
                        degradationPosition
                );
        helper.assertTrue(
                overflowReloaded != null && degradationReloaded != null,
                "Reloaded Crucible fixtures were not created"
        );
        overflowReloaded.load(overflowSaved);
        degradationReloaded.load(degradationSaved);
        helper.assertTrue(
                overflowReloaded.essentiaAmount() == 100
                        && degradationReloaded.water() == 998
                        && degradationReloaded.essentia()
                        .equals(degradationEssentia),
                "Overflow/degradation state did not survive NBT reload"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allCastingToolFormsCleanCrucibleWhileSneaking(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, ModBlocks.CRUCIBLE.get());
        CrucibleBlockEntity crucible =
                (CrucibleBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        helper.assertTrue(crucible != null, "Crucible block entity missing");
        ServerPlayer player = fakePlayer(
                helper,
                "crucible-shift-wand-forms"
        );
        player.setShiftKeyDown(true);

        for (ItemStack castingTool : List.of(
                ModItems.BASIC_WAND.get().getDefaultInstance(),
                ModItems.CRAFTING_SCEPTRE.get().getDefaultInstance(),
                ModItems.GREATWOOD_STAFF.get().getDefaultInstance()
        )) {
            helper.assertTrue(
                    crucible.fillWater(),
                    "Crucible could not be refilled for casting-tool test"
            );
            player.setItemInHand(
                    InteractionHand.MAIN_HAND,
                    castingTool
            );
            InteractionResult result = useOn(
                    helper,
                    player,
                    TABLE_POSITION
            );
            helper.assertTrue(
                    result.consumesAction()
                            && crucible.water() == 0
                            && !helper.getBlockState(TABLE_POSITION)
                            .getValue(CrucibleBlock.FILLED),
                    "Sneaking casting tool did not clean the Crucible: "
                            + castingTool.getItem()
            );
        }
        helper.succeed();
    }

    private static void seedCrucible(
            CrucibleBlockEntity crucible,
            int water,
            short heat,
            String aspect,
            int amount
    ) {
        CompoundTag seed = crucible.saveWithFullMetadata();
        EssentiaStore essentia = new EssentiaStore();
        essentia.add(aspect, amount);
        seed.putInt("Water", water);
        seed.putShort("Heat", heat);
        seed.put("Aspects", essentia.save());
        crucible.load(seed);
    }

    @GameTest(template = "empty")
    public static void sneakingWandStartsAuraNodeDrain(
            GameTestHelper helper
    ) {
        BlockPos nodePosition = new BlockPos(2, 2, 2);
        helper.setBlock(nodePosition, ModBlocks.AURA_NODE.get());
        helper.assertTrue(
                helper.getBlockEntity(nodePosition)
                        instanceof AuraNodeBlockEntity,
                "Aura node block entity missing"
        );
        ServerPlayer player = fakePlayer(
                helper,
                "shift-wand-node-drain"
        );
        player.setShiftKeyDown(true);
        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                ModItems.BASIC_WAND.get().getDefaultInstance()
        );

        InteractionResult result = useOn(
                helper,
                player,
                nodePosition
        );
        helper.assertTrue(
                result.consumesAction() && player.isUsingItem(),
                "Shift-wand interaction did not start aura-node draining"
        );
        player.stopUsingItem();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void nitorIsAClassicCrucibleHeatSource(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, ModBlocks.NITOR.get());
        helper.assertTrue(
                CrucibleBlockEntity.isHeatSource(
                        helper.getLevel(),
                        helper.absolutePos(TABLE_POSITION)
                ),
                "Placed Nitor was not recognized as a Crucible heat source"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wandFormsClassicInfusionAltarAndConsumesExactVis(
            GameTestHelper helper
    ) {
        BlockPos anchor = new BlockPos(1, 1, 1);
        for (int x : new int[]{0, 2}) {
            for (int z : new int[]{0, 2}) {
                helper.setBlock(
                        anchor.offset(x, 0, z),
                        ModBlocks.ARCANE_STONE_BRICK.get()
                );
                helper.setBlock(
                        anchor.offset(x, 1, z),
                        ModBlocks.ARCANE_STONE.get()
                );
            }
        }
        helper.setBlock(anchor.offset(1, 0, 1), ModBlocks.ARCANE_PEDESTAL.get());
        helper.setBlock(anchor.offset(1, 2, 1), ModBlocks.RUNIC_MATRIX.get());

        ServerPlayer player = fakePlayer(helper, "infusion-construction");
        KnowledgeAccess.get(player).orElseThrow().completeResearch("infusion");
        ItemStack wand = ModItems.SILVERWOOD_WAND.get().getDefaultInstance();
        for (String aspect : List.of(
                "aer", "terra", "ignis", "aqua", "ordo", "perditio"
        )) {
            WandVisService.add(player, wand, aspect, 30);
        }

        ClassicStructureConstructionEvents.ConstructionResult result =
                ClassicStructureConstructionEvents.tryConstruct(
                        helper.getLevel(),
                        player,
                        wand,
                        helper.absolutePos(anchor.offset(1, 2, 1)),
                        Direction.UP
                );
        helper.assertTrue(
                result == ClassicStructureConstructionEvents
                        .ConstructionResult.CONSTRUCTED,
                "Complete classic Infusion Altar was not formed"
        );
        BlockState matrix = helper.getBlockState(anchor.offset(1, 2, 1));
        helper.assertTrue(
                matrix.is(ModBlocks.RUNIC_MATRIX.get())
                        && matrix.getValue(RunicMatrixBlock.ACTIVE),
                "Runic Matrix was not activated"
        );
        for (String aspect : List.of(
                "aer", "terra", "ignis", "aqua", "ordo", "perditio"
        )) {
            helper.assertTrue(
                    WandVisService.visCentivis(wand, aspect) == 250,
                    "Infusion Altar did not consume adjusted 27.5 "
                            + aspect + " vis"
            );
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 220)
    public static void wandFormsClassicInfernalFurnace(
            GameTestHelper helper
    ) {
        BlockPos anchor = new BlockPos(1, 1, 1);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int z = 0; z < 3; z++) {
                    BlockPos position = anchor.offset(x, y, z);
                    boolean corner = x != 1 && z != 1;
                    if (y == 2 && x == 1 && z == 1) {
                        continue;
                    }
                    if (y == 1 && x == 1 && z == 1) {
                        helper.setBlock(position, Blocks.LAVA);
                    } else if (y == 1 && x == 1 && z == 0) {
                        helper.setBlock(position, Blocks.IRON_BARS);
                    } else {
                        helper.setBlock(
                                position,
                                corner ? Blocks.NETHER_BRICKS : Blocks.OBSIDIAN
                        );
                    }
                }
            }
        }
        ServerPlayer player = fakePlayer(helper, "infernal-construction");
        KnowledgeAccess.get(player)
                .orElseThrow()
                .completeResearch("infernalfurnace");
        ItemStack wand = ModItems.SILVERWOOD_WAND.get().getDefaultInstance();
        WandVisService.add(player, wand, "ignis", 60);
        WandVisService.add(player, wand, "terra", 60);

        ClassicStructureConstructionEvents.ConstructionResult result =
                ClassicStructureConstructionEvents.tryConstruct(
                        helper.getLevel(),
                        player,
                        wand,
                        helper.absolutePos(anchor),
                        Direction.UP
                );
        helper.assertTrue(
                result == ClassicStructureConstructionEvents
                        .ConstructionResult.CONSTRUCTED,
                "Complete classic Infernal Furnace was not formed"
        );
        BlockState core = helper.getBlockState(anchor.offset(1, 1, 1));
        BlockState grate = helper.getBlockState(anchor.offset(1, 1, 0));
        helper.assertTrue(
                core.is(ModBlocks.INFERNAL_FURNACE.get())
                        && core.getValue(InfernalFurnaceBlock.PART) == 0,
                "Infernal Furnace lava core did not become metadata part 0"
        );
        helper.assertTrue(
                grate.is(ModBlocks.INFERNAL_FURNACE.get())
                        && grate.getValue(InfernalFurnaceBlock.PART) == 10,
                "Infernal Furnace iron bars did not become grate part 10"
        );
        helper.assertTrue(
                WandVisService.visCentivis(wand, "ignis") == 500
                        && WandVisService.visCentivis(wand, "terra") == 500,
                "Infernal Furnace did not consume adjusted 55 Ignis/Terra vis"
        );
        BlockPos absoluteCore = helper.absolutePos(anchor.offset(1, 1, 1));
        ItemEntity rawIron = new ItemEntity(
                helper.getLevel(),
                absoluteCore.getX() + 0.5D,
                absoluteCore.getY() + 1.3D,
                absoluteCore.getZ() + 0.5D,
                new ItemStack(Items.RAW_IRON)
        );
        rawIron.setNeverPickUp();
        helper.getLevel().addFreshEntity(rawIron);
        helper.runAfterDelay(155, () -> {
            boolean ejected = helper.getEntities(
                            EntityType.ITEM,
                            anchor.offset(1, 1, 0),
                            3.0D
                    ).stream()
                    .map(ItemEntity::getItem)
                    .anyMatch(stack -> stack.is(Items.IRON_INGOT));
            helper.assertTrue(ejected,
                    "Infernal Furnace did not absorb, smelt and eject raw iron");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void wandFormsAdvancedAlchemicalFurnace(
            GameTestHelper helper
    ) {
        BlockPos center = new BlockPos(2, 1, 2);
        for (int y = 0; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos position = center.offset(x, y, z);
                    boolean corner = x != 0 && z != 0;
                    if (y == 0 && x == 0 && z == 0) {
                        helper.setBlock(
                                position,
                                ModBlocks.ALCHEMICAL_FURNACE.get()
                        );
                    } else if (y == 0) {
                        helper.setBlock(
                                position,
                                ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()
                        );
                    } else if (x == 0 && z == 0) {
                        continue;
                    } else {
                        helper.setBlock(
                                position,
                                corner
                                        ? ModBlocks.ARCANE_ALEMBIC.get()
                                        : ModBlocks.ALCHEMICAL_CONSTRUCT.get()
                        );
                    }
                }
            }
        }
        ServerPlayer player = fakePlayer(helper, "advanced-furnace");
        KnowledgeAccess.get(player)
                .orElseThrow()
                .completeResearch("advalchemyfurnace");
        ItemStack wand = ModItems.SILVERWOOD_WAND.get().getDefaultInstance();
        for (String aspect : List.of("ignis", "aqua", "ordo")) {
            WandVisService.add(player, wand, aspect, 60);
        }

        ClassicStructureConstructionEvents.ConstructionResult result =
                ClassicStructureConstructionEvents.tryConstruct(
                        helper.getLevel(),
                        player,
                        wand,
                        helper.absolutePos(center),
                        Direction.UP
                );
        helper.assertTrue(
                result == ClassicStructureConstructionEvents
                        .ConstructionResult.CONSTRUCTED,
                "Complete Advanced Alchemical Furnace was not formed"
        );
        helper.assertTrue(
                helper.getBlockState(center)
                        .is(ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())
                        && helper.getBlockState(center)
                        .getValue(ClassicPartBlock.PART) == 0
                        && helper.getBlockState(center.offset(1, 1, 1))
                        .is(ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())
                        && helper.getBlockState(center.offset(1, 1, 1))
                        .getValue(ClassicPartBlock.PART) == 2
                        && helper.getBlockState(center.offset(1, 1, 0))
                        .is(ModBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())
                        && helper.getBlockState(center.offset(1, 1, 0))
                        .getValue(ClassicPartBlock.PART) == 3,
                "Advanced furnace source blocks received wrong classic parts"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wandFormsThreeBlockThaumatorium(
            GameTestHelper helper
    ) {
        BlockPos furnace = new BlockPos(2, 1, 2);
        helper.setBlock(furnace, ModBlocks.ALCHEMICAL_FURNACE.get());
        helper.setBlock(furnace.above(), ModBlocks.ALCHEMICAL_CONSTRUCT.get());
        helper.setBlock(
                furnace.above(2),
                ModBlocks.ALCHEMICAL_CONSTRUCT.get()
        );
        ServerPlayer player = fakePlayer(helper, "thaumatorium");
        KnowledgeAccess.get(player)
                .orElseThrow()
                .completeResearch("thaumatorium");
        ItemStack wand = ModItems.SILVERWOOD_WAND.get().getDefaultInstance();
        WandVisService.add(player, wand, "ignis", 20);
        WandVisService.add(player, wand, "aqua", 40);
        WandVisService.add(player, wand, "ordo", 40);

        ClassicStructureConstructionEvents.ConstructionResult result =
                ClassicStructureConstructionEvents.tryConstruct(
                        helper.getLevel(),
                        player,
                        wand,
                        helper.absolutePos(furnace.above()),
                        Direction.NORTH
                );
        helper.assertTrue(
                result == ClassicStructureConstructionEvents
                        .ConstructionResult.CONSTRUCTED,
                "Furnace plus two constructs did not form a Thaumatorium"
        );
        helper.assertBlockPresent(ModBlocks.ALCHEMICAL_FURNACE.get(), furnace);
        BlockState lower = helper.getBlockState(furnace.above());
        BlockState upper = helper.getBlockState(furnace.above(2));
        helper.assertTrue(
                lower.is(ModBlocks.THAUMATORIUM.get())
                        && lower.getValue(ThaumatoriumBlock.HALF)
                        == DoubleBlockHalf.LOWER
                        && upper.is(ModBlocks.THAUMATORIUM.get())
                        && upper.getValue(ThaumatoriumBlock.HALF)
                        == DoubleBlockHalf.UPPER,
                "Thaumatorium lower/upper parts were not assigned"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thaumatoriumFoundationOpensControllerMenu(
            GameTestHelper helper
    ) {
        BlockPos furnacePos = new BlockPos(2, 1, 2);
        BlockPos controllerPos = furnacePos.above();
        helper.setBlock(furnacePos, ModBlocks.ALCHEMICAL_FURNACE.get());
        helper.setBlock(controllerPos,
                ModBlocks.THAUMATORIUM.get().defaultBlockState()
                        .setValue(ThaumatoriumBlock.HALF,
                                DoubleBlockHalf.LOWER));
        ServerPlayer player = fakePlayer(helper, "thaumatorium-foundation-menu");
        BlockPos absoluteFurnace = helper.absolutePos(furnacePos);
        InteractionResult result = ModBlocks.ALCHEMICAL_FURNACE.get().use(
                helper.getBlockState(furnacePos),
                helper.getLevel(),
                absoluteFurnace,
                player,
                InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absoluteFurnace),
                        Direction.UP, absoluteFurnace, false)
        );
        helper.assertTrue(result.consumesAction()
                        && player.containerMenu
                        instanceof com.thaumcraftmodern.world.menu.ThaumatoriumMenu,
                "Thaumatorium foundation opened the standalone furnace menu");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void discoveryCompletionRejectsForgedPayloads(GameTestHelper helper) {
        Player player = helper.makeMockSurvivalPlayer();
        PlayerThaumKnowledge knowledge = KnowledgeAccess.get(player).orElseThrow();
        knowledge.revealResearch("first_discovery");
        ResearchCompletionService.markDiscoveryReady(knowledge, "first_discovery");

        helper.assertTrue(
                ResearchCompletionService.complete(
                        knowledge,
                        new ItemStack(Items.PAPER)
                ) == ResearchCompletionService.Result.INVALID_DISCOVERY,
                "A non-Discovery item was accepted as completed research"
        );

        ItemStack mismatched = DiscoveryItem.create("first_discovery");
        mismatched.getOrCreateTag().putString(
                "ValidatedResearch",
                "different_research"
        );
        helper.assertTrue(
                ResearchCompletionService.complete(knowledge, mismatched)
                        == ResearchCompletionService.Result.INVALID_DISCOVERY,
                "A mismatched Discovery payload was accepted"
        );

        helper.assertTrue(
                ResearchCompletionService.complete(
                        knowledge,
                        DiscoveryItem.create("first_discovery")
                ) == ResearchCompletionService.Result.COMPLETED,
                "A valid server-authorized Discovery did not complete research"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void completedResearchCopiesWithEscalatingOriginalCost(
            GameTestHelper helper
    ) {
        ServerPlayer player = fakePlayer(helper, "research-duplication");
        PlayerThaumKnowledge knowledge = KnowledgeAccess.get(player).orElseThrow();
        knowledge.completeResearch(ResearchDuplicationService.UNLOCK_RESEARCH);
        var research = ResearchRegistry.find("researcher1").orElseThrow();
        java.util.Map<String, Integer> amountsBefore = new java.util.HashMap<>();
        for (var cost : research.researchCost()) {
            knowledge.addAspectPoints(cost.aspectId(), 20);
            amountsBefore.put(cost.aspectId(), knowledge.aspectAmount(cost.aspectId()));
        }
        player.getInventory().add(new ItemStack(Items.PAPER));
        player.getInventory().add(new ItemStack(Items.FEATHER));
        ItemStack discovery = DiscoveryItem.create(research.id());

        helper.assertTrue(
                ResearchDuplicationService.duplicate(
                        player,
                        knowledge,
                        discovery
                ) == ResearchDuplicationService.Result.CREATED,
                "Completed research was not duplicated"
        );
        helper.assertTrue(
                DiscoveryItem.copies(discovery) == 1,
                "Source discovery did not retain the incremented copy count"
        );
        for (var baseCost : research.researchCost()) {
            helper.assertTrue(
                    knowledge.aspectAmount(baseCost.aspectId())
                            == amountsBefore.get(baseCost.aspectId())
                            - baseCost.amount(),
                    "First copy did not consume the original research aspect cost"
            );
        }
        helper.assertTrue(
                ResearchDuplicationService.cost(research, 1).stream()
                        .allMatch(next -> research.researchCost().stream()
                                .filter(base -> base.aspectId().equals(next.aspectId()))
                                .anyMatch(base -> next.amount() == base.amount() + 1)),
                "Second-copy cost did not add one to every research aspect"
        );
        helper.assertTrue(
                player.getInventory().contains(discovery),
                "Duplicate discovery was not placed in the player inventory"
        );
        helper.assertTrue(
                !player.getInventory().contains(new ItemStack(Items.PAPER))
                        && !player.getInventory().contains(new ItemStack(Items.FEATHER)),
                "Paper or feather was not consumed"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void emptyWandCreatesThaumonomiconWithoutConsumingVis(
            GameTestHelper helper
    ) {
        helper.setBlock(BOOKSHELF_POSITION, Blocks.BOOKSHELF);
        helper.setBlock(INVALID_WAND_TARGET, Blocks.STONE);
        ServerPlayer player = fakePlayer(helper, "bookshelf-wand");
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);
        CompoundTag before = wand.getTag().copy();

        InteractionResult success = useOn(
                helper,
                player,
                BOOKSHELF_POSITION
        );
        helper.assertTrue(
                success.consumesAction(),
                "An empty valid wand did not consume the bookshelf interaction"
        );
        helper.assertBlockPresent(Blocks.AIR, BOOKSHELF_POSITION);
        helper.assertItemEntityCountIs(
                ModItems.THAUMONOMICON.get(),
                BOOKSHELF_POSITION,
                1.5D,
                1
        );
        helper.assertTrue(
                before.equals(wand.getTag()),
                "Bookshelf conversion changed the wand vis or composition"
        );

        InteractionResult invalid = useOn(
                helper,
                player,
                INVALID_WAND_TARGET
        );
        helper.assertTrue(
                invalid == InteractionResult.PASS,
                "A non-bookshelf target was not passed through"
        );
        helper.assertBlockPresent(Blocks.STONE, INVALID_WAND_TARGET);
        helper.assertTrue(
                before.equals(wand.getTag()),
                "Invalid target changed the wand vis or composition"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void basicWandConvertsTableWithoutConsumingVis(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, ModBlocks.THAUMCRAFT_TABLE.get());
        ServerPlayer player = fakePlayer(helper, "table-wand");
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        CompoundTag before = wand.getTag().copy();
        player.setItemInHand(InteractionHand.MAIN_HAND, wand);

        BlockPos absolutePosition = helper.absolutePos(TABLE_POSITION);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolutePosition),
                Direction.UP,
                absolutePosition,
                false
        );
        InteractionResult result = ModBlocks.THAUMCRAFT_TABLE.get().use(
                helper.getLevel().getBlockState(absolutePosition),
                helper.getLevel(),
                absolutePosition,
                player,
                InteractionHand.MAIN_HAND,
                hit
        );

        helper.assertTrue(
                result.consumesAction(),
                "A valid basic wand did not convert the Thaumcraft Table"
        );
        helper.assertBlockPresent(
                ModBlocks.ARCANE_WORKBENCH.get(),
                TABLE_POSITION
        );
        helper.assertTrue(
                helper.getBlockEntity(TABLE_POSITION)
                        instanceof ArcaneWorkbenchBlockEntity,
                "Table conversion did not create an Arcane Workbench block entity"
        );
        ArcaneWorkbenchBlockEntity workbench =
                (ArcaneWorkbenchBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        ItemStack installed = workbench.wand().getItem(0);
        helper.assertTrue(
                installed.is(ModItems.BASIC_WAND.get()) && installed.getCount() == 1,
                "Converted Arcane Workbench did not receive the basic wand"
        );
        helper.assertTrue(
                before.equals(installed.getTag()),
                "Table conversion changed the installed wand vis or composition"
        );
        helper.assertTrue(
                player.getMainHandItem().isEmpty(),
                "Survival table conversion did not remove the wand from the player's hand"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void arcaneStoneCraftConsumesIngredientsAndExactIronVis(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, ModBlocks.ARCANE_WORKBENCH.get());
        helper.assertTrue(
                helper.getBlockEntity(TABLE_POSITION)
                        instanceof ArcaneWorkbenchBlockEntity,
                "Arcane Workbench block entity was not created"
        );
        ArcaneWorkbenchBlockEntity workbench =
                (ArcaneWorkbenchBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        ServerPlayer player = fakePlayer(helper, "arcane-stone");
        BlockPos absolutePosition = helper.absolutePos(TABLE_POSITION);
        player.setPos(
                absolutePosition.getX() + 0.5D,
                absolutePosition.getY() + 1.0D,
                absolutePosition.getZ() + 0.5D
        );
        KnowledgeAccess.get(player)
                .orElseThrow()
                .completeResearch("arcanestone");

        for (int slot = 0; slot < workbench.crafting().getContainerSize(); slot++) {
            workbench.crafting().setItem(slot, new ItemStack(Items.STONE));
        }
        workbench.crafting().setItem(4, new ItemStack(ModItems.AIR_SHARD.get()));

        ArcaneWorkbenchMenu menuWithoutWand = new ArcaneWorkbenchMenu(
                0,
                player.getInventory(),
                workbench
        );
        helper.assertTrue(
                menuWithoutWand.displayCostCentivis(player).getOrDefault("terra", 0) == 100
                        && menuWithoutWand.displayCostCentivis(player)
                        .getOrDefault("ignis", 0) == 100,
                "A correctly assembled recipe did not expose its base vis cost without a wand"
        );

        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        helper.assertTrue(
                WandVisService.add(player, wand, "terra", 2) == 200,
                "Basic wand did not accept two Terra vis"
        );
        helper.assertTrue(
                WandVisService.add(player, wand, "ignis", 2) == 200,
                "Basic wand did not accept two Ignis vis"
        );
        workbench.wand().setItem(0, wand);

        ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(
                0,
                player.getInventory(),
                workbench
        );
        helper.assertTrue(
                menu.previewCostCentivis(player).getOrDefault("terra", 0) == 110
                        && menu.previewCostCentivis(player)
                        .getOrDefault("ignis", 0) == 110,
                "Iron caps did not apply the exact 1.10 Arcane Stone vis cost"
        );
        ItemStack preview = menu.getSlot(
                ArcaneWorkbenchMenu.RESULT_MENU_SLOT
        ).getItem();
        helper.assertTrue(
                preview.is(ModItems.ARCANE_STONE.get())
                        && preview.getCount() == 9,
                "Arcane Stone recipe did not preview exactly nine blocks"
        );

        ItemStack crafted = menu.getSlot(
                ArcaneWorkbenchMenu.RESULT_MENU_SLOT
        ).remove(64);
        helper.assertTrue(
                crafted.is(ModItems.ARCANE_STONE.get())
                        && crafted.getCount() == 9,
                "Arcane Workbench transaction did not issue nine Arcane Stone"
        );
        helper.assertTrue(
                workbench.crafting().isEmpty(),
                "Arcane Stone transaction did not consume all eight stone and the shard"
        );
        ItemStack installed = workbench.wand().getItem(0);
        helper.assertTrue(
                installed.is(ModItems.BASIC_WAND.get())
                        && installed.getCount() == 1,
                "Arcane Stone transaction removed or replaced the installed wand"
        );
        helper.assertTrue(
                WandVisService.visCentivis(installed, "terra") == 90
                        && WandVisService.visCentivis(installed, "ignis") == 90,
                "Arcane Stone transaction did not consume exactly 110 centivis of Terra and Ignis"
        );
        for (String primal : List.of("aer", "aqua", "ordo", "perditio")) {
            helper.assertTrue(
                    WandVisService.visCentivis(installed, primal) == 0,
                    "Arcane Stone transaction changed unrelated " + primal + " vis"
            );
        }
        menu.removed(player);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void playerVisModifiersCombineGearAndActiveEffects(
            GameTestHelper helper
    ) {
        ServerPlayer player = fakePlayer(helper, "vis-modifiers");
        ItemStack wand = ModItems.BASIC_WAND.get().getDefaultInstance();
        player.setItemSlot(
                EquipmentSlot.HEAD,
                ModItems.GOGGLES_OF_REVEALING.get().getDefaultInstance()
        );
        player.addEffect(new MobEffectInstance(
                ModEffects.VIS_EXHAUST.get(),
                200,
                0
        ));

        Map<String, Integer> cost =
                WandVisService.adjustedCostCentivis(
                        player,
                        wand,
                        Map.of("ignis", 1)
                );
        helper.assertTrue(
                cost.getOrDefault("ignis", 0) == 115,
                "Iron cap 1.10, Goggles -5%, and Flux Flu +10% "
                        + "did not combine into 115 centivis"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void warpVerticalRegistersStateEffectsAndPlayableRoot(
            GameTestHelper helper
    ) {
        ServerPlayer player = fakePlayer(helper, "warp-vertical");
        PlayerThaumKnowledge knowledge = KnowledgeAccess.get(player)
                .orElseThrow();
        knowledge.addWarp(
                com.thaumcraftmodern.knowledge.WarpType.PERMANENT,
                2
        );
        knowledge.addWarp(
                com.thaumcraftmodern.knowledge.WarpType.NORMAL,
                1
        );
        helper.assertTrue(
                knowledge.totalWarp() == 3 && knowledge.warpCounter() == 3,
                "Warp pools did not refresh classic event pressure"
        );
        helper.assertTrue(
                ResearchRegistry.find("warp")
                        .map(definition -> !definition.inactive())
                        .orElse(false),
                "Warp root research was not enabled"
        );
        helper.assertTrue(
                BuiltInRegistries.MOB_EFFECT.getKey(
                        ModEffects.DEATH_GAZE.get()
                ).getPath().equals("death_gaze")
                        && BuiltInRegistries.MOB_EFFECT.getKey(
                                ModEffects.WARP_WARD.get()
                        ).getPath().equals("warp_ward"),
                "Classic warp effects were not registered"
        );
        helper.assertTrue(
                BuiltInRegistries.ITEM.getKey(ModItems.SANITY_CHECKER.get())
                        .getPath().equals("sanity_checker")
                        && BuiltInRegistries.ITEM.getKey(
                                ModItems.SANITY_SOAP.get()
                        ).getPath().equals("sanity_soap"),
                "Warp utility items were not registered"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void moundNodeGuardianMarkerSurvivesSaveLoad(
            GameTestHelper helper
    ) {
        BlockPos sourcePosition = new BlockPos(1, 1, 1);
        BlockPos restoredPosition = new BlockPos(2, 1, 1);
        helper.setBlock(sourcePosition, ModBlocks.AURA_NODE.get());
        helper.setBlock(restoredPosition, ModBlocks.AURA_NODE.get());
        AuraNodeBlockEntity source =
                (AuraNodeBlockEntity) helper.getBlockEntity(sourcePosition);
        AuraNodeBlockEntity restored =
                (AuraNodeBlockEntity) helper.getBlockEntity(restoredPosition);
        source.enableMoundGuardianSpawner();
        CompoundTag saved = source.getUpdateTag();
        restored.load(saved);
        helper.assertTrue(
                source.isMoundGuardianSpawner()
                        && restored.isMoundGuardianSpawner(),
                "Mound dark-node guardian marker did not survive save/load"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingAuraNodeDropsClassicAspectEssences(
            GameTestHelper helper
    ) {
        BlockPos nodePosition = new BlockPos(2, 1, 2);
        helper.setBlock(nodePosition, ModBlocks.AURA_NODE.get());
        helper.assertTrue(
                helper.getBlockEntity(nodePosition)
                        instanceof AuraNodeBlockEntity,
                "Aura node block entity was not created"
        );

        EnumMap<PrimalAspect, Integer> current =
                new EnumMap<>(PrimalAspect.class);
        EnumMap<PrimalAspect, Integer> maximum =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            current.put(aspect, 0);
            maximum.put(aspect, 20);
        }
        current.put(PrimalAspect.AER, 5);
        current.put(PrimalAspect.IGNIS, 10);

        AuraNodeBlockEntity node =
                (AuraNodeBlockEntity) helper.getBlockEntity(nodePosition);
        helper.assertTrue(
                node.initializeOnce(new AuraNodeState(
                        UUID.fromString(
                                "38b4c88a-e07d-4d40-a473-acde4848b66a"
                        ),
                        AuraNodeType.NORMAL,
                        AuraNodeModifier.NORMAL,
                        current,
                        maximum,
                        0L
                )),
                "Aura node rejected its test state"
        );

        ServerPlayer player = fakePlayer(helper, "node-break-drops");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        helper.assertTrue(
                player.gameMode.destroyBlock(helper.absolutePos(nodePosition)),
                "Survival player could not destroy an aura node"
        );
        helper.assertBlockPresent(ModBlocks.FLUX_GOO.get(), nodePosition);
        helper.assertItemEntityCountIs(
                ModItems.ETHEREAL_ESSENCE.get(),
                nodePosition,
                2.0D,
                3
        );

        int aer = 0;
        int ignis = 0;
        for (ItemEntity entity : helper.getEntities(
                EntityType.ITEM,
                nodePosition,
                2.0D
        )) {
            ItemStack essence = entity.getItem();
            if (!essence.is(ModItems.ETHEREAL_ESSENCE.get())) {
                continue;
            }
            PrimalAspect aspect = EtherealEssenceItem.aspect(essence)
                    .orElseThrow();
            helper.assertTrue(
                    EtherealEssenceItem.amount(essence) == 2,
                    "Dropped essence did not contain two aspect points"
            );
            if (aspect == PrimalAspect.AER) {
                aer += essence.getCount();
            } else if (aspect == PrimalAspect.IGNIS) {
                ignis += essence.getCount();
            }
        }
        helper.assertTrue(
                aer == 1 && ignis == 2,
                "Aura node dropped the wrong aspect-specific essence counts"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void silverwoodAndTotemNodeBlocksHostAuraNodes(
            GameTestHelper helper
    ) {
        BlockPos silverwood = new BlockPos(1, 1, 1);
        BlockPos totem = new BlockPos(3, 1, 1);
        helper.setBlock(silverwood, ModBlocks.SILVERWOOD_NODE.get());
        helper.setBlock(totem, ModBlocks.OBSIDIAN_TOTEM_NODE.get());

        helper.assertTrue(
                helper.getBlockEntity(silverwood)
                        instanceof AuraNodeBlockEntity,
                "Silverwood node block did not create an aura node"
        );
        helper.assertTrue(
                helper.getBlockEntity(totem)
                        instanceof AuraNodeBlockEntity,
                "Obsidian totem node block did not create an aura node"
        );

        AuraNodeBlockEntity silverwoodNode =
                (AuraNodeBlockEntity) helper.getBlockEntity(silverwood);
        AuraNodeBlockEntity totemNode =
                (AuraNodeBlockEntity) helper.getBlockEntity(totem);
        ServerNodeJarWorld jarWorld = new ServerNodeJarWorld(
                helper.getLevel(),
                ModBlocks.JARRED_AURA_NODE.get().defaultBlockState()
        );
        helper.assertTrue(
                jarWorld.isAuraNode(
                        helper.absolutePos(silverwood),
                        silverwoodNode.scanIdentity().nodeId()
                ),
                "Node-jar capture did not recognize the Silverwood node"
        );
        helper.assertTrue(
                jarWorld.isAuraNode(
                        helper.absolutePos(totem),
                        totemNode.scanIdentity().nodeId()
                ),
                "Node-jar capture did not recognize the totem node"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingLegacyCapturedNodeJarReturnsPayloadItem(
            GameTestHelper helper
    ) {
        BlockPos jarPosition = new BlockPos(2, 1, 2);
        NodeJarData data = NodeJarFactory.captured(
                UUID.randomUUID(),
                AuraNodeFactory.newWorldNode()
        );
        helper.setBlock(jarPosition, ModBlocks.JARRED_AURA_NODE.get());
        helper.assertTrue(
                helper.getBlockEntity(jarPosition)
                        instanceof JarredAuraNodeBlockEntity,
                "Jarred aura node block entity was not created"
        );
        JarredAuraNodeBlockEntity jar =
                (JarredAuraNodeBlockEntity) helper.getBlockEntity(jarPosition);
        helper.assertTrue(
                jar.initializeOnce(data),
                "Jarred aura node rejected its test payload"
        );

        NodeJarSavedData savedData = NodeJarSavedData.get(helper.getLevel());
        helper.assertTrue(
                savedData.ledger().registerCaptured(data),
                "Legacy captured payload could not be registered"
        );
        savedData.markLedgerChanged();

        ServerPlayer player = fakePlayer(helper, "node-jar-break-drop");
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        helper.assertTrue(
                player.gameMode.destroyBlock(helper.absolutePos(jarPosition)),
                "Survival player could not destroy a jarred aura node"
        );
        helper.assertBlockPresent(Blocks.AIR, jarPosition);
        helper.assertItemEntityCountIs(
                ModItems.JARRED_AURA_NODE.get(),
                jarPosition,
                2.0D,
                1
        );

        ItemStack dropped = helper.getEntities(
                        EntityType.ITEM,
                        jarPosition,
                        2.0D
                ).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(ModItems.JARRED_AURA_NODE.get()))
                .findFirst()
                .orElseThrow();
        NodeJarData restored = NodeJarCodec.read(dropped).orElseThrow();
        helper.assertTrue(
                restored.payloadId().equals(data.payloadId())
                        && restored.node().snapshot().equals(data.node().snapshot()),
                "Dropped jar did not preserve its node payload"
        );
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allLegacyWorldMobsConstructWithKindGoals(
            GameTestHelper helper
    ) {
        for (LegacyMobKind kind : LegacyMobKind.values()) {
            var entity = ModEntities.forKind(kind).get().create(
                    helper.getLevel()
            );
            helper.assertTrue(
                    entity != null && entity.kind() == kind,
                    "Could not construct legacy entity " + kind.id()
            );
            entity.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crimsonKnightAndPraetorAlwaysCarryIronSwords(
            GameTestHelper helper
    ) {
        var knight = ModEntities.CRIMSON_KNIGHT.get().create(
                helper.getLevel()
        );
        var praetor = ModEntities.CRIMSON_PRAETOR.get().create(
                helper.getLevel()
        );
        helper.assertTrue(knight != null, "Crimson Knight was not created");
        helper.assertTrue(praetor != null, "Crimson Praetor was not created");
        helper.assertTrue(
                knight.getMainHandItem().is(Items.IRON_SWORD),
                "Crimson Knight did not receive an iron sword"
        );
        helper.assertTrue(
                praetor.getMainHandItem().is(Items.IRON_SWORD),
                "Crimson Praetor did not receive an iron sword"
        );

        knight.setItemSlot(
                EquipmentSlot.MAINHAND,
                new ItemStack(Items.STICK)
        );
        praetor.setItemSlot(
                EquipmentSlot.MAINHAND,
                new ItemStack(Items.STICK)
        );
        knight.readAdditionalSaveData(new CompoundTag());
        praetor.readAdditionalSaveData(new CompoundTag());
        helper.assertTrue(
                knight.getMainHandItem().is(Items.IRON_SWORD),
                "Saved Crimson Knight did not migrate to an iron sword"
        );
        helper.assertTrue(
                praetor.getMainHandItem().is(Items.IRON_SWORD),
                "Saved Crimson Praetor did not migrate to an iron sword"
        );
        knight.discard();
        praetor.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void furiousZombieGrowsWhenDamagedAndPersistsAnger(
            GameTestHelper helper
    ) {
        var furious = ModEntities.FURIOUS_ZOMBIE.get().create(
                helper.getLevel()
        );
        helper.assertTrue(furious != null, "Furious Zombie was not created");
        float initialWidth = furious.getBbWidth();
        helper.assertTrue(
                furious.furiousAnger() == 1.0F,
                "Furious Zombie did not start at classic anger 1.0"
        );
        helper.assertTrue(
                furious.hurt(helper.getLevel().damageSources().generic(), 1.0F),
                "Furious Zombie rejected generic test damage"
        );
        helper.assertTrue(
                Math.abs(furious.furiousAnger() - 1.1F) < 0.0001F,
                "Damage did not add the classic 0.1 anger"
        );
        helper.assertTrue(
                furious.getBbWidth() > initialWidth,
                "Damage did not enlarge the Furious Zombie hitbox"
        );

        CompoundTag saved = new CompoundTag();
        furious.addAdditionalSaveData(saved);
        var restored = ModEntities.FURIOUS_ZOMBIE.get().create(
                helper.getLevel()
        );
        helper.assertTrue(restored != null, "Restored Furious Zombie missing");
        restored.readAdditionalSaveData(saved);
        helper.assertTrue(
                Math.abs(restored.furiousAnger() - 1.1F) < 0.0001F,
                "Furious Zombie anger did not survive save/load"
        );
        furious.discard();
        restored.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void altarGuardsKeepCombatBeyondHomeWithClassicDetection(
            GameTestHelper helper
    ) {
        var knight = ModEntities.CRIMSON_KNIGHT.get().create(
                helper.getLevel()
        );
        helper.assertTrue(knight != null, "Crimson Knight was not created");
        knight.configureCrimsonAltar(BlockPos.ZERO, false);
        helper.assertTrue(
                !knight.hasRestriction(),
                "Altar home radius still blocks combat target acquisition"
        );
        helper.assertTrue(
                knight.getAttributeValue(Attributes.FOLLOW_RANGE) == 32.0D,
                "Crimson Knight does not use the classic 32-block range"
        );

        var zombie = ModEntities.ANGRY_ZOMBIE.get().create(
                helper.getLevel()
        );
        helper.assertTrue(zombie != null, "Angry Zombie was not created");
        helper.assertTrue(
                zombie.getAttributeValue(Attributes.FOLLOW_RANGE) == 32.0D,
                "Angry Zombie does not use the classic generic range"
        );
        var mindSpider = ModEntities.MIND_SPIDER.get().create(
                helper.getLevel()
        );
        helper.assertTrue(mindSpider != null, "Mind Spider was not created");
        helper.assertTrue(
                mindSpider.getAttributeValue(Attributes.FOLLOW_RANGE) == 12.0D,
                "Mind Spider does not use the classic 12-block range"
        );
        mindSpider.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(40.0D);
        mindSpider.readAdditionalSaveData(new CompoundTag());
        helper.assertTrue(
                mindSpider.getAttributeValue(Attributes.FOLLOW_RANGE) == 12.0D,
                "Existing Mind Spider did not migrate to its classic range"
        );
        knight.discard();
        zombie.discard();
        mindSpider.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void classicSpecialMobsUseOriginalCombatAttributes(
            GameTestHelper helper
    ) {
        var wisp = ModEntities.WISP.get().create(helper.getLevel());
        var firebat = ModEntities.FIREBAT.get().create(helper.getLevel());
        var mindSpider = ModEntities.MIND_SPIDER.get().create(
                helper.getLevel()
        );
        var slime = ModEntities.THAUMIC_SLIME.get().create(
                helper.getLevel()
        );
        helper.assertTrue(wisp != null, "Wisp was not created");
        helper.assertTrue(firebat != null, "Firebat was not created");
        helper.assertTrue(mindSpider != null, "Mind Spider was not created");
        helper.assertTrue(slime != null, "Thaumic Slime was not created");
        helper.assertTrue(
                wisp.getMaxHealth() == 22.0F
                        && wisp.getAttributeValue(
                                Attributes.ATTACK_DAMAGE
                        ) == 3.0D,
                "Wisp does not use TC4 health and damage"
        );
        helper.assertTrue(
                firebat.getMaxHealth() == 5.0F
                        && firebat.getAttributeValue(
                                Attributes.ATTACK_DAMAGE
                        ) == 1.0D,
                "Firebat does not use TC4 health and damage"
        );
        helper.assertTrue(
                mindSpider.getMaxHealth() == 1.0F
                        && mindSpider.getAttributeValue(
                                Attributes.ATTACK_DAMAGE
                        ) == 1.0D,
                "Mind Spider does not use TC4 health and damage"
        );
        slime.setThaumicSlimeSize(4);
        helper.assertTrue(
                slime.getMaxHealth() == 4.0F
                        && slime.getHealth() == 4.0F
                        && slime.getAttributeValue(
                                Attributes.ATTACK_DAMAGE
                        ) == 4.0D,
                "Thaumic Slime size does not control health and damage"
        );
        CompoundTag savedSlime = new CompoundTag();
        slime.addAdditionalSaveData(savedSlime);
        helper.assertTrue(
                savedSlime.getInt("ThaumicSlimeSize") == 4,
                "Thaumic Slime size was not saved"
        );
        wisp.discard();
        firebat.discard();
        mindSpider.discard();
        slime.discard();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void arcaneWorkbenchCraftsVanillaRecipesWithoutWand(
            GameTestHelper helper
    ) {
        helper.setBlock(TABLE_POSITION, ModBlocks.ARCANE_WORKBENCH.get());
        helper.assertTrue(
                helper.getBlockEntity(TABLE_POSITION)
                        instanceof ArcaneWorkbenchBlockEntity,
                "Arcane Workbench block entity was not created"
        );
        ArcaneWorkbenchBlockEntity workbench =
                (ArcaneWorkbenchBlockEntity) helper.getBlockEntity(TABLE_POSITION);
        ServerPlayer player = fakePlayer(helper, "vanilla-crafting");
        BlockPos absolutePosition = helper.absolutePos(TABLE_POSITION);
        player.setPos(
                absolutePosition.getX() + 0.5D,
                absolutePosition.getY() + 1.0D,
                absolutePosition.getZ() + 0.5D
        );
        workbench.crafting().setItem(0, new ItemStack(Items.OAK_LOG));

        ArcaneWorkbenchMenu menu = new ArcaneWorkbenchMenu(
                0,
                player.getInventory(),
                workbench
        );
        ItemStack preview = menu.getSlot(
                ArcaneWorkbenchMenu.RESULT_MENU_SLOT
        ).getItem();
        helper.assertTrue(
                preview.is(Items.OAK_PLANKS) && preview.getCount() == 4,
                "Arcane Workbench did not preview the vanilla oak planks recipe"
        );

        ItemStack crafted = menu.getSlot(
                ArcaneWorkbenchMenu.RESULT_MENU_SLOT
        ).remove(64);
        helper.assertTrue(
                crafted.is(Items.OAK_PLANKS) && crafted.getCount() == 4,
                "Arcane Workbench did not issue the vanilla recipe output"
        );
        helper.assertTrue(
                workbench.crafting().isEmpty(),
                "Vanilla crafting transaction did not consume the oak log"
        );
        helper.assertTrue(
                workbench.wand().isEmpty(),
                "Vanilla crafting unexpectedly required or changed a wand"
        );
        menu.removed(player);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 700)
    public static void essentiaDistillationCompletesClassicMachineCycle(
            GameTestHelper helper
    ) {
        BlockPos furnacePos = new BlockPos(1, 1, 1);
        BlockPos alembicPos = furnacePos.above();
        BlockPos firstTube = new BlockPos(2, 2, 1);
        BlockPos secondTube = new BlockPos(3, 2, 1);
        BlockPos jarPos = new BlockPos(4, 1, 1);
        BlockPos jarTube = jarPos.above();
        helper.setBlock(furnacePos, ModBlocks.ALCHEMICAL_FURNACE.get());
        helper.setBlock(alembicPos, ModBlocks.ARCANE_ALEMBIC.get());
        helper.setBlock(firstTube, ModBlocks.ESSENTIA_TUBE.get());
        helper.setBlock(secondTube, ModBlocks.RESTRICTED_ESSENTIA_TUBE.get());
        helper.setBlock(jarTube, ModBlocks.FILTERED_ESSENTIA_TUBE.get());
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());

        AlchemicalFurnaceBlockEntity furnace =
                (AlchemicalFurnaceBlockEntity) helper.getBlockEntity(furnacePos);
        EssentiaTubeBlockEntity filtered =
                (EssentiaTubeBlockEntity) helper.getBlockEntity(jarTube);
        Map<String, Integer> logAspects =
                com.thaumcraftmodern.crucible.ItemAspectRegistry
                        .aspects(new ItemStack(Items.OAK_LOG)).orElse(Map.of());
        helper.assertTrue(!logAspects.isEmpty(),
                "Oak log has no runtime aspects for furnace distillation");
        String expected = logAspects.keySet().iterator().next();
        filtered.setFilter(expected);
        furnace.setItem(0, new ItemStack(Items.OAK_LOG));
        furnace.setItem(1, new ItemStack(Items.COAL));

        helper.runAfterDelay(620, () -> {
            EssentiaJarBlockEntity jar =
                    (EssentiaJarBlockEntity) helper.getBlockEntity(jarPos);
            helper.assertTrue(jar.amount() > 0,
                    "Item essentia did not reach the warded jar through mixed tubes");
            helper.assertTrue(expected.equals(jar.aspect()),
                    "Filtered mixed network moved the wrong aspect");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void essentiaMachinesPersistTheirNbtState(
            GameTestHelper helper
    ) {
        BlockPos jarPos = new BlockPos(1, 1, 1);
        BlockPos alembicPos = new BlockPos(2, 1, 1);
        BlockPos furnacePos = new BlockPos(3, 1, 1);
        BlockPos tubePos = new BlockPos(4, 1, 1);
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());
        helper.setBlock(alembicPos, ModBlocks.ARCANE_ALEMBIC.get());
        helper.setBlock(furnacePos, ModBlocks.ALCHEMICAL_FURNACE.get());
        helper.setBlock(tubePos, ModBlocks.ESSENTIA_TUBE.get());
        EssentiaJarBlockEntity jar =
                (EssentiaJarBlockEntity) helper.getBlockEntity(jarPos);
        ArcaneAlembicBlockEntity alembic =
                (ArcaneAlembicBlockEntity) helper.getBlockEntity(alembicPos);
        AlchemicalFurnaceBlockEntity furnace =
                (AlchemicalFurnaceBlockEntity) helper.getBlockEntity(furnacePos);
        EssentiaTubeBlockEntity tube =
                (EssentiaTubeBlockEntity) helper.getBlockEntity(tubePos);
        jar.setFilter("aer");
        helper.assertTrue(jar.addEssentia("aer", 17, Direction.UP) == 17,
                "Jar rejected valid filtered essentia");
        alembic.setFilter("ignis");
        helper.assertTrue(alembic.acceptFromFurnace("ignis", 9) == 9,
                "Alembic rejected furnace essentia");
        helper.assertTrue(!alembic.canReturnEssentia(),
                "Arcane Alembic ignored canReturnEssentia=false data flag");
        furnace.setItem(0, new ItemStack(Items.OAK_LOG));
        furnace.setItem(1, new ItemStack(Items.COAL));
        AlchemicalFurnaceBlockEntity.serverTick(
                helper.getLevel(), helper.absolutePos(furnacePos),
                helper.getBlockState(furnacePos), furnace);
        tube.setFilter("aer");
        tube.setFacing(Direction.EAST);
        tube.toggleSide(Direction.NORTH);
        helper.assertTrue(tube.addEssentia("aer", 1, Direction.UP) == 1,
                "Tube rejected valid buffered essentia before NBT round-trip");

        CompoundTag jarTag = jar.saveWithFullMetadata();
        CompoundTag alembicTag = alembic.saveWithFullMetadata();
        CompoundTag furnaceTag = furnace.saveWithFullMetadata();
        CompoundTag tubeTag = tube.saveWithFullMetadata();
        EssentiaJarBlockEntity restoredJar = new EssentiaJarBlockEntity(
                helper.absolutePos(new BlockPos(3, 1, 1)),
                ModBlocks.WARDED_JAR.get().defaultBlockState());
        ArcaneAlembicBlockEntity restoredAlembic =
                new ArcaneAlembicBlockEntity(
                        helper.absolutePos(new BlockPos(4, 1, 1)),
                        ModBlocks.ARCANE_ALEMBIC.get().defaultBlockState());
        AlchemicalFurnaceBlockEntity restoredFurnace =
                new AlchemicalFurnaceBlockEntity(
                        helper.absolutePos(new BlockPos(5, 1, 1)),
                        ModBlocks.ALCHEMICAL_FURNACE.get().defaultBlockState());
        EssentiaTubeBlockEntity restoredTube =
                new EssentiaTubeBlockEntity(
                        helper.absolutePos(new BlockPos(6, 1, 1)),
                        ModBlocks.ESSENTIA_TUBE.get().defaultBlockState());
        restoredJar.load(jarTag);
        restoredAlembic.load(alembicTag);
        restoredFurnace.load(furnaceTag);
        restoredTube.load(tubeTag);
        helper.assertTrue(restoredJar.amount() == 17
                        && "aer".equals(restoredJar.aspect())
                        && "aer".equals(restoredJar.filter()),
                "Warded jar NBT round-trip lost contents or filter");
        helper.assertTrue(restoredAlembic.storedAmount() == 9
                        && "ignis".equals(restoredAlembic.storedAspect())
                        && "ignis".equals(restoredAlembic.filterAspect()),
                "Arcane Alembic NBT round-trip lost contents or filter");
        helper.assertTrue(restoredFurnace.getItem(0).is(Items.OAK_LOG)
                        && restoredFurnace.data().get(0) > 0
                        && restoredFurnace.data().get(2) > 0,
                "Alchemical furnace NBT round-trip lost inventory or progress");
        helper.assertTrue("aer".equals(restoredTube.filter())
                        && restoredTube.facing() == Direction.EAST
                        && !restoredTube.isSideOpen(Direction.NORTH)
                        && "aer".equals(restoredTube.essentiaType(Direction.UP))
                        && restoredTube.essentiaAmount(Direction.UP) == 1,
                "Essentia tube NBT round-trip lost filter, direction, connection or buffer");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bufferVoidJarAndCentrifugeFollowTransferRules(
            GameTestHelper helper) {
        BlockPos bufferPos = new BlockPos(1, 1, 1);
        BlockPos jarPos = new BlockPos(3, 1, 1);
        BlockPos centrifugePos = new BlockPos(5, 1, 1);
        helper.setBlock(bufferPos, ModBlocks.ESSENTIA_BUFFER.get());
        helper.setBlock(jarPos, ModBlocks.VOID_JAR.get());
        helper.setBlock(centrifugePos, ModBlocks.ESSENTIA_CENTRIFUGE.get());
        EssentiaBufferBlockEntity buffer = (EssentiaBufferBlockEntity) helper.getBlockEntity(bufferPos);
        VoidJarBlockEntity jar = (VoidJarBlockEntity) helper.getBlockEntity(jarPos);
        EssentiaCentrifugeBlockEntity centrifuge = (EssentiaCentrifugeBlockEntity) helper.getBlockEntity(centrifugePos);

        for (int i = 0; i < EssentiaBufferBlockEntity.CAPACITY_PER_ASPECT; i++) {
            helper.assertTrue(buffer.addEssentia("aer", 1, Direction.UP) == 1,
                    "Buffer rejected valid aer point");
            helper.assertTrue(buffer.addEssentia("ignis", 1, Direction.UP) == 1,
                    "Buffer rejected valid mixed ignis point");
        }
        helper.assertTrue(buffer.addEssentia("aer", 1, Direction.UP) == 0
                        && buffer.addEssentia("ignis", 1, Direction.UP) == 0,
                "Buffer accepted more than 8 points of one aspect");
        helper.assertTrue(buffer.addEssentia("aqua", 1, Direction.UP) == 1
                        && buffer.totalAmount() == 17
                        && buffer.contents().getOrDefault("aer", 0) == 8
                        && buffer.contents().getOrDefault("ignis", 0) == 8,
                "Buffer did not provide an independent eight-point capacity per aspect");
        buffer.cycleChoke(Direction.NORTH);
        helper.assertTrue(buffer.chokeMode(Direction.NORTH) == 1
                        && buffer.suctionAmount(Direction.NORTH) == 1,
                "Buffer weak choke mode changed classic suction");
        buffer.cycleChoke(Direction.NORTH);
        helper.assertTrue(buffer.chokeMode(Direction.NORTH) == 2
                        && buffer.suctionAmount(Direction.NORTH) == 0,
                "Buffer closed choke mode did not stop suction");

        jar.setFilter("aer", Direction.EAST);
        helper.assertTrue(jar.addEssentia("aer", 80, Direction.UP) == 80
                        && jar.amount() == 64,
                "Void jar did not accept and destroy matching overflow");
        helper.assertTrue(jar.addEssentia("ignis", 1, Direction.UP) == 0,
                "Void jar accepted an aspect that contradicts its label");

        helper.assertTrue(centrifuge.addEssentia("motus", 1, Direction.DOWN) == 1,
                "Centrifuge rejected a compound aspect");
        for (int tick = 0; tick <= EssentiaCentrifugeBlockEntity.PROCESS_TICKS; tick++) {
            EssentiaCentrifugeBlockEntity.serverTick(helper.getLevel(),
                    helper.absolutePos(centrifugePos), helper.getBlockState(centrifugePos), centrifuge);
        }
        var components = AspectRegistryRuntime.find("motus").orElseThrow().components();
        helper.assertTrue(centrifuge.inputAspect() == null
                        && components.contains(centrifuge.outputAspect()),
                "Centrifuge output was not one direct TC4 component");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reservoirConnectsToBufferAndTubeThroughSelectedFace(
            GameTestHelper helper) {
        BlockPos reservoirPos = new BlockPos(2, 2, 2);
        BlockPos neighbourPos = reservoirPos.east();
        helper.setBlock(reservoirPos, ModBlocks.ESSENTIA_RESERVOIR.get()
                .defaultBlockState().setValue(EssentiaReservoirBlock.FACING,
                        Direction.EAST));
        helper.setBlock(neighbourPos, ModBlocks.ESSENTIA_BUFFER.get());
        EssentiaReservoirBlockEntity reservoir =
                (EssentiaReservoirBlockEntity) helper.getBlockEntity(reservoirPos);
        EssentiaBufferBlockEntity buffer =
                (EssentiaBufferBlockEntity) helper.getBlockEntity(neighbourPos);
        helper.assertTrue(reservoir.isConnectable(Direction.EAST)
                        && !reservoir.isConnectable(Direction.DOWN),
                "Reservoir exposed more than its selected TC4 port");
        helper.assertTrue(buffer.addEssentia("aer", 1, Direction.WEST) == 1,
                "Buffer rejected the transfer fixture");
        for (int tick = 0; tick < 5; tick++) {
            EssentiaReservoirBlockEntity.serverTick(helper.getLevel(),
                    helper.absolutePos(reservoirPos),
                    helper.getBlockState(reservoirPos), reservoir);
        }
        helper.assertTrue(reservoir.contents().getOrDefault("aer", 0) == 1
                        && buffer.contents().getOrDefault("aer", 0) == 0,
                "Reservoir did not pull essentia from an adjacent buffer");

        helper.setBlock(neighbourPos, ModBlocks.ESSENTIA_TUBE.get());
        EssentiaTubeBlockEntity tube =
                (EssentiaTubeBlockEntity) helper.getBlockEntity(neighbourPos);
        EssentiaTubeBlock.refreshConnections(helper.getLevel(),
                helper.absolutePos(neighbourPos));
        helper.assertTrue(tube.isConnectable(Direction.WEST)
                        && reservoir.isConnectable(Direction.EAST)
                        && helper.getBlockState(neighbourPos)
                                .getValue(EssentiaTubeBlock.WEST),
                "Reservoir and ordinary tube did not expose matching ports");

        EssentiaReservoirBlockEntity restored = new EssentiaReservoirBlockEntity(
                helper.absolutePos(reservoirPos), helper.getBlockState(reservoirPos));
        restored.load(reservoir.saveWithFullMetadata());
        helper.assertTrue(restored.totalAmount() == 1
                        && restored.contents().getOrDefault("aer", 0) == 1,
                "Reservoir lost its mixed store during NBT round-trip");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void crystallizerCreatesAspectCrystalOnServer(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 2, 2);
        helper.setBlock(pos, ModBlocks.ESSENTIA_CRYSTALLIZER.get());
        EssentiaCrystallizerBlockEntity machine =
                (EssentiaCrystallizerBlockEntity) helper.getBlockEntity(pos);
        helper.assertTrue(machine.addEssentia("aer", 1, Direction.DOWN) == 1,
                "Crystallizer rejected one essentia point");
        for (int tick = 0; tick < EssentiaCrystallizerBlockEntity.PROGRESS_MAX * 5; tick++) {
            EssentiaCrystallizerBlockEntity.serverTick(helper.getLevel(),
                    helper.absolutePos(pos), helper.getBlockState(pos), machine);
        }
        helper.assertTrue(machine.aspect() == null,
                "Crystallizer did not consume its one-point reservoir");
        helper.assertTrue(helper.getEntities(EntityType.ITEM, pos.above(), 2.0D).stream()
                        .map(ItemEntity::getItem)
                        .anyMatch(stack -> "aer".equals(EssentiaCrystalItem.aspect(stack).orElse(null))),
                "Crystallizer did not create the corresponding server-owned crystal");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void crystallizerTurnsItsInputTowardAdjacentTransport(
            GameTestHelper helper) {
        BlockPos crystalPos = new BlockPos(2, 2, 2);
        BlockPos bufferPos = crystalPos.east();
        helper.setBlock(crystalPos, ModBlocks.ESSENTIA_CRYSTALLIZER.get());
        helper.setBlock(bufferPos, ModBlocks.ESSENTIA_BUFFER.get());
        BlockPos absoluteCrystal = helper.absolutePos(crystalPos);
        EssentiaCrystallizerBlockEntity machine =
                (EssentiaCrystallizerBlockEntity) helper.getBlockEntity(crystalPos);
        EssentiaCrystallizerBlockEntity.serverTick(helper.getLevel(),
                absoluteCrystal, helper.getBlockState(crystalPos), machine);
        helper.assertTrue(helper.getBlockState(crystalPos).getValue(
                        EssentiaCrystallizerBlock.FACING) == Direction.EAST,
                "Crystallizer tick did not turn its input toward the adjacent buffer");

        helper.setBlock(bufferPos, Blocks.AIR);
        BlockPos tubePos = crystalPos.west();
        helper.setBlock(tubePos, ModBlocks.ESSENTIA_TUBE.get());
        EssentiaCrystallizerBlockEntity.serverTick(helper.getLevel(),
                absoluteCrystal, helper.getBlockState(crystalPos), machine);
        helper.assertTrue(helper.getBlockState(crystalPos).getValue(
                        EssentiaCrystallizerBlock.FACING) == Direction.WEST,
                "Crystallizer tick did not turn its input toward the adjacent tube");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thaumatoriumReservesEssentiaAndCompletesKnownRecipe(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 3, 3);
        helper.setBlock(pos.below(2), Blocks.LAVA);
        helper.setBlock(pos, ModBlocks.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.HALF, DoubleBlockHalf.LOWER));
        ThaumatoriumBlockEntity machine = (ThaumatoriumBlockEntity) helper.getBlockEntity(pos);
        ServerPlayer player = fakePlayer(helper, "thaumatorium-known-recipe");
        KnowledgeAccess.get(player).orElseThrow().completeResearch("nitor");
        ItemStack catalyst = new ItemStack(Items.GLOWSTONE_DUST);
        helper.assertTrue(machine.insertCatalyst(player, catalyst) && catalyst.isEmpty(),
                "Thaumatorium did not server-reserve a catalyst for known recipe");
        helper.assertTrue(machine.addEssentia("potentia", 3, Direction.WEST) == 3
                        && machine.addEssentia("ignis", 3, Direction.WEST) == 3
                        && machine.addEssentia("lux", 3, Direction.WEST) == 3,
                "Thaumatorium did not reserve the exact selected recipe costs");
        for (int tick = 0; tick < 5; tick++) {
            ThaumatoriumBlockEntity.serverTick(helper.getLevel(), helper.absolutePos(pos),
                    helper.getBlockState(pos), machine);
        }
        helper.assertTrue(machine.catalyst().isEmpty() && machine.reservedEssentia().isEmpty(),
                "Thaumatorium did not atomically consume catalyst and reserved essentia");
        helper.assertTrue(helper.getEntities(EntityType.ITEM, pos.north(), 2.0D).stream()
                        .anyMatch(entity -> entity.getItem().is(ModItems.NITOR.get())),
                "Thaumatorium did not create the selected crucible result");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void constructedThaumatoriumPullsPotentiaFromJarThroughTube(
            GameTestHelper helper) {
        BlockPos machinePos = new BlockPos(3, 2, 3);
        BlockPos tubePos = machinePos.above().west();
        BlockPos jarPos = tubePos.below();
        helper.setBlock(machinePos.below(), ModBlocks.ALCHEMICAL_FURNACE.get());
        helper.setBlock(machinePos, ModBlocks.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(ThaumatoriumBlock.FACING, Direction.NORTH));
        helper.setBlock(machinePos.above(), ModBlocks.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(ThaumatoriumBlock.FACING, Direction.NORTH));
        helper.setBlock(tubePos, ModBlocks.ESSENTIA_TUBE.get());
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());

        ThaumatoriumBlockEntity machine = (ThaumatoriumBlockEntity)
                helper.getBlockEntity(machinePos);
        EssentiaTubeBlockEntity tube = (EssentiaTubeBlockEntity)
                helper.getBlockEntity(tubePos);
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getBlockEntity(jarPos);
        ServerPlayer player = fakePlayer(helper, "thaumatorium-potentia-pull");
        KnowledgeAccess.get(player).orElseThrow().completeResearch("nitor");
        helper.assertTrue(machine.insertCatalyst(
                        player, new ItemStack(Items.GLOWSTONE_DUST)),
                "Constructed Thaumatorium rejected the Nitor catalyst");
        helper.assertTrue(machine.addEssentia("ignis", 3, Direction.EAST) == 3
                        && machine.addEssentia("lux", 3, Direction.EAST) == 3,
                "Could not advance the recipe to its Potentia stage");
        helper.assertTrue(jar.addEssentia("potentia", 1, Direction.UP) == 1,
                "Test jar rejected Potentia");

        for (int tick = 0; tick < 40; tick++) {
            ThaumatoriumBlockEntity.serverTick(
                    helper.getLevel(), helper.absolutePos(machinePos),
                    helper.getBlockState(machinePos), machine);
            EssentiaTubeBlockEntity.serverTick(
                    helper.getLevel(), helper.absolutePos(tubePos),
                    helper.getBlockState(tubePos), tube);
        }
        helper.assertTrue(jar.amount() == 0
                        && machine.reservedEssentia()
                        .getOrDefault("potentia", 0) == 1,
                "Constructed Thaumatorium did not pull Potentia through its tube");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledThaumatoriumCraftReturnsEssentiaThroughTube(
            GameTestHelper helper) {
        BlockPos machinePos = new BlockPos(3, 2, 3);
        BlockPos tubePos = machinePos.above().west();
        BlockPos jarPos = tubePos.below();
        helper.setBlock(machinePos.below(), ModBlocks.ALCHEMICAL_FURNACE.get());
        helper.setBlock(machinePos, ModBlocks.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(ThaumatoriumBlock.FACING, Direction.NORTH));
        helper.setBlock(machinePos.above(), ModBlocks.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(ThaumatoriumBlock.FACING, Direction.NORTH));
        helper.setBlock(tubePos, ModBlocks.ESSENTIA_TUBE.get());
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());

        ThaumatoriumBlockEntity machine = (ThaumatoriumBlockEntity)
                helper.getBlockEntity(machinePos);
        EssentiaTubeBlockEntity tube = (EssentiaTubeBlockEntity)
                helper.getBlockEntity(tubePos);
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getBlockEntity(jarPos);
        ServerPlayer player = fakePlayer(helper, "thaumatorium-refund");
        KnowledgeAccess.get(player).orElseThrow().completeResearch("nitor");
        helper.assertTrue(jar.addEssentia("potentia", 12, Direction.UP) == 12,
                "Test jar rejected its initial Potentia");
        helper.assertTrue(machine.insertCatalyst(
                        player, new ItemStack(Items.GLOWSTONE_DUST)),
                "Thaumatorium rejected the refund test catalyst");

        for (int tick = 0; tick < 160
                && machine.reservedEssentia().getOrDefault("potentia", 0) < 3;
                tick++) {
            ThaumatoriumBlockEntity.serverTick(
                    helper.getLevel(), helper.absolutePos(machinePos),
                    helper.getBlockState(machinePos), machine);
            EssentiaTubeBlockEntity.serverTick(
                    helper.getLevel(), helper.absolutePos(tubePos),
                    helper.getBlockState(tubePos), tube);
        }
        helper.assertTrue(
                machine.reservedEssentia().getOrDefault("potentia", 0) == 3,
                "Thaumatorium did not reserve Potentia before cancellation");

        ItemStack returnedCatalyst = machine.removeCatalyst();
        helper.assertTrue(returnedCatalyst.is(Items.GLOWSTONE_DUST),
                "Cancelling the craft did not return its catalyst");
        for (int tick = 0; tick < 320 && jar.amount() < 12; tick++) {
            EssentiaTubeBlockEntity.serverTick(
                    helper.getLevel(), helper.absolutePos(tubePos),
                    helper.getBlockState(tubePos), tube);
            EssentiaJarBlockEntity.serverTick(
                    helper.getLevel(), helper.absolutePos(jarPos),
                    helper.getBlockState(jarPos), jar);
        }
        helper.assertTrue(jar.amount() == 12
                        && machine.reservedEssentia().isEmpty(),
                "Cancelling the craft did not return all reserved Potentia to the jar");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mnemonicMatrixConnectsToBufferAndTubeWithoutMovingEssentia(
            GameTestHelper helper) {
        BlockPos matrixPos = new BlockPos(2, 2, 2);
        BlockPos neighbourPos = matrixPos.east();
        helper.setBlock(matrixPos, ModBlocks.MNEMONIC_MATRIX.get()
                .defaultBlockState().setValue(MnemonicMatrixBlock.FACING,
                        Direction.EAST));
        helper.setBlock(neighbourPos, ModBlocks.ESSENTIA_BUFFER.get());
        MnemonicMatrixBlockEntity matrix =
                (MnemonicMatrixBlockEntity) helper.getBlockEntity(matrixPos);
        EssentiaBufferBlockEntity buffer =
                (EssentiaBufferBlockEntity) helper.getBlockEntity(neighbourPos);

        helper.assertTrue(matrix.isConnectable(Direction.EAST)
                        && !matrix.isConnectable(Direction.WEST),
                "Mnemonic Matrix exposed a socket on the wrong face");
        helper.assertTrue(com.thaumcraftmodern.essentia.EssentiaConnections
                        .connected(helper.getLevel(), helper.absolutePos(neighbourPos),
                                Direction.WEST, buffer),
                "Essentia Buffer did not recognize the Mnemonic Matrix socket");
        helper.assertTrue(matrix.addEssentia("aer", 1, Direction.EAST) == 0
                        && matrix.takeEssentia("aer", 1, Direction.EAST) == 0
                        && matrix.essentiaAmount(Direction.EAST) == 0,
                "Mnemonic Matrix incorrectly became an essentia inventory");

        helper.setBlock(neighbourPos, ModBlocks.ESSENTIA_TUBE.get());
        EssentiaTubeBlock.refreshConnections(helper.getLevel(),
                helper.absolutePos(neighbourPos));
        helper.assertTrue(helper.getBlockState(neighbourPos)
                        .getValue(EssentiaTubeBlock.WEST),
                "Ordinary tube did not render its arm to the Mnemonic Matrix");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void mnemonicMatricesAddAndRemoveTwoFormulaSlots(GameTestHelper helper) {
        BlockPos machinePos = new BlockPos(3, 2, 3);
        BlockPos matrixPos = machinePos.east();
        helper.setBlock(machinePos, ModBlocks.THAUMATORIUM.get().defaultBlockState()
                .setValue(ThaumatoriumBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(ThaumatoriumBlock.FACING, Direction.NORTH));
        helper.setBlock(matrixPos, ModBlocks.MNEMONIC_MATRIX.get().defaultBlockState()
                .setValue(MnemonicMatrixBlock.FACING, Direction.WEST));
        ThaumatoriumBlockEntity machine = (ThaumatoriumBlockEntity)
                helper.getBlockEntity(machinePos);
        helper.assertTrue(machine.formulaCapacity() == 3,
                "One correctly facing Mnemonic Matrix did not add two slots");

        ServerPlayer player = fakePlayer(helper, "mnemonic-matrix-capacity");
        var recipes = com.thaumcraftmodern.crucible.CrucibleRecipeRegistry.all()
                .stream().limit(3).toList();
        helper.assertTrue(recipes.size() == 3,
                "Not enough crucible recipes to test matrix capacity");
        for (var recipe : recipes) {
            if (!recipe.research().isBlank()) {
                KnowledgeAccess.get(player).orElseThrow()
                        .completeResearch(recipe.research());
            }
            helper.assertTrue(machine.selectRecipe(player, recipe.id()),
                    "Mnemonic Matrix rejected a formula within capacity");
        }
        helper.assertTrue(machine.formulaCount() == 3,
                "Thaumatorium did not retain all Matrix formula slots");

        helper.setBlock(matrixPos, Blocks.AIR);
        for (int tick = 0; tick < 41; tick++) {
            ThaumatoriumBlockEntity.serverTick(helper.getLevel(),
                    helper.absolutePos(machinePos), helper.getBlockState(machinePos), machine);
        }
        helper.assertTrue(machine.formulaCapacity() == 1
                        && machine.formulaCount() == 1,
                "Removing Mnemonic Matrix did not trim formulae to base capacity");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void automatedEssentiaVerticalSurvivesNbtRoundTrip(GameTestHelper helper) {
        BlockPos bufferPos = new BlockPos(1, 1, 1);
        BlockPos jarPos = new BlockPos(2, 1, 1);
        BlockPos centrifugePos = new BlockPos(3, 1, 1);
        BlockPos crystalPos = new BlockPos(4, 1, 1);
        BlockPos thaumatoriumPos = new BlockPos(5, 1, 1);
        helper.setBlock(bufferPos, ModBlocks.ESSENTIA_BUFFER.get());
        helper.setBlock(jarPos, ModBlocks.VOID_JAR.get());
        helper.setBlock(centrifugePos, ModBlocks.ESSENTIA_CENTRIFUGE.get());
        helper.setBlock(crystalPos, ModBlocks.ESSENTIA_CRYSTALLIZER.get());
        helper.setBlock(thaumatoriumPos, ModBlocks.THAUMATORIUM.get());
        EssentiaBufferBlockEntity buffer = (EssentiaBufferBlockEntity) helper.getBlockEntity(bufferPos);
        VoidJarBlockEntity jar = (VoidJarBlockEntity) helper.getBlockEntity(jarPos);
        EssentiaCentrifugeBlockEntity centrifuge = (EssentiaCentrifugeBlockEntity) helper.getBlockEntity(centrifugePos);
        EssentiaCrystallizerBlockEntity crystal = (EssentiaCrystallizerBlockEntity) helper.getBlockEntity(crystalPos);
        ThaumatoriumBlockEntity thaumatorium = (ThaumatoriumBlockEntity) helper.getBlockEntity(thaumatoriumPos);
        for (int i = 0; i < EssentiaBufferBlockEntity.CAPACITY_PER_ASPECT; i++) {
            buffer.addEssentia("aer", 1, Direction.UP);
            buffer.addEssentia("ignis", 1, Direction.UP);
        }
        buffer.toggleSide(Direction.WEST);
        buffer.cycleChoke(Direction.EAST);
        jar.setFilter("aqua", Direction.SOUTH);
        jar.addEssentia("aqua", 72, Direction.UP);
        centrifuge.addEssentia("motus", 1, Direction.DOWN);
        crystal.addEssentia("ordo", 1, Direction.DOWN);
        ServerPlayer player = fakePlayer(helper, "automated-essentia-nbt");
        KnowledgeAccess.get(player).orElseThrow().completeResearch("nitor");
        thaumatorium.insertCatalyst(player, new ItemStack(Items.GLOWSTONE_DUST));
        thaumatorium.addEssentia("ignis", 2, Direction.WEST);

        EssentiaBufferBlockEntity restoredBuffer = new EssentiaBufferBlockEntity(
                helper.absolutePos(bufferPos), ModBlocks.ESSENTIA_BUFFER.get().defaultBlockState());
        VoidJarBlockEntity restoredJar = new VoidJarBlockEntity(
                helper.absolutePos(jarPos), ModBlocks.VOID_JAR.get().defaultBlockState());
        EssentiaCentrifugeBlockEntity restoredCentrifuge = new EssentiaCentrifugeBlockEntity(
                helper.absolutePos(centrifugePos), ModBlocks.ESSENTIA_CENTRIFUGE.get().defaultBlockState());
        EssentiaCrystallizerBlockEntity restoredCrystal = new EssentiaCrystallizerBlockEntity(
                helper.absolutePos(crystalPos), ModBlocks.ESSENTIA_CRYSTALLIZER.get().defaultBlockState());
        ThaumatoriumBlockEntity restoredThaumatorium = new ThaumatoriumBlockEntity(
                helper.absolutePos(thaumatoriumPos), ModBlocks.THAUMATORIUM.get().defaultBlockState());
        restoredBuffer.load(buffer.saveWithFullMetadata());
        restoredJar.load(jar.saveWithFullMetadata());
        restoredCentrifuge.load(centrifuge.saveWithFullMetadata());
        restoredCrystal.load(crystal.saveWithFullMetadata());
        restoredThaumatorium.load(thaumatorium.saveWithFullMetadata());
        helper.assertTrue(restoredBuffer.totalAmount() == 16
                        && restoredBuffer.contents().getOrDefault("aer", 0) == 8
                        && restoredBuffer.contents().getOrDefault("ignis", 0) == 8
                        && !restoredBuffer.sideOpen(Direction.WEST)
                        && restoredBuffer.chokeMode(Direction.EAST) == 1,
                "Buffer lost mixed contents or independent side state");
        helper.assertTrue(restoredJar.amount() == 64
                        && "aqua".equals(restoredJar.aspect())
                        && "aqua".equals(restoredJar.filter())
                        && restoredJar.filterFacing() == Direction.SOUTH,
                "Void jar lost capped contents, label or facing");
        helper.assertTrue("motus".equals(restoredCentrifuge.inputAspect())
                        && restoredCentrifuge.processTicks() == EssentiaCentrifugeBlockEntity.PROCESS_TICKS,
                "Centrifuge lost its in-flight compound aspect");
        helper.assertTrue("ordo".equals(restoredCrystal.aspect())
                        && restoredCrystal.progress() == 0,
                "Crystallizer lost its in-flight aspect");
        helper.assertTrue(restoredThaumatorium.selectedRecipe() != null
                        && restoredThaumatorium.catalyst().is(Items.GLOWSTONE_DUST)
                        && restoredThaumatorium.reservedEssentia().getOrDefault("ignis", 0) == 2,
                "Thaumatorium lost recipe, catalyst or reserved essentia");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void phialsAndJarLabelsFollowClassicTransactions(
            GameTestHelper helper
    ) {
        BlockPos jarPos = new BlockPos(1, 1, 1);
        BlockPos alembicPos = new BlockPos(3, 1, 1);
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());
        helper.setBlock(alembicPos, ModBlocks.ARCANE_ALEMBIC.get());
        EssentiaJarBlockEntity jar =
                (EssentiaJarBlockEntity) helper.getBlockEntity(jarPos);
        ArcaneAlembicBlockEntity alembic =
                (ArcaneAlembicBlockEntity) helper.getBlockEntity(alembicPos);
        jar.addEssentia("aer", 16, Direction.UP);
        alembic.acceptFromFurnace("ignis", 9);

        ServerPlayer player = fakePlayer(helper, "phial-label-transactions");
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.ESSENTIA_PHIAL.get()));
        helper.assertTrue(useOn(helper, player, jarPos).consumesAction(),
                "Empty phial did not extract from a warded jar");
        helper.assertTrue(jar.amount() == 8,
                "Warded jar extraction did not move exactly eight essentia");
        ItemStack filled = player.getInventory().items.stream()
                .filter(stack -> "aer".equals(
                        EssentiaPhialItem.aspect(stack).orElse(null)))
                .findFirst().orElse(ItemStack.EMPTY);
        helper.assertTrue(!filled.isEmpty(),
                "Jar extraction did not return a filled essentia phial");

        player.setItemInHand(InteractionHand.MAIN_HAND, filled);
        helper.assertTrue(useOn(helper, player, jarPos).consumesAction(),
                "Filled phial did not empty into a warded jar");
        helper.assertTrue(jar.amount() == 16,
                "Filled phial did not return exactly eight essentia");

        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.ESSENTIA_PHIAL.get()));
        helper.assertTrue(useOn(helper, player, alembicPos).consumesAction(),
                "Empty phial did not extract from an arcane alembic");
        helper.assertTrue(alembic.storedAmount() == 1,
                "Alembic extraction did not leave the exact one-point remainder");
        helper.assertTrue(alembic.comparatorSignal() == 1,
                "Alembic comparator did not report its one-point remainder");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        BlockPos absoluteAlembic = helper.absolutePos(alembicPos);
        BlockHitResult alembicHit = new BlockHitResult(
                Vec3.atCenterOf(absoluteAlembic), Direction.WEST,
                absoluteAlembic, false);
        helper.getLevel().getBlockState(absoluteAlembic).use(
                helper.getLevel(), player, InteractionHand.MAIN_HAND, alembicHit);
        helper.assertTrue(alembic.storedAmount() == 0
                        && alembic.storedAspect() == null
                        && alembic.comparatorSignal() == 0,
                "Sneak-empty did not clear the alembic and comparator output");
        player.setShiftKeyDown(false);

        ItemStack label = new ItemStack(ModItems.JAR_LABEL.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, label);
        player.setYRot(90.0F);
        BlockPos absoluteJar = helper.absolutePos(jarPos);
        BlockHitResult eastHit = new BlockHitResult(
                Vec3.atCenterOf(absoluteJar), Direction.EAST, absoluteJar, false);
        BlockHitResult westHit = new BlockHitResult(
                Vec3.atCenterOf(absoluteJar), Direction.WEST, absoluteJar, false);
        helper.getLevel().getBlockState(absoluteJar).use(helper.getLevel(),
                player, InteractionHand.MAIN_HAND, eastHit);
        helper.assertTrue("aer".equals(jar.filter())
                        && jar.filterFacing() == Direction.EAST && label.isEmpty(),
                "Jar label did not face the player and tune from the filled jar");

        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        helper.getLevel().getBlockState(absoluteJar).use(helper.getLevel(),
                player, InteractionHand.MAIN_HAND, westHit);
        helper.assertTrue(jar.amount() == 0 && "aer".equals(jar.filter()),
                "Wrong-side sneak click removed the label instead of emptying the jar");
        player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND,
                EssentiaPhialItem.filled(ModItems.ESSENTIA_PHIAL.get(), "aer"));
        helper.assertTrue(useOn(helper, player, jarPos).consumesAction()
                        && jar.amount() == 8,
                "Matching filled phial was not accepted by an empty labeled jar");
        player.setItemInHand(InteractionHand.MAIN_HAND,
                EssentiaPhialItem.filled(ModItems.ESSENTIA_PHIAL.get(), "ignis"));
        helper.assertTrue(!useOn(helper, player, jarPos).consumesAction()
                        && jar.amount() == 8 && "aer".equals(jar.aspect()),
                "Labeled jar accepted a mismatched aspect");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.setShiftKeyDown(true);
        helper.getLevel().getBlockState(absoluteJar).use(helper.getLevel(),
                player, InteractionHand.MAIN_HAND, eastHit);
        helper.assertTrue(jar.filter() == null && jar.amount() == 8,
                "Filter-side sneak click did not remove only the jar label");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                        new net.minecraft.world.phys.AABB(absoluteJar).inflate(1.0D))
                        .stream().anyMatch(entity -> entity.getItem().is(
                                ModItems.JAR_LABEL.get())),
                "Removing a jar label did not drop the original label item");

        player.setShiftKeyDown(false);
        ItemStack shard = new ItemStack(ModItems.AIR_SHARD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, shard);
        helper.getLevel().getBlockState(absoluteJar).use(helper.getLevel(),
                player, InteractionHand.MAIN_HAND, eastHit);
        helper.assertTrue(jar.filter() == null && shard.getCount() == 1,
                "Aspect-bearing non-label item was consumed as a jar filter");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void labeledJarPullsOnlyItsAspectFromTheTubeAbove(
            GameTestHelper helper) {
        BlockPos jarPos = new BlockPos(2, 1, 2);
        BlockPos tubePos = jarPos.above();
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());
        helper.setBlock(tubePos, ModBlocks.ESSENTIA_TUBE.get());
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getBlockEntity(jarPos);
        EssentiaTubeBlockEntity tube = (EssentiaTubeBlockEntity)
                helper.getBlockEntity(tubePos);
        jar.setFilter("aer", Direction.EAST);
        helper.assertTrue(tube.addEssentia("aer", 1, Direction.UP) == 1,
                "Test tube rejected matching essentia");
        for (int tick = 0; tick < 5; tick++) {
            EssentiaJarBlockEntity.serverTick(helper.getLevel(),
                    helper.absolutePos(jarPos), helper.getBlockState(jarPos), jar);
        }
        helper.assertTrue(jar.amount() == 1 && "aer".equals(jar.aspect())
                        && tube.essentiaAmount(Direction.DOWN) == 0,
                "Labeled jar did not pull its matching aspect from above");

        helper.assertTrue(tube.addEssentia("ignis", 1, Direction.UP) == 1,
                "Test tube rejected mismatched essentia setup");
        for (int tick = 0; tick < 5; tick++) {
            EssentiaJarBlockEntity.serverTick(helper.getLevel(),
                    helper.absolutePos(jarPos), helper.getBlockState(jarPos), jar);
        }
        helper.assertTrue(jar.amount() == 1 && "aer".equals(jar.aspect())
                        && "ignis".equals(tube.essentiaType(Direction.DOWN)),
                "Labeled jar pulled an aspect that contradicts its label");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeFilledPhialAddsEightOnEveryClick(
            GameTestHelper helper
    ) {
        BlockPos jarPos = new BlockPos(1, 1, 1);
        BlockPos voidJarPos = new BlockPos(3, 1, 1);
        helper.setBlock(jarPos, ModBlocks.WARDED_JAR.get());
        helper.setBlock(voidJarPos, ModBlocks.VOID_JAR.get());
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getBlockEntity(jarPos);
        VoidJarBlockEntity voidJar = (VoidJarBlockEntity)
                helper.getBlockEntity(voidJarPos);

        ServerPlayer player = fakePlayer(helper, "creative-phial-repeated-fill");
        player.getAbilities().instabuild = true;
        player.onUpdateAbilities();
        player.setItemInHand(
                InteractionHand.MAIN_HAND,
                EssentiaPhialItem.filled(ModItems.ESSENTIA_PHIAL.get(), "aer")
        );

        helper.assertTrue(useOn(helper, player, jarPos).consumesAction(),
                "First creative phial click was rejected");
        helper.assertTrue(useOn(helper, player, jarPos).consumesAction(),
                "Second creative phial click was rejected");
        helper.assertTrue(jar.amount() == 16,
                "Creative filled phial did not add eight essentia per click");
        helper.assertTrue("aer".equals(EssentiaPhialItem.aspect(
                        player.getMainHandItem()).orElse(null)),
                "Creative filled phial was consumed after one click");

        voidJar.addEssentia("aer", 64, Direction.UP);
        helper.assertTrue(useOn(helper, player, voidJarPos).consumesAction(),
                "Full Void Jar rejected matching phial overflow");
        helper.assertTrue(voidJar.amount() == 64,
                "Void Jar exposed destroyed phial overflow above its cap");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void brokenWardedJarPreservesExactContentsAndPlacesThemBack(
            GameTestHelper helper
    ) {
        helper.assertTrue(new ItemStack(ModItems.WARDED_JAR.get())
                        .getMaxStackSize() == 64,
                "Empty warded jars did not stack to 64");
        helper.assertTrue(new ItemStack(ModItems.VOID_JAR.get())
                        .getMaxStackSize() == 64,
                "Empty void jars did not stack to 64");
        helper.assertTrue(new ItemStack(ModItems.FILLED_WARDED_JAR.get())
                        .getMaxStackSize() == 1,
                "Filled warded jar item was stackable");
        helper.assertTrue(new ItemStack(ModItems.FILLED_VOID_JAR.get())
                        .getMaxStackSize() == 1,
                "Filled void jar item was stackable");
        BlockPos jarPosition = new BlockPos(1, 1, 1);
        helper.setBlock(jarPosition, ModBlocks.WARDED_JAR.get());
        EssentiaJarBlockEntity jar = (EssentiaJarBlockEntity)
                helper.getBlockEntity(jarPosition);
        jar.setFilter("ignis", Direction.WEST);
        helper.assertTrue(jar.addEssentia("ignis", 37, Direction.UP) == 37,
                "Warded jar fixture rejected exact essentia amount");

        helper.getLevel().destroyBlock(helper.absolutePos(jarPosition), true);
        ItemStack dropped = helper.getEntities(EntityType.ITEM, jarPosition, 2.0D)
                .stream().map(ItemEntity::getItem)
                .filter(stack -> stack.is(ModItems.FILLED_WARDED_JAR.get()))
                .findFirst().orElseThrow();
        helper.assertTrue(dropped.getMaxStackSize() == 1,
                "Filled warded jar remained stackable");
        var contents = WardedJarItem.contents(dropped).orElseThrow();
        helper.assertTrue("ignis".equals(contents.aspect())
                        && contents.amount() == 37,
                "Broken warded jar changed or lost its exact contents");
        helper.assertTrue("ignis".equals(contents.filter()),
                "Broken warded jar lost its aspect label");

        BlockPos support = new BlockPos(4, 0, 1);
        BlockPos restoredPosition = support.above();
        helper.setBlock(support, Blocks.STONE);
        ServerPlayer player = fakePlayer(helper, "warded-jar-item-round-trip");
        player.setItemInHand(InteractionHand.MAIN_HAND, dropped.copy());
        helper.assertTrue(useOn(helper, player, support).consumesAction(),
                "Filled warded jar item could not be placed");
        helper.assertBlockPresent(ModBlocks.WARDED_JAR.get(), restoredPosition);
        EssentiaJarBlockEntity restored = (EssentiaJarBlockEntity)
                helper.getBlockEntity(restoredPosition);
        helper.assertTrue("ignis".equals(restored.aspect())
                        && restored.amount() == 37,
                "Placed warded jar did not restore the exact essentia amount");
        helper.assertTrue("ignis".equals(restored.filter()),
                "Placed warded jar did not restore its aspect label");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disconnectedTubeBranchRemainsSelectableAndRetoggles(
            GameTestHelper helper
    ) {
        BlockPos leftPos = new BlockPos(1, 1, 1);
        BlockPos rightPos = leftPos.east();
        BlockPos isolatedPos = new BlockPos(1, 1, 3);
        helper.setBlock(leftPos, ModBlocks.ESSENTIA_TUBE.get());
        helper.setBlock(rightPos, ModBlocks.ESSENTIA_TUBE.get());
        helper.setBlock(isolatedPos, ModBlocks.ESSENTIA_TUBE.get());
        EssentiaTubeBlockEntity left =
                (EssentiaTubeBlockEntity) helper.getBlockEntity(leftPos);
        EssentiaTubeBlockEntity right =
                (EssentiaTubeBlockEntity) helper.getBlockEntity(rightPos);

        for (int toggle = 0; toggle < 6; toggle++) {
            left.toggleSide(Direction.EAST);
            boolean expectedOpen = toggle % 2 != 0;
            helper.assertTrue(left.isSideOpen(Direction.EAST) == expectedOpen,
                    "Local tube side failed toggle " + toggle);
            helper.assertTrue(right.isSideOpen(Direction.WEST) == expectedOpen,
                    "Remote tube side desynchronised on toggle " + toggle);
            net.minecraft.world.phys.shapes.VoxelShape outline =
                    helper.getLevel().getBlockState(helper.absolutePos(leftPos))
                            .getShape(helper.getLevel(), helper.absolutePos(leftPos));
            helper.assertTrue(outline.min(Direction.Axis.X) == 0.375D
                            && outline.max(Direction.Axis.X) == 1.0D,
                    "Retracted branch toward the adjacent tube is not selectable");
            helper.assertTrue(outline.min(Direction.Axis.Y) == 0.375D
                            && outline.max(Direction.Axis.Y) == 0.625D
                            && outline.min(Direction.Axis.Z) == 0.375D
                            && outline.max(Direction.Axis.Z) == 0.625D,
                    "Selection outline leaked into directions without adjacent tubes");
        }
        net.minecraft.world.phys.shapes.VoxelShape isolatedOutline =
                helper.getLevel().getBlockState(helper.absolutePos(isolatedPos))
                        .getShape(helper.getLevel(), helper.absolutePos(isolatedPos));
        for (Direction.Axis axis : Direction.Axis.values()) {
            helper.assertTrue(isolatedOutline.min(axis) == 0.375D
                            && isolatedOutline.max(axis) == 0.625D,
                    "Isolated tube exposed a phantom " + axis + " branch");
        }

        BlockPos endpointTubePos = new BlockPos(4, 1, 3);
        BlockPos alembicPos = endpointTubePos.east();
        helper.setBlock(endpointTubePos, ModBlocks.ESSENTIA_TUBE.get());
        helper.setBlock(alembicPos, ModBlocks.ARCANE_ALEMBIC.get());
        EssentiaTubeBlockEntity endpointTube = (EssentiaTubeBlockEntity)
                helper.getBlockEntity(endpointTubePos);
        endpointTube.toggleSide(Direction.EAST);
        net.minecraft.world.phys.shapes.VoxelShape endpointOutline =
                helper.getLevel().getBlockState(helper.absolutePos(endpointTubePos))
                        .getShape(helper.getLevel(), helper.absolutePos(endpointTubePos));
        helper.assertTrue(endpointOutline.max(Direction.Axis.X) == 1.0D,
                "Retracted branch toward the adjacent alembic is not selectable");
        BlockPos absoluteEndpointTube = helper.absolutePos(endpointTubePos);
        Vec3 endpointEye = new Vec3(
                absoluteEndpointTube.getX() - 2.0D,
                absoluteEndpointTube.getY() + 0.5D,
                absoluteEndpointTube.getZ() + 0.5D
        );
        Direction endpointReconnect = EssentiaTubeBlock.selectReconnectDirection(
                helper.getLevel().getBlockState(absoluteEndpointTube),
                helper.getLevel(), absoluteEndpointTube, endpointEye,
                endpointEye.add(16.0D, 0.0D, 0.0D));
        helper.assertTrue(endpointReconnect == Direction.EAST,
                "View ray could not retarget the closed alembic branch");
        endpointTube.toggleSide(endpointReconnect);
        helper.assertTrue(endpointTube.isSideOpen(Direction.EAST),
                "Closed alembic branch did not reopen");

        BlockPos upperPos = leftPos.above();
        helper.setBlock(upperPos, ModBlocks.ESSENTIA_TUBE.get());
        left.toggleSide(Direction.EAST);
        left.toggleSide(Direction.UP);
        BlockPos absoluteLeft = helper.absolutePos(leftPos);
        Vec3 westEye = new Vec3(
                absoluteLeft.getX() - 2.0D,
                absoluteLeft.getY() + 0.5D,
                absoluteLeft.getZ() + 0.5D
        );
        Direction viewedReconnect = EssentiaTubeBlock
                .selectReconnectDirection(
                        helper.getLevel().getBlockState(absoluteLeft),
                        helper.getLevel(), absoluteLeft, westEye,
                        westEye.add(16.0D, 0.0D, 0.0D));
        helper.assertTrue(viewedReconnect == Direction.EAST,
                "View ray selected an off-ray reconnect branch instead of east");
        helper.succeed();
    }

    private static InteractionResult useOn(
            GameTestHelper helper,
            ServerPlayer player,
            BlockPos relativePosition
    ) {
        BlockPos absolutePosition = helper.absolutePos(relativePosition);
        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(absolutePosition),
                Direction.UP,
                absolutePosition,
                false
        );
        return player.getMainHandItem().useOn(
                new UseOnContext(player, InteractionHand.MAIN_HAND, hit)
        );
    }

    private static ServerPlayer fakePlayer(GameTestHelper helper, String testId) {
        UUID id = UUID.nameUUIDFromBytes(
                ("thaumic_reborn:gametest:" + testId)
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        return FakePlayerFactory.get(
                helper.getLevel(),
                new GameProfile(id, "tcm-" + testId)
        );
    }
}
