package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import net.minecraft.resources.ResourceLocation;

public class InheritMotionTrack extends BaseMotionTrack {
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.particle_motion");

    public InheritMotionTrack() {
        super(TYPE_NAME);
    }

    @Override
    public InheritMotionTrack copy() {
        return new InheritMotionTrack();
    }
}
