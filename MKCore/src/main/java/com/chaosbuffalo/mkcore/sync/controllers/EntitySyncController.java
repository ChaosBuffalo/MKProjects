package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.packets.EntityDataUpdatePacket;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncGroup;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.EnumSet;

public class EntitySyncController extends SyncController {

    protected final Entity entity;
    protected boolean anyDirty;

    public EntitySyncController(Entity entity) {
        this.entity = entity;
    }

    @Override
    protected SyncGroup createGroup(SyncVisibility visibility) {
        var group = super.createGroup(visibility);
        group.setNotifier(this::childUpdated);
        return group;
    }

    protected void childUpdated(ISyncObject child) {
        setAnyDirty();
    }

    protected void setAnyDirty() {
        anyDirty = true;
    }

    // Only call on the server
    @Override
    public boolean syncUpdates() {
        if (!anyDirty) {
            return false;
        }

        var context = new SyncContext(entity.registryAccess());
        for (SyncVisibility visibility : supportedVisibilities()) {
            SyncGroup group = getVisibilityGroup(visibility);
            if (group.isDirty()) {
                CompoundTag tag = group.writeUpdateValue(context);
                if (tag == null) {
                    continue;
                }

                EntityDataUpdatePacket packet = new EntityDataUpdatePacket(entity.getId(), tag, EnumSet.of(visibility));
                MKCore.LOGGER.info("sending {} dirty update {} for {}\n{}", visibility, packet, entity, NbtUtils.prettyPrint(tag));
                visibility.sendPacket(packet, entity);
            }
        }
        anyDirty = false;
        return true;
    }

    @Override
    public void sendFullSync(ServerPlayer otherPlayer) {
        if (entity.level().isClientSide) {
            return;
        }

        var context = new SyncContext(entity.registryAccess());
        for (SyncVisibility visibility : supportedVisibilities()) {
            SyncGroup group = getVisibilityGroup(visibility);
            if (visibility.isVisibleTo(entity, otherPlayer)) {
                CompoundTag tag = group.writeFullValue(context);
                if (tag == null) {
                    continue;
                }

                EntityDataUpdatePacket packet = new EntityDataUpdatePacket(entity.getId(), tag, EnumSet.of(visibility));
                MKCore.LOGGER.info("sending {} full update {} for {}\n{}", visibility, packet, entity, NbtUtils.prettyPrint(tag));

                PacketHandler.sendMessage(packet, otherPlayer);
            }
        }
    }
}
