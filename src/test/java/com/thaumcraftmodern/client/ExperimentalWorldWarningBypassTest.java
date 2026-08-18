package com.thaumcraftmodern.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class ExperimentalWorldWarningBypassTest {
    @Test
    void recognizesOnlyExperimentalWorldCreationWarning() {
        ConfirmScreen experimental = new ConfirmScreen(ignored -> {},
                Component.translatable(
                        ExperimentalWorldWarningBypass.EXPERIMENTAL_WARNING_TITLE),
                Component.literal("question"));
        ConfirmScreen deprecated = new ConfirmScreen(ignored -> {},
                Component.translatable("selectWorld.warning.deprecated.title"),
                Component.literal("question"));
        ConfirmScreen unrelated = new ConfirmScreen(ignored -> {},
                Component.literal("Experimental Features"),
                Component.literal("question"));

        assertTrue(ExperimentalWorldWarningBypass
                .isExperimentalWorldWarning(experimental));
        assertFalse(ExperimentalWorldWarningBypass
                .isExperimentalWorldWarning(deprecated));
        assertFalse(ExperimentalWorldWarningBypass
                .isExperimentalWorldWarning(unrelated));
    }
}
