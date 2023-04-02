package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.fx.ParticleEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ParticleEffectSpawnPacket {
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

    public ParticleEffectSpawnPacket(FriendlyByteBuf buf) {
        this.particleID = EntityDataSerializers.PARTICLE.read(buf);
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

    public void toBytes(FriendlyByteBuf buf) {
        EntityDataSerializers.PARTICLE.write(buf, particleID);
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

    public static void handle(ParticleEffectSpawnPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
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
                    player.level);
        }
    }
}
