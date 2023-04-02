package com.chaosbuffalo.mkcore.fx.particles.animation_tracks.motions;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticle;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.FloatAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class OrbitingInPlaneMotionTrack extends BaseMotionTrack {
    protected final DoubleAttribute rpm = new DoubleAttribute("rpm", 1.0f);
    protected final DoubleAttribute rpmVarianceMagnitude = new DoubleAttribute("rpmVariance", 0.0f);
    protected final FloatAttribute rampTime = new FloatAttribute("ramp", -1.0f);
    public final static ResourceLocation TYPE_NAME = new ResourceLocation(MKCore.MOD_ID, "particle_anim.orbit_in_plane");
    private final MKParticle.ParticleDataKey MOTION_VECTOR = new MKParticle.ParticleDataKey(this,
            keyCount++);
    private final MKParticle.ParticleDataKey VARIANCE_SCALAR = new MKParticle.ParticleDataKey(this, keyCount++);


    public OrbitingInPlaneMotionTrack(double rpm, double rpmVarianceMagnitude, float rampTime) {
        this();
        this.rpm.setValue(rpm);
        this.rpmVarianceMagnitude.setValue(rpmVarianceMagnitude);
        this.rampTime.setValue(rampTime);
    }

    public OrbitingInPlaneMotionTrack() {
        super(TYPE_NAME);
        addAttributes(rpm, rpmVarianceMagnitude, rampTime);
    }


    @Override
    public OrbitingInPlaneMotionTrack copy() {
        return new OrbitingInPlaneMotionTrack(rpm.value(), rpmVarianceMagnitude.value(), rampTime.value());
    }

    @Override
    public void begin(MKParticle particle, int duration) {
        Vec3 pos = particle.getPosition();
        particle.setTrackFloatData(VARIANCE_SCALAR, generateVariance(particle));
        Vec3 originVertical = new Vec3(particle.getOrigin().x(), pos.y(),
                particle.getOrigin().z());
        Vec3 diff = pos.subtract(originVertical);
        double realRadius = pos.distanceTo(originVertical);
        double angle = MathUtils.getAngleAroundYAxis(diff.z(), diff.x());
        double w = (Math.PI * 2) / GameConstants.TICKS_PER_SECOND * ((rpm.value() + particle.getTrackFloatData(VARIANCE_SCALAR) * rpmVarianceMagnitude.value()) / 60);
        particle.setTrackVector3dData(MOTION_VECTOR, new Vec3(angle, w, realRadius));
        particle.setMotion(0, 0, 0);
        updateParticle(particle, 0, duration, 0);
    }

    private void updateParticle(MKParticle particle, float time, int duration, float partialTicks) {
        Vec3 motionData = particle.getTrackVector3dData(MOTION_VECTOR);

        float rT = rampTime.value();
        float realTime;
        boolean usingRamp = false;
        if (rT <= 0) {
            realTime = time;
        } else {
            usingRamp = true;
            realTime = time < rT ? 0.0f : MathUtils.lerp(0.0f, 1.0f, (time - rT) / (1.0f - rT));
        }
        float elapsed = realTime * duration;
        double vx = -motionData.z * Math.sin(motionData.x + elapsed * motionData.y);
        double vz = -motionData.z * Math.cos(motionData.x + elapsed * motionData.y);
        Vec3 desiredPosition = new Vec3(particle.getOrigin().x() + vx,
                particle.getPosition().y(), particle.getOrigin().z() + vz);
        if (usingRamp) {
            Vec3 pos = particle.getPosition();
            particle.setPos(
                    MathUtils.lerpDouble(pos.x(), desiredPosition.x(), time / rT),
                    MathUtils.lerpDouble(pos.y(), desiredPosition.y(), time / rT),
                    MathUtils.lerpDouble(pos.z(), desiredPosition.z(), time / rT)
            );
        } else {
            particle.setPos(desiredPosition.x(), desiredPosition.y(), desiredPosition.z());
        }
    }

    @Override
    public void animate(MKParticle particle, float time, int trackTick, int duration, float partialTicks) {
        updateParticle(particle, time, duration, partialTicks);
    }

    @Override
    public void update(MKParticle particle, int tick, float time) {
//        Vector3d variance = particle.getTrackVector3dData(MOTION_VECTOR);
//        double vx = -variance.z * Math.sin(variance.x + tick * variance.y);
//        double vz = -variance.z * Math.cos(variance.x + tick * variance.y);
//        Vector3d desiredPosition = new Vector3d(particle.getOrigin().getX() + vx,
//                particle.getPosition().getY(), particle.getOrigin().getZ() + vz);
//        Vector3d finalMotion = particle.getPosition().subtract(desiredPosition).scale(1.0 / GameConstants.TICKS_PER_SECOND);
//        if (centralGravity.getValue() != 0.0f){
//            Vector3d toOrigin = particle.getOrigin().subtract(particle.getPosition());
//            Vector3d norm = toOrigin.normalize().scale(centralGravity.getValue());
//            finalMotion = finalMotion.add(norm);
//        }
//        particle.setMotion(
//                MathUtils.lerpDouble(particle.getMotionX(), finalMotion.x, 0.9),
//                MathUtils.lerpDouble(particle.getMotionY(), finalMotion.y, 0.9),
//                MathUtils.lerpDouble(particle.getMotionZ(), finalMotion.z, 0.9)
//        );
//        particle.setMotion(0, 0, 0);
//        particle.setPosition(desiredPosition.getX(), desiredPosition.getY(), desiredPosition.getZ());


    }
}
