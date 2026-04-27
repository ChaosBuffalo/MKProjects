package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceHorizontalExitConnectionMode {
    HALLWAY("hallway"),
    DIRECT_ROOM("direct_room");

    private final String serializedName;

    MKWorkspaceHorizontalExitConnectionMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public static MKWorkspaceHorizontalExitConnectionMode fromSerializedName(String serializedName) {
        for (MKWorkspaceHorizontalExitConnectionMode mode : values()) {
            if (mode.serializedName.equalsIgnoreCase(serializedName)) {
                return mode;
            }
        }
        return HALLWAY;
    }

    public MKWorkspaceHorizontalExitConnectionMode next() {
        return this == HALLWAY ? DIRECT_ROOM : HALLWAY;
    }
}
