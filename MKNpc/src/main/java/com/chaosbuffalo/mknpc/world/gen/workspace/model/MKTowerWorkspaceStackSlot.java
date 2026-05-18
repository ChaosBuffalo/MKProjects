package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.List;
import java.util.Optional;

public enum MKTowerWorkspaceStackSlot {
    BASEMENT_CAP("basement_cap", MKTowerWorkspaceCategory.BASEMENT_CAP,
            MKWorkspacePieceRole.BASEMENT_CAP, "cap", "terminal_bottom", true),
    BASEMENT_CAP_APPROACH("basement_cap_approach", MKTowerWorkspaceCategory.BASEMENT_CAP,
            MKWorkspacePieceRole.BASEMENT_CAP_APPROACH, "cap_approach", "room", false),
    BASEMENT_ENTRY("basement_entry", MKTowerWorkspaceCategory.BASEMENT,
            MKWorkspacePieceRole.BASEMENT_ENTRY, "floor", "room", false),
    BASEMENT_FLOOR("basement_floor", MKTowerWorkspaceCategory.BASEMENT,
            MKWorkspacePieceRole.BASEMENT_MAIN, "floor", "room", false),
    ENTRY("entry", MKTowerWorkspaceCategory.ENTRY,
            MKWorkspacePieceRole.ENTRY, "floor", "room", false),
    MAIN_FLOOR("main_floor", MKTowerWorkspaceCategory.MAIN,
            MKWorkspacePieceRole.FLOOR_MAIN, "floor", "room", false),
    TOP_CAP_APPROACH("top_cap_approach", MKTowerWorkspaceCategory.TOP_CAP,
            MKWorkspacePieceRole.TOP_CAP_APPROACH, "cap_approach", "room", false),
    TOP_CAP("top_cap", MKTowerWorkspaceCategory.TOP_CAP,
            MKWorkspacePieceRole.TOP_CAP, "cap", "top_cap", true);

    private static final List<MKTowerWorkspaceStackSlot> SCHEMA_ORDER = List.of(
            BASEMENT_CAP,
            BASEMENT_CAP_APPROACH,
            BASEMENT_ENTRY,
            BASEMENT_FLOOR,
            ENTRY,
            MAIN_FLOOR,
            TOP_CAP_APPROACH,
            TOP_CAP
    );
    private static final List<MKTowerWorkspaceStackSlot> FAMILY_DEFAULT_ORDER = List.of(
            ENTRY,
            MAIN_FLOOR,
            TOP_CAP_APPROACH,
            TOP_CAP,
            BASEMENT_ENTRY,
            BASEMENT_FLOOR,
            BASEMENT_CAP_APPROACH,
            BASEMENT_CAP
    );

    private final String suffix;
    private final MKTowerWorkspaceCategory category;
    private final MKWorkspacePieceRole pieceRole;
    private final String roleKind;
    private final String pieceKind;
    private final boolean terminal;

    MKTowerWorkspaceStackSlot(String suffix, MKTowerWorkspaceCategory category,
                              MKWorkspacePieceRole pieceRole, String roleKind,
                              String pieceKind, boolean terminal) {
        this.suffix = suffix;
        this.category = category;
        this.pieceRole = pieceRole;
        this.roleKind = roleKind;
        this.pieceKind = pieceKind;
        this.terminal = terminal;
    }

    public String suffix() {
        return suffix;
    }

    public MKTowerWorkspaceCategory category() {
        return category;
    }

    public MKWorkspacePieceRole pieceRole() {
        return pieceRole;
    }

    public String roleKind() {
        return roleKind;
    }

    public String pieceKind() {
        return pieceKind;
    }

    public boolean terminal() {
        return terminal;
    }

    public String slotId(String stackId) {
        return stackId + "." + suffix;
    }

    public String baseName(String basePrefix) {
        return basePrefix + "_" + suffix;
    }

    public static List<MKTowerWorkspaceStackSlot> schemaOrder() {
        return SCHEMA_ORDER;
    }

    public static List<MKTowerWorkspaceStackSlot> familyDefaultOrder() {
        return FAMILY_DEFAULT_ORDER;
    }

    public static Optional<MKTowerWorkspaceStackSlot> fromTopologySlotId(String topologySlotId) {
        if (topologySlotId == null || topologySlotId.isBlank()) {
            return Optional.empty();
        }
        for (MKTowerWorkspaceStackSlot slot : values()) {
            if (topologySlotId.endsWith("." + slot.suffix)) {
                String stackId = topologySlotId.substring(0, topologySlotId.length() - slot.suffix.length() - 1);
                if (!stackId.isBlank()) {
                    return Optional.of(slot);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<String> stackIdForTopologySlot(String topologySlotId) {
        return fromTopologySlotId(topologySlotId)
                .map(slot -> topologySlotId.substring(0, topologySlotId.length() - slot.suffix.length() - 1));
    }
}
