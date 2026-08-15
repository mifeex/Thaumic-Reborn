package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * TC4's inhabited zombie renderer: the vanilla zombie model with czombie.png.
 */
public final class InhabitedZombieRenderer extends MobRenderer<
        LegacyThaumcraftMob,
        BrainyZombieModel> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID,
            "textures/entity/models/czombie.png"
    );

    public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new BrainyZombieModel(context.bakeLayer(ModelLayers.ZOMBIE)),
                0.5F
        );
        addLayer(new HumanoidArmorLayer<>(
                this,
                new net.minecraft.client.model.HumanoidModel<>(
                        context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)
                ),
                new net.minecraft.client.model.HumanoidModel<>(
                        context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)
                ),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(LegacyThaumcraftMob entity) {
        return TEXTURE;
    }
}
