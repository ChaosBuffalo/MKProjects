package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.BrownianMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.OrbitingInPlaneMotionTrack;

public class CoreParticleAnimations {
    public static final ParticleAnimation BlueMagicAnimation = new ParticleAnimation()
            .withKeyFrame(new ParticleKeyFrame()
                    .withColor(0.0f, 1.0f, 242.0f / 255.0f)
                    .withScale(0.5f, 0.25f)
            )
            .withKeyFrame(new ParticleKeyFrame(0, 20)
                    .withMotion(new OrbitingInPlaneMotionTrack(10.0, 0.0, .2f))
            )
            .withKeyFrame(new ParticleKeyFrame(20, 20)
                    .withMotion(new OrbitingInPlaneMotionTrack(15.0, 0.0, .2f))
            )
//            .withKeyFrame(new ParticleKeyFrame(50, 100)
//                    .withMotion(new ParticleStaticMotionAnimation(0.0, -0.05, 0.0, 0.01))
//            )
            .withKeyFrame(new ParticleKeyFrame(0, 40)
                    .withColor(0.0f, 0.5f, 0.5f)
                    .withScale(0.15f, .05f)
            )
            .withKeyFrame(new ParticleKeyFrame(40, 40)
                    .withColor(1.0f, 0.0f, 1.0f)
                    .withScale(.01f, 0.0f)
                    .withMotion(new BrownianMotionTrack(5, 0.025f))
            );
}
