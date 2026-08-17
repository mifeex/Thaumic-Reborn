package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Prevents natural Outer Lands population from selecting the labyrinth roof. */
@Mod.EventBusSubscriber(
        modid = ThaumcraftModern.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class OuterLandsSpawnEvents {
    private OuterLandsSpawnEvents() {
    }

    @SubscribeEvent
    public static void restrictNaturalSpawnsToMaze(
            MobSpawnEvent.PositionCheck event
    ) {
        if (!OuterLandsSpawnRules.isOuterLands(event.getLevel())
                || event.getSpawnType() == MobSpawnType.SPAWNER) {
            return;
        }
        BlockPos position = BlockPos.containing(
                event.getX(), event.getY(), event.getZ()
        );
        if (!OuterLandsSpawnRules.isEnclosedMazePosition(
                event.getLevel(), position
        )) {
            event.setResult(Event.Result.DENY);
        }
    }
}
