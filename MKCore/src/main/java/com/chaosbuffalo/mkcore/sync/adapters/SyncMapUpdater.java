package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import com.chaosbuffalo.mkcore.sync.v2.ISyncNotifier;
import com.chaosbuffalo.mkcore.sync.v2.ISyncObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;


public class SyncMapUpdater<K, V extends IMKSerializable<CompoundTag>> implements ISyncObject {

    public interface KeyCodec<K> {
        Tag writeEntries(SyncContext context, Collection<K> keys,
                         Function<K, Tag> valueSerializer);

        void readEntries(SyncContext context, Tag tag, BiConsumer<K, CompoundTag> entryConsumer);

        Tag writeRemovals(SyncContext context, Collection<K> removedKeys);

        void readRemovals(SyncContext context, Tag tag, Consumer<K> keyConsumer);

        static <K> KeyCodec<K> stringKeys(Function<K, String> encoder, Function<String, K> decoder) {
            return new KeyCodec<>() {
                @Override
                public Tag writeEntries(SyncContext context, Collection<K> keys,
                                        Function<K, Tag> valueSerializer) {
                    CompoundTag result = new CompoundTag();
                    for (K key : keys) {
                        Tag valueTag = valueSerializer.apply(key);
                        if (valueTag != null) {
                            result.put(encoder.apply(key), valueTag);
                        }
                    }
                    return result;
                }

                @Override
                public void readEntries(SyncContext context, Tag tag, BiConsumer<K, CompoundTag> entryConsumer) {
                    if (!(tag instanceof CompoundTag ct)) return;
                    for (String key : ct.getAllKeys()) {
                        K decoded = decodeKey(key);
                        if (decoded != null) {
                            entryConsumer.accept(decoded, ct.getCompound(key));
                        }
                    }
                }

                @Override
                public Tag writeRemovals(SyncContext context, Collection<K> removedKeys) {
                    ListTag list = new ListTag();
                    for (K key : removedKeys) {
                        list.add(StringTag.valueOf(encoder.apply(key)));
                    }
                    return list;
                }

                @Override
                public void readRemovals(SyncContext context, Tag tag, Consumer<K> keyConsumer) {
                    if (!(tag instanceof ListTag list)) return;
                    for (int i = 0; i < list.size(); i++) {
                        String encoded = list.getString(i);
                        if (!encoded.isEmpty()) {
                            K key = decodeKey(encoded);
                            if (key != null) {
                                keyConsumer.accept(key);
                            }
                        }
                    }
                }

                @Nullable
                private K decodeKey(String encoded) {
                    try {
                        return decoder.apply(encoded);
                    } catch (Exception e) {
                        MKCore.LOGGER.error("Exception decoding map key {}", encoded, e);
                        return null;
                    }
                }
            };
        }

        static <K, RV> KeyCodec<K> registry(ResourceKey<? extends Registry<RV>> registryKey,
                                             RegistryElementAdapter<K, RV> adapter) {
            return new KeyCodec<>() {
                @Override
                public Tag writeEntries(SyncContext context, Collection<K> keys,
                                        Function<K, Tag> valueSerializer) {
                    Registry<RV> registry = context.registryOrThrow(registryKey);
                    ListTag result = new ListTag();
                    for (K key : keys) {
                        Tag valueTag = valueSerializer.apply(key);
                        if (valueTag == null) continue;
                        RV regValue = adapter.toRegistryValue(registry, key);
                        if (regValue == null) {
                            MKCore.LOGGER.warn("Failed to resolve map key {} in registry {}", key, registryKey.location());
                            continue;
                        }
                        int rawId = registry.getId(regValue);
                        if (rawId < 0) {
                            MKCore.LOGGER.warn("Failed to encode map key {} in registry {}", key, registryKey.location());
                            continue;
                        }
                        CompoundTag entry = new CompoundTag();
                        entry.putInt("k", rawId);
                        entry.put("v", valueTag);
                        result.add(entry);
                    }
                    return result;
                }

                @Override
                public void readEntries(SyncContext context, Tag tag, BiConsumer<K, CompoundTag> entryConsumer) {
                    if (!(tag instanceof ListTag list)) return;
                    Registry<RV> registry = context.registryOrThrow(registryKey);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag entry = list.getCompound(i);
                        int rawId = entry.getInt("k");
                        RV regValue = registry.byId(rawId);
                        if (regValue == null) {
                            MKCore.LOGGER.warn("Failed to decode map key raw id {} in registry {}", rawId, registryKey.location());
                            continue;
                        }
                        K decoded = adapter.fromRegistryValue(registry, regValue);
                        if (decoded == null) {
                            MKCore.LOGGER.warn("Failed to convert map key {} from registry {}", regValue, registryKey.location());
                            continue;
                        }
                        entryConsumer.accept(decoded, entry.getCompound("v"));
                    }
                }

                @Override
                public Tag writeRemovals(SyncContext context, Collection<K> removedKeys) {
                    Registry<RV> registry = context.registryOrThrow(registryKey);
                    int[] ids = new int[removedKeys.size()];
                    int count = 0;
                    for (K key : removedKeys) {
                        RV regValue = adapter.toRegistryValue(registry, key);
                        if (regValue == null) continue;
                        int rawId = registry.getId(regValue);
                        if (rawId >= 0) {
                            ids[count++] = rawId;
                        }
                    }
                    return new IntArrayTag(Arrays.copyOf(ids, count));
                }

                @Override
                public void readRemovals(SyncContext context, Tag tag, Consumer<K> keyConsumer) {
                    if (!(tag instanceof IntArrayTag intArray)) return;
                    Registry<RV> registry = context.registryOrThrow(registryKey);
                    for (int rawId : intArray.getAsIntArray()) {
                        RV regValue = registry.byId(rawId);
                        if (regValue == null) {
                            MKCore.LOGGER.warn("Failed to decode removal key raw id {} in registry {}", rawId, registryKey.location());
                            continue;
                        }
                        K decoded = adapter.fromRegistryValue(registry, regValue);
                        if (decoded != null) {
                            keyConsumer.accept(decoded);
                        }
                    }
                }
            };
        }

