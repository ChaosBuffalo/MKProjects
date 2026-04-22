package com.chaosbuffalo.mkcore.abilities2.definition;

import java.util.Locale;

public enum AbilityTargetRelation {
    ALL,
    FRIENDLY,
    ENEMY;

    private final String serializedName = name().toLowerCase(Locale.ROOT);

    public String serializedName() {
        return serializedName;
    }

    public static AbilityTargetRelation fromSerializedName(String name) {
        for (AbilityTargetRelation value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown ability target relation '" + name + "'");
    }
}
