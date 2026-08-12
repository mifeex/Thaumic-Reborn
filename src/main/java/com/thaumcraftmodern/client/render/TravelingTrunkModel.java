package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.TravelingTrunkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Direct 1.20 geometry port of TC4 ModelTrunk. */
public final class TravelingTrunkModel extends EntityModel<TravelingTrunkEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "traveling_trunk"), "main");
    private final ModelPart root;
    private final ModelPart lid;
    private final ModelPart knob;

    public TravelingTrunkModel(ModelPart root) {
        this.root = root;
        lid = root.getChild("lid");
        knob = lid.getChild("knob");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 19)
                .addBox(0F, 0F, 0F, 14F, 10F, 14F), PartPose.offset(-7F, 6F, -7F));
        PartDefinition lid = root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0F, -5F, -14F, 14F, 5F, 14F), PartPose.offset(-7F, 7F, 7F));
        lid.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1F, -2F, -15F, 2F, 4F, 1F), PartPose.offset(7F, 0F, 0F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override public void setupAnim(TravelingTrunkEntity entity, float limbSwing,
            float limbSwingAmount, float age, float yaw, float pitch) {
        float partial = age - entity.tickCount;
        float openness = Mth.lerp(partial, entity.previousLidAngle, entity.lidAngle);
        openness = 1F - openness;
        openness = 1F - openness * openness * openness;
        lid.xRot = -(openness * Mth.HALF_PI);
    }

    @Override public void renderToBuffer(PoseStack poses, VertexConsumer consumer, int light,
            int overlay, float red, float green, float blue, float alpha) {
        root.render(poses, consumer, light, overlay, red, green, blue, alpha);
    }
}
