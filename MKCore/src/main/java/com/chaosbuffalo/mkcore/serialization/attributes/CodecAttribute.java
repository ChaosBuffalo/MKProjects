package com.chaosbuffalo.mkcore.serialization.attributes;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class CodecAttribute<T> implements ISerializableAttribute<T> {
    private final String name;
    protected T currentValue;
    protected T defaultValue;
    protected final Codec<T> codec;

    public CodecAttribute(String name, T defaultValue, Codec<T> codec) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.codec = codec;
        reset();
    }

    @Override
    public T getValue() {
        return currentValue;
    }

    @Override
    public void setValue(T newValue) {
        currentValue = newValue;
    }

    @Override
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void reset() {
        currentValue = defaultValue;
    }


    @Override
    public <D> D serialize(DynamicOps<D> ops) {
        return codec.encodeStart(ops, currentValue).getOrThrow(false, MKCore.LOGGER::error);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(codec.parse(dynamic).getOrThrow(false, MKCore.LOGGER::error));
    }

    @Override
    public void setValueFromString(String stringValue) {

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
        return currentValue.toString();
    }
}
