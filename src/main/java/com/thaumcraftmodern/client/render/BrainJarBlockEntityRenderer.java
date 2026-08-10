package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.entity.BrainJarBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class BrainJarBlockEntityRenderer implements BlockEntityRenderer<BrainJarBlockEntity>{
    static final ResourceLocation JAR=new ResourceLocation(ThaumcraftModern.MOD_ID,"textures/block/jar.png");
    static final ResourceLocation BRAIN=new ResourceLocation(ThaumcraftModern.MOD_ID,"textures/block/brain2.png");
    static final ResourceLocation BRINE=new ResourceLocation(ThaumcraftModern.MOD_ID,"textures/block/jarbrine.png");
    private final ClassicBrainJarModel model;
    public BrainJarBlockEntityRenderer(BlockEntityRendererProvider.Context context){model=new ClassicBrainJarModel(context.bakeLayer(ClassicBrainJarModel.LAYER));}
    @Override public void render(BrainJarBlockEntity jar,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){renderAll(model,jar.rotation(partial),jar.bob(partial),poses,buffers,light,overlay);}
    static void renderAll(ClassicBrainJarModel model,float rotation,float bob,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        poses.pushPose();poses.translate(.5,.01,.5);poses.mulPose(Axis.XP.rotationDegrees(180));poses.translate(0,-.8+bob,0);poses.mulPose(Axis.YP.rotation(rotation));poses.mulPose(Axis.YP.rotationDegrees(-90));poses.scale(.4f,.4f,.4f);
        model.brain(poses,buffers.getBuffer(RenderType.entityCutoutNoCull(BRAIN)),light,overlay);poses.popPose();
        poses.pushPose();poses.translate(.5,.01,.5);poses.mulPose(Axis.XP.rotationDegrees(180));model.brine(poses,buffers.getBuffer(RenderType.entityTranslucent(BRINE)),light,overlay);poses.popPose();
        // TC4 renders the glass shell last, after the brain and brine.
        poses.pushPose();poses.translate(.5,.01,.5);poses.mulPose(Axis.XP.rotationDegrees(180));
        model.shell(poses,buffers.getBuffer(RenderType.entityTranslucent(JAR)),light,overlay);poses.popPose();
    }
}
