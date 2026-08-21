package com.thaumcraftmodern.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The single, always-burning white tallow candle from TC4. */
public final class TallowCandleBlock extends Block {
    public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
    private static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final int FLAME_PARTICLES_PER_TICK = 3;
    private static final double WICK_XZ_OFFSET = 0.5D;
    private static final double WICK_Y_OFFSET = 10.5D / 16.0D;
    public TallowCandleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
    }

    public static int tintColor(DyeColor color) {
        float[] diffuse = color.getTextureDiffuseColors();
        int red = Math.round(diffuse[0] * 255.0F);
        int green = Math.round(diffuse[1] * 255.0F);
        int blue = Math.round(diffuse[2] * 255.0F);
        return red << 16 | green << 8 | blue;
    }

    public static DyeColor color(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("BlockStateTag", CompoundTag.TAG_COMPOUND)) {
            DyeColor[] colors = DyeColor.values();
            return colors[(int) ((System.currentTimeMillis() / 500L) % colors.length)];
        }
        return DyeColor.byName(
                tag.getCompound("BlockStateTag").getString(COLOR.getName()),
                DyeColor.WHITE
        );
    }

    public static boolean hasStoredColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null
                && tag.contains("BlockStateTag", CompoundTag.TAG_COMPOUND)
                && tag.getCompound("BlockStateTag").contains(
                        COLOR.getName(), CompoundTag.TAG_STRING);
    }

    public static void storeColor(ItemStack stack, DyeColor color) {
        stack.getOrCreateTagElement("BlockStateTag")
                .putString(COLOR.getName(), color.getName());
    }

    public static ItemStack stack(Item item, DyeColor color) {
        ItemStack stack = new ItemStack(item);
        storeColor(stack, color);
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + WICK_XZ_OFFSET;
        double y = pos.getY() + WICK_Y_OFFSET;
        double z = pos.getZ() + WICK_XZ_OFFSET;

        for (int i = 0; i < FLAME_PARTICLES_PER_TICK; i++) {
            level.addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        }
        if (random.nextFloat() < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
