package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.registry.ModItems;
import com.thaumcraftmodern.registry.ModBlocks;
import com.thaumcraftmodern.world.block.EldritchLockBlock;
import com.thaumcraftmodern.world.block.entity.EldritchLockBlockEntity;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Modern port of TC4's TileEldritchLockRenderer and its 100-tick opening. */
public final class EldritchLockRenderer
        implements BlockEntityRenderer<EldritchLockBlockEntity> {
    private static final float FIELD_MIN = -2.0F;
    private static final float FIELD_MAX = 3.0F;
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
        renderField(lock, facing, ticks, pose, buffers);
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
                0.285D,
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
            Direction facing, float ticks,
            PoseStack pose, MultiBufferSource buffers) {
        float time = (ticks % 1400.0F) / 500.0F;
        drawFieldLayer(lock, buffers.getBuffer(EldritchLockRenderType.background()),
                pose.last(), facing, time * 0.125F, time * 0.125F,
                0.125F, 0.1F, 0.1F, 0.1F, 1.0F);
        Random random = new Random(31100L);
        Vec3 camera = Minecraft.getInstance().gameRenderer
                .getMainCamera().getPosition();
        for (int layer = 1; layer < 16; layer++) {
            float depth = 16.0F - layer;
            float brightness = 1.0F / (depth + 1.0F);
            float scale = layer == 1 ? 0.5F : 0.0625F;
            float shift = (time + (layer * layer * 4321 + layer * 9) * 2.0F) * scale;
            float parallaxScale = scale * (0.75F + depth * 0.015625F);
            float parallaxU = (float) (facing.getAxis() == Direction.Axis.Z
                    ? camera.x : camera.z) * parallaxScale;
            float parallaxV = (float) camera.y * parallaxScale;
            float red = (random.nextFloat() * 0.5F + 0.1F) * brightness;
            float green = (random.nextFloat() * 0.5F + 0.4F) * brightness;
            float blue = (random.nextFloat() * 0.5F + 0.5F) * brightness;
            drawFieldLayer(lock, buffers.getBuffer(EldritchLockRenderType.stars()),
                    pose.last(), facing, shift + parallaxU,
                    shift + parallaxV, scale,
                    red, green, blue, 1.0F);
        }
    }

    private static void drawFieldLayer(EldritchLockBlockEntity lock,
            VertexConsumer vertices, PoseStack.Pose pose,
            Direction facing, float u, float v, float scale,
            float red, float green, float blue, float alpha) {
        for (int horizontal = -2; horizontal <= 2; horizontal++) {
            for (int vertical = -2; vertical <= 2; vertical++) {
                if (!isBarrierCell(lock, facing, horizontal, vertical)) continue;
                drawFieldCell(vertices, pose, facing, horizontal, vertical,
                        u, v, scale, red, green, blue, alpha);
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

    private static void drawFieldCell(VertexConsumer vertices, PoseStack.Pose pose,
            Direction facing, int horizontal, int vertical,
            float u, float v, float scale,
            float red, float green, float blue, float alpha) {
        float minHorizontal = horizontal;
        float maxHorizontal = horizontal + 1.0F;
        float minVertical = vertical;
        float maxVertical = vertical + 1.0F;
        float cellScale = scale / (FIELD_MAX - FIELD_MIN);
        float u0 = u + (minHorizontal - FIELD_MIN) * cellScale;
        float u1 = u + (maxHorizontal - FIELD_MIN) * cellScale;
        float v0 = v + (minVertical - FIELD_MIN) * cellScale;
        float v1 = v + (maxVertical - FIELD_MIN) * cellScale;
        if (facing.getAxis() == Direction.Axis.Z) {
            float z = 0.5F - facing.getStepZ() * 0.02F;
            vertex(vertices, pose, minHorizontal, minVertical, z, u0, v1, red, green, blue, alpha, 0, 0, 1);
            vertex(vertices, pose, maxHorizontal, minVertical, z, u1, v1, red, green, blue, alpha, 0, 0, 1);
            vertex(vertices, pose, maxHorizontal, maxVertical, z, u1, v0, red, green, blue, alpha, 0, 0, 1);
            vertex(vertices, pose, minHorizontal, maxVertical, z, u0, v0, red, green, blue, alpha, 0, 0, 1);
        } else {
            float x = 0.5F - facing.getStepX() * 0.02F;
            vertex(vertices, pose, x, minVertical, minHorizontal, u0, v1, red, green, blue, alpha, 1, 0, 0);
            vertex(vertices, pose, x, minVertical, maxHorizontal, u1, v1, red, green, blue, alpha, 1, 0, 0);
            vertex(vertices, pose, x, maxVertical, maxHorizontal, u1, v0, red, green, blue, alpha, 1, 0, 0);
            vertex(vertices, pose, x, maxVertical, minHorizontal, u0, v0, red, green, blue, alpha, 1, 0, 0);
        }
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
