package com.chaosbuffalo.mkcore.abilities2.definition;

public enum AbilityValueKind {
    FLOAT,
    INT,
    BOOL,
    ENTITY_REF,
    RESOURCE_LOCATION;

    public boolean isNumeric() {
        return this == FLOAT || this == INT;
    }
}
