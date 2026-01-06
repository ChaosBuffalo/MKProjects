package com.chaosbuffalo.mkcore.sync.v2;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public interface ISyncUpdatableBase {
    void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility);

    @Nullable
    Tag writeFullValue(SyncContext context, SyncVisibility visibility);

    @Nullable
    Tag writeDirtyValue(SyncContext context, SyncVisibility visibility);
}
