package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Direct port of TC4's ModelResearchTable geometry and 128x64 UV layout.
 * One model is deliberately two blocks wide; only the main half owns a BER.
 */
public final class ResearchTableModel {
    private static final String TABLE = "table";
    private static final String INKWELL = "inkwell";
    private static final String SCROLL_TUBE = "scroll_tube";
    private static final String SCROLL_RIBBON = "scroll_ribbon";

    private final ModelPart table;
    private final ModelPart inkwell;
    private final ModelPart scrollTube;
    private final ModelPart scrollRibbon;

    ResearchTableModel(ModelPart root) {
        table = root.getChild(TABLE);
        inkwell = root.getChild(INKWELL);
        scrollTube = root.getChild(SCROLL_TUBE);
        scrollRibbon = root.getChild(SCROLL_RIBBON);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        CubeListBuilder tableCubes = CubeListBuilder.create()
                .texOffs(0, 0).mirror()
                .addBox(-8.0F, 0.0F, -8.0F, 32.0F, 4.0F, 16.0F)
                .texOffs(0, 24).mirror()
                .addBox(-6.0F, 4.0F, -6.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(0, 24).mirror()
                .addBox(-6.0F, 4.0F, 2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(0, 24).mirror()
                .addBox(18.0F, 4.0F, -6.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(0, 24).mirror()
                .addBox(18.0F, 4.0F, 2.0F, 4.0F, 12.0F, 4.0F)
                .texOffs(24, 24).mirror()
                .addBox(-4.0F, 10.0F, -2.0F, 24.0F, 4.0F, 4.0F);
        root.addOrReplaceChild(TABLE, tableCubes, PartPose.ZERO);

        root.addOrReplaceChild(
                INKWELL,
                CubeListBuilder.create()
                        .texOffs(0, 44).mirror()
                        .addBox(-6.0F, -2.0F, 3.0F, 3.0F, 2.0F, 3.0F),
                PartPose.ZERO
        );
        root.addOrReplaceChild(
                SCROLL_TUBE,
                CubeListBuilder.create()
                        .texOffs(0, 0).mirror()
                        .addBox(-21.0F, -0.5F, -8.0F, 8.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F)
        );
        root.addOrReplaceChild(
                SCROLL_RIBBON,
                CubeListBuilder.create()
                        .texOffs(0, 4).mirror()
                        .addBox(-15.1F, -0.275F, -6.75F, 1.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -2.0F, 2.0F, 0.0F, 10.0F, 0.0F)
        );
        return LayerDefinition.create(mesh, 128, 64);
    }

    void renderTable(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay
    ) {
        table.render(poseStack, vertices, packedLight, packedOverlay);
    }

    void renderInkwell(
            PoseStack poseStack,
            VertexConsumer vertices,
            int packedLight,
            int packedOverlay
    ) {
        inkwell.render(poseStack, vertices, packedLight, packedOverlay);
    }

    void renderScroll(
            PoseStack poseStack,
            VertexConsumer vertices,
            int color,
            int packedLight,
            int packedOverlay
    ) {
        scrollTube.render(poseStack, vertices, packedLight, packedOverlay);
        poseStack.pushPose();
        poseStack.scale(1.2F, 1.2F, 1.2F);
        scrollRibbon.render(
                poseStack,
                vertices,
                packedLight,
                packedOverlay,
                (color >> 16 & 0xFF) / 255.0F,
                (color >> 8 & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                1.0F
        );
        poseStack.popPose();
    }
}
