package com.thaumcraftmodern.client;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.item.ThaumometerItem;
import com.thaumcraftmodern.knowledge.KnowledgeAccess;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.InventoryScanPacket;
import com.thaumcraftmodern.registry.ModSounds;
import com.thaumcraftmodern.scan.ScanDefinition;
import com.thaumcraftmodern.scan.ScanRegistry;
import com.thaumcraftmodern.scan.ScanSessionManager;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Thaumic-Tweaks-style inventory aspects and carried-Thaumometer scanning. */
@Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID, value = Dist.CLIENT)
public final class InventoryThaumometerEvents {
    private static String hoverKey = "";
    private static int hoverTicks;
    private static boolean attemptFinished;

    private InventoryThaumometerEvents() {}

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (!(minecraft.screen instanceof AbstractContainerScreen<?> screen)
                || minecraft.player == null
                || !(screen.getMenu().getCarried().getItem()
                instanceof ThaumometerItem)) {
            resetHover();
            return;
        }
        Slot hovered = screen.getSlotUnderMouse();
        if (hovered == null || !hovered.hasItem()
                || hovered.getItem().getItem() instanceof ThaumometerItem) {
            resetHover();
            return;
        }
        ScanRegistry.ItemScanIdentity identity =
                ScanRegistry.identityForItem(hovered.getItem());
        String scanKey = ScanRegistry.knowledgeKey(identity.type(), identity.targetId());
        boolean studied = KnowledgeAccess.get(minecraft.player)
                .map(knowledge -> knowledge.hasScan(scanKey)).orElse(false);
        int menuSlot = screen.getMenu().slots.indexOf(hovered);
        if (!studied && menuSlot >= 0) {
            String candidate = screen.getMenu().containerId + ":" + menuSlot
                    + ":" + scanKey;
            if (!candidate.equals(hoverKey)) {
                hoverKey = candidate;
                hoverTicks = 0;
                attemptFinished = false;
            }
            /*
             * One stable hover gets one scan attempt. If the server rejects
             * the target (unknown aspects, missing prerequisite, and so on),
             * keep it suppressed until the hovered target changes or the
             * carried Thaumometer is put back into a slot. The visible sound
             * ends at REQUIRED_TICKS, while silent heartbeats continue until
             * the authoritative success/failure feedback arrives.
             */
            if (attemptFinished) return;
            if (hoverTicks < ScanSessionManager.REQUIRED_TICKS) {
                hoverTicks++;
                if ((hoverTicks & 1) == 0) {
                    minecraft.level.playLocalSound(minecraft.player.getX(),
                            minecraft.player.getY(), minecraft.player.getZ(),
                            ModSounds.CAMERA_TICKS.get(), SoundSource.PLAYERS,
                            0.2F, 0.45F + minecraft.player.getRandom().nextFloat()
                                    * 0.1F, false);
                }
            }
            // Client and server tick phases are not lock-step. Keep a silent heartbeat after
            // the visible 20-tick scan until the authoritative server sends ScanFeedbackPacket;
            // otherwise the server can stop at 18-19 elapsed ticks and never complete the scan.
            ModNetwork.sendToServer(new InventoryScanPacket(
                    screen.getMenu().containerId, menuSlot));
        } else {
            resetHover();
        }
    }

    @SubscribeEvent
    public static void renderInventoryNotification(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AbstractContainerScreen<?>) {
            ClientScanOverlay.renderScreenNotification(event.getGuiGraphics(),
                    event.getScreen().width, event.getScreen().height);
            if (hoverTicks > 0
                    && hoverTicks < ScanSessionManager.REQUIRED_TICKS) {
                float scale = 0.65F;
                Component text = Component.translatable(
                        "screen.thaumcraftmodern.thaumometer.inventory_scanning");
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(event.getMouseX() + 12,
                        event.getMouseY() - 9, 500.0F);
                event.getGuiGraphics().pose().scale(scale, scale, 1.0F);
                event.getGuiGraphics().drawString(Minecraft.getInstance().font,
                        text, 0, 0, 0xD8C7FF, true);
                event.getGuiGraphics().pose().popPose();
            }
        }
    }

    @SubscribeEvent
    public static void gatherAspectTooltip(RenderTooltipEvent.GatherComponents event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!Screen.hasShiftDown() || event.getItemStack().isEmpty()
                || minecraft.player == null) return;
        ScanRegistry.ItemScanIdentity identity =
                ScanRegistry.identityForItem(event.getItemStack());
        String scanKey = ScanRegistry.knowledgeKey(identity.type(), identity.targetId());
        boolean studied = KnowledgeAccess.get(minecraft.player)
                .map(knowledge -> knowledge.hasScan(scanKey)).orElse(false);
        if (!studied) return;
        ScanDefinition definition = ScanRegistry.findForItem(event.getItemStack())
                .orElse(null);
        if (definition == null || definition.aspects().isEmpty()) return;

        java.util.List<AspectTooltipComponent.Entry> aspects = definition.aspects()
                .stream().map(reward -> AspectRegistryRuntime.find(reward.aspectId())
                        .map(aspect -> new AspectTooltipComponent.Entry(
                                ResourceLocation.tryParse(aspect.icon()), aspect.color(),
                                Component.translatable("aspect.thaumcraftmodern."
                                        + reward.aspectId()), reward.amount()))
                        .orElse(null)).filter(java.util.Objects::nonNull).toList();
        if (!aspects.isEmpty()) {
            event.getTooltipElements().add(Math.min(1,
                    event.getTooltipElements().size()),
                    Either.right(new AspectTooltipComponent(aspects)));
        }
    }

    private static void resetHover() {
        hoverKey = "";
        hoverTicks = 0;
        attemptFinished = false;
    }

    /** Called for the authoritative success/failure response that also drives the TC4 popup. */
    public static void onScanFeedback() {
        if (!hoverKey.isEmpty() && hoverTicks >= ScanSessionManager.REQUIRED_TICKS) {
            attemptFinished = true;
        }
    }

    @Mod.EventBusSubscriber(modid = ThaumcraftModern.MOD_ID,
            bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class Registration {
        @SubscribeEvent
        public static void registerTooltipComponents(
                RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(AspectTooltipComponent.class,
                    ClientAspectTooltipComponent::new);
        }
    }
}
