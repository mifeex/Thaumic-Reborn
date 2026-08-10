package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemCoreType;
import com.thaumcraftmodern.entity.GolemUpgradeType;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** TC4 chest core and upgrade-icon layer, attached to the animated body part. */
public final class GolemCoreRenderLayer<T extends ClassicGolemEntity>
        extends RenderLayer<T, StrawGolemModel<T>> {
    private final ItemRenderer items;

    public GolemCoreRenderLayer(RenderLayerParent<T, StrawGolemModel<T>> parent, ItemRenderer items) {
        super(parent);
        this.items = items;
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffers, int light, T golem,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
            float netHeadYaw, float headPitch) {
        GolemCoreType core = golem.core();
        if (core == null) return;
        poses.pushPose();
        poses.scale(.4F, .4F, .4F);
        getParentModel().translateToBody(poses);
        renderIcon(poses, buffers, light, ModItems.golemCore(core).get().getDefaultInstance(),
                0F, 4F, -6.08F, .4375F);
        int slots = golem.upgradeSlots();
        for (int slot = 0; slot < slots; slot++) {
            GolemUpgradeType upgrade = golem.upgrade(slot);
            ItemStack icon = upgrade == null ? ItemStack.EMPTY
                    : net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                            new net.minecraft.resources.ResourceLocation(
                                    com.thaumcraftmodern.ThaumcraftModern.MOD_ID, "golem_upgrade_" + upgrade.id())).getDefaultInstance();
            if (!icon.isEmpty()) {
                float x = .08F * (slot - (slots - 1) / 2F) / .025F;
                renderIcon(poses, buffers, light, icon, x, 12F, -4F, .25F);
            }
        }
        poses.popPose();
    }

    private void renderIcon(PoseStack poses, MultiBufferSource buffers, int light, ItemStack icon,
            float x, float y, float z, float scale) {
        poses.pushPose();
        poses.translate(x / 16F, y / 16F, z / 16F);
        poses.scale(scale, scale, scale);
        poses.mulPose(Axis.ZP.rotationDegrees(180F));
        poses.mulPose(Axis.YP.rotationDegrees(180F));
        items.renderStatic(icon, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                poses, buffers, null, 0);
        poses.popPose();
    }
}
