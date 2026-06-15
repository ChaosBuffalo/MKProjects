package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKWorkspacePlannerRegistry {
    private final Map<ResourceLocation, MKWorkspacePlanner> planners = new LinkedHashMap<>();

    public MKWorkspacePlannerRegistry() {
    }

    public static MKWorkspacePlannerRegistry withBuiltIns() {
        MKWorkspacePlannerRegistry registry = new MKWorkspacePlannerRegistry();
        registry.register(new MKTowerWorkspacePlanner());
        registry.register(new MKWalledKeepWorkspacePlanner());
        return registry;
    }

    public MKWorkspacePlannerRegistry registerBuiltIns() {
        register(new MKTowerWorkspacePlanner());
        register(new MKWalledKeepWorkspacePlanner());
        return this;
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
        return plannerFor(MKTowerWorkspacePlanner.PLANNER_ID);
    }

    public List<String> validate(MKStructureWorkspace workspace) {
        ArrayList<String> errors = new ArrayList<>(workspace.validate());
        errors.addAll(plannerFor(workspace).validateTopology(workspace));
        return List.copyOf(errors);
    }
}
