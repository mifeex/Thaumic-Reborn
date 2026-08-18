package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchLockBlock;
import com.thaumcraftmodern.world.block.entity.EldritchLockBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Modern port of TC4's TileEldritchLockRenderer and its 100-tick opening. */
public final class EldritchLockRenderer
        implements BlockEntityRenderer<EldritchLockBlockEntity> {
    private final ItemRenderer items;

    public EldritchLockRenderer(BlockEntityRendererProvider.Context context) {
        items = context.getItemRenderer();
    }

    @Override
    public void render(EldritchLockBlockEntity lock, float partialTick,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (lock.getLevel() == null) return;
        Direction facing = lock.getBlockState().getValue(EldritchLockBlock.FACING);
        float ticks = lock.getLevel().getGameTime() + partialTick;
        renderField(lock, facing, pose, buffers);
        renderRings(lock.countdown(), ticks, facing, pose, buffers);
        renderInsertedTablet(lock, facing, pose, buffers, light, overlay);
    }

    private static void renderRings(int count, float ticks, Direction facing,
            PoseStack pose, MultiBufferSource buffers) {
        VertexConsumer vertices = buffers.getBuffer(EldritchLockRenderType.cubes());
        for (int arm = 0; arm < 4; arm++) {
            int segments = 5 - (count + arm * 5) / 20;
            for (int segment = 1; segment < segments; segment++) {
                float wobble = Mth.sin((ticks + segment * 10.0F + arm * 20.0F) / 20.0F) * 0.1F;
                if (segment == 1 || segment == 4) wobble = wobble * 0.5F + 0.2F;
                pose.pushPose();
                pose.translate(0.5D, 0.5D, 0.5D);
                if (facing.getAxis() == Direction.Axis.Z) {
                    pose.mulPose(Axis.ZP.rotationDegrees(90.0F * arm));
                } else {
                    pose.mulPose(Axis.XP.rotationDegrees(90.0F * arm));
                }
                pose.translate(0.0D, 0.25D + 0.5D * segment, 0.0D);
                pose.scale(0.5F + wobble, 0.5F, 0.5F + wobble);
                drawCube(vertices, pose.last());
                pose.popPose();
            }
        }
    }

    private void renderInsertedTablet(EldritchLockBlockEntity lock, Direction facing,
            PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (lock.countdown() < 0) return;
        pose.pushPose();
        pose.translate(
                0.5D + facing.getStepX() * 0.526D,
                0.3475D,
                0.5D + facing.getStepZ() * 0.526D
        );
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        pose.scale(0.72F, 0.72F, 0.72F);
        items.renderStatic(new ItemStack(ModItems.RUNED_TABLET.get()),
                ItemDisplayContext.FIXED, light, overlay, pose, buffers,
                lock.getLevel(), 0);
        pose.popPose();
    }

    private static void renderField(EldritchLockBlockEntity lock,
            Direction facing,
            PoseStack pose, MultiBufferSource buffers) {
        VertexConsumer vertices = buffers.getBuffer(RenderType.endPortal());
        for (int horizontal = -2; horizontal <= 2; horizontal++) {
            for (int vertical = -2; vertical <= 2; vertical++) {
                if (!isBarrierCell(lock, facing, horizontal, vertical)) continue;
                drawFieldCell(vertices, pose.last().pose(), facing,
                        horizontal, vertical);
            }
        }
    }

    private static boolean isBarrierCell(EldritchLockBlockEntity lock,
            Direction facing, int horizontal, int vertical) {
        if (lock.getLevel() == null) return false;
        int dx = facing.getAxis() == Direction.Axis.Z ? horizontal : 0;
        int dz = facing.getAxis() == Direction.Axis.X ? horizontal : 0;
        return lock.getLevel().getBlockState(lock.getBlockPos().offset(
                dx, vertical, dz)).is(ModBlocks.ELDRITCH_BARRIER.get());
    }

    private static void drawFieldCell(VertexConsumer vertices, Matrix4f pose,
            Direction facing, int horizontal, int vertical) {
        float minHorizontal = horizontal;
        float maxHorizontal = horizontal + 1.0F;
        float minVertical = vertical;
        float maxVertical = vertical + 1.0F;
        if (facing.getAxis() == Direction.Axis.Z) {
            float z = 0.5F - facing.getStepZ() * 0.02F;
            fieldVertex(vertices, pose, minHorizontal, minVertical, z);
            fieldVertex(vertices, pose, maxHorizontal, minVertical, z);
            fieldVertex(vertices, pose, maxHorizontal, maxVertical, z);
            fieldVertex(vertices, pose, minHorizontal, maxVertical, z);
            fieldVertex(vertices, pose, minHorizontal, maxVertical, z);
            fieldVertex(vertices, pose, maxHorizontal, maxVertical, z);
            fieldVertex(vertices, pose, maxHorizontal, minVertical, z);
            fieldVertex(vertices, pose, minHorizontal, minVertical, z);
        } else {
            float x = 0.5F - facing.getStepX() * 0.02F;
            fieldVertex(vertices, pose, x, minVertical, minHorizontal);
            fieldVertex(vertices, pose, x, minVertical, maxHorizontal);
            fieldVertex(vertices, pose, x, maxVertical, maxHorizontal);
            fieldVertex(vertices, pose, x, maxVertical, minHorizontal);
            fieldVertex(vertices, pose, x, maxVertical, minHorizontal);
            fieldVertex(vertices, pose, x, maxVertical, maxHorizontal);
            fieldVertex(vertices, pose, x, minVertical, maxHorizontal);
            fieldVertex(vertices, pose, x, minVertical, minHorizontal);
        }
    }

    private static void fieldVertex(VertexConsumer vertices, Matrix4f pose,
            float x, float y, float z) {
        vertices.vertex(pose, x, y, z).endVertex();
    }

    private static void drawCube(VertexConsumer vertices, PoseStack.Pose pose) {
        float a = -0.5F, b = 0.5F;
        quad(vertices, pose, a,a,b, b,a,b, b,b,b, a,b,b,
                0.25F,0.25F,0.50F,0.50F, 0,0,1);
        quad(vertices, pose, b,a,a, a,a,a, a,b,a, b,b,a,
                0.75F,0.25F,1.00F,0.50F, 0,0,-1);
        quad(vertices, pose, b,a,b, b,a,a, b,b,a, b,b,b,
                0.50F,0.25F,0.75F,0.50F, 1,0,0);
        quad(vertices, pose, a,a,a, a,a,b, a,b,b, a,b,a,
                0.00F,0.25F,0.25F,0.50F, -1,0,0);
        quad(vertices, pose, a,b,b, b,b,b, b,b,a, a,b,a,
                0.25F,0.00F,0.50F,0.25F, 0,1,0);
        quad(vertices, pose, a,a,a, b,a,a, b,a,b, a,a,b,
                0.50F,0.00F,0.75F,0.25F, 0,-1,0);
    }

    private static void quad(VertexConsumer vertices, PoseStack.Pose pose,
            float x1,float y1,float z1,float x2,float y2,float z2,
            float x3,float y3,float z3,float x4,float y4,float z4,
            float u0,float v0,float u1,float v1,
            float nx,float ny,float nz) {
        vertex(vertices, pose, x1,y1,z1, u0,v1, 1,1,1,1, nx,ny,nz);
        vertex(vertices, pose, x2,y2,z2, u1,v1, 1,1,1,1, nx,ny,nz);
        vertex(vertices, pose, x3,y3,z3, u1,v0, 1,1,1,1, nx,ny,nz);
        vertex(vertices, pose, x4,y4,z4, u0,v0, 1,1,1,1, nx,ny,nz);
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose,
            float x,float y,float z,float u,float v,
            float red,float green,float blue,float alpha,
            float nx,float ny,float nz) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertices.vertex(matrix, x, y, z).color(red, green, blue, alpha)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(normal, nx, ny, nz).endVertex();
    }

    @Override public boolean shouldRenderOffScreen(EldritchLockBlockEntity lock) { return true; }
    @Override public int getViewDistance() { return 96; }
}
