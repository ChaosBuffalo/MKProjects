package com.chaosbuffalo.mkcore.core;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.player.PlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public final class MKServerPlayerData extends MKPlayerData {
    public MKServerPlayerData(ServerPlayer playerEntity) {
        super(playerEntity);
    }

    @NotNull
    @Override
    public ServerPlayer getEntity() {
        return (ServerPlayer) super.getEntity();
    }

    @Override
    public void onJoinWorld() {
        super.onJoinWorld();
        events().trigger(PlayerEvents.SERVER_JOIN_WORLD, new PlayerEvents.JoinWorldServerEvent(this));
        initialSync();
    }

    @Override
    public void update() {
        super.update();
        syncState();
    }

    private void syncState() {
        syncController.syncUpdates();
    }

    public void initialSync() {
        MKCore.LOGGER.debug("Sending initial sync for {}", player);
        syncController.sendFullSync(getEntity());
    }
}
