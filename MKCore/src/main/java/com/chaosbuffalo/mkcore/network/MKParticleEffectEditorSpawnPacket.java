package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MKParticleEffectEditorSpawnPacket extends MKParticleEffectSpawnPacket {

    public MKParticleEffectEditorSpawnPacket(Vec3 posVec, ParticleAnimation anim) {
        super(posVec, anim);
    }

    public MKParticleEffectEditorSpawnPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public static final CustomPacketPayload.Type<MKParticleEffectEditorSpawnPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "particle_effect_editor_spawn"));

    public static final StreamCodec<FriendlyByteBuf, MKParticleEffectEditorSpawnPacket> STREAM_CODEC = StreamCodec.ofMember(
            MKParticleEffectEditorSpawnPacket::toBytes, MKParticleEffectEditorSpawnPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MKParticleEffectEditorSpawnPacket packet, IPayloadContext context) {
        if (context.player().isCreative()) {
            PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(packet.xPos, packet.yPos, packet.zPos, packet.anim),
                    context.player());
        }

    }
}
