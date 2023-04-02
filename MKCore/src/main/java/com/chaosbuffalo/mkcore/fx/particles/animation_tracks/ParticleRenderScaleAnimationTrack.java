package com.chaosbuffalo.mkcore.fx.particles.animation_tracks;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.resources.ResourceLocation;

public class ParticleRenderScaleAnimationTrack extends ParticleAnimationTrack {
    protected final FloatAttribute renderScale = new FloatAttribute("renderScale", 1.0f);
    protected final FloatAttribute maxVariance = new FloatAttribute("maxVariance", 0.0f);
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.render_scale");
    private final MKParticle.ParticleDataKey VARIANCE_KEY = new MKParticle.ParticleDataKey(this, keyCount++);


    public ParticleRenderScaleAnimationTrack(float scale, float maxVariance) {
        this();
        this.renderScale.setValue(scale);
        this.maxVariance.setValue(maxVariance);
    }

    public ParticleRenderScaleAnimationTrack() {
        super(TYPE_NAME, AnimationTrackType.SCALE);
        addAttributes(renderScale, maxVariance);
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setScale(getScaleWithVariance(particle));
    }

    @Override
    public ParticleRenderScaleAnimationTrack copy() {
        return new ParticleRenderScaleAnimationTrack(renderScale.value(), maxVariance.value());
    }

    protected float getScaleWithVariance(MKParticle particle) {
        return renderScale.value() + (particle.getTrackFloatData(VARIANCE_KEY) * maxVariance.value());
    }

    @Override
    public void end(MKParticle particle) {
        particle.getCurrentFrame().setScaleTrack(this);
    }

    @Override
    public void begin(MKParticle particle, int duration) {
        particle.setTrackFloatData(VARIANCE_KEY, generateVariance(particle));
    }

    @Override
    public void animate(MKParticle particle, float time, int trackTick, int duration, float partialTicks) {
        particle.setScale(MathUtils.lerp(particle.getCurrentFrame().getScaleTrack().getScaleWithVariance(particle),
                getScaleWithVariance(particle), time));
    }
}
