package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.MKPlayerData;

public class SimpleEntitlement extends MKEntitlement {

    public SimpleEntitlement(int maxEntitlements) {
        super(maxEntitlements);
    }

    @Override
    public EntitlementTypeHandler createTypeHandler(MKPlayerData playerData) {
        return new SimpleEntitlementHandler();
    }

    public static class SimpleEntitlementHandler extends EntitlementTypeHandler {

    }
}
