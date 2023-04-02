package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.EntityDataSyncPacket;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EntityUpdateEngine extends UpdateEngine {

    private final Entity entity;

    public EntityUpdateEngine(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void syncUpdates() {
        if (entity.getCommandSenderWorld().isClientSide) {
            return;
        }
        if (publicUpdater.isDirty()) {
            EntityDataSyncPacket packet = getUpdateMessage();
            MKCore.LOGGER.info("sending public dirty update {} for {}", packet, entity);
            PacketHandler.sendToTracking(packet, entity);
        }
    }

    private EntityDataSyncPacket getUpdateMessage() {
        CompoundTag tag = new CompoundTag();
        serializeUpdate(tag, false, false);
        return new EntityDataSyncPacket(entity.getId(), tag);
    }

    @Override
    public void serializeUpdate(CompoundTag updateTag, boolean fullSync, boolean privateUpdate) {
        if (fullSync) {
            publicUpdater.serializeFull(updateTag);
        } else {
            publicUpdater.serializeUpdate(updateTag);
        }
    }

    @Override
    public void deserializeUpdate(CompoundTag updateTag, boolean privateUpdate) {
        publicUpdater.deserializeUpdate(updateTag);
    }

    @Override
    public void sendAll(ServerPlayer otherPlayer) {
        if (entity.getCommandSenderWorld().isClientSide)
            return;
        CompoundTag tag = new CompoundTag();
        publicUpdater.serializeFull(tag);
        EntityDataSyncPacket packet = new EntityDataSyncPacket(entity.getId(), tag);
        MKCore.LOGGER.info("sending full sync {} for {} to {}", packet, entity, otherPlayer);
        PacketHandler.sendMessage(packet, otherPlayer);
    }
}
