package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.mojang.serialization.MapCodec;

public class ArmorClassMasteryEntitlementType extends EntitlementType<ArmorClassMasteryEntitlement> {
    @Override
    public MapCodec<ArmorClassMasteryEntitlement> codec() {
        return ArmorClassMasteryEntitlement.MAP_CODEC;
    }

    @Override
    public EntitlementTypeHandler createTypeHandler(Persona persona) {
        return new ArmorClassMasteryEntitlement.ArmorClassMasteryHandler(persona);
    }
}
