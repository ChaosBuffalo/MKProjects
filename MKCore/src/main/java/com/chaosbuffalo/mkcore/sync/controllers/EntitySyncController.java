package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.packets.EntityDataUpdatePacket;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import com.chaosbuffalo.mkcore.sync.v2.SyncGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EntitySyncController implements SyncController {

    protected final Entity entity;
    protected final SyncGroup rootGroup = new SyncGroup() {
        @Override
        protected void onMemberUpdated(SyncVisibility visibility) {
            super.onMemberUpdated(visibility);
            childUpdated();
        }
    };
    protected boolean anyDirty;
    protected boolean enableLogging = false;

    public EntitySyncController(Entity entity) {
        this.entity = entity;
    }

    public void add(String name, ISyncObject syncObject, SyncVisibility visibility) {
        rootGroup.add(name, syncObject, visibility);
    }

    public void addGroup(String name, SyncGroup group) {
        rootGroup.addGroup(name, group);
    }

    public void remove(String name, ISyncObject syncObject, SyncVisibility visibility) {
        rootGroup.remove(name, syncObject, visibility);
    }

    public void applyRemoteUpdate(SyncContext context, CompoundTag updateTag, SyncVisibility visibility) {
        rootGroup.handleUpdatePayload(context, updateTag, visibility);
    }

    protected void childUpdated() {
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
            if (rootGroup.isDirty(visibility)) {
                CompoundTag tag = rootGroup.writeDirtyValue(context, visibility);
                if (tag == null) {
                    continue;
                }

                var updateTag = new EntityDataUpdatePacket.UpdateTag(visibility, tag);
                EntityDataUpdatePacket packet = new EntityDataUpdatePacket(entity.getId(), List.of(updateTag));
                if (MKCore.DEV_LOGGING && logEntity(entity)) {
                    MKCore.LOGGER.info("sending {} dirty update {} for {}\n{}", visibility, packet, entity, NbtUtils.prettyPrint(tag));
                }
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
        List<EntityDataUpdatePacket.UpdateTag> updateTags = new ArrayList<>(2);
        for (SyncVisibility visibility : supportedVisibilities()) {
            if (visibility.isVisibleTo(entity, otherPlayer)) {
                CompoundTag tag = rootGroup.writeFullValue(context, visibility);
                if (tag == null) {
                    continue;
                }

                updateTags.add(new EntityDataUpdatePacket.UpdateTag(visibility, tag));
            }
        }

        if (updateTags.isEmpty()) {
            return;
        }

        EntityDataUpdatePacket packet = new EntityDataUpdatePacket(entity.getId(), updateTags);
        if (MKCore.DEV_LOGGING && logEntity(entity)) {
            for (var updateTag : updateTags) {
                MKCore.LOGGER.info("sending {} full update for {}\n{}", updateTag.visibility(), entity,
                        NbtUtils.prettyPrint(updateTag.tag()));
            }
        }

        PacketHandler.sendMessage(packet, otherPlayer);
    }

    public void onJoinLevel() {
        // Clear all dirty elements to avoid pointless packets after spawn.
        // Should be safe because no one has seen this entity yet.
        if (!entity.isAddedToLevel()) {
            rootGroup.clearDirty();
        }
    }

    protected boolean logEntity(Entity entity) {
        return enableLogging;
    }
}
