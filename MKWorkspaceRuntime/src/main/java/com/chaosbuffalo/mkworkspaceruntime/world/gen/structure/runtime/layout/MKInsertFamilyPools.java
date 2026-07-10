package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import net.minecraft.resources.ResourceLocation;

public final class MKInsertFamilyPools {
    public static final String TAG_INSERT_FAMILY_ID = "workspace_insert_family_id";
    public static final String TAG_INSERT_FAMILY_KIND = "workspace_insert_family_kind";
    public static final String INSERT_FAMILY_POOL_SEGMENT = "insert_families";

    private MKInsertFamilyPools() {
    }

    public static ResourceLocation poolId(String namespace, String structureName, String familyId) {
        return ResourceLocation.fromNamespaceAndPath(namespace,
                structureName + "/" + INSERT_FAMILY_POOL_SEGMENT + "/" + familyId);
    }
}
