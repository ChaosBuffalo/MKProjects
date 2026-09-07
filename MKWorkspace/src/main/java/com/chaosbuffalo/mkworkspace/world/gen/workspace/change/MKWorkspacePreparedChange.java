package com.chaosbuffalo.mkworkspace.world.gen.workspace.change;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/** Server-only prepared state. The mutation callback closes over the exact values reported to the user. */
public record MKWorkspacePreparedChange(
        MKWorkspaceChangeRequest request,
        BlockPos anchor,
        @Nullable UUID workspaceId,
        long workspaceFingerprint,
        String operationStateGuard,
        MKWorkspaceChangeSummary summary,
        MKWorkspacePreparedMutation mutation
) {
    public MKWorkspacePreparedChange {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(anchor, "anchor");
        operationStateGuard = Objects.requireNonNullElse(operationStateGuard, "");
        Objects.requireNonNull(summary, "summary");
        Objects.requireNonNull(mutation, "mutation");
    }
}
