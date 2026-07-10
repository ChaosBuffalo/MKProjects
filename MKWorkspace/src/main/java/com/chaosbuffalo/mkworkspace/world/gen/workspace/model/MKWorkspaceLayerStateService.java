package com.chaosbuffalo.mkworkspace.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MKWorkspaceLayerStateService {
    public MKStructureWorkspace ensureLayerStates(MKStructureWorkspace workspace, long nowEpochMillis) {
        List<MKWorkspaceGeneratedLayerState> states = new ArrayList<>(workspace.layerStates());
        Set<MKWorkspaceGeneratedLayer> present = EnumSet.noneOf(MKWorkspaceGeneratedLayer.class);
        for (MKWorkspaceGeneratedLayerState state : states) {
            present.add(state.layer());
        }
        for (MKWorkspaceGeneratedLayer layer : MKWorkspaceGeneratedLayer.values()) {
            if (!present.contains(layer)) {
                states.add(MKWorkspaceGeneratedLayerState.unlocked(layer, 0L, nowEpochMillis));
            }
        }
        return workspace.withLayerStates(states);
    }

    public MKStructureWorkspace lockLayers(MKStructureWorkspace workspace, List<MKWorkspaceGeneratedLayer> layers) {
        return updateLockState(workspace, layers, true);
    }

    public MKStructureWorkspace unlockLayers(MKStructureWorkspace workspace, List<MKWorkspaceGeneratedLayer> layers) {
        return updateLockState(workspace, layers, false);
    }

    public MKStructureWorkspace applyInvalidation(MKStructureWorkspace workspace,
                                                  MKWorkspaceInvalidationReport report,
                                                  long nowEpochMillis) {
        MKStructureWorkspace updated = workspace;
        for (MKWorkspaceGeneratedLayer layer : report.invalidatedLayers()) {
            MKWorkspaceGeneratedLayerState state = updated.layerState(layer)
                    .orElseGet(() -> MKWorkspaceGeneratedLayerState.unlocked(layer, 0L, nowEpochMillis));
            updated = updated.withLayerState(state.markDirty());
        }
        return updated;
    }

    public MKStructureWorkspace refreshLayers(MKStructureWorkspace workspace,
                                              List<MKWorkspaceGeneratedLayer> layers,
                                              long sourceSettingsHash,
                                              long nowEpochMillis) {
        MKStructureWorkspace updated = workspace;
        for (MKWorkspaceGeneratedLayer layer : layers) {
            MKWorkspaceGeneratedLayerState state = updated.layerState(layer)
                    .orElseGet(() -> MKWorkspaceGeneratedLayerState.unlocked(layer, 0L, nowEpochMillis));
            updated = updated.withLayerState(state.refreshed(sourceSettingsHash, nowEpochMillis));
        }
        return updated;
    }

    public List<MKWorkspaceGeneratedLayer> staleLayers(MKStructureWorkspace workspace,
                                                       Map<MKWorkspaceGeneratedLayer, Long> expectedSourceHashes) {
        ArrayList<MKWorkspaceGeneratedLayer> staleLayers = new ArrayList<>();
        for (MKWorkspaceGeneratedLayer layer : MKWorkspaceGeneratedLayer.values()) {
            Long expectedHash = expectedSourceHashes.get(layer);
            if (expectedHash == null) {
                continue;
            }
            MKWorkspaceGeneratedLayerState state = workspace.layerState(layer).orElse(null);
            if (state == null || state.dirty() || state.sourceSettingsHash() != expectedHash) {
                staleLayers.add(layer);
            }
        }
        return List.copyOf(staleLayers);
    }

    private MKStructureWorkspace updateLockState(MKStructureWorkspace workspace,
                                                 List<MKWorkspaceGeneratedLayer> layers,
                                                 boolean locked) {
        MKStructureWorkspace updated = workspace;
        long now = System.currentTimeMillis();
        for (MKWorkspaceGeneratedLayer layer : layers) {
            MKWorkspaceGeneratedLayerState state = updated.layerState(layer)
                    .orElseGet(() -> MKWorkspaceGeneratedLayerState.unlocked(layer, 0L, now));
            updated = updated.withLayerState(state.withLocked(locked));
        }
        return updated;
    }
}
