package com.chaosbuffalo.mknpc.world.gen.feature.structure;

public record MKDungeonPieceState(
        int progressionFloorIndex,
        int verticalLevelIndex,
        int piecesOnFloor,
        int branchDepth,
        boolean onMainPath,
        int targetFloors
) {
}
