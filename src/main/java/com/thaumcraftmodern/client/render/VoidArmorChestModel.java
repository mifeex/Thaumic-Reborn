package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** Vanilla outer armor geometry with only the sleeve width reduced. */
public final class VoidArmorChestModel extends HumanoidModel<LivingEntity> {
    public static final float SLEEVE_HORIZONTAL_SCALE = 0.65F;
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "void_chestplate"),
            "outer"
    );

    public VoidArmorChestModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(
                HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F),
                64,
                32
        );
    }

    public void narrowSleevesHorizontally() {
        rightArm.xScale = SLEEVE_HORIZONTAL_SCALE;
        leftArm.xScale = SLEEVE_HORIZONTAL_SCALE;
    }
}
