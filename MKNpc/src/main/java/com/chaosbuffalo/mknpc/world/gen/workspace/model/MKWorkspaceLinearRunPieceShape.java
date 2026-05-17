package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceLinearRunPieceShape {
    STRAIGHT("straight"),
    CORNER("corner"),
    T_JUNCTION("t_junction"),
    CROSS("cross");

    private final String serializedName;

    MKWorkspaceLinearRunPieceShape(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static MKWorkspaceLinearRunPieceShape fromSerializedName(String name) {
        for (MKWorkspaceLinearRunPieceShape shape : values()) {
            if (shape.serializedName.equals(name)) {
                return shape;
            }
        }
        return STRAIGHT;
    }
}
