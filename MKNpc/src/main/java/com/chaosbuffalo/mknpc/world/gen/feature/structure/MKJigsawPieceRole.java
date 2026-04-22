package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.util.StringRepresentable;

public enum MKJigsawPieceRole implements StringRepresentable {
    MAIN("main"),
    BRANCH("branch"),
    ROOM("room"),
    STAIRS_DOWN("stairs_down"),
    STAIRS_UP("stairs_up"),
    BOSS_APPROACH("boss_approach"),
    BOSS("boss"),
    TERMINAL("terminal");

    public static final EnumCodec<MKJigsawPieceRole> CODEC = StringRepresentable.fromEnum(MKJigsawPieceRole::values);

    private final String serializedName;

    MKJigsawPieceRole(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
