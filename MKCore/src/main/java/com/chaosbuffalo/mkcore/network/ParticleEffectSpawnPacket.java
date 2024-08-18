package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ParticleEffectSpawnPacket implements CustomPacketPayload {
    private final double xPos;
    private final double yPos;
    private final double zPos;
    private final int motionType;
    private final double speed;
    private final int count;
    private final double radiusX;
    private final double radiusY;
    private final double radiusZ;
    private final ParticleOptions particleID;
    private final int data;
    private final double headingX;
    private final double headingY;
    private final double headingZ;

    public static final CustomPacketPayload.Type<ParticleEffectSpawnPacket> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MKCore.MOD_ID, "particle_effect_spawn"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ParticleEffectSpawnPacket> STREAM_CODEC = StreamCodec.ofMember(
            ParticleEffectSpawnPacket::toBytes, ParticleEffectSpawnPacket::new
    );


    public ParticleEffectSpawnPacket(ParticleOptions particleID, int motionType, int count, int data,
                                     double xPos, double yPos, double zPos,
                                     double radiusX, double radiusY, double radiusZ,
                                     double speed, double headingX, double headingY, double headingZ) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.motionType = motionType;
        this.count = count;
        this.speed = speed;
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
        this.particleID = particleID;
        this.data = data;
        this.headingX = headingX;
        this.headingY = headingY;
        this.headingZ = headingZ;
    }

    public ParticleEffectSpawnPacket(ParticleOptions particleID, int motionType, int count, int data,
                                     double xPos, double yPos, double zPos,
                                     double radiusX, double radiusY, double radiusZ,
                                     double speed, Vec3 headingVec) {
        this(particleID, motionType, count, data,
                xPos, yPos, zPos,
                radiusX, radiusY, radiusZ, speed,
                headingVec.x, headingVec.y, headingVec.z);
    }

    public ParticleEffectSpawnPacket(ParticleOptions particleID, int motionType, int count, int data,
                                     Vec3 posVec,
                                     double radiusX, double radiusY, double radiusZ,
                                     double speed, Vec3 headingVec) {
        this(particleID, motionType, count, data, posVec.x, posVec.y, posVec.z, radiusX,
                radiusY, radiusZ, speed, headingVec.x, headingVec.y, headingVec.z);
    }

    public ParticleEffectSpawnPacket(RegistryFriendlyByteBuf buf) {
        this.particleID = EntityDataSerializers.PARTICLE.codec().decode(buf);
        this.motionType = buf.readInt();
        this.data = buf.readInt();
        this.count = buf.readInt();
        this.xPos = buf.readDouble();
        this.yPos = buf.readDouble();
        this.zPos = buf.readDouble();
        this.radiusX = buf.readDouble();
        this.radiusY = buf.readDouble();
        this.radiusZ = buf.readDouble();
        this.speed = buf.readDouble();
        this.headingX = buf.readDouble();
        this.headingY = buf.readDouble();
        this.headingZ = buf.readDouble();
    }

    public void toBytes(RegistryFriendlyByteBuf buf) {
        EntityDataSerializers.PARTICLE.codec().encode(buf, particleID);
        buf.writeInt(this.motionType);
        buf.writeInt(this.data);
        buf.writeInt(this.count);
        buf.writeDouble(this.xPos);
        buf.writeDouble(this.yPos);
        buf.writeDouble(this.zPos);
        buf.writeDouble(this.radiusX);
        buf.writeDouble(this.radiusY);
        buf.writeDouble(this.radiusZ);
        buf.writeDouble(this.speed);
        buf.writeDouble(this.headingX);
        buf.writeDouble(this.headingY);
        buf.writeDouble(this.headingZ);
    }

    public static void handle(final ParticleEffectSpawnPacket packet, IPayloadContext context) {
        ClientHandler.handleClient(packet);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static class ClientHandler {
        public static void handleClient(ParticleEffectSpawnPacket packet) {
            Player player = Minecraft.getInstance().player;
            if (player == null)
                return;

            ParticleEffects.spawnParticleEffect(
                    packet.particleID, packet.motionType, packet.data, packet.speed, packet.count,
                    new Vec3(packet.xPos, packet.yPos, packet.zPos),
                    new Vec3(packet.radiusX, packet.radiusY, packet.radiusZ),
                    new Vec3(packet.headingX, packet.headingY, packet.headingZ),
                    player.level());
        }
    }
}
