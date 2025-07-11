package com.chaosbuffalo.mkcore.sync;

import net.minecraft.core.HolderLookup;

// This exists to pass around anything we need to provide to sync or serialization methods
public record SyncContext(HolderLookup.Provider provider) {
}
