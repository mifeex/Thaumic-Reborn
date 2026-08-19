package com.thaumcraftmodern.mixin;

import com.mojang.datafixers.util.Pair;
import com.thaumcraftmodern.worldgen.ThaumicOverworldBiomeParameters;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
abstract class OverworldBiomeBuilderMixin {
    @ModifyVariable(
            method = {
                    "addBiomes(Ljava/util/function/Consumer;)V",
                    "m_187175_(Ljava/util/function/Consumer;)V"
            },
            at = @At("HEAD"),
            argsOnly = true,
            remap = false
    )
    private Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>
    thaumicReborn$addThaumicSurfaceBiomes(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper
    ) {
        return ThaumicOverworldBiomeParameters.wrapVanillaMapper(mapper);
    }
}
