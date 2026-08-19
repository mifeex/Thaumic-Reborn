package com.thaumcraftmodern.mixin;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.worldgen.ThaumicRegionalBiomeSelector;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicBoolean;

/** Optional bridge for TerraBlender's positional ParameterList lookup. */
@Mixin(value = Climate.ParameterList.class, priority = 900)
abstract class TerraBlenderParameterListMixin {
    @Unique
    private static final AtomicBoolean thaumicReborn$bridgeLogged =
            new AtomicBoolean();
    @Unique
    private static final AtomicBoolean thaumicReborn$replacementLogged =
            new AtomicBoolean();

    @Inject(
            method = "findValuePositional(Lnet/minecraft/world/level/biome/Climate$TargetPoint;III)Ljava/lang/Object;",
            at = @At("RETURN"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void thaumicReborn$selectRegionalBiomeAfterTerraBlender(
            Climate.TargetPoint target,
            int quartX,
            int quartY,
            int quartZ,
            CallbackInfoReturnable<Object> callback
    ) {
        if (thaumicReborn$bridgeLogged.compareAndSet(false, true)) {
            ThaumcraftModern.LOGGER.info(
                    "TerraBlender positional biome bridge is active"
            );
        }
        Object value = callback.getReturnValue();
        if (!(value instanceof Holder<?> holder)
                || !(holder.value() instanceof Biome)) {
            return;
        }
        @SuppressWarnings("unchecked")
        Holder<Biome> original = (Holder<Biome>) holder;
        Holder<Biome> selected = ThaumicRegionalBiomeSelector.select(
                quartX,
                quartZ,
                original
        );
        if (selected != original) {
            if (thaumicReborn$replacementLogged.compareAndSet(false, true)) {
                ThaumcraftModern.LOGGER.info(
                        "TerraBlender bridge selected its first TC4 biome at "
                                + "quart {}, {}",
                        quartX,
                        quartZ
                );
            }
            callback.setReturnValue(selected);
        }
    }
}
