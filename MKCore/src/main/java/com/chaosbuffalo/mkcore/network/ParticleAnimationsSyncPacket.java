package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class ParticleAnimationsSyncPacket implements CustomPacketPayload {
    private final Map<ResourceLocation, CompoundTag> data;

    public static final CustomPacketPayload.Type<ParticleAnimationsSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "particle_animations_sync_packet"));

    public static final StreamCodec<FriendlyByteBuf, ParticleAnimationsSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
            ParticleAnimationsSyncPacket::toBytes, ParticleAnimationsSyncPacket::new
    );

    public ParticleAnimationsSyncPacket(Map<ResourceLocation, ParticleAnimation> animations) {
        data = new HashMap<>();
        for (Map.Entry<ResourceLocation, ParticleAnimation> entry : animations.entrySet()) {
            Tag dyn = entry.getValue().serialize(NbtOps.INSTANCE);
            if (dyn instanceof CompoundTag) {
                data.put(entry.getKey(), (CompoundTag) dyn);
            } else {
                throw new RuntimeException(String.format(
                        "Particle Animation %s did not serialize to a CompoundNBT!", entry.getKey()));
            }
        }
    }


    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(data.size());
        for (Map.Entry<ResourceLocation, CompoundTag> animData : data.entrySet()) {
            buffer.writeResourceLocation(animData.getKey());
            buffer.writeNbt(animData.getValue());
        }
    }

    public ParticleAnimationsSyncPacket(FriendlyByteBuf buffer) {
        int count = buffer.readInt();
        data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            ResourceLocation animName = buffer.readResourceLocation();
            CompoundTag animData = buffer.readNbt();
            data.put(animName, animData);
        }
    }

    public static void handle(final ParticleAnimationsSyncPacket packet, IPayloadContext context) {
        for (Map.Entry<ResourceLocation, CompoundTag> animData : packet.data.entrySet()) {
            ParticleAnimation anim = ParticleAnimation.deserializeFromDynamic(animData.getKey(),
                    new Dynamic<>(NbtOps.INSTANCE, animData.getValue()));
            ParticleAnimationManager.ANIMATIONS.put(animData.getKey(), anim);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
