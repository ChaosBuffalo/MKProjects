package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;

public abstract class ParticleColorAnimationTrack extends ParticleAnimationTrack {

    public ParticleColorAnimationTrack(ResourceLocation typeName) {
        super(typeName, AnimationTrackType.COLOR);
    }

    public abstract Vector3f getColor(MKParticle particle);

    @Override
    public abstract ParticleColorAnimationTrack copy();

    protected float getColorWithVariance(float color, float varianceMagnitude, float variance) {
        return Math.max(0.0f, Math.min(1.0f, color + varianceMagnitude * variance));
    }

    @Override
    public void apply(MKParticle particle) {
        Vector3f color = getColor(particle);
        particle.setColor(color.x(), color.y(), color.z());
    }

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setColorTrack(this);
    }


}
