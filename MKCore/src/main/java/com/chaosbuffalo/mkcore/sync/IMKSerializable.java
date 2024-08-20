package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

public interface IMKSerializable<T extends Tag> {
    T serialize(HolderLookup.Provider provider);

    boolean deserialize(HolderLookup.Provider provider, T tag);

    default T serializeSync(HolderLookup.Provider provider) {
        return serialize(provider);
    }

    default boolean deserializeSync(HolderLookup.Provider provider, T tag) {
        return deserialize(provider, tag);
    }

    default T serializeStorage(HolderLookup.Provider provider) {
        return serialize(provider);
    }

    default boolean deserializeStorage(HolderLookup.Provider provider, T tag) {
        return deserialize(provider, tag);
    }
}
