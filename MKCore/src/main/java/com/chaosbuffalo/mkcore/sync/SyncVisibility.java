package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.network.packets.EntityDataUpdatePacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public enum SyncVisibility {
    Private((e, p) -> e == p, SyncVisibility::sendToPlayer),
    Public((e, p) -> true, PacketHandler::sendToTracking);

    private final BiPredicate<Entity, ServerPlayer> trackingPlayerTest;
    private final BiConsumer<EntityDataUpdatePacket, Entity> sender;

    private static void sendToPlayer(EntityDataUpdatePacket packet, Entity target) {
        if (target instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendMessage(packet, serverPlayer);
        }
    }

    SyncVisibility(BiPredicate<Entity, ServerPlayer> trackingPlayerTest,
                   BiConsumer<EntityDataUpdatePacket, Entity> sender) {
        this.trackingPlayerTest = trackingPlayerTest;
        this.sender = sender;
    }

    public boolean isVisibleTo(Entity hostEntity, ServerPlayer trackingPlayer) {
        return trackingPlayerTest.test(hostEntity, trackingPlayer);
    }

    public void sendPacket(EntityDataUpdatePacket packet, Entity target) {
        if (sender != null) {
            sender.accept(packet, target);
        }
    }
}
