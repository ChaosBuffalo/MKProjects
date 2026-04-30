package com.chaosbuffalo.mkcore.sync.v2;

import com.chaosbuffalo.mkcore.sync.SyncContext;
import com.chaosbuffalo.mkcore.sync.SyncVisibility;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public interface ISyncUpdatableBase {
    /**
     * Applies a remote sync payload for the requested visibility.
     */
    void handleUpdatePayload(SyncContext context, Tag valueTag, SyncVisibility visibility);

    /**
     * Writes a full snapshot for the requested visibility.
     * <p>
     * This method is for snapshot serialization only. It must not interfere with dirty tracking in
     * any way: do not clear dirty flags, drain queued deltas, or otherwise mutate the pending dirty
     * state from inside {@code writeFullValue(...)}. A later call to {@link #writeDirtyValue} must
     * still be able to observe and write any previously queued dirty state.
     * <p>
     * Returns {@code null} when there is no full payload to send for the requested visibility.
     */
    @Nullable
    Tag writeFullValue(SyncContext context, SyncVisibility visibility);

    /**
     * Writes and clears the currently queued dirty state for the requested visibility.
     * <p>
     * Returns {@code null} only when there is no dirty state to write for the requested visibility.
     */
    @Nullable
    Tag writeDirtyValue(SyncContext context, SyncVisibility visibility);

    /**
     * Clears all locally tracked dirty state.
     */
    void clearDirty();
}
