package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspacePieceRole implements StringRepresentable {
    ENTRY("entry"),
    FLOOR_MAIN("floor_main"),
    TOP_CAP_APPROACH("top_cap_approach"),
    TOP_CAP("top_cap"),
    BASEMENT_ENTRY("basement_entry"),
    BASEMENT_MAIN("basement_main"),
    BASEMENT_CAP_APPROACH("basement_cap_approach"),
    BASEMENT_CAP("basement_cap"),
    HALLWAY("hallway");

    private final String serializedName;

    MKWorkspacePieceRole(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKWorkspacePieceRole fromSerializedName(String name) {
        for (MKWorkspacePieceRole value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown workspace piece role: " + name);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}

