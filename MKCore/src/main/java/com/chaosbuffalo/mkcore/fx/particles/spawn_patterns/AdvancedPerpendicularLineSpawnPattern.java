package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdvancedPerpendicularLineSpawnPattern extends ParticleSpawnPattern {
    public static final ResourceLocation TYPE = MKCore.id("particle_spawn_pattern.advanced_perpendicular_line");
    private static final double MIN_DIRECTION_LENGTH_SQR = 1.0e-8;
    private static final Vec3 DEFAULT_DIRECTION = new Vec3(0.0, 1.0, 0.0);

    protected final DoubleAttribute offset = new DoubleAttribute("offset", 0.0);
    protected final DoubleAttribute motion = new DoubleAttribute("motion", 0.0);
    protected final IntAttribute perPosCount = new IntAttribute("per_pos_count", 10);
    protected final DoubleAttribute randomStepLerp = new DoubleAttribute("random_step_lerp", 0.0);

    public AdvancedPerpendicularLineSpawnPattern() {
        super(TYPE);
        addAttributes(offset, motion, perPosCount, randomStepLerp);
    }

    public AdvancedPerpendicularLineSpawnPattern(double offset, double motion, int perPosCount, double randomStepLerp) {
        this();
        this.offset.setValue(offset);
        this.motion.setValue(motion);
        this.perPosCount.setValue(perPosCount);
        this.randomStepLerp.setValue(randomStepLerp);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new AdvancedPerpendicularLineSpawnPattern(offset.value(), motion.value(),
                perPosCount.value(), randomStepLerp.value());
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        if (additionalLocs == null || additionalLocs.size() < 2) {
            return;
        }

        Vec3 direction = additionalLocs.get(0);
        Vec3 data = additionalLocs.get(1);
        double perParticle = data.y();
        Vec3 finalOrigin = origin.add(direction.scale(perParticle * particleNumber));
        double clampedRandomLerp = Mth.clamp(randomStepLerp.value(), 0.0, 1.0);

        int countForPos = Math.max(1, perPosCount.value());
        if (countForPos == 1) {
            Vec3 spawnOrigin = finalOrigin;
            if (clampedRandomLerp > 0.0) {
                Vec3 nextOrigin = finalOrigin.add(direction.scale(perParticle));
                spawnOrigin = finalOrigin.lerp(nextOrigin, world.getRandom().nextDouble() * clampedRandomLerp);
            }
            finalParticles.add(new ParticleSpawnEntry(
                    particleDataSupplier.apply(spawnOrigin),
                    spawnOrigin,
                    Vec3.ZERO
            ));
            return;
        }

        Vec3 right = getPerpendicularAxis(direction);

        for (int i = 0; i < countForPos; i++) {
            double lerpV = (double) i / (countForPos - 1);
            double slotDistance = Mth.lerp(lerpV, -offset.value(), offset.value());
            Vec3 spawnOrigin = finalOrigin;
            if (clampedRandomLerp > 0.0) {
                Vec3 nextOrigin = finalOrigin.add(direction.scale(perParticle));
                spawnOrigin = finalOrigin.lerp(nextOrigin, world.getRandom().nextDouble() * clampedRandomLerp);
            }

            Vec3 spawnPos = spawnOrigin.add(right.scale(slotDistance));
            Vec3 motionVec = spawnOrigin.subtract(spawnPos);
            if (motionVec.lengthSqr() > 0.0) {
                motionVec = motionVec.normalize().scale(this.motion.value());
            } else {
                motionVec = Vec3.ZERO;
            }

            finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(spawnOrigin), spawnPos, motionVec));
        }
    }

    @Override
    public void spawn(ParticleType<MKParticleData> particleType,
                      Vec3 position, Vec3 scale, Level world, ParticleAnimation anim, @Nullable List<Vec3> additionalLocs) {
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        Tuple<List<Vec3>, Double> spawnData = getSpawnData(position, additionalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        for (int i = 0; i < particleCount; i++) {
            produceParticlesForIndex(position, i, spawnData.getA(), world,
                    (pos) -> new MKParticleData(particleType, pos, scale, anim),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles) {
            spawnParticle(world, entry);
        }
    }

    @Override
    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType,
                                      Vec3 offset, Vec3 scale, Level world,
                                      ParticleAnimation anim, Entity entity, List<Vec3> additionalLocs) {
        Vec3 position = offset.add(entity.position());
        List<Vec3> finalLocs = additionalLocs.stream().map(
                x -> x.add(entity.position())).collect(Collectors.toList());
        Tuple<List<Vec3>, Double> spawnData = getSpawnData(position, finalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        for (int i = 0; i < particleCount; i++) {
            produceParticlesForIndex(position, i, spawnData.getA(), world,
                    (pos) -> new MKParticleData(particleType, offset, anim, entity.getId(), scale),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles) {
            spawnParticle(world, entry);
        }
    }

    public Vec3 getEndpoint(Vec3 position, @Nullable List<Vec3> additionalLocs) {
        if (additionalLocs != null && !additionalLocs.isEmpty()) {
            return additionalLocs.get(0);
        }
        return position.add(new Vec3(5.0, 0.0, 0.0));
    }

    protected Tuple<List<Vec3>, Double> getSpawnData(Vec3 position, @Nullable List<Vec3> additionalLocs) {
        List<Vec3> spawnData = new ArrayList<>();
        Vec3 endPoint = getEndpoint(position, additionalLocs);
        Vec3 axis = endPoint.subtract(position);
        if (axis.lengthSqr() < MIN_DIRECTION_LENGTH_SQR) {
            axis = DEFAULT_DIRECTION;
            endPoint = position.add(axis);
        }
        double distance = endPoint.distanceTo(position);
        Vec3 direction = axis.normalize();
        long particleCount = Math.max(1L, Math.round(distance * count.value()));
        Vec3 lineData = new Vec3(distance, distance / particleCount, 0);
        spawnData.add(direction);
        spawnData.add(lineData);
        return new Tuple<>(spawnData, distance);
    }

    private Vec3 getPerpendicularAxis(Vec3 direction) {
        Vec3 normalizedDirection = direction.lengthSqr() < MIN_DIRECTION_LENGTH_SQR ? DEFAULT_DIRECTION : direction.normalize();
        Vec3 reference = Math.abs(normalizedDirection.y()) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 perpendicular = normalizedDirection.cross(reference);
        if (perpendicular.lengthSqr() < MIN_DIRECTION_LENGTH_SQR) {
            reference = new Vec3(0.0, 0.0, 1.0);
            perpendicular = normalizedDirection.cross(reference);
        }
        return perpendicular.normalize();
    }
}
