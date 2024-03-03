package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.sync.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.ISyncObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;


public class SyncMapUpdater<K, V extends IMKSerializable<CompoundTag>> implements ISyncObject {

    private final String rootName;
    private final Map<K, V> backingMap;
    private final Function<K, String> keyEncoder;
    private final Function<String, K> keyDecoder;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> valueFactory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncMapUpdater(String rootName,
                          Map<K, V> mapSupplier,
                          Function<K, String> keyEncoder,
                          Function<String, K> keyDecoder,
                          Function<K, V> valueFactory) {
        this.rootName = rootName;
        this.backingMap = mapSupplier;
        this.keyEncoder = keyEncoder;
        this.keyDecoder = keyDecoder;
        this.valueFactory = valueFactory;
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
            backingMap.remove(key);
//            MKCore.LOGGER.info("removing {} {} {} {}", encodedKey, key, old != null, backingMap.size());
        }
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        CompoundTag root = tag.getCompound(rootName);

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
                deserializeMap(list, IMKSerializable::deserializeSync);
            }
        }
    }

    private CompoundTag makeSyncMap(Collection<K> keySet) {
        return serializeMap(keySet, IMKSerializable::serializeSync);
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirty.isEmpty())
            return;

        CompoundTag root = new CompoundTag();
        ListTag removals = gatherDirtyRemovals();
        if (removals != null && !removals.isEmpty()) {
            root.put("r", removals);
        }

        CompoundTag updates = makeSyncMap(dirty);
        if (!updates.isEmpty()) {
            root.put("l", updates);
        }
        tag.put(rootName, root);

        dirty.clear();
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        root.put("l", makeSyncMap(backingMap.keySet()));
        tag.put(rootName, root);

        dirty.clear();
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

    public CompoundTag serializeStorage() {
        return serializeMap(backingMap.keySet(), IMKSerializable::serializeStorage);
    }

    public void deserializeStorage(Tag tag) {
        if (tag instanceof CompoundTag compoundTag) {
            backingMap.clear();
            deserializeMap(compoundTag, IMKSerializable::deserializeStorage);
        }
    }

    @Override
    public String toString() {
        return String.format("SyncMap[name='%s', dirty=%d, map=%s]", rootName, dirty.size(), backingMap);
    }
}
