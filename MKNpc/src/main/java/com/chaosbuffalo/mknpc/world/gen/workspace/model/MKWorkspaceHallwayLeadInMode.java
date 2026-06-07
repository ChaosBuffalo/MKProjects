package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceHallwayLeadInMode implements StringRepresentable {
    AUTO("auto"),
    MANUAL("manual");

    public static final Codec<MKWorkspaceHallwayLeadInMode> CODEC = StringRepresentable.fromEnum(
            MKWorkspaceHallwayLeadInMode::values);

    private final String serializedName;

    MKWorkspaceHallwayLeadInMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
