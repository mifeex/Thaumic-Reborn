package com.thaumcraftmodern.registry;

import com.mojang.serialization.Codec;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.worldgen.outerlands.OuterLandsChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/** Runtime codecs used by data-driven dimensions. */
public final class ModChunkGenerators {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>>
            CHUNK_GENERATORS = DeferredRegister.create(
                    Registries.CHUNK_GENERATOR,
                    ThaumcraftModern.MOD_ID
            );

    public static final RegistryObject<Codec<? extends ChunkGenerator>>
            OUTER_LANDS = CHUNK_GENERATORS.register(
                    "outer_lands",
                    () -> OuterLandsChunkGenerator.CODEC
            );

    private ModChunkGenerators() {
    }

    public static void register(IEventBus modBus) {
        CHUNK_GENERATORS.register(modBus);
    }
}
