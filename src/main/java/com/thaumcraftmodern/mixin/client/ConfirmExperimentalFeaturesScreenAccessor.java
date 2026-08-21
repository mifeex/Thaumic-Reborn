package com.thaumcraftmodern.mixin.client;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfirmExperimentalFeaturesScreen.class)
public interface ConfirmExperimentalFeaturesScreenAccessor {
    @Accessor("callback")
    BooleanConsumer thaumicReborn$getCallback();
}
