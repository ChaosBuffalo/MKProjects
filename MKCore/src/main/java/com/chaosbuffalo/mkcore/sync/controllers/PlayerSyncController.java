package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.events.PlayerDataEvent;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class PlayerSyncController extends EntitySyncController {
    private static final EnumSet<SyncVisibility> PLAYER_VISIBILITY = EnumSet.of(SyncVisibility.Public, SyncVisibility.Private);
    private final MKPlayerData playerData;
    private boolean readyForUpdates = false;
    private final boolean enableUpdateLogging = false;
    private final List<Throwable> dirtyLog = new ArrayList<>();

    public PlayerSyncController(MKPlayerData playerData) {
        super(playerData.getEntity());
        this.playerData = playerData;
    }

    @Override
    protected Set<SyncVisibility> supportedVisibilities() {
        return PLAYER_VISIBILITY;
    }

    @Override
    protected void childUpdated(ISyncObject child) {
        super.childUpdated(child);
        if (enableUpdateLogging) {
            dirtyLog.add(new Exception().fillInStackTrace());
        }
    }

    @Override
    public void deserializeUpdate(CompoundTag updateTag, Set<SyncVisibility> visibility) {
        super.deserializeUpdate(updateTag, visibility);
        MinecraftForge.EVENT_BUS.post(new PlayerDataEvent.Updated(playerData));
    }

    @Override
    public boolean syncUpdates() {
        if (!readyForUpdates) {
            return false;
        }
        if (enableUpdateLogging) {
            MKCore.LOGGER.info("player {} dirty {}", playerData, dirtyLog.size());
        }
        boolean updated = super.syncUpdates();
        if (enableUpdateLogging && updated) {
            dirtyLog.clear();
        }
        return updated;
    }

    @Override
    public void sendFullSync(ServerPlayer otherPlayer) {
        super.sendFullSync(otherPlayer);
        if (entity == otherPlayer) {
            readyForUpdates = true;
        }
    }
}
