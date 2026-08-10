package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

/** Direct modern equivalent of TC4's two {@code ModelCube(int)} instances. */
public final class RunicMatrixCubeModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID, "runic_matrix_cube"), "main");

    private final ModelPart solid;
    private final ModelPart overlay;

    public RunicMatrixCubeModel(ModelPart root) {
        solid = root.getChild("solid");
        overlay = root.getChild("overlay");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("solid",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        mesh.getRoot().addOrReplaceChild("overlay",
                CubeListBuilder.create().texOffs(0, 32).mirror()
                        .addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void renderSolid(PoseStack pose, VertexConsumer vertices,
            int light, int packedOverlay) {
        solid.render(pose, vertices, light, packedOverlay, 1, 1, 1, 1);
    }

    public void renderOverlay(PoseStack pose, VertexConsumer vertices,
            int light, int packedOverlay, float alpha) {
        overlay.render(pose, vertices, light, packedOverlay,
                0.8F, 0.1F, 1.0F, alpha);
    }
}
