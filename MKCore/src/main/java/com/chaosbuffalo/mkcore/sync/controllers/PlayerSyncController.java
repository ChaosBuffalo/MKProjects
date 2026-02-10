package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.events.PlayerDataEvent;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class PlayerSyncController extends EntitySyncController {
    private static final EnumSet<SyncVisibility> PLAYER_VISIBILITY = EnumSet.of(SyncVisibility.Public, SyncVisibility.Private);
    private final MKPlayerData playerData;
    private final boolean enableTraceLogging = false;
    private boolean readyForUpdates = false;
    private List<Throwable> dirtyLog;

    public PlayerSyncController(MKPlayerData playerData) {
        super(playerData.getEntity());
        this.playerData = playerData;
        enableLogging = true;
    }

    @Override
    public Set<SyncVisibility> supportedVisibilities() {
        return PLAYER_VISIBILITY;
    }

    @Override
    protected void childUpdated() {
        super.childUpdated();
        if (enableTraceLogging) {
            getDirtyLog().add(new Exception().fillInStackTrace());
        }
    }

    @Override
    public void applyRemoteUpdate(SyncContext context, CompoundTag updateTag, SyncVisibility visibility) {
        super.applyRemoteUpdate(context, updateTag, visibility);
        NeoForge.EVENT_BUS.post(new PlayerDataEvent.Updated(playerData));
    }

    @Override
    public boolean syncUpdates() {
        if (!readyForUpdates) {
            return false;
        }
        if (enableTraceLogging) {
            MKCore.LOGGER.info("player {} dirty {}", playerData, getDirtyLog().size());
        }
        boolean updated = super.syncUpdates();
        if (enableTraceLogging && updated) {
            getDirtyLog().clear();
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

    private List<Throwable> getDirtyLog() {
        if (dirtyLog == null) {
            dirtyLog = new ArrayList<>();
        }
        return dirtyLog;
    }
}
