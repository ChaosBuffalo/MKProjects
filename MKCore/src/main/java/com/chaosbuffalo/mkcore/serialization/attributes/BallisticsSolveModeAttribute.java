package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.abilities.ProjectileAbility;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class BallisticsSolveModeAttribute extends SimpleAttribute<ProjectileAbility.BallisticsSolveMode> {

    public BallisticsSolveModeAttribute(String name, ProjectileAbility.BallisticsSolveMode defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createInt(getValue().ordinal());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(ProjectileAbility.BallisticsSolveMode.values()[dynamic.asInt(0)]);
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(ProjectileAbility.BallisticsSolveMode.valueOf(stringValue));
    }

    @Override
    public boolean validateString(String stringValue) {
        return false;
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return false;
    }

    @Override
    public String valueAsString() {
        return getValue().toString();
    }
}
