package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
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

public class LineSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.line");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 1.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final Vec3 EMPTY_VEC = new Vec3(0.0, 0.0, 0.0);

    public LineSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius);
    }

    public LineSpawnPattern(double xRadius, double yRadius, double zRadius) {
        this();
        this.xRadius.setValue(xRadius);
        this.yRadius.setValue(yRadius);
        this.zRadius.setValue(zRadius);
    }


    @Override
    public ParticleSpawnPattern copy() {
        return new LineSpawnPattern(xRadius.value(), yRadius.value(), zRadius.value());
    }

    private Vec3 getRandomOffset(Level world) {
        double x = (2.0 * xRadius.value()) * world.getRandom().nextDouble() - xRadius.value();
        double y = (2.0 * yRadius.value()) * world.getRandom().nextDouble() - yRadius.value();
        double z = (2.0 * zRadius.value()) * world.getRandom().nextDouble() - zRadius.value();
        return new Vec3(x, y, z);
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        if (additionalLocs != null) {
            Vec3 direction = additionalLocs.get(0);
            Vec3 data = additionalLocs.get(1);
            double perParticle = data.y();
            Vec3 offset = getRandomOffset(world);
            Vec3 finalPos = origin.add(direction.scale(perParticle * particleNumber)).add(offset);
            finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(finalPos), finalPos, EMPTY_VEC));
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
}
