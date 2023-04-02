package com.chaosbuffalo.mkcore.core.talents.handlers;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.talents.TalentRecord;
import com.chaosbuffalo.mkcore.core.talents.TalentTypeHandler;
import com.chaosbuffalo.mkcore.core.talents.nodes.EntitlementGrantTalentNode;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Objects;

public class EntitlementGrantTalentTypeHandler extends TalentTypeHandler {
    @Nonnull
    private final RegistryObject<? extends MKEntitlement> entitlement;

    public EntitlementGrantTalentTypeHandler(MKPlayerData playerData, RegistryObject<? extends MKEntitlement> entitlement) {
        super(playerData);
        this.entitlement = Objects.requireNonNull(entitlement, "Must provide a valid MKEntitlement instance");
    }

    @Override
    public void onRecordUpdated(TalentRecord record) {
        if (record.getNode() instanceof EntitlementGrantTalentNode slotNode) {
            if (record.isKnown()) {
                playerData.getEntitlements().addEntitlement(new EntitlementInstance(entitlement.get(), slotNode.getNodeId()));
            } else {
                playerData.getEntitlements().removeEntitlementByUUID(slotNode.getNodeId());
            }
        }
    }
}
