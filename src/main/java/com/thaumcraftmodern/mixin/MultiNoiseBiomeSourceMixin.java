package com.thaumcraftmodern.mixin;

import com.thaumcraftmodern.worldgen.ThaumicRegionalBiomeSelector;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Normal MultiNoiseBiomeSource path used when TerraBlender is absent. */
@Mixin(MultiNoiseBiomeSource.class)
abstract class MultiNoiseBiomeSourceMixin {
    @Inject(
            method = {
                    "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
                    "m_203407_(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;"
            },
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void thaumicReborn$selectRegionalBiome(
            int quartX,
            int quartY,
            int quartZ,
            Climate.Sampler sampler,
            CallbackInfoReturnable<Holder<Biome>> callback
    ) {
        Holder<Biome> original = callback.getReturnValue();
        Holder<Biome> selected = ThaumicRegionalBiomeSelector.select(
                quartX,
                quartZ,
                original
        );
        if (selected != original) {
            callback.setReturnValue(selected);
        }
    }
}
