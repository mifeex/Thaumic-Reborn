package com.thaumcraftmodern.client.particle;

import com.thaumcraftmodern.particle.TubeVentParticleOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/** Faithful 1.20 rendering adapter for TC4 {@code FXVent}. */
public final class TubeVentParticle extends TextureSheetParticle {
    private static final float RENDER_SCALE = 0.3F;
    private static final float BASE_ALPHA = 0.4F;
    private final SpriteSet sprites;
    private final float targetScale;
    private float growthScale;

    private TubeVentParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            int color,
            float scale,
            SpriteSet sprites
    ) {
        super(level, x, y, z);
        this.sprites = sprites;
        setSize(0.02F, 0.02F);
        targetScale = Math.max(0.01F, scale);
        growthScale = (random.nextFloat() * 0.1F + 0.05F)
                * targetScale;
        quadSize = RENDER_SCALE * growthScale;
        rCol = ((color >> 16) & 255) / 255.0F;
        gCol = ((color >> 8) & 255) / 255.0F;
        bCol = (color & 255) / 255.0F;
        alpha = BASE_ALPHA;
        lifetime = 40;
        hasPhysics = false;
        setHeading(xSpeed, ySpeed, zSpeed, 0.125F, 5.0F);
        updateSprite();
    }

    private void setHeading(
            double x,
            double y,
            double z,
            float speed,
            float spread
    ) {
        double norm = Math.sqrt(x * x + y * y + z * z);
        if (norm < 1.0E-4D) {
            xd = yd = zd = 0.0D;
            return;
        }
        x /= norm;
        y /= norm;
        z /= norm;
        x += random.nextGaussian() * (random.nextBoolean() ? -1 : 1)
                * 0.0075F * spread;
        y += random.nextGaussian() * (random.nextBoolean() ? -1 : 1)
                * 0.0075F * spread;
        z += random.nextGaussian() * (random.nextBoolean() ? -1 : 1)
                * 0.0075F * spread;
        xd = x * speed;
        yd = y * speed;
        zd = z * speed;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime || growthScale > targetScale) {
            remove();
            return;
        }
        yd += 0.0025D;
        move(xd, yd, zd);
        xd *= 0.85D;
        yd *= 0.85D;
        zd *= 0.85D;
        if (growthScale < targetScale) {
            growthScale *= 1.15F;
            quadSize = RENDER_SCALE * growthScale;
        }
        if (onGround) {
            xd *= 0.7D;
            zd *= 0.7D;
        }
        alpha = BASE_ALPHA * Mth.clamp(
                (targetScale - growthScale) / targetScale,
                0.0F,
                1.0F
        );
        updateSprite();
    }

    private void updateSprite() {
        int frame = Mth.clamp(
                (int) (growthScale / targetScale * 4.0F), 0, 4
        );
        setSprite(sprites.get(frame, 4));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider
            implements ParticleProvider<TubeVentParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public TubeVentParticle createParticle(
                TubeVentParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new TubeVentParticle(
                    level,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    options.color(),
                    options.scale(),
                    sprites
            );
        }
    }
}
