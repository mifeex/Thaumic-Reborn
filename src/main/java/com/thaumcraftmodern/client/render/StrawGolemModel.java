package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 ModelGolem box dimensions; the original renders the whole rig at 0.4 scale. */
public final class StrawGolemModel<T extends ClassicGolemEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "straw_golem"), "main");
    private final ModelPart root, head, body, rightArm, leftArm, rightLeg, leftLeg;

    public StrawGolemModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head"); body = root.getChild("body"); rightArm = root.getChild("right_arm"); leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg"); leftLeg = root.getChild("left_leg");
    }

    public void translateToBody(PoseStack poses) { body.translateAndRotate(poses); }
    public void translateToRightArm(PoseStack poses) { rightArm.translateAndRotate(poses); }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition(); PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0,0).addBox(-4,-11,-5.5F,8,9,8), PartPose.offset(0,30,-2));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0,40).addBox(-8,-2,-6,16,12,11)
                .texOffs(0,70).addBox(-4.5F,10,-3,9,5,6, new CubeDeformation(.5F)), PartPose.offset(0,30,0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60,21).addBox(-12,-2.5F,-3,4,25,6), PartPose.offset(0,30,0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror().texOffs(60,21).addBox(8,-2.5F,-3,4,25,6), PartPose.offset(0,30,0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(37,0).addBox(-3.5F,-3,-3,6,16,5), PartPose.offset(-4,48,0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(37,0).addBox(-3.5F,-3,-3,6,16,5), PartPose.offset(5,48,0));
        return LayerDefinition.create(mesh,128,128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float age, float yaw, float pitch) {
        head.yRot = 0F; head.xRot = 0.57595867F;
        rightLeg.xRot = leftLeg.xRot = 0F;
        rightLeg.yRot = leftLeg.yRot = 0F;
        rightArm.xRot = leftArm.xRot = 0F;
        rightArm.zRot = leftArm.zRot = 0F;
        if (entity.core() == null || entity.bootup() < 0F) return;
        if (entity.isInactive()) {
            head.xRot = 0.57595867F;
        } else if (entity.bootup() > 0F) {
            head.xRot = entity.bootup() / 57.295776F;
        } else {
            head.yRot = yaw / 57.295776F;
            head.xRot = pitch / 57.295776F;
        }
        rightLeg.xRot = -1.5F * triangle(limbSwing,13) * limbSwingAmount;
        leftLeg.xRot = 1.5F * triangle(limbSwing,13) * limbSwingAmount;
        if (entity.core() == com.thaumcraftmodern.entity.GolemCoreType.ALCHEMY) {
            float spread = (1F - (.5F + Math.min(64, entity.carryLimit()) / 128F)) * 25F;
            leftArm.zRot = spread / 57.295776F;
            rightArm.zRot = -spread / 57.295776F;
        }
        if (entity.actionTimer() > 0) {
            float angle = -2F + 1.5F * triangle(entity.actionTimer(), 5F);
            rightArm.xRot = leftArm.xRot = angle;
        } else if (entity.leftArmTimer() > 0 || entity.rightArmTimer() > 0) {
            if (entity.leftArmTimer() > 0) leftArm.xRot = -2F + 1.5F * triangle(entity.leftArmTimer(), 20F);
            if (entity.rightArmTimer() > 0) rightArm.xRot = -2F + 1.5F * triangle(entity.rightArmTimer(), 20F);
        } else if (entity.isCarryingForAnimation()
                || entity.core() == com.thaumcraftmodern.entity.GolemCoreType.LIQUID) {
            rightArm.xRot = leftArm.xRot = -1F;
        } else {
            rightArm.xRot = (-.2F + 1.5F * triangle(limbSwing,13)) * limbSwingAmount;
            leftArm.xRot = (-.2F - 1.5F * triangle(limbSwing,13)) * limbSwingAmount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer out, int light, int overlay,
            float red, float green, float blue, float alpha) {
        poses.pushPose(); poses.scale(.4F,.4F,.4F); root.render(poses,out,light,overlay,red,green,blue,alpha); poses.popPose();
    }
    private static float triangle(float value,float period){return (Math.abs(value%period-period*.5F)-period*.25F)/(period*.25F);}
}
