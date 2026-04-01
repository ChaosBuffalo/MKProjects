package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;


public class SyncMapUpdater<K, V extends IMKSerializable<CompoundTag>> implements ISyncObject {

    private final Map<K, V> backingMap;
    private final Function<K, String> keyEncoder;
    private final Function<String, K> keyDecoder;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> valueFactory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private Consumer<K> onRemoveCallback;

    public SyncMapUpdater(Map<K, V> mapSupplier,
                          Function<K, String> keyEncoder,
                          Function<String, K> keyDecoder,
                          Function<K, V> valueFactory) {
        this.backingMap = mapSupplier;
        this.keyEncoder = keyEncoder;
        this.keyDecoder = keyDecoder;
        this.valueFactory = valueFactory;
        onRemoveCallback = null;
    }

    public void setOnRemoveCallback(Consumer<K> onRemoveCallback) {
        this.onRemoveCallback = onRemoveCallback;
    }

    public void markDirty(K key) {
        dirty.add(key);
        parentNotifier.notifyUpdate();
    }

    @Override
    public void setSyncUpdateNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirty.isEmpty();
    }

    @Override
    public void clearDirty() {
        dirty.clear();
    }

    @Nullable
    private ListTag gatherDirtyRemovals() {
        if (dirty.isEmpty())
            return null;

        ListTag removedKeys = new ListTag();
        dirty.removeIf(key -> {
            V value = backingMap.get(key);
            if (value == null) {
                removedKeys.add(StringTag.valueOf(keyEncoder.apply(key)));
                return true;
            }
            return false;
        });

        return removedKeys;
    }

    private void processDirtyRemovals(ListTag list) {
        for (int i = 0; i < list.size(); i++) {
            String encodedKey = list.getString(i);
            if (encodedKey.isEmpty())
                continue;

            K key = decodeKey(encodedKey);
            if (key == null) {
                continue;
            }
            removeEntry(key);
        }
    }

    private CompoundTag makeSyncMap(HolderLookup.Provider provider, Collection<K> keySet) {
        return serializeMap(keySet, o -> o.serializeSync(provider));
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        if (!backingMap.isEmpty()) {
            root.put("l", makeSyncMap(context.provider(), backingMap.keySet()));
        }
        return root;
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        if (dirty.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        ListTag removals = gatherDirtyRemovals();
        if (removals != null && !removals.isEmpty()) {
            root.put("r", removals);
        }

        CompoundTag updates = makeSyncMap(context.provider(), dirty);
        if (!updates.isEmpty()) {
            root.put("l", updates);
        }

        dirty.clear();
        return root;
    }

    @Override
    public void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility) {
        if (valueTag instanceof CompoundTag root) {
            if (root.getBoolean("f")) {
                clearMap();
            }

            if (root.contains("r")) {
                // server has deleted entries, so remove them from the local map
                processDirtyRemovals(root.getList("r", Tag.TAG_STRING));
            }

            if (root.contains("l")) {
                CompoundTag list = root.getCompound("l");
                if (!list.isEmpty()) {
                    deserializeMap(list, (o, t) -> o.deserializeSync(context.provider(), t));
                }
            }
        }
    }

    private CompoundTag serializeMap(Collection<K> keyCollection,
                                     Function<V, Tag> valueSerializer) {
        CompoundTag list = new CompoundTag();
        for (K key : keyCollection) {
            V value = backingMap.get(key);
            if (value == null)
                continue;

            Tag tag = valueSerializer.apply(value);
            if (tag != null) {
                list.put(keyEncoder.apply(key), tag);
            }
        }
        return list;
    }

    private void deserializeMap(CompoundTag tag,
                                BiPredicate<V, CompoundTag> valueDeserializer) {
        for (String key : tag.getAllKeys()) {
            K decodedKey = decodeKey(key);
            if (decodedKey == null) {
                MKCore.LOGGER.error("Failed to decode map key {}", key);
                continue;
            }

            V current = backingMap.get(decodedKey);
            boolean isNewValue = current == null;
            if (current == null) {
                current = valueFactory.apply(decodedKey);
            }
            if (current == null) {
                MKCore.LOGGER.error("Failed to compute map value for key {}", decodedKey);
                continue;
            }

            CompoundTag entryTag = tag.getCompound(key);
            if (!valueDeserializer.test(current, entryTag)) {
                MKCore.LOGGER.error("Failed to deserialize map value for {}", decodedKey);
                continue;
            }
            if (isNewValue) {
                backingMap.put(decodedKey, current);
            }
        }
    }

    public CompoundTag serializeStorage(HolderLookup.Provider provider) {
        return serializeMap(backingMap.keySet(), o -> o.serializeStorage(provider));
    }

    public void deserializeStorage(HolderLookup.Provider provider, Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            clearMap();
            deserializeMap(compoundTag, (o, t) -> o.deserializeStorage(provider, t));
        }
    }

    @Nullable
    private K decodeKey(String encodedKey) {
        try {
            return keyDecoder.apply(encodedKey);
        } catch (Exception e) {
            MKCore.LOGGER.error("Exception decoding map key {}", encodedKey, e);
            return null;
        }
    }

    private void removeEntry(K key) {
        if (onRemoveCallback != null) {
            onRemoveCallback.accept(key);
        }
        backingMap.remove(key);
    }

    private void clearMap() {
        if (backingMap.isEmpty()) {
            return;
        }

        for (K key : new HashSet<>(backingMap.keySet())) {
            removeEntry(key);
        }
    }

    @Override
    public String toString() {
        return String.format("SyncMap[dirty=%d, map=%s]", dirty.size(), backingMap);
    }
}
