package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.entity.ClassicGolemEntity;
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

/** TC4 advanced-golem brain, glass jar and evil-eye head overlay. */
public final class AdvancedGolemLayer<T extends ClassicGolemEntity>
        extends RenderLayer<T, StrawGolemModel<T>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "advanced_golem"), "main");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "textures/entity/models/golem_decoration.png");
    private final ModelPart root;

    public AdvancedGolemLayer(RenderLayerParent<T, StrawGolemModel<T>> parent, ModelPart root) {
        super(parent);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("jar", CubeListBuilder.create().texOffs(96, 56)
                .addBox(-4F, -15F, -5.5F, 8F, 4F, 8F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("brain", CubeListBuilder.create().texOffs(96, 70)
                .addBox(-3.5F, -14F, -5F, 7F, 3F, 7F), PartPose.offset(0F, 30F, -2F));
        root.addOrReplaceChild("evil_head", CubeListBuilder.create().texOffs(64, 65)
                .addBox(-4F, -9F, -5.5F, 8F, 7F, 8F), PartPose.offset(0F, 30F, -2F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override public void render(PoseStack poses, MultiBufferSource buffers, int light, T golem,
            float limbSwing, float limbSwingAmount, float partialTick, float age,
            float yaw, float pitch) {
        if (!golem.isAdvanced()) return;
        float x = golem.core() == null || golem.isInactive() ? .57595867F
                : golem.bootup() > 0F ? golem.bootup() / 57.295776F : pitch / 57.295776F;
        float y = golem.core() == null || golem.isInactive() || golem.bootup() > 0F
                ? 0F : yaw / 57.295776F;
        for (ModelPart part : root.getAllParts().toList()) {
            if (part == root) continue;
            part.xRot = x;
            part.yRot = y;
        }
        poses.pushPose();
        poses.scale(.4F, .4F, .4F);
        root.render(poses, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poses.popPose();
    }
}
