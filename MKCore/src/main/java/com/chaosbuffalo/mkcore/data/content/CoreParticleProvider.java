package com.chaosbuffalo.mkcore.data.content;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.data.providers.ParticleAnimationProvider;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleKeyFrame;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.BrownianMotionTrack;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions.OrbitingInPlaneMotionTrack;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;

import java.util.concurrent.CompletableFuture;

public class CoreParticleProvider extends ParticleAnimationProvider {

    public CoreParticleProvider(DataGenerator generator) {
        super(generator, MKCore.MOD_ID);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return writeBlueMagicTest(pOutput);
    }

    public CompletableFuture<?> writeBlueMagicTest(CachedOutput pOutput) {
        ParticleAnimation anim = new ParticleAnimation()
                .withKeyFrame(new ParticleKeyFrame()
                        .withColor(0.0f, 1.0f, 242.0f / 255.0f)
                        .withScale(0.5f, 0.25f)
                )
                .withKeyFrame(new ParticleKeyFrame(0, 20)
                        .withMotion(new OrbitingInPlaneMotionTrack(10.0, 0.0, .25f))
                )
                .withKeyFrame(new ParticleKeyFrame(20, 20)
                        .withMotion(new OrbitingInPlaneMotionTrack(15.0, 0.0, .25f))
                )
                .withKeyFrame(new ParticleKeyFrame(0, 40)
                        .withColor(0.0f, 0.5f, 0.5f)
                        .withScale(0.15f, .05f)
                )
                .withKeyFrame(new ParticleKeyFrame(40, 40)
                        .withColor(1.0f, 0.0f, 1.0f)
                        .withScale(.01f, 0.0f)
                        .withMotion(new BrownianMotionTrack(5, 0.025f))
                );
        return writeAnimation(MKCore.makeRL("particle_anim.blue_magic"), anim, pOutput);
    }
}
