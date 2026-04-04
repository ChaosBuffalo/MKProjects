package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class DirectionalConeSpawnPattern extends ParticleSpawnPattern {
    public static final ResourceLocation TYPE = MKCore.id("particle_spawn_pattern.directional_cone");

    protected final DoubleAttribute halfAngleDegrees = new DoubleAttribute("halfAngleDegrees", 20.0);
    protected final DoubleAttribute innerHalfAngleDegrees = new DoubleAttribute("innerHalfAngleDegrees", 0.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.2);

    public DirectionalConeSpawnPattern() {
        super(TYPE);
        addAttributes(halfAngleDegrees, innerHalfAngleDegrees, speed);
        count.setValue(40);
    }

    public DirectionalConeSpawnPattern(int count, double halfAngleDegrees, double innerHalfAngleDegrees, double speed) {
        this();
        this.count.setValue(count);
        this.halfAngleDegrees.setValue(halfAngleDegrees);
        this.innerHalfAngleDegrees.setValue(innerHalfAngleDegrees);
        this.speed.setValue(speed);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new DirectionalConeSpawnPattern(count.value(), halfAngleDegrees.value(),
                innerHalfAngleDegrees.value(), speed.value());
    }

    protected Vec3 getEndpoint(Vec3 origin, @Nullable List<Vec3> additionalLocs) {
        if (additionalLocs != null && !additionalLocs.isEmpty()) {
            return additionalLocs.get(0);
        }
        return origin.add(0.0, 1.0, 0.0);
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        Vec3 endpoint = getEndpoint(origin, additionalLocs);
        Vec3 axis = endpoint.subtract(origin);
        if (axis.lengthSqr() < 1.0e-8) {
            return;
        }

        Vec3 forward = axis.normalize();
        Vec3 reference = Math.abs(forward.y()) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = forward.cross(reference).normalize();
        Vec3 up = right.cross(forward).normalize();

        double minAngle = Math.toRadians(Math.max(0.0, innerHalfAngleDegrees.value()));
        double maxAngle = Math.toRadians(Math.max(minAngle, halfAngleDegrees.value()));

        // Sample by cosine so particles are distributed across the cone volume uniformly.
        double cosMin = Math.cos(minAngle);
        double cosMax = Math.cos(maxAngle);
        double cosPhi = Mth.lerp(world.getRandom().nextDouble(), cosMin, cosMax);
        double sinPhi = Math.sqrt(Math.max(0.0, 1.0 - cosPhi * cosPhi));
        double theta = world.getRandom().nextDouble() * (Math.PI * 2.0);

        Vec3 direction = forward.scale(cosPhi)
                .add(right.scale(Math.cos(theta) * sinPhi))
                .add(up.scale(Math.sin(theta) * sinPhi))
                .normalize();

        finalParticles.add(new ParticleSpawnEntry(
                particleDataSupplier.apply(origin),
                origin,
                direction.scale(speed.value())
        ));
    }
}
