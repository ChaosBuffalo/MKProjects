package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;

public class AbilitySlotEntitlement extends MKEntitlement {

    public static final EntitlementType ABILITY_SLOT = new EntitlementType() {
        @Override
        public EntitlementTypeHandler createTypeHandler(MKPlayerData playerData) {
            return new AbilitySlotEntitlement.AbilitySlotEntitlementHandler(playerData);
        }
    };

    private final AbilityGroupId group;

    public AbilitySlotEntitlement(AbilityGroupId group) {
        this(group, group.getMaxSlots());
    }

    public AbilitySlotEntitlement(AbilityGroupId group, int maxEntitlements) {
        super(maxEntitlements);
        this.group = group;
    }

    public AbilityGroupId getGroup() {
        return group;
    }

    @Override
    public EntitlementType getEntitlementType() {
        return ABILITY_SLOT;
    }

    public static class AbilitySlotEntitlementHandler extends EntitlementTypeHandler {
        private final MKPlayerData playerData;

        public AbilitySlotEntitlementHandler(MKPlayerData playerData) {
            this.playerData = playerData;
        }

        private void recalculateSlots(AbilitySlotEntitlement entitlement) {
            int count = playerData.getEntitlements().getEntitlementLevel(entitlement);
            playerData.getLoadout()
                    .getAbilityGroup(entitlement.getGroup())
                    .setSlots(entitlement.getGroup().getDefaultSlots() + count);
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            if (record.getEntitlement() instanceof AbilitySlotEntitlement slotEntitlement) {
                recalculateSlots(slotEntitlement);
            }
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
