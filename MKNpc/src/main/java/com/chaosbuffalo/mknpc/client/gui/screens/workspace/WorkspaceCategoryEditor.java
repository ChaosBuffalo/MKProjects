package com.chaosbuffalo.mknpc.client.gui.screens.workspace;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairMode;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceStairRiseType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface WorkspaceCategoryEditor {
    String selectedTopologyKey();

    List<MKWorkspacePieceDefinition> selectedPieces();

    void clearSelection();

    void ensureOverridesInitialized();

    void resetOverrides();

    int hallwayWidth();

    MKWorkspaceStairMode stairMode();

    void stairMode(MKWorkspaceStairMode value);

    MKWorkspaceStairRiseType stairRiseType();

    void stairRiseType(MKWorkspaceStairRiseType value);

    int stairWidth();

    void stairWidth(int value);

    ResourceLocation stairBlock();

    void stairBlock(ResourceLocation value);

    ResourceLocation slabBlock();

    void slabBlock(ResourceLocation value);

    ResourceLocation ladderBlock();

    void ladderBlock(ResourceLocation value);
}
