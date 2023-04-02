package com.chaosbuffalo.mkcore.serialization.attributes;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class BooleanAttribute implements ISerializableAttribute<Boolean> {
    private final String name;
    private boolean currentValue;
    private boolean defaultValue;

    public BooleanAttribute(String name, boolean defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        reset();
    }

    /**
     * @deprecated Use value() instead
     */
    @Deprecated
    @Override
    public Boolean getValue() {
        return currentValue;
    }

    public boolean value() {
        return currentValue;
    }

    @Override
    public void setValue(Boolean newValue) {
        setValue(newValue.booleanValue());
    }

    public void setValue(boolean newValue) {
        currentValue = newValue;
    }

    /**
     * @deprecated Use defaultValue() instead
     */
    @Deprecated
    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean defaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Boolean newValue) {
        setDefaultValue(newValue.booleanValue());
    }

    public void setDefaultValue(boolean newValue) {
        defaultValue = newValue;
        reset();
    }

    @Override
    public void reset() {
        currentValue = defaultValue;
    }

    @Override
    public boolean isDefaultValue() {
        return currentValue == defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createBoolean(currentValue);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asBoolean(defaultValue));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Boolean.parseBoolean(stringValue));
    }

    @Override
    public boolean validateString(String stringValue) {
        return "true".contains(stringValue) || "false".contains(stringValue);
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return !(string.equals("true") || string.equals("false"));
    }

    @Override
    public String valueAsString() {
        return Boolean.toString(currentValue);
    }
}
