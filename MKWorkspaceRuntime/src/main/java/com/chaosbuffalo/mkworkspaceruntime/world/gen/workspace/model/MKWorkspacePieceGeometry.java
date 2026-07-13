package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

public final class MKWorkspacePieceGeometry {
    public static final String TAG_TOWER_PIECE_KIND = "tower_piece_kind";
    public static final String TOWER_PIECE_KIND_EMBEDDED_STAIR = "embedded_stair";
    public static final String TOWER_PIECE_KIND_FLOOR_LINK_INSERT = "floor_link_insert";

    private MKWorkspacePieceGeometry() {
    }

    public static boolean isExactBoundsScaffold(MKWorkspacePieceDefinition piece) {
        String towerPieceKind = piece.tags().get(TAG_TOWER_PIECE_KIND);
        return TOWER_PIECE_KIND_EMBEDDED_STAIR.equals(towerPieceKind) ||
                TOWER_PIECE_KIND_FLOOR_LINK_INSERT.equals(towerPieceKind);
    }

    public static int effectiveShellMargin(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        return isExactBoundsScaffold(piece) ? 0 : Math.max(0, workspace.shellMargin());
    }

    public static int effectiveVerticalShellMargin(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        return isExactBoundsScaffold(piece) ? 0 : Math.max(0, workspace.verticalShellMargin());
    }

    public static int effectiveExteriorAirMargin(MKStructureWorkspace workspace, MKWorkspacePieceDefinition piece) {
        return isExactBoundsScaffold(piece) ? 0 : Math.max(0, workspace.exteriorAirMargin());
    }
}
