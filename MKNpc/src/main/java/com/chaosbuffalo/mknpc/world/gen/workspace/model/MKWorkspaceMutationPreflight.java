package com.chaosbuffalo.mknpc.world.gen.workspace.model;

public record MKWorkspaceMutationPreflight(
        MKWorkspaceInvalidationReport report,
        MKStructureWorkspace workspaceWithDirtyLayers
) {
}
