package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementTypeHandler;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;

public class AbilityPoolEntitlement extends MKEntitlement {

    public AbilityPoolEntitlement(int maxEntitlements) {
        super(maxEntitlements);
    }

    @Override
    public EntitlementTypeHandler createTypeHandler(MKPlayerData playerData) {
        return new AbilityPoolEntitlementHandler(playerData, this);
    }

    public static class AbilityPoolEntitlementHandler extends EntitlementTypeHandler {
        private final MKPlayerData playerData;
        private final AbilityPoolEntitlement entitlement;

        public AbilityPoolEntitlementHandler(MKPlayerData playerData, AbilityPoolEntitlement entitlement) {
            this.playerData = playerData;
            this.entitlement = entitlement;
        }

        @Override
        public void onRecordUpdated(EntitlementInstance record) {
            int count = playerData.getEntitlements().getEntitlementLevel(entitlement);
            playerData.getAbilities().setAbilityPoolSize(count + GameConstants.DEFAULT_ABILITY_POOL_SIZE);
        }

        @Override
        public void onRecordLoaded(EntitlementInstance record) {
            onRecordUpdated(record);
        }
    }
}
