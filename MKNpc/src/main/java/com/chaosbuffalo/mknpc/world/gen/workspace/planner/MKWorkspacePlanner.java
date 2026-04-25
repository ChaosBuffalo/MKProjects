package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;

import java.util.List;

public interface MKWorkspacePlanner {
    List<MKPlannedPiece> createCanonicalPieces(MKStructureWorkspace workspace);
}
