package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.player.AbilityGroupId;

public class AbilitySlotEntitlement extends MKEntitlement {

    public static final EntitlementType ABILITY_SLOT = new EntitlementType() {
        @Override
        public EntitlementTypeHandler createTypeHandler(Persona persona) {
            return new AbilitySlotEntitlement.AbilitySlotEntitlementHandler(persona);
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
        private final Persona persona;

        public AbilitySlotEntitlementHandler(Persona persona) {
            this.persona = persona;
        }

        private void recalculateSlots(AbilitySlotEntitlement entitlement) {
            int count = persona.getEntitlements().getEntitlementLevel(entitlement);
            persona.getLoadout()
                    .getAbilityGroup(entitlement.getGroup())
                    .setBonusSlots(count);
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            if (record.entitlement() instanceof AbilitySlotEntitlement slotEntitlement) {
                recalculateSlots(slotEntitlement);
            }
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
