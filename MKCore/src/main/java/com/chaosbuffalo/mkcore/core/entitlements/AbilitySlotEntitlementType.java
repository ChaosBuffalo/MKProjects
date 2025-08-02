package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;
import com.mojang.serialization.MapCodec;

public class AbilitySlotEntitlementType extends EntitlementType<AbilitySlotEntitlement> {

    @Override
    public MapCodec<AbilitySlotEntitlement> codec() {
        return AbilitySlotEntitlement.MAP_CODEC;
    }

    @Override
    public EntitlementTypeHandler createTypeHandler(Persona persona) {
        return new AbilitySlotEntitlement.AbilitySlotEntitlementHandler(persona);
    }
}
