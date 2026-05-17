package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;

import java.util.List;

public interface MKWorkspaceTopologyPlanner extends MKWorkspacePlanner {
    String profileType();

    MKWorkspaceTopologySchema schema();

    default List<String> validateTopology(MKStructureWorkspace workspace) {
        return List.of();
    }
}
