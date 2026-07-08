package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.MKNpc;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

final class MKWorkspacePacketPayloads {
    private static final int LARGE_PAYLOAD_WARNING_BYTES = 900_000;
    private static final int MAX_WORKSPACE_PAYLOAD_BYTES = 983_040;

    private MKWorkspacePacketPayloads() {
    }

    static CompoundTag editableWorkspaceTag(MKStructureWorkspace workspace) {
        return withPiecesPreservingMetadata(workspace, List.of()).toTag();
    }

    static CompoundTag screenWorkspaceTag(MKStructureWorkspace workspace) {
        List<MKWorkspacePieceDefinition> physicalPieces = physicalAuthoringPieces(workspace);
        CompoundTag tag = withPiecesPreservingMetadata(workspace, physicalPieces).toTag();
        int encodedBytes = encodedNbtBytes(tag);
        if (encodedBytes < MAX_WORKSPACE_PAYLOAD_BYTES) {
            return tag;
        }
        MKNpc.LOGGER.warn("Workspace screen payload for {}:{} at {} would encode {} bytes with {} physical " +
                        "authoring pieces; sending metadata only to avoid an oversized custom payload.",
                workspace.namespace(), workspace.structureName(), workspace.anchor(), encodedBytes,
                physicalPieces.size());
        return editableWorkspaceTag(workspace);
    }

    static MKWorkspaceMutationPreflight compactPreflight(MKWorkspaceMutationPreflight preflight) {
        return new MKWorkspaceMutationPreflight(
                preflight.report(),
                withPiecesPreservingMetadata(preflight.workspaceWithDirtyLayers(), List.of())
        );
    }

    static void warnIfLarge(String packetName, int encodedBytes) {
        if (encodedBytes >= LARGE_PAYLOAD_WARNING_BYTES) {
            MKNpc.LOGGER.warn("Workspace packet {} encoded {} bytes; this is near the custom payload limit.",
                    packetName, encodedBytes);
        }
    }

    private static List<MKWorkspacePieceDefinition> physicalAuthoringPieces(MKStructureWorkspace workspace) {
        return workspace.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
    }

    private static int encodedNbtBytes(CompoundTag tag) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeNbt(tag);
            return buffer.writerIndex();
        } finally {
            buffer.release();
        }
    }

    private static MKStructureWorkspace withPiecesPreservingMetadata(MKStructureWorkspace workspace,
                                                                     List<MKWorkspacePieceDefinition> pieces) {
        return new MKStructureWorkspace(
                workspace.id(),
                workspace.anchor(),
                workspace.namespace(),
                workspace.structureName(),
                workspace.topologyProfile(),
                workspace.dimensions(),
                workspace.palette(),
                workspace.stairConfig(),
                workspace.verticalAccessPlacement(),
                workspace.shellMargin(),
                workspace.exteriorAirMargin(),
                workspace.previewMargin(),
                workspace.verticalAccessSpec(),
                workspace.familyDefinitions(),
                workspace.openingProfiles(),
                workspace.linearRunFamilies(),
                workspace.insertFamilies(),
                workspace.createdAt(),
                workspace.updatedAt(),
                pieces,
                workspace.layerStates()
        );
    }
}
