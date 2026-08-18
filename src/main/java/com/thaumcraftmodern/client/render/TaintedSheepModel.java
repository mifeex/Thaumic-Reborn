package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Direct 1.20 model-layer port of TC4 ModelTaintSheep1/ModelTaintSheep2. */
public final class TaintedSheepModel
        extends HierarchicalModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation BASE_LAYER = layer("base");
    public static final ModelLayerLocation FUR_LAYER = layer("fur");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public TaintedSheepModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        rightHindLeg = root.getChild("right_hind_leg");
        leftHindLeg = root.getChild("left_hind_leg");
        rightFrontLeg = root.getChild("right_front_leg");
        leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBaseLayer() {
        return createLayer(false);
    }

    public static LayerDefinition createFurLayer() {
        return createLayer(true);
    }

    private static LayerDefinition createLayer(boolean fur) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation headInflation = new CubeDeformation(fur ? 0.6F : 0.0F);
        CubeDeformation bodyInflation = new CubeDeformation(fur ? 1.75F : 0.0F);
        CubeDeformation legInflation = new CubeDeformation(fur ? 0.5F : 0.0F);

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0).addBox(
                        -3.0F, -4.0F, fur ? -4.0F : -6.0F,
                        6.0F, 6.0F, fur ? 6.0F : 8.0F,
                        headInflation
                ),
                PartPose.offset(0.0F, 6.0F, -8.0F)
        );
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(28, 8).addBox(
                        -4.0F, -10.0F, -7.0F,
                        8.0F, 16.0F, 6.0F,
                        bodyInflation
                ),
                PartPose.offsetAndRotation(
                        0.0F, 5.0F, 2.0F,
                        Mth.HALF_PI, 0.0F, 0.0F
                )
        );
        /*
         * TC4 ModelTaintSheep2 keeps ModelQuadruped(12)'s 12-pixel base
         * legs. ModelTaintSheep1 replaces only the wool pass with short,
         * inflated 6-pixel cuffs over the upper half of those legs.
         */
        float legHeight = fur ? 6.0F : 12.0F;
        addLeg(root, "right_hind_leg", -3.0F, 7.0F,
                legHeight, legInflation);
        addLeg(root, "left_hind_leg", 3.0F, 7.0F,
                legHeight, legInflation);
        addLeg(root, "right_front_leg", -3.0F, -5.0F,
                legHeight, legInflation);
        addLeg(root, "left_front_leg", 3.0F, -5.0F,
                legHeight, legInflation);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void addLeg(
            PartDefinition root,
            String name,
            float x,
            float z,
            float height,
            CubeDeformation inflation
    ) {
        root.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(0, 16).addBox(
                        -2.0F, 0.0F, -2.0F,
                        4.0F, height, 4.0F,
                        inflation
                ),
                PartPose.offset(x, 12.0F, z)
        );
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(
            LegacyThaumcraftMob entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F)
                * 1.4F * limbSwingAmount;
        leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI)
                * 1.4F * limbSwingAmount;
        rightFrontLeg.xRot = leftHindLeg.xRot;
        leftFrontLeg.xRot = rightHindLeg.xRot;
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(
                new ResourceLocation(
                        ThaumcraftModern.MOD_ID,
                        "tainted_sheep"
                ),
                name
        );
    }
}
