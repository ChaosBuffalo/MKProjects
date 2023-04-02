package com.chaosbuffalo.mkcore.core.player;

import com.chaosbuffalo.mkcore.GameConstants;
import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementInstance;
import com.chaosbuffalo.mkcore.core.entitlements.EntitlementTypeHandler;
import com.chaosbuffalo.mkcore.core.entitlements.MKEntitlement;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import net.minecraft.resources.ResourceLocation;

public class AbilityPoolEntitlement extends MKEntitlement {
    private final IRecordType<AbilityPoolEntitlementHandler> recordType;

    public AbilityPoolEntitlement(int maxEntitlements) {
        super(maxEntitlements);
        recordType = playerData -> new AbilityPoolEntitlementHandler(playerData, this);
    }

    @Override
    public IRecordType<?> getRecordType() {
        return recordType;
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
