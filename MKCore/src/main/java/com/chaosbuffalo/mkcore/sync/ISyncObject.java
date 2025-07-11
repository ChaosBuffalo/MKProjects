package com.chaosbuffalo.mkcore.sync;

import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public interface ISyncObject {

    void setNotifier(ISyncNotifier notifier);

    boolean isDirty();

    void clearDirty();

    void handleUpdatePayload(SyncContext context, Tag valueTag);

    @Nullable
    Tag writeFullValue(SyncContext context);

    @Nullable
    Tag writeUpdateValue(SyncContext context);

    static Tag notImplementedByDesign(Object self) {
        throw new IllegalStateException("object '%s' does not implement sync method".formatted(self));
    }
}
