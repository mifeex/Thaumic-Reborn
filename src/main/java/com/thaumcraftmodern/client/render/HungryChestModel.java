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

/**
 * TC4's animated ModelChest pieces rendered with the Minecraft 1.7 ModelBox
 * UV contract. The static body is supplied by the exact baked block model.
 */
public final class HungryChestModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(ThaumcraftModern.MOD_ID,"hungry_chest"),"main");
    private static final float PIXEL=1F/16F;
    public HungryChestModel(ModelPart ignored){}
    public static LayerDefinition createBodyLayer(){
        return LayerDefinition.create(new MeshDefinition(),64,64);
    }

    public void renderLidAndLock(PoseStack poses,VertexConsumer out,int light,int overlay,float openness){
        float angle=-(openness*((float)Math.PI/2F));
        poses.pushPose();
        poses.translate(1*PIXEL,7*PIXEL,15*PIXEL);
        poses.mulPose(Axis.XP.rotation(angle));
        box(poses,out,light,overlay,0,-5,-14,14,5,14,0,0);
        poses.popPose();

        poses.pushPose();
        poses.translate(8*PIXEL,7*PIXEL,15*PIXEL);
        poses.mulPose(Axis.XP.rotation(angle));
        box(poses,out,light,overlay,-1,-2,-15,2,4,1,0,0);
        poses.popPose();
    }

    private static void box(PoseStack poses,VertexConsumer out,int light,int overlay,
            float x,float y,float z,int dx,int dy,int dz,int u,int v){
        float x0=x*PIXEL,y0=y*PIXEL,z0=z*PIXEL,x1=(x+dx)*PIXEL,y1=(y+dy)*PIXEL,z1=(z+dz)*PIXEL;
        quad(poses,out,light,overlay,x1,y0,z1,x1,y0,z0,x1,y1,z0,x1,y1,z1,u+dz+dx,v+dz,u+dz+dx+dz,v+dz+dy,1,0,0);
        quad(poses,out,light,overlay,x0,y0,z0,x0,y0,z1,x0,y1,z1,x0,y1,z0,u,v+dz,u+dz,v+dz+dy,-1,0,0);
        quad(poses,out,light,overlay,x1,y0,z1,x0,y0,z1,x0,y0,z0,x1,y0,z0,u+dz,v,u+dz+dx,v+dz,0,-1,0);
        quad(poses,out,light,overlay,x1,y1,z0,x0,y1,z0,x0,y1,z1,x1,y1,z1,u+dz+dx,v,u+dz+dx+dx,v+dz,0,1,0);
        quad(poses,out,light,overlay,x1,y0,z0,x0,y0,z0,x0,y1,z0,x1,y1,z0,u+dz,v+dz,u+dz+dx,v+dz+dy,0,0,-1);
        quad(poses,out,light,overlay,x0,y0,z1,x1,y0,z1,x1,y1,z1,x0,y1,z1,u+dz+dx+dz,v+dz,u+dz+dx+dz+dx,v+dz+dy,0,0,1);
    }
    private static void quad(PoseStack poses,VertexConsumer out,int light,int overlay,
            float x0,float y0,float z0,float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,
            float u0,float v0,float u1,float v1,float nx,float ny,float nz){
        PoseStack.Pose pose=poses.last();Matrix4f matrix=pose.pose();Matrix3f normal=pose.normal();
        vertex(out,matrix,normal,x0,y0,z0,u1/64F,v0/64F,nx,ny,nz,light,overlay);
        vertex(out,matrix,normal,x1,y1,z1,u0/64F,v0/64F,nx,ny,nz,light,overlay);
        vertex(out,matrix,normal,x2,y2,z2,u0/64F,v1/64F,nx,ny,nz,light,overlay);
        vertex(out,matrix,normal,x3,y3,z3,u1/64F,v1/64F,nx,ny,nz,light,overlay);
    }
    private static void vertex(VertexConsumer out,Matrix4f matrix,Matrix3f normal,float x,float y,float z,
            float u,float v,float nx,float ny,float nz,int light,int overlay){
        out.vertex(matrix,x,y,z).color(255,255,255,255).uv(u,v).overlayCoords(overlay).uv2(light)
                .normal(normal,nx,ny,nz).endVertex();
    }
}
