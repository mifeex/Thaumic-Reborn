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
    public static final ModelLayerLocation BOOTS_LAYER = layer("crimson_boots");

    private static final String MODEL_RESOURCE_ROOT =
            "/assets/thaumic_reborn/models/entity/";

    public CrimsonCultArmorModel(ModelPart root) {
        super(root);
    }

    /**
     * Vanilla enables the body root for leggings so its texture can provide a
     * waistband. The restored cult models instead attach the full robe or
     * breastplate geometry to that root, so allowing the vanilla visibility
     * pass to expose it makes every pair of leggings repeat the chest piece.
     */
    public void suppressChestGeometryForLeggings(EquipmentSlot slot) {
        if (slot == EquipmentSlot.LEGS) {
            body.getAllParts().skip(1).forEach(part -> part.visible = false);
        }
    }

    public static LayerDefinition createKnightLayer() {
        return createLegacyLayer("crimson_knight_armor.csv");
    }

    public static LayerDefinition createClericLayer() {
        return createLegacyLayer("crimson_cleric_armor.csv");
    }

    public static LayerDefinition createPraetorLayer() {
        return createLegacyLayer("crimson_praetor_armor.csv");
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

    private static LayerDefinition createLegacyLayer(String resourceName) {
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
        loadOriginalParts(resourceName, parents);
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
            Map<String, PartDefinition> parents
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
                        .forEach(line -> addOriginalPart(line, parents, path));
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Unable to read crimson model data " + path,
                    exception
            );
        }
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
