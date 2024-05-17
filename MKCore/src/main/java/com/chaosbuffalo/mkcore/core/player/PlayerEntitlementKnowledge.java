package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entity.EntityEntitlementsKnowledge;
import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.PlayerRecordDispatcher;

public class PlayerEntitlementKnowledge extends EntityEntitlementsKnowledge {

    private final PlayerRecordDispatcher<EntitlementInstance> dispatcher;

    public PlayerEntitlementKnowledge(Persona persona) {
        super(persona.getPlayerData());
        dispatcher = new PlayerRecordDispatcher<>(persona, this::getInstanceStream);
    }

    private MKPlayerData getPlayerData() {
        return (MKPlayerData) entityData;
    }

    @Override
    protected void onInstanceChanged(EntitlementInstance instance) {
        super.onInstanceChanged(instance);
        dispatcher.onRecordUpdated(instance);
    }

    public void onPersonaActivated() {
        MKCore.LOGGER.debug("PlayerEntitlementKnowledge.onPersonaActivated");
        dispatcher.onPersonaActivated();
    }
}
