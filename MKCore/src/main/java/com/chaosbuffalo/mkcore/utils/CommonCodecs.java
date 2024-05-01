package com.chaosbuffalo.mkcore.utils;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Map;
import java.util.function.Function;

public class CommonCodecs {
    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = ExtraCodecs.stringResolverCodec(EquipmentSlot::getName, EquipmentSlot::byName);

    public static <K, V> Codec<V> createMapBackedDispatch(Codec<K> keyCodec,
                                                          Map<K, Codec<? extends V>> codecMap,
                                                          Function<V, K> valueToKey) {
        return keyCodec.dispatch(valueToKey, type -> {
            Codec<? extends V> codec = codecMap.get(type);
            if (codec != null) {
                return codec;
            }
            throw new IllegalStateException("No codec registered for " + type);
        });
    }

    public static <K, V> Codec<V> createLookupDispatch(Codec<K> keyCodec,
                                                       Function<K, Codec<? extends V>> keyToValueCodec,
                                                       Function<V, K> valueToKey) {
        return keyCodec.dispatch(valueToKey, type -> {
            Codec<? extends V> codec = keyToValueCodec.apply(type);
            if (codec != null) {
                return codec;
            }
            throw new IllegalStateException("No codec registered for " + type);
        });
    }
}
