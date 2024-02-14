package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.MKPlayerData;
import com.chaosbuffalo.mkcore.core.records.IRecordType;

public abstract class EntitlementType implements IRecordType<EntitlementInstance> {
    @Override
    public abstract EntitlementTypeHandler createTypeHandler(MKPlayerData playerData);
}
