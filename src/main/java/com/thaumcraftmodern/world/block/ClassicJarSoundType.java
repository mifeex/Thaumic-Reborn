package com.thaumcraftmodern.world.block;

import com.thaumcraftmodern.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

/** Modern equivalent of TC4 4.2.3.5's {@code CustomStepSound("jar", 1, 1)}. */
public final class ClassicJarSoundType extends SoundType {
    public static final ClassicJarSoundType INSTANCE = new ClassicJarSoundType();

    private ClassicJarSoundType() {
        super(
                1.0F,
                1.0F,
                SoundEvents.GLASS_BREAK,
                SoundEvents.GLASS_STEP,
                SoundEvents.GLASS_PLACE,
                SoundEvents.GLASS_HIT,
                SoundEvents.GLASS_FALL
        );
    }

    @Override
    public SoundEvent getBreakSound() {
        return jar();
    }

    @Override
    public SoundEvent getPlaceSound() {
        return jar();
    }

    private static SoundEvent jar() {
        // Resolve lazily because blocks are registered before sound events.
        return ModSounds.JAR.get();
    }
}
