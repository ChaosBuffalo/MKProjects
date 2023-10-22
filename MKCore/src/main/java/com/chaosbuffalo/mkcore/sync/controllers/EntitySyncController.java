package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.packets.EntityDataUpdatePacket;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.EnumSet;

public class EntitySyncController extends SyncController {

    protected final Entity entity;

    public EntitySyncController(Entity entity) {
        this.entity = entity;
    }

    protected boolean updatesBlocked() {
        // Only support server->client sync
        return entity.getLevel().isClientSide;
    }

    @Override
    public void syncUpdates() {
        if (updatesBlocked()) {
            return;
        }

        supportedVisibilities().forEach(visibility -> {
            SyncGroup group = getVisibilityGroup(visibility);
            if (group.isDirty()) {
                CompoundTag tag = new CompoundTag();
                group.serializeUpdate(tag);
                EntityDataUpdatePacket packet = new EntityDataUpdatePacket(entity, tag, EnumSet.of(visibility));
                MKCore.LOGGER.info("sending {} dirty update {} for {}", visibility, packet, entity);
                visibility.sendPacket(packet, entity);
            }
        });
    }

    @Override
    public void sendFullSync(ServerPlayer otherPlayer) {
        if (entity.getLevel().isClientSide) {
            return;
        }

        CompoundTag tag = new CompoundTag();

        EnumSet<SyncVisibility> visibilities = EnumSet.noneOf(SyncVisibility.class);
        supportedVisibilities().forEach(visibility -> {
            if (visibility.isVisibleTo(entity, otherPlayer)) {
                getVisibilityGroup(visibility).serializeFull(tag);
                visibilities.add(visibility);
            }
        });

        EntityDataUpdatePacket packet = new EntityDataUpdatePacket(entity, tag, visibilities);
        MKCore.LOGGER.info("sending full sync {} for {} to {}", packet, entity, otherPlayer);
        PacketHandler.sendMessage(packet, otherPlayer);
    }
}
