package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.thaumcraftmodern.crucible.CrucibleFluidPresentation;
import com.thaumcraftmodern.world.block.entity.CrucibleBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * TC4 liquid surface: water height rises with essentia and darkens as the
 * Crucible becomes saturated.
 */
public final class CrucibleBlockEntityRenderer
        implements BlockEntityRenderer<CrucibleBlockEntity> {
    private static final ResourceLocation WATER_STILL =
            new ResourceLocation(
                    "minecraft",
                    "block/water_still"
            );
    /*
     * Manual water-surface calibration. Values are in block-local units:
     * 0.0F is one outside edge and 1.0F is the opposite outside edge.
     * TC4 draws the surface across the full block and lets the crucible's
     * transparent top texture mask it to the opening.
     */
    private static final float WATER_SURFACE_MIN_X = 0.0F;
    private static final float WATER_SURFACE_MAX_X = 1.0F;
    private static final float WATER_SURFACE_MIN_Z = 0.0F;
    private static final float WATER_SURFACE_MAX_Z = 1.0F;
    private static final float WATER_SURFACE_Y_OFFSET = 0.0F;

    public CrucibleBlockEntityRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public void render(
            CrucibleBlockEntity crucible,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight,
            int packedOverlay
    ) {
        if (crucible.water() <= 0) {
            return;
        }
        TextureAtlasSprite water = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(WATER_STILL);
        VertexConsumer vertices = water.wrap(
                buffers.getBuffer(RenderType.translucent())
        );
        int baseWaterColor = crucible.getLevel() == null
                ? 0x3F76E4
                : BiomeColors.getAverageWaterColor(
                        crucible.getLevel(),
                        crucible.getBlockPos()
                );
        int color = CrucibleFluidPresentation.color(
                baseWaterColor,
                crucible.essentiaAmount(),
                CrucibleBlockEntity.MAX_ESSENTIA
        );
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        float height = crucible.fluidHeight() + WATER_SURFACE_Y_OFFSET;
        PoseStack.Pose pose = poseStack.last();
        vertex(
                pose,
                vertices,
                WATER_SURFACE_MIN_X,
                height,
                WATER_SURFACE_MAX_Z,
                1.0F,
                1.0F,
                red,
                green,
                blue,
                packedLight
        );
        vertex(
                pose,
                vertices,
                WATER_SURFACE_MAX_X,
                height,
                WATER_SURFACE_MAX_Z,
                0.0F,
                1.0F,
                red,
                green,
                blue,
                packedLight
        );
        vertex(
                pose,
                vertices,
                WATER_SURFACE_MAX_X,
                height,
                WATER_SURFACE_MIN_Z,
                0.0F,
                0.0F,
                red,
                green,
                blue,
                packedLight
        );
        vertex(
                pose,
                vertices,
                WATER_SURFACE_MIN_X,
                height,
                WATER_SURFACE_MIN_Z,
                1.0F,
                0.0F,
                red,
                green,
                blue,
                packedLight
        );
    }

    private static void vertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            float x,
            float y,
            float z,
            float u,
            float v,
            float red,
            float green,
            float blue,
            int packedLight
    ) {
        Matrix4f position = pose.pose();
        Matrix3f normal = pose.normal();
        vertices.vertex(position, x, y, z)
                .color(red, green, blue, 1.0F)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
