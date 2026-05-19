package com.chaosbuffalo.mknpc.world.gen.workspace.model;

import com.chaosbuffalo.mknpc.world.gen.feature.structure.MKJigsawPieceRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MKWorkspaceTopologySlotMetadata(
        String topologySlotId,
        MKTowerWorkspaceCategory category,
        MKWorkspacePieceRole pieceRole,
        String roleKind,
        String pieceKind,
        boolean terminal,
        MKJigsawPieceRole jigsawPieceRole
) {
    public static final Codec<MKWorkspaceTopologySlotMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("topology_slot_id").forGetter(MKWorkspaceTopologySlotMetadata::topologySlotId),
            Codec.STRING.fieldOf("role_kind").forGetter(MKWorkspaceTopologySlotMetadata::roleKind),
            Codec.STRING.fieldOf("piece_kind").forGetter(MKWorkspaceTopologySlotMetadata::pieceKind),
            Codec.BOOL.fieldOf("terminal").forGetter(MKWorkspaceTopologySlotMetadata::terminal)
    ).apply(instance, MKWorkspaceTopologySlotMetadata::fromTopologyRole));

    public static MKWorkspaceTopologySlotMetadata fromFamily(MKTowerWorkspaceFamilyDefinition family) {
        return family.slotMetadata();
    }

    public static MKWorkspaceTopologySlotMetadata fromTopologySlotId(String topologySlotId) {
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
                .map(slot -> new MKWorkspaceTopologySlotMetadata(
                        topologySlotId,
                        slot.category(),
                        slot.pieceRole(),
                        slot.roleKind(),
                        slot.pieceKind(),
                        slot.terminal(),
                        jigsawRoleFor(slot.pieceRole())))
                .orElseGet(() -> fromTopologyRole(topologySlotId, "floor", "room", false));
    }

    public static MKWorkspaceTopologySlotMetadata fromTowerStackSlot(MKTowerWorkspaceStackSlot slot, String stackId) {
        return fromTopologySlotId(slot.slotId(stackId));
    }

    public static MKWorkspaceTopologySlotMetadata explicit(String topologySlotId, String roleKind,
                                                           String pieceKind, boolean terminal) {
        return fromTopologyRole(topologySlotId, roleKind, pieceKind, terminal);
    }

    public static MKWorkspaceTopologySlotMetadata fromTopologyRole(String topologySlotId, String roleKind,
                                                                   String pieceKind, boolean terminal) {
        return MKTowerWorkspaceStackSlot.fromTopologySlotId(topologySlotId)
                .map(slot -> new MKWorkspaceTopologySlotMetadata(
                        topologySlotId,
                        slot.category(),
                        slot.pieceRole(),
                        slot.roleKind(),
                        slot.pieceKind(),
                        slot.terminal(),
                        jigsawRoleFor(slot.pieceRole())))
                .orElseGet(() -> {
                    MKWorkspacePieceRole pieceRole = pieceRoleForTopologyRole(roleKind, pieceKind);
                    return new MKWorkspaceTopologySlotMetadata(
                            topologySlotId,
                            categoryForTopologyRole(roleKind, pieceKind),
                            pieceRole,
                            roleKind,
                            pieceKind,
                            terminal,
                            jigsawRoleFor(pieceRole));
                });
    }

    public MKWorkspaceTopologySlotMetadata withTopologySlotId(String topologySlotId) {
        return fromTopologyRole(topologySlotId, roleKind, pieceKind, terminal);
    }

    private static MKTowerWorkspaceCategory categoryForTopologyRole(String roleKind, String pieceKind) {
        return switch (roleKind) {
            case "entry" -> MKTowerWorkspaceCategory.ENTRY;
            case "cap", "cap_approach" -> "terminal_bottom".equals(pieceKind) ?
                    MKTowerWorkspaceCategory.BASEMENT_CAP : MKTowerWorkspaceCategory.TOP_CAP;
            default -> MKTowerWorkspaceCategory.MAIN;
        };
    }

    private static MKWorkspacePieceRole pieceRoleForTopologyRole(String roleKind, String pieceKind) {
        return switch (roleKind) {
            case "entry" -> MKWorkspacePieceRole.ENTRY;
            case "cap" -> "terminal_bottom".equals(pieceKind) ?
                    MKWorkspacePieceRole.BASEMENT_CAP : MKWorkspacePieceRole.TOP_CAP;
            case "cap_approach" -> "terminal_bottom".equals(pieceKind) ?
                    MKWorkspacePieceRole.BASEMENT_CAP_APPROACH : MKWorkspacePieceRole.TOP_CAP_APPROACH;
            default -> MKWorkspacePieceRole.FLOOR_MAIN;
        };
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
