package com.chaosbuffalo.mkcore.fx.particles.spawn_patterns;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.MKParticleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class SinglePositionSpawnPattern extends ParticleSpawnPattern {
    public final static ResourceLocation TYPE = new ResourceLocation(MKCore.MOD_ID, "particle_spawn_pattern.single");

    public SinglePositionSpawnPattern() {
        super(TYPE);
        count.setValue(1);
    }

    @Override
    public SinglePositionSpawnPattern copy() {
        return new SinglePositionSpawnPattern();
    }

    @Override
    public void produceParticlesForIndex(Vec3 origin, int particleNumber, @Nullable List<Vec3> additionalLocs, Level world, Function<Vec3, MKParticleData> particleDataSupplier, List<ParticleSpawnEntry> finalParticles) {
        finalParticles.add(new ParticleSpawnEntry(particleDataSupplier.apply(origin), origin, new Vec3(0, 0, 0)));
    }
}
