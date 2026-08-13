package com.thaumcraftmodern.visnet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

final class VisNetwork {
    private VisNetwork() {
    }

    static @Nullable BlockPos findParent(
            ServerLevel level,
            VisNetworkNodeBlockEntity child
    ) {
        BlockPos origin = child.getBlockPos();
        double nearest = Double.MAX_VALUE;
        BlockPos result = null;
        for (long position : VisNetworkSpatialIndex.networkCandidates(
                level, origin)) {
            BlockPos cursor = BlockPos.of(position);
            if (!(level.getBlockEntity(cursor)
                    instanceof VisNetworkNodeBlockEntity candidate)) {
                continue;
            }
            if (cursor.equals(origin)
                    || !attunementsMatch(child, candidate)
                    // A candidate whose route already passes through this
                    // child is a descendant, not a valid parent. Without the
                    // pre-seeded visited set two relays periodically select
                    // each other during the 40-tick rebuild, lose the route,
                    // and reconnect on the following rebuild.
                    || !VisNetworkSpatialIndex.routeAvoids(
                    level, candidate, origin)) {
                continue;
            }
            double distance = cursor.distSqr(origin);
            if (distance >= nearest) {
                continue;
            }
            nearest = distance;
            result = cursor.immutable();
        }
        return result;
    }

    private static boolean attunementsMatch(
            VisNetworkNodeBlockEntity first,
            VisNetworkNodeBlockEntity second
    ) {
        return first.attunement() == -1 || second.attunement() == -1
                || first.attunement() == second.attunement();
    }

}
