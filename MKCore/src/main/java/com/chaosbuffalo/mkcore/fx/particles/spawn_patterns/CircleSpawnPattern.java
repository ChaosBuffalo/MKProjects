package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class CircleSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.circle");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 0.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.05);

    public CircleSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius, speed);
    }

    public CircleSpawnPattern(int count, Vec3 radii, double speed) {
        this();
        this.count.setValue(count);
        xRadius.setValue(radii.x);
        yRadius.setValue(radii.y);
        zRadius.setValue(radii.z);
        this.speed.setValue(speed);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new CircleSpawnPattern(count.value(), new Vec3(xRadius.value(), yRadius.value(), zRadius.value()),
                speed.value());
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        double degrees = (360.0 / count.value()) * particleNumber;
        Vec3 posVec = new Vec3(origin.x + xRadius.value() * Math.cos(Math.toRadians(degrees)),
                origin.y + yRadius.value(), origin.z + zRadius.value() * Math.sin(Math.toRadians(degrees)));
        Vec3 diffVec = posVec.subtract(origin).normalize();
        finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(origin), posVec, diffVec.scale(speed.value())));
    }

}
