package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.Tag;

public interface IMKSerializable<T extends Tag> {
    T serialize();

    boolean deserialize(T tag);

    default T serializeSync() {
        return serialize();
    }

    default boolean deserializeSync(T tag) {
        return deserialize(tag);
    }

    default T serializeStorage() {
        return serialize();
    }

    default boolean deserializeStorage(T tag) {
        return deserialize(tag);
    }
}
