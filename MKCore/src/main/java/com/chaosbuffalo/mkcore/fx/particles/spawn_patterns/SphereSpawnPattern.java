package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SphereSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.sphere");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 1.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.00);
    protected final IntAttribute layers = new IntAttribute("layers", 4);

    public SphereSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius, speed, layers);
        this.count.setValue(40);
    }

    public SphereSpawnPattern(int count, Vec3 radii, double speed, int layers) {
        this();
        xRadius.setValue(radii.x);
        yRadius.setValue(radii.y);
        zRadius.setValue(radii.z);
        this.speed.setValue(speed);
        this.layers.setValue(layers);
        this.count.setValue(count);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new SphereSpawnPattern(count.value(),
                new Vec3(xRadius.value(), yRadius.value(), zRadius.value()),
                speed.value(),
                layers.value());
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        int perLayer = count.value() / layers.value();
        particleNumber = particleNumber % (perLayer * layers.value());
        int currentLayer = particleNumber / perLayer;
        int realNum = particleNumber % perLayer;
        double ratio = (double) (currentLayer + 1) / (double) (layers.value() + 2);
        double scaledRatio = 2.0 * (ratio - 0.5);
        double realDegrees = (360.0 / perLayer) * realNum;
        double inverseScale = 1.0 - Math.pow(scaledRatio, 2.0);
        Vec3 posVec = new Vec3(
                origin.x + (xRadius.value() * inverseScale * Math.cos(Math.toRadians(realDegrees))),
                scaledRatio * yRadius.value() + origin.y,
                origin.z + (zRadius.value() * inverseScale * Math.sin(Math.toRadians(realDegrees))));
        Vec3 diffVec = posVec.subtract(origin).normalize();
        finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(origin), posVec, diffVec.scale(speed.value())));
    }
}
