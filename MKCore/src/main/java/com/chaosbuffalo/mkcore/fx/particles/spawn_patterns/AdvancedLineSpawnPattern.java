package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.math.AxisAngle;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdvancedLineSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.advanced_line");
    protected final DoubleAttribute offset = new DoubleAttribute("offset", 0.0);
    protected final DoubleAttribute motion = new DoubleAttribute("motion", 0.0);
    protected final IntAttribute perPosCount = new IntAttribute("per_pos_count", 10);

    public AdvancedLineSpawnPattern() {
        super(TYPE);
        addAttributes(offset, motion, perPosCount);
    }

    public AdvancedLineSpawnPattern(double offset, double motion, int perPosCount) {
        this();
        this.offset.setValue(offset);
        this.motion.setValue(motion);
        this.perPosCount.setValue(perPosCount);

    }


    @Override
    public ParticleSpawnPattern copy() {
        return new AdvancedLineSpawnPattern(offset.value(), motion.value(), perPosCount.value());
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {

        if (additionalLocs != null) {
            Vec3 direction = additionalLocs.get(0);
            Vec3 data = additionalLocs.get(1);
            for (int i = 0; i < perPosCount.value(); i++) {
                AxisAngle axis = new AxisAngle(Math.PI * 2 * i / perPosCount.value(), direction.x(), direction.y(), direction.z());
                Vec3 mcOffset = axis.transform(new Vec3(1.0, 0.0, 0.0));
                double perParticle = data.y();
                Vec3 finalOrigin = origin.add(direction.scale(perParticle * particleNumber));
                Vec3 spawnPos = finalOrigin.add(mcOffset.scale(this.offset.value()));
                Vec3 motion = finalOrigin.subtract(spawnPos).normalize().scale(this.motion.value());
                finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(finalOrigin), spawnPos, motion));
            }
        }
    }

    @Override
    public void spawn(ParticleType<MKParticleData> particleType,
                      Vec3 position, Level world, ParticleAnimation anim, @Nullable List<Vec3> additionalLocs) {
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        Tuple<List<Vec3>, Double> spawnData = getSpawnData(position, additionalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        for (int i = 0; i < particleCount; i++) {
            produceParticlesForIndex(position, i, spawnData.getA(), world,
                    (pos) -> new MKParticleData(particleType, pos, anim),
                    finalParticles);
        }
        for (ParticleSpawnEntry entry : finalParticles) {
            spawnParticle(world, entry);
        }
    }

    @Override
    public void spawnOffsetFromEntity(ParticleType<MKParticleData> particleType,
                                      Vec3 offset, Level world,
                                      ParticleAnimation anim, Entity entity, List<Vec3> additionalLocs) {
        Vec3 position = offset.add(entity.position());
        List<Vec3> finalLocs = additionalLocs.stream().map(
                x -> x.add(entity.position())).collect(Collectors.toList());
        Tuple<List<Vec3>, Double> spawnData = getSpawnData(position, finalLocs);
        long particleCount = Math.round(spawnData.getB() * count.value());
        List<ParticleSpawnEntry> finalParticles = new ArrayList<>();
        for (int i = 0; i < particleCount; i++) {
            produceParticlesForIndex(position, i, spawnData.getA(), world,
                    (pos) -> new MKParticleData(particleType, offset, anim, entity.getId()),
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
        return position.add(new Vec3(0.0, 5.0, 0.0));
    }

    protected Tuple<List<Vec3>, Double> getSpawnData(Vec3 position, @Nullable List<Vec3> additionalLocs) {
        List<Vec3> spawnData = new ArrayList<>();
        Vec3 endPoint = getEndpoint(position, additionalLocs);
        double distance = endPoint.distanceTo(position);
        Vec3 direction = endPoint.subtract(position).normalize();
        long particleCount = Math.round(distance * count.value());
        Vec3 lineData = new Vec3(distance, distance / particleCount, 0);
        spawnData.add(direction);
        spawnData.add(lineData);
        return new Tuple<>(spawnData, distance);
    }
}
