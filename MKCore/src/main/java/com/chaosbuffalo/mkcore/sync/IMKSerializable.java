package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface IMKSerializable<T extends Tag> {
    T serialize(HolderLookup.Provider provider);

    boolean deserialize(HolderLookup.Provider provider, CompoundTag tag);

    default T serializeSync() {
        return serialize(provider);
    }

    default boolean deserializeSync(T tag) {
        return deserialize(provider, tag);
    }

    default T serializeStorage() {
        return serialize(provider);
    }

    default boolean deserializeStorage(T tag) {
        return deserialize(provider, tag);
    }
}
