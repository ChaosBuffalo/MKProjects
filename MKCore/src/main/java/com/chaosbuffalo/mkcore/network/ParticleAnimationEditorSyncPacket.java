package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


import static com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager.RAW_EFFECT;

public class ParticleAnimationEditorSyncPacket implements CustomPacketPayload {

    protected final ParticleAnimation anim;
    protected final int currentKeyFrame;

    public static final CustomPacketPayload.Type<ParticleAnimationEditorSyncPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "particle_animation_editor_sync"));

    public static final StreamCodec<FriendlyByteBuf, ParticleAnimationEditorSyncPacket> STREAM_CODEC = StreamCodec.ofMember(
            ParticleAnimationEditorSyncPacket::toBytes, ParticleAnimationEditorSyncPacket::new
    );



    public ParticleAnimationEditorSyncPacket(ParticleAnimation anim, int currentKeyFrame) {
        this.anim = anim;
        this.currentKeyFrame = currentKeyFrame;
    }


    public ParticleAnimationEditorSyncPacket(FriendlyByteBuf buf) {
        this.currentKeyFrame = buf.readInt();
        this.anim = ParticleAnimation.deserializeFromDynamic(RAW_EFFECT, new Dynamic<>(NbtOps.INSTANCE,
                buf.readNbt()));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(currentKeyFrame);

        Tag dyn = anim.serialize(NbtOps.INSTANCE);
        if (dyn instanceof CompoundTag) {
            buf.writeNbt((CompoundTag) dyn);
        } else {
            throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", anim));
        }
    }


    public static void handle(final ParticleAnimationEditorSyncPacket packet, IPayloadContext context) {
        if (context.player().isCreative()) {
            MKCore.getPlayer(context.player()).ifPresent(data -> data.getEditor().getParticleEditorData()
                    .update(packet.anim, packet.currentKeyFrame, false));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
