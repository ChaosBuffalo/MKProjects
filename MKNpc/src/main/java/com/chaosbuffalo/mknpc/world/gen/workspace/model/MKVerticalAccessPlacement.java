package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKVerticalAccessPlacement implements StringRepresentable {
    CENTER("center"),
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west");

    private final String serializedName;

    MKVerticalAccessPlacement(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKVerticalAccessPlacement fromSerializedName(String name) {
        for (MKVerticalAccessPlacement value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return CENTER;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public MKVerticalAccessPlacement next() {
        MKVerticalAccessPlacement[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}

