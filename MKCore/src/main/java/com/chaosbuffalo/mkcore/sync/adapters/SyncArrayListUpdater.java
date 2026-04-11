package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SyncArrayListUpdater<T> implements ISyncObject {
    private static final int INVALID_REGISTRY_ID = -1;

    private final List<T> parent;
    private final ElementSerializer<T> serializer;
    private final @Nullable T defaultValue;
    private final BitSet dirtyEntries = new BitSet();
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public interface ElementSerializer<T> {
        Tag encodeValue(SyncContext context, T value);

        T decodeValue(SyncContext context, Tag value);

        ElementSerializer<ResourceLocation> RESOURCE_LOCATION = new ElementSerializer<>() {
            @Override
            public Tag encodeValue(SyncContext context, ResourceLocation value) {
                return StringTag.valueOf(value.toString());
            }

            @Override
            public ResourceLocation decodeValue(SyncContext context, Tag value) {
                return ResourceLocation.tryParse(value.getAsString());
            }
        };

        static <K, V> ElementSerializer<K> registryElements(
                ResourceKey<? extends Registry<V>> registryKey,
                RegistryElementAdapter<K, V> adapter,
                @Nullable K defaultValue) {
            return new ElementSerializer<>() {
                @Override
                public Tag encodeValue(SyncContext context, K value) {
                    if (defaultValue != null && Objects.equals(defaultValue, value)) {
                        return IntTag.valueOf(INVALID_REGISTRY_ID);
                    }

                    Registry<V> registry = context.registryOrThrow(registryKey);
                    V registryValue = adapter.toRegistryValue(registry, value);
                    if (registryValue == null) {
                        MKCore.LOGGER.error("Failed to resolve registry-backed list value {} in {}", value, registryKey.location());
                        return IntTag.valueOf(INVALID_REGISTRY_ID);
                    }

                    int rawId = registry.getId(registryValue);
                    if (rawId < 0) {
                        MKCore.LOGGER.error("Failed to encode registry-backed list value {} in {}", value, registryKey.location());
                        return IntTag.valueOf(INVALID_REGISTRY_ID);
                    }
                    return IntTag.valueOf(rawId);
                }

                @Override
                public K decodeValue(SyncContext context, Tag value) {
                    if (!(value instanceof IntTag intTag)) {
                        MKCore.LOGGER.error("Expected int tag for registry-backed list in {} but found {}", registryKey.location(), value);
                        return defaultValue;
                    }

                    int rawId = intTag.getAsInt();
                    if (rawId == INVALID_REGISTRY_ID) {
                        return defaultValue;
                    }

                    Registry<V> registry = context.registryOrThrow(registryKey);
                    V registryValue = registry.byId(rawId);
                    if (registryValue == null) {
                        MKCore.LOGGER.error("Failed to decode registry-backed list raw id {} in {}", rawId, registryKey.location());
                        return defaultValue;
                    }

                    K decoded = adapter.fromRegistryValue(registry, registryValue);
                    if (decoded == null) {
                        MKCore.LOGGER.error("Failed to convert registry-backed list value {} from {}", registryValue, registryKey.location());
                        return defaultValue;
                    }
                    return decoded;
                }
            };
        }
    }

    public static SyncArrayListUpdater<ResourceLocation> resourceLocations(List<ResourceLocation> list) {
        return new SyncArrayListUpdater<>(list, ElementSerializer.RESOURCE_LOCATION);
    }

    public static SyncArrayListUpdater<ResourceLocation> resourceLocations(List<ResourceLocation> list,
                                                                           @Nullable ResourceLocation defaultValue) {
        return new SyncArrayListUpdater<>(list, ElementSerializer.RESOURCE_LOCATION, defaultValue);
    }

    public static <V> SyncArrayListUpdater<ResourceLocation> registryResourceLocations(
            List<ResourceLocation> list,
            ResourceKey<? extends Registry<V>> registryKey) {
        return registryResourceLocations(list, registryKey, null);
    }

    public static <V> SyncArrayListUpdater<ResourceLocation> registryResourceLocations(
            List<ResourceLocation> list,
            ResourceKey<? extends Registry<V>> registryKey,
            @Nullable ResourceLocation defaultValue) {
        return new SyncArrayListUpdater<>(
                list,
                ElementSerializer.registryElements(registryKey, RegistryElementAdapter.resourceLocations(), defaultValue),
                defaultValue
        );
    }

    public static <V> SyncArrayListUpdater<ResourceKey<V>> registryResourceKeys(
            List<ResourceKey<V>> list,
            ResourceKey<? extends Registry<V>> registryKey) {
        return registryResourceKeys(list, registryKey, null);
    }

    public static <V> SyncArrayListUpdater<ResourceKey<V>> registryResourceKeys(
            List<ResourceKey<V>> list,
            ResourceKey<? extends Registry<V>> registryKey,
            @Nullable ResourceKey<V> defaultValue) {
        return new SyncArrayListUpdater<>(
                list,
                ElementSerializer.registryElements(registryKey, RegistryElementAdapter.resourceKeys(), defaultValue),
                defaultValue
        );
    }

    public SyncArrayListUpdater(List<T> list, ElementSerializer<T> elementSerializer) {
        this(list, elementSerializer, null);
    }

    public SyncArrayListUpdater(List<T> list, ElementSerializer<T> elementSerializer, @Nullable T defaultValue) {
        this.parent = list;
        this.serializer = elementSerializer;
        this.defaultValue = defaultValue;
    }

    public void setDirty(int index) {
        dirtyEntries.set(index);
        parentNotifier.notifyUpdate();
    }

    @Override
    public void setSyncUpdateNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirtyEntries.isEmpty();
    }

    @Override
    public void clearDirty() {
        dirtyEntries.clear();
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        if (parent.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        if (hasDefaultValue()) {
            root.putInt("n", parent.size());
            ListTag sparseList = new ListTag();
            for (int i = 0; i < parent.size(); i++) {
                T value = parent.get(i);
                if (isDefaultValue(value)) {
                    continue;
                }
                CompoundTag tag = new CompoundTag();
                tag.putInt("i", i);
                tag.put("v", serializer.encodeValue(context, value));
                sparseList.add(tag);
            }
            root.put("s", sparseList);
        } else {
            ListTag list = new ListTag();
            parent.forEach(r -> list.add(serializer.encodeValue(context, r)));
            root.put("l", list);
        }
        return root;
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        if (dirtyEntries.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        ListTag list = new ListTag();
        dirtyEntries.stream().forEach(i -> {
            CompoundTag tag = new CompoundTag();
            tag.putInt("i", i);
            tag.put("v", serializer.encodeValue(context, parent.get(i)));
            list.add(tag);
        });
        root.put("s", list);
        dirtyEntries.clear();
        return root;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof CompoundTag root) {
            if (root.getBoolean("f")) {
                resetValues(root.contains("n") ? root.getInt("n") : parent.size());
            }

            if (root.contains("s")) {
                var sparseList = root.getList("s", Tag.TAG_COMPOUND);
                for (int i = 0; i < sparseList.size(); i++) {
                    CompoundTag entry = sparseList.getCompound(i);
                    int index = entry.getInt("i");
                    setValueInternal(context, index, entry.get("v"));
                }
            } else if (root.contains("l")) {
                if (root.get("l") instanceof ListTag fullList) {
                    for (int i = 0; i < fullList.size(); i++) {
                        setValueInternal(context, i, fullList.get(i));
                    }
                }
            }
        }
    }

    private void setValueInternal(SyncContext context, int index, Tag encodedValue) {
        T decoded = serializer.decodeValue(context, encodedValue);
        if (decoded != null) {
            if (index < parent.size()) {
                parent.set(index, decoded);
            } else {
                MKCore.LOGGER.error("Failed set update item: Index {} out of range ({} max)", index, parent.size());
            }
        } else {
            MKCore.LOGGER.error("Failed to decode list entry {}: {}", index, encodedValue);
        }
    }

    private boolean hasDefaultValue() {
        return defaultValue != null;
    }

    private boolean isDefaultValue(@Nullable T value) {
        return hasDefaultValue() && Objects.equals(defaultValue, value);
    }

    private void resetValues(int targetSize) {
        if (hasDefaultValue()) {
            if (parent.size() != targetSize) {
                MKCore.LOGGER.warn("Default-backed sync list size mismatch. Local: {}, Remote: {}",
                        parent.size(), targetSize);
            }
            Collections.fill(parent, defaultValue);
        } else {
            parent.clear();
        }
    }
}
