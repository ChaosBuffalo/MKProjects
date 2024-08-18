package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager.RAW_EFFECT;

public class WriteAnimationPacket implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<WriteAnimationPacket> TYPE = new CustomPacketPayload.Type<>(
            MKCore.id("write_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WriteAnimationPacket> STREAM_CODEC = StreamCodec.ofMember(
            WriteAnimationPacket::toBytes, WriteAnimationPacket::new
    );

    protected final ResourceLocation name;
    protected final ParticleAnimation anim;

    public WriteAnimationPacket(ResourceLocation name, ParticleAnimation anim) {
        this.name = name;
        this.anim = anim;
    }

    public WriteAnimationPacket(RegistryFriendlyByteBuf buf) {
        this.name = buf.readResourceLocation();
        this.anim = ParticleAnimation.deserializeFromDynamic(RAW_EFFECT, new Dynamic<>(NbtOps.INSTANCE,
                buf.readNbt()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(name);
        Tag dyn = anim.serialize(NbtOps.INSTANCE);
        if (dyn instanceof CompoundTag) {
            buf.writeNbt(dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", name));
        }
    }

    public void handle(IPayloadContext context) {
        if (context.player().isCreative()) {
            MKCore.getAnimationManager().writeAnimationToWorldGenerated(name, anim);
        }
    }
}
