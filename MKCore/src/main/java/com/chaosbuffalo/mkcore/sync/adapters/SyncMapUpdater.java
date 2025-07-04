package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import com.chaosbuffalo.mkcore.sync.SyncContext;
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
        parentNotifier.notifyUpdate(this);
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return !dirty.isEmpty();
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

            K key = keyDecoder.apply(encodedKey);
            if (onRemoveCallback != null) {
                onRemoveCallback.accept(key);
            }
            backingMap.remove(key);
//            MKCore.LOGGER.info("removing {} {} {} {}", encodedKey, key, old != null, backingMap.size());
        }
    }

    private CompoundTag makeSyncMap(HolderLookup.Provider provider, Collection<K> keySet) {
        return serializeMap(keySet, o -> o.serializeSync(provider));
    }

    @Override
    public @Nullable Tag writeFullValue(SyncContext context) {
        if (backingMap.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        root.put("l", makeSyncMap(context.provider(), backingMap.keySet()));
        return root;
    }

    @Override
    public @Nullable Tag writeUpdateValue(SyncContext context) {
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
    public void handleUpdatePayload(SyncContext context, Tag valueTag) {
        if (valueTag instanceof CompoundTag root) {
            if (root.getBoolean("f")) {
                backingMap.clear();
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
            K decodedKey = keyDecoder.apply(key);
            if (decodedKey == null) {
                MKCore.LOGGER.error("Failed to decode map key {}", key);
                continue;
            }

            V current = backingMap.computeIfAbsent(decodedKey, valueFactory);
            if (current == null) {
                MKCore.LOGGER.error("Failed to compute map value for key {}", decodedKey);
                continue;
            }

            CompoundTag entryTag = tag.getCompound(key);
            if (!valueDeserializer.test(current, entryTag)) {
                MKCore.LOGGER.error("Failed to deserialize map value for {}", decodedKey);
                continue;
            }
            backingMap.put(decodedKey, current);
        }
    }

    public CompoundTag serializeStorage(HolderLookup.Provider provider) {
        return serializeMap(backingMap.keySet(), o -> o.serializeStorage(provider));
    }

    public void deserializeStorage(HolderLookup.Provider provider, Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            backingMap.clear();
            deserializeMap(compoundTag, (o, t) -> o.deserializeStorage(provider, t));
        }
    }

    @Override
    public String toString() {
        return String.format("SyncMap[dirty=%d, map=%s]", dirty.size(), backingMap);
    }
}
