package com.chaosbuffalo.mkcore.sync.controllers;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.events.PlayerDataEvent;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.controllers.EntitySyncController;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;

import java.util.EnumSet;
import java.util.Set;

public class PlayerSyncController extends EntitySyncController {
    private static final EnumSet<SyncVisibility> PLAYER_VISIBILITY = EnumSet.of(SyncVisibility.Public, SyncVisibility.Private);
    private final MKPlayerData playerData;
    protected boolean readyForUpdates = false;

    public PlayerSyncController(MKPlayerData playerData) {
        super(playerData.getEntity());
        this.playerData = playerData;
    }

    @Override
    protected Set<SyncVisibility> supportedVisibilities() {
        return PLAYER_VISIBILITY;
    }

    @Override
    protected boolean updatesBlocked() {
        return super.updatesBlocked() || !readyForUpdates;
    }

    @Override
    public void deserializeUpdate(CompoundTag updateTag, Set<SyncVisibility> visibility) {
        super.deserializeUpdate(updateTag, visibility);
        MinecraftForge.EVENT_BUS.post(new PlayerDataEvent.Updated(playerData));
    }

    @Override
    public void sendFullSync(ServerPlayer otherPlayer) {
        super.sendFullSync(otherPlayer);
        if (entity == otherPlayer) {
            readyForUpdates = true;
        }
    }
}
