package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.structure.runtime.layout.MKInsertFamilyPools;

import java.util.Map;

public final class MKWorkspacePieceGeometry {
    public static final String TAG_TOWER_PIECE_KIND = "tower_piece_kind";
    public static final String TOWER_PIECE_KIND_EMBEDDED_STAIR = "embedded_stair";
    public static final String TOWER_PIECE_KIND_FLOOR_LINK_INSERT = "floor_link_insert";

    private MKWorkspacePieceGeometry() {
    }

    public static boolean isExactBoundsScaffold(MKWorkspacePieceDefinition piece) {
        return isExactBoundsScaffold(piece.tags());
    }

    public static boolean isExactBoundsScaffold(Map<String, String> tags) {
        String towerPieceKind = tags.get(TAG_TOWER_PIECE_KIND);
        return TOWER_PIECE_KIND_EMBEDDED_STAIR.equals(towerPieceKind) ||
                TOWER_PIECE_KIND_FLOOR_LINK_INSERT.equals(towerPieceKind) ||
                isCourtyardSocketInsert(tags);
    }

    public static boolean isEmptyScaffold(Map<String, String> tags) {
        String towerPieceKind = tags.get(TAG_TOWER_PIECE_KIND);
        return TOWER_PIECE_KIND_EMBEDDED_STAIR.equals(towerPieceKind) ||
                isCourtyardSocketInsert(tags);
    }

    public static boolean isFloorLinkInsert(Map<String, String> tags) {
        return TOWER_PIECE_KIND_FLOOR_LINK_INSERT.equals(tags.get(TAG_TOWER_PIECE_KIND));
    }

    public static boolean isCourtyardSocketInsert(Map<String, String> tags) {
        return MKWorkspaceInsertFamilyKind.COURTYARD_SOCKET.getSerializedName()
                .equals(tags.get(MKInsertFamilyPools.TAG_INSERT_FAMILY_KIND));
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
