package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;

import java.util.LinkedHashMap;
import java.util.Map;

public class MKWorkspacePlannerRegistry {
    private final Map<String, MKWorkspaceTopologyPlanner> planners = new LinkedHashMap<>();

    public MKWorkspacePlannerRegistry() {
        register(new MKTowerWorkspacePlanner());
    }

    public void register(MKWorkspaceTopologyPlanner planner) {
        planners.put(planner.profileType(), planner);
    }

    public MKWorkspaceTopologyPlanner plannerFor(MKStructureWorkspace workspace) {
        return plannerFor(workspace.topologyProfile().profileType());
    }

    public MKWorkspaceTopologyPlanner plannerFor(String profileType) {
        MKWorkspaceTopologyPlanner planner = planners.get(profileType);
        if (planner == null) {
            throw new IllegalArgumentException("No workspace topology planner registered for profile type " + profileType);
        }
        return planner;
    }

    public MKWorkspaceTopologyPlanner towerPlanner() {
        return plannerFor(MKWorkspaceTopologyProfile.TOWER_PROFILE_TYPE);
    }
}
