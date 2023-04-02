package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager.RAW_EFFECT;

public class WriteAnimationPacket {

    protected final ResourceLocation name;
    protected final ParticleAnimation anim;

    public WriteAnimationPacket(ResourceLocation name, ParticleAnimation anim) {
        this.name = name;
        this.anim = anim;
    }

    public WriteAnimationPacket(FriendlyByteBuf buf) {
        this.name = buf.readResourceLocation();
        this.anim = ParticleAnimation.deserializeFromDynamic(RAW_EFFECT, new Dynamic<>(NbtOps.INSTANCE,
                buf.readNbt()));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeResourceLocation(name);
        Tag dyn = anim.serialize(NbtOps.INSTANCE);
        if (dyn instanceof CompoundTag) {
            buf.writeNbt((CompoundTag) dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", name));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null && ctx.getSender().isCreative()) {
                MKCore.getAnimationManager().writeAnimationToWorldGenerated(name, anim);
            }
        });
        ctx.setPacketHandled(true);
    }
}
