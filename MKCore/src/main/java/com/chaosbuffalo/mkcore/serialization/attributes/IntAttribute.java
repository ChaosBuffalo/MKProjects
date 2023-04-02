package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.utils.MathUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import java.util.function.Consumer;

public class IntAttribute implements ISerializableAttribute<Integer> {
    private final String name;
    private int currentValue;
    private int defaultValue;
    private Consumer<ISerializableAttribute<Integer>> valueChanged;

    public IntAttribute(String name, int defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        reset();
        valueChanged = null;
    }

    /**
     * @deprecated Use value() instead
     */
    @Deprecated
    @Override
    public Integer getValue() {
        return currentValue;
    }

    public int value() {
        return currentValue;
    }

    @Override
    public void setValue(Integer newValue) {
        setValue(newValue.intValue());
    }

    public void setValue(int newValue) {
        currentValue = newValue;
        if (valueChanged != null) {
            valueChanged.accept(this);
        }
    }

    /**
     * @deprecated Use defaultValue() instead
     */
    @Deprecated
    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

    public int defaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Integer newValue) {
        setDefaultValue(newValue.intValue());

    }

    public void setDefaultValue(int newValue) {
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

    public void setValueSetCallback(Consumer<ISerializableAttribute<Integer>> onSetCallback) {
        this.valueChanged = onSetCallback;
    }

    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return ops.createInt(currentValue);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(dynamic.asInt(defaultValue));
    }

    @Override
    public void setValueFromString(String stringValue) {
        setValue(Integer.parseInt(stringValue));
    }

    @Override
    public String valueAsString() {
        return Integer.toString(currentValue);
    }

    @Override
    public boolean isEmptyStringInput(String string) {
        return string.isEmpty() || string.equals("-");
    }

    @Override
    public boolean validateString(String stringValue) {
        return MathUtils.isInteger(stringValue) || MathUtils.isInteger(stringValue + "0");
    }
}
