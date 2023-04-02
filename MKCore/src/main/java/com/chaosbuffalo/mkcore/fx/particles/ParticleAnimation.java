package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.fx.particles.spawn_patterns.ParticleSpawnPattern;
import com.chaosbuffalo.mkcore.init.CoreParticles;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ParticleAnimation {

    protected List<ParticleKeyFrame> keyFrames;
    protected ParticleSpawnPattern spawnPattern;
    protected ParticleType<MKParticleData> particleType;

    public ParticleAnimation() {
        this.keyFrames = new ArrayList<>();
        this.particleType = CoreParticles.MAGIC_CROSS.get();
    }

    public void setSpawnPattern(ParticleSpawnPattern spawnPattern) {
        this.spawnPattern = spawnPattern;
    }

    public void setParticleType(ParticleType<MKParticleData> particleType) {
        this.particleType = particleType;
    }

    public ParticleType<MKParticleData> getParticleType() {
        return particleType;
    }

    public boolean hasParticleType() {
        return particleType != null;
    }

    public boolean hasSpawnPattern() {
        return spawnPattern != null;
    }

    public ParticleSpawnPattern getSpawnPattern() {
        return spawnPattern;
    }

    public void addKeyFrame(ParticleKeyFrame frame) {
        this.keyFrames.add(frame);
    }

    public ParticleAnimation withKeyFrame(ParticleKeyFrame frame) {
        addKeyFrame(frame);
        return this;
    }

    public List<ParticleKeyFrame> getKeyFrames() {
        return keyFrames;
    }

    public void deleteKeyFrame(ParticleKeyFrame frame) {
        this.keyFrames.remove(frame);
    }

    public ParticleAnimation copy() {
        ParticleAnimation copy = new ParticleAnimation();
        for (ParticleKeyFrame frame : getKeyFrames()) {
            copy.addKeyFrame(frame.copy());
        }
        if (hasSpawnPattern()) {
            copy.setSpawnPattern(getSpawnPattern().copy());
        }
        copy.setParticleType(particleType);
        return copy;
    }

    public <D> D serialize(DynamicOps<D> ops) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        builder.put(ops.createString("frames"),
                ops.createList(keyFrames.stream().map(frame -> frame.serialize(ops))));
        if (hasSpawnPattern()) {
            builder.put(ops.createString("spawnPattern"), spawnPattern.serialize(ops));
        }
        if (hasParticleType()) {
            builder.put(ops.createString("particleType"),
                    ops.createString(ForgeRegistries.PARTICLE_TYPES.getKey(particleType).toString()));
        }
        return ops.createMap(builder.build());
    }

    public <D> void deserialize(Dynamic<D> dynamic) {
        List<ParticleKeyFrame> newFrames = dynamic.get("frames").asList(d -> {
            ParticleKeyFrame frame = new ParticleKeyFrame();
            frame.deserialize(d);
            return frame;
        });
        keyFrames.clear();
        keyFrames.addAll(newFrames);
        spawnPattern = dynamic.get("spawnPattern").map(d -> {
            ResourceLocation spawnPatternType = ParticleSpawnPattern.getType(d);
            ParticleSpawnPattern spawnPattern = ParticleAnimationManager.getSpawnPattern(spawnPatternType);
            if (spawnPattern != null) {
                spawnPattern.deserialize(d);
            }
            return spawnPattern;
        }).result().orElse(null);
        ResourceLocation loc = new ResourceLocation(dynamic.get("particleType").asString().result()
                .orElse(CoreParticles.MAGIC_CROSS.getId().toString()));
        particleType = (ParticleType<MKParticleData>) ForgeRegistries.PARTICLE_TYPES.getValue(loc);
    }

    public void tick(MKParticle particle) {
        for (ParticleKeyFrame frame : keyFrames) {
            if (frame.getTickStart() == particle.getAge()) {
                frame.begin(particle);
            }
            if (frame.getDuration() > 0 && particle.getAge() >= frame.getTickStart() && particle.getAge() < frame.getTickEnd()) {
                frame.update(particle, particle.getAge());
            }
            if (particle.getAge() == frame.getTickEnd() - 1) {
                frame.end(particle);
            }
        }
    }

    public int getTickLength() {
        int totalTicks = 0;
        for (ParticleKeyFrame frame : keyFrames) {
            if (frame.getTickEnd() > totalTicks) {
                totalTicks = frame.getTickEnd();
            }
        }
        return totalTicks;
    }

    public void tickAnimation(MKParticle particle, float partialTicks) {
        for (ParticleKeyFrame frame : keyFrames) {
            if (frame.getDuration() > 0 && particle.getAge() > frame.getTickStart() && particle.getAge() < frame.getTickEnd()) {
                frame.animate(particle, particle.getAge(), partialTicks);
            }
        }
    }

    public static <D> ParticleAnimation deserializeFromDynamic(ResourceLocation name, Dynamic<D> dynamic) {
        ParticleAnimation anim = new ParticleAnimation();
        anim.deserialize(dynamic);
        return anim;
    }

    public void spawn(Level world, Vec3 location, @Nullable List<Vec3> additionalLocs) {
        if (hasSpawnPattern() && hasParticleType()) {
            spawnPattern.spawn(getParticleType(), location, world, this, additionalLocs);
        }
    }

    public void spawnOffsetFromEntity(Level world, Vec3 offset, Entity entity, @Nullable List<Vec3> additionalLocs) {
        if (hasSpawnPattern() && hasParticleType()) {
            spawnPattern.spawnOffsetFromEntity(getParticleType(), offset, world, this, entity, additionalLocs);
        }
    }


}
