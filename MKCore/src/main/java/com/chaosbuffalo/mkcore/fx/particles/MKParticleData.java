package com.chaosbuffalo.mkcore.fx.particles;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Stream;


public class MKParticleData implements ParticleOptions {

    protected final Vec3 origin;
    protected final ParticleAnimation animation;
    protected final int entityId;

    private final ParticleType<MKParticleData> particleType;

    public static PrimitiveCodec<MKParticleData> typeCodec(ParticleType<MKParticleData> type) {
        return new PrimitiveCodec<MKParticleData>() {


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
                int sourceId = d.get("entityId").asInt(-1);
                ParticleAnimation newAnim = d.get("animation").map(x -> {
                    ParticleAnimation anim = new ParticleAnimation();
                    anim.deserialize(x);
                    return anim;
                }).result().orElse(new ParticleAnimation());
                return DataResult.success(new MKParticleData(type, origin, newAnim, sourceId));
            }

            @Override
            public <T> T write(DynamicOps<T> ops, MKParticleData value) {
                ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
                builder.put(ops.createString("origin"),
                        ops.createList(Stream.of(ops.createDouble(value.origin.x()),
                                ops.createDouble(value.origin.y()), ops.createDouble(value.origin.z()))));
                builder.put(ops.createString("animation"), value.animation.serialize(ops));
                builder.put(ops.createString("entityId"), ops.createInt(value.entityId));
                return ops.createMap(builder.build());
            }
        };

    }

    public static final ParticleOptions.Deserializer<MKParticleData> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        public MKParticleData fromCommand(ParticleType<MKParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            // todo make this read json nbt
            return new MKParticleData(particleTypeIn, new Vec3(reader.readDouble(), reader.readDouble(), reader.readDouble()),
                    new ParticleAnimation(), -1);
        }

        public MKParticleData fromNetwork(ParticleType<MKParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            Vec3 origin = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            int source = buffer.readInt();
            Dynamic<?> dynamic = new Dynamic<>(NbtOps.INSTANCE, buffer.readNbt());
            ParticleAnimation newAnim = dynamic.into(d -> {
                ParticleAnimation anim = new ParticleAnimation();
                anim.deserialize(d);
                return anim;
            });
            return new MKParticleData(particleTypeIn, origin, newAnim, source);
        }
    };

    public MKParticleData(ParticleType<MKParticleData> typeIn, Vec3 origin, ParticleAnimation animation, int entityId) {
        this.particleType = typeIn;
        this.origin = origin;
        this.animation = animation;
        this.entityId = entityId;
    }

    public MKParticleData(ParticleType<MKParticleData> typeIn, Vec3 origin, ParticleAnimation animation) {
        this(typeIn, origin, animation, -1);
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

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeDouble(origin.x);
        buffer.writeDouble(origin.y);
        buffer.writeDouble(origin.z);
        buffer.writeInt(entityId);
        Tag dyn = animation.serialize(NbtOps.INSTANCE);
        if (dyn instanceof CompoundTag) {
            buffer.writeNbt((CompoundTag) dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", ForgeRegistries.PARTICLE_TYPES.getKey(getType())));
        }
    }

    @Override
    public String writeToString() {
        return ForgeRegistries.PARTICLE_TYPES.getKey(this.getType()) + " " + origin.toString();
    }

}
