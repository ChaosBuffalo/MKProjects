package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKConnectorRole implements StringRepresentable {
    MAIN_FORWARD("main_forward"),
    MAIN_BACK("main_back"),
    BRANCH("branch"),
    CONNECT_UP("connect_up"),
    CONNECT_DOWN("connect_down"),
    STAIR_INSERT_UP("stair_insert_up"),
    STAIR_INSERT_DOWN("stair_insert_down"),
    BOSS_FORWARD("boss_forward"),
    BOSS_BACK("boss_back");

    private final String serializedName;

    MKConnectorRole(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKConnectorRole fromSerializedName(String name) {
        for (MKConnectorRole value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return MAIN_FORWARD;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
