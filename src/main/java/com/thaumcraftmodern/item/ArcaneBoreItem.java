package com.thaumcraftmodern.item;

import com.thaumcraftmodern.client.render.ArcaneBoreItemClientExtensions;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Built-in entity-model item for the two original Arcane Bore devices. */
public final class ArcaneBoreItem extends BlockItem {
    public enum Kind { BASE, BORE }
    private final Kind kind;
    public ArcaneBoreItem(Block block, Kind kind, Properties properties) {
        super(block, properties); this.kind = kind;
    }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ArcaneBoreItemClientExtensions.create(kind));
    }
}
