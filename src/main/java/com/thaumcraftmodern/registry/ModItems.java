package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.AspectShardItem;
import com.thaumcraftmodern.item.AlumentumItem;
import com.thaumcraftmodern.item.CrimsonRitesItem;
import com.thaumcraftmodern.item.CultistArmorItem;
import com.thaumcraftmodern.item.WingedMantleArmorItem;
import com.thaumcraftmodern.item.CrystalClusterItem;
import com.thaumcraftmodern.item.EldritchCrystalItem;
import com.thaumcraftmodern.crystal.CrystalClusterVariant;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.item.DeconstructionTableItem;
import com.thaumcraftmodern.item.ArcaneBellowsItem;
import com.thaumcraftmodern.item.ManaBeanItem;
import com.thaumcraftmodern.item.MirrorBlockItem;
import com.thaumcraftmodern.item.HandMirrorItem;
import com.thaumcraftmodern.item.FortressArmorItem;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.EssentiaPhialItem;
import com.thaumcraftmodern.item.EssentiaReservoirItem;
import com.thaumcraftmodern.item.EssentiaResonatorItem;
import com.thaumcraftmodern.item.EssentiaCrystalItem;
import com.thaumcraftmodern.item.FluxGooBlockItem;
import com.thaumcraftmodern.item.GogglesOfRevealingItem;
import com.thaumcraftmodern.item.JarredAuraNodeItem;
import com.thaumcraftmodern.item.JarLabelItem;
import com.thaumcraftmodern.item.LootBagItem;
import com.thaumcraftmodern.item.ResearchNotesItem;
import com.thaumcraftmodern.item.KnowledgeFragmentItem;
import com.thaumcraftmodern.item.ScribingToolsItem;
import com.thaumcraftmodern.item.SanityCheckerItem;
import com.thaumcraftmodern.item.SanitySoapItem;
import com.thaumcraftmodern.item.BathSaltsItem;
import com.thaumcraftmodern.item.BottledTaintItem;
import com.thaumcraftmodern.item.BoneBowItem;
import com.thaumcraftmodern.item.PrimalArrowItem;
import com.thaumcraftmodern.item.FocusPouchItem;
import com.thaumcraftmodern.item.LiquidDeathBucketItem;
import com.thaumcraftmodern.item.TripleMeatTreatItem;
import com.thaumcraftmodern.item.ThaumometerItem;
import com.thaumcraftmodern.item.ThaumonomiconItem;
import com.thaumcraftmodern.item.ThaumaturgeRobeItem;
import com.thaumcraftmodern.item.ThaumiumArmorItem;
import com.thaumcraftmodern.item.ThaumiumHoeItem;
import com.thaumcraftmodern.item.ThaumiumTier;
import com.thaumcraftmodern.item.BootsTravellerItem;
import com.thaumcraftmodern.item.ElementalAxeItem;
import com.thaumcraftmodern.item.ElementalHoeItem;
import com.thaumcraftmodern.item.ElementalPickaxeItem;
import com.thaumcraftmodern.item.ElementalShovelItem;
import com.thaumcraftmodern.item.ElementalSwordItem;
import com.thaumcraftmodern.item.PrimalCrusherItem;
import com.thaumcraftmodern.item.PrimordialPearlItem;
import com.thaumcraftmodern.item.EldritchEyeItem;
import com.thaumcraftmodern.item.TaintedMaterialItem;
import com.thaumcraftmodern.item.VoidArmorItem;
import com.thaumcraftmodern.item.VoidRobeArmorItem;
import com.thaumcraftmodern.item.VoidSwordItem;
import com.thaumcraftmodern.item.VoidPickaxeItem;
import com.thaumcraftmodern.item.VoidAxeItem;
import com.thaumcraftmodern.item.VoidShovelItem;
import com.thaumcraftmodern.item.VoidHoeItem;
import com.thaumcraftmodern.item.SinisterLodestoneItem;
import com.thaumcraftmodern.item.FluxScrubberItem;
import com.thaumcraftmodern.item.ArcaneDoorItem;
import com.thaumcraftmodern.item.ArcaneDoorKeyItem;
import com.thaumcraftmodern.item.BrainJarItem;
import com.thaumcraftmodern.item.AspectRingItem;
import com.thaumcraftmodern.item.StrawGolemItem;
import com.thaumcraftmodern.item.ClassicGolemItem;
import com.thaumcraftmodern.item.GolemCoreItem;
import com.thaumcraftmodern.item.GolemUpgradeItem;
import com.thaumcraftmodern.item.GolemDecorationItem;
import com.thaumcraftmodern.item.HungryChestItem;
import com.thaumcraftmodern.item.TravelingTrunkItem;
import com.thaumcraftmodern.item.CurioAccessoryItem;
import com.thaumcraftmodern.item.VisStorageItem;
import com.thaumcraftmodern.item.RunicAccessoryItem;
import com.thaumcraftmodern.item.WandComponentItem;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.item.ArcaneBoreItem;
import com.thaumcraftmodern.item.WandFocusItem;
import com.thaumcraftmodern.focus.WandFocusType;
import com.thaumcraftmodern.item.WardedJarItem;
import com.thaumcraftmodern.item.VisDeviceBlockItem;
import com.thaumcraftmodern.entity.LegacyMobKind;
import com.thaumcraftmodern.entity.GolemMaterial;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.GolemUpgradeType;
import com.thaumcraftmodern.entity.GolemDecorationType;
import com.thaumcraftmodern.entity.PrimalArrowType;
import com.thaumcraftmodern.wand.WandForm;
import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ThaumcraftModern.MOD_ID);
    public static final Map<LegacyMobKind, RegistryObject<Item>> SPAWN_EGGS =
            registerSpawnEggs();
    public static final RegistryObject<Item> FACELESS_WITNESS_SPAWN_EGG =
            ITEMS.register(
                    "faceless_witness_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.FACELESS_WITNESS,
                            0x111019,
                            0x845BB2,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> THAUMONOMICON =
            ITEMS.register("thaumonomicon", () -> new ThaumonomiconItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> THAUMOMETER =
            ITEMS.register("thaumometer", () -> new ThaumometerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> SANITY_CHECKER =
            ITEMS.register(
                    "sanity_checker",
                    () -> new SanityCheckerItem(
                            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    )
            );
    public static final RegistryObject<Item> SANITY_SOAP =
            ITEMS.register(
                    "sanity_soap",
                    () -> new SanitySoapItem(new Item.Properties())
            );
    public static final RegistryObject<Item> BATH_SALTS =
            ITEMS.register("bath_salts", () -> new BathSaltsItem(new Item.Properties()));
    public static final RegistryObject<Item> BOTTLED_TAINT =
            ITEMS.register("bottled_taint", () -> new BottledTaintItem(
                    new Item.Properties().stacksTo(8)));
    public static final RegistryObject<Item> LIQUID_DEATH_BUCKET =
            ITEMS.register("liquid_death_bucket", () -> new LiquidDeathBucketItem(
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> PURIFYING_FLUID_BUCKET =
            ITEMS.register(
                    "purifying_fluid_bucket",
                    () -> new BucketItem(
                            ModFluids.PURIFYING_SOURCE,
                            new Item.Properties().stacksTo(1)
                                    .craftRemainder(Items.BUCKET)
                    )
            );
    public static final RegistryObject<Item> SCRIBING_TOOLS =
            ITEMS.register("scribing_tools", () -> new ScribingToolsItem(new Item.Properties().stacksTo(1).durability(100)));
    public static final RegistryObject<Item> RESEARCH_NOTES =
            ITEMS.register("research_notes", () -> new ResearchNotesItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> DISCOVERY =
            ITEMS.register("discovery", () -> new DiscoveryItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> KNOWLEDGE_FRAGMENT =
            ITEMS.register(
                    "knowledge_fragment",
                    () -> new KnowledgeFragmentItem(new Item.Properties())
            );
    public static final RegistryObject<Item> ETHEREAL_ESSENCE =
            ITEMS.register(
                    "ethereal_essence",
                    () -> new EtherealEssenceItem(new Item.Properties())
            );
    public static final RegistryObject<Item> QUICKSILVER =
            ITEMS.register("quicksilver", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> QUICKSILVER_NUGGET =
            ITEMS.register(
                    "quicksilver_nugget",
                    () -> new Item(new Item.Properties())
            );
    public static final RegistryObject<Item> COPPER_NUGGET = simple("copper_nugget");
    public static final RegistryObject<Item> TIN_NUGGET = simple("tin_nugget");
    public static final RegistryObject<Item> LEAD_NUGGET = simple("lead_nugget");
    public static final RegistryObject<Item> TIN_INGOT = simple("tin_ingot");
    public static final RegistryObject<Item> SILVER_INGOT = simple("silver_ingot");
    public static final RegistryObject<Item> LEAD_INGOT = simple("lead_ingot");
    public static final RegistryObject<Item> NATIVE_IRON_CLUSTER = simple("native_iron_cluster");
    public static final RegistryObject<Item> NATIVE_GOLD_CLUSTER = simple("native_gold_cluster");
    public static final RegistryObject<Item> NATIVE_CINNABAR_CLUSTER = simple("native_cinnabar_cluster");
    public static final RegistryObject<Item> NATIVE_COPPER_CLUSTER = simple("native_copper_cluster");
    public static final RegistryObject<Item> NATIVE_TIN_CLUSTER = simple("native_tin_cluster");
    public static final RegistryObject<Item> NATIVE_SILVER_CLUSTER = simple("native_silver_cluster");
    public static final RegistryObject<Item> NATIVE_LEAD_CLUSTER = simple("native_lead_cluster");
    public static final RegistryObject<Item> TRIPLE_MEAT_TREAT =
            ITEMS.register(
                    "triple_meat_treat",
                    () -> new TripleMeatTreatItem(
                            new Item.Properties().stacksTo(16)
                                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                                            .nutrition(6)
                                            .saturationMod(0.8F)
                                            .meat()
                                            .build())
                    )
            );
    public static final RegistryObject<Item> THAUMIC_TALLOW = simple("thaumic_tallow");
    public static final RegistryObject<Item> TALLOW_CANDLE =
            blockItem("tallow_candle", ModBlocks.TALLOW_CANDLE);
    public static final RegistryObject<Item> AMBER =
            ITEMS.register("amber", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MANA_BEAN =
            ITEMS.register(
                    "mana_bean",
                    () -> new ManaBeanItem(new Item.Properties())
            );
    public static final RegistryObject<Item> ZOMBIE_BRAIN =
            ITEMS.register(
                    "zombie_brain",
                    () -> new Item(
                            new Item.Properties()
                                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                                            .nutrition(1)
                                            .saturationMod(0.1F)
                                            .meat()
                                            .build())
                    )
            );
    public static final RegistryObject<Item> TAINTED_GOO =
            ITEMS.register(
                    "tainted_goo",
                    () -> new TaintedMaterialItem(new Item.Properties())
            );
    public static final RegistryObject<Item> TAINT_TENDRIL =
            ITEMS.register(
                    "taint_tendril",
                    () -> new TaintedMaterialItem(new Item.Properties())
            );
    public static final RegistryObject<Item> GOLD_COIN =
            ITEMS.register("gold_coin", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> VOID_SEED =
            ITEMS.register("void_seed", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELDRITCH_EYE =
            ITEMS.register(
                    "eldritch_eye",
                    () -> new EldritchEyeItem(
                            new Item.Properties().rarity(Rarity.UNCOMMON)
                    )
            );
    public static final RegistryObject<Item> CRIMSON_RITES =
            ITEMS.register(
                    "crimson_rites",
                    () -> new CrimsonRitesItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.UNCOMMON)
                    )
            );
    public static final RegistryObject<Item> RUNED_TABLET =
            ITEMS.register(
                    "runed_tablet",
                    () -> new Item(new Item.Properties().rarity(Rarity.RARE))
            );
    public static final RegistryObject<Item> PRIMORDIAL_PEARL =
            ITEMS.register(
                    "primordial_pearl",
                    () -> new PrimordialPearlItem(
                            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
                    )
            );
    public static final RegistryObject<Item> MUNDANE_AMULET = ITEMS.register(
            "mundane_amulet", () -> new CurioAccessoryItem(
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MUNDANE_RING = ITEMS.register(
            "mundane_ring", () -> new CurioAccessoryItem(
                    new Item.Properties().stacksTo(1)));
    public static final Map<PrimalAspect, RegistryObject<Item>> ASPECT_RINGS =
            registerAspectRings();
    public static final RegistryObject<Item> VIS_STONE = ITEMS.register(
            "vis_stone", () -> new VisStorageItem(25,
                    new Item.Properties().stacksTo(1)
                            .rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VIS_STORAGE_AMULET = ITEMS.register(
            "vis_storage_amulet", () -> new VisStorageItem(250,
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> RUNIC_RING_LESSER = runicAccessory(
            "runic_ring_lesser", 1, RunicAccessoryItem.Upgrade.NONE, Rarity.UNCOMMON);
    public static final RegistryObject<Item> RUNIC_RING = runicAccessory(
            "runic_ring", 5, RunicAccessoryItem.Upgrade.NONE, Rarity.RARE);
    public static final RegistryObject<Item> RUNIC_RING_CHARGED = runicAccessory(
            "runic_ring_charged", 4, RunicAccessoryItem.Upgrade.CHARGED, Rarity.RARE);
    public static final RegistryObject<Item> RUNIC_RING_REGEN = runicAccessory(
            "runic_ring_regen", 4, RunicAccessoryItem.Upgrade.HEALING, Rarity.RARE);
    public static final RegistryObject<Item> RUNIC_AMULET = runicAccessory(
            "runic_amulet", 8, RunicAccessoryItem.Upgrade.NONE, Rarity.RARE);
    public static final RegistryObject<Item> RUNIC_AMULET_EMERGENCY = runicAccessory(
            "runic_amulet_emergency", 7, RunicAccessoryItem.Upgrade.EMERGENCY, Rarity.RARE);
    public static final RegistryObject<Item> RUNIC_GIRDLE = runicAccessory(
            "runic_girdle", 10, RunicAccessoryItem.Upgrade.NONE, Rarity.RARE);
    public static final RegistryObject<Item> RUNIC_GIRDLE_KINETIC = runicAccessory(
            "runic_girdle_kinetic", 9, RunicAccessoryItem.Upgrade.KINETIC, Rarity.RARE);
    public static final RegistryObject<Item> THAUMIUM_SWORD = ITEMS.register(
            "thaumium_sword", () -> new SwordItem(ThaumiumTier.INSTANCE, 3, -2.4F,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THAUMIUM_PICKAXE = ITEMS.register(
            "thaumium_pickaxe", () -> new PickaxeItem(ThaumiumTier.INSTANCE, 1, -2.8F,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THAUMIUM_AXE = ITEMS.register(
            "thaumium_axe", () -> new AxeItem(ThaumiumTier.INSTANCE, 5.5F, -3.2F,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THAUMIUM_SHOVEL = ITEMS.register(
            "thaumium_shovel", () -> new ShovelItem(ThaumiumTier.INSTANCE, 1.5F, -3.0F,
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THAUMIUM_HOE = ITEMS.register(
            "thaumium_hoe", () -> new ThaumiumHoeItem(
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> THAUMIUM_HELMET = thaumiumArmor(
            "thaumium_helmet", ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> THAUMIUM_CHESTPLATE = thaumiumArmor(
            "thaumium_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> THAUMIUM_LEGGINGS = thaumiumArmor(
            "thaumium_leggings", ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> THAUMIUM_BOOTS = thaumiumArmor(
            "thaumium_boots", ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> VOID_SWORD = ITEMS.register(
            "void_sword", () -> new VoidSwordItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VOID_PICKAXE = ITEMS.register(
            "void_pickaxe", () -> new VoidPickaxeItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VOID_AXE = ITEMS.register(
            "void_axe", () -> new VoidAxeItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VOID_SHOVEL = ITEMS.register(
            "void_shovel", () -> new VoidShovelItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VOID_HOE = ITEMS.register(
            "void_hoe", () -> new VoidHoeItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> VOID_HELMET = voidArmor(
            "void_helmet", ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> VOID_CHESTPLATE = voidArmor(
            "void_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> VOID_LEGGINGS = voidArmor(
            "void_leggings", ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> VOID_BOOTS = voidArmor(
            "void_boots", ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PRIMAL_CRUSHER = ITEMS.register(
            "primal_crusher",
            () -> new PrimalCrusherItem(
                    new Item.Properties().rarity(Rarity.EPIC)
            )
    );
    public static final RegistryObject<Item> VOID_ROBE_HOOD = voidRobeArmor(
            "void_robe_hood", ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> VOID_ROBE_CHESTPLATE = voidRobeArmor(
            "void_robe_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> VOID_ROBE_LEGGINGS = voidRobeArmor(
            "void_robe_leggings", ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> FORTRESS_HELMET = ITEMS.register(
            "fortress_helmet", () -> new FortressArmorItem(ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FORTRESS_HELMET_MASK_GRINNING_DEVIL =
            ITEMS.register("fortress_helmet_mask_grinning_devil",
                    () -> new FortressArmorItem(ArmorItem.Type.HELMET, 0,
                            new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FORTRESS_HELMET_MASK_ANGRY_GHOST =
            ITEMS.register("fortress_helmet_mask_angry_ghost",
                    () -> new FortressArmorItem(ArmorItem.Type.HELMET, 1,
                            new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FORTRESS_HELMET_MASK_SIPPING_FIEND =
            ITEMS.register("fortress_helmet_mask_sipping_fiend",
                    () -> new FortressArmorItem(ArmorItem.Type.HELMET, 2,
                            new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FORTRESS_CHESTPLATE = ITEMS.register(
            "fortress_chestplate", () -> new FortressArmorItem(ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FORTRESS_LEGGINGS = ITEMS.register(
            "fortress_leggings", () -> new FortressArmorItem(ArmorItem.Type.LEGGINGS,
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> FORTRESS_BOOTS = ITEMS.register(
            "fortress_boots", () -> new FortressArmorItem(ArmorItem.Type.BOOTS,
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> PICKAXE_OF_THE_CORE = ITEMS.register(
            "pickaxe_of_the_core", () -> new ElementalPickaxeItem(
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> AXE_OF_THE_STREAM = ITEMS.register(
            "axe_of_the_stream", () -> new ElementalAxeItem(
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SWORD_OF_THE_ZEPHYR = ITEMS.register(
            "sword_of_the_zephyr", () -> new ElementalSwordItem(
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> SHOVEL_OF_THE_EARTHMOVER = ITEMS.register(
            "shovel_of_the_earthmover", () -> new ElementalShovelItem(
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> HOE_OF_GROWTH = ITEMS.register(
            "hoe_of_growth", () -> new ElementalHoeItem(
                    new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> BOOTS_OF_THE_TRAVELLER = ITEMS.register(
            "boots_of_the_traveller", () -> new BootsTravellerItem(
                    new Item.Properties().stacksTo(1).durability(350).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> CULTIST_KNIGHT_HELMET = cultistArmor(
            "cultist_knight_helmet", CultistArmorItem.Set.KNIGHT, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CULTIST_KNIGHT_CHESTPLATE = cultistArmor(
            "cultist_knight_chestplate", CultistArmorItem.Set.KNIGHT, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CULTIST_KNIGHT_LEGGINGS = cultistArmor(
            "cultist_knight_leggings", CultistArmorItem.Set.KNIGHT, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> CULTIST_CLERIC_HOOD = cultistArmor(
            "cultist_cleric_hood", CultistArmorItem.Set.CLERIC, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CULTIST_CLERIC_ROBE = cultistArmor(
            "cultist_cleric_robe", CultistArmorItem.Set.CLERIC, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CULTIST_CLERIC_LEGGINGS = cultistArmor(
            "cultist_cleric_leggings", CultistArmorItem.Set.CLERIC, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> CULTIST_PRAETOR_HELMET = cultistArmor(
            "cultist_praetor_helmet", CultistArmorItem.Set.PRAETOR, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> CULTIST_PRAETOR_CHESTPLATE = cultistArmor(
            "cultist_praetor_chestplate", CultistArmorItem.Set.PRAETOR, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> CULTIST_PRAETOR_LEGGINGS = cultistArmor(
            "cultist_praetor_leggings", CultistArmorItem.Set.PRAETOR, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> CULTIST_BOOTS = cultistArmor(
            "cultist_boots", CultistArmorItem.Set.BOOTS, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> WINGED_MANTLE_HOOD = wingedMantleArmor(
            "winged_mantle_hood", ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> WINGED_MANTLE_CHESTPLATE = wingedMantleArmor(
            "winged_mantle_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> WINGED_MANTLE_LEGGINGS = wingedMantleArmor(
            "winged_mantle_leggings", ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> WINGED_MANTLE_BOOTS = wingedMantleArmor(
            "winged_mantle_boots", ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> COMMON_LOOT_BAG =
            lootBag("common_loot_bag", "common", Rarity.COMMON);
    public static final RegistryObject<Item> UNCOMMON_LOOT_BAG =
            lootBag("uncommon_loot_bag", "uncommon", Rarity.UNCOMMON);
    public static final RegistryObject<Item> RARE_LOOT_BAG =
            lootBag("rare_loot_bag", "rare", Rarity.RARE);

    public static final RegistryObject<Item> IRON_WAND_CAP =
            wandCap("iron");
    public static final RegistryObject<Item> GOLD_WAND_CAP = wandCap("gold");
    public static final RegistryObject<Item> THAUMIUM_WAND_CAP =
            wandCap("thaumium");
    public static final RegistryObject<Item> VOID_WAND_CAP = wandCap("void");
    public static final RegistryObject<Item> COPPER_WAND_CAP =
            wandCap("copper");
    public static final RegistryObject<Item> SILVER_WAND_CAP =
            wandCap("silver");

    public static final RegistryObject<Item> WOODEN_WAND_ROD =
            wandRod("wood", "wooden_wand_rod");
    public static final RegistryObject<Item> GREATWOOD_WAND_ROD =
            wandRod("greatwood");
    public static final RegistryObject<Item> SILVERWOOD_WAND_ROD =
            wandRod("silverwood");
    public static final RegistryObject<Item> OBSIDIAN_WAND_ROD =
            wandRod("obsidian");
    public static final RegistryObject<Item> BLAZE_WAND_ROD =
            wandRod("blaze");
    public static final RegistryObject<Item> ICE_WAND_ROD = wandRod("ice");
    public static final RegistryObject<Item> QUARTZ_WAND_ROD =
            wandRod("quartz");
    public static final RegistryObject<Item> BONE_WAND_ROD = wandRod("bone");
    public static final RegistryObject<Item> REED_WAND_ROD = wandRod("reed");

    public static final RegistryObject<Item> GREATWOOD_STAFF_ROD =
            staffRod("greatwood");
    public static final RegistryObject<Item> SILVERWOOD_STAFF_ROD =
            staffRod("silverwood");
    public static final RegistryObject<Item> OBSIDIAN_STAFF_ROD =
            staffRod("obsidian");
    public static final RegistryObject<Item> BLAZE_STAFF_ROD =
            staffRod("blaze");
    public static final RegistryObject<Item> ICE_STAFF_ROD = staffRod("ice");
    public static final RegistryObject<Item> QUARTZ_STAFF_ROD =
            staffRod("quartz");
    public static final RegistryObject<Item> BONE_STAFF_ROD = staffRod("bone");
    public static final RegistryObject<Item> REED_STAFF_ROD = staffRod("reed");
    public static final RegistryObject<Item> PRIMAL_STAFF_ROD =
            staffRod("primal");

    public static final RegistryObject<Item> BASIC_WAND =
            assembledWand("basic_wand", "wood", "iron", WandForm.WAND, false);
    public static final RegistryObject<Item> SILVERWOOD_WAND =
            assembledWand(
                    "silverwood_wand",
                    "silverwood",
                    "iron",
                    WandForm.WAND,
                    false
            );
    public static final RegistryObject<Item> CASTING_WAND =
            assembledWand(
                    "casting_wand",
                    "greatwood",
                    "gold",
                    WandForm.WAND,
                    false
            );
    public static final RegistryObject<Item> CRAFTING_SCEPTRE =
            assembledWand(
                    "crafting_sceptre",
                    "silverwood",
                    "thaumium",
                    WandForm.SCEPTRE,
                    false
            );
    public static final RegistryObject<Item> GREATWOOD_STAFF =
            assembledWand(
                    "greatwood_staff",
                    "greatwood_staff",
                    "gold",
                    WandForm.STAFF,
                    false
            );
    public static final RegistryObject<Item> SILVERWOOD_STAFF =
            assembledWand(
                    "silverwood_staff",
                    "silverwood_staff",
                    "thaumium",
                    WandForm.STAFF,
                    false
            );
    public static final RegistryObject<Item> PRIMAL_STAFF =
            assembledWand(
                    "primal_staff",
                    "primal_staff",
                    "void",
                    WandForm.STAFF,
                    false
            );
    public static final RegistryObject<Item> CODEX_WAND =
            assembledWand(
                    "codex_wand",
                    "codex",
                    "void",
                    WandForm.WAND,
                    true,
                    1000
            );
    public static final RegistryObject<Item> GOGGLES_OF_REVEALING =
            ITEMS.register(
                    "goggles_of_revealing",
                    () -> new GogglesOfRevealingItem(
                            new Item.Properties()
                                    .stacksTo(1)
                                    .rarity(Rarity.RARE)
                    )
            );
    public static final RegistryObject<Item> MAGIC_MIRROR = ITEMS.register(
            "magic_mirror", () -> new MirrorBlockItem(ModBlocks.MAGIC_MIRROR.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> ESSENTIA_MIRROR = ITEMS.register(
            "essentia_mirror", () -> new MirrorBlockItem(ModBlocks.ESSENTIA_MIRROR.get(),
                    new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> HAND_MIRROR = ITEMS.register(
            "hand_mirror", () -> new HandMirrorItem(new Item.Properties()
                    .stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final RegistryObject<Item> AIR_SHARD =
            shard("air_shard", "aer", 0xFFFF7E);
    public static final RegistryObject<Item> FIRE_SHARD =
            shard("fire_shard", "ignis", 0xFF3C01);
    public static final RegistryObject<Item> WATER_SHARD =
            shard("water_shard", "aqua", 0x0090FF);
    public static final RegistryObject<Item> EARTH_SHARD =
            shard("earth_shard", "terra", 0x00A000);
    public static final RegistryObject<Item> ORDER_SHARD =
            shard("order_shard", "ordo", 0xEECCFF);
    public static final RegistryObject<Item> ENTROPY_SHARD =
            shard("entropy_shard", "perditio", 0x555577);

    public static final RegistryObject<Item> RESEARCH_TABLE =
            ITEMS.register("research_table", () -> new BlockItem(ModBlocks.RESEARCH_TABLE.get(), new Item.Properties()));
    public static final RegistryObject<Item> THAUMCRAFT_TABLE =
            ITEMS.register(
                    "thaumcraft_table",
                    () -> new BlockItem(
                            ModBlocks.THAUMCRAFT_TABLE.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> ARCANE_WORKBENCH =
            ITEMS.register(
                    "arcane_workbench",
                    () -> new BlockItem(
                            ModBlocks.ARCANE_WORKBENCH.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> ARCANE_SPA =
            blockItem("arcane_spa", ModBlocks.ARCANE_SPA);
    public static final RegistryObject<Item> ARCANE_BORE_BASE = ITEMS.register(
            "arcane_bore_base", () -> new ArcaneBoreItem(
                    ModBlocks.ARCANE_BORE_BASE.get(), ArcaneBoreItem.Kind.BASE,
                    new Item.Properties()));
    public static final RegistryObject<Item> ARCANE_BORE = ITEMS.register(
            "arcane_bore", () -> new ArcaneBoreItem(
                    ModBlocks.ARCANE_BORE.get(), ArcaneBoreItem.Kind.BORE,
                    new Item.Properties()));
    public static final RegistryObject<Item> DECONSTRUCTION_TABLE =
            ITEMS.register(
                    "deconstruction_table",
                    () -> new DeconstructionTableItem(
                            ModBlocks.DECONSTRUCTION_TABLE.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> AIR_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "air_crystal_cluster",
                    ModBlocks.AIR_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.AIR
            );
    public static final RegistryObject<Item> FIRE_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "fire_crystal_cluster",
                    ModBlocks.FIRE_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.FIRE
            );
    public static final RegistryObject<Item> WATER_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "water_crystal_cluster",
                    ModBlocks.WATER_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.WATER
            );
    public static final RegistryObject<Item> EARTH_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "earth_crystal_cluster",
                    ModBlocks.EARTH_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.EARTH
            );
    public static final RegistryObject<Item> ORDER_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "order_crystal_cluster",
                    ModBlocks.ORDER_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.ORDER
            );
    public static final RegistryObject<Item> ENTROPY_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "entropy_crystal_cluster",
                    ModBlocks.ENTROPY_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.ENTROPY
            );
    public static final RegistryObject<Item> BALANCED_CRYSTAL_CLUSTER =
            crystalClusterItem(
                    "balanced_crystal_cluster",
                    ModBlocks.BALANCED_CRYSTAL_CLUSTER,
                    CrystalClusterVariant.BALANCED
            );
    public static final RegistryObject<Item> ELDRITCH_CRYSTAL_CLUSTER =
            ITEMS.register(
                    "eldritch_crystal_cluster",
                    () -> new EldritchCrystalItem(
                            ModBlocks.ELDRITCH_CRYSTAL_CLUSTER.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> ARCANE_STONE =
            ITEMS.register(
                    "arcane_stone",
                    () -> new BlockItem(
                            ModBlocks.ARCANE_STONE.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> ARCANE_LEVITATOR =
            blockItem("arcane_levitator", ModBlocks.ARCANE_LEVITATOR);
    public static final RegistryObject<Item> ARCANE_DOOR =
            ITEMS.register("arcane_door",()->new ArcaneDoorItem(ModBlocks.ARCANE_DOOR.get(),new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> IRON_KEY=ITEMS.register("iron_key",()->new ArcaneDoorKeyItem(false,new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> GOLD_KEY=ITEMS.register("gold_key",()->new ArcaneDoorKeyItem(true,new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BRAIN_JAR =
            ITEMS.register("brain_jar",()->new BrainJarItem(ModBlocks.BRAIN_JAR.get(),new Item.Properties()));
    public static final RegistryObject<Item> FLUX_SCRUBBER =
            ITEMS.register("flux_scrubber", () -> new FluxScrubberItem(
                    ModBlocks.FLUX_SCRUBBER.get(), new Item.Properties()));
    public static final RegistryObject<Item> SINISTER_LODESTONE = ITEMS.register(
            "sinister_lodestone", () -> new SinisterLodestoneItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> ARCANE_STONE_BRICK =
            blockItem("arcane_stone_brick", ModBlocks.ARCANE_STONE_BRICK);
    public static final RegistryObject<Item> ARCANE_STONE_SLAB =
            blockItem("arcane_stone_slab", ModBlocks.ARCANE_STONE_SLAB);
    public static final RegistryObject<Item> THAUMIUM_BLOCK =
            blockItem("thaumium_block", ModBlocks.THAUMIUM_BLOCK);
    public static final RegistryObject<Item> LOOT_URN =
            blockItem("loot_urn", ModBlocks.LOOT_URN);
    public static final RegistryObject<Item> LOOT_CRATE =
            blockItem("loot_crate", ModBlocks.LOOT_CRATE);
    public static final RegistryObject<Item> CRUCIBLE =
            blockItem("crucible", ModBlocks.CRUCIBLE);
    public static final RegistryObject<Item> ALCHEMICAL_FURNACE =
            blockItem("alchemical_furnace", ModBlocks.ALCHEMICAL_FURNACE);
    public static final RegistryObject<Item> RUNIC_MATRIX =
            blockItem("runic_matrix", ModBlocks.RUNIC_MATRIX);
    public static final RegistryObject<Item> ARCANE_PEDESTAL =
            blockItem("arcane_pedestal", ModBlocks.ARCANE_PEDESTAL);
    public static final RegistryObject<Item> WAND_RECHARGE_PEDESTAL =
            blockItem("wand_recharge_pedestal", ModBlocks.WAND_RECHARGE_PEDESTAL);
    public static final RegistryObject<Item> COMPOUND_RECHARGE_FOCUS =
            blockItem("compound_recharge_focus", ModBlocks.COMPOUND_RECHARGE_FOCUS);
    public static final RegistryObject<Item> ARCANE_ALEMBIC =
            blockItem("arcane_alembic", ModBlocks.ARCANE_ALEMBIC);
    public static final RegistryObject<Item> JAR_LABEL =
            ITEMS.register("jar_label", () -> new JarLabelItem(new Item.Properties()));
    public static final RegistryObject<Item> ESSENTIA_PHIAL =
            ITEMS.register("essentia_phial",
                    () -> new EssentiaPhialItem(new Item.Properties()));
    public static final RegistryObject<Item> WARDED_JAR =
            ITEMS.register("warded_jar", () -> new WardedJarItem(
                    ModBlocks.WARDED_JAR.get(), new Item.Properties()));
    /** TC4 ItemJarFilled equivalent; intentionally hidden from the creative tab. */
    public static final RegistryObject<Item> FILLED_WARDED_JAR =
            ITEMS.register("filled_warded_jar", () -> new WardedJarItem(
                    ModBlocks.WARDED_JAR.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ESSENTIA_TUBE =
            blockItem("essentia_tube", ModBlocks.ESSENTIA_TUBE);
    public static final RegistryObject<Item> FILTERED_ESSENTIA_TUBE =
            blockItem("filtered_essentia_tube", ModBlocks.FILTERED_ESSENTIA_TUBE);
    public static final RegistryObject<Item> RESTRICTED_ESSENTIA_TUBE =
            blockItem("restricted_essentia_tube", ModBlocks.RESTRICTED_ESSENTIA_TUBE);
    public static final RegistryObject<Item> ONE_WAY_ESSENTIA_TUBE =
            blockItem("one_way_essentia_tube", ModBlocks.ONE_WAY_ESSENTIA_TUBE);
    public static final RegistryObject<Item> ESSENTIA_VALVE =
            blockItem("essentia_valve", ModBlocks.ESSENTIA_VALVE);
    public static final RegistryObject<Item> REVERSIBLE_ESSENTIA_TUBE =
            blockItem("reversible_essentia_tube",
                    ModBlocks.REVERSIBLE_ESSENTIA_TUBE);
    public static final RegistryObject<Item> ADVANCED_ESSENTIA_BUFFER =
            blockItem("advanced_essentia_buffer",
                    ModBlocks.ADVANCED_ESSENTIA_BUFFER);
    public static final RegistryObject<Item> ALCHEMICAL_CONSTRUCT =
            blockItem("alchemical_construct", ModBlocks.ALCHEMICAL_CONSTRUCT);
    public static final RegistryObject<Item> ADVANCED_ALCHEMICAL_CONSTRUCT =
            blockItem(
                    "advanced_alchemical_construct",
                    ModBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT
            );
    public static final RegistryObject<Item> JARRED_AURA_NODE =
            ITEMS.register(
                    "jarred_aura_node",
                    () -> new JarredAuraNodeItem(
                            ModBlocks.JARRED_AURA_NODE.get(),
                            new Item.Properties().stacksTo(1).rarity(Rarity.RARE)
                    )
            );

    public static final RegistryObject<Item> CINNABAR_ORE =
            blockItem("cinnabar_ore", ModBlocks.CINNABAR_ORE);
    public static final RegistryObject<Item> AIR_INFUSED_STONE =
            blockItem("air_infused_stone", ModBlocks.AIR_INFUSED_STONE);
    public static final RegistryObject<Item> FIRE_INFUSED_STONE =
            blockItem("fire_infused_stone", ModBlocks.FIRE_INFUSED_STONE);
    public static final RegistryObject<Item> WATER_INFUSED_STONE =
            blockItem("water_infused_stone", ModBlocks.WATER_INFUSED_STONE);
    public static final RegistryObject<Item> EARTH_INFUSED_STONE =
            blockItem("earth_infused_stone", ModBlocks.EARTH_INFUSED_STONE);
    public static final RegistryObject<Item> ORDER_INFUSED_STONE =
            blockItem("order_infused_stone", ModBlocks.ORDER_INFUSED_STONE);
    public static final RegistryObject<Item> ENTROPY_INFUSED_STONE =
            blockItem("entropy_infused_stone", ModBlocks.ENTROPY_INFUSED_STONE);
    public static final RegistryObject<Item> DEEPSLATE_AIR_INFUSED_STONE =
            blockItem("deepslate_air_infused_stone", ModBlocks.DEEPSLATE_AIR_INFUSED_STONE);
    public static final RegistryObject<Item> DEEPSLATE_FIRE_INFUSED_STONE =
            blockItem("deepslate_fire_infused_stone", ModBlocks.DEEPSLATE_FIRE_INFUSED_STONE);
    public static final RegistryObject<Item> DEEPSLATE_WATER_INFUSED_STONE =
            blockItem("deepslate_water_infused_stone", ModBlocks.DEEPSLATE_WATER_INFUSED_STONE);
    public static final RegistryObject<Item> DEEPSLATE_EARTH_INFUSED_STONE =
            blockItem("deepslate_earth_infused_stone", ModBlocks.DEEPSLATE_EARTH_INFUSED_STONE);
    public static final RegistryObject<Item> DEEPSLATE_ORDER_INFUSED_STONE =
            blockItem("deepslate_order_infused_stone", ModBlocks.DEEPSLATE_ORDER_INFUSED_STONE);
    public static final RegistryObject<Item> DEEPSLATE_ENTROPY_INFUSED_STONE =
            blockItem("deepslate_entropy_infused_stone", ModBlocks.DEEPSLATE_ENTROPY_INFUSED_STONE);
    public static final RegistryObject<Item> AMBER_ORE =
            blockItem("amber_ore", ModBlocks.AMBER_ORE);
    public static final RegistryObject<Item> GREATWOOD_LOG =
            blockItem("greatwood_log", ModBlocks.GREATWOOD_LOG);
    public static final RegistryObject<Item> SILVERWOOD_LOG =
            blockItem("silverwood_log", ModBlocks.SILVERWOOD_LOG);
    public static final RegistryObject<Item> GREATWOOD_LEAVES =
            blockItem("greatwood_leaves", ModBlocks.GREATWOOD_LEAVES);
    public static final RegistryObject<Item> SILVERWOOD_LEAVES =
            blockItem("silverwood_leaves", ModBlocks.SILVERWOOD_LEAVES);
    public static final RegistryObject<Item> GREATWOOD_PLANKS =
            blockItem("greatwood_planks", ModBlocks.GREATWOOD_PLANKS);
    public static final RegistryObject<Item> SILVERWOOD_PLANKS =
            blockItem("silverwood_planks", ModBlocks.SILVERWOOD_PLANKS);
    public static final RegistryObject<Item> GREATWOOD_STAIRS =
            blockItem("greatwood_stairs", ModBlocks.GREATWOOD_STAIRS);
    public static final RegistryObject<Item> SILVERWOOD_STAIRS =
            blockItem("silverwood_stairs", ModBlocks.SILVERWOOD_STAIRS);
    public static final RegistryObject<Item> GREATWOOD_SLAB =
            blockItem("greatwood_slab", ModBlocks.GREATWOOD_SLAB);
    public static final RegistryObject<Item> SILVERWOOD_SLAB =
            blockItem("silverwood_slab", ModBlocks.SILVERWOOD_SLAB);
    public static final RegistryObject<Item> GREATWOOD_SAPLING =
            blockItem("greatwood_sapling", ModBlocks.GREATWOOD_SAPLING);
    public static final RegistryObject<Item> SILVERWOOD_SAPLING =
            blockItem("silverwood_sapling", ModBlocks.SILVERWOOD_SAPLING);
    public static final RegistryObject<Item> SHIMMERLEAF =
            blockItem("shimmerleaf", ModBlocks.SHIMMERLEAF);
    public static final RegistryObject<Item> CINDERPEARL =
            blockItem("cinderpearl", ModBlocks.CINDERPEARL);
    public static final RegistryObject<Item> ETHEREAL_BLOOM =
            blockItem("ethereal_bloom", ModBlocks.ETHEREAL_BLOOM);
    public static final RegistryObject<Item> VISHROOM =
            blockItem("vishroom", ModBlocks.VISHROOM);
    public static final RegistryObject<Item> CRUSTED_TAINT =
            blockItem("crusted_taint", ModBlocks.CRUSTED_TAINT);
    public static final RegistryObject<Item> TAINTED_SOIL =
            blockItem("tainted_soil", ModBlocks.TAINTED_SOIL);
    public static final RegistryObject<Item> TAINTED_LEAVES =
            blockItem("tainted_leaves", ModBlocks.TAINTED_LEAVES);
    public static final RegistryObject<Item> TAINT_FIBRES =
            blockItem("taint_fibres", ModBlocks.TAINT_FIBRES);
    public static final RegistryObject<Item> TAINTED_CAVE_MOSS_TEST =
            blockItem("tainted_cave_moss_test", ModBlocks.TAINTED_CAVE_MOSS_TEST);
    public static final RegistryObject<Item> TAINTED_CAVE_VINE_TEST =
            blockItem("tainted_cave_vine_test", ModBlocks.TAINTED_CAVE_VINE_TEST);
    public static final RegistryObject<Item> TAINTED_GLOW_BERRY_VINE_TEST =
            blockItem(
                    "tainted_glow_berry_vine_test",
                    ModBlocks.TAINTED_GLOW_BERRY_VINE_TEST
            );
    public static final RegistryObject<Item> SHORT_TAINTED_GRASS =
            blockItem("short_tainted_grass", ModBlocks.SHORT_TAINTED_GRASS);
    public static final RegistryObject<Item> TALL_TAINTED_GRASS =
            blockItem("tall_tainted_grass", ModBlocks.TALL_TAINTED_GRASS);
    public static final RegistryObject<Item> SPORE_STALK =
            blockItem("spore_stalk", ModBlocks.SPORE_STALK);
    public static final RegistryObject<Item> MATURE_SPORE_STALK =
            blockItem("mature_spore_stalk", ModBlocks.MATURE_SPORE_STALK);
    public static final RegistryObject<Item> FLUX_GOO =
            ITEMS.register(
                    "flux_goo",
                    () -> new FluxGooBlockItem(
                            ModBlocks.FLUX_GOO.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<Item> FLUX_GAS =
            blockItem("flux_gas", ModBlocks.FLUX_GAS);
    public static final RegistryObject<Item> MANA_POD =
            blockItem("mana_pod", ModBlocks.MANA_POD);
    public static final RegistryObject<Item> OBSIDIAN_TOTEM =
            blockItem("obsidian_totem", ModBlocks.OBSIDIAN_TOTEM);
    public static final RegistryObject<Item> OBSIDIAN_TILE =
            blockItem("obsidian_tile", ModBlocks.OBSIDIAN_TILE);
    public static final RegistryObject<Item> ANCIENT_STONE =
            blockItem("ancient_stone", ModBlocks.ANCIENT_STONE);
    public static final RegistryObject<Item> ELDRITCH_GLOWING_CRUST =
            blockItem(
                    "eldritch_glowing_crust",
                    ModBlocks.ELDRITCH_GLOWING_CRUST
            );
    public static final RegistryObject<Item> ANCIENT_ROCK =
            blockItem("ancient_rock", ModBlocks.ANCIENT_ROCK);
    public static final RegistryObject<Item> ANCIENT_STAIRS =
            blockItem("ancient_stairs", ModBlocks.ANCIENT_STAIRS);
    public static final RegistryObject<Item> ANCIENT_SLAB =
            blockItem("ancient_slab", ModBlocks.ANCIENT_SLAB);
    public static final RegistryObject<Item> ANCIENT_CRUST =
            blockItem("ancient_crust", ModBlocks.ANCIENT_CRUST);
    public static final RegistryObject<Item> BALANCED_SHARD =
            ITEMS.register(
                    "balanced_shard",
                    () -> new Item(new Item.Properties())
            );
    public static final RegistryObject<Item> SALIS_MUNDUS =
            ITEMS.register(
                    "salis_mundus",
                    () -> new Item(new Item.Properties())
            );
    public static final RegistryObject<Item> ALUMENTUM =
            ITEMS.register(
                    "alumentum",
                    () -> new AlumentumItem(new Item.Properties())
            );
    public static final RegistryObject<Item> NITOR =
            ITEMS.register(
                    "nitor",
                    () -> new BlockItem(
                            ModBlocks.NITOR.get(),
                            new Item.Properties()
                    )
            );
    public static final RegistryObject<EssentiaCrystalItem> ESSENTIA_CRYSTAL =
            ITEMS.register("essentia_crystal",
                    () -> new EssentiaCrystalItem(new Item.Properties()));
    /**
     * Stable 1.20 registry identities for classic arcane-recipe components.
     * Items whose gameplay class has not been ported yet deliberately remain
     * plain items; recipes and research pages no longer depend on TC4 metadata.
     */
    public static final RegistryObject<Item> STRAW_GOLEM = ITEMS.register(
            "straw_golem", () -> new StrawGolemItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> WOOD_GOLEM = ITEMS.register(
            "wood_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.WOOD, ModEntities.WOOD_GOLEM));
    public static final RegistryObject<Item> TALLOW_GOLEM = ITEMS.register(
            "tallow_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.TALLOW, ModEntities.TALLOW_GOLEM));
    public static final RegistryObject<Item> CLAY_GOLEM = ITEMS.register(
            "clay_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.CLAY, ModEntities.CLAY_GOLEM));
    public static final RegistryObject<Item> FLESH_GOLEM = ITEMS.register(
            "flesh_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.FLESH, ModEntities.FLESH_GOLEM));
    public static final RegistryObject<Item> STONE_GOLEM = ITEMS.register(
            "stone_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.STONE, ModEntities.STONE_GOLEM));
    public static final RegistryObject<Item> IRON_GOLEM = ITEMS.register(
            "iron_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.IRON, ModEntities.IRON_GOLEM));
    public static final RegistryObject<Item> THAUMIUM_GOLEM = ITEMS.register(
            "thaumium_golem", () -> new ClassicGolemItem(new Item.Properties().stacksTo(1),
                    GolemMaterial.THAUMIUM, ModEntities.THAUMIUM_GOLEM));
    public static final RegistryObject<Item> TRAVELING_TRUNK = ITEMS.register(
            "traveling_trunk", () -> new TravelingTrunkItem(new Item.Properties().stacksTo(1)));
    public static final Map<GolemCoreType, RegistryObject<Item>> GOLEM_CORES = registerGolemCores();
    public static final RegistryObject<Item> TALLOW_BLOCK = blockItem("tallow_block", ModBlocks.TALLOW_BLOCK);
    public static final RegistryObject<Item> FLESH_BLOCK = blockItem("flesh_block", ModBlocks.FLESH_BLOCK);
    public static final Map<String, RegistryObject<Item>> ARCANE_RECIPE_COMPONENTS =
            registerArcaneRecipeComponents();
    public static final RegistryObject<Item> HUNGRY_CHEST =
            ARCANE_RECIPE_COMPONENTS.get("hungry_chest");
    public static final RegistryObject<Item> GOLEM_FETTER =
            ARCANE_RECIPE_COMPONENTS.get("golem_fetter");
    public static final RegistryObject<Item> THAUMATURGE_ROBE =
            ARCANE_RECIPE_COMPONENTS.get("thaumaturge_robe");
    public static final RegistryObject<Item> THAUMATURGE_LEGGINGS =
            ARCANE_RECIPE_COMPONENTS.get("thaumaturge_leggings");
    public static final RegistryObject<Item> THAUMATURGE_BOOTS =
            ARCANE_RECIPE_COMPONENTS.get("thaumaturge_boots");
    public static final RegistryObject<Item> ESSENTIA_BUFFER =
            ARCANE_RECIPE_COMPONENTS.get("essentia_buffer");
    public static final RegistryObject<Item> ESSENTIA_CENTRIFUGE =
            ARCANE_RECIPE_COMPONENTS.get("essentia_centrifuge");
    public static final RegistryObject<Item> ESSENTIA_CRYSTALLIZER =
            ARCANE_RECIPE_COMPONENTS.get("essentia_crystallizer");
    public static final RegistryObject<Item> ESSENTIA_RESERVOIR =
            ARCANE_RECIPE_COMPONENTS.get("essentia_reservoir");
    public static final RegistryObject<Item> VOID_JAR =
            ARCANE_RECIPE_COMPONENTS.get("void_jar");
    /** Filled void-jar counterpart with the same hard non-stacking limit. */
    public static final RegistryObject<Item> FILLED_VOID_JAR =
            ITEMS.register("filled_void_jar", () -> new WardedJarItem(
                    ModBlocks.VOID_JAR.get(), new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> MNEMONIC_MATRIX =
            ARCANE_RECIPE_COMPONENTS.get("mnemonic_matrix");
    public static final RegistryObject<Item> NODE_STABILIZER =
            ARCANE_RECIPE_COMPONENTS.get("node_stabilizer");
    public static final RegistryObject<Item> ADVANCED_NODE_STABILIZER =
            ARCANE_RECIPE_COMPONENTS.get("advanced_node_stabilizer");
    public static final RegistryObject<Item> NODE_TRANSDUCER =
            ARCANE_RECIPE_COMPONENTS.get("node_transducer");
    public static final RegistryObject<Item> VIS_RELAY =
            ARCANE_RECIPE_COMPONENTS.get("vis_relay");
    public static final RegistryObject<Item> VIS_CHARGE_RELAY =
            ARCANE_RECIPE_COMPONENTS.get("vis_charge_relay");

    private ModItems() {
    }

    private static RegistryObject<Item> lootBag(
            String registryName,
            String tableName,
            Rarity rarity
    ) {
        return ITEMS.register(
                registryName,
                () -> new LootBagItem(
                        new Item.Properties(),
                        new net.minecraft.resources.ResourceLocation(
                                ThaumcraftModern.MOD_ID,
                                "gameplay/loot_bags/" + tableName
                        ),
                        rarity
                )
        );
    }

    private static RegistryObject<Item> crystalClusterItem(
            String name,
            RegistryObject<Block> block,
            CrystalClusterVariant variant
    ) {
        return ITEMS.register(
                name,
                () -> new CrystalClusterItem(
                        block.get(),
                        variant,
                        new Item.Properties()
                )
        );
    }

    private static RegistryObject<Item> thaumiumArmor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ThaumiumArmorItem(
                type, new Item.Properties().rarity(Rarity.UNCOMMON)));
    }

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private static RegistryObject<Item> cultistArmor(
            String name,
            CultistArmorItem.Set set,
            ArmorItem.Type type
    ) {
        return ITEMS.register(name, () -> new CultistArmorItem(
                set,
                type,
                new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
        ));
    }

    private static RegistryObject<Item> wingedMantleArmor(
            String name, ArmorItem.Type type
    ) {
        return ITEMS.register(name, () -> new WingedMantleArmorItem(
                type,
                new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
        ));
    }

    private static RegistryObject<Item> voidArmor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new VoidArmorItem(type,
                new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    }

    private static RegistryObject<Item> voidRobeArmor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new VoidRobeArmorItem(type,
                new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    }

    private static Map<String, RegistryObject<Item>> registerArcaneRecipeComponents() {
        List<String> names = List.of(
                "aer_primal_arrow", "aqua_primal_arrow", "ignis_primal_arrow",
                "ordo_primal_arrow", "perditio_primal_arrow", "terra_primal_arrow",
                "arcane_bellows", "arcane_ear",
                "chicken_nugget", "beef_nugget", "pork_nugget", "fish_nugget",
                "arcane_lamp", "lamp_growth", "lamp_fertility", "item_grate",
                "arcane_pressure_plate",
                "arcane_stone_slab", "blank_belt", "blank_golem_core",
                "bone_bow", "deconstruction_table", "enchanted_fabric", "essentia_buffer",
                "essentia_centrifuge", "essentia_crystallizer", "essentia_reservoir",
                "essentia_resonator", "focal_manipulator",
                "focus_excavation", "focus_fire", "focus_frost", "focus_pouch",
                "focus_primal", "focus_shock", "focus_trade",
                "focus_hellbat", "focus_portable_hole", "focus_warding", "golem_bell",
                "golem_decoration_armor", "golem_decoration_bow_tie",
                "golem_decoration_dart_launcher", "golem_decoration_fez",
                "golem_decoration_glasses", "golem_decoration_hammer",
                "golem_decoration_top_hat", "golem_decoration_visor", "golem_fetter",
                "golem_upgrade_aer", "golem_upgrade_aqua", "golem_upgrade_ignis",
                "golem_upgrade_ordo", "golem_upgrade_perditio", "golem_upgrade_terra",
                "hungry_chest", "inert_silver_wand_cap", "inert_thaumium_wand_cap",
                "inert_void_wand_cap", "mirrored_glass", "mnemonic_matrix",
                "node_stabilizer", "advanced_node_stabilizer", "node_transducer",
                "paving_stone_of_travel", "paving_stone_of_warding", "primal_charm",
                "thaumaturge_boots", "thaumaturge_leggings",
                "thaumaturge_robe", "thaumium_ingot", "thaumium_nugget",
                "silver_nugget", "void_nugget",
                "vis_charge_relay", "vis_filter", "vis_relay", "void_jar",
                "void_metal_ingot", "warded_glass",
                "white_thaumcraft_banner", "orange_thaumcraft_banner",
                "magenta_thaumcraft_banner", "light_blue_thaumcraft_banner",
                "yellow_thaumcraft_banner", "lime_thaumcraft_banner",
                "pink_thaumcraft_banner", "gray_thaumcraft_banner",
                "light_gray_thaumcraft_banner", "cyan_thaumcraft_banner",
                "purple_thaumcraft_banner", "blue_thaumcraft_banner",
                "brown_thaumcraft_banner", "green_thaumcraft_banner",
                "red_thaumcraft_banner", "black_thaumcraft_banner"
        );
        Map<String, RegistryObject<Item>> registered = new LinkedHashMap<>();
        names.forEach(name -> registered.put(name, switch (name) {
            case "aer_primal_arrow" -> ITEMS.register(name,
                    () -> new PrimalArrowItem(PrimalArrowType.AER, new Item.Properties()));
            case "ignis_primal_arrow" -> ITEMS.register(name,
                    () -> new PrimalArrowItem(PrimalArrowType.IGNIS, new Item.Properties()));
            case "aqua_primal_arrow" -> ITEMS.register(name,
                    () -> new PrimalArrowItem(PrimalArrowType.AQUA, new Item.Properties()));
            case "terra_primal_arrow" -> ITEMS.register(name,
                    () -> new PrimalArrowItem(PrimalArrowType.TERRA, new Item.Properties()));
            case "ordo_primal_arrow" -> ITEMS.register(name,
                    () -> new PrimalArrowItem(PrimalArrowType.ORDO, new Item.Properties()));
            case "perditio_primal_arrow" -> ITEMS.register(name,
                    () -> new PrimalArrowItem(PrimalArrowType.PERDITIO, new Item.Properties()));
            case "bone_bow" -> ITEMS.register(name,
                    () -> new BoneBowItem(new Item.Properties()));
            case "focus_pouch" -> ITEMS.register(name,
                    () -> new FocusPouchItem(new Item.Properties().rarity(Rarity.RARE)));
            case "arcane_pressure_plate" -> blockItem(name, ModBlocks.ARCANE_PRESSURE_PLATE);
            case "warded_glass" -> blockItem(name, ModBlocks.WARDED_GLASS);
            case "arcane_stone_slab" -> ARCANE_STONE_SLAB;
            case "deconstruction_table" -> DECONSTRUCTION_TABLE;
            case "hungry_chest" -> ITEMS.register(name, () -> new HungryChestItem(
                    ModBlocks.HUNGRY_CHEST.get(), new Item.Properties()));
            case "golem_fetter" -> blockItem(name, ModBlocks.GOLEM_FETTER);
            case "golem_bell" -> ITEMS.register(name,
                    () -> new com.thaumcraftmodern.item.GolemBellItem(new Item.Properties().stacksTo(1)));
            case "golem_decoration_armor", "golem_decoration_bow_tie",
                    "golem_decoration_dart_launcher", "golem_decoration_fez",
                    "golem_decoration_glasses", "golem_decoration_hammer",
                    "golem_decoration_top_hat", "golem_decoration_visor" -> ITEMS.register(name,
                            () -> new GolemDecorationItem(GolemDecorationType.byItemId(name)));
            case "golem_upgrade_aer" -> ITEMS.register(name,
                    () -> new GolemUpgradeItem(GolemUpgradeType.AER));
            case "golem_upgrade_terra" -> ITEMS.register(name,
                    () -> new GolemUpgradeItem(GolemUpgradeType.TERRA));
            case "golem_upgrade_ignis" -> ITEMS.register(name,
                    () -> new GolemUpgradeItem(GolemUpgradeType.IGNIS));
            case "golem_upgrade_aqua" -> ITEMS.register(name,
                    () -> new GolemUpgradeItem(GolemUpgradeType.AQUA));
            case "golem_upgrade_ordo" -> ITEMS.register(name,
                    () -> new GolemUpgradeItem(GolemUpgradeType.ORDO));
            case "golem_upgrade_perditio" -> ITEMS.register(name,
                    () -> new GolemUpgradeItem(GolemUpgradeType.PERDITIO));
            case "focal_manipulator" -> blockItem(name, ModBlocks.FOCAL_MANIPULATOR);
            case "blank_belt" -> ITEMS.register(name,
                    () -> new CurioAccessoryItem(
                            new Item.Properties().stacksTo(1)));
            case "arcane_bellows" -> ITEMS.register(name,
                    () -> new ArcaneBellowsItem(ModBlocks.ARCANE_BELLOWS.get(), new Item.Properties()));
            case "arcane_lamp" -> blockItem(name, ModBlocks.ARCANE_LAMP);
            case "lamp_growth" -> blockItem(name, ModBlocks.GROWTH_LAMP);
            case "lamp_fertility" -> blockItem(name, ModBlocks.FERTILITY_LAMP);
            case "item_grate" -> blockItem(name, ModBlocks.ITEM_GRATE);
            case "arcane_ear" -> blockItem(name, ModBlocks.ARCANE_EAR);
            case "essentia_buffer" -> blockItem(name, ModBlocks.ESSENTIA_BUFFER);
            case "essentia_centrifuge" -> blockItem(name, ModBlocks.ESSENTIA_CENTRIFUGE);
            case "essentia_crystallizer" -> blockItem(name, ModBlocks.ESSENTIA_CRYSTALLIZER);
            case "essentia_reservoir" -> ITEMS.register(name,
                    () -> new EssentiaReservoirItem(
                            ModBlocks.ESSENTIA_RESERVOIR.get(),
                            new Item.Properties()));
            case "essentia_resonator" -> ITEMS.register(name,
                    () -> new EssentiaResonatorItem(new Item.Properties()));
            case "focus_fire" -> focus(name, WandFocusType.FIRE);
            case "focus_frost" -> focus(name, WandFocusType.FROST);
            case "focus_shock" -> focus(name, WandFocusType.SHOCK);
            case "focus_trade" -> focus(name, WandFocusType.TRADE);
            case "focus_excavation" -> focus(name, WandFocusType.EXCAVATION);
            case "focus_primal" -> focus(name, WandFocusType.PRIMAL);
            case "focus_hellbat" -> focus(name, WandFocusType.HELLBAT);
            case "focus_portable_hole" -> focus(name, WandFocusType.PORTABLE_HOLE);
            case "focus_warding" -> focus(name, WandFocusType.WARDING);
            case "mnemonic_matrix" -> blockItem(name, ModBlocks.MNEMONIC_MATRIX);
            case "node_stabilizer" -> visDeviceItem(
                    name, ModBlocks.NODE_STABILIZER,
                    VisDeviceBlockItem.Kind.STABILIZER);
            case "advanced_node_stabilizer" -> visDeviceItem(
                    name, ModBlocks.ADVANCED_NODE_STABILIZER,
                    VisDeviceBlockItem.Kind.ADVANCED_STABILIZER);
            case "node_transducer" -> visDeviceItem(
                    name, ModBlocks.NODE_TRANSDUCER,
                    VisDeviceBlockItem.Kind.TRANSDUCER);
            case "vis_relay" -> visDeviceItem(
                    name, ModBlocks.VIS_RELAY,
                    VisDeviceBlockItem.Kind.RELAY);
            case "vis_charge_relay" -> visDeviceItem(
                    name, ModBlocks.VIS_CHARGE_RELAY,
                    VisDeviceBlockItem.Kind.CHARGER);
            case "paving_stone_of_travel" -> blockItem(
                    name,
                    ModBlocks.PAVING_STONE_OF_TRAVEL
            );
            case "paving_stone_of_warding" -> blockItem(
                    name,
                    ModBlocks.PAVING_STONE_OF_WARDING
            );
            case "thaumaturge_boots" -> ITEMS.register(
                    name,
                    () -> new ThaumaturgeRobeItem(
                            ArmorItem.Type.BOOTS,
                            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    )
            );
            case "thaumaturge_leggings" -> ITEMS.register(
                    name,
                    () -> new ThaumaturgeRobeItem(
                            ArmorItem.Type.LEGGINGS,
                            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    )
            );
            case "thaumaturge_robe" -> ITEMS.register(
                    name,
                    () -> new ThaumaturgeRobeItem(
                            ArmorItem.Type.CHESTPLATE,
                            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
                    )
            );
            case "void_jar" -> ITEMS.register(name, () -> new WardedJarItem(
                    ModBlocks.VOID_JAR.get(), new Item.Properties()));
            case "chicken_nugget", "beef_nugget", "pork_nugget", "fish_nugget" ->
                    ITEMS.register(name, () -> new Item(new Item.Properties().food(
                            new net.minecraft.world.food.FoodProperties.Builder()
                                    .nutrition(1).saturationMod(0.3F).build()
                    )));
            default -> ITEMS.register(name, () -> new Item(new Item.Properties()));
        }));
        return Collections.unmodifiableMap(registered);
    }

    public static RegistryObject<Item> golem(GolemMaterial material) {
        return switch (material) {
            case STRAW -> STRAW_GOLEM;
            case WOOD -> WOOD_GOLEM;
            case TALLOW -> TALLOW_GOLEM;
            case CLAY -> CLAY_GOLEM;
            case FLESH -> FLESH_GOLEM;
            case STONE -> STONE_GOLEM;
            case IRON -> IRON_GOLEM;
            case THAUMIUM -> THAUMIUM_GOLEM;
        };
    }

    public static RegistryObject<Item> golemCore(GolemCoreType type) {
        return GOLEM_CORES.get(type);
    }

    public static RegistryObject<Item> golemUpgrade(GolemUpgradeType type) {
        return ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_" + type.id());
    }

    private static Map<GolemCoreType, RegistryObject<Item>> registerGolemCores() {
        Map<GolemCoreType, RegistryObject<Item>> cores = new EnumMap<>(GolemCoreType.class);
        for (GolemCoreType type : GolemCoreType.values()) {
            cores.put(type, ITEMS.register(type.id() + "_golem_core", () -> new GolemCoreItem(type)));
        }
        return Collections.unmodifiableMap(cores);
    }

    private static RegistryObject<Item> visDeviceItem(
            String name,
            RegistryObject<Block> block,
            VisDeviceBlockItem.Kind kind
    ) {
        return ITEMS.register(
                name,
                () -> new VisDeviceBlockItem(
                        block.get(), kind, new Item.Properties())
        );
    }

    private static RegistryObject<Item> focus(String name, WandFocusType type) {
        return ITEMS.register(name, () -> new WandFocusItem(
                type, new Item.Properties().rarity(Rarity.RARE)));
    }

    private static RegistryObject<Item> runicAccessory(String name, int charge,
            RunicAccessoryItem.Upgrade upgrade, Rarity rarity) {
        return ITEMS.register(name, () -> new RunicAccessoryItem(charge, upgrade,
                new Item.Properties().stacksTo(1).rarity(rarity)));
    }

    private static RegistryObject<Item> shard(String name, String aspectId, int color) {
        return ITEMS.register(name, () -> new AspectShardItem(aspectId, color, new Item.Properties()));
    }

    private static RegistryObject<Item> wandCap(String id) {
        return ITEMS.register(id + "_wand_cap", () -> new WandComponentItem(
                WandComponentItem.Kind.CAP,
                id,
                new Item.Properties()
        ));
    }

    private static RegistryObject<Item> wandRod(String id) {
        return wandRod(id, id + "_wand_rod");
    }

    private static RegistryObject<Item> wandRod(
            String componentId,
            String registryName
    ) {
        return ITEMS.register(registryName, () -> new WandComponentItem(
                WandComponentItem.Kind.ROD,
                componentId,
                new Item.Properties()
        ));
    }

    private static RegistryObject<Item> staffRod(String id) {
        return wandRod(id + "_staff", id + "_staff_rod");
    }

    private static RegistryObject<Item> assembledWand(
            String registryName,
            String rodId,
            String capId,
            WandForm form,
            boolean filledByDefault
    ) {
        return assembledWand(
                registryName,
                rodId,
                capId,
                form,
                filledByDefault,
                0
        );
    }

    private static RegistryObject<Item> assembledWand(
            String registryName,
            String rodId,
            String capId,
            WandForm form,
            boolean filledByDefault,
            int fallbackFilledCapacityVis
    ) {
        return ITEMS.register(registryName, () -> new WandItem(
                rodId,
                capId,
                form,
                filledByDefault,
                fallbackFilledCapacityVis,
                new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)
        ));
    }

    private static RegistryObject<Item> blockItem(
            String name,
            RegistryObject<? extends net.minecraft.world.level.block.Block> block
    ) {
        return ITEMS.register(
                name,
                () -> new BlockItem(block.get(), new Item.Properties())
        );
    }

    private static Map<PrimalAspect, RegistryObject<Item>> registerAspectRings() {
        EnumMap<PrimalAspect, RegistryObject<Item>> rings =
                new EnumMap<>(PrimalAspect.class);
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            rings.put(aspect, ITEMS.register(
                    "apprentice_ring_" + aspect.id(),
                    () -> new AspectRingItem(aspect,
                            new Item.Properties().stacksTo(1)
                                    .rarity(Rarity.UNCOMMON))));
        }
        return Collections.unmodifiableMap(rings);
    }

    private static Map<LegacyMobKind, RegistryObject<Item>> registerSpawnEggs() {
        EnumMap<LegacyMobKind, RegistryObject<Item>> eggs =
                new EnumMap<>(LegacyMobKind.class);
        for (LegacyMobKind kind : LegacyMobKind.values()) {
            long colors = spawnEggColors(kind);
            eggs.put(
                    kind,
                    ITEMS.register(
                            kind.id() + "_spawn_egg",
                            () -> new ForgeSpawnEggItem(
                                    ModEntities.forKind(kind),
                                    (int) (colors >>> 32),
                                    (int) colors,
                                    new Item.Properties()
                            )
                    )
            );
        }
        return Collections.unmodifiableMap(eggs);
    }

    private static long spawnEggColors(LegacyMobKind kind) {
        int primary = switch (kind) {
            case ANGRY_ZOMBIE, FURIOUS_ZOMBIE -> 0xFFF0FF;
            case WISP -> 0xFFFFFF;
            case FIREBAT -> 0xC00000;
            case PECH -> 0x400040;
            case MIND_SPIDER -> 0xAAAAAA;
            case ELDRITCH_GUARDIAN, ELDRITCH_WARDEN -> 0x222222;
            case CRIMSON_KNIGHT, CRIMSON_INQUISITOR, CRIMSON_CLERIC, CRIMSON_PRAETOR -> 0xFF5055;
            case CONVERTED_VILLAGER -> 0x7A3A32;
            case ELDRITCH_CONSTRUCT, ELDRITCH_CRAB, INHABITED_ZOMBIE -> 0x555555;
            default -> kind.tainted() ? 0xFFC0FF : 0x666666;
        };
        int secondary = switch (kind) {
            case ANGRY_ZOMBIE -> 0x008000;
            case FURIOUS_ZOMBIE -> 0x004000;
            case FIREBAT -> 0x800000;
            case PECH, MIND_SPIDER, ELDRITCH_GUARDIAN,
                    ELDRITCH_WARDEN -> 0x404000;
            case CRIMSON_KNIGHT -> 0x000080;
            case CRIMSON_INQUISITOR -> 0x2B202E;
            case CONVERTED_VILLAGER -> 0x8B1F2D;
            case CRIMSON_CLERIC -> 0x800000;
            case CRIMSON_PRAETOR -> 0x505050;
            case ELDRITCH_CRAB, INHABITED_ZOMBIE -> 0x550000;
            default -> kind.tainted() ? 0x800080 : 0xFFFFFF;
        };
        return ((long) primary << 32) | (secondary & 0xFFFFFFFFL);
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
