package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import net.minecraft.resources.ResourceLocation;

public final class MKFloorMaskPools {
    public static final String FLOOR_MASK_TAG = "workspace_floor_exit_mask";
    public static final String FLOOR_MASK_WEIGHT_TAG = "workspace_floor_mask_weight";
    public static final String FLOOR_RANDOMIZE_MAIN_EXIT_TAG = "workspace_floor_randomize_main_exit";
    public static final String FLOOR_SELECTED_MAIN_EXIT_TAG = "workspace_floor_selected_main_exit";
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
