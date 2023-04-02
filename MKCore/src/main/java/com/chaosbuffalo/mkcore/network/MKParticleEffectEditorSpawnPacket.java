package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MKParticleEffectEditorSpawnPacket extends MKParticleEffectSpawnPacket {

    public MKParticleEffectEditorSpawnPacket(Vec3 posVec, ParticleAnimation anim) {
        super(posVec, anim);
    }

    public MKParticleEffectEditorSpawnPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public static void handle(MKParticleEffectEditorSpawnPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            if (ctx.getSender() != null && ctx.getSender().isCreative()) {
                PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(packet.xPos, packet.yPos, packet.zPos, packet.anim),
                        ctx.getSender());
            }
        });
        ctx.setPacketHandled(true);
    }
}
