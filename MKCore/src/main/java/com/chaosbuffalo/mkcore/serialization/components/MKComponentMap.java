package com.chaosbuffalo.mkcore.serialization.components;

import com.chaosbuffalo.mkcore.MKCore;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MKComponentMap {
    private final Map<MKComponentKey<?>, DataItem<?>> components = new HashMap<>(); // list or array
    private final Map<String, DataItem<?>> byName = new HashMap<>();
    @Nullable
    private final MKComponentMap parentMap;

    public MKComponentMap() {
        parentMap = null;
    }

    public MKComponentMap(MKComponentMap parent) {
        this.parentMap = parent;
    }

    public MKComponentMap derive() {
        return new MKComponentMap(this);
    }

    private <T> DataItem<T> getPrivateItem(MKComponentKey<T> key) {
        return (DataItem<T>) components.get(key);
    }


    public <T> T getComponentValue(MKComponentKey<T> component) {
        DataItem<T> localItem = getPrivateItem(component);
        if (localItem != null) {
            return localItem.value;
        }
        if (parentMap != null) {
            return parentMap.getComponentValue(component);
        }
        return null;
    }

    public <T> void setComponent(MKComponentKey<T> component, T value) {
        DataItem<T> localItem = getPrivateItem(component);
        if (localItem != null) {
            localItem.set(value);
        } else {
            define(component, value);
        }
    }

    public <T> boolean isValueDefault(MKComponentKey<T> key) {
        var item = getPrivateItem(key);
        if (item != null) {
            return item.value.equals(item.defaultValue);
        }
        return false;
    }

    public <T> void define(MKComponentKey<T> component, T defaultValue) {
        DataItem<T> entry = new DataItem<>(component, defaultValue);
        components.put(component, entry);
        byName.put(component.name(), entry);
    }

    static class DataItem<T> {
        final MKComponentKey<T> component;
        private T value;
        final T defaultValue;

        public DataItem(MKComponentKey<T> component, T value) {
            this.component = component;
            this.value = value;
            this.defaultValue = value;
        }

        private void set(T newValue) {
            value = newValue;
        }

        <D> D serialize(DynamicOps<D> ops) {
            return component.serializer().serialize(ops, value);
        }

        <D> void deserialize(Dynamic<D> dynamic) {
            value = component.serializer().deserialize(dynamic, defaultValue);
        }
    }

    public <D> D serializeNamedAttributeMap(DynamicOps<D> ops) {
        return ops.createMap(components.values().stream()
                .map(attr -> Pair.of(ops.createString(attr.component.name()), attr.serialize(ops)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }

    public <D> void deserializeNamedAttributeMap(Dynamic<D> dynamic, String field) {
        Map<String, Dynamic<D>> map1 = dynamic.get(field).asMap(
                dk -> dk.asString().getOrThrow(false, MKCore.LOGGER::error),
                Function.identity());
        map1.forEach((k, v) -> {
            var item = byName.get(k);
            if (item != null) {
                item.deserialize(v);
            }
        });
    }
}
