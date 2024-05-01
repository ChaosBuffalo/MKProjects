package com.chaosbuffalo.mkweapons.items.effects.armor;

import com.chaosbuffalo.mkweapons.MKWeapons;
import com.chaosbuffalo.mkweapons.items.effects.IItemEffect;
import com.chaosbuffalo.mkweapons.items.effects.ItemEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public interface IArmorEffect extends IItemEffect {
    Codec<IArmorEffect> DISPATCH_CODEC = ItemEffects.ARMOR_EFFECT_CODEC;

    default IArmorEffect copy() {
        Tag tag = serialize(NbtOps.INSTANCE);
        return deserialize(new Dynamic<>(NbtOps.INSTANCE, tag));
    }

    default <D> D serialize(DynamicOps<D> ops) {
        return DISPATCH_CODEC.encodeStart(ops, this).getOrThrow(false, MKWeapons.LOGGER::error);
    }

    static <D> IArmorEffect deserialize(Dynamic<D> dynamic) {
        return DISPATCH_CODEC.parse(dynamic).getOrThrow(false, MKWeapons.LOGGER::error);
    }
}
