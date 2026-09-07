package com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MKHallwayLeadInMode implements StringRepresentable {
    AUTO("auto"),
    MANUAL("manual");

    public static final Codec<MKHallwayLeadInMode> CODEC = StringRepresentable.fromEnum(
            MKHallwayLeadInMode::values);

    private final String serializedName;

    MKHallwayLeadInMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
