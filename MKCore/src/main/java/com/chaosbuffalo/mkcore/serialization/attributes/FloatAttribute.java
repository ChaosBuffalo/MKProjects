package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class FloatAttribute implements ISerializableAttribute<Float> {
    private final String name;
    private float currentValue;
    private float defaultValue;

    public FloatAttribute(String name, float defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        reset();
    }

    /**
     * @deprecated Use value() instead
     */
    @Deprecated
    @Override
    public Float getValue() {
        return currentValue;
    }

    public float value() {
        return currentValue;
    }

    @Override
    public void setValue(Float newValue) {
        setValue(newValue.floatValue());
    }

    public void setValue(float newValue) {
        currentValue = newValue;
    }

    /**
     * @deprecated Use defaultValue() instead
     */
    @Deprecated
    @Override
    public Float getDefaultValue() {
        return defaultValue;
    }

    public float defaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Float newValue) {
        setDefaultValue(newValue.floatValue());

    }

    public void setDefaultValue(float newValue) {
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
        return ops.createFloat(currentValue);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asFloat(defaultValue));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Float.parseFloat(stringValue));
    }

    @Override
    public String valueAsString() {
        return Float.toString(currentValue);
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty() || string.equals("-");
    }

    @Override
    public boolean validateString(String stringValue) {
        return MathUtils.isNumeric(stringValue) || MathUtils.isNumeric(stringValue + "0");
    }
}
