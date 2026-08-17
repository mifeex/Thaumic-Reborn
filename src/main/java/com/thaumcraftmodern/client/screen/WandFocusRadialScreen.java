package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.focus.WandFocusService;
import com.thaumicreborn.api.focus.FocusItem;
import com.thaumcraftmodern.item.WandItem;
import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.ChangeWandFocusPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** TC4-style mouse-bound selector shown while the focus key is held. */
public final class WandFocusRadialScreen extends Screen {
    private static final ResourceLocation RADIAL = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/misc/radial.png");
    private static final ResourceLocation RADIAL_2 = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/misc/radial2.png");
    private static final int TEXTURE_SIZE = 256;
    private static final float OPEN_STEP = 0.1F;
    private static final List<String> ORIGINAL_ORDER = List.of(
            "focus_fire", "focus_excavation", "focus_frost",
            "focus_shock", "focus_trade", "focus_primal", "focus_hellbat",
            "focus_portable_hole", "focus_warding");

    private final List<ItemStack> foci = new ArrayList<>();
    private final List<Float> focusScales = new ArrayList<>();
    private ItemStack currentFocus = ItemStack.EMPTY;
    private int hoveredIndex = -1;
    private float radialScale;
    private boolean closing;
    private boolean sent;

    public WandFocusRadialScreen() {
        super(Component.translatable("screen.thaumic_reborn.focus_radial"));
    }

    @Override
    protected void init() {
        if (minecraft == null || minecraft.player == null) return;
        foci.clear();
        focusScales.clear();
        ItemStack held = minecraft.player.getMainHandItem();
        currentFocus = WandFocusService.focusStack(held)
                .map(stack -> stack.copyWithCount(1)).orElse(ItemStack.EMPTY);
        minecraft.player.getInventory().items.stream()
                .filter(stack -> stack.getItem() instanceof FocusItem)
                .sorted(Comparator.comparingInt(WandFocusRadialScreen::sortIndex))
                .forEach(stack -> {
                    boolean duplicate = foci.stream().anyMatch(existing ->
                            ItemStack.isSameItemSameTags(existing, stack));
                    if (!duplicate) {
                        foci.add(stack.copyWithCount(1));
                        focusScales.add(1.0F);
                    }
                });
    }

    private static int sortIndex(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return Integer.MAX_VALUE;
        int index = ORIGINAL_ORDER.indexOf(key.getPath());
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    @Override
    public void tick() {
        if (minecraft == null || minecraft.player == null
                || !(minecraft.player.getMainHandItem().getItem() instanceof WandItem wand)
                || !wand.form().acceptsFocus()) {
            onClose();
            return;
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY,
                       float partialTick) {
        radialScale = Mth.clamp(radialScale + (closing ? -OPEN_STEP : OPEN_STEP),
                0.0F, 1.0F);
        if (closing && radialScale <= 0.0F) {
            onClose();
            return;
        }

        int centerX = width / 2;
        int centerY = height / 2;
        float radius = 16.0F + foci.size() * 2.5F;
        hoveredIndex = hovered(mouseX, mouseY, centerX, centerY, radius);

        float rotation = minecraft == null || minecraft.player == null
                ? 0.0F : (minecraft.player.tickCount % 720) / 2.0F + partialTick;
        drawRadial(graphics, RADIAL, centerX, centerY,
                radius * 2.75F * radialScale, rotation, 0.5F);
        drawRadial(graphics, RADIAL_2, centerX, centerY,
                radius * 2.55F * radialScale, -rotation, 0.5F);

        if (!currentFocus.isEmpty()) {
            graphics.renderItem(currentFocus, centerX - 8, centerY - 8);
        }

        for (int index = 0; index < foci.size(); index++) {
            float target = index == hoveredIndex && !closing ? 1.3F : 1.0F;
            float scale = Mth.lerp(0.25F, focusScales.get(index), target);
            focusScales.set(index, scale);
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / foci.size();
            float x = centerX + (float) Math.cos(angle) * radius * radialScale;
            float y = centerY + (float) Math.sin(angle) * radius * radialScale;
            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 100.0F);
            graphics.pose().scale(scale * radialScale, scale * radialScale, 1.0F);
            graphics.renderItem(foci.get(index), -8, -8);
            graphics.pose().popPose();
        }

        ItemStack tooltip = hoveredIndex >= 0 ? foci.get(hoveredIndex)
                : isOverCenter(mouseX, mouseY, centerX, centerY) ? currentFocus
                : ItemStack.EMPTY;
        if (!tooltip.isEmpty()) {
            graphics.renderTooltip(font, tooltip, centerX - 4,
                    centerY + Math.round(radius) + 20);
        }
    }

    private static void drawRadial(GuiGraphics graphics, ResourceLocation texture,
                                   int centerX, int centerY, float size,
                                   float rotation, float alpha) {
        if (size <= 0.0F) return;
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(rotation));
        float scale = size / TEXTURE_SIZE;
        graphics.pose().scale(scale, scale, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(texture, -TEXTURE_SIZE / 2, -TEXTURE_SIZE / 2,
                0, 0, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        graphics.pose().popPose();
    }

    private int hovered(int mouseX, int mouseY, int centerX, int centerY,
                        float radius) {
        if (closing || radialScale < 0.5F || foci.isEmpty()) return -1;
        for (int index = 0; index < foci.size(); index++) {
            double angle = -Math.PI / 2.0D + Math.PI * 2.0D * index / foci.size();
            double x = centerX + Math.cos(angle) * radius * radialScale;
            double y = centerY + Math.sin(angle) * radius * radialScale;
            if (Math.abs(mouseX - x) <= 10.0D && Math.abs(mouseY - y) <= 10.0D) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isOverCenter(int mouseX, int mouseY,
                                        int centerX, int centerY) {
        return Math.abs(mouseX - centerX) <= 10
                && Math.abs(mouseY - centerY) <= 10;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !closing) {
            beginClosing();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (WandFocusKeyEvents.matchesFocusKey(keyCode, scanCode)) {
            beginClosing();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void beginClosing() {
        if (closing) return;
        sendHovered();
        closing = true;
    }

    private void sendHovered() {
        if (sent || hoveredIndex < 0 || hoveredIndex >= foci.size()) return;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(
                foci.get(hoveredIndex).getItem());
        if (id != null) {
            ModNetwork.sendToServer(new ChangeWandFocusPacket(id.toString()));
            sent = true;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
