package com.chaosbuffalo.mkcore.sync.adapters;

import com.chaosbuffalo.mkcore.MKCore;
import com.chaosbuffalo.mkcore.sync.IMKSerializable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles reading and writing a {@code Map<K, V>} to a string-keyed {@link CompoundTag}
 * for stable disk storage. This is intentionally separate from the sync adapters — storage
 * keys are always human-readable strings that survive across server restarts and registry
 * changes, while sync encoding can use compact representations like integer registry IDs.
 */
public class MapStorageCodec<K, V extends IMKSerializable<CompoundTag>> {
    public interface StorageKeyCodec<K> {
        String encode(K key);

        @Nullable
        K decode(HolderLookup.Provider provider, String encoded);

        static <K> StorageKeyCodec<K> stringKeys(Function<K, String> encoder, Function<String, K> decoder) {
            return new StorageKeyCodec<>() {
                @Override
                public String encode(K key) {
                    return encoder.apply(key);
                }

                @Override
                public K decode(HolderLookup.Provider provider, String encoded) {
                    return decoder.apply(encoded);
                }
            };
        }

        static <RV> StorageKeyCodec<ResourceLocation> registryResourceLocations(
                ResourceKey<? extends Registry<RV>> registryKey) {
            return new StorageKeyCodec<>() {
                @Override
                public String encode(ResourceLocation key) {
                    return key.toString();
                }

                @Override
                public ResourceLocation decode(HolderLookup.Provider provider, String encoded) {
                    ResourceLocation parsed = ResourceLocation.tryParse(encoded);
                    if (parsed == null) {
                        return null;
                    }
                    ResourceKey<RV> resourceKey = ResourceKey.create(registryKey, parsed);
                    return provider.lookupOrThrow(registryKey).get(resourceKey).isPresent() ? parsed : null;
                }
            };
        }

        static <RV> StorageKeyCodec<ResourceKey<RV>> registryResourceKeys(
                ResourceKey<? extends Registry<RV>> registryKey) {
            return new StorageKeyCodec<>() {
                @Override
                public String encode(ResourceKey<RV> key) {
                    return key.location().toString();
                }

                @Override
                public ResourceKey<RV> decode(HolderLookup.Provider provider, String encoded) {
                    ResourceLocation parsed = ResourceLocation.tryParse(encoded);
                    if (parsed == null) {
                        return null;
                    }
                    ResourceKey<RV> resourceKey = ResourceKey.create(registryKey, parsed);
                    return provider.lookupOrThrow(registryKey).get(resourceKey).isPresent() ? resourceKey : null;
                }
            };
        }

        static <RV> StorageKeyCodec<Holder<RV>> registryHolders(
                ResourceKey<? extends Registry<RV>> registryKey) {
            return new StorageKeyCodec<>() {
                @Override
                public String encode(Holder<RV> key) {
                    return key.getKey().location().toString();
                }

                @Override
                public Holder<RV> decode(HolderLookup.Provider provider, String encoded) {
                    ResourceLocation parsed = ResourceLocation.tryParse(encoded);
                    if (parsed == null) {
                        return null;
                    }
                    ResourceKey<RV> resourceKey = ResourceKey.create(registryKey, parsed);
                    return provider.lookupOrThrow(registryKey).get(resourceKey).orElse(null);
                }
            };
        }
    }

    private final Map<K, V> backingMap;
    private final StorageKeyCodec<K> keyCodec;
    private final Function<K, V> valueFactory;

    public MapStorageCodec(Map<K, V> map,
                           Function<K, String> keyEncoder,
                           Function<String, K> keyDecoder,
                           Function<K, V> valueFactory) {
        this(map, StorageKeyCodec.stringKeys(keyEncoder, keyDecoder), valueFactory);
    }

    public MapStorageCodec(Map<K, V> map,
                           StorageKeyCodec<K> keyCodec,
                           Function<K, V> valueFactory) {
        this.backingMap = map;
        this.keyCodec = keyCodec;
        this.valueFactory = valueFactory;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        for (Map.Entry<K, V> entry : backingMap.entrySet()) {
            Tag valueTag = entry.getValue().serializeStorage(provider);
            if (valueTag != null) {
                result.put(keyCodec.encode(entry.getKey()), valueTag);
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
            return keyCodec.decode(provider, encoded);
        } catch (Exception e) {
            MKCore.LOGGER.error("Exception decoding storage map key {}", encoded, e);
            return null;
        }
    }

    private void clearMap() {
        backingMap.clear();
    }
}
