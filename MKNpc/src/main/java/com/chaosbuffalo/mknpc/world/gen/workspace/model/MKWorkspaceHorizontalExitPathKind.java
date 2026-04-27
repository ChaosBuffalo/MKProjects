package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceHorizontalExitPathKind implements StringRepresentable {
    MAIN_ENTRY("main_entry"),
    MAIN_EXIT("main_exit"),
    BRANCH("branch");

    private final String serializedName;

    MKWorkspaceHorizontalExitPathKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public static MKWorkspaceHorizontalExitPathKind fromSerializedName(String name) {
        if ("main".equals(name)) {
            return MAIN_EXIT;
        }
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

    public boolean usesMainPath() {
        return this == MAIN_ENTRY || this == MAIN_EXIT;
    }

    public MKWorkspaceHorizontalExitPathKind next() {
        return switch (this) {
            case MAIN_ENTRY -> MAIN_EXIT;
            case MAIN_EXIT -> BRANCH;
            case BRANCH -> MAIN_ENTRY;
        };
    }
}
