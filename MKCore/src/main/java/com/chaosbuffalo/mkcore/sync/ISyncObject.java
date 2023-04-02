package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.CompoundTag;

public interface ISyncObject {

    void setNotifier(ISyncNotifier notifier);

    boolean isDirty();

    void deserializeUpdate(CompoundTag tag);

    void serializeUpdate(CompoundTag tag);

    void serializeFull(CompoundTag tag);
}
