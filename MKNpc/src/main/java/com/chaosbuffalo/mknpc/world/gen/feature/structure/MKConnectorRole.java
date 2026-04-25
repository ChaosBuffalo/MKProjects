package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.util.StringRepresentable;

public enum MKConnectorRole implements StringRepresentable {
    MAIN_FORWARD("main_forward"),
    MAIN_BACK("main_back"),
    BRANCH("branch"),
    CONNECT_DOWN("connect_down"),
    CONNECT_UP("connect_up"),
    STAIR_INSERT_DOWN("stair_insert_down"),
    STAIR_INSERT_UP("stair_insert_up"),
    BOSS_FORWARD("boss_forward"),
    BOSS_BACK("boss_back"),
    ROOM("room"),
    TERMINAL("terminal"),
    UNKNOWN("unknown");

    public static final EnumCodec<MKConnectorRole> CODEC = StringRepresentable.fromEnum(MKConnectorRole::values);

    private final String serializedName;

    MKConnectorRole(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
