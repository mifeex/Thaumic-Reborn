package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** TC4 ModelJar/ModelBrain rendered with the Minecraft 1.7 ModelBox UV contract. */
public final class ClassicBrainJarModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID, "brain_jar"), "main");
    private static final float PIXEL = 1F / 16F;

    public ClassicBrainJarModel(ModelPart ignored) {}

    public static LayerDefinition createBodyLayer() {
        // Geometry is emitted manually because 1.20 ModelPart assigns the six
        // box UV regions differently from 1.7 ModelBox.
        return LayerDefinition.create(new MeshDefinition(), 64, 32);
    }

    public void shell(PoseStack poses, VertexConsumer out, int light, int overlay) {
        box(poses, out, light, overlay, -3, -14, -3, 6, 2, 6, 0, 24, 64, 32);
        box(poses, out, light, overlay, -5, -12, -5, 10, 12, 10, 0, 0, 64, 32);
    }

    public void brine(PoseStack poses, VertexConsumer out, int light, int overlay) {
        box(poses, out, light, overlay, -4, -11, -4, 8, 10, 8, 0, 0, 64, 32);
    }

    public void brain(PoseStack poses, VertexConsumer out, int light, int overlay) {
        box(poses, out, light, overlay, -6, 8, -8, 12, 10, 16, 0, 0, 128, 64);
        box(poses, out, light, overlay, -4, 18, 0, 8, 3, 7, 64, 0, 128, 64);
        poses.pushPose();
        poses.translate(-PIXEL, 18 * PIXEL, -2 * PIXEL);
        poses.mulPose(Axis.XP.rotation(.4089647F));
        box(poses, out, light, overlay, 0, 0, 0, 2, 6, 2, 0, 32, 128, 64);
        poses.popPose();
    }

    private static void box(PoseStack poses, VertexConsumer out, int light, int overlay,
            float x, float y, float z, int dx, int dy, int dz,
            int u, int v, int textureWidth, int textureHeight) {
        float x0=x*PIXEL, y0=y*PIXEL, z0=z*PIXEL;
        float x1=(x+dx)*PIXEL, y1=(y+dy)*PIXEL, z1=(z+dz)*PIXEL;
        // Exact ModelBox texture regions and vertex winding from Minecraft 1.7.10.
        quad(poses,out,light,overlay, x1,y0,z1, x1,y0,z0, x1,y1,z0, x1,y1,z1,
                u+dz+dx, v+dz, u+dz+dx+dz, v+dz+dy, textureWidth,textureHeight, 1,0,0);
        quad(poses,out,light,overlay, x0,y0,z0, x0,y0,z1, x0,y1,z1, x0,y1,z0,
                u, v+dz, u+dz, v+dz+dy, textureWidth,textureHeight, -1,0,0);
        quad(poses,out,light,overlay, x1,y0,z1, x0,y0,z1, x0,y0,z0, x1,y0,z0,
                u+dz, v, u+dz+dx, v+dz, textureWidth,textureHeight, 0,-1,0);
        quad(poses,out,light,overlay, x1,y1,z0, x0,y1,z0, x0,y1,z1, x1,y1,z1,
                u+dz+dx, v, u+dz+dx+dx, v+dz, textureWidth,textureHeight, 0,1,0);
        quad(poses,out,light,overlay, x1,y0,z0, x0,y0,z0, x0,y1,z0, x1,y1,z0,
                u+dz, v+dz, u+dz+dx, v+dz+dy, textureWidth,textureHeight, 0,0,-1);
        quad(poses,out,light,overlay, x0,y0,z1, x1,y0,z1, x1,y1,z1, x0,y1,z1,
                u+dz+dx+dz, v+dz, u+dz+dx+dz+dx, v+dz+dy,
                textureWidth,textureHeight, 0,0,1);
    }

    private static void quad(PoseStack poses, VertexConsumer out, int light, int overlay,
            float x0,float y0,float z0, float x1,float y1,float z1,
            float x2,float y2,float z2, float x3,float y3,float z3,
            float u0,float v0,float u1,float v1,int tw,int th,float nx,float ny,float nz) {
        PoseStack.Pose pose=poses.last(); Matrix4f matrix=pose.pose(); Matrix3f normal=pose.normal();
        vertex(out,matrix,normal,x0,y0,z0,u1/tw,v0/th,nx,ny,nz,light,overlay);
        vertex(out,matrix,normal,x1,y1,z1,u0/tw,v0/th,nx,ny,nz,light,overlay);
        vertex(out,matrix,normal,x2,y2,z2,u0/tw,v1/th,nx,ny,nz,light,overlay);
        vertex(out,matrix,normal,x3,y3,z3,u1/tw,v1/th,nx,ny,nz,light,overlay);
    }

    private static void vertex(VertexConsumer out,Matrix4f matrix,Matrix3f normal,
            float x,float y,float z,float u,float v,float nx,float ny,float nz,int light,int overlay) {
        out.vertex(matrix,x,y,z).color(255,255,255,255).uv(u,v).overlayCoords(overlay)
                .uv2(light).normal(normal,nx,ny,nz).endVertex();
    }
}
