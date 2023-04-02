package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class BrownianMotionTrack extends ParticleMotionAnimationTrack {
    protected final IntAttribute tickInterval = new IntAttribute("tickInterval", 5);
    protected final FloatAttribute magnitude = new FloatAttribute("magnitude", 1.0f);
    protected final BooleanAttribute doX = new BooleanAttribute("doX", true);
    protected final BooleanAttribute doY = new BooleanAttribute("doY", true);
    protected final BooleanAttribute doZ = new BooleanAttribute("doZ", true);
    protected final BooleanAttribute doGravity = new BooleanAttribute("doGravity", true);
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.brownian_motion");
    private final MKParticle.ParticleDataKey VARIANCE_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);

    public BrownianMotionTrack(int tickInterval, float magnitude) {
        this();
        this.tickInterval.setValue(tickInterval);
        this.magnitude.setValue(magnitude);
    }

    public BrownianMotionTrack() {
        super(TYPE_NAME);
        addAttributes(tickInterval, magnitude, doX, doY, doZ, doGravity);
    }

    public BrownianMotionTrack disableX() {
        this.doX.setValue(false);
        return this;
    }

    public BrownianMotionTrack disableY() {
        this.doY.setValue(false);
        return this;
    }

    public BrownianMotionTrack disableZ() {
        this.doZ.setValue(false);
        return this;
    }

    public BrownianMotionTrack withGravity(boolean value) {
        this.doGravity.setValue(value);
        return this;
    }

    @Override
    public BrownianMotionTrack copy() {
        BrownianMotionTrack copy = new BrownianMotionTrack(tickInterval.value(), magnitude.value()).withGravity(doGravity.value());
        if (!doX.value()) {
            copy.disableX();
        }
        if (!doY.value()) {
            copy.disableY();
        }
        if (!doZ.value()) {
            copy.disableZ();
        }
        return copy;
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
        if (tick % tickInterval.value() == 0) {
            double motionX = doX.value() ? generateVariance(particle) * magnitude.value() : particle.getMotionX();
            double motionY = doY.value() ? generateVariance(particle) * magnitude.value() : particle.getMotionY();
            double motionZ = doZ.value() ? generateVariance(particle) * magnitude.value() : particle.getMotionZ();
            if (doGravity.value()) {
                motionY += particle.getParticleGravity() * tickInterval.value();
            }
            particle.setTrackVector3dData(VARIANCE_VECTOR, new Vec3(motionX, motionY, motionZ));
        }
        float tickTime = Math.min(1.0f, ((tick % tickInterval.value()) + 1.0f) / tickInterval.value());
        Vec3 goalVec = particle.getTrackVector3dData(VARIANCE_VECTOR);
        particle.setMotion(
                MathUtils.lerpDouble(particle.getMotionX(), goalVec.x, tickTime),
                MathUtils.lerpDouble(particle.getMotionY(), goalVec.y, tickTime),
                MathUtils.lerpDouble(particle.getMotionZ(), goalVec.z, tickTime)
        );
    }

    @Override
    public Vec3 getMotion(MKParticle particle) {
        return particle.getTrackVector3dData(VARIANCE_VECTOR);
    }
}
