package com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout;

import net.minecraft.resources.ResourceLocation;

public final class MKFloorMaskPools {
    public static final String FLOOR_MASK_TAG = "workspace_floor_exit_mask";
    public static final String MASK_POOL_SEGMENT = "masks";

    private MKFloorMaskPools() {
    }

    public static ResourceLocation maskPool(ResourceLocation basePool, String maskName) {
        if (maskName == null || maskName.isBlank()) {
            return basePool;
        }
        return ResourceLocation.fromNamespaceAndPath(basePool.getNamespace(),
                basePool.getPath() + "/" + MASK_POOL_SEGMENT + "/" + maskName);
    }
}
