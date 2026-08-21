package com.thaumcraftmodern.mixin.client;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfirmScreen.class)
public interface ConfirmScreenAccessor {
    @Accessor("callback")
    BooleanConsumer thaumicReborn$getCallback();
}
