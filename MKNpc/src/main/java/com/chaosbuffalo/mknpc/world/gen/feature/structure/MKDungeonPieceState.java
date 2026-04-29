package com.chaosbuffalo.mknpc.world.gen.feature.structure;

public record MKDungeonPieceState(
        int progressionFloorIndex,
        int verticalLevelIndex,
        int piecesOnFloor,
        int branchDepth,
        boolean onMainPath,
        int targetFloors,
        String category,
        int mainPathPiecesInCategory,
        int mainPathTargetInCategory
) {
    public MKDungeonPieceState(int progressionFloorIndex, int verticalLevelIndex, int piecesOnFloor,
                               int branchDepth, boolean onMainPath, int targetFloors) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                "", 0, 0);
    }
}
