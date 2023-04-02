package com.chaosbuffalo.mkcore.sync;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.events.PlayerDataEvent;
import com.chaosbuffalo.mkcore.network.PacketHandler;
import com.chaosbuffalo.mkcore.network.PlayerDataSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class PlayerUpdateEngine extends UpdateEngine {
    private final MKPlayerData playerData;
    private final Player player;
    private final SyncGroup privateUpdater = new SyncGroup();

    public PlayerUpdateEngine(MKPlayerData playerEntity) {
        this.playerData = playerEntity;
        this.player = playerEntity.getEntity();
    }

    @Override
    public void addPrivate(ISyncObject syncObject) {
        privateUpdater.add(syncObject);
        if (syncObject instanceof SyncGroup) {
            ((SyncGroup) syncObject).forceDirty();
        }
    }

    @Override
    public void removePrivate(ISyncObject syncObject) {
        privateUpdater.remove(syncObject);
    }

    @Override
    public void serializeUpdate(CompoundTag updateTag, boolean fullSync, boolean privateUpdate) {
//        MKCore.LOGGER.info("serializeClientUpdate full:{} private:{}", fullSync, privateUpdate);
        ISyncObject updater = privateUpdate ? privateUpdater : publicUpdater;
        if (fullSync) {
            updater.serializeFull(updateTag);
        } else {
            updater.serializeUpdate(updateTag);
        }
    }

    @Override
    public void deserializeUpdate(CompoundTag updateTag, boolean privateUpdate) {
//        MKCore.LOGGER.info("deserializeClientUpdatePre private:{} {}", privateUpdate, updateTag);
        publicUpdater.deserializeUpdate(updateTag);
        if (privateUpdate) {
            privateUpdater.deserializeUpdate(updateTag);
        }

        MinecraftForge.EVENT_BUS.post(new PlayerDataEvent.Updated(playerData));
    }

    @Override
    public void syncUpdates() {
        if (!playerData.isServerSide())
            return;

        if (!readyForUpdates) {
//            MKCore.LOGGER.info("deferring update because client not ready");
            return;
        }

        if (publicUpdater.isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage(false);
            MKCore.LOGGER.info("sending public dirty update {} for {}", packet, player);
            PacketHandler.sendToTrackingAndSelf(packet, player);
        }

        if (privateUpdater.isDirty()) {
            PlayerDataSyncPacket packet = getUpdateMessage(true);
            MKCore.LOGGER.info("sending private dirty update {} for {}", packet, player);
            PacketHandler.sendMessage(packet, (ServerPlayer) player);
        }
    }

    private PlayerDataSyncPacket getUpdateMessage(boolean privateUpdate) {
        CompoundTag tag = new CompoundTag();
        serializeUpdate(tag, false, privateUpdate);
        return new PlayerDataSyncPacket(player.getUUID(), tag, privateUpdate);
    }

    @Override
    public void sendAll(ServerPlayer otherPlayer) {
        if (!playerData.isServerSide())
            return;
        CompoundTag tag = new CompoundTag();
        publicUpdater.serializeFull(tag);
        boolean privateUpdate = player == otherPlayer;
        if (privateUpdate) {
            privateUpdater.serializeFull(tag);
            readyForUpdates = true;
        }
        PlayerDataSyncPacket packet = new PlayerDataSyncPacket(player.getUUID(), tag, privateUpdate);
        MKCore.LOGGER.info("sending full sync {} for {} to {}", packet, player, otherPlayer);
        PacketHandler.sendMessage(packet, otherPlayer);
    }

}
