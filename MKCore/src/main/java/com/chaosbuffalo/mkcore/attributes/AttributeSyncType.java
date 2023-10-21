package com.chaosbuffalo.mkcore.attributes;

public enum AttributeSyncType {
    None,
    /**
     * Only sent to the owning player when the value changes
     */
    Private,
    Public;

    public boolean needsInitialSync() {
        return this == Private;
    }

    public boolean syncChanges() {
        return this == Private;
    }

    public boolean syncToAll() {
        return this == Public;
    }
}
