package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKTowerWorkspaceCategory implements StringRepresentable {
    ENTRY("entry"),
    MAIN("main"),
    BASEMENT("basement"),
    BOSS("boss"),
    BASEMENT_CAP("basement_cap");

    private final String serializedName;

    MKTowerWorkspaceCategory(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKTowerWorkspaceCategory fromSerializedName(String name) {
        for (MKTowerWorkspaceCategory value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown tower workspace category: " + name);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
