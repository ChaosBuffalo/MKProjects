package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceFoundationMode {
    NONE("none"),
    UNIFORM_STATE("uniform_state"),
    EXTEND_BOTTOM_BLOCKS("extend_bottom_blocks"),
    MASKED_EXTEND_BOTTOM_BLOCKS("masked_extend_bottom_blocks");

    private final String serializedName;

    MKWorkspaceFoundationMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static MKWorkspaceFoundationMode fromSerializedName(String name) {
        for (MKWorkspaceFoundationMode mode : values()) {
            if (mode.serializedName.equals(name)) {
                return mode;
            }
        }
        return NONE;
    }
}
