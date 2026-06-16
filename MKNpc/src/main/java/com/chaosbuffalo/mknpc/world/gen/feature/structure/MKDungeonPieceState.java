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
        String verticalStackId,
        String verticalStackSlot,
        int verticalStackMainTargetFloors,
        int verticalStackMainPlacedFloors,
        int verticalStackBasementTargetFloors,
        int verticalStackBasementPlacedFloors,
        boolean verticalStackTopCapApproachEnabled,
        boolean verticalStackBasementEntryEnabled,
        boolean verticalStackBasementCapApproachEnabled,
        String floorExitMask
) {
    public MKDungeonPieceState {
        floorExitMask = floorExitMask == null ? "" : floorExitMask;
    }

    public MKDungeonPieceState(int progressionFloorIndex, int verticalLevelIndex, int piecesOnFloor,
                               int branchDepth, boolean onMainPath, int targetFloors) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                "", 0, 0, "", "", 0, 0, 0, 0, true, true, false, "");
    }

    public MKDungeonPieceState(int progressionFloorIndex, int verticalLevelIndex, int piecesOnFloor,
                               int branchDepth, boolean onMainPath, int targetFloors,
                               String topologyGroup, int mainPathPiecesInTopologyGroup,
                               int mainPathTargetInTopologyGroup) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                topologyGroup, mainPathPiecesInTopologyGroup, mainPathTargetInTopologyGroup,
                "", "", 0, 0, 0, 0, true, true, false, "");
    }

    public MKDungeonPieceState(int progressionFloorIndex,
                               int verticalLevelIndex,
                               int piecesOnFloor,
                               int branchDepth,
                               boolean onMainPath,
                               int targetFloors,
                               String topologyGroup,
                               int mainPathPiecesInTopologyGroup,
                               int mainPathTargetInTopologyGroup,
                               String verticalStackId,
                               String verticalStackSlot,
                               int verticalStackMainTargetFloors,
                               int verticalStackMainPlacedFloors,
                               int verticalStackBasementTargetFloors,
                               int verticalStackBasementPlacedFloors,
                               boolean verticalStackTopCapApproachEnabled,
                               boolean verticalStackBasementEntryEnabled,
                               boolean verticalStackBasementCapApproachEnabled) {
        this(progressionFloorIndex, verticalLevelIndex, piecesOnFloor, branchDepth, onMainPath, targetFloors,
                topologyGroup, mainPathPiecesInTopologyGroup, mainPathTargetInTopologyGroup, verticalStackId,
                verticalStackSlot, verticalStackMainTargetFloors, verticalStackMainPlacedFloors,
                verticalStackBasementTargetFloors, verticalStackBasementPlacedFloors, verticalStackTopCapApproachEnabled,
                verticalStackBasementEntryEnabled, verticalStackBasementCapApproachEnabled, "");
    }
}
