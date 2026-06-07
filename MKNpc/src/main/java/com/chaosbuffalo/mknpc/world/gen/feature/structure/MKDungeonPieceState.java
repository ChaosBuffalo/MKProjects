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
        int mainPathTargetInTopologyGroup,
        String towerStackId,
        String towerStackSlot,
        int towerStackMainTargetFloors,
        int towerStackMainPlacedFloors,
        int towerStackBasementTargetFloors,
        int towerStackBasementPlacedFloors,
        boolean towerStackTopCapApproachEnabled,
        boolean towerStackBasementEntryEnabled,
        boolean towerStackBasementCapApproachEnabled
) {
    public MKDungeonPieceState(int progressionFloorIndex, int verticalLevelIndex, int piecesOnFloor,
                               int branchDepth, boolean onMainPath, int targetFloors) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                "", 0, 0, "", "", 0, 0, 0, 0, true, true, false);
    }

    public MKDungeonPieceState(int progressionFloorIndex, int verticalLevelIndex, int piecesOnFloor,
                               int branchDepth, boolean onMainPath, int targetFloors,
                               String topologyGroup, int mainPathPiecesInTopologyGroup,
                               int mainPathTargetInTopologyGroup) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                topologyGroup, mainPathPiecesInTopologyGroup, mainPathTargetInTopologyGroup,
                "", "", 0, 0, 0, 0, true, true, false);
    }
}
