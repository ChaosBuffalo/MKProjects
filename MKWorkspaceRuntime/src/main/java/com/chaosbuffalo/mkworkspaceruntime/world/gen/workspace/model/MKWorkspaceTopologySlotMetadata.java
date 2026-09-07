package com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.feature.structure.MKJigsawPieceRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record MKWorkspaceTopologySlotMetadata(
        String topologySlotId,
        String topologyGroupId,
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

    public static MKWorkspaceTopologySlotMetadata fromFamily(MKWorkspaceRoomFamilyDefinition family) {
        return fromVerticalStackTopologySlotId(family.sourceTopologySlotIdOrSelf())
                .orElse(family.slotMetadata())
                .withTopologySlotId(family.topologySlotId());
    }

    public static MKWorkspaceTopologySlotMetadata fromTopologySlotId(String topologySlotId) {
        return fromTopologyRole(topologySlotId, "floor", "room", false);
    }

    public static MKWorkspaceTopologySlotMetadata fromVerticalStackSlot(MKWorkspaceVerticalStackSlot slot, String stackId) {
        return new MKWorkspaceTopologySlotMetadata(
                slot.slotId(stackId),
                slot.topologyGroupId(),
                slot.roleKind(),
                slot.pieceKind(),
                slot.terminal(),
                jigsawRoleFor(slot.roleKind(), slot.pieceKind()));
    }

    public static Optional<MKWorkspaceTopologySlotMetadata> fromVerticalStackTopologySlotId(String topologySlotId) {
        Optional<MKWorkspaceVerticalStackSlot> slot = MKWorkspaceVerticalStackSlot.fromTopologySlotId(topologySlotId);
        Optional<String> stackId = MKWorkspaceVerticalStackSlot.stackIdForTopologySlot(topologySlotId);
        if (slot.isEmpty() || stackId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromVerticalStackSlot(slot.get(), stackId.get()));
    }

    public static MKWorkspaceTopologySlotMetadata explicit(String topologySlotId, String roleKind,
                                                           String pieceKind, boolean terminal) {
        return fromTopologyRole(topologySlotId, roleKind, pieceKind, terminal);
    }

    public static MKWorkspaceTopologySlotMetadata fromTopologyRole(String topologySlotId, String roleKind,
                                                                   String pieceKind, boolean terminal) {
        return new MKWorkspaceTopologySlotMetadata(
                topologySlotId,
                topologyGroupForRole(roleKind, pieceKind),
                roleKind,
                pieceKind,
                terminal,
                jigsawRoleFor(roleKind, pieceKind));
    }

    public MKWorkspaceTopologySlotMetadata withTopologySlotId(String topologySlotId) {
        return new MKWorkspaceTopologySlotMetadata(topologySlotId, topologyGroupId, roleKind, pieceKind, terminal,
                jigsawPieceRole);
    }

    private static String topologyGroupForRole(String roleKind, String pieceKind) {
        return switch (roleKind) {
            case "entry" -> "entry";
            case "cap", "cap_approach" -> "terminal_bottom".equals(pieceKind) ?
                    "basement_cap" : "top_cap";
            default -> "main";
        };
    }

    private static MKJigsawPieceRole jigsawRoleFor(String roleKind, String pieceKind) {
        return switch (roleKind) {
            case "cap" -> "terminal_bottom".equals(pieceKind) ? MKJigsawPieceRole.TERMINAL : MKJigsawPieceRole.TOP_CAP;
            case "cap_approach" -> "terminal_bottom".equals(pieceKind) ?
                    MKJigsawPieceRole.BASEMENT_CAP_APPROACH : MKJigsawPieceRole.TOP_CAP_APPROACH;
            default -> MKJigsawPieceRole.ROOM;
        };
    }
}
