package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Handles reading and writing a {@code Map<K, V>} to a string-keyed {@link CompoundTag}
 * for stable disk storage. This is intentionally separate from the sync adapters — storage
 * keys are always human-readable strings that survive across server restarts and registry
 * changes, while sync encoding can use compact representations like integer registry IDs.
 */
public class MapStorageCodec<K, V extends IMKSerializable<CompoundTag>> {
    private final Map<K, V> backingMap;
    private final Function<K, String> keyEncoder;
    private final BiFunction<HolderLookup.Provider, String, K> keyDecoder;
    private final Function<K, V> valueFactory;

    public MapStorageCodec(Map<K, V> map,
                           Function<K, String> keyEncoder,
                           Function<String, K> keyDecoder,
                           Function<K, V> valueFactory) {
        this(map, keyEncoder, (provider, encoded) -> keyDecoder.apply(encoded), valueFactory);
    }

    public MapStorageCodec(Map<K, V> map,
                           Function<K, String> keyEncoder,
                           BiFunction<HolderLookup.Provider, String, K> keyDecoder,
                           Function<K, V> valueFactory) {
        this.backingMap = map;
        this.keyEncoder = keyEncoder;
        this.keyDecoder = keyDecoder;
        this.valueFactory = valueFactory;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        for (Map.Entry<K, V> entry : backingMap.entrySet()) {
            Tag valueTag = entry.getValue().serializeStorage(provider);
            if (valueTag != null) {
                result.put(keyEncoder.apply(entry.getKey()), valueTag);
            }
        }
        return result;
    }

    public void deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        clearMap();
        for (String key : tag.getAllKeys()) {
            K decodedKey = decodeKey(provider, key);
            if (decodedKey == null) {
                MKCore.LOGGER.error("Failed to decode storage map key {}", key);
                continue;
            }

            V current = backingMap.get(decodedKey);
            boolean isNew = current == null;
            if (isNew) {
                current = valueFactory.apply(decodedKey);
            }
            if (current == null) {
                MKCore.LOGGER.error("Failed to compute map value for key {}", decodedKey);
                continue;
            }

            if (!current.deserializeStorage(provider, tag.getCompound(key))) {
                MKCore.LOGGER.error("Failed to deserialize storage map value for {}", decodedKey);
                continue;
            }
            if (isNew) {
                backingMap.put(decodedKey, current);
            }
        }
    }

    @Nullable
    private K decodeKey(HolderLookup.Provider provider, String encoded) {
        try {
            return keyDecoder.apply(provider, encoded);
        } catch (Exception e) {
            MKCore.LOGGER.error("Exception decoding storage map key {}", encoded, e);
            return null;
        }
    }

    private void clearMap() {
        backingMap.clear();
    }
}
