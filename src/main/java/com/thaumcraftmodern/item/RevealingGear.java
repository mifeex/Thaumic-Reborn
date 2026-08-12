package com.thaumcraftmodern.item;

import net.minecraft.world.item.ItemStack;

/** Common server/client predicate for TC4 node- and essentia-revealing headgear. */
public interface RevealingGear extends com.thaumicreborn.api.equipment.RevealingGear {

    static boolean equipped(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getItem() instanceof com.thaumicreborn.api.equipment.RevealingGear gear
                && gear.reveals(stack);
    }
}
