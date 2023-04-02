package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ParticleAnimationsSyncPacket {
    private final Map<ResourceLocation, CompoundTag> data;

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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        MKCore.LOGGER.debug("Handling particle animation sync packet");
        ctx.enqueueWork(() -> {
            for (Map.Entry<ResourceLocation, CompoundTag> animData : data.entrySet()) {
                ParticleAnimation anim = ParticleAnimation.deserializeFromDynamic(animData.getKey(),
                        new Dynamic<>(NbtOps.INSTANCE, animData.getValue()));
                ParticleAnimationManager.ANIMATIONS.put(animData.getKey(), anim);
            }
        });
        ctx.setPacketHandled(true);
    }
}
