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
    public static final ModelLayerLocation BOOTS_LAYER =
            new ModelLayerLocation(
                    new ResourceLocation(
                            ThaumcraftModern.MOD_ID,
                            "thaumaturge_robe_boots"
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

    /**
     * Modern outer armor expands every leg cube by a full pixel. With TC4's
     * robe UVs that makes the two dark boots overlap into one wide cuboid.
     * Keep the outer depth/height, but shrink X by one tenth of a pixel on
     * either side. Vanilla's leg pivots are only 3.8 pixels apart, so any
     * non-negative X deformation joins both boots across the centre.
     */
    public static LayerDefinition createBootsLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(
                new CubeDeformation(-0.1F, 0.5F, 0.5F), 0.0F), 64, 32);
    }
}
