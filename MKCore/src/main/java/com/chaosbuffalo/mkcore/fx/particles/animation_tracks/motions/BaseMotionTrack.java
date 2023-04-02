package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public abstract class BaseMotionTrack extends ParticleMotionAnimationTrack {
    public BaseMotionTrack(ResourceLocation typeName) {
        super(typeName);
    }

    @Override
    public Vec3 getMotion(MKParticle particle) {
        return particle.getMotion();
    }
}
