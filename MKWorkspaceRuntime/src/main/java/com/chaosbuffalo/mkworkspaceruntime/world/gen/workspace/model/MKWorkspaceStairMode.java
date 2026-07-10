package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceStairMode implements StringRepresentable {
    AUTO("auto"),
    RUN_PROFILE("run_profile"),
    STAIR_STAIRS("stair_stairs"),
    SLAB_STAIRS("slab_stairs"),
    LADDER("ladder"),
    NONE("none");

    private final String serializedName;

    MKWorkspaceStairMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKWorkspaceStairMode fromSerializedName(String name) {
        if ("spiral_stairs".equals(name)) {
            return STAIR_STAIRS;
        }
        for (MKWorkspaceStairMode value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return AUTO;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public MKWorkspaceStairMode next() {
        return switch (this) {
            case AUTO -> RUN_PROFILE;
            case RUN_PROFILE -> LADDER;
            case LADDER -> NONE;
            case NONE, STAIR_STAIRS, SLAB_STAIRS -> AUTO;
        };
    }
}
