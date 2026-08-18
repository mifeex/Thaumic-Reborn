package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Skips only the vanilla warning shown when a new world uses experimental
 * registry data. Outer Lands remains registered normally, while unrelated
 * confirmation and backup screens retain their vanilla behaviour.
 */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class ExperimentalWorldWarningBypass {
    static final String EXPERIMENTAL_WARNING_TITLE =
            "selectWorld.warning.experimental.title";

    private static Screen pendingScreen;
    private static Button pendingProceed;

    private ExperimentalWorldWarningBypass() {}

    @SubscribeEvent
    public static void skipExperimentalWorldWarning(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (!isExperimentalWorldWarning(screen) || pendingScreen == screen) {
            return;
        }

        Button proceed = event.getListenersList().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> CommonComponents.GUI_YES.equals(button.getMessage()))
                .findFirst()
                .orElse(null);
        if (proceed == null) {
            return;
        }

        pendingScreen = screen;
        pendingProceed = proceed;
    }

    @SubscribeEvent
    public static void acceptPendingWarning(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || pendingScreen == null) {
            return;
        }

        Screen screen = pendingScreen;
        Button proceed = pendingProceed;
        pendingScreen = null;
        pendingProceed = null;
        if (Minecraft.getInstance().screen == screen && proceed != null) {
            proceed.onPress();
        }
    }

    static boolean isExperimentalWorldWarning(Screen screen) {
        if (!(screen instanceof ConfirmScreen)
                || !(screen.getTitle().getContents()
                instanceof TranslatableContents title)) {
            return false;
        }
        return EXPERIMENTAL_WARNING_TITLE.equals(title.getKey());
    }
}
