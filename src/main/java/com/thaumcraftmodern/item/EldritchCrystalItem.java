package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.EldritchCrystalItemClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Hidden BlockCrystal metadata-7 item, matching TC4's non-creative variant. */
public final class EldritchCrystalItem extends BlockItem {
    public EldritchCrystalItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(EldritchCrystalItemClientExtensions.create());
    }
}
