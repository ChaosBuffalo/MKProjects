package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.structure.runtime.layout.MKFloorTopologySettings;
public class MKWorkspaceFloorTopologyMutationPreflightService {
    private final MKWorkspaceFloorTopologyInvalidationAnalyzer invalidationAnalyzer;
    private final MKWorkspaceLayerStateService layerStateService;

    public MKWorkspaceFloorTopologyMutationPreflightService() {
        this(new MKWorkspaceFloorTopologyInvalidationAnalyzer(), new MKWorkspaceLayerStateService());
    }

    public MKWorkspaceFloorTopologyMutationPreflightService(
            MKWorkspaceFloorTopologyInvalidationAnalyzer invalidationAnalyzer,
            MKWorkspaceLayerStateService layerStateService) {
        this.invalidationAnalyzer = invalidationAnalyzer;
        this.layerStateService = layerStateService;
    }

    public MKWorkspaceMutationPreflight preflight(MKStructureWorkspace workspace,
                                                  MKWorkspacePlannerId floorPlannerId,
                                                  MKFloorTopologySettings previous,
                                                  MKFloorTopologySettings updated,
                                                  long nowEpochMillis) {
        MKWorkspaceInvalidationReport report = invalidationAnalyzer.analyze(floorPlannerId, previous, updated);
        MKStructureWorkspace workspaceWithLayerStates = layerStateService.ensureLayerStates(workspace, nowEpochMillis);
        MKStructureWorkspace workspaceWithDirtyLayers = layerStateService.applyInvalidation(
                workspaceWithLayerStates, report, nowEpochMillis);
        return new MKWorkspaceMutationPreflight(report, workspaceWithDirtyLayers);
    }
}
