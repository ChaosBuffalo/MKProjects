package com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKFloorLinkGenerationMode implements StringRepresentable {
    FULL_HALLWAY("full_hallway"),
    DECAYING_HALLWAY("decaying_hallway"),
    DEBUG("debug");

    public static final Codec<MKFloorLinkGenerationMode> CODEC = StringRepresentable.fromEnum(
            MKFloorLinkGenerationMode::values);

    private final String serializedName;

    MKFloorLinkGenerationMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
