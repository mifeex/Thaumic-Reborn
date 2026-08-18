package com.thaumcraftmodern.registry;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aura.AuraNodeBreakDrops;
import com.thaumcraftmodern.aura.PrimalAspect;
import com.thaumcraftmodern.item.EtherealEssenceItem;
import com.thaumcraftmodern.item.EssentiaPhialItem;
import com.thaumcraftmodern.item.JarLabelItem;
import com.thaumcraftmodern.nodejar.NodeJarFactory;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.wand.WandComponentRegistry;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.LegacyMobKind;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ThaumcraftModern.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register(
            "thaumcraft",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.thaumic_reborn"))
                    .icon(() -> ModItems.THAUMONOMICON.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.THAUMONOMICON.get());
                        output.accept(ModItems.THAUMOMETER.get());
                        output.accept(ModItems.HUNGRY_CHEST.get());
                        output.accept(ModItems.STRAW_GOLEM.get());
                        output.accept(ModItems.WOOD_GOLEM.get());
                        output.accept(ModItems.TALLOW_GOLEM.get());
                        output.accept(ModItems.CLAY_GOLEM.get());
                        output.accept(ModItems.FLESH_GOLEM.get());
                        output.accept(ModItems.STONE_GOLEM.get());
                        output.accept(ModItems.IRON_GOLEM.get());
                        output.accept(ModItems.THAUMIUM_GOLEM.get());
                        output.accept(ModItems.TRAVELING_TRUNK.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_bell").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("blank_golem_core").get());
                        for (GolemCoreType core : GolemCoreType.values()) {
                            output.accept(ModItems.golemCore(core).get());
                        }
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_aer").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_terra").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_ignis").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_aqua").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_ordo").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("golem_upgrade_perditio").get());
                        for (com.thaumcraftmodern.entity.GolemDecorationType decoration
                                : com.thaumcraftmodern.entity.GolemDecorationType.values()) {
                            output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get(decoration.itemId()).get());
                        }
                        output.accept(ModItems.TALLOW_BLOCK.get());
                        output.accept(ModItems.FLESH_BLOCK.get());
                        output.accept(ModItems.GOLEM_FETTER.get());
                        output.accept(ModItems.ETHEREAL_BLOOM.get());
                        output.accept(ModItems.SANITY_CHECKER.get());
                        output.accept(ModItems.SANITY_SOAP.get());
                        output.accept(ModItems.BATH_SALTS.get());
                        output.accept(ModItems.BOTTLED_TAINT.get());
                        output.accept(ModItems.MUNDANE_AMULET.get());
                        output.accept(ModItems.MUNDANE_RING.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("blank_belt").get());
                        output.accept(ModItems.VIS_STONE.get());
                        output.accept(ModItems.VIS_STORAGE_AMULET.get());
                        output.accept(ModItems.RUNIC_RING_LESSER.get());
                        output.accept(ModItems.RUNIC_RING.get());
                        output.accept(ModItems.RUNIC_RING_CHARGED.get());
                        output.accept(ModItems.RUNIC_RING_REGEN.get());
                        output.accept(ModItems.RUNIC_AMULET.get());
                        output.accept(ModItems.RUNIC_AMULET_EMERGENCY.get());
                        output.accept(ModItems.RUNIC_GIRDLE.get());
                        output.accept(ModItems.RUNIC_GIRDLE_KINETIC.get());
                        output.accept(ModItems.ARCANE_LEVITATOR.get());
                        output.accept(ModItems.ARCANE_DOOR.get());
                        output.accept(ModItems.BRAIN_JAR.get());
                        output.accept(ModItems.FLUX_SCRUBBER.get());
                        output.accept(ModItems.SINISTER_LODESTONE.get());
                        output.accept(ModItems.IRON_KEY.get());
                        output.accept(ModItems.GOLD_KEY.get());
                        output.accept(ModItems.LIQUID_DEATH_BUCKET.get());
                        output.accept(ModItems.IRON_WAND_CAP.get());
                        output.accept(ModItems.GOLD_WAND_CAP.get());
                        output.accept(ModItems.COPPER_WAND_CAP.get());
                        output.accept(ModItems.SILVER_WAND_CAP.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("inert_silver_wand_cap").get());
                        output.accept(ModItems.THAUMIUM_WAND_CAP.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("inert_thaumium_wand_cap").get());
                        output.accept(ModItems.VOID_WAND_CAP.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("inert_void_wand_cap").get());
                        output.accept(ModItems.GREATWOOD_WAND_ROD.get());
                        output.accept(ModItems.OBSIDIAN_WAND_ROD.get());
                        output.accept(ModItems.SILVERWOOD_WAND_ROD.get());
                        output.accept(ModItems.ICE_WAND_ROD.get());
                        output.accept(ModItems.QUARTZ_WAND_ROD.get());
                        output.accept(ModItems.REED_WAND_ROD.get());
                        output.accept(ModItems.BLAZE_WAND_ROD.get());
                        output.accept(ModItems.BONE_WAND_ROD.get());
                        output.accept(ModItems.GREATWOOD_STAFF_ROD.get());
                        output.accept(ModItems.OBSIDIAN_STAFF_ROD.get());
                        output.accept(ModItems.SILVERWOOD_STAFF_ROD.get());
                        output.accept(ModItems.ICE_STAFF_ROD.get());
                        output.accept(ModItems.QUARTZ_STAFF_ROD.get());
                        output.accept(ModItems.REED_STAFF_ROD.get());
                        output.accept(ModItems.BLAZE_STAFF_ROD.get());
                        output.accept(ModItems.BONE_STAFF_ROD.get());
                        output.accept(ModItems.PRIMAL_STAFF_ROD.get());
                        WandComponentRegistry.current().ifPresent(ignored -> {
                            WandItem wand = (WandItem)
                                    ModItems.CASTING_WAND.get();
                            output.accept(wand.createFilled("wood", "iron"));
                            output.accept(wand.createFilled(
                                    "greatwood", "gold"
                            ));
                            output.accept(wand.createFilled(
                                    "silverwood", "thaumium"
                            ));
                            output.accept(((WandItem)
                                    ModItems.CRAFTING_SCEPTRE.get())
                                    .createFilled("silverwood", "thaumium"));
                        });
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_fire").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_frost").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_shock").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_excavation").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_trade").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_primal").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_hellbat").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_portable_hole").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focus_warding").get());
                        output.accept(ModItems.GOGGLES_OF_REVEALING.get());
                        output.accept(ModItems.MAGIC_MIRROR.get());
                        output.accept(ModItems.ESSENTIA_MIRROR.get());
                        output.accept(ModItems.HAND_MIRROR.get());
                        output.accept(ModItems.THAUMATURGE_ROBE.get());
                        output.accept(ModItems.THAUMATURGE_LEGGINGS.get());
                        output.accept(ModItems.THAUMATURGE_BOOTS.get());
                        output.accept(ModItems.FORTRESS_HELMET.get());
                        output.accept(ModItems.FORTRESS_HELMET_MASK_GRINNING_DEVIL.get());
                        output.accept(ModItems.FORTRESS_HELMET_MASK_ANGRY_GHOST.get());
                        output.accept(ModItems.FORTRESS_HELMET_MASK_SIPPING_FIEND.get());
                        output.accept(ModItems.FORTRESS_CHESTPLATE.get());
                        output.accept(ModItems.FORTRESS_LEGGINGS.get());
                        output.accept(ModItems.FORTRESS_BOOTS.get());
                        output.accept(ModItems.CULTIST_KNIGHT_HELMET.get());
                        output.accept(ModItems.CULTIST_KNIGHT_CHESTPLATE.get());
                        output.accept(ModItems.CULTIST_KNIGHT_LEGGINGS.get());
                        output.accept(ModItems.CULTIST_CLERIC_HOOD.get());
                        output.accept(ModItems.CULTIST_CLERIC_ROBE.get());
                        output.accept(ModItems.CULTIST_CLERIC_LEGGINGS.get());
                        output.accept(ModItems.CULTIST_PRAETOR_HELMET.get());
                        output.accept(ModItems.CULTIST_PRAETOR_CHESTPLATE.get());
                        output.accept(ModItems.CULTIST_PRAETOR_LEGGINGS.get());
                        output.accept(ModItems.CULTIST_BOOTS.get());
                        output.accept(NodeJarFactory.deterministicCreativeStack(
                                ModItems.JARRED_AURA_NODE.get()
                        ));
                        output.accept(ModItems.THAUMCRAFT_TABLE.get());
                        output.accept(ModItems.ARCANE_WORKBENCH.get());
                        output.accept(ModItems.ARCANE_SPA.get());
                        output.accept(ModItems.ARCANE_BORE.get());
                        output.accept(ModItems.NODE_STABILIZER.get());
                        output.accept(ModItems.ADVANCED_NODE_STABILIZER.get());
                        output.accept(ModItems.NODE_TRANSDUCER.get());
                        output.accept(ModItems.VIS_RELAY.get());
                        output.accept(ModItems.VIS_CHARGE_RELAY.get());
                        output.accept(ModItems.DECONSTRUCTION_TABLE.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("arcane_lamp").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("lamp_growth").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("lamp_fertility").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("item_grate").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("arcane_ear").get());
                        output.accept(ModItems.ARCANE_STONE.get());
                        output.accept(ModItems.ARCANE_STONE_BRICK.get());
                        output.accept(ModItems.ARCANE_STONE_SLAB.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("focal_manipulator").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("paving_stone_of_travel").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("paving_stone_of_warding").get());
                        output.accept(ModItems.CRUCIBLE.get());
                        output.accept(ModItems.ALCHEMICAL_FURNACE.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("arcane_bellows").get());
                        output.accept(ModItems.RUNIC_MATRIX.get());
                        output.accept(ModItems.ARCANE_PEDESTAL.get());
                        output.accept(ModItems.WAND_RECHARGE_PEDESTAL.get());
                        output.accept(ModItems.COMPOUND_RECHARGE_FOCUS.get());
                        output.accept(ModItems.ARCANE_ALEMBIC.get());
                        output.accept(ModItems.ESSENTIA_PHIAL.get());
                        for (PrimalAspect aspect : PrimalAspect.ordered()) {
                            output.accept(EssentiaPhialItem.filled(
                                    ModItems.ESSENTIA_PHIAL.get(), aspect.id()));
                        }
                        output.accept(ModItems.JAR_LABEL.get());
                        output.accept(JarLabelItem.tuned(
                                ModItems.JAR_LABEL.get(), PrimalAspect.AER.id()));
                        output.accept(ModItems.WARDED_JAR.get());
                        output.accept(ModItems.ESSENTIA_TUBE.get());
                        output.accept(ModItems.FILTERED_ESSENTIA_TUBE.get());
                        output.accept(ModItems.RESTRICTED_ESSENTIA_TUBE.get());
                        output.accept(ModItems.ONE_WAY_ESSENTIA_TUBE.get());
                        output.accept(ModItems.ESSENTIA_VALVE.get());
                        output.accept(ModItems.REVERSIBLE_ESSENTIA_TUBE.get());
                        output.accept(ModItems.ESSENTIA_BUFFER.get());
                        output.accept(ModItems.ADVANCED_ESSENTIA_BUFFER.get());
                        output.accept(ModItems.VOID_JAR.get());
                        output.accept(ModItems.ESSENTIA_CENTRIFUGE.get());
                        output.accept(ModItems.ESSENTIA_CRYSTALLIZER.get());
                        output.accept(ModItems.ESSENTIA_RESERVOIR.get());
                        output.accept(ModItems.ESSENTIA_CRYSTAL.get());
                        output.accept(ModItems.MNEMONIC_MATRIX.get());
                        output.accept(ModItems.ALCHEMICAL_CONSTRUCT.get());
                        output.accept(
                                ModItems.ADVANCED_ALCHEMICAL_CONSTRUCT.get()
                        );
                        output.accept(ModItems.SCRIBING_TOOLS.get());
                        output.accept(ModItems.RESEARCH_NOTES.get());
                        output.accept(ModItems.DISCOVERY.get());
                        output.accept(ModItems.AIR_SHARD.get());
                        output.accept(ModItems.FIRE_SHARD.get());
                        output.accept(ModItems.WATER_SHARD.get());
                        output.accept(ModItems.EARTH_SHARD.get());
                        output.accept(ModItems.ORDER_SHARD.get());
                        output.accept(ModItems.ENTROPY_SHARD.get());
                        output.accept(ModItems.BALANCED_SHARD.get());
                        output.accept(ModItems.AIR_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.FIRE_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.WATER_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.EARTH_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.ORDER_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.ENTROPY_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.BALANCED_CRYSTAL_CLUSTER.get());
                        output.accept(ModItems.SALIS_MUNDUS.get());
                        output.accept(ModItems.NITOR.get());
                        output.accept(ModItems.ALUMENTUM.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("thaumium_ingot").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS
                                .get("thaumium_nugget").get());
                        output.accept(ModItems.THAUMIUM_BLOCK.get());
                        output.accept(ModItems.THAUMIUM_SWORD.get());
                        output.accept(ModItems.THAUMIUM_PICKAXE.get());
                        output.accept(ModItems.THAUMIUM_AXE.get());
                        output.accept(ModItems.THAUMIUM_SHOVEL.get());
                        output.accept(ModItems.THAUMIUM_HOE.get());
                        output.accept(ModItems.THAUMIUM_HELMET.get());
                        output.accept(ModItems.THAUMIUM_CHESTPLATE.get());
                        output.accept(ModItems.THAUMIUM_LEGGINGS.get());
                        output.accept(ModItems.THAUMIUM_BOOTS.get());
                        output.accept(ModItems.PICKAXE_OF_THE_CORE.get());
                        output.accept(ModItems.AXE_OF_THE_STREAM.get());
                        output.accept(ModItems.SWORD_OF_THE_ZEPHYR.get());
                        output.accept(ModItems.SHOVEL_OF_THE_EARTHMOVER.get());
                        output.accept(ModItems.HOE_OF_GROWTH.get());
                        output.accept(ModItems.BOOTS_OF_THE_TRAVELLER.get());
                        for (PrimalAspect aspect : PrimalAspect.ordered()) {
                            output.accept(EtherealEssenceItem.create(
                                    ModItems.ETHEREAL_ESSENCE.get(),
                                    aspect,
                                    AuraNodeBreakDrops.ESSENCE_ASPECT_AMOUNT
                            ));
                        }
                        output.accept(ModItems.KNOWLEDGE_FRAGMENT.get());
                        output.accept(ModItems.QUICKSILVER.get());
                        output.accept(ModItems.QUICKSILVER_NUGGET.get());
                        output.accept(ModItems.COPPER_NUGGET.get());
                        output.accept(ModItems.TIN_NUGGET.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("silver_nugget").get());
                        output.accept(ModItems.LEAD_NUGGET.get());
                        output.accept(ModItems.TIN_INGOT.get());
                        output.accept(ModItems.SILVER_INGOT.get());
                        output.accept(ModItems.LEAD_INGOT.get());
                        output.accept(ModItems.NATIVE_IRON_CLUSTER.get());
                        output.accept(ModItems.NATIVE_GOLD_CLUSTER.get());
                        output.accept(ModItems.NATIVE_COPPER_CLUSTER.get());
                        output.accept(ModItems.NATIVE_TIN_CLUSTER.get());
                        output.accept(ModItems.NATIVE_SILVER_CLUSTER.get());
                        output.accept(ModItems.NATIVE_LEAD_CLUSTER.get());
                        output.accept(ModItems.THAUMIC_TALLOW.get());
                        output.accept(ModItems.TALLOW_CANDLE.get());
                        output.accept(ModItems.AMBER.get());
                        output.accept(ModItems.MANA_BEAN.get());
                        output.accept(ModItems.ZOMBIE_BRAIN.get());
                        output.accept(ModItems.TAINTED_GOO.get());
                        output.accept(ModItems.TAINT_TENDRIL.get());
                        output.accept(ModItems.GOLD_COIN.get());
                        output.accept(ModItems.VOID_SEED.get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("void_metal_ingot").get());
                        output.accept(ModItems.ARCANE_RECIPE_COMPONENTS.get("void_nugget").get());
                        output.accept(ModItems.VOID_SWORD.get());
                        output.accept(ModItems.VOID_PICKAXE.get());
                        output.accept(ModItems.VOID_AXE.get());
                        output.accept(ModItems.VOID_SHOVEL.get());
                        output.accept(ModItems.VOID_HOE.get());
                        output.accept(ModItems.VOID_HELMET.get());
                        output.accept(ModItems.VOID_CHESTPLATE.get());
                        output.accept(ModItems.VOID_LEGGINGS.get());
                        output.accept(ModItems.VOID_BOOTS.get());
                        output.accept(ModItems.VOID_ROBE_HOOD.get());
                        output.accept(ModItems.VOID_ROBE_CHESTPLATE.get());
                        output.accept(ModItems.VOID_ROBE_LEGGINGS.get());
                        output.accept(ModItems.ELDRITCH_EYE.get());
                        output.accept(ModItems.CRIMSON_RITES.get());
                        output.accept(ModItems.RUNED_TABLET.get());
                        output.accept(ModItems.PRIMORDIAL_PEARL.get());
                        output.accept(ModItems.PRIMAL_CRUSHER.get());
                        output.accept(ModItems.COMMON_LOOT_BAG.get());
                        output.accept(ModItems.UNCOMMON_LOOT_BAG.get());
                        output.accept(ModItems.RARE_LOOT_BAG.get());
                        output.accept(ModItems.CINNABAR_ORE.get());
                        output.accept(ModItems.AMBER_ORE.get());
                        output.accept(ModItems.AIR_INFUSED_STONE.get());
                        output.accept(ModItems.FIRE_INFUSED_STONE.get());
                        output.accept(ModItems.WATER_INFUSED_STONE.get());
                        output.accept(ModItems.EARTH_INFUSED_STONE.get());
                        output.accept(ModItems.ORDER_INFUSED_STONE.get());
                        output.accept(ModItems.ENTROPY_INFUSED_STONE.get());
                        output.accept(ModItems.DEEPSLATE_AIR_INFUSED_STONE.get());
                        output.accept(ModItems.DEEPSLATE_FIRE_INFUSED_STONE.get());
                        output.accept(ModItems.DEEPSLATE_WATER_INFUSED_STONE.get());
                        output.accept(ModItems.DEEPSLATE_EARTH_INFUSED_STONE.get());
                        output.accept(ModItems.DEEPSLATE_ORDER_INFUSED_STONE.get());
                        output.accept(ModItems.DEEPSLATE_ENTROPY_INFUSED_STONE.get());
                        output.accept(ModItems.GREATWOOD_LOG.get());
                        output.accept(ModItems.GREATWOOD_LEAVES.get());
                        output.accept(ModItems.GREATWOOD_SAPLING.get());
                        output.accept(ModItems.GREATWOOD_PLANKS.get());
                        output.accept(ModItems.GREATWOOD_STAIRS.get());
                        output.accept(ModItems.GREATWOOD_SLAB.get());
                        output.accept(ModItems.SILVERWOOD_LOG.get());
                        output.accept(ModItems.SILVERWOOD_LEAVES.get());
                        output.accept(ModItems.SILVERWOOD_SAPLING.get());
                        output.accept(ModItems.SILVERWOOD_PLANKS.get());
                        output.accept(ModItems.SILVERWOOD_STAIRS.get());
                        output.accept(ModItems.SILVERWOOD_SLAB.get());
                        output.accept(ModItems.SHIMMERLEAF.get());
                        output.accept(ModItems.CINDERPEARL.get());
                        output.accept(ModItems.VISHROOM.get());
                        output.accept(ModItems.MANA_POD.get());
                        output.accept(ModItems.CRUSTED_TAINT.get());
                        output.accept(ModItems.TAINTED_SOIL.get());
                        output.accept(ModItems.TAINT_FIBRES.get());
                        output.accept(ModItems.SHORT_TAINTED_GRASS.get());
                        output.accept(ModItems.TALL_TAINTED_GRASS.get());
                        output.accept(ModItems.SPORE_STALK.get());
                        output.accept(ModItems.FLUX_GOO.get());
                        output.accept(ModItems.FLUX_GAS.get());
                        output.accept(ModItems.OBSIDIAN_TOTEM.get());
                        output.accept(ModItems.OBSIDIAN_TILE.get());
                        output.accept(ModItems.ANCIENT_STONE.get());
                        output.accept(ModItems.ELDRITCH_GLOWING_CRUST.get());
                        output.accept(ModItems.ANCIENT_ROCK.get());
                        output.accept(ModItems.ANCIENT_STAIRS.get());
                        output.accept(ModItems.ANCIENT_SLAB.get());
                        output.accept(ModItems.ANCIENT_CRUST.get());
                        for (LegacyMobKind mob : LegacyMobKind.values()) {
                            if (mob == LegacyMobKind.CONVERTED_VILLAGER
                                    || mob == LegacyMobKind.CRIMSON_INQUISITOR) {
                                continue;
                            }
                            output.accept(ModItems.SPAWN_EGGS.get(mob).get());
                        }
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
