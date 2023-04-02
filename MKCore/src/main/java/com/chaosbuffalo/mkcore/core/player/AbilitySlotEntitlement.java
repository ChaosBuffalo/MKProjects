package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.core.AbilityGroupId;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementTypeHandler;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.records.IRecordType;

public class AbilitySlotEntitlement extends MKEntitlement {
    private final AbilityGroupId group;
    private final IRecordType<AbilitySlotEntitlementHandler> recordType;

    public AbilitySlotEntitlement(AbilityGroupId group) {
        this(group, group.getMaxSlots());
    }

    public AbilitySlotEntitlement(AbilityGroupId group, int maxEntitlements) {
        super(maxEntitlements);
        this.group = group;
        recordType = playerData -> new AbilitySlotEntitlementHandler(playerData, this);
    }

    @Override
    public IRecordType<?> getRecordType() {
        return recordType;
    }

    public static class AbilitySlotEntitlementHandler extends EntitlementTypeHandler {
        private final MKPlayerData playerData;
        private final AbilitySlotEntitlement entitlement;

        public AbilitySlotEntitlementHandler(MKPlayerData playerData, AbilitySlotEntitlement entitlement) {
            this.playerData = playerData;
            this.entitlement = entitlement;
        }

        private void recalculateSlots() {
            int count = playerData.getEntitlements().getEntitlementLevel(entitlement);
            playerData.getLoadout()
                    .getAbilityGroup(entitlement.group)
                    .setSlots(entitlement.group.getDefaultSlots() + count);
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            recalculateSlots();
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            recalculateSlots();
        }
    }
}
