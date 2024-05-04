package com.chaosbuffalo.mkcore.utils;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.K1;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class CommonCodecs {
    public static final Codec<EquipmentSlot> EQUIPMENT_SLOT_CODEC = ExtraCodecs.stringResolverCodec(EquipmentSlot::getName, EquipmentSlot::byName);

    // ItemStack.CODEC does not include capabilities, so we need this workaround
    public static final Codec<ItemStack> ITEM_STACK_WITH_CAPS_CODEC = CompoundTag.CODEC.xmap(ItemStack::of, i -> i.save(new CompoundTag()));

    public static final Codec<ItemStack> ITEM_STACK = ItemStack.CODEC;

    public static final Codec<AttributeModifier.Operation> ATTRIBUTE_MODIFIER_OPERATION_CODEC =
            Codec.intRange(0, 2).xmap(AttributeModifier.Operation::fromValue, AttributeModifier.Operation::toValue);

    public static final Codec<AttributeModifier> ATTRIBUTE_MODIFIER_CODEC = RecordCodecBuilder.<AttributeModifier>mapCodec(builder -> {
        return builder.group(
                UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(AttributeModifier::getId),
                Codec.STRING.fieldOf("name").forGetter(AttributeModifier::getName),
                Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::getAmount),
                ATTRIBUTE_MODIFIER_OPERATION_CODEC.fieldOf("operation").forGetter(AttributeModifier::getOperation)
        ).apply(builder, AttributeModifier::new);
    }).codec();


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
