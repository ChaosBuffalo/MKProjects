package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public enum MKWorkspaceHorizontalExitConnectionMode {
    LINEAR_RUN("hallway"),
    DIRECT_ROOM("direct_room"),
    NO_CONNECTION("no_connection");

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
        return LINEAR_RUN;
    }

    public MKWorkspaceHorizontalExitConnectionMode next() {
        return switch (this) {
            case LINEAR_RUN -> DIRECT_ROOM;
            case DIRECT_ROOM -> NO_CONNECTION;
            case NO_CONNECTION -> LINEAR_RUN;
        };
    }
}
