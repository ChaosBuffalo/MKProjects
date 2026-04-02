package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.records.IRecordTypeHandler;

public interface EntitlementTypeHandler extends IRecordTypeHandler<EntitlementInstance> {

    EntitlementTypeHandler NONE = new EntitlementTypeHandler() {

    };
}
