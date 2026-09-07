package com.chaosbuffalo.mkworkspace.world.gen.workspace.planner;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKStructureWorkspace;

import java.util.List;

public interface MKWorkspacePiecePlanner {
    List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace);
}
