package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import net.minecraft.core.Holder;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EntitlementGrantTalentTypeHandler extends TalentTypeHandler {
    @Nonnull
    private final Holder<MKEntitlement> entitlement;

    public EntitlementGrantTalentTypeHandler(MKPlayerData playerData, Holder<MKEntitlement> entitlement) {
        super(playerData);
        this.entitlement = Objects.requireNonNull(entitlement, "Must provide a valid MKEntitlement instance");
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode() instanceof EntitlementGrantTalentNode slotNode) {
            if (record.isKnown()) {
                playerData.getEntitlements().addEntitlement(new EntitlementInstance(entitlement.value(), slotNode.getNodeId()));
            } else {
                playerData.getEntitlements().removeEntitlement(slotNode.getNodeId());
            }
        }
    }
}
