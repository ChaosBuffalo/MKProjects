package com.chaosbuffalo.mkworkspace.client.gui.screens.workspace;

import com.chaosbuffalo.mkworkspace.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceStairRiseType;

import java.util.List;

public class WorkspaceTopologySlotEditor {
    private final MKWorkspaceScreen screen;

    public WorkspaceTopologySlotEditor(MKWorkspaceScreen screen) {
        this.screen = screen;
    }

    public String selectedTopologyKey() {
        return screen.selectedTopologyKey();
    }

    public List<MKWorkspacePieceDefinition> selectedPieces() {
        return screen.selectedTopologySlotPieces();
    }

    public void clearSelection() {
        screen.clearSelectedTopologyKey();
    }

    public void ensureOverridesInitialized() {
        screen.ensureTopologySlotOverridesInitialized();
    }

    public void resetOverrides() {
        screen.resetTopologySlotOverrides();
    }

    public int shaftWidth() {
        return screen.topologySlotShaftWidth();
    }

    public MKWorkspaceStairMode stairMode() {
        return screen.topologySlotStairMode();
    }

    public void stairMode(MKWorkspaceStairMode value) {
        screen.topologySlotStairMode(value);
    }

    public MKWorkspaceStairRiseType stairRiseType() {
        return screen.topologySlotStairRiseType();
    }

    public void stairRiseType(MKWorkspaceStairRiseType value) {
        screen.topologySlotStairRiseType(value);
    }

    public int stairWidth() {
        return screen.topologySlotStairWidth();
    }

    public void stairWidth(int value) {
        screen.topologySlotStairWidth(value);
    }

}
