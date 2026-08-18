package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.world.block.entity.EldritchCrabVentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

/** Material-backed anchor for TC4's rendered eldritch-crab burrow. */
public final class EldritchCrabVentBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty CRUSTED = BooleanProperty.create(
            "crusted"
    );

    public EldritchCrabVentBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(
                FACING, Direction.SOUTH
        ).setValue(CRUSTED, false));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(
            BlockPos position, BlockState state
    ) {
        return new EldritchCrabVentBlockEntity(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                ModBlockEntities.ELDRITCH_CRAB_VENT.get(),
                level.isClientSide
                        ? EldritchCrabVentBlockEntity::clientTick
                        : EldritchCrabVentBlockEntity::serverTick
        );
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<net.minecraft.world.level.block.Block,
                    BlockState> builder
    ) {
        builder.add(FACING, CRUSTED);
    }
}
