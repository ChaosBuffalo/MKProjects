package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKStructureFamilyType implements StringRepresentable {
    TOWER("tower"),
    DUNGEON("dungeon"),
    LABYRINTH("labyrinth");

    private final String serializedName;

    MKStructureFamilyType(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKStructureFamilyType fromSerializedName(String name) {
        for (MKStructureFamilyType value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return TOWER;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
