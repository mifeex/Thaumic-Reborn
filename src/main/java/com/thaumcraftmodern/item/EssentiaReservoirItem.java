package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.EssentiaReservoirItemClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Uses TC4's combined core and OBJ-shell inventory renderer. */
public final class EssentiaReservoirItem extends BlockItem {
    public EssentiaReservoirItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(EssentiaReservoirItemClientExtensions.create());
    }
}
