package com.chaosbuffalo.mknpc.world.gen.feature.structure;

public record MKDungeonPieceState(
        int progressionFloorIndex,
        int verticalLevelIndex,
        int piecesOnFloor,
        int branchDepth,
        boolean onMainPath,
        int targetFloors,
        String topologyGroup,
        int mainPathPiecesInTopologyGroup,
        int mainPathTargetInTopologyGroup
) {
    public MKDungeonPieceState(int progressionFloorIndex, int verticalLevelIndex, int piecesOnFloor,
                               int branchDepth, boolean onMainPath, int targetFloors) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                "", 0, 0);
    }
}
