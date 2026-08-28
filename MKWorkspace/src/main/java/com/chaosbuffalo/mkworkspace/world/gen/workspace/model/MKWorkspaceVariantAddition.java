package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import javax.annotation.Nullable;

public record MKWorkspaceVariantAddition(String basePieceName, @Nullable String sourcePieceName) {
    public MKWorkspaceVariantAddition {
        basePieceName = basePieceName == null ? "" : basePieceName;
        sourcePieceName = sourcePieceName == null || sourcePieceName.isBlank() ? null : sourcePieceName;
    }
}
