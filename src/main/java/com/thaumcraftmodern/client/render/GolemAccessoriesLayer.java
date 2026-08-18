package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
import com.thaumcraftmodern.entity.GolemDecorationType;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/** Exact TC4 ModelGolemAccessories boxes, driven by the synchronized decoration string. */
public final class GolemAccessoriesLayer<T extends ClassicGolemEntity>
        extends RenderLayer<T, StrawGolemModel<T>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "golem_accessories"), "main");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/entity/models/golem_decoration.png");
    private final ModelPart root;

    public GolemAccessoriesLayer(RenderLayerParent<T, StrawGolemModel<T>> parent, ModelPart root) {
        super(parent);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("fez", CubeListBuilder.create().texOffs(0, 94)
                .addBox(-4.5F, -15F, -6F, 9F, 7F, 9F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(32, 40)
                .addBox(-6.5F, -1F, -7F, 13F, 12F, 13F), PartPose.offset(0F, 30F, 0F));
        root.addOrReplaceChild("plate_left", CubeListBuilder.create().texOffs(0, 44)
                .addBox(-8.5F, -4F, -6.5F, 3F, 6F, 12F), PartPose.offset(0F, 30F, 0F));
        root.addOrReplaceChild("plate_right", CubeListBuilder.create().mirror().texOffs(0, 44)
                .addBox(5.5F, -4F, -6.5F, 3F, 6F, 12F), PartPose.offset(0F, 30F, 0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 110)
                .addBox(-4.5F, -17F, -6F, 9F, 9F, 9F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(0, 80)
                .addBox(-4.5F, -8F, -6F, 9F, 4F, 9F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("visor", CubeListBuilder.create().texOffs(0, 70)
                .addBox(-5F, -8F, -6F, 10F, 5F, 5F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("hat_rim", CubeListBuilder.create().texOffs(36, 114)
                .addBox(-6.5F, -9F, -8F, 13F, 1F, 13F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("dart", CubeListBuilder.create().texOffs(80, 80)
                .addBox(7.9F, 7.5F, -3.5F, 6F, 16F, 7F), PartPose.offset(0F, 30F, 0F));
        root.addOrReplaceChild("hammer", CubeListBuilder.create().texOffs(80, 26)
                .addBox(-13F, 15F, -5F, 6F, 8F, 10F), PartPose.offset(0F, 30F, 0F));
        root.addOrReplaceChild("bow_tie", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.5F, -2F, -6.5F, 17F, 4F, 12F), PartPose.offset(0F, 30F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void render(PoseStack poses, MultiBufferSource buffers, int light, T golem,
            float limbSwing, float limbSwingAmount, float partialTick, float age,
            float yaw, float pitch) {
        if (golem.decorations().isEmpty()) return;
        visible("fez", golem.hasDecoration(GolemDecorationType.FEZ));
        visible("hat", golem.hasDecoration(GolemDecorationType.TOP_HAT));
        visible("hat_rim", golem.hasDecoration(GolemDecorationType.TOP_HAT));
        visible("glasses", golem.hasDecoration(GolemDecorationType.GLASSES));
        visible("visor", golem.hasDecoration(GolemDecorationType.VISOR));
        visible("bow_tie", golem.hasDecoration(GolemDecorationType.BOW_TIE));
        visible("plate", golem.hasDecoration(GolemDecorationType.ARMOR));
        visible("plate_left", golem.hasDecoration(GolemDecorationType.ARMOR));
        visible("plate_right", golem.hasDecoration(GolemDecorationType.ARMOR));
        visible("dart", golem.hasDecoration(GolemDecorationType.DART_LAUNCHER));
        visible("hammer", golem.hasDecoration(GolemDecorationType.HAMMER));

        float headX = golem.core() == null || golem.isInactive() ? .57595867F
                : golem.bootup() > 0F ? golem.bootup() / 57.295776F : pitch / 57.295776F;
        float headY = golem.core() == null || golem.isInactive() || golem.bootup() > 0F
                ? 0F : yaw / 57.295776F;
        for (String name : new String[]{"fez", "hat", "hat_rim", "glasses", "visor"}) {
            ModelPart part = root.getChild(name);
            part.xRot = headX;
            part.yRot = headY;
        }
        root.getChild("dart").xRot = armAngle(golem, false, limbSwing, limbSwingAmount, partialTick);
        root.getChild("hammer").xRot = armAngle(golem, true, limbSwing, limbSwingAmount, partialTick);

        poses.pushPose();
        poses.scale(.4F, .4F, .4F);
        root.render(poses, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poses.popPose();
    }

    private void visible(String name, boolean visible) { root.getChild(name).visible = visible; }

    private static float armAngle(ClassicGolemEntity golem, boolean right, float swing,
            float amount, float partialTick) {
        if (golem.actionTimer() > 0) {
            return -2F + 1.5F * triangle(golem.actionTimer() - partialTick, 10F);
        }
        int timer = right ? golem.rightArmTimer() : golem.leftArmTimer();
        if (timer > 0) return -2F + 1.5F * triangle(timer - partialTick, 10F);
        if (golem.isCarryingForAnimation()) return -1F;
        return (-.2F + (right ? 1.5F : -1.5F) * triangle(swing, 13F)) * amount;
    }

    private static float triangle(float value, float period) {
        return (Math.abs(value % period - period * .5F) - period * .25F) / (period * .25F);
    }
}
