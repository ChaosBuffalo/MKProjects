package com.chaosbuffalo.mkcore.serialization.attributes;


import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.utils.location.LocationProvider;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class LocationProviderAttribute implements ISerializableAttribute<LocationProvider>{
    private final String name;
    protected LocationProvider currentValue;
    protected LocationProvider defaultValue;

    public LocationProviderAttribute(String name, LocationProvider defaultProvider) {
        this.name = name;
        this.defaultValue = defaultProvider;
        reset();
    }

    @Override
    public LocationProvider getValue() {
        return currentValue;
    }

    @Override
    public void setValue(LocationProvider newValue) {
        currentValue = newValue;
    }

    @Override
    public LocationProvider getDefaultValue() {
        return currentValue;
    }

    @Override
    public void setDefaultValue(LocationProvider newValue) {
        defaultValue = newValue;
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
        return LocationProvider.CODEC.encodeStart(ops, currentValue).getOrThrow(false, MKCore.LOGGER::error);
    }

    @Override
    public <D> void deserialize(Dynamic<D> dynamic) {
        setValue(LocationProvider.CODEC.parse(dynamic).getOrThrow(false, MKCore.LOGGER::error));
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
