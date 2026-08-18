package com.thaumcraftmodern.world.block.entity;

import com.thaumcraftmodern.network.ModNetwork;
import com.thaumcraftmodern.network.packet.NodeZapPacket;
import com.thaumcraftmodern.knowledge.WarpType;
import com.thaumcraftmodern.registry.ModBlockEntities;
import com.thaumcraftmodern.research.ResearchProgressService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Exact gameplay cadence of TC4's TileEldritchTrap. */
public final class EldritchRunedStoneBlockEntity extends BlockEntity {
    private int count = 20;

    public EldritchRunedStoneBlockEntity(
            BlockPos position, BlockState state
    ) {
        super(ModBlockEntities.ELDRITCH_RUNED_STONE.get(), position, state);
    }

    public static void serverTick(
            Level ignored,
            BlockPos position,
            BlockState state,
            EldritchRunedStoneBlockEntity trap
    ) {
        if (!(trap.level instanceof ServerLevel level) || trap.count-- > 0) {
            return;
        }
        trap.count = 10 + level.random.nextInt(25);
        Player nearest = level.getNearestPlayer(
                position.getX() + 0.5D,
                position.getY() + 0.5D,
                position.getZ() + 0.5D,
                3.0D,
                false
        );
        if (!(nearest instanceof ServerPlayer player)) {
            return;
        }
        player.hurt(level.damageSources().magic(), 2.0F);
        if (level.random.nextBoolean()) {
            ResearchProgressService.addWarp(
                    player,
                    WarpType.TEMPORARY,
                    1 + level.random.nextInt(2),
                    "eldritch_runed_stone"
            );
        }
        ModNetwork.sendToTrackingChunk(
                level,
                position,
                new NodeZapPacket(
                        position,
                        player.blockPosition(),
                        level.random.nextLong()
                )
        );
    }
}