        static <RV> KeyCodec<ResourceLocation> registryResourceLocations(
                ResourceKey<? extends Registry<RV>> registryKey) {
            return registry(registryKey, RegistryElementAdapter.resourceLocations());
        }

        static <RV> KeyCodec<ResourceKey<RV>> registryResourceKeys(
                ResourceKey<? extends Registry<RV>> registryKey) {
            return registry(registryKey, RegistryElementAdapter.resourceKeys());
        }

        static <RV> KeyCodec<Holder<RV>> registryHolders(
                ResourceKey<? extends Registry<RV>> registryKey) {
            return registry(registryKey, RegistryElementAdapter.holders());
        }
    }

    private final Map<K, V> backingMap;
    private final KeyCodec<K> keyCodec;
    private final Set<K> dirty = new HashSet<>();
    private final Function<K, V> valueFactory;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;
    private Consumer<K> onRemoveCallback;

    public SyncMapUpdater(Map<K, V> mapSupplier,
                          Function<K, String> keyEncoder,
                          Function<String, K> keyDecoder,
                          Function<K, V> valueFactory) {
        this(mapSupplier, KeyCodec.stringKeys(keyEncoder, keyDecoder), valueFactory);
    }

    public SyncMapUpdater(Map<K, V> map, KeyCodec<K> keyCodec, Function<K, V> valueFactory) {
        this.backingMap = map;
        this.keyCodec = keyCodec;
        this.valueFactory = valueFactory;
        this.onRemoveCallback = null;
    }

    public static <V extends IMKSerializable<CompoundTag>, RV> SyncMapUpdater<ResourceLocation, V> registryResourceLocations(
            Map<ResourceLocation, V> map,
            ResourceKey<? extends Registry<RV>> registryKey,
            Function<ResourceLocation, V> valueFactory) {
        return new SyncMapUpdater<>(map, KeyCodec.registryResourceLocations(registryKey), valueFactory);
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

    @Override
    public @Nullable Tag writeFullValue(SyncContext context, SyncVisibility visibility) {
        CompoundTag root = new CompoundTag();
        root.putBoolean("f", true);
        if (!backingMap.isEmpty()) {
            Tag entries = keyCodec.writeEntries(context, backingMap.keySet(),
                    key -> {
                        V value = backingMap.get(key);
                        return value != null ? value.serializeSync(context.provider()) : null;
                    });
            root.put("l", entries);
        }
        return root;
    }

    @Override
    public @Nullable Tag writeDirtyValue(SyncContext context, SyncVisibility visibility) {
        if (dirty.isEmpty())
            return null;

        CompoundTag root = new CompoundTag();

        List<K> removedKeys = gatherDirtyRemovals();
        if (!removedKeys.isEmpty()) {
            root.put("r", keyCodec.writeRemovals(context, removedKeys));
        }

        if (!dirty.isEmpty()) {
            Tag entries = keyCodec.writeEntries(context, dirty,
                    key -> {
                        V value = backingMap.get(key);
                        return value != null ? value.serializeSync(context.provider()) : null;
                    });
            root.put("l", entries);
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
                keyCodec.readRemovals(context, root.get("r"), this::removeEntry);
            }

            if (root.contains("l")) {
                applySyncEntries(context, root.get("l"));
            }
        }
    }

    private List<K> gatherDirtyRemovals() {
        List<K> removedKeys = new ArrayList<>();
        dirty.removeIf(key -> {
            if (backingMap.get(key) == null) {
                removedKeys.add(key);
                return true;
            }
            return false;
        });
        return removedKeys;
    }

    private void applySyncEntries(SyncContext context, Tag entriesTag) {
        keyCodec.readEntries(context, entriesTag, (key, valueTag) -> {
            V current = backingMap.get(key);
            boolean isNewValue = current == null;
            if (current == null) {
                current = valueFactory.apply(key);
            }
            if (current == null) {
                MKCore.LOGGER.error("Failed to compute map value for key {}", key);
                return;
            }

            if (!current.deserializeSync(context.provider(), valueTag)) {
                MKCore.LOGGER.error("Failed to deserialize map value for {}", key);
                return;
            }
            if (isNewValue) {
                backingMap.put(key, current);
            }
        });
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
