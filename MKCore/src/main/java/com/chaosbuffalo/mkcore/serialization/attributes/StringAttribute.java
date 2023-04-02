package com.chaosbuffalo.mkcore.serialization.attributes;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class StringAttribute extends SimpleAttribute<String> {

    public StringAttribute(String name, String defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createString(getValue());
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asString(getDefaultValue()));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(stringValue);
    }

    @Override
    public String valueAsString() {
        return getValue();
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty();
    }

    @Override
    public boolean validateString(String stringValue) {
        return true;
    }
}
