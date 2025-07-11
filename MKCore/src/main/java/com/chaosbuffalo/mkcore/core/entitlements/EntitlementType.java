package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.chaosbuffalo.mkcore.core.records.IRecordType;
import com.mojang.serialization.MapCodec;

public abstract class EntitlementType<T extends MKEntitlement> implements IRecordType<EntitlementInstance> {

    public abstract MapCodec<T> codec();

    @Override
    public abstract EntitlementTypeHandler createTypeHandler(Persona persona);
}
