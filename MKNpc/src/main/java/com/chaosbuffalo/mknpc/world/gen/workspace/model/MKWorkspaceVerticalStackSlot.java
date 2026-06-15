package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import java.util.List;
import java.util.Optional;

public enum MKWorkspaceVerticalStackSlot {
    BASEMENT_CAP("basement_cap", "basement_cap",
            "cap", "terminal_bottom", true),
    BASEMENT_CAP_APPROACH("basement_cap_approach", "basement_cap",
            "cap_approach", "room", false),
    BASEMENT_ENTRY("basement_entry", "basement",
            "floor", "room", false),
    BASEMENT_FLOOR("basement_floor", "basement",
            "floor", "room", false),
    ENTRY("entry", "entry",
            "floor", "room", false),
    MAIN_FLOOR("main_floor", "main",
            "floor", "room", false),
    TOP_CAP_APPROACH("top_cap_approach", "top_cap",
            "cap_approach", "room", false),
    TOP_CAP("top_cap", "top_cap",
            "cap", "top_cap", true);

    private static final List<MKWorkspaceVerticalStackSlot> SCHEMA_ORDER = List.of(
            BASEMENT_CAP,
            BASEMENT_CAP_APPROACH,
            BASEMENT_ENTRY,
            BASEMENT_FLOOR,
            ENTRY,
            MAIN_FLOOR,
            TOP_CAP_APPROACH,
            TOP_CAP
    );
    private static final List<MKWorkspaceVerticalStackSlot> FAMILY_DEFAULT_ORDER = List.of(
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
    private final String topologyGroupId;
    private final String roleKind;
    private final String pieceKind;
    private final boolean terminal;

    MKWorkspaceVerticalStackSlot(String suffix, String topologyGroupId,
                              String roleKind,
                              String pieceKind, boolean terminal) {
        this.suffix = suffix;
        this.topologyGroupId = topologyGroupId;
        this.roleKind = roleKind;
        this.pieceKind = pieceKind;
        this.terminal = terminal;
    }

    public String suffix() {
        return suffix;
    }

    public String topologyGroupId() {
        return topologyGroupId;
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

    public static List<MKWorkspaceVerticalStackSlot> schemaOrder() {
        return SCHEMA_ORDER;
    }

    public static List<MKWorkspaceVerticalStackSlot> familyDefaultOrder() {
        return FAMILY_DEFAULT_ORDER;
    }

    public static Optional<MKWorkspaceVerticalStackSlot> fromTopologySlotId(String topologySlotId) {
        if (topologySlotId == null || topologySlotId.isBlank()) {
            return Optional.empty();
        }
        for (MKWorkspaceVerticalStackSlot slot : values()) {
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
