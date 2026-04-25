package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspacePieceRole implements StringRepresentable {
    ENTRY("entry"),
    ENTRY_STAIRS_UP("entry_stairs_up"),
    FLOOR_MAIN("floor_main"),
    STAIRS_UP("stairs_up"),
    STAIRS_DOWN("stairs_down"),
    BOSS_CAP("boss_cap"),
    BASEMENT_ENTRY("basement_entry"),
    BASEMENT_MAIN("basement_main"),
    BASEMENT_CAP("basement_cap"),
    SURFACE_ENTRY("surface_entry"),
    MAIN_HALL("main_hall"),
    MAIN_ROOM("main_room"),
    BOSS_APPROACH("boss_approach"),
    BOSS_ROOM("boss_room"),
    MAZE_HALL("maze_hall"),
    MAZE_ROOM("maze_room");

    private final String serializedName;

    MKWorkspacePieceRole(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKWorkspacePieceRole fromSerializedName(String name) {
        for (MKWorkspacePieceRole value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return ENTRY;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
