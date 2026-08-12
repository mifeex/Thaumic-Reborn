package com.thaumcraftmodern.essentia;

import com.thaumcraftmodern.world.block.entity.EssentiaMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Modern server-owned equivalent of TC4's {@code EssentiaHandler}. */
public final class EssentiaAirHandler {
    private EssentiaAirHandler() {
    }

    /**
     * Removes one matching essentia from the first loaded source in the
     * classic scan order and returns that source's position.
     */
    public static @Nullable BlockPos drain(
            ServerLevel level,
            BlockPos requester,
            String aspect,
            @Nullable Direction direction,
            int range,
            boolean ignoreMirrors
    ) {
        if (aspect == null || aspect.isBlank() || range <= 0) {
            return null;
        }
        for (BlockPos sourcePos : buildScan(requester, direction, range)) {
            if (sourcePos.equals(requester) || !level.hasChunkAt(sourcePos)) {
                continue;
            }
            BlockEntity sourceEntity = level.getBlockEntity(sourcePos);
            if (sourceEntity instanceof EssentiaMirrorBlockEntity mirror) {
                if (!ignoreMirrors && mirror.takeFromAir(aspect)) {
                    return sourcePos.immutable();
                }
                continue;
            }
            if (!(sourceEntity instanceof
                    com.thaumicreborn.api.essentia.EssentiaTransport source)) {
                continue;
            }
            for (Direction side : Direction.values()) {
                if (source.canOutputTo(side)
                        && source.takeEssentia(aspect, 1, side) == 1) {
                    return sourcePos.immutable();
                }
            }
        }
        return null;
    }

    /** Exact full-cube/directional-half-space iteration used by TC4. */
    static List<BlockPos> buildScan(
            BlockPos origin,
            @Nullable Direction direction,
            int range
    ) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        if (direction == null) {
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        positions.add(origin.offset(x, y, z));
                    }
                }
            }
            return List.copyOf(positions);
        }
        for (int a = -range; a <= range; a++) {
            for (int b = -range; b <= range; b++) {
                for (int c = 0; c < range; c++) {
                    if (direction.getStepY() != 0) {
                        positions.add(origin.offset(
                                a,
                                c * direction.getStepY(),
                                b
                        ));
                    } else if (direction.getStepX() != 0) {
                        positions.add(origin.offset(
                                c * direction.getStepX(),
                                a,
                                b
                        ));
                    } else {
                        positions.add(origin.offset(
                                a,
                                b,
                                c * direction.getStepZ()
                        ));
                    }
                }
            }
        }
        return List.copyOf(positions);
    }
}
