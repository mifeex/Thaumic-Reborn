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

/** Verbatim cuboids, pivots and UV offsets from TC4 ModelBore/Base/Emit. */
public final class ArcaneBoreModel {
    public static final ModelLayerLocation BORE_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_bore"), "bore");
    public static final ModelLayerLocation EMITTER_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_bore"), "emitter");
    public static final ModelLayerLocation SUPPORT_LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "arcane_bore"), "support");
    private final ModelPart boreBase;
    private final ModelPart boreNozzle;
    private final ModelPart emitter;
    private final ModelPart emitterKnob;
    private final ModelPart support;
    private final ModelPart supportNozzle;

    public ArcaneBoreModel(
            ModelPart boreRoot,
            ModelPart emitterRoot,
            ModelPart supportRoot
    ) {
        boreBase = boreRoot.getChild("bore_base");
        boreNozzle = boreRoot.getChild("bore_nozzle");
        emitter = emitterRoot.getChild("emitter");
        emitterKnob = emitterRoot.getChild("emitter_knob");
        support = supportRoot.getChild("support");
        supportNozzle = supportRoot.getChild("support_nozzle");
    }

    /** The bundled byte-exact Bore.png is the original atlas at 2x size. */
    public static LayerDefinition createBoreLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var boreBase = root.addOrReplaceChild("bore_base", CubeListBuilder.create()
                .texOffs(0, 32).mirror().addBox(-6, 0, -6, 12, 2, 12)
                .texOffs(0, 0).mirror().addBox(-2, 2, -5.5F, 4, 8, 1)
                .texOffs(0, 0).mirror().addBox(-2, 2, 4.5F, 4, 8, 1), PartPose.ZERO);
        boreBase.addOrReplaceChild("crossbar", CubeListBuilder.create()
                .texOffs(0, 48).mirror().addBox(-1, -1, -6, 2, 2, 12),
                PartPose.offset(0, 8, 0));
        root.addOrReplaceChild("bore_nozzle", CubeListBuilder.create()
                .texOffs(30, 14).mirror().addBox(4, -2.5F, -2.5F, 4, 5, 5)
                .texOffs(0, 14).mirror().addBox(-2, -4, -4, 6, 8, 8),
                PartPose.offset(0, 8, 0));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** TC4 ModelBoreEmit assigns every part a 128x64 UV grid. */
    public static LayerDefinition createEmitterLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("emitter", CubeListBuilder.create()
                .texOffs(56, 16).mirror().addBox(-2, 0, -2, 4, 1, 4)
                .texOffs(56, 16).mirror().addBox(-2, -8, -2, 4, 1, 4)
                .texOffs(56, 24).mirror().addBox(-3, -4, -3, 6, 1, 6)
                .texOffs(56, 0).mirror().addBox(-1, -7, -1, 2, 11, 2),
                PartPose.offset(0, 8, 0));
        root.addOrReplaceChild("emitter_knob", CubeListBuilder.create()
                .texOffs(66, 0).mirror().addBox(-2, -4, -2, 4, 4, 4),
                PartPose.offset(0, 16, 0));
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** TC4 ModelBoreBase assigns every part a 128x64 UV grid. */
    public static LayerDefinition createSupportLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("support", CubeListBuilder.create()
                .texOffs(64, 24).mirror().addBox(-8, 0, -8, 16, 2, 16)
                .texOffs(64, 24).mirror().addBox(-8, 14, -8, 16, 2, 16)
                .texOffs(84, 42).mirror().addBox(-2.5F, 2, -2.5F, 5, 12, 5)
                .texOffs(64, 42).mirror().addBox(-7, 2, -7, 4, 12, 4)
                .texOffs(64, 42).mirror().addBox(-7, 2, 3, 4, 12, 4)
                .texOffs(64, 42).mirror().addBox(3, 2, 3, 4, 12, 4)
                .texOffs(64, 42).mirror().addBox(3, 2, -7, 4, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("support_nozzle", CubeListBuilder.create()
                .texOffs(106, 42).mirror().addBox(2.5F, -2, -2, 5, 4, 4)
                .texOffs(106, 51).mirror().addBox(7, -2.5F, -2.5F, 1, 5, 5),
                PartPose.offset(0, 8, 0));
        return LayerDefinition.create(mesh, 128, 64);
    }

    public void renderSupport(PoseStack pose, VertexConsumer vertices, int light, int overlay) {
        support.render(pose, vertices, light, overlay);
    }
    public void renderSupportNozzle(PoseStack pose, VertexConsumer vertices, int light, int overlay) {
        supportNozzle.render(pose, vertices, light, overlay);
    }
    public void renderBoreBase(PoseStack pose, VertexConsumer vertices, int light, int overlay) {
        boreBase.render(pose, vertices, light, overlay);
    }
    public void renderBoreNozzle(PoseStack pose, VertexConsumer vertices, int light, int overlay) {
        boreNozzle.render(pose, vertices, light, overlay);
    }
    public void renderEmitter(PoseStack pose, VertexConsumer vertices, int light, int overlay,
            boolean focus) {
        emitter.render(pose, vertices, light, overlay);
        if (focus) emitterKnob.render(pose, vertices, light, overlay);
    }
}
