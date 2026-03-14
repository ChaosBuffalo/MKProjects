package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.mojang.serialization.MapCodec;

public class SimpleEntitlementType extends EntitlementType<SimpleEntitlement> {
    @Override
    public MapCodec<SimpleEntitlement> codec() {
        return SimpleEntitlement.MAP_CODEC;
    }

    @Override
    public EntitlementTypeHandler createTypeHandler(Persona persona) {
        return EntitlementTypeHandler.NONE;
    }
}
