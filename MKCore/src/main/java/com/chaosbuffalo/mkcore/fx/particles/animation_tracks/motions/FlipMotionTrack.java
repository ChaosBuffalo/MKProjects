package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class FlipMotionTrack extends BaseMotionTrack {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.flip_motion");

    public FlipMotionTrack() {
        super(TYPE_NAME);
    }

    @Override
    public FlipMotionTrack copy() {
        return new FlipMotionTrack();
    }

    @Override
    public void apply(MKParticle particle) {
        super.apply(particle);
        Vec3 newMotion = particle.getMotion().scale(-1);
        particle.setMotion(newMotion.x(), newMotion.y(), newMotion.z());
    }
}
