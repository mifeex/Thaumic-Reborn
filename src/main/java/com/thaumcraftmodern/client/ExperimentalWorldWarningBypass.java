package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.ConfirmExperimentalFeaturesScreen;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.thaumcraftmodern.mixin.client.ConfirmExperimentalFeaturesScreenAccessor;
import com.thaumcraftmodern.mixin.client.ConfirmScreenAccessor;

/**
 * Skips only the vanilla warning shown when a new world uses experimental
 * registry data. Outer Lands remains registered normally, while unrelated
 * confirmation and backup screens retain their vanilla behaviour.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ExperimentalWorldWarningBypass {
    static final String EXPERIMENTAL_WARNING_TITLE =
            "selectWorld.warning.experimental.title";
    static final String EXPERIMENTAL_FEATURES_TITLE =
            "selectWorld.experimental.title";

    private ExperimentalWorldWarningBypass() {}

    @SubscribeEvent
    public static void skipExperimentalWorldWarning(ScreenEvent.Opening event) {
        Screen screen = event.getNewScreen();
        if (screen == null || !isExperimentalWorldWarning(screen)) {
            return;
        }
        event.setCanceled(true);
        if (screen instanceof ConfirmExperimentalFeaturesScreen experimental) {
            ((ConfirmExperimentalFeaturesScreenAccessor) experimental)
                    .thaumicReborn$getCallback().accept(true);
        } else if (screen instanceof ConfirmScreen confirm) {
            ((ConfirmScreenAccessor) confirm)
                    .thaumicReborn$getCallback().accept(true);
        }
    }

    static boolean isExperimentalWorldWarning(Screen screen) {
        if (!(screen instanceof ConfirmScreen)
                && !(screen instanceof ConfirmExperimentalFeaturesScreen)) {
            return false;
        }
        if (!(screen.getTitle().getContents()
                instanceof TranslatableContents title)) {
            return false;
        }
        return EXPERIMENTAL_WARNING_TITLE.equals(title.getKey())
                || EXPERIMENTAL_FEATURES_TITLE.equals(title.getKey());
    }
}
