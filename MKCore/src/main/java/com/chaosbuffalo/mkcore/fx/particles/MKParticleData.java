package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.stream.Stream;


public class MKParticleData implements ParticleOptions {

    protected final Vec3 origin;
    protected final ParticleAnimation animation;
    protected final int entityId;
    protected final Vec3 scale;

    private final ParticleType<MKParticleData> particleType;

    public static MapCodec<MKParticleData> mapCodec(ParticleType<MKParticleData> type) {
        return MapCodec.assumeMapUnsafe(typeCodec(type));
    }

    public static PrimitiveCodec<MKParticleData> typeCodec(ParticleType<MKParticleData> type) {
        return new PrimitiveCodec<>() {
            @Override
            public <T> DataResult<MKParticleData> read(DynamicOps<T> ops, T input) {
                Dynamic<T> d = new Dynamic<>(ops, input);
                List<Double> vecD = d.get("origin").asList(x -> x.asDouble(0.0));
                Vec3 origin = new Vec3(0.0, 0.0, 0.0);
                if (vecD.size() == 3) {
                    origin = new Vec3(vecD.get(0), vecD.get(1), vecD.get(2));
                } else {
                    MKCore.LOGGER.warn("Failed to read origin from MKParticleData {}", input);
                }
                List<Double> scaleD = d.get("scale").asList(x -> x.asDouble(1.0));
                Vec3 scale = new Vec3(1., 1., 1.);
                if (vecD.size() == 3) {
                    scale = new Vec3(scaleD.get(0), scaleD.get(1), scaleD.get(2));
                } else {
                    MKCore.LOGGER.warn("Failed to read scale from MKParticleData {}", input);
                }
                int sourceId = d.get("entityId").asInt(-1);
                ParticleAnimation newAnim = d.get("animation").map(x -> {
                    ParticleAnimation anim = new ParticleAnimation();
                    anim.deserialize(x);
                    return anim;
                }).result().orElse(new ParticleAnimation());
                return DataResult.success(new MKParticleData(type, origin, newAnim, sourceId, scale));
            }

            @Override
            public <T> T write(DynamicOps<T> ops, MKParticleData value) {
                ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
                builder.put(ops.createString("origin"),
                        ops.createList(Stream.of(ops.createDouble(value.origin.x()),
                                ops.createDouble(value.origin.y()), ops.createDouble(value.origin.z()))));
                builder.put(ops.createString("animation"), value.animation.serialize(ops));
                builder.put(ops.createString("entityId"), ops.createInt(value.entityId));
                builder.put(ops.createString("scale"),
                        ops.createList(Stream.of(ops.createDouble(value.scale.x),
                                ops.createDouble(value.scale.y), ops.createDouble(value.scale.z))));
                return ops.createMap(builder.build());
            }
        };

    }

    public static StreamCodec<RegistryFriendlyByteBuf, MKParticleData> streamCodec(ParticleType<MKParticleData> type) {
        return StreamCodec.of((bytes, particle) -> {
            bytes.writeDouble(particle.origin.x);
            bytes.writeDouble(particle.origin.y);
            bytes.writeDouble(particle.origin.z);
            bytes.writeInt(particle.entityId);
            bytes.writeDouble(particle.scale.x);
            bytes.writeDouble(particle.scale.y);
            bytes.writeDouble(particle.scale.z);
            Tag dyn = particle.animation.serialize(NbtOps.INSTANCE);
            if (dyn instanceof CompoundTag) {
                bytes.writeNbt(dyn);
            } else {
                throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", BuiltInRegistries.PARTICLE_TYPE.getKey(particle.getType())));
            }
        }, (bytes) -> {
            Vec3 origin = new Vec3(bytes.readDouble(), bytes.readDouble(), bytes.readDouble());
            int source = bytes.readInt();
            Vec3 scale = new Vec3(bytes.readDouble(), bytes.readDouble(), bytes.readDouble());
            Dynamic<?> dynamic = new Dynamic<>(NbtOps.INSTANCE, bytes.readNbt());
            ParticleAnimation newAnim = dynamic.into(d -> {
                ParticleAnimation anim = new ParticleAnimation();
                anim.deserialize(d);
                return anim;
            });
            return new MKParticleData(type, origin, newAnim, source, scale);
        });
    }


    public MKParticleData(ParticleType<MKParticleData> typeIn, Vec3 origin, ParticleAnimation animation, int entityId, Vec3 scale) {
        this.particleType = typeIn;
        this.origin = origin;
        this.animation = animation;
        this.entityId = entityId;
        this.scale = scale;
    }

    public MKParticleData(ParticleType<MKParticleData> typeIn, Vec3 origin, Vec3 scale, ParticleAnimation animation) {
        this(typeIn, origin, animation, -1, scale);
    }

    public boolean hasSource() {
        return entityId != -1;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public ParticleType<MKParticleData> getType() {
        return particleType;
    }
}
