package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceHorizontalExitPathKind implements StringRepresentable {
    MAIN("main"),
    BRANCH("branch");

    private final String serializedName;

    MKWorkspaceHorizontalExitPathKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKWorkspaceHorizontalExitPathKind fromSerializedName(String name) {
        for (MKWorkspaceHorizontalExitPathKind value : values()) {
            if (value.serializedName.equals(name)) {
                return value;
            }
        }
        return BRANCH;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    public MKWorkspaceHorizontalExitPathKind next() {
        return this == MAIN ? BRANCH : MAIN;
    }
}
