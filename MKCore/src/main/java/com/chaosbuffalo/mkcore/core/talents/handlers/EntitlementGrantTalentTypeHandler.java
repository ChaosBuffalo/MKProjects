package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;

public class EntitlementGrantTalentTypeHandler extends TalentTypeHandler {

    public EntitlementGrantTalentTypeHandler(Persona persona) {
        super(persona);
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode() instanceof EntitlementGrantTalentNode slotNode) {
            if (record.isKnown()) {
                persona.getEntitlements().addEntitlement(slotNode.createInstance());
            } else {
                persona.getEntitlements().removeEntitlement(slotNode.getNodeId());
            }
        }
    }

    @Override
    public void onRecordLoaded(TalentRecord record) {
        onRecordUpdated(record);
    }
}
