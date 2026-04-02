package com.chaosbuffalo.mkcore.sync.v2;


public interface ISyncObject extends ISyncUpdatableBase {
    void setSyncUpdateNotifier(ISyncNotifier notifier);

    boolean isDirty();

    static void notImplementedByDesign(Object self) {
        throw new IllegalStateException("object '%s' does not implement sync method".formatted(self));
    }
}
