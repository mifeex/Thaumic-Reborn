package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Exact cuboid and UV conversion of TC4 {@code ModelRobe}. */
public final class VoidRobeArmorModel extends HumanoidModel<LivingEntity> {
    public static final ModelLayerLocation OUTER_LAYER = layer("outer");
    public static final ModelLayerLocation INNER_LAYER = layer("inner");
    private static final String DATA =
            "/assets/thaumic_reborn/models/entity/void_robe_armor.csv";

    private final ModelPart frontClothR1;
    private final ModelPart frontClothR2;
    private final ModelPart frontClothL1;
    private final ModelPart frontClothL2;
    private final ModelPart clothBackR1;
    private final ModelPart clothBackR2;
    private final ModelPart clothBackR3;
    private final ModelPart clothBackL1;
    private final ModelPart clothBackL2;
    private final ModelPart clothBackL3;

    public VoidRobeArmorModel(ModelPart root, boolean inner) {
        super(root);
        frontClothR1 = inner ? body.getChild("front_cloth_r1") : null;
        frontClothR2 = inner ? body.getChild("front_cloth_r2") : null;
        frontClothL1 = inner ? body.getChild("front_cloth_l1") : null;
        frontClothL2 = inner ? body.getChild("front_cloth_l2") : null;
        clothBackR1 = inner ? body.getChild("cloth_back_r1") : null;
        clothBackR2 = inner ? body.getChild("cloth_back_r2") : null;
        clothBackR3 = inner ? body.getChild("cloth_back_r3") : null;
        clothBackL1 = inner ? body.getChild("cloth_back_l1") : null;
        clothBackL2 = inner ? body.getChild("cloth_back_l2") : null;
        clothBackL3 = inner ? body.getChild("cloth_back_l3") : null;
    }

    public static LayerDefinition createOuterLayer() {
        return createLayer("outer");
    }

    public static LayerDefinition createInnerLayer() {
        return createLayer("inner");
    }

    private static LayerDefinition createLayer(String variant) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create(),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create(),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create(),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create(),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        Map<String, PartDefinition> parents = Map.of(
                "head", root.getChild("head"),
                "body", root.getChild("body"),
                "rightArm", root.getChild("right_arm"),
                "leftArm", root.getChild("left_arm"),
                "rightLeg", root.getChild("right_leg"),
                "leftLeg", root.getChild("left_leg")
        );
        try (InputStream stream = VoidRobeArmorModel.class
                .getResourceAsStream(DATA)) {
            if (stream == null) {
                throw new IllegalStateException("Missing " + DATA);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                reader.lines()
                        .filter(line -> !line.isBlank()
                                && !line.startsWith("#"))
                        .forEach(line -> addPart(line, variant, parents));
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to read " + DATA,
                    exception
            );
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addPart(
            String line,
            String variant,
            Map<String, PartDefinition> parents
    ) {
        String[] value = line.split(",");
        if (value.length != 18) {
            throw new IllegalStateException("Invalid void robe row " + line);
        }
        if (!"both".equals(value[0]) && !variant.equals(value[0])) {
            return;
        }
        CubeListBuilder cube = CubeListBuilder.create()
                .texOffs(integer(value[3]), integer(value[4]));
        if (Boolean.parseBoolean(value[5])) {
            cube.mirror();
        }
        cube.addBox(
                decimal(value[6]), decimal(value[7]), decimal(value[8]),
                decimal(value[9]), decimal(value[10]), decimal(value[11])
        );
        PartDefinition parent = parents.get(value[1]);
        if (parent == null) {
            throw new IllegalStateException(
                    "Unknown void robe parent " + value[1]
            );
        }
        parent.addOrReplaceChild(
                value[2],
                cube,
                PartPose.offsetAndRotation(
                        decimal(value[12]),
                        decimal(value[13]),
                        decimal(value[14]),
                        decimal(value[15]),
                        decimal(value[16]),
                        decimal(value[17])
                )
        );
    }

    @Override
    public void setupAnim(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        super.setupAnim(
                entity,
                limbSwing,
                limbSwingAmount,
                ageInTicks,
                netHeadYaw,
                headPitch
        );
        if (frontClothR1 == null) {
            return;
        }
        float right = Mth.cos(limbSwing * 0.6662F)
                * 1.4F * limbSwingAmount;
        float left = Mth.cos(limbSwing * 0.6662F + Mth.PI)
                * 1.4F * limbSwingAmount;
        float cloth = Math.min(right, left);
        frontClothR1.xRot = frontClothL1.xRot = cloth - 0.1047198F;
        frontClothR2.xRot = frontClothL2.xRot = cloth - 0.3316126F;
        clothBackR1.xRot = clothBackL1.xRot = -cloth + 0.1047198F;
        clothBackR3.xRot = clothBackL3.xRot = -cloth + 0.2268928F;
        clothBackR2.xRot = clothBackL2.xRot = clothBackL3.xRot;
    }

    private static ModelLayerLocation layer(String variant) {
        return new ModelLayerLocation(
                new ResourceLocation(
                        ThaumcraftModern.MOD_ID,
                        "void_robe_" + variant
                ),
                "main"
        );
    }

    private static int integer(String value) {
        return Integer.parseInt(value);
    }

    private static float decimal(String value) {
        return Float.parseFloat(value);
    }
}
