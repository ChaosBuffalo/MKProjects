package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceFloorLinkGenerationMode implements StringRepresentable {
    FULL_HALLWAY("full_hallway"),
    DECAYING_HALLWAY("decaying_hallway"),
    DEBUG("debug");

    public static final Codec<MKWorkspaceFloorLinkGenerationMode> CODEC = StringRepresentable.fromEnum(
            MKWorkspaceFloorLinkGenerationMode::values);

    private final String serializedName;

    MKWorkspaceFloorLinkGenerationMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
