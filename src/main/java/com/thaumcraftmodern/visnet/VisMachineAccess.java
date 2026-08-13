package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Generic TC4 VisNetHandler-style entry point for non-node machines. */
public final class VisMachineAccess {
    private VisMachineAccess(){}
    public static int consumeNearest(ServerLevel level,BlockPos origin,PrimalAspect aspect,int amount){
        if (amount <= 0) return 0;
        int consumed = 0;
        for (long position : VisNetworkSpatialIndex.machineCandidates(
                level, origin)) {
            if (!(level.getBlockEntity(BlockPos.of(position))
                    instanceof VisNetworkNodeBlockEntity node)) {
                continue;
            }
            consumed += node.consumeVis(aspect, amount - consumed);
            if (consumed >= amount) break;
        }
        return consumed;
    }
}
