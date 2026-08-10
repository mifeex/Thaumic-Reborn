package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.item.DiscoveryItem;
import com.thaumcraftmodern.world.block.ResearchTableBlock;
import com.thaumcraftmodern.world.block.entity.ResearchTableBlockEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public final class ResearchTableBlockEntityRenderer
        implements BlockEntityRenderer<ResearchTableBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "research_table"
            ),
            "main"
    );
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/models/restable.png"
            );
    private static final ResourceLocation PARCHMENT_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/misc/parchment.png"
            );
    private static final ResourceLocation NOTES_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/models/restable2.png"
            );
    private static final ResourceLocation QUILL_TEXTURE =
            new ResourceLocation(
                    ThaumcraftModern.MOD_ID,
                    "textures/block/tablequill.png"
            );
    // Research-table decoration placement. X runs along the two-block tabletop;
    // Y is negative above its surface because the classic model is flipped;
    // Z runs from the front edge to the back edge.
    private static final double PARCHMENT_CENTER_X = 0.4D;
    private static final double PARCHMENT_CENTER_Y = -0.012D;
    private static final double PARCHMENT_CENTER_Z = 0.2D;
    private static final float PARCHMENT_WIDTH = 0.60F;
    private static final float PARCHMENT_DEPTH = 0.48F;
    private static final float PARCHMENT_ROTATION = 105.0F;

    private static final double QUILL_CENTER_X = -0.12D;
    private static final double QUILL_CENTER_Y = 0.0D;
    private static final double QUILL_CENTER_Z = 0.25D;
    private static final float QUILL_MIN_X = -0.20F;
    private static final float QUILL_TOP_Y = -0.78F;
    private static final float QUILL_MAX_X = 0.20F;
    private static final float QUILL_BOTTOM_Y = -0.05F;
    private static final float QUILL_ROTATION = 15.0F;

    private final ResearchTableModel model;

    public ResearchTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new ResearchTableModel(context.bakeLayer(LAYER));
    }

    @Override
    public void render(
            ResearchTableBlockEntity table,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (!table.getBlockState().hasProperty(ResearchTableBlock.FACING)) {
            return;
        }

        float yaw = switch (table.getBlockState().getValue(ResearchTableBlock.FACING)) {
            case EAST -> 0.0F;
            case SOUTH -> 90.0F;
            case WEST -> 180.0F;
            case NORTH -> 270.0F;
            default -> 0.0F;
        };

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.0D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));

        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderTable(poseStack, vertices, packedLight, packedOverlay);
        ItemStack tools = table.items().getStackInSlot(
                ResearchTableBlockEntity.SCRIBING_TOOLS_SLOT
        );
        if (!tools.isEmpty()) {
            model.renderInkwell(poseStack, vertices, packedLight, packedOverlay);
            renderQuill(poseStack, buffers, packedLight, packedOverlay);
        }

        ItemStack notes = table.items().getStackInSlot(
                ResearchTableBlockEntity.NOTES_SLOT
        );
        if (notes.getItem() instanceof DiscoveryItem) {
            int notesColor = DiscoveryItem.color(notes);
            model.renderScroll(
                    poseStack,
                    buffers.getBuffer(RenderType.entityCutoutNoCull(NOTES_TEXTURE)),
                    notesColor,
                    packedLight,
                    packedOverlay
            );
        } else {
            renderParchmentStack(
                    poseStack,
                    buffers,
                    packedOverlay
            );
        }
        poseStack.popPose();
    }

    /** Draws the ordinary six-sheet state used until research is completed. */
    private static void renderParchmentStack(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedOverlay
    ) {
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityCutoutNoCull(PARCHMENT_TEXTURE)
        );
        for (int layer = 0; layer < 6; layer++) {
            poseStack.pushPose();
            poseStack.translate(
                    PARCHMENT_CENTER_X,
                    PARCHMENT_CENTER_Y - layer * 0.012D,
                    PARCHMENT_CENTER_Z
            );
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    PARCHMENT_ROTATION + (layer % 3) * 2.0F
            ));
            poseStack.scale(PARCHMENT_WIDTH, 1.0F, PARCHMENT_DEPTH);
            renderHorizontalQuad(
                    poseStack,
                    vertices,
                    0xFFFFFF,
                    LightTexture.FULL_BRIGHT,
                    packedOverlay
            );
            poseStack.popPose();
        }
    }

    private static void renderQuill(
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(
                QUILL_CENTER_X,
                QUILL_CENTER_Y,
                QUILL_CENTER_Z
        );
        poseStack.mulPose(Axis.YP.rotationDegrees(QUILL_ROTATION));
        VertexConsumer vertices = buffers.getBuffer(
                RenderType.entityCutoutNoCull(QUILL_TEXTURE)
        );
        renderVerticalQuad(
                poseStack,
                vertices,
                QUILL_MIN_X,
                QUILL_TOP_Y,
                QUILL_MAX_X,
                QUILL_BOTTOM_Y,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
    }

    private static void renderHorizontalQuad(
            PoseStack poseStack,
            VertexConsumer vertices,
            int color,
            int packedLight,
            int packedOverlay
    ) {
        PoseStack.Pose pose = poseStack.last();
        vertex(
                vertices, pose,
                -0.5F, 0.0F, -0.5F,
                0.0F, 0.0F,
                0.0F, -1.0F, 0.0F,
                color,
                packedLight, packedOverlay
        );
        vertex(
                vertices, pose,
                -0.5F, 0.0F, 0.5F,
                0.0F, 1.0F,
                0.0F, -1.0F, 0.0F,
                color,
                packedLight, packedOverlay
        );
        vertex(
                vertices, pose,
                0.5F, 0.0F, 0.5F,
                1.0F, 1.0F,
                0.0F, -1.0F, 0.0F,
                color,
                packedLight, packedOverlay
        );
        vertex(
                vertices, pose,
                0.5F, 0.0F, -0.5F,
                1.0F, 0.0F,
                0.0F, -1.0F, 0.0F,
                color,
                packedLight, packedOverlay
        );
    }

    private static void renderVerticalQuad(
            PoseStack poseStack,
            VertexConsumer vertices,
            float minX,
            float minY,
            float maxX,
            float maxY,
            int packedLight,
            int packedOverlay
    ) {
        PoseStack.Pose pose = poseStack.last();
        vertex(
                vertices, pose,
                minX, minY, 0.0F,
                0.0F, 0.0F,
                0.0F, 0.0F, 1.0F,
                0xFFFFFF,
                packedLight, packedOverlay
        );
        vertex(
                vertices, pose,
                minX, maxY, 0.0F,
                0.0F, 1.0F,
                0.0F, 0.0F, 1.0F,
                0xFFFFFF,
                packedLight, packedOverlay
        );
        vertex(
                vertices, pose,
                maxX, maxY, 0.0F,
                1.0F, 1.0F,
                0.0F, 0.0F, 1.0F,
                0xFFFFFF,
                packedLight, packedOverlay
        );
        vertex(
                vertices, pose,
                maxX, minY, 0.0F,
                1.0F, 0.0F,
                0.0F, 0.0F, 1.0F,
                0xFFFFFF,
                packedLight, packedOverlay
        );
    }

    private static void vertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            float normalX,
            float normalY,
            float normalZ,
            int color,
            int packedLight,
            int packedOverlay
    ) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        vertices.vertex(position, x, y, z)
                .color(
                        color >> 16 & 0xFF,
                        color >> 8 & 0xFF,
                        color & 0xFF,
                        255
                )
                .uv(u, v)
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    @Override
    public boolean shouldRenderOffScreen(ResearchTableBlockEntity table) {
        return true;
    }
}
