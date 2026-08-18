package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
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

/** Exact neighbor-stretching geometry from TC4 EldritchCrustBakedModel. */
public final class EldritchCrustBakedModel implements BakedModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final ModelProperty<Integer> NEIGHBOR_MASK =
            new ModelProperty<>();
    private static final Set<String> CONNECTED_MODELS = Set.of(
            "eldritch_glowing_crust",
            "eldritch_glyphed_stone"
    );

    private final BakedModel delegate;
    private final ResourceLocation modelId;
    private final Map<Integer, Map<Direction, List<BakedQuad>>> cache =
            new HashMap<>();

    private EldritchCrustBakedModel(
            BakedModel delegate,
            ResourceLocation modelId
    ) {
        this.delegate = delegate;
        this.modelId = modelId;
    }

    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((id, model) ->
                id.getNamespace().equals(ThaumcraftModern.MOD_ID)
                        && CONNECTED_MODELS.contains(id.getPath())
                        ? new EldritchCrustBakedModel(model, id)
                        : model
        );
    }

    @Override
    public ModelData getModelData(
            BlockAndTintGetter level,
            BlockPos position,
            BlockState state,
            ModelData original
    ) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighbourPosition = position.relative(direction);
            BlockState neighbour = level.getBlockState(neighbourPosition);
            if (neighbour.isFaceSturdy(
                    level,
                    neighbourPosition,
                    direction.getOpposite()
            )) {
                mask |= 1 << direction.get3DDataValue();
            }
        }
        return ModelData.builder().with(NEIGHBOR_MASK, mask).build();
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random
    ) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override
    public List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            RandomSource random,
            ModelData data,
            @Nullable RenderType renderType
    ) {
        if (state == null) {
            return delegate.getQuads(state, side, random, data, renderType);
        }
        if (side == null) {
            return List.of();
        }
        Integer storedMask = data.get(NEIGHBOR_MASK);
        int mask = storedMask == null ? 0 : storedMask;
        return cache.computeIfAbsent(mask, this::build).get(side);
    }

    private Map<Direction, List<BakedQuad>> build(int mask) {
        float minX = hasNeighbour(mask, Direction.WEST) ? 0.0F : 2.0F;
        float minY = hasNeighbour(mask, Direction.DOWN) ? 0.0F : 2.0F;
        float minZ = hasNeighbour(mask, Direction.NORTH) ? 0.0F : 2.0F;
        float maxX = hasNeighbour(mask, Direction.EAST) ? 16.0F : 14.0F;
        float maxY = hasNeighbour(mask, Direction.UP) ? 16.0F : 14.0F;
        float maxZ = hasNeighbour(mask, Direction.SOUTH) ? 16.0F : 14.0F;
        Vector3f from = new Vector3f(minX, minY, minZ);
        Vector3f to = new Vector3f(maxX, maxY, maxZ);
        BlockElement element = new BlockElement(
                from,
                to,
                new EnumMap<>(Direction.class),
                null,
                true
        );
        TextureAtlasSprite texture = delegate.getParticleIcon();
        Map<Direction, List<BakedQuad>> quads =
                new EnumMap<>(Direction.class);
        for (Direction side : Direction.values()) {
            BlockElementFace face = new BlockElementFace(
                    side,
                    -1,
                    "",
                    new BlockFaceUV(element.uvsByFace(side), 0)
            );
            quads.put(side, List.of(FACE_BAKERY.bakeQuad(
                    from,
                    to,
                    face,
                    texture,
                    side,
                    BlockModelRotation.X0_Y0,
                    null,
                    true,
                    modelId
            )));
        }
        return Map.copyOf(quads);
    }

    private static boolean hasNeighbour(int mask, Direction direction) {
        return (mask & 1 << direction.get3DDataValue()) != 0;
    }

    @Override public boolean useAmbientOcclusion() {
        return delegate.useAmbientOcclusion();
    }
    @Override public boolean isGui3d() { return delegate.isGui3d(); }
    @Override public boolean usesBlockLight() {
        return delegate.usesBlockLight();
    }
    @Override public boolean isCustomRenderer() {
        return delegate.isCustomRenderer();
    }
    @Override public TextureAtlasSprite getParticleIcon() {
        return delegate.getParticleIcon();
    }
    @Override public ItemTransforms getTransforms() {
        return delegate.getTransforms();
    }
    @Override public ItemOverrides getOverrides() {
        return delegate.getOverrides();
    }
    @Override
    public BakedModel applyTransform(
            ItemDisplayContext context,
            PoseStack poseStack,
            boolean leftHand
    ) {
        delegate.applyTransform(context, poseStack, leftHand);
        return this;
    }
}
