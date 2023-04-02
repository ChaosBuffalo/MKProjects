package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.records.IRecordType;

public class SimpleEntitlement extends MKEntitlement {
    private final IRecordType<SimpleEntitlementHandler> recordType;

    public SimpleEntitlement(int maxEntitlements) {
        super(maxEntitlements);
        recordType = playerData -> new SimpleEntitlementHandler();
    }

    @Override
    public IRecordType<?> getRecordType() {
        return recordType;
    }

    public static class SimpleEntitlementHandler extends EntitlementTypeHandler {

    }
}
