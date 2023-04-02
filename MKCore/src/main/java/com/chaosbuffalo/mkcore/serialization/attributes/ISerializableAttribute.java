package com.chaosbuffalo.mkcore.serialization.attributes;


import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public interface ISerializableAttribute<T> {
    T getValue();

    void setValue(T newValue);

    T getDefaultValue();

    void setDefaultValue(T newValue);

    default void reset() {
        setValue(getDefaultValue());
    }

    default boolean isDefaultValue() {
        return getValue().equals(getDefaultValue());
    }

    String getName();

    <D> D serialize(DynamicOps<D> ops);

    <D> void deserialize(Dynamic<D> dynamic);

    void setValueFromString(String stringValue);

    boolean validateString(String stringValue);

    boolean isEmptyStringInput(String string);

    String valueAsString();
}
