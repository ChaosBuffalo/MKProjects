package com.chaosbuffalo.mknpc.world.gen.feature.structure;

import net.minecraft.util.StringRepresentable;

public enum MKVerticalProgressionMode implements StringRepresentable {
    DOWNWARD("downward"),
    UPWARD("upward"),
    MIXED("mixed");

    public static final EnumCodec<MKVerticalProgressionMode> CODEC = StringRepresentable.fromEnum(MKVerticalProgressionMode::values);

    private final String serializedName;

    MKVerticalProgressionMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
