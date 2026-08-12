package com.thaumcraftmodern.entity;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public final class WorldContentEntityEvents {
    private WorldContentEntityEvents() {
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
                ModEntities.FACELESS_WITNESS.get(),
                FacelessWitnessEntity.createAttributes().build()
        );
        event.put(ModEntities.STRAW_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.STRAW).build());
        event.put(ModEntities.WOOD_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.WOOD).build());
        event.put(ModEntities.TALLOW_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.TALLOW).build());
        event.put(ModEntities.CLAY_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.CLAY).build());
        event.put(ModEntities.FLESH_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.FLESH).build());
        event.put(ModEntities.STONE_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.STONE).build());
        event.put(ModEntities.IRON_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.IRON).build());
        event.put(ModEntities.THAUMIUM_GOLEM.get(), ClassicGolemEntity.createAttributes(GolemMaterial.THAUMIUM).build());
        event.put(ModEntities.TRAVELING_TRUNK.get(), TravelingTrunkEntity.createAttributes().build());
        for (var entry : ModEntities.entries()) {
            event.put(
                    entry.getValue().get(),
                    LegacyThaumcraftMob.createAttributes(entry.getKey()).build()
            );
        }
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(
            SpawnPlacementRegisterEvent event
    ) {
        for (var entry : ModEntities.entries()) {
            event.register(
                    entry.getValue().get(),
                    entry.getKey().flying()
                            ? SpawnPlacements.Type.NO_RESTRICTIONS
                            : SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    LegacyThaumcraftMob::checkSpawnRules,
                    SpawnPlacementRegisterEvent.Operation.REPLACE
            );
        }
    }
}
