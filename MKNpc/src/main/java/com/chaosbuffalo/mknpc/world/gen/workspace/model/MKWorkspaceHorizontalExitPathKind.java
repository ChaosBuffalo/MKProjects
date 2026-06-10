package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import net.minecraft.util.StringRepresentable;

public enum MKWorkspaceHorizontalExitPathKind implements StringRepresentable {
    INGRESS("ingress"),
    MAIN_ENTRY("main_entry"),
    MAIN_EXIT("main_exit"),
    MAIN_ENDING_ENTRY("main_ending_entry"),
    BRANCH("branch"),
    BRANCH_CAP_ENTRY("branch_cap_entry"),
    LINK_CANDIDATE("link_candidate"),
    VERTICAL_ACCESS("vertical_access");

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
        return this == MAIN_ENTRY || this == MAIN_EXIT || this == MAIN_ENDING_ENTRY;
    }

    public boolean isPlannerIngress() {
        return this == INGRESS;
    }

    public boolean isHorizontal() {
        return this != VERTICAL_ACCESS;
    }

    public boolean isVerticalAccess() {
        return this == VERTICAL_ACCESS;
    }

    public MKWorkspaceHorizontalExitPathKind next() {
        return switch (this) {
            case INGRESS -> MAIN_ENTRY;
            case MAIN_ENTRY -> MAIN_EXIT;
            case MAIN_EXIT -> MAIN_ENDING_ENTRY;
            case MAIN_ENDING_ENTRY -> BRANCH;
            case BRANCH -> BRANCH_CAP_ENTRY;
            case BRANCH_CAP_ENTRY -> LINK_CANDIDATE;
            case LINK_CANDIDATE, VERTICAL_ACCESS -> MAIN_ENTRY;
        };
    }
}
