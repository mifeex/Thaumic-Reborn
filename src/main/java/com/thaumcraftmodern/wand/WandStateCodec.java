package com.thaumcraftmodern.wand;

import com.thaumcraftmodern.aura.PrimalAspect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import com.thaumcraftmodern.item.WandItem;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Versioned NBT codec for wand item stacks. Unknown versions, components,
 * incomplete primal maps, and out-of-capacity values are rejected rather than
 * silently repaired.
 */
public final class WandStateCodec {
    public static final int SERIAL_VERSION = 1;
    public static final String ROOT_KEY = "thaumic_reborn_wand";

    private static final String VERSION_KEY = "version";
    private static final String ROD_KEY = "rod";
    private static final String CAP_KEY = "cap";
    private static final String VIS_KEY = "vis";

    private WandStateCodec() {
    }

    public static CompoundTag encode(WandState state) {
        Objects.requireNonNull(state, "state");
        if (state.version() != SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "cannot encode wand state version " + state.version()
            );
        }
        CompoundTag root = new CompoundTag();
        root.putInt(VERSION_KEY, state.version());
        root.putString(ROD_KEY, state.rodId());
        root.putString(CAP_KEY, state.capId());
        CompoundTag vis = new CompoundTag();
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            vis.putInt(aspect.id(), state.visCentivis(aspect));
        }
        root.put(VIS_KEY, vis);
        return root;
    }

    public static void write(ItemStack stack, WandState state) {
        Objects.requireNonNull(stack, "stack");
        stack.getOrCreateTag().put(ROOT_KEY, encode(state));
    }

    public static WandState decode(
            ItemStack stack,
            WandComponentCatalog components
    ) {
        Objects.requireNonNull(stack, "stack");
        CompoundTag owner = stack.getTag();
        if (owner == null || !owner.contains(ROOT_KEY, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("wand stack is missing " + ROOT_KEY);
        }
        int capacityPercent = stack.getItem() instanceof WandItem wand
                ? wand.form().applyCapacity(100)
                : 100;
        WandState state = decode(
                owner.getCompound(ROOT_KEY),
                components,
                capacityPercent
        );
        WandRodDefinition rod = components.rod(state.rodId()).orElseThrow();
        if (stack.getItem() instanceof WandItem wand
                && (wand.form() == WandForm.STAFF) != rod.staff()) {
            throw new IllegalArgumentException(
                    "wand form " + wand.form()
                            + " is incompatible with rod " + rod.id()
            );
        }
        return state;
    }

    public static WandState decode(
            CompoundTag root,
            WandComponentCatalog components
    ) {
        return decode(root, components, 100);
    }

    private static WandState decode(
            CompoundTag root,
            WandComponentCatalog components,
            int capacityPercent
    ) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(components, "components");
        if (!root.contains(VERSION_KEY, Tag.TAG_INT)) {
            throw new IllegalArgumentException("wand state is missing version");
        }
        int version = root.getInt(VERSION_KEY);
        if (version != SERIAL_VERSION) {
            throw new IllegalArgumentException(
                    "unsupported wand state version " + version
                            + "; expected " + SERIAL_VERSION
            );
        }
        if (!root.contains(ROD_KEY, Tag.TAG_STRING)) {
            throw new IllegalArgumentException("wand state is missing rod");
        }
        if (!root.contains(CAP_KEY, Tag.TAG_STRING)) {
            throw new IllegalArgumentException("wand state is missing cap");
        }
        String rodId = root.getString(ROD_KEY);
        String capId = root.getString(CAP_KEY);
        WandRodDefinition rod = components.rod(rodId).orElseThrow(() ->
                new IllegalArgumentException("unknown wand rod id: " + rodId)
        );
        components.cap(capId).orElseThrow(() ->
                new IllegalArgumentException("unknown wand cap id: " + capId)
        );
        if (!root.contains(VIS_KEY, Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("wand state is missing vis");
        }

        CompoundTag vis = root.getCompound(VIS_KEY);
        Set<String> expected = new HashSet<>();
        EnumMap<PrimalAspect, Integer> amounts =
                new EnumMap<>(PrimalAspect.class);
        int capacityCentivis = Math.multiplyExact(
                Math.multiplyExact(rod.capacityVis(), capacityPercent) / 100,
                100
        );
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            expected.add(aspect.id());
            if (!vis.contains(aspect.id(), Tag.TAG_INT)) {
                throw new IllegalArgumentException(
                        "wand vis is missing " + aspect.id()
                );
            }
            int amount = vis.getInt(aspect.id());
            if (amount < 0 || amount > capacityCentivis) {
                throw new IllegalArgumentException(
                        "wand " + aspect.id() + " vis " + amount
                                + " is outside 0.." + capacityCentivis
                );
            }
            amounts.put(aspect, amount);
        }
        if (!vis.getAllKeys().equals(expected)) {
            Set<String> unexpected = new HashSet<>(vis.getAllKeys());
            unexpected.removeAll(expected);
            throw new IllegalArgumentException(
                    "wand vis contains non-primal entries: " + unexpected
            );
        }
        return new WandState(version, rodId, capId, amounts);
    }

    public static ItemStack create(ItemStack stack, String rodId, String capId) {
        write(stack, WandState.empty(SERIAL_VERSION, rodId, capId));
        return stack;
    }
}
