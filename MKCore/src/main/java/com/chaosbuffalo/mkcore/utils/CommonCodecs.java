package com.chaosbuffalo.mkcore.utils;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.K1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CommonCodecs {

    public static <K, V> Codec<V> createMapBackedDispatch(Codec<K> keyCodec,
                                                          Map<K, MapCodec<? extends V>> codecMap,
                                                          Function<V, K> valueToKey) {
        return keyCodec.dispatch(valueToKey, type -> {
            MapCodec<? extends V> codec = codecMap.get(type);
            if (codec != null) {
                return codec;
            }
            throw new IllegalStateException("No codec registered for " + type);
        });
    }

    public static <K, V> MapCodec<V> createMapBackedDispatchMap(Codec<K> keyCodec,
                                                                Map<K, MapCodec<? extends V>> codecMap,
                                                                Function<V, K> valueToKey) {
        return keyCodec.dispatchMap(valueToKey, type -> {
            MapCodec<? extends V> codec = codecMap.get(type);
            if (codec != null) {
                return codec;
            }
            throw new IllegalStateException("No codec registered for " + type);
        });
    }

    public static <K, V> Codec<V> createLookupDispatch(Codec<K> keyCodec,
                                                       Function<K, MapCodec<? extends V>> keyToValueCodec,
                                                       Function<V, K> valueToKey) {
        return keyCodec.dispatch(valueToKey, type -> {
            MapCodec<? extends V> codec = keyToValueCodec.apply(type);
            if (codec != null) {
                return codec;
            }
            throw new IllegalStateException("No codec registered for " + type);
        });
    }

    public static <E> Codec<Set<E>> set(Codec<E> elementCodec) {
        return elementCodec.listOf().xmap(Set::copyOf, ImmutableList::copyOf);
    }

    // Converts a set to a sorted list for stable datagen output
    public static <E> Codec<Set<E>> sortedSet(Codec<E> elementCodec, Comparator<E> comparator) {
        return elementCodec.listOf().xmap(Set::copyOf, set -> ImmutableList.sortedCopyOf(comparator, set));
    }

    public static <F extends K1, T1, T2, T3, T4, T5, T6, T7, T8, T9> Products.P9<F, T1, T2, T3, T4, T5, T6, T7, T8, T9> and(
            final Products.P5<F, T1, T2, T3, T4, T5> p1,
            final Products.P4<F, T6, T7, T8, T9> p2) {
        return new Products.P9<>(p1.t1(), p1.t2(), p1.t3(), p1.t4(), p1.t5(), p2.t1(), p2.t2(), p2.t3(), p2.t4());
    }
}
