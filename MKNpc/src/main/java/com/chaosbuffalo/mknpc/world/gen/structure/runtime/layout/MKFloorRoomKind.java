package com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKFloorRoomKind implements StringRepresentable {
    MAIN_ROOM("main_room"),
    BRANCH_ROOM("branch_room"),
    BRANCH_CAP("branch_cap"),
    MAIN_CAP_APPROACH("main_cap_approach"),
    MAIN_CAP("main_cap");

    public static final Codec<MKFloorRoomKind> CODEC = StringRepresentable.fromEnum(
            MKFloorRoomKind::values);

    private final String serializedName;

    MKFloorRoomKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public boolean usesMainPath() {
        return this == MAIN_ROOM || this == MAIN_CAP_APPROACH || this == MAIN_CAP;
    }

    public boolean hasMainExit() {
        return this == MAIN_ROOM || this == MAIN_CAP_APPROACH;
    }

    public boolean isMainPathEnding() {
        return this == MAIN_CAP_APPROACH || this == MAIN_CAP;
    }

    public boolean isBranchPath() {
        return this == BRANCH_ROOM || this == BRANCH_CAP;
    }

    public boolean isBranchCap() {
        return this == BRANCH_CAP;
    }
}
