package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceFloorRoomKind implements StringRepresentable {
    MAIN_ROOM("main_room"),
    BRANCH_ROOM("branch_room");

    public static final Codec<MKWorkspaceFloorRoomKind> CODEC = StringRepresentable.fromEnum(
            MKWorkspaceFloorRoomKind::values);

    private final String serializedName;

    MKWorkspaceFloorRoomKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
