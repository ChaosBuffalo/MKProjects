package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceStairRiseType implements StringRepresentable {
    STAIR("stair"),
    SLAB("slab"),
    MIXED("mixed");

    private final String serializedName;

    MKWorkspaceStairRiseType(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKWorkspaceStairRiseType fromSerializedName(String name) {
        for (MKWorkspaceStairRiseType value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return MIXED;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public MKWorkspaceStairRiseType next() {
        MKWorkspaceStairRiseType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
