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

/**
 * Direct model-layer port of Minecraft 1.7.10 ModelVillager (obfuscated
 * client class {@code bik}), used by TC4's RenderTaintVillager.
 */
public final class TaintedVillagerModel
        extends HierarchicalModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "tainted_villager"
            ),
            "main"
    );

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart arms;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public TaintedVillagerModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        arms = root.getChild("arms");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(0, 0).addBox(
                        -4.0F, -10.0F, -4.0F,
                        8.0F, 10.0F, 8.0F
                ),
                PartPose.ZERO
        );
        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(24, 0).addBox(
                        -1.0F, -1.0F, -6.0F,
                        2.0F, 4.0F, 2.0F
                ),
                PartPose.offset(0.0F, -2.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 20).addBox(
                                -4.0F, 0.0F, -3.0F,
                                8.0F, 12.0F, 6.0F
                        )
                        .texOffs(0, 38).addBox(
                                -4.0F, 0.0F, -3.0F,
                                8.0F, 18.0F, 6.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "arms",
                CubeListBuilder.create()
                        .texOffs(44, 22).addBox(
                                -8.0F, -2.0F, -2.0F,
                                4.0F, 8.0F, 4.0F
                        )
                        .texOffs(44, 22).addBox(
                                4.0F, -2.0F, -2.0F,
                                4.0F, 8.0F, 4.0F
                        )
                        .texOffs(40, 38).addBox(
                                -4.0F, 2.0F, -2.0F,
                                8.0F, 4.0F, 4.0F
                        ),
                PartPose.offsetAndRotation(
                        0.0F, 3.0F, -1.0F,
                        -0.75F, 0.0F, 0.0F
                )
        );

        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create().texOffs(0, 22).addBox(
                        -2.0F, 0.0F, -2.0F,
                        4.0F, 12.0F, 4.0F
                ),
                PartPose.offset(-2.0F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create().texOffs(0, 22).mirror().addBox(
                        -2.0F, 0.0F, -2.0F,
                        4.0F, 12.0F, 4.0F
                ),
                PartPose.offset(2.0F, 12.0F, 0.0F)
        );

        /* TC4's 128x128 PNG is a 2x HD version of this 64x64 UV layout. */
        return LayerDefinition.create(mesh, 64, 64);
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

        arms.x = 0.0F;
        arms.y = 3.0F;
        arms.z = -1.0F;
        arms.xRot = -0.75F;

        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F)
                * 1.4F * limbSwingAmount * 0.5F;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI)
                * 1.4F * limbSwingAmount * 0.5F;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
    }
}
