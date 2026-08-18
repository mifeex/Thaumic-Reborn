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

/** The translucent ModelJar.core suspended inside the original bore. */
public final class ArcaneBoreJarCoreModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_bore_jar_core"), "main");
    private final ModelPart core;
    public ArcaneBoreJarCoreModel(ModelPart root) { core = root.getChild("core"); }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(0, 0).mirror().addBox(-5, -12, -5, 10, 12, 10), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }
    public void render(PoseStack pose, VertexConsumer vertices, int light, int overlay) {
        core.render(pose, vertices, light, overlay, 1, 1, 1, 0.65F);
    }
}
