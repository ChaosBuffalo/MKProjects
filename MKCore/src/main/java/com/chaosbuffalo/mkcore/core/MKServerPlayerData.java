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
        combatExtensionModule.serverInit();
        events().trigger(PlayerEvents.SERVER_JOIN_LEVEL, new PlayerEvents.JoinLevelServerEvent(this));
        initialSync();
    }

    @Override
    public void update() {
        super.update();
        attributeMonitor.syncUpdates();
        syncController.syncUpdates();
    }

    public void initialSync() {
        MKCore.LOGGER.debug("Sending initial sync for {}", player);
        attributeMonitor.syncInitial();
        syncController.onJoinLevel();
        syncController.sendFullSync(getEntity());
        getEffects().sendAllEffectsToPlayer(getEntity());
    }
}
