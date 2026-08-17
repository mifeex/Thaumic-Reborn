package com.thaumcraftmodern;

import com.mojang.logging.LogUtils;
import com.thaumcraftmodern.arcane.ModArcaneRecipes;
import com.thaumcraftmodern.config.ThaumcraftModernClientConfig;
import com.thaumcraftmodern.config.ThaumcraftModernServerConfig;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.registry.ModBiomeSources;
import com.thaumcraftmodern.registry.ModChunkGenerators;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.registry.ModCreativeTabs;
import com.thaumcraftmodern.registry.ModFeatures;
import com.thaumcraftmodern.registry.ModFluids;
import com.thaumcraftmodern.registry.ModEntities;
import com.thaumcraftmodern.registry.ModEnchantments;
import com.thaumcraftmodern.registry.ModEffects;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModMenus;
import com.thaumcraftmodern.registry.ModLootModifiers;
import com.thaumcraftmodern.registry.ModParticles;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.registry.ModStructures;
import com.thaumcraftmodern.registry.ModVillagers;
import com.thaumcraftmodern.enchantment.ThaumcraftEnchantmentEvents;
import com.thaumcraftmodern.integration.api.ThaumicRebornApiServices;
import com.thaumicreborn.api.ThaumicRebornApi;
import com.thaumcraftmodern.worldgen.LegacyVillagePoolInjector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.registries.MissingMappingsEvent;

@Mod(ThaumcraftModern.MOD_ID)
public final class ThaumcraftModern {
    public static final String MOD_ID = "thaumic_reborn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ThaumcraftModern(FMLJavaModLoadingContext context) {
        ThaumicRebornApi.install(new ThaumicRebornApiServices());
        IEventBus modBus = context.getModEventBus();

        context.registerConfig(
                ModConfig.Type.CLIENT,
                ThaumcraftModernClientConfig.SPEC,
                "thaumic_reborn-client.toml"
        );
        context.registerConfig(
                ModConfig.Type.SERVER,
                ThaumcraftModernServerConfig.SPEC,
                "thaumic_reborn-server.toml"
        );
        ModBlocks.register(modBus);
        ModFluids.register(modBus);
        ModBiomeSources.register(modBus);
        ModChunkGenerators.register(modBus);
        ModFeatures.register(modBus);
        ModStructures.register(modBus);
        ModEffects.register(modBus);
        ModEnchantments.register(modBus);
        ModEntities.register(modBus);
        ModItems.register(modBus);
        ModVillagers.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenus.register(modBus);
        ModLootModifiers.register(modBus);
        ModParticles.register(modBus);
        ModArcaneRecipes.register(modBus);
        ModSounds.register(modBus);
        ModCreativeTabs.register(modBus);
        modBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(LegacyVillagePoolInjector.class);
        MinecraftForge.EVENT_BUS.register(ThaumcraftEnchantmentEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    /** Migrates worlds that contained the removed standalone emergency vent. */
    @SubscribeEvent
    public void remapRemovedEmergencyVent(MissingMappingsEvent event) {
        event.getMappings(Registries.BLOCK, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getPath()
                        .equals("emergency_essentia_vent"))
                .forEach(mapping -> mapping.remap(ModBlocks.ESSENTIA_TUBE.get()));
        event.getMappings(Registries.ITEM, MOD_ID).stream()
                .filter(mapping -> mapping.getKey().getPath()
                        .equals("emergency_essentia_vent"))
                .forEach(mapping -> mapping.remap(ModItems.ESSENTIA_TUBE.get()));
    }
}
