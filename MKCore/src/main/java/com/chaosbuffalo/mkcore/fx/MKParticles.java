package com.chaosbuffalo.mkcore.fx;

import com.chaosbuffalo.mkcore.network.MKParticleEffectSpawnPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class MKParticles {

    public static void spawn(Entity target, Vec3 posVec, ResourceLocation animName, int entityId) {
        PacketHandler.sendToTrackingAndSelf(new MKParticleEffectSpawnPacket(
                posVec, animName,
                entityId), target);
    }

    public static void spawn(Entity target, Vec3 posVec, ResourceLocation animName) {
        spawn(target, posVec, animName, target.getId());
    }

    public static void spawn(Entity target, Vec3 posVec, ResourceLocation animName,
                             Consumer<MKParticleEffectSpawnPacket> customizer) {
        var packet = new MKParticleEffectSpawnPacket(posVec, animName);
        customizer.accept(packet);
        PacketHandler.sendToTrackingAndSelf(packet, target);
    }
}
