package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import net.minecraft.resources.ResourceLocation;

/** @deprecated Insert pools are topology-slot pools. Use {@link MKInsertSlotPools}. */
@Deprecated(forRemoval = false)
public final class MKInsertFamilyPools {
    public static final String TAG_INSERT_FAMILY_ID = MKInsertSlotPools.LEGACY_TAG_INSERT_FAMILY_ID;
    public static final String TAG_INSERT_FAMILY_KIND = MKInsertSlotPools.TAG_INSERT_SLOT_KIND;
    public static final String INSERT_FAMILY_POOL_SEGMENT = MKInsertSlotPools.INSERT_SLOT_POOL_SEGMENT;

    private MKInsertFamilyPools() {
    }

    public static ResourceLocation poolId(String namespace, String structureName, String familyId) {
        return MKInsertSlotPools.poolId(namespace, structureName, familyId);
    }
}
