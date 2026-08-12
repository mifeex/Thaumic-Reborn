package com.thaumcraftmodern.integration.api;

import com.thaumcraftmodern.aspect.AspectRegistryRuntime;
import com.thaumcraftmodern.client.AspectContainerHudRegistry;
import com.thaumcraftmodern.client.ClassicUiRender;
import com.thaumcraftmodern.scan.AspectReward;
import com.thaumicreborn.api.client.AspectRenderApi;
import com.thaumicreborn.api.client.ClientApiServices;
import com.thaumicreborn.api.client.HudApi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class ThaumicRebornClientApiServices implements ClientApiServices {
    private final AspectRenderApi aspects = new AspectRenderApiImpl();
    private final HudApi hud = new HudApiImpl();

    @Override public AspectRenderApi aspects() { return aspects; }
    @Override public HudApi hud() { return hud; }

    private static final class AspectRenderApiImpl implements AspectRenderApi {
        @Override
        public boolean draw(net.minecraft.client.gui.GuiGraphics graphics,
                            String aspectId, int x, int y, int size, float alpha) {
            var aspect = AspectRegistryRuntime.find(aspectId).orElse(null);
            if (aspect == null) return false;
            var icon = net.minecraft.resources.ResourceLocation.tryParse(aspect.icon());
            if (icon == null) return false;
            ClassicUiRender.drawAspect(graphics, icon, x, y, size, aspect.color(), alpha);
            return true;
        }

        @Override
        public boolean drawTag(net.minecraft.client.gui.GuiGraphics graphics,
                               net.minecraft.client.gui.Font font, String aspectId,
                               int x, int y, int size, int amount, float alpha) {
            var aspect = AspectRegistryRuntime.find(aspectId).orElse(null);
            if (aspect == null) return false;
            var icon = net.minecraft.resources.ResourceLocation.tryParse(aspect.icon());
            if (icon == null) return false;
            ClassicUiRender.drawAspectTag(graphics, font, icon, x, y, size,
                    aspect.color(), Integer.toString(amount), alpha, 0.5F);
            return true;
        }

        @Override public String formatVis(int centivis) {
            return ClassicUiRender.formatVis(centivis);
        }
    }

    private static final class HudApiImpl implements HudApi {
        @Override
        public <T extends BlockEntity> void registerAspectContainer(
                Class<T> type,
                com.thaumicreborn.api.client.HudContainerAdapter<T> adapter) {
            AspectContainerHudRegistry.register(type, (entity, hit) ->
                    adapter.resolve(entity, hit).map(readout ->
                            new AspectContainerHudRegistry.Readout(
                                    readout.aspects().stream()
                                            .map(value -> new AspectReward(
                                                    value.aspectId(), value.amount()))
                                            .toList(), readout.anchor())));
        }

        @Override
        public void drawAspectHud(net.minecraft.client.gui.GuiGraphics graphics,
                                  java.util.List<com.thaumicreborn.api.client.HudAspect> aspects,
                                  int centerX, int bottomY, float alpha, float scale) {
            com.thaumcraftmodern.client.ClientScanOverlay.renderNodeAspects(
                    graphics, net.minecraft.client.Minecraft.getInstance(),
                    aspects.stream().map(value -> new AspectReward(
                            value.aspectId(), value.amount())).toList(),
                    centerX, bottomY, alpha, scale);
        }

        @Override public Vec3 onHitFace(BlockHitResult hit) {
            return AspectContainerHudRegistry.onHitFace(hit);
        }

        @Override public Vec3 aboveBlock(BlockPos position, double height) {
            return AspectContainerHudRegistry.aboveBlock(position, height);
        }
    }
}
