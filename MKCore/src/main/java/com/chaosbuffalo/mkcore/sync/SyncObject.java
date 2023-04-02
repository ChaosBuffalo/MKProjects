package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;

import java.util.function.BiConsumer;

public class SyncObject<T> implements ISyncObject {
    protected final String name;
    private T value;
    private boolean dirty;
    private final BiConsumer<CompoundTag, SyncObject<T>> serializer;
    private final BiConsumer<CompoundTag, SyncObject<T>> deserializer;
    private ISyncNotifier parentNotifier = ISyncNotifier.NONE;

    public SyncObject(String name, T value, BiConsumer<CompoundTag, SyncObject<T>> serializer, BiConsumer<CompoundTag, SyncObject<T>> deserializer) {
        this.name = name;
        this.serializer = serializer;
        this.deserializer = deserializer;
        set(value);
    }

    public void set(T value) {
        this.value = value;
        this.dirty = true;
        parentNotifier.notifyUpdate(this);
    }

    public T get() {
        return value;
    }

    @Override
    public void setNotifier(ISyncNotifier notifier) {
        parentNotifier = notifier;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void deserializeUpdate(CompoundTag tag) {
        if (tag.contains(name)) {
            deserializer.accept(tag, this);
        }
    }

    @Override
    public void serializeUpdate(CompoundTag tag) {
        if (dirty) {
            serializeFull(tag);
            dirty = false;
        }
    }

    @Override
    public void serializeFull(CompoundTag tag) {
        serializer.accept(tag, this);
        dirty = false;
    }
}
