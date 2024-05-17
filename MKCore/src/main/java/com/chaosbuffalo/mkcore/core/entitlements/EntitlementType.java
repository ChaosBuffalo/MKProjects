package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.IRecordType;

public abstract class EntitlementType implements IRecordType<EntitlementInstance> {
    @Override
    public abstract EntitlementTypeHandler createTypeHandler(Persona persona);
}
