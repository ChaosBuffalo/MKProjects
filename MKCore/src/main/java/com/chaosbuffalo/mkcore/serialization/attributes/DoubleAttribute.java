package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class DoubleAttribute implements ISerializableAttribute<Double> {
    private final String name;
    private double currentValue;
    private double defaultValue;

    public DoubleAttribute(String name, double defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        reset();
    }

    /**
     * @deprecated Use value() instead
     */
    @Deprecated
    @Override
    public Double getValue() {
        return currentValue;
    }

    public double value() {
        return currentValue;
    }

    @Override
    public void setValue(Double newValue) {
        setValue(newValue.doubleValue());
    }

    public void setValue(double newValue) {
        currentValue = newValue;
    }

    /**
     * @deprecated Use defaultValue() instead
     */
    @Deprecated
    @Override
    public Double getDefaultValue() {
        return defaultValue;
    }

    public double defaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Double newValue) {
        setDefaultValue(newValue.doubleValue());

    }

    public void setDefaultValue(double newValue) {
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
        return ops.createDouble(currentValue);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asDouble(defaultValue));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Double.parseDouble(stringValue));
    }

    @Override
    public boolean validateString(String stringValue) {
        return MathUtils.isNumeric(stringValue) || MathUtils.isNumeric(stringValue + "0");
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty() || string.equals("-");
    }

    @Override
    public String valueAsString() {
        return Double.toString(currentValue);
    }
}
