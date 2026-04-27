package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceHorizontalExtrusionMode {
    TUNNEL_ONLY("tunnel_only"),
    FULL_BODY("full_body");

    private final String serializedName;

    MKWorkspaceHorizontalExtrusionMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static MKWorkspaceHorizontalExtrusionMode fromSerializedName(String serializedName) {
        for (MKWorkspaceHorizontalExtrusionMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(serializedName)) {
                return mode;
            }
        }
        return TUNNEL_ONLY;
    }

    public MKWorkspaceHorizontalExtrusionMode next() {
        return this == TUNNEL_ONLY ? FULL_BODY : TUNNEL_ONLY;
    }
}
