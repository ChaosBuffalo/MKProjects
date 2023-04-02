package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public abstract class ParticleMotionAnimationTrack extends ParticleAnimationTrack {

    public ParticleMotionAnimationTrack(ResourceLocation typeName) {
        super(typeName, AnimationTrackType.MOTION);
    }

    public abstract Vec3 getMotion(MKParticle particle);

    @Override
    public abstract ParticleMotionAnimationTrack copy();

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setMotionTrack(this);
    }
}
