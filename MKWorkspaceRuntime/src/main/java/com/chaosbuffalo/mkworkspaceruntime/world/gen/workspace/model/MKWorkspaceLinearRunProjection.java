package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

public enum MKWorkspaceLinearRunProjection {
    RIGID("rigid"),
    TERRAIN_MATCHED("terrain_matched");

    private final String serializedName;

    MKWorkspaceLinearRunProjection(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static MKWorkspaceLinearRunProjection fromSerializedName(String name) {
        for (MKWorkspaceLinearRunProjection projection : values()) {
            if (projection.serializedName.equals(name)) {
                return projection;
            }
        }
        return RIGID;
    }
}
