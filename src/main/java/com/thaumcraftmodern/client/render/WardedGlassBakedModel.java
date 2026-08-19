package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.registry.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Exact 47-tile connected-texture selection used by TC4 warded glass. */
public final class WardedGlassBakedModel implements BakedModel {
    private static final int[] TEXTURE_BY_MASK = {
        0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,
        0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,
        4,4,5,5,4,4,5,5,17,17,22,26,17,17,22,26,16,16,20,20,16,16,28,28,21,21,46,42,21,21,43,38,
        4,4,5,5,4,4,5,5,9,9,30,12,9,9,30,12,16,16,20,20,16,16,28,28,25,25,45,37,25,25,40,32,
        0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,
        0,0,6,6,0,0,6,6,3,3,19,15,3,3,19,15,1,1,18,18,1,1,13,13,2,2,23,31,2,2,27,14,
        4,4,5,5,4,4,5,5,17,17,22,26,17,17,22,26,7,7,24,24,7,7,10,10,29,29,44,41,29,29,39,33,
        4,4,5,5,4,4,5,5,9,9,30,12,9,9,30,12,7,7,24,24,7,7,10,10,8,8,36,35,8,8,34,11
    };
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final BlockFaceUV FULL_UV = new BlockFaceUV(new float[]{0, 0, 16, 16}, 0);
    private static final ModelProperty<Long> FACE_TEXTURES = new ModelProperty<>();
    private static final Vector3f FROM = new Vector3f(0, 0, 0);
    private static final Vector3f TO = new Vector3f(16, 16, 16);

    private final BakedModel delegate;
    private final Map<Long, Map<Direction, List<BakedQuad>>> cache = new HashMap<>();

    private WardedGlassBakedModel(BakedModel delegate) { this.delegate = delegate; }

    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((id, model) -> id.getNamespace().equals(ThaumcraftModern.MOD_ID)
                && id.getPath().equals("warded_glass")
                ? new WardedGlassBakedModel(model) : model);
    }

    @Override public ModelData getModelData(BlockAndTintGetter level, BlockPos pos,
            BlockState state, ModelData original) {
        long packed = 0L;
        for (Direction side : Direction.values())
            packed |= (long) textureIndex(level, pos, side) << side.get3DDataValue() * 6;
        return ModelData.builder().with(FACE_TEXTURES, packed).build();
    }

    private static int textureIndex(BlockAndTintGetter level, BlockPos pos, Direction side) {
        boolean[] connected = new boolean[8];
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (side == Direction.DOWN || side == Direction.UP) {
            connected[0] = same(level, x-1,y,z-1); connected[1] = same(level,x,y,z-1);
            connected[2] = same(level,x+1,y,z-1); connected[3] = same(level,x-1,y,z);
            connected[4] = same(level,x+1,y,z); connected[5] = same(level,x-1,y,z+1);
            connected[6] = same(level,x,y,z+1); connected[7] = same(level,x+1,y,z+1);
        } else if (side == Direction.NORTH || side == Direction.SOUTH) {
            boolean north = side == Direction.NORTH;
            connected[0] = same(level,x+(north?1:-1),y+1,z); connected[1] = same(level,x,y+1,z);
            connected[2] = same(level,x+(north?-1:1),y+1,z); connected[3] = same(level,x+(north?1:-1),y,z);
            connected[4] = same(level,x+(north?-1:1),y,z); connected[5] = same(level,x+(north?1:-1),y-1,z);
            connected[6] = same(level,x,y-1,z); connected[7] = same(level,x+(north?-1:1),y-1,z);
        } else {
            boolean east = side == Direction.EAST;
            connected[0] = same(level,x,y+1,z+(east?1:-1)); connected[1] = same(level,x,y+1,z);
            connected[2] = same(level,x,y+1,z+(east?-1:1)); connected[3] = same(level,x,y,z+(east?1:-1));
            connected[4] = same(level,x,y,z+(east?-1:1)); connected[5] = same(level,x,y-1,z+(east?1:-1));
            connected[6] = same(level,x,y-1,z); connected[7] = same(level,x,y-1,z+(east?-1:1));
        }
        int mask = 0;
        for (int i = 0; i < 8; i++) if (connected[i]) mask |= 1 << i;
        return TEXTURE_BY_MASK[mask];
    }

    private static boolean same(BlockAndTintGetter level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).is(ModBlocks.WARDED_GLASS.get());
    }

    @Override public List<BakedQuad> getQuads(@Nullable BlockState state,
            @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override public List<BakedQuad> getQuads(@Nullable BlockState state,
            @Nullable Direction side, RandomSource random, ModelData data,
            @Nullable RenderType renderType) {
        if (state == null) return delegate.getQuads(null, side, random, data, renderType);
        if (side == null) return List.of();
        Long packed = data.get(FACE_TEXTURES);
        long value = packed == null ? 0L : packed;
        return cache.computeIfAbsent(value, this::build).get(side);
    }

    private Map<Direction, List<BakedQuad>> build(long packed) {
        Map<Direction, List<BakedQuad>> faces = new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            int index = (int) (packed >> side.get3DDataValue() * 6 & 63L);
            ResourceLocation texture = new ResourceLocation(ThaumcraftModern.MOD_ID,
                    "block/warded_glass_" + (index + 1));
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(texture);
            BlockElementFace face = new BlockElementFace(side, -1, "", FULL_UV);
            faces.put(side, List.of(FACE_BAKERY.bakeQuad(FROM, TO, face, sprite, side,
                    BlockModelRotation.X0_Y0, null, true,
                    new ResourceLocation(ThaumcraftModern.MOD_ID, "warded_glass"))));
        }
        return Map.copyOf(faces);
    }

    @Override public boolean useAmbientOcclusion() { return delegate.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return delegate.isGui3d(); }
    @Override public boolean usesBlockLight() { return delegate.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return delegate.isCustomRenderer(); }
    @Override public TextureAtlasSprite getParticleIcon() { return delegate.getParticleIcon(); }
    @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return delegate.getOverrides(); }
    @Override public BakedModel applyTransform(ItemDisplayContext context,
            PoseStack poseStack, boolean leftHand) {
        delegate.applyTransform(context, poseStack, leftHand);
        return this;
    }
}
