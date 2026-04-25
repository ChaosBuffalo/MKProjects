package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKTowerStairPlacement implements StringRepresentable {
    CENTER("center"),
    NORTH("north"),
    SOUTH("south"),
    EAST("east"),
    WEST("west");

    private final String serializedName;

    MKTowerStairPlacement(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKTowerStairPlacement fromSerializedName(String name) {
        for (MKTowerStairPlacement value : values()) {
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

    public MKTowerStairPlacement next() {
        MKTowerStairPlacement[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
