package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import net.minecraft.core.BlockPos;

import java.util.Objects;

public record MKWorkspaceChangeApplyResult(boolean success, BlockPos anchor, String message,
                                           boolean refreshWorkspaceScreen) {
    public MKWorkspaceChangeApplyResult {
        Objects.requireNonNull(anchor, "anchor");
        message = Objects.requireNonNullElse(message, "");
    }

    public static MKWorkspaceChangeApplyResult success(BlockPos anchor, String message) {
        return new MKWorkspaceChangeApplyResult(true, anchor, message, true);
    }

    public static MKWorkspaceChangeApplyResult failure(BlockPos anchor, String message) {
        return new MKWorkspaceChangeApplyResult(false, anchor, message, false);
    }
}
