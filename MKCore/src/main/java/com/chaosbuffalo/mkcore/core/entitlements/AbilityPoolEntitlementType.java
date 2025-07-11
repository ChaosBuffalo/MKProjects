package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.mojang.serialization.MapCodec;

public class AbilityPoolEntitlementType extends EntitlementType<AbilityPoolEntitlement> {

    @Override
    public MapCodec<AbilityPoolEntitlement> codec() {
        return AbilityPoolEntitlement.MAP_CODEC;
    }

    @Override
    public EntitlementTypeHandler createTypeHandler(Persona persona) {
        return new AbilityPoolEntitlement.AbilityPoolEntitlementHandler(persona);
    }
}
