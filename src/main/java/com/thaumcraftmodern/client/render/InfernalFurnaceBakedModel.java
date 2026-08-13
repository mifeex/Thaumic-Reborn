package com.thaumcraftmodern.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thaumcraftmodern.ThaumcraftModern;
import com.thaumcraftmodern.world.block.InfernalFurnaceBlock;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dynamic 1.20 baked-model port of TC4 BlockArcaneFurnaceRenderer. */
public final class InfernalFurnaceBakedModel implements BakedModel {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final BlockFaceUV FULL_UV = new BlockFaceUV(
            new float[]{0, 0, 16, 16}, 0);
    private static final ModelProperty<Integer> RENDER_LEVEL = new ModelProperty<>();
    private static final ModelProperty<Integer> NOZZLE_SIDE = new ModelProperty<>();
    private static final ModelProperty<Direction> NOZZLE_OUTWARD = new ModelProperty<>();
    private static final ResourceLocation MODEL_ID = new ResourceLocation(
            ThaumcraftModern.MOD_ID, "infernal_furnace");

    private final BakedModel delegate;
    private final Map<String, List<BakedQuad>> cache = new HashMap<>();

    private InfernalFurnaceBakedModel(BakedModel delegate) {
        this.delegate = delegate;
    }

    public static void wrapModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((id, model) ->
                id.getNamespace().equals(ThaumcraftModern.MOD_ID)
                        && id.getPath().equals("infernal_furnace")
                        ? new InfernalFurnaceBakedModel(model) : model);
    }

    @Override public ModelData getModelData(BlockAndTintGetter level,
            BlockPos pos, BlockState state, ModelData original) {
        int part = state.getValue(InfernalFurnaceBlock.PART);
        int renderLevel = calculateRenderLevel(level, pos, part);
        int nozzleSide = touchingNozzleSide(level, pos);
        Direction outward = nozzleOutward(level, pos, part);
        return ModelData.builder().with(RENDER_LEVEL, renderLevel)
                .with(NOZZLE_SIDE, nozzleSide)
                .with(NOZZLE_OUTWARD, outward).build();
    }

    @Override public List<BakedQuad> getQuads(@Nullable BlockState state,
            @Nullable Direction side, RandomSource random) {
        return getQuads(state, side, random, ModelData.EMPTY, null);
    }

    @Override public List<BakedQuad> getQuads(@Nullable BlockState state,
            @Nullable Direction side, RandomSource random, ModelData data,
            @Nullable RenderType renderType) {
        if (state == null || side != null) return List.of();
        int part = state.getValue(InfernalFurnaceBlock.PART);
        int level = data.get(RENDER_LEVEL) == null ? 0 : data.get(RENDER_LEVEL);
        int nozzle = data.get(NOZZLE_SIDE) == null ? -1 : data.get(NOZZLE_SIDE);
        Direction outward = data.get(NOZZLE_OUTWARD) == null
                ? Direction.SOUTH : data.get(NOZZLE_OUTWARD);
        String key = part + ":" + level + ":" + nozzle + ":" + outward;
        return cache.computeIfAbsent(key,
                ignored -> build(part, level, nozzle, outward));
    }

    private List<BakedQuad> build(int part, int level, int nozzleSide,
            Direction outward) {
        List<BakedQuad> quads = new ArrayList<>();
        if (part == 0) {
            TextureAtlasSprite lava = sprite(new ResourceLocation(
                    "minecraft", "block/lava_still"));
            addCube(quads, 0, 0, 0, 16, 16, 16,
                    lava, lava, lava, lava, lava, lava);
            return List.copyOf(quads);
        }
        if (part == 10) {
            addNozzle(quads, outward);
            return List.copyOf(quads);
        }
        addCube(quads, 0, 0, 0, 16, 16, 16,
                sprite(textureForSide(part, level, nozzleSide, Direction.DOWN)),
                sprite(textureForSide(part, level, nozzleSide, Direction.UP)),
                sprite(textureForSide(part, level, nozzleSide, Direction.NORTH)),
                sprite(textureForSide(part, level, nozzleSide, Direction.SOUTH)),
                sprite(textureForSide(part, level, nozzleSide, Direction.WEST)),
                sprite(textureForSide(part, level, nozzleSide, Direction.EAST)));
        return List.copyOf(quads);
    }

    private static void addNozzle(List<BakedQuad> quads, Direction outward) {
        // Keep the animated fire recessed behind both the brick opening and
        // the outer iron grate. A distinct depth prevents the face-like fire
        // frame from winning the depth test over the grate at close range.
        addNozzleFace(quads, outward, 11, 12,
                sprite(new ResourceLocation("minecraft", "block/fire_0")), false);
        addNozzleFace(quads, outward, 12, 13, sprite(13), true);
        addNozzleFace(quads, outward, 14, 15, sprite(15), true);
    }

    private static void addNozzleFace(List<BakedQuad> quads,
            Direction outward, float from, float to,
            TextureAtlasSprite texture, boolean shade) {
        switch (outward) {
            case SOUTH -> addFace(quads, 0, 0, from, 16, 16, to,
                    Direction.SOUTH, texture, shade);
            case NORTH -> addFace(quads, 0, 0, 16 - to, 16, 16, 16 - from,
                    Direction.NORTH, texture, shade);
            case EAST -> addFace(quads, from, 0, 0, to, 16, 16,
                    Direction.EAST, texture, shade);
            case WEST -> addFace(quads, 16 - to, 0, 0, 16 - from, 16, 16,
                    Direction.WEST, texture, shade);
            default -> throw new IllegalArgumentException(
                    "Infernal furnace nozzle cannot face " + outward);
        }
    }

    public static int textureForSide(int part, int level, int nozzleSide,
            Direction face) {
        int nozzleOffset = nozzleSide == face.get3DDataValue() ? 3 : 0;
        return switch (face) {
            case DOWN, UP -> {
                if (face == Direction.UP && level == 18) {
                    yield switch (part) {
                        case 2 -> 16;
                        case 4 -> 17;
                        case 6 -> 26;
                        case 8 -> 25;
                        default -> nozzleOffset == 3 ? 6
                                : (part - 1) % 3 + ((part - 1) / 3) * 9;
                    };
                }
                yield nozzleOffset == 3 ? 6 : part == 5 ? 10
                        : (part - 1) % 3 + ((part - 1) / 3) * 9;
            }
            case NORTH -> switch (part) {
                case 1 -> 2 + level + nozzleOffset;
                case 2 -> 1 + level + nozzleOffset;
                case 3 -> level + nozzleOffset;
                default -> level != 9 ? 7 : 6;
            };
            case SOUTH -> switch (part) {
                case 7 -> level + nozzleOffset;
                case 8 -> 1 + level + nozzleOffset;
                case 9 -> 2 + level + nozzleOffset;
                default -> level != 9 ? 7 : 6;
            };
            case WEST -> switch (part) {
                case 1 -> level + nozzleOffset;
                case 4 -> 1 + level + nozzleOffset;
                case 7 -> 2 + level + nozzleOffset;
                default -> level != 9 ? 7 : 6;
            };
            case EAST -> switch (part) {
                case 3 -> 2 + level + nozzleOffset;
                case 6 -> 1 + level + nozzleOffset;
                case 9 -> level + nozzleOffset;
                default -> level != 9 ? 7 : 6;
            };
        };
    }

    private static int calculateRenderLevel(BlockAndTintGetter level,
            BlockPos pos, int part) {
        int up = furnacePart(level.getBlockState(pos.above()));
        int down = furnacePart(level.getBlockState(pos.below()));
        if (up == 10 || up == 0) up = part;
        if (down == 10 || down == 0) down = part;
        if (part == up && part == down) return 9;
        if (part == up && part != down) return 18;
        return 0;
    }

    private static int furnacePart(BlockState state) {
        return state.getBlock() instanceof InfernalFurnaceBlock
                ? state.getValue(InfernalFurnaceBlock.PART) : -1;
    }

    private static int touchingNozzleSide(BlockAndTintGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (furnacePart(level.getBlockState(pos.relative(direction))) == 10) {
                return direction.get3DDataValue();
            }
        }
        return -1;
    }

    private static Direction nozzleOutward(BlockAndTintGetter level,
            BlockPos pos, int part) {
        if (part != 10) return Direction.SOUTH;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (furnacePart(level.getBlockState(pos.relative(direction))) == 0) {
                return direction.getOpposite();
            }
        }
        return Direction.SOUTH;
    }

    private static void addCube(List<BakedQuad> quads,
            float x1, float y1, float z1, float x2, float y2, float z2,
            TextureAtlasSprite down, TextureAtlasSprite up,
            TextureAtlasSprite north, TextureAtlasSprite south,
            TextureAtlasSprite west, TextureAtlasSprite east) {
        addFace(quads, x1, y1, z1, x2, y2, z2, Direction.DOWN, down);
        addFace(quads, x1, y1, z1, x2, y2, z2, Direction.UP, up);
        addFace(quads, x1, y1, z1, x2, y2, z2, Direction.NORTH, north);
        addFace(quads, x1, y1, z1, x2, y2, z2, Direction.SOUTH, south);
        addFace(quads, x1, y1, z1, x2, y2, z2, Direction.WEST, west);
        addFace(quads, x1, y1, z1, x2, y2, z2, Direction.EAST, east);
    }

    private static void addFace(List<BakedQuad> quads,
            float x1, float y1, float z1, float x2, float y2, float z2,
            Direction face, TextureAtlasSprite sprite) {
        addFace(quads, x1, y1, z1, x2, y2, z2, face, sprite, true);
    }

    private static void addFace(List<BakedQuad> quads,
            float x1, float y1, float z1, float x2, float y2, float z2,
            Direction face, TextureAtlasSprite sprite, boolean shade) {
        quads.add(FACE_BAKERY.bakeQuad(
                new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2),
                new BlockElementFace(null, -1, "", FULL_UV), sprite, face,
                BlockModelRotation.X0_Y0, null, shade, MODEL_ID));
    }

    private static TextureAtlasSprite sprite(int index) {
        return sprite(new ResourceLocation(ThaumcraftModern.MOD_ID,
                "block/furnace" + index));
    }

    private static TextureAtlasSprite sprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
                .apply(location);
    }

    @Override public boolean useAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return true; }
    @Override public boolean usesBlockLight() { return true; }
    @Override public boolean isCustomRenderer() { return false; }
    @Override public TextureAtlasSprite getParticleIcon() { return sprite(0); }
    @Override public ItemTransforms getTransforms() { return delegate.getTransforms(); }
    @Override public ItemOverrides getOverrides() { return delegate.getOverrides(); }
    @Override public BakedModel applyTransform(ItemDisplayContext context,
            PoseStack poseStack, boolean leftHand) {
        delegate.applyTransform(context, poseStack, leftHand);
        return this;
    }
}
