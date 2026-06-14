package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceHorizontalExtrusionMode {
    NO_EXTRUSION("no_extrusion"),
    FLOOR_ONLY("floor_only"),
    TUNNEL_ONLY("tunnel_only"),
    FULL_BODY("full_body"),
    FULL_FACE("full_face");

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
        return FULL_BODY;
    }

    public MKWorkspaceHorizontalExtrusionMode next() {
        return switch (this) {
            case NO_EXTRUSION -> FLOOR_ONLY;
            case FLOOR_ONLY -> TUNNEL_ONLY;
            case TUNNEL_ONLY -> FULL_BODY;
            case FULL_BODY -> FULL_FACE;
            case FULL_FACE -> NO_EXTRUSION;
        };
    }
}
