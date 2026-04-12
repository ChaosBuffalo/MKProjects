package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class SyncStoredMap<K, V extends IMKSerializable<CompoundTag>> implements ISyncObject {
    private final Map<K, V> backingMap;
    private final SyncMapUpdater<K, V> syncUpdater;
    private final MapStorageCodec<K, V> storageCodec;

    public SyncStoredMap(Map<K, V> backingMap,
                         SyncMapUpdater.KeyCodec<K> syncKeyCodec,
                         MapStorageCodec.StorageKeyCodec<K> storageKeyCodec,
                         Function<K, V> valueFactory) {
        this.backingMap = backingMap;
        this.syncUpdater = new SyncMapUpdater<>(backingMap, syncKeyCodec, valueFactory);
        this.storageCodec = new MapStorageCodec<>(backingMap, storageKeyCodec, valueFactory);
    }

    public static <K, V extends IMKSerializable<CompoundTag>> SyncStoredMap<K, V> stringKeys(
            Function<K, String> keyEncoder,
            Function<String, K> keyDecoder,
            Function<K, V> valueFactory) {
        return new SyncStoredMap<>(
                new HashMap<>(),
                SyncMapUpdater.KeyCodec.stringKeys(keyEncoder, keyDecoder),
                MapStorageCodec.StorageKeyCodec.stringKeys(keyEncoder, keyDecoder),
                valueFactory
        );
    }

    public static <V extends IMKSerializable<CompoundTag>, RV> SyncStoredMap<ResourceLocation, V> registryResourceLocations(
            ResourceKey<? extends Registry<RV>> registryKey,
            Function<ResourceLocation, V> valueFactory) {
        return new SyncStoredMap<>(
                new HashMap<>(),
                SyncMapUpdater.KeyCodec.registryResourceLocations(registryKey),
                MapStorageCodec.StorageKeyCodec.registryResourceLocations(registryKey),
                valueFactory
        );
    }

    public static <V extends IMKSerializable<CompoundTag>, RV> SyncStoredMap<ResourceKey<RV>, V> registryResourceKeys(
            ResourceKey<? extends Registry<RV>> registryKey,
            Function<ResourceKey<RV>, V> valueFactory) {
        return new SyncStoredMap<>(
                new HashMap<>(),
                SyncMapUpdater.KeyCodec.registryResourceKeys(registryKey),
                MapStorageCodec.StorageKeyCodec.registryResourceKeys(registryKey),
                valueFactory
        );
    }

    public static <V extends IMKSerializable<CompoundTag>, RV> SyncStoredMap<Holder<RV>, V> registryHolders(
            ResourceKey<? extends Registry<RV>> registryKey,
            Function<Holder<RV>, V> valueFactory) {
        return new SyncStoredMap<>(
                new HashMap<>(),
                SyncMapUpdater.KeyCodec.registryHolders(registryKey),
                MapStorageCodec.StorageKeyCodec.registryHolders(registryKey),
                valueFactory
        );
    }

    public void setOnRemoveCallback(Consumer<K> onRemoveCallback) {
        syncUpdater.setOnRemoveCallback(onRemoveCallback);
    }

    public Map<K, V> asMap() {
        return Collections.unmodifiableMap(backingMap);
    }

    public Collection<V> values() {
        return Collections.unmodifiableCollection(backingMap.values());
    }

    public Set<K> keySet() {
        return Collections.unmodifiableSet(backingMap.keySet());
    }

    public @Nullable V get(K key) {
        return backingMap.get(key);
    }

    public boolean containsKey(K key) {
        return backingMap.containsKey(key);
    }

    public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
        return backingMap.computeIfAbsent(key, mappingFunction);
    }

    public @Nullable V remove(K key) {
        V removed = backingMap.remove(key);
        if (removed != null) {
            syncUpdater.markDirty(key);
        }
        return removed;
    }

    public void markDirty(K key) {
        syncUpdater.markDirty(key);
    }

    public CompoundTag serializeStorage(HolderLookup.Provider provider) {
        return storageCodec.serialize(provider);
    }

    public void deserializeStorage(HolderLookup.Provider provider, CompoundTag tag) {
        storageCodec.deserialize(provider, tag);
    }

    @Override
    public void setSyncUpdateNotifier(ISyncNotifier notifier) {
        syncUpdater.setSyncUpdateNotifier(notifier);
    }

    @Override
    public boolean isDirty() {
        return syncUpdater.isDirty();
    }

    @Override
    public void clearDirty() {
        syncUpdater.clearDirty();
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        return syncUpdater.writeFullValue(context, visibility);
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        return syncUpdater.writeDirtyValue(context, visibility);
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        syncUpdater.handleUpdatePayload(context, valueTag, visibility);
    }
}
