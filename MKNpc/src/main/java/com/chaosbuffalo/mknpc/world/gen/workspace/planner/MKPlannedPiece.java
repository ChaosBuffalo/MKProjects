package com.chaosbuffalo.mknpc.world.gen.workspace.planner;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKTowerWorkspaceStackSlot;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceRole;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceRuntimePieceInfo;

import java.util.List;
import java.util.Map;

public record MKPlannedPiece(
        String roleId,
        String pieceName,
        int interiorWidth,
        int interiorLength,
        int interiorHeight,
        List<MKPlannedConnector> connectors,
        Map<String, String> tags
) {
    public MKPlannedPiece(MKWorkspacePieceRole role,
                          String pieceName,
                          int interiorWidth,
                          int interiorLength,
                          int interiorHeight,
                          List<MKPlannedConnector> connectors,
                          Map<String, String> tags) {
        this(role.getSerializedName(), pieceName, interiorWidth, interiorLength, interiorHeight, connectors, tags);
    }

    public MKWorkspacePieceRole role() {
        return legacyRole();
    }

    public MKWorkspacePieceRole legacyRole() {
        MKWorkspacePieceRole parsed = parseWorkspacePieceRole(roleId);
        if (parsed != null) {
            return parsed;
        }
        String topologySlotId = tags.getOrDefault("workspace_topology_slot_id", roleId);
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
                .map(MKPlannedPiece::legacyRoleForStackSlot)
                .orElseGet(this::legacyRoleFromTags);
    }

    private MKWorkspacePieceRole legacyRoleFromTags() {
        if ("linear_run".equals(tags.get("tower_piece_kind"))) {
            return MKWorkspacePieceRole.HALLWAY;
        }
        if (roleId.endsWith(".entry") || roleId.endsWith("_entry") || roleId.contains(".gate.")) {
            return MKWorkspacePieceRole.ENTRY;
        }
        if (roleId.endsWith(".top_cap") || roleId.endsWith("_top_cap")) {
            return MKWorkspacePieceRole.TOP_CAP;
        }
        if (roleId.endsWith(".top_cap_approach") || roleId.endsWith("_top_cap_approach")) {
            return MKWorkspacePieceRole.TOP_CAP_APPROACH;
        }
        if (roleId.endsWith(".basement_cap") || roleId.endsWith("_basement_cap")) {
            return MKWorkspacePieceRole.BASEMENT_CAP;
        }
        if (roleId.endsWith(".basement_cap_approach") || roleId.endsWith("_basement_cap_approach")) {
            return MKWorkspacePieceRole.BASEMENT_CAP_APPROACH;
        }
        return MKWorkspaceRuntimePieceInfo.fromTags(tags)
                .map(MKWorkspaceRuntimePieceInfo::role)
                .map(this::legacyRoleForJigsawRole)
                .orElse(MKWorkspacePieceRole.FLOOR_MAIN);
    }

    private MKWorkspacePieceRole legacyRoleForJigsawRole(MKJigsawPieceRole role) {
        return switch (role) {
            case TOP_CAP -> MKWorkspacePieceRole.TOP_CAP;
            case TOP_CAP_APPROACH -> MKWorkspacePieceRole.TOP_CAP_APPROACH;
            case BASEMENT_CAP_APPROACH -> MKWorkspacePieceRole.BASEMENT_CAP_APPROACH;
            case TERMINAL -> MKWorkspacePieceRole.BASEMENT_CAP;
            default -> MKWorkspacePieceRole.FLOOR_MAIN;
        };
    }

    private static MKWorkspacePieceRole legacyRoleForStackSlot(MKTowerWorkspaceStackSlot slot) {
        return switch (slot) {
            case ENTRY -> MKWorkspacePieceRole.ENTRY;
            case MAIN_FLOOR -> MKWorkspacePieceRole.FLOOR_MAIN;
            case TOP_CAP_APPROACH -> MKWorkspacePieceRole.TOP_CAP_APPROACH;
            case TOP_CAP -> MKWorkspacePieceRole.TOP_CAP;
            case BASEMENT_ENTRY -> MKWorkspacePieceRole.BASEMENT_ENTRY;
            case BASEMENT_FLOOR -> MKWorkspacePieceRole.BASEMENT_MAIN;
            case BASEMENT_CAP_APPROACH -> MKWorkspacePieceRole.BASEMENT_CAP_APPROACH;
            case BASEMENT_CAP -> MKWorkspacePieceRole.BASEMENT_CAP;
        };
    }

    private static MKWorkspacePieceRole parseWorkspacePieceRole(String roleId) {
        for (MKWorkspacePieceRole role : MKWorkspacePieceRole.values()) {
            if (role.getSerializedName().equals(roleId)) {
                return role;
            }
        }
        return null;
    }
}
