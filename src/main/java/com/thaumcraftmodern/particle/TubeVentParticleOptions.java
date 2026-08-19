package com.thaumcraftmodern.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.thaumcraftmodern.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Locale;

/** Carries TC4's RGB value and caller-selected scale to the vent particle. */
public record TubeVentParticleOptions(int color, float scale)
        implements ParticleOptions {
    public TubeVentParticleOptions(int color) {
        this(color, 1.0F);
    }

    public static final Codec<TubeVentParticleOptions> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(
                            TubeVentParticleOptions::color
                    ),
                    Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(
                            TubeVentParticleOptions::scale
                    )
            ).apply(instance, TubeVentParticleOptions::new));

    public static final Deserializer<TubeVentParticleOptions> DESERIALIZER =
            new Deserializer<>() {
                @Override
                public TubeVentParticleOptions fromCommand(
                        ParticleType<TubeVentParticleOptions> type,
                        StringReader reader
                ) throws CommandSyntaxException {
                    reader.expect(' ');
                    int color = reader.readInt();
                    float scale = 1.0F;
                    if (reader.canRead()) {
                        reader.expect(' ');
                        scale = reader.readFloat();
                    }
                    return new TubeVentParticleOptions(color, scale);
                }

                @Override
                public TubeVentParticleOptions fromNetwork(
                        ParticleType<TubeVentParticleOptions> type,
                        FriendlyByteBuf buffer
                ) {
                    return new TubeVentParticleOptions(
                            buffer.readInt(), buffer.readFloat()
                    );
                }
            };

    @Override
    public ParticleType<?> getType() {
        return ModParticles.TUBE_VENT.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeInt(color);
        buffer.writeFloat(scale);
    }

    @Override
    public String writeToString() {
        return String.format(
                Locale.ROOT,
                "%s %d %.3f",
                BuiltInRegistries.PARTICLE_TYPE.getKey(getType()),
                color,
                scale
        );
    }
}
