package com.chaosbuffalo.mkcore.sync.v2;


public interface ISyncObject extends ISyncUpdatableBase {
    void setSyncUpdateNotifier(ISyncNotifier notifier);

    /**
     * Contract: if this returns true, writeDirtyValue(...) must return a non-null payload that
     * represents and clears the dirty state being written.
     */
    boolean isDirty();

    static void notImplementedByDesign(Object self) {
        throw new IllegalStateException("object '%s' does not implement sync method".formatted(self));
    }
}
