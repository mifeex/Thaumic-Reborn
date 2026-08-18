package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.EldritchRunedStoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/** TC4 BlockEldritch meta 10: disguised runed masonry trap. */
public final class EldritchRunedStoneBlock extends BaseEntityBlock {
    public static final IntegerProperty PATTERN =
            IntegerProperty.create("pattern", 0, 3);

    public EldritchRunedStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PATTERN, 0));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position, BlockState state
    ) {
        return new EldritchRunedStoneBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        return level.isClientSide ? null : createTickerHelper(
                type,
                ModBlockEntities.ELDRITCH_RUNED_STONE.get(),
                EldritchRunedStoneBlockEntity::serverTick
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block,
                    BlockState> builder
    ) {
        builder.add(PATTERN);
    }
}
