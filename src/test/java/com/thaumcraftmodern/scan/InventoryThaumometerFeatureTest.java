package com.thaumcraftmodern.scan;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InventoryThaumometerFeatureTest {
    private static final Path MAIN = Path.of("src/main/java/com/thaumcraftmodern");

    @Test
    void inventoryScanIsStableAndServerAuthoritative() throws Exception {
        String client = Files.readString(MAIN.resolve(
                "client/InventoryThaumometerEvents.java"));
        String server = Files.readString(MAIN.resolve(
                "scan/InventoryScanService.java"));
        String network = Files.readString(MAIN.resolve("network/ModNetwork.java"));

        assertTrue(client.contains("getMenu().getCarried()"));
        assertTrue(client.contains("slots.indexOf(hovered)"));
        assertTrue(client.contains("if (attemptFinished) return"),
                "A rejected target must stay suppressed after server feedback");
        assertTrue(client.contains("if (hoverTicks < ScanSessionManager.REQUIRED_TICKS)"));
        assertTrue(client.contains("Keep a silent heartbeat"),
                "The client must not stop before the server reaches its own 20 stable ticks");
        assertTrue(client.contains("onScanFeedback()"));
        assertTrue(server.contains("menu.containerId != containerId"));
        assertTrue(server.contains("menu.getCarried().getItem()"
                + " instanceof ThaumometerItem"));
        assertTrue(server.contains("gameTick > previous.lastGameTick"));
        assertTrue(server.contains("ScanSessionManager.REQUIRED_TICKS"));
        assertTrue(server.contains("ScanService.complete"));
        assertTrue(network.contains("InventoryScanPacket.class"));
        String handlers = Files.readString(MAIN.resolve(
                "client/ClientPacketHandlers.java"));
        assertTrue(handlers.contains("InventoryThaumometerEvents.onScanFeedback()"));
    }

    @Test
    void shiftTooltipRequiresPriorScanAndUsesDefinitionAspects()
            throws Exception {
        String client = Files.readString(MAIN.resolve(
                "client/InventoryThaumometerEvents.java"));
        assertTrue(client.contains("Screen.hasShiftDown()"));
        assertTrue(client.contains("knowledge.hasScan(scanKey)"));
        assertTrue(client.contains("identity.knowledgeKey()"),
                "Item NBT variants must check their resolved item knowledge key");
        assertTrue(client.contains("ScanRegistry.findForItem"));
        assertTrue(client.contains("definition.aspects()"));
        assertTrue(client.contains("AspectRegistryRuntime.find"));
        assertTrue(client.contains("Either.right(new AspectTooltipComponent"));
        assertTrue(client.contains("ModSounds.CAMERA_TICKS"));
        assertTrue(client.contains("inventory_scanning"));

        String renderer = Files.readString(MAIN.resolve(
                "client/ClientAspectTooltipComponent.java"));
        assertTrue(renderer.contains("ICON_SIZE = 16"));
        assertTrue(renderer.contains("BACKGROUND = 0xB0100010"));
        assertTrue(renderer.contains("entry.amount()"));
        assertTrue(renderer.contains("ClassicUiRender.drawAspect"));
        assertFalse(renderer.contains("RenderSystem"));

        for (String locale : new String[]{"en_us", "ru_ru"}) {
            Path lang = Path.of("src/main/resources/assets/thaumcraftmodern/lang/"
                    + locale + ".json");
            assertTrue(JsonParser.parseString(Files.readString(lang))
                    .getAsJsonObject().has("tooltip.thaumcraftmodern.aspects"));
            assertTrue(JsonParser.parseString(Files.readString(lang))
                    .getAsJsonObject().has("screen.thaumcraftmodern.thaumometer."
                            + "inventory_scanning"));
        }
    }
}
