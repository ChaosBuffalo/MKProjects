package com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout;

import net.minecraft.resources.ResourceLocation;

public final class MKFloorTopologyPoolNames {
    private static final String LINEAR_RUN_POOL_PREFIX = "linear_runs";
    private static final String ROOM_POOL_PREFIX = "rooms";
    private static final String FLOOR_PLAN_POOL_PREFIX = "floor_plan";
    private static final String MAIN_CAP_APPROACH_POOL_PREFIX = "main_cap_approaches";
    private static final String MAIN_CAP_POOL_PREFIX = "main_caps";

    private MKFloorTopologyPoolNames() {
    }

    public static String mainCapApproachPoolName(String topologyGroupId) {
        return MAIN_CAP_APPROACH_POOL_PREFIX + "/" + topologyGroupId;
    }

    public static String mainCapPoolName(String topologyGroupId) {
        return MAIN_CAP_POOL_PREFIX + "/" + topologyGroupId;
    }

    public static String floorLinearRunPoolName(String topologyGroupId, String openingProfileId, boolean mainPath) {
        return FLOOR_PLAN_POOL_PREFIX + "/" + topologyGroupId + "/" + LINEAR_RUN_POOL_PREFIX + "/" +
                pathPoolSegment(mainPath) + "/" + openingProfileId;
    }

    public static String floorRoomPoolName(String topologyGroupId, String openingProfileId, boolean mainPath) {
        return FLOOR_PLAN_POOL_PREFIX + "/" + topologyGroupId + "/" + ROOM_POOL_PREFIX + "/" +
                pathPoolSegment(mainPath) + "/" + openingProfileId;
    }

    public static String floorRoomMaskPoolName(String topologyGroupId, String openingProfileId, boolean mainPath,
                                               String maskName) {
        ResourceLocation basePool = ResourceLocation.fromNamespaceAndPath("mknpc",
                floorRoomPoolName(topologyGroupId, openingProfileId, mainPath));
        return MKFloorMaskPools.maskPool(basePool, maskName).getPath();
    }

    public static String directMainRoomPoolName(String openingProfileId) {
        return ROOM_POOL_PREFIX + "/main/" + openingProfileId;
    }

    public static String directBranchRoomPoolName(String openingProfileId) {
        return ROOM_POOL_PREFIX + "/branch/" + openingProfileId;
    }

    private static String pathPoolSegment(boolean mainPath) {
        return mainPath ? "main" : "branch";
    }
}
