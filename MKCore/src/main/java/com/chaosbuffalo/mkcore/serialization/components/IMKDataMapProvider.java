package com.chaosbuffalo.mkcore.serialization.components;

public interface IMKDataMapProvider {
    MKComponentMap getComponentMap();

    default <T> T getComponentValue(MKComponentKey<T> component) {
        return getComponentMap().getComponentValue(component);
    }

    default <T> void setComponent(MKComponentKey<T> component, T value) {
        getComponentMap().setComponent(component, value);
    }
}
