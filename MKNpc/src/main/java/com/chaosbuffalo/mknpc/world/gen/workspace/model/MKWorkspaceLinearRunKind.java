package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceLinearRunKind {
    ENCLOSED_CORRIDOR("enclosed_corridor"),
    OPEN_WALKWAY("open_walkway"),
    DEFENSIVE_WALL("defensive_wall"),
    SOLID_WALL("solid_wall"),
    PARAPET("parapet");

    private final String serializedName;

    MKWorkspaceLinearRunKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static MKWorkspaceLinearRunKind fromSerializedName(String name) {
        for (MKWorkspaceLinearRunKind kind : values()) {
            if (kind.serializedName.equals(name)) {
                return kind;
            }
        }
        return ENCLOSED_CORRIDOR;
    }
}
