package com.thaumcraftmodern.worldgen.outerlands;

import com.thaumcraftmodern.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

/** Configures the TC4 feature-14 web nest spawner. */
public final class OuterLandsMindSpiderSpawners {
    private OuterLandsMindSpiderSpawners() {
    }

    public static boolean configure(
            ServerLevelAccessor level,
            BlockPos position
    ) {
        if (!(level.getBlockEntity(position)
                instanceof SpawnerBlockEntity spawner)) {
            return false;
        }
        if (!isConfigured(spawner)) {
            spawner.setEntityId(ModEntities.MIND_SPIDER.get(), level.getRandom());
            spawner.setChanged();
        }
        return true;
    }

    public static boolean isConfigured(SpawnerBlockEntity spawner) {
        CompoundTag root = spawner.getSpawner().save(new CompoundTag());
        String entityId = root.getCompound("SpawnData")
                .getCompound("entity").getString("id");
        ResourceLocation expected = ForgeRegistries.ENTITY_TYPES.getKey(
                ModEntities.MIND_SPIDER.get()
        );
        return expected != null && expected.toString().equals(entityId);
    }
}
