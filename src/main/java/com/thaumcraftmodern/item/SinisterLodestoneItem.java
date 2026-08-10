package com.thaumcraftmodern.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** Original 256-block, 0.66-radian field-of-view sinister-node indicator. */
public final class SinisterLodestoneItem extends Item {
    public SinisterLodestoneItem(Properties properties) { super(properties); }

    public static boolean isVisibleTo(Entity holder, Vec3 nodePosition) {
        return SinisterLodestoneVisibility.isVisibleTo(
                holder.getEyePosition(),
                holder.getLookAngle().normalize(),
                nodePosition
        );
    }
}
