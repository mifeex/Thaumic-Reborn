package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.GolemUpgradeType;
import com.thaumcraftmodern.entity.TravelingTrunkEntity;
import com.thaumcraftmodern.registry.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class TravelingTrunkRenderer extends MobRenderer<TravelingTrunkEntity, TravelingTrunkModel> {
    private static final ResourceLocation NORMAL = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/entity/models/trunk.png");
    private static final ResourceLocation ANGRY = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/entity/models/trunkangry.png");
    private final net.minecraft.client.renderer.entity.ItemRenderer itemRenderer;

    public TravelingTrunkRenderer(EntityRendererProvider.Context context) {
        super(context, new TravelingTrunkModel(context.bakeLayer(TravelingTrunkModel.LAYER)), .6F);
        itemRenderer = context.getItemRenderer();
    }

    @Override protected void scale(TravelingTrunkEntity trunk, PoseStack poses, float partialTick) {
        float scale = trunk.upgrade() == GolemUpgradeType.TERRA
                ? 2F / 1.33F : 2F / 1.5F;
        float squish = (trunk.previousSquish
                + (trunk.squish - trunk.previousSquish) * partialTick) / 2F;
        float inverse = 1F / (squish + 1F);
        squish /= 1.5F;
        inverse /= 1.4F;
        poses.scale(inverse * scale, .5F / inverse * scale, inverse * scale);
        /* LivingEntityRenderer flips model-space Y before this transform:
         * positive Y therefore lowers the rendered trunk. */
        poses.translate(0F, .5F, 0F);
    }

    @Override public void render(TravelingTrunkEntity trunk, float yaw, float partialTick,
            PoseStack poses, MultiBufferSource buffers, int light) {
        super.render(trunk, yaw, partialTick, poses, buffers, light);
        GolemUpgradeType upgrade = trunk.upgrade();
        if (upgrade == null) return;
        var item = ModItems.golemUpgrade(upgrade).get();
        poses.pushPose();
        poses.translate(0D, .68D, -.53D);
        poses.scale(.22F, .22F, .22F);
        itemRenderer.renderStatic(new ItemStack(item), ItemDisplayContext.FIXED,
                light, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                poses, buffers, trunk.level(), trunk.getId());
        poses.popPose();
    }

    @Override public ResourceLocation getTextureLocation(TravelingTrunkEntity trunk) {
        return trunk.anger() > 0 ? ANGRY : NORMAL;
    }
}
