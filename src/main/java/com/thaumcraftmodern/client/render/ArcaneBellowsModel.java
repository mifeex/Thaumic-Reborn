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

/** Pixel-for-pixel port of TC4 {@code ModelBellows}: three planks, bag and nozzle. */
public final class ArcaneBellowsModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_bellows"),
            "main");
    private final ModelPart bottomPlank;
    private final ModelPart middlePlank;
    private final ModelPart topPlank;
    private final ModelPart bag;
    private final ModelPart nozzle;

    public ArcaneBellowsModel(ModelPart root) {
        bottomPlank = root.getChild("bottom_plank");
        middlePlank = root.getChild("middle_plank");
        topPlank = root.getChild("top_plank");
        bag = root.getChild("bag");
        nozzle = root.getChild("nozzle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        // These offsets and UVs are the original ModelBellows constructor verbatim.
        mesh.getRoot().addOrReplaceChild("bottom_plank", CubeListBuilder.create()
                        .texOffs(0, 0).mirror().addBox(-6, 0, -6, 12, 2, 12),
                PartPose.offset(0, 22, 0));
        mesh.getRoot().addOrReplaceChild("middle_plank", CubeListBuilder.create()
                        .texOffs(0, 0).mirror().addBox(-6, -1, -6, 12, 2, 12),
                PartPose.offset(0, 16, 0));
        mesh.getRoot().addOrReplaceChild("top_plank", CubeListBuilder.create()
                        .texOffs(0, 0).mirror().addBox(-6, 0, -6, 12, 2, 12),
                PartPose.offset(0, 8, 0));
        mesh.getRoot().addOrReplaceChild("bag", CubeListBuilder.create()
                        .texOffs(48, 0).mirror().addBox(-10, -12.03333F, -10, 20, 24, 20),
                PartPose.offset(0, 0.5F, 0));
        mesh.getRoot().addOrReplaceChild("nozzle", CubeListBuilder.create()
                        .texOffs(0, 36).mirror().addBox(-2, -2, 0, 4, 4, 2),
                PartPose.offset(0, 16, 6));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public void render(PoseStack poses, VertexConsumer vertices, int light, int overlay,
            float inflation) {
        float plankScale = 0.125F + inflation * 0.875F;
        poses.pushPose();
        poses.translate(0, 1, 0);
        poses.scale(0.5F, (inflation + 0.1F) / 2.0F, 0.5F);
        bag.render(poses, vertices, light, overlay);
        poses.popPose();

        poses.pushPose();
        poses.translate(0, -plankScale / 2.0F + 0.5F, 0);
        topPlank.render(poses, vertices, light, overlay);
        poses.popPose();
        poses.pushPose();
        poses.translate(0, plankScale / 2.0F - 0.5F, 0);
        bottomPlank.render(poses, vertices, light, overlay);
        poses.popPose();
        middlePlank.render(poses, vertices, light, overlay);
        nozzle.render(poses, vertices, light, overlay);
    }
}
