package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import com.chaosbuffalo.mkcore.serialization.attributes.BooleanAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.DoubleAttribute;
import com.chaosbuffalo.mkcore.serialization.attributes.IntAttribute;
import com.chaosbuffalo.mkcore.utils.MathUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ConeSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.cone");
    protected final DoubleAttribute minRadius = new DoubleAttribute("minRadius", 0.1);
    protected final DoubleAttribute maxRadius = new DoubleAttribute("maxRadius", 1.0);
    protected final DoubleAttribute height = new DoubleAttribute("height", 1.0);
    protected final DoubleAttribute speed = new DoubleAttribute("speed", 0.00);
    protected final BooleanAttribute inverted = new BooleanAttribute("inverted", false);
    protected final IntAttribute layers = new IntAttribute("layers", 4);

    public ConeSpawnPattern() {
        super(TYPE);
        addAttributes(minRadius, maxRadius, height, inverted, speed, layers);
        this.count.setValue(40);
    }

    public ConeSpawnPattern(int count, double minRadiusIn, double maxRadiusIn, double heightIn, boolean invertedIn, double speed, int layers) {
        this();
        minRadius.setValue(minRadiusIn);
        maxRadius.setValue(maxRadiusIn);
        height.setValue(heightIn);
        inverted.setValue(invertedIn);
        this.speed.setValue(speed);
        this.layers.setValue(layers);
        this.count.setValue(count);
    }

    @Override
    public ParticleSpawnPattern copy() {
        return new ConeSpawnPattern(count.value(),
                minRadius.value(),
                maxRadius.value(),
                height.value(),
                inverted.value(),
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
        double ratio = (double) (currentLayer) / (double) (layers.value() - 1);

        double lerpWidth = inverted.value() ? MathUtils.lerpDouble(maxRadius.value(), minRadius.value(), ratio) :
                MathUtils.lerpDouble(minRadius.value(), maxRadius.value(), ratio);
        double realDegrees = (360.0 / perLayer) * realNum;
        Vec3 posVec = new Vec3(
                origin.x + (lerpWidth * Math.cos(Math.toRadians(realDegrees))),
                ratio * height.value() + origin.y,
                origin.z + (lerpWidth * Math.sin(Math.toRadians(realDegrees))));
        Vec3 diffVec = posVec.subtract(origin).normalize();
        finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(origin), posVec, diffVec.scale(speed.value())));
    }
}
