package com.chaosbuffalo.mkcore.core.entitlements;

import com.chaosbuffalo.mkcore.core.persona.Persona;

public class SimpleEntitlement extends MKEntitlement {

    public static final EntitlementType SIMPLE = new EntitlementType() {
        @Override
        public EntitlementTypeHandler createTypeHandler(Persona persona) {
            return new SimpleEntitlement.SimpleEntitlementHandler();
        }
    };

    public SimpleEntitlement(int maxEntitlements) {
        super(maxEntitlements);
    }

    @Override
    public EntitlementType getEntitlementType() {
        return SIMPLE;
    }

    public static class SimpleEntitlementHandler extends EntitlementTypeHandler {

    }
}
