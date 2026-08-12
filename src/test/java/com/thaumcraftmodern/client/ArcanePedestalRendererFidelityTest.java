package com.thaumcraftmodern.client;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class ArcanePedestalRendererFidelityTest {
    private static final Path JAVA = Path.of("src/main/java");

    @Test
    void pedestalRendererIsRegisteredAndKeepsClassicItemTransform()
            throws Exception {
        String clientEvents = source(
                "com/thaumcraftmodern/client/ClientModEvents.java"
        );
        String renderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "ArcanePedestalBlockEntityRenderer.java"
        );

        assertTrue(clientEvents.contains(
                "ModBlockEntities.ARCANE_PEDESTAL.get(),\n"
                        + "                ArcanePedestalBlockEntityRenderer::new"
        ));
        assertTrue(renderer.contains("1.15D + bob"));
        assertTrue(renderer.contains("/ 16.0F) * 0.05F"));
        assertTrue(renderer.contains("ticks % 360.0F"));
        assertTrue(renderer.contains("float scale = 1.0F"));
        assertTrue(renderer.contains("ItemDisplayContext.GROUND"));
        assertEquals(1, occurrences(renderer, "items.renderStatic("));
        assertFalse(renderer.contains("Minecraft.useFancyGraphics()"));

        String rechargeRenderer = source(
                "com/thaumcraftmodern/client/render/"
                        + "WandRechargePedestalBlockEntityRenderer.java"
        );
        assertEquals(1, occurrences(rechargeRenderer, "items.renderStatic("));
        assertFalse(rechargeRenderer.contains("Minecraft.useFancyGraphics()"));
    }

    @Test
    void nodeJarItemDefersToThePedestalInteractionPath() throws Exception {
        String pedestal = source(
                "com/thaumcraftmodern/world/block/ArcanePedestalBlock.java");
        String nodeJar = source(
                "com/thaumcraftmodern/item/JarredAuraNodeItem.java");

        assertTrue(pedestal.contains(
                "public static InteractionResult placeHeldItem("));
        assertTrue(nodeJar.contains(
                "ArcanePedestalBlock.placeHeldItem("));
    }

    private static String source(String relative) throws Exception {
        return Files.readString(JAVA.resolve(relative));
    }

    private static int occurrences(String source, String needle) {
        return source.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
    }
}
