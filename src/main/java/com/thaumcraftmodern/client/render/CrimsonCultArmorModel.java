package com.thaumcraftmodern.client.render;

import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.LegacyThaumcraftMob;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * Exact modern ModelPart conversion of TC4's ModelKnightArmor, ModelRobe and
 * ModelLeaderArmor. The CSV files preserve the original cuboids, UVs, pivots
 * and Euler rotations instead of approximating the silhouettes with inflated
 * vanilla armor.
 */
public final class CrimsonCultArmorModel
        extends HumanoidModel<LegacyThaumcraftMob> {
    public static final ModelLayerLocation KNIGHT_LAYER = layer("crimson_knight");
    public static final ModelLayerLocation CLERIC_LAYER = layer("crimson_cleric");
    public static final ModelLayerLocation PRAETOR_LAYER = layer("crimson_praetor");
    public static final ModelLayerLocation KNIGHT_LEGGINGS_LAYER =
            layer("crimson_knight_leggings");
    public static final ModelLayerLocation CLERIC_LEGGINGS_LAYER =
            layer("crimson_cleric_leggings");
    public static final ModelLayerLocation PRAETOR_LEGGINGS_LAYER =
            layer("crimson_praetor_leggings");
    public static final ModelLayerLocation BOOTS_LAYER = layer("crimson_boots");

    private static final String MODEL_RESOURCE_ROOT =
            "/assets/thaumic_reborn/models/entity/";
    private static final Set<String> BASIC_BELT_PARTS = Set.of(
            "mbelt", "mbeltb", "mbeltl", "mbeltr"
    );
    private static final Set<String> CLERIC_INNER_BODY_PARTS = Set.of(
            "frontclothr1", "frontclothr2", "frontclothl1", "frontclothl2",
            "clothbackr1", "clothbackr2", "clothbackr3",
            "clothbackl1", "clothbackl2", "clothbackl3"
    );

    public CrimsonCultArmorModel(ModelPart root) {
        super(root);
    }

    /**
     * Mirrors the slot visibility used by TC4's three cultist armor items.
     * Visibility is applied to every descendant so a previously rendered
     * outer model cannot leave jacket leg panels enabled for another slot.
     */
    public void configureForSlot(EquipmentSlot slot) {
        setTreeVisible(head, false);
        setTreeVisible(hat, false);
        setTreeVisible(body, false);
        setTreeVisible(rightArm, false);
        setTreeVisible(leftArm, false);
        setTreeVisible(rightLeg, false);
        setTreeVisible(leftLeg, false);
        switch (slot) {
            case HEAD -> {
                setTreeVisible(head, true);
                setTreeVisible(hat, true);
            }
            case CHEST -> {
                setTreeVisible(body, true);
                setTreeVisible(rightArm, true);
                setTreeVisible(leftArm, true);
            }
            case LEGS -> {
                setTreeVisible(body, true);
                setTreeVisible(rightLeg, true);
                setTreeVisible(leftLeg, true);
            }
            case FEET -> {
                setTreeVisible(rightLeg, true);
                setTreeVisible(leftLeg, true);
            }
            default -> {
            }
        }
    }

    private static void setTreeVisible(ModelPart root, boolean visible) {
        root.getAllParts().forEach(part -> part.visible = visible);
    }

    public static LayerDefinition createKnightLayer() {
        return createLegacyLayer("crimson_knight_armor.csv", false);
    }

    public static LayerDefinition createClericLayer() {
        return createLegacyLayer("crimson_cleric_armor.csv", false);
    }

    public static LayerDefinition createPraetorLayer() {
        return createLegacyLayer("crimson_praetor_armor.csv", false);
    }

    public static LayerDefinition createKnightLeggingsLayer() {
        return createLegacyLayer("crimson_knight_armor.csv", true);
    }

    public static LayerDefinition createClericLeggingsLayer() {
        return createLegacyLayer("crimson_cleric_armor.csv", true);
    }

    public static LayerDefinition createPraetorLeggingsLayer() {
        return createLegacyLayer("crimson_praetor_armor.csv", true);
    }

    public static LayerDefinition createBootsLayer() {
        MeshDefinition mesh = emptyHumanoidMesh();
        PartDefinition root = mesh.getRoot();
        CubeDeformation deformation = new CubeDeformation(1.0F);
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
                                deformation),
                PartPose.offset(-1.9F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .mirror()
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
                                deformation),
                PartPose.offset(1.9F, 12.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition createLegacyLayer(
            String resourceName,
            boolean leggingsOnly
    ) {
        // All three TC4 constructors clear the inherited torso and leg cube
        // lists. Their f=0.5 variants select attached cloth/belt parts only.
        MeshDefinition mesh = emptyHumanoidMesh();
        PartDefinition root = mesh.getRoot();
        Map<String, PartDefinition> parents = Map.of(
                "head", root.getChild("head"),
                "body", root.getChild("body"),
                "rightArm", root.getChild("right_arm"),
                "leftArm", root.getChild("left_arm"),
                "rightLeg", root.getChild("right_leg"),
                "leftLeg", root.getChild("left_leg")
        );
        loadOriginalParts(resourceName, parents, leggingsOnly);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static MeshDefinition emptyHumanoidMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "hat",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create(),
                PartPose.ZERO
        );
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
        return mesh;
    }

    private static void loadOriginalParts(
            String resourceName,
            Map<String, PartDefinition> parents,
            boolean leggingsOnly
    ) {
        String path = MODEL_RESOURCE_ROOT + resourceName;
        try (InputStream stream =
                     CrimsonCultArmorModel.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing crimson model data " + path);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                reader.lines()
                        .filter(line -> !line.isBlank() && !line.startsWith("#"))
                        .filter(line -> isAttachedInOriginal(
                                resourceName, leggingsOnly, line))
                        .forEach(line -> addOriginalPart(line, parents, path));
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read crimson model data " + path,
                    exception
            );
        }
    }

    private static boolean isAttachedInOriginal(
            String resourceName,
            boolean leggingsOnly,
            String line
    ) {
        String[] value = line.split(",", 3);
        String parent = value[0];
        String name = value[1];
        boolean cleric = resourceName.contains("cleric");
        if (parent.equals("rightLeg") || parent.equals("leftLeg")) {
            return !cleric || !name.equals("focipouch") || leggingsOnly;
        }
        if (!parent.equals("body")) {
            return !leggingsOnly;
        }
        if (BASIC_BELT_PARTS.contains(name)) {
            return true;
        }
        if (cleric && CLERIC_INNER_BODY_PARTS.contains(name)) {
            return leggingsOnly;
        }
        return !leggingsOnly;
    }

    private static void addOriginalPart(
            String line,
            Map<String, PartDefinition> parents,
            String source
    ) {
        String[] value = line.split(",");
        if (value.length != 17) {
            throw new IllegalStateException(
                    "Invalid crimson model row in " + source + ": " + line
            );
        }
        PartDefinition parent = parents.get(value[0]);
        if (parent == null) {
            throw new IllegalStateException(
                    "Unknown crimson model parent " + value[0] + " in " + source
            );
        }
        CubeListBuilder cube = CubeListBuilder.create()
                .texOffs(integer(value[2]), integer(value[3]));
        if (Boolean.parseBoolean(value[4])) {
            cube.mirror();
        }
        cube.addBox(
                decimal(value[5]),
                decimal(value[6]),
                decimal(value[7]),
                decimal(value[8]),
                decimal(value[9]),
                decimal(value[10])
        );
        parent.addOrReplaceChild(
                value[1],
                cube,
                PartPose.offsetAndRotation(
                        decimal(value[11]),
                        decimal(value[12]),
                        decimal(value[13]),
                        decimal(value[14]),
                        decimal(value[15]),
                        decimal(value[16])
                )
        );
    }

    private static int integer(String value) {
        return Integer.parseInt(value);
    }

    private static float decimal(String value) {
        int suffix = value.endsWith("F") || value.endsWith("f")
                ? value.length() - 1
                : value.length();
        return Float.parseFloat(value.substring(0, suffix));
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(
                new ResourceLocation(
                        ThaumcraftModern.MOD_ID,
                        path
                ),
                "main"
        );
    }
}
