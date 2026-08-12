package com.thaumcraftmodern.visnet;

import com.thaumcraftmodern.aura.PrimalAspect;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Generic TC4 VisNetHandler-style entry point for non-node machines. */
public final class VisMachineAccess {
    private VisMachineAccess(){}
    public static int consumeNearest(ServerLevel level,BlockPos origin,PrimalAspect aspect,int amount){
        if (amount <= 0) return 0;
        List<VisNetworkNodeBlockEntity> nearby = new ArrayList<>();
        for(BlockPos cursor:BlockPos.betweenClosed(origin.offset(-VisNetworkNodeBlockEntity.RANGE,-VisNetworkNodeBlockEntity.RANGE,-VisNetworkNodeBlockEntity.RANGE),origin.offset(VisNetworkNodeBlockEntity.RANGE,VisNetworkNodeBlockEntity.RANGE,VisNetworkNodeBlockEntity.RANGE))){
            if(!(level.getBlockEntity(cursor) instanceof VisNetworkNodeBlockEntity node)
                    || !node.hasRouteToSource(new HashSet<>())) continue;
            double next=cursor.distSqr(origin);
            if(next<=VisNetworkNodeBlockEntity.RANGE*VisNetworkNodeBlockEntity.RANGE) nearby.add(node);
        }
        nearby.sort(Comparator
                .comparingDouble((VisNetworkNodeBlockEntity node) ->
                        node.getBlockPos().distSqr(origin))
                .thenComparingLong(node -> node.getBlockPos().asLong()));
        int consumed = 0;
        for (VisNetworkNodeBlockEntity node : nearby) {
            consumed += node.consumeVis(aspect, amount - consumed);
            if (consumed >= amount) break;
        }
        return consumed;
    }
}
