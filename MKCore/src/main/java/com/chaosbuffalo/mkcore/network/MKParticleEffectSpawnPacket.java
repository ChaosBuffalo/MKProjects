package com.chaosbuffalo.mkcore.network;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimation;
import com.chaosbuffalo.mkcore.fx.particles.ParticleAnimationManager;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MKParticleEffectSpawnPacket {
    protected final double xPos;
    protected final double yPos;
    protected final double zPos;
    protected final ParticleAnimation anim;
    protected final ResourceLocation animName;
    protected final boolean hasRaw;
    protected final int entityId;
    protected final List<Vec3> additionalLocs;


    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ParticleAnimation anim, int entityId) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.anim = anim;
        this.hasRaw = true;
        this.animName = ParticleAnimationManager.RAW_EFFECT;
        this.entityId = entityId;
        this.additionalLocs = new ArrayList<>();
    }

    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ParticleAnimation anim) {
        this(xPos, yPos, zPos, anim, -1);
    }

    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ResourceLocation animName, int entityId) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.zPos = zPos;
        this.anim = null;
        this.animName = animName;
        this.hasRaw = false;
        this.entityId = entityId;
        this.additionalLocs = new ArrayList<>();
    }

    public void addLoc(Vec3 loc) {
        additionalLocs.add(loc);
    }

    public MKParticleEffectSpawnPacket(double xPos, double yPos, double zPos, ResourceLocation animName) {
        this(xPos, yPos, zPos, animName, -1);
    }


    public MKParticleEffectSpawnPacket(Vec3 posVec, ParticleAnimation anim) {
        this(posVec.x, posVec.y, posVec.z, anim);
    }

    public MKParticleEffectSpawnPacket(Vec3 posVec, ResourceLocation animName, int entityId) {
        this(posVec.x(), posVec.y(), posVec.z(), animName, entityId);
    }

    public MKParticleEffectSpawnPacket(Vec3 posVec, ResourceLocation animName) {
        this(posVec, animName, -1);
    }

    public MKParticleEffectSpawnPacket(FriendlyByteBuf buf) {
        this.xPos = buf.readDouble();
        this.yPos = buf.readDouble();
        this.zPos = buf.readDouble();
        this.entityId = buf.readInt();
        int addVecCount = buf.readInt();
        additionalLocs = new ArrayList<>();
        for (int i = 0; i < addVecCount; i++) {
            additionalLocs.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        this.hasRaw = buf.readBoolean();
        if (hasRaw) {
            this.anim = ParticleAnimation.deserializeFromDynamic(ParticleAnimationManager.RAW_EFFECT,
                    new Dynamic<>(NbtOps.INSTANCE, buf.readNbt()));
            this.animName = ParticleAnimationManager.RAW_EFFECT;
        } else {
            this.animName = buf.readResourceLocation();
            ParticleAnimation anim = ParticleAnimationManager.ANIMATIONS.get(animName);
            if (anim == null) {
                this.anim = new ParticleAnimation();
                MKCore.LOGGER.warn("Failed to find managed particle animation {}", animName);
            } else {
                this.anim = anim;
            }
        }

    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(this.xPos);
        buf.writeDouble(this.yPos);
        buf.writeDouble(this.zPos);
        buf.writeInt(this.entityId);
        buf.writeInt(additionalLocs.size());
        for (Vec3 vec : additionalLocs) {
            buf.writeDouble(vec.x());
            buf.writeDouble(vec.y());
            buf.writeDouble(vec.z());
        }
        buf.writeBoolean(hasRaw);
        if (hasRaw) {
            Tag dyn = anim.serialize(NbtOps.INSTANCE);
            if (dyn instanceof CompoundTag) {
                buf.writeNbt((CompoundTag) dyn);
            } else {
                throw new RuntimeException(String.format("Particle Animation %s did not serialize to a CompoundNBT!", anim));
            }
        } else {
            if (animName != null) {
                buf.writeResourceLocation(animName);
            } else {
                buf.writeResourceLocation(ParticleAnimationManager.INVALID_EFFECT);
            }
        }

    }

    public static void handle(MKParticleEffectSpawnPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> ClientHandler.handleClient(packet));
        ctx.setPacketHandled(true);
    }

    static class ClientHandler {
        public static void handleClient(MKParticleEffectSpawnPacket packet) {
            Player player = Minecraft.getInstance().player;
            if (player == null || packet.anim == null)
                return;
            if (packet.entityId != -1) {
                Entity source = player.getCommandSenderWorld().getEntity(packet.entityId);
                if (source != null) {
                    packet.anim.spawnOffsetFromEntity(player.getCommandSenderWorld(), new Vec3(packet.xPos, packet.yPos, packet.zPos),
                            source, packet.additionalLocs);
                }
            } else {
                packet.anim.spawn(player.getCommandSenderWorld(), new Vec3(packet.xPos, packet.yPos, packet.zPos), packet.additionalLocs);
            }
        }
    }
}
