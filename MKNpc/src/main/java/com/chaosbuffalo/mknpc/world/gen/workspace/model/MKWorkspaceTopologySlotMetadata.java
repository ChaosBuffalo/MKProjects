package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;

public record MKWorkspaceTopologySlotMetadata(
        String topologySlotId,
        MKTowerWorkspaceCategory category,
        MKWorkspacePieceRole pieceRole,
        String roleKind,
        String pieceKind,
        boolean terminal,
        MKJigsawPieceRole jigsawPieceRole
) {
    public static MKWorkspaceTopologySlotMetadata fromFamily(MKTowerWorkspaceFamilyDefinition family) {
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(family.topologySlotId())
                .map(slot -> new MKWorkspaceTopologySlotMetadata(
                        family.topologySlotId(),
                        slot.category(),
                        slot.pieceRole(),
                        slot.roleKind(),
                        slot.pieceKind(),
                        slot.terminal(),
                        jigsawRoleFor(slot.pieceRole())))
                .orElseGet(() -> legacy(family));
    }

    private static MKWorkspaceTopologySlotMetadata legacy(MKTowerWorkspaceFamilyDefinition family) {
        return new MKWorkspaceTopologySlotMetadata(
                family.topologySlotId(),
                family.category(),
                family.pieceRole(),
                legacyRoleKind(family.pieceRole()),
                legacyPieceKind(family.pieceRole()),
                legacyTerminal(family.pieceRole()),
                jigsawRoleFor(family.pieceRole())
        );
    }

    private static String legacyRoleKind(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case TOP_CAP, BASEMENT_CAP -> "cap";
            case TOP_CAP_APPROACH, BASEMENT_CAP_APPROACH -> "cap_approach";
            case HALLWAY -> "linear_run";
            default -> "floor";
        };
    }

    private static String legacyPieceKind(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case TOP_CAP -> "top_cap";
            case BASEMENT_CAP -> "terminal_bottom";
            case HALLWAY -> "linear_run";
            default -> "room";
        };
    }

    private static boolean legacyTerminal(MKWorkspacePieceRole pieceRole) {
        return pieceRole == MKWorkspacePieceRole.TOP_CAP || pieceRole == MKWorkspacePieceRole.BASEMENT_CAP;
    }

    private static MKJigsawPieceRole jigsawRoleFor(MKWorkspacePieceRole pieceRole) {
        return switch (pieceRole) {
            case TOP_CAP -> MKJigsawPieceRole.TOP_CAP;
            case TOP_CAP_APPROACH -> MKJigsawPieceRole.TOP_CAP_APPROACH;
            case BASEMENT_CAP_APPROACH -> MKJigsawPieceRole.BASEMENT_CAP_APPROACH;
            case BASEMENT_CAP -> MKJigsawPieceRole.TERMINAL;
            default -> MKJigsawPieceRole.ROOM;
        };
    }
}
