package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.persona.Persona;

public class AbilityPoolEntitlement extends MKEntitlement {

    public static final EntitlementType ABILITY_POOL_SLOT = new EntitlementType() {
        @Override
        public EntitlementTypeHandler createTypeHandler(Persona persona) {
            return new AbilityPoolEntitlement.AbilityPoolEntitlementHandler(persona);
        }
    };

    public AbilityPoolEntitlement(int maxEntitlements) {
        super(maxEntitlements);
    }

    @Override
    public EntitlementType getEntitlementType() {
        return ABILITY_POOL_SLOT;
    }

    public static class AbilityPoolEntitlementHandler extends EntitlementTypeHandler {
        private final Persona persona;

        public AbilityPoolEntitlementHandler(Persona persona) {
            this.persona = persona;
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            int count = persona.getEntitlements().getEntitlementLevel(record.entitlement());
            persona.getAbilities().setAbilityPoolSize(count + GameConstants.DEFAULT_ABILITY_POOL_SIZE);
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
