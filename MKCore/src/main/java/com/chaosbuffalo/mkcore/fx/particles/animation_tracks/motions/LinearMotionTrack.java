package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.fx.particles.animation_tracks.ParticleMotionAnimationTrack;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class LinearMotionTrack extends ParticleMotionAnimationTrack {
    protected final DoubleAttribute xSpeed = new DoubleAttribute("xSpeed", 0.0f);
    protected final DoubleAttribute ySpeed = new DoubleAttribute("ySpeed", 0.0f);
    protected final DoubleAttribute zSpeed = new DoubleAttribute("zSpeed", 0.0f);
    protected final DoubleAttribute varianceMagnitude = new DoubleAttribute("varianceMagnitude", 0.0f);
    private Vec3 motionVec;
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.linear_motion");
    private final MKParticle.ParticleDataKey VARIANCE_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);

    public LinearMotionTrack(double xSpeed, double ySpeed, double zSpeed, double varianceMagnitude) {
        this();
        this.xSpeed.setValue(xSpeed);
        this.ySpeed.setValue(ySpeed);
        this.zSpeed.setValue(zSpeed);
        this.motionVec = new Vec3(xSpeed, ySpeed, zSpeed);
        this.varianceMagnitude.setValue(varianceMagnitude);
    }

    public LinearMotionTrack() {
        super(TYPE_NAME);
        addAttributes(xSpeed, ySpeed, zSpeed, varianceMagnitude);
    }

    @Override
    public void begin(MKParticle particle, int duration) {
        particle.setTrackVector3dData(VARIANCE_VECTOR,
                new Vec3(generateVariance(particle), generateVariance(particle), generateVariance(particle)));
    }

    @Override
    public void apply(MKParticle particle) {
        particle.setMotion(xSpeed.value(), ySpeed.value(), zSpeed.value());
    }

    @Override
    public LinearMotionTrack copy() {
        return new LinearMotionTrack(xSpeed.value(), ySpeed.value(), zSpeed.value(), varianceMagnitude.value());
    }

    @Override
    public Vec3 getMotion(MKParticle particle) {
        return motionVec.add(particle.getTrackVector3dData(VARIANCE_VECTOR).scale(varianceMagnitude.value()));
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
        Vec3 currentMotion = particle.getCurrentFrame().getMotionTrack().getMotion(particle);
        Vec3 desiredMotion = getMotion(particle);
        particle.setMotion(
                MathUtils.lerpDouble(currentMotion.x, desiredMotion.x, time),
                MathUtils.lerpDouble(currentMotion.y, desiredMotion.y, time),
                MathUtils.lerpDouble(currentMotion.z, desiredMotion.z, time)
        );
    }

    @Override
    public <D> void readAdditionalData(Dynamic<D> dynamic) {
        super.readAdditionalData(dynamic);
        motionVec = new Vec3(xSpeed.value(), ySpeed.value(), zSpeed.value());
    }
}
