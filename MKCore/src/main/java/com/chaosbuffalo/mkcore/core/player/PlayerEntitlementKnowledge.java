package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entity.EntityEntitlementsKnowledge;
import com.chaosbuffalo.mkcore.core.records.PlayerRecordDispatcher;

public class PlayerEntitlementKnowledge extends EntityEntitlementsKnowledge {

    private final PlayerRecordDispatcher dispatcher;

    public PlayerEntitlementKnowledge(MKPlayerData entityData) {
        super(entityData);
        dispatcher = new PlayerRecordDispatcher(entityData, this::getInstanceStream);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    @Override
    protected void broadcastChange(EntitlementInstance instance) {
        super.broadcastChange(instance);
        dispatcher.onRecordUpdated(instance);
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerEntitlementKnowledge.onPersonaActivated");
        dispatcher.onPersonaActivated();
        broadcastLoaded();
    }

    public void onPersonaDeactivated() {
        MKCore.LOGGER.debug("PlayerEntitlementKnowledge.onPersonaDeactivated");
        dispatcher.onPersonaDeactivated();
    }

    public void onJoinWorld() {
        MKCore.LOGGER.debug("PlayerEntitlementKnowledge.onJoinWorld");
        dispatcher.onJoinWorld();
    }
}
