package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MKWorkspacePlannerRegistry {
    private final Map<ResourceLocation, MKWorkspacePlanner> planners = new LinkedHashMap<>();

    public MKWorkspacePlannerRegistry() {
        register(new MKTowerWorkspacePlanner());
        register(new MKWalledKeepWorkspacePlanner());
    }

    public void register(MKWorkspacePlanner planner) {
        planners.put(planner.plannerId(), planner);
    }

    public MKWorkspacePlanner plannerFor(MKStructureWorkspace workspace) {
        return plannerFor(workspace.topologyProfile().plannerId());
    }

    public MKWorkspacePlanner plannerFor(ResourceLocation plannerId) {
        MKWorkspacePlanner planner = planners.get(plannerId);
        if (planner == null) {
            throw new IllegalArgumentException("No workspace topology planner registered for planner id " + plannerId);
        }
        return planner;
    }

    public MKWorkspacePlanner towerPlanner() {
        return plannerFor(MKWorkspaceTopologyProfile.TOWER_PLANNER_ID);
    }
}
