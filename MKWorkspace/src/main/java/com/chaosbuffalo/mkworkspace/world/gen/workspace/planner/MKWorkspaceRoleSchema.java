package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import java.util.Set;

public record MKWorkspaceRoleSchema(
        String roleId,
        String roleKind,
        String runtimeRoleHint,
        boolean terminal,
        boolean startCandidate,
        Set<String> tags
) {
    public MKWorkspaceRoleSchema {
        tags = Set.copyOf(tags);
    }
}
