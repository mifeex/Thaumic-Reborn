package com.thaumcraftmodern.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.GolemUpgradeType;
import com.thaumcraftmodern.world.menu.GolemMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Exact TC4 guigolem.png layout with material-colored filters and server toggles. */
public final class GolemScreen extends AbstractContainerScreen<GolemMenu> {
    private static final float TEXT_SCALE = .5F;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/gui/guigolem.png");

    public GolemScreen(GolemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        menu.refreshPageIndices();
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ClassicGolemEntity golem = menu.golem();
        if (golem == null || !golem.isAlive()) {
            if (minecraft != null && minecraft.player != null) minecraft.player.closeContainer();
            return;
        }
        graphics.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);

        // Render the actual configured entity so material, core and upgrade layers match the
        // golem standing in the world. The original GUI reserves the area left of the blurb.
        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, leftPos + 21, topPos + 67, 27,
                (float) (leftPos + 21 - mouseX), (float) (topPos + 31 - mouseY), golem);

        int typeV = golem.material().ordinal() * 24;
        int visible = Math.min(menu.visibleFilters(), Math.max(0, menu.filterSlots() - menu.page() * 6));
        for (int view = 0; view < visible; view++) {
            graphics.blit(BACKGROUND, leftPos + 96 + view / 2 * 28, topPos + 12 + view % 2 * 31,
                    184, typeV, 24, 24, 256, 256);
            if (golem.upgradeAmount(GolemUpgradeType.ORDO) > 0) {
                graphics.blit(BACKGROUND, leftPos + 96 + view / 2 * 28, topPos + 4 + view % 2 * 31,
                        72, 168, 24, 12, 256, 256);
                int color = golem.filterColor(menu.page() * 6 + view);
                if (color >= 0) {
                    int rgb = net.minecraft.world.item.DyeColor.byId(color).getTextColor();
                    graphics.fill(leftPos + 105 + view / 2 * 28, topPos + 7 + view % 2 * 31,
                            leftPos + 111 + view / 2 * 28, topPos + 13 + view % 2 * 31, 0xFF000000 | rgb);
                }
            }
            ItemStack icon = menu.filterIcon(view);
            if (!icon.isEmpty()) graphics.renderItem(icon, leftPos + 100 + view / 2 * 28,
                    topPos + 16 + view % 2 * 31);
            if (clickAt(mouseX, mouseY, 100 + view / 2 * 28, 16 + view % 2 * 31, 16, 16)) {
                graphics.fill(leftPos + 100 + view / 2 * 28, topPos + 16 + view % 2 * 31,
                        leftPos + 116 + view / 2 * 28, topPos + 32 + view % 2 * 31, 0x80FFFFFF);
            }
        }
        if (menu.maxPage() > 0) {
            graphics.blit(BACKGROUND, leftPos + 111, topPos + 68,
                    0, menu.page() > 0 ? 200 : 208, 24, 8, 256, 256);
            graphics.blit(BACKGROUND, leftPos + 135, topPos + 68,
                    24, menu.page() < menu.maxPage() ? 200 : 208, 24, 8, 256, 256);
        }
        drawCoreToggles(graphics, golem);
        RenderSystem.disableBlend();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ClassicGolemEntity golem = menu.golem();
        if (golem == null || golem.core() == null) return;
        graphics.pose().pushPose();
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1F);
        int lineY = scaled(6);
        for (var line : font.split(Component.translatable("golemblurb." + golem.core().legacyId() + ".text"), 96)) {
            graphics.drawString(font, line, scaled(43), lineY, 0xEEEEEE, true);
            lineY += 9;
            if (lineY > scaled(64)) break;
        }
        if (menu.maxPage() > 0) {
            graphics.drawString(font, (menu.page() + 1) + "/" + (menu.maxPage() + 1), scaled(160), scaled(69), 0xDDDDDD, false);
        }
        if (golem.core() == GolemCoreType.FILL) {
            drawText(graphics, "fill", 72, 54, 0xEEEECC);
        } else if (golem.core() == GolemCoreType.USE) {
            drawText(graphics, "block", 53, 40, 0xEEEEEE);
            drawText(graphics, "right_click", 53, 50, 0xEEEEEE);
            drawText(graphics, "not_sneaking", 53, 60, 0xEEEEEE);
        } else if (golem.core() == GolemCoreType.GUARD && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0) {
            drawGuardLabel(graphics, "monsters", 6, 0xFFCCCC);
            drawGuardLabel(graphics, "animals", 22, 0xFFFFCC);
            drawGuardLabel(graphics, "players", 38, 0xCCCCFF);
            drawGuardLabel(graphics, "creepers", 54, 0xCCFFCC);
        }
        if (golem.upgradeAmount(GolemUpgradeType.PERDITIO) > 0 && canSort(golem.core())) {
            drawText(graphics, "ore", 14, 72, 0xDDDDDD);
            drawText(graphics, "damage", 68, 72, 0xDDDDDD);
            drawText(graphics, "nbt", 122, 72, 0xDDDDDD);
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        int view = hoveredFilterView(mouseX, mouseY);
        if (view < 0) return;
        ItemStack icon = menu.filterIcon(view);
        if (!icon.isEmpty()) graphics.renderTooltip(font, icon, mouseX, mouseY);
        else graphics.renderTooltip(font,
                Component.translatable("gui.thaumcraftmodern.golem.filter_hint"), mouseX, mouseY);
    }

    private void drawGuardLabel(GuiGraphics graphics, String key, int y, int color) {
        drawText(graphics, key, 122, y, color);
    }

    private void drawText(GuiGraphics graphics, String key, int x, int y, int color) {
        graphics.drawString(font, Component.translatable("gui.thaumcraftmodern.golem." + key),
                scaled(x), scaled(y), color, true);
    }

    private static int scaled(int coordinate) { return Math.round(coordinate / TEXT_SCALE); }

    private void drawCoreToggles(GuiGraphics graphics, ClassicGolemEntity golem) {
        if (golem.core() == GolemCoreType.FILL) drawToggle(graphics, 62, 54, !golem.toggle(0));
        if (golem.core() == GolemCoreType.USE) {
            drawToggle(graphics, 42, 40, !golem.toggle(0));
            drawToggle(graphics, 42, 50, !golem.toggle(1));
            drawToggle(graphics, 42, 60, !golem.toggle(2));
        }
        if (golem.core() == GolemCoreType.GUARD && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0) {
            drawToggle(graphics, 104, 5, golem.canAttackHostiles());
            drawToggle(graphics, 104, 21, golem.canAttackAnimals());
            drawToggle(graphics, 104, 37, golem.canAttackPlayers());
            drawToggle(graphics, 104, 53, golem.canAttackCreepers());
        }
        if (golem.upgradeAmount(GolemUpgradeType.PERDITIO) > 0 && canSort(golem.core())) {
            drawToggle(graphics, 4, 72, golem.toggle(5));
            drawToggle(graphics, 58, 72, golem.toggle(6));
            drawToggle(graphics, 112, 72, golem.toggle(7));
        }
    }

    private void drawToggle(GuiGraphics graphics, int x, int y, boolean enabled) {
        graphics.blit(BACKGROUND, leftPos + x, topPos + y, 8, 168, 8, 8, 256, 256);
        if (enabled) graphics.blit(BACKGROUND, leftPos + x, topPos + y, 8, 176, 8, 8, 256, 256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (clickAt(mouseX, mouseY, 111, 68, 24, 8) && menu.page() > 0) return sendButton(66);
        if (clickAt(mouseX, mouseY, 135, 68, 24, 8) && menu.page() < menu.maxPage()) return sendButton(67);
        ClassicGolemEntity golem = menu.golem();
        if (golem != null && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0) {
            int visible = Math.min(menu.visibleFilters(), Math.max(0, menu.filterSlots() - menu.page() * 6));
            for (int view = 0; view < visible; view++) {
                int filter = menu.page() * 6 + view;
                if (clickAt(mouseX, mouseY, 96 + view / 2 * 28, 4 + view % 2 * 31, 12, 12)) return sendButton(filter);
                if (clickAt(mouseX, mouseY, 108 + view / 2 * 28, 4 + view % 2 * 31, 12, 12)) {
                    return sendButton(filter + menu.filterSlots());
                }
            }
        }
        if (golem != null) {
            int visible = Math.min(menu.visibleFilters(), Math.max(0, menu.filterSlots() - menu.page() * 6));
            for (int view = 0; view < visible; view++) {
                if (clickAt(mouseX, mouseY, 100 + view / 2 * 28, 16 + view % 2 * 31, 16, 16)) {
                    return sendButton(GolemMenu.FILTER_BUTTON_BASE + menu.page() * 6 + view);
                }
            }
        }
        if (golem != null && golem.core() == GolemCoreType.FILL && clickAt(mouseX, mouseY, 62, 54, 8, 8)) return sendButton(50);
        if (golem != null && golem.core() == GolemCoreType.USE) {
            if (clickAt(mouseX, mouseY, 42, 40, 8, 8)) return sendButton(50);
            if (clickAt(mouseX, mouseY, 42, 50, 8, 8)) return sendButton(51);
            if (clickAt(mouseX, mouseY, 42, 60, 8, 8)) return sendButton(52);
        }
        if (golem != null && golem.core() == GolemCoreType.GUARD
                && golem.upgradeAmount(GolemUpgradeType.ORDO) > 0) {
            if (clickAt(mouseX, mouseY, 104, 5, 8, 8)) return sendButton(51);
            if (clickAt(mouseX, mouseY, 104, 21, 8, 8)) return sendButton(52);
            if (clickAt(mouseX, mouseY, 104, 37, 8, 8)) return sendButton(53);
            if (clickAt(mouseX, mouseY, 104, 53, 8, 8)) return sendButton(54);
        }
        if (golem != null && golem.upgradeAmount(GolemUpgradeType.PERDITIO) > 0 && canSort(golem.core())) {
            if (clickAt(mouseX, mouseY, 4, 72, 8, 8)) return sendButton(55);
            if (clickAt(mouseX, mouseY, 58, 72, 8, 8)) return sendButton(56);
            if (clickAt(mouseX, mouseY, 112, 72, 8, 8)) return sendButton(57);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean sendButton(int id) {
        if (minecraft == null || minecraft.gameMode == null) return false;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        return true;
    }

    private boolean clickAt(double mouseX, double mouseY, int x, int y, int width, int height) {
        double relX = mouseX - leftPos - x;
        double relY = mouseY - topPos - y;
        return relX >= 0 && relY >= 0 && relX < width && relY < height;
    }

    private int hoveredFilterView(double mouseX, double mouseY) {
        int visible = Math.min(menu.visibleFilters(), Math.max(0, menu.filterSlots() - menu.page() * 6));
        for (int view = 0; view < visible; view++) {
            if (clickAt(mouseX, mouseY, 100 + view / 2 * 28, 16 + view % 2 * 31, 16, 16)) return view;
        }
        return -1;
    }

    private static boolean canSort(GolemCoreType core) {
        return core == GolemCoreType.FILL || core == GolemCoreType.EMPTY || core == GolemCoreType.GATHER
                || core == GolemCoreType.USE || core == GolemCoreType.SORTING;
    }
}
