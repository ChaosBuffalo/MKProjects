package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MKWorkspacePlannerRegistry {
    private final Map<ResourceLocation, MKWorkspaceTopologyPlanner> planners = new LinkedHashMap<>();

    public MKWorkspacePlannerRegistry() {
        register(new MKTowerWorkspacePlanner());
        register(new MKWalledKeepWorkspacePlanner());
    }

    public void register(MKWorkspaceTopologyPlanner planner) {
        planners.put(planner.plannerId(), planner);
    }

    public MKWorkspaceTopologyPlanner plannerFor(MKStructureWorkspace workspace) {
        return plannerFor(workspace.topologyProfile().plannerId());
    }

    public MKWorkspaceTopologyPlanner plannerFor(ResourceLocation plannerId) {
        MKWorkspaceTopologyPlanner planner = planners.get(plannerId);
        if (planner == null) {
            throw new IllegalArgumentException("No workspace topology planner registered for planner id " + plannerId);
        }
        return planner;
    }

    public MKWorkspaceTopologyPlanner towerPlanner() {
        return plannerFor(MKWorkspaceTopologyProfile.TOWER_PLANNER_ID);
    }
}
