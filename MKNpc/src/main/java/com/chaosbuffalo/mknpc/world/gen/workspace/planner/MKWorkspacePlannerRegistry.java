package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceLinearRunFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMaterialPalette;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRoomFamilyDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTopologyProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MKWorkspacePlannerRegistry {
    private static final MKWorkspacePlannerRegistry SHARED = new MKWorkspacePlannerRegistry();
    private static final ResourceLocation DEFAULT_PLANNER_ID = MKTowerWorkspacePlanner.PLANNER_ID;

    static {
        registerBuiltIns(SHARED);
    }

    private final Map<ResourceLocation, MKWorkspacePlanner> planners = new LinkedHashMap<>();

    public MKWorkspacePlannerRegistry() {
    }

    public static MKWorkspacePlannerRegistry shared() {
        return SHARED;
    }

    public static void registerShared(MKWorkspacePlanner planner) {
        SHARED.register(planner);
    }

    private static void registerBuiltIns(MKWorkspacePlannerRegistry registry) {
        registry.register(new MKTowerWorkspacePlanner());
        registry.register(new MKWalledKeepWorkspacePlanner());
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
            throw new IllegalArgumentException("No workspace planner registered for planner id " + plannerId);
        }
        return planner;
    }

    public MKWorkspacePlanner defaultPlanner() {
        return plannerFor(DEFAULT_PLANNER_ID);
    }

    public MKWorkspaceTopologyProfile defaultTopologyProfile() {
        return defaultPlanner().createDefaultTopologyProfile();
    }

    public List<MKWorkspaceRoomFamilyDefinition> defaultRoomFamilyDefinitions(MKWorkspaceDimensions dimensions) {
        return defaultPlanner().createDefaultRoomFamilyDefinitions(dimensions);
    }

    public List<MKWorkspaceLinearRunFamilyDefinition> defaultLinearRunFamilyDefinitions(
            MKWorkspaceDimensions dimensions, MKWorkspaceMaterialPalette palette) {
        return defaultPlanner().createDefaultLinearRunFamilyDefinitions(dimensions, palette);
    }

    public List<String> validate(MKStructureWorkspace workspace) {
        ArrayList<String> errors = new ArrayList<>(workspace.validate());
        errors.addAll(plannerFor(workspace).validateTopology(workspace));
        return List.copyOf(errors);
    }
}
