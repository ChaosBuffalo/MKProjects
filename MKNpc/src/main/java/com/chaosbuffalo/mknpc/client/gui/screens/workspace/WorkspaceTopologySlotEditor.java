package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.client.gui.screens.MKWorkspaceScreen;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import net.minecraft.resources.ResourceLocation;

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
        return screen.selectedCategoryPieces();
    }

    public void clearSelection() {
        screen.clearSelectedTopologyKey();
    }

    public void ensureOverridesInitialized() {
        screen.ensureCategoryOverridesInitialized();
    }

    public void resetOverrides() {
        screen.resetCategoryOverrides();
    }

    public int hallwayWidth() {
        return screen.categoryHallwayWidth();
    }

    public MKWorkspaceStairMode stairMode() {
        return screen.categoryStairMode();
    }

    public void stairMode(MKWorkspaceStairMode value) {
        screen.categoryStairMode(value);
    }

    public MKWorkspaceStairRiseType stairRiseType() {
        return screen.categoryStairRiseType();
    }

    public void stairRiseType(MKWorkspaceStairRiseType value) {
        screen.categoryStairRiseType(value);
    }

    public int stairWidth() {
        return screen.categoryStairWidth();
    }

    public void stairWidth(int value) {
        screen.categoryStairWidth(value);
    }

    public ResourceLocation stairBlock() {
        return screen.categoryStairBlock();
    }

    public void stairBlock(ResourceLocation value) {
        screen.categoryStairBlock(value);
    }

    public ResourceLocation slabBlock() {
        return screen.categorySlabBlock();
    }

    public void slabBlock(ResourceLocation value) {
        screen.categorySlabBlock(value);
    }

    public ResourceLocation ladderBlock() {
        return screen.categoryLadderBlock();
    }

    public void ladderBlock(ResourceLocation value) {
        screen.categoryLadderBlock(value);
    }
}
