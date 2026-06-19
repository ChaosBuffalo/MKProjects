package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceInsertFamilyKind implements StringRepresentable {
    FLOOR_LINK_HALLWAY("floor_link_hallway"),
    COURTYARD_SOCKET("courtyard_socket"),
    FLOOR_OPENING_CLOSURE("floor_opening_closure");

    public static final Codec<MKWorkspaceInsertFamilyKind> CODEC = StringRepresentable.fromEnum(
            MKWorkspaceInsertFamilyKind::values);

    private final String serializedName;

    MKWorkspaceInsertFamilyKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
