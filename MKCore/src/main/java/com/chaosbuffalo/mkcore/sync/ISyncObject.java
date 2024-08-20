package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface ISyncObject {

    void setNotifier(ISyncNotifier notifier);

    boolean isDirty();

    void deserializeUpdate(HolderLookup.Provider provider, CompoundTag tag);

    void serializeUpdate(HolderLookup.Provider provider, CompoundTag tag);

    void serializeFull(HolderLookup.Provider provider, CompoundTag tag);
}
