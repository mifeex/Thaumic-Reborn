package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/** Standard outer armor with only the full-length robe sleeves narrowed. */
public final class ThaumaturgeRobeArmorModel
        extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation OUTER_LAYER =
            new ModelLayerLocation(
                    new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            "thaumaturge_robe_outer"
                    ),
                    "main"
            );

    public ThaumaturgeRobeArmorModel(
            net.minecraft.client.model.geom.ModelPart root
    ) {
        super(root);
    }

    public static LayerDefinition createOuterLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(
                new CubeDeformation(1.0F),
                0.0F
        );
        PartDefinition root = mesh.getRoot();
        CubeDeformation sleeve = new CubeDeformation(0.5F);
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, sleeve),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror()
                        .addBox(-1.0F, -2.0F, -2.0F,
                                4.0F, 12.0F, 4.0F, sleeve),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 32);
    }
}
