package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKPlayerData;

public class AbilityPoolEntitlement extends MKEntitlement {

    public static final EntitlementType ABILITY_POOL_SLOT = new EntitlementType() {
        @Override
        public EntitlementTypeHandler createTypeHandler(MKPlayerData playerData) {
            return new AbilityPoolEntitlement.AbilityPoolEntitlementHandler(playerData);
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
        private final MKPlayerData playerData;

        public AbilityPoolEntitlementHandler(MKPlayerData playerData) {
            this.playerData = playerData;
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            int count = playerData.getEntitlements().getEntitlementLevel(record.getEntitlement());
            playerData.getAbilities().setAbilityPoolSize(count + GameConstants.DEFAULT_ABILITY_POOL_SIZE);
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
