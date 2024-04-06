package com.chaosbuffalo.mkcore.serialization.components;

import com.chaosbuffalo.mkcore.MKCore;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class MKComponentMap {
    private final DataItem<?>[] items;
    private final Map<String, DataItem<?>> byName = new HashMap<>();
    @Nullable
    private final MKComponentMap parentMap;

    public MKComponentMap(Object owner) {
        this(owner.getClass());
    }

    public MKComponentMap(Class<?> owningClass) {
        parentMap = null;
        items = new DataItem[MKDataComponents.ID_REGISTRY.getCount(owningClass)];
    }

    public MKComponentMap(@Nonnull MKComponentMap parent) {
        this.parentMap = Objects.requireNonNull(parent);
        items = new DataItem[parent.items.length];
    }

    public MKComponentMap derive() {
        return new MKComponentMap(this);
    }

    private <T> DataItem<T> getLocalItem(MKComponentKey<T> key) {
        return (DataItem<T>) items[key.id()];
    }

    public <T> T getComponentValue(MKComponentKey<T> component) {
        DataItem<T> localItem = getLocalItem(component);
        if (localItem != null) {
            return localItem.value;
        }
        if (parentMap != null) {
            return parentMap.getComponentValue(component);
        }
        return null;
    }

    public <T> void setComponent(MKComponentKey<T> component, T value) {
        DataItem<T> localItem = getLocalItem(component);
        if (localItem != null) {
            localItem.set(value);
        } else {
            define(component, value);
        }
    }

    public <T> void define(MKComponentKey<T> component, T defaultValue) {
        DataItem<T> entry = new DataItem<>(component, defaultValue);
        defineEntry(entry);
    }

    public <T> void define(MKComponentKey<T> component, T defaultValue, BiConsumer<T, T> callback) {
        DataItem<T> entry = new ObservableDataItem<>(component, defaultValue, callback);
        defineEntry(entry);
    }

    private <T> void defineEntry(DataItem<T> entry) {
        items[entry.component.id()] = entry;
        byName.put(entry.component.name(), entry);
    }

    static class ObservableDataItem<T> extends DataItem<T> {
        private final BiConsumer<T, T> changeCallback;

        public ObservableDataItem(MKComponentKey<T> component, T value, BiConsumer<T, T> changeCallback) {
            super(component, value);
            this.changeCallback = changeCallback;
        }

        @Override
        protected void set(T newValue) {
            T oldValue = value;
            super.set(newValue);
            if (changeCallback != null) {
                changeCallback.accept(oldValue, newValue);
            }
        }
    }

    static class DataItem<T> {
        private final MKComponentKey<T> component;
        private final T defaultValue;
        protected T value;

        public DataItem(MKComponentKey<T> component, T value) {
            this.component = component;
            this.value = value;
            this.defaultValue = value;
        }

        protected void set(T newValue) {
            value = newValue;
        }

        protected boolean isDefault() {
            return value.equals(defaultValue);
        }

        protected <D> D serialize(DynamicOps<D> ops, boolean useDefault) {
            return component.serializer().serialize(ops, useDefault ? defaultValue : value);
        }

        protected <D> void deserialize(Dynamic<D> dynamic) {
            value = component.serializer().deserialize(dynamic, defaultValue);
        }
    }

    @Nullable
    private DataItem<?> findItemByName(String key) {
        return byName.get(key);
    }

    private <D> void writeItems(DynamicOps<D> ops, BiConsumer<D, D> consumer, boolean includeParent, boolean skipIfDefault, boolean useDefaults) {
        if (includeParent && parentMap != null) {
            parentMap.writeItems(ops, consumer, includeParent, skipIfDefault, useDefaults);
        }
        Arrays.stream(items)
                .filter(item -> !skipIfDefault || !item.isDefault())
                .forEach(item -> {
                    consumer.accept(ops.createString(item.component.name()), item.serialize(ops, useDefaults));
                });
    }

    public <D> D serializeNamedAttributeMap(DynamicOps<D> ops) {
        return serializeNamedAttributeMap(ops, false, true, false);
    }

    public <D> D serializeAllNamedAttributesDatagen(DynamicOps<D> ops) {
        return serializeNamedAttributeMap(ops, true, true, true);
    }

    public <D> D serializeNamedAttributeMap(DynamicOps<D> ops, boolean includeParent, boolean skipIfDefault,
                                            boolean createDefaultEntry) {
        ImmutableMap.Builder<D, D> builder = ImmutableMap.builder();
        if (createDefaultEntry) {
            ImmutableMap.Builder<D, D> defaultBuilder = ImmutableMap.builder();
            writeItems(ops, defaultBuilder::put, includeParent, false, true);
            builder.put(ops.createString("#defaults"), ops.createMap(defaultBuilder.build()));
        }
        writeItems(ops, builder::put, includeParent, skipIfDefault, false);
        return ops.createMap(builder.build());
    }

    public <D> void deserializeNamedAttributeMap(Dynamic<D> dynamic, String field) {
        dynamic.get(field).asMapOpt().result().ifPresent(s -> s.forEach(p -> {
            String key = p.getFirst().asString().getOrThrow(false, MKCore.LOGGER::error);
            DataItem<?> item = findItemByName(key);
            if (item != null) {
                item.deserialize(p.getSecond());
            }
        }));
    }
}
