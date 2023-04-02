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

public class PillarSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.pillar");
    protected final DoubleAttribute xRadius = new DoubleAttribute("xRadius", 1.0);
    protected final DoubleAttribute yRadius = new DoubleAttribute("yRadius", 0.0);
    protected final DoubleAttribute zRadius = new DoubleAttribute("zRadius", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.0);
    protected final IntAttribute layers = new IntAttribute("layers", 4);
    protected final DoubleAttribute layerHeight = new DoubleAttribute("layerHeight", 0.25);

    public PillarSpawnPattern() {
        super(TYPE);
        addAttributes(xRadius, yRadius, zRadius, speed, layers, layerHeight);
    }

    public PillarSpawnPattern(int count, Vec3 radii, double speed, int layers, double layerHeight) {
        this();
        this.count.setValue(count);
        xRadius.setValue(radii.x);
        yRadius.setValue(radii.y);
        zRadius.setValue(radii.z);
        this.speed.setValue(speed);
        this.layers.setValue(layers);
        this.layerHeight.setValue(layerHeight);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new PillarSpawnPattern(count.value(),
                new Vec3(xRadius.value(), yRadius.value(), zRadius.value()),
                speed.value(), layers.value(), layerHeight.value());
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs,
                                         Level world, Function<Vec3, MKParticleData> particleDataSupplier,
                                         List<ParticleSpawnEntry> finalParticles) {
        int perLayer = count.value() / layers.value();
        int currentLayer = particleNumber / perLayer;
        int inLayer = particleNumber % perLayer;
        double height = currentLayer * layerHeight.value();
        double degrees = (360.0 / perLayer) * inLayer;
        Vec3 posVec = new Vec3(origin.x + xRadius.value() * Math.cos(Math.toRadians(degrees)),
                origin.y + yRadius.value() + height, origin.z + zRadius.value() * Math.sin(Math.toRadians(degrees)));
        Vec3 diffVec = posVec.subtract(origin).normalize();
        finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(origin), posVec, diffVec.scale(speed.value())));
    }

}