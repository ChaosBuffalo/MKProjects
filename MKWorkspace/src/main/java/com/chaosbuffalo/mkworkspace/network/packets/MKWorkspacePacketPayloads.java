package com.chaosbuffalo.mkworkspace.network.packets;

import com.chaosbuffalo.mkworkspace.MKWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.nbt.NbtOps;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

final class MKWorkspacePacketPayloads {
    private static final int LARGE_PAYLOAD_WARNING_BYTES = 900_000;
    private static final int MAX_WORKSPACE_PAYLOAD_BYTES = 983_040;
    static final int WORKSPACE_PIECE_CHUNK_TARGET_BYTES = 512 * 1024;
    private static final String PIECES_KEY = "pieces";

    private MKWorkspacePacketPayloads() {
    }

    record PieceChunk(List<MKWorkspacePieceDefinition> pieces, int nextOffset, int totalPieces, long revision) {
        boolean hasMore() {
            return nextOffset < totalPieces;
        }
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
        MKWorkspace.LOGGER.warn("Workspace screen payload for {}:{} at {} would encode {} bytes with {} physical " +
                        "authoring pieces; sending metadata only to avoid an oversized custom payload.",
                workspace.namespace(), workspace.structureName(), workspace.anchor(), encodedBytes,
                physicalPieces.size());
        return editableWorkspaceTag(workspace);
    }

    static CompoundTag screenWorkspaceChunkTag(MKStructureWorkspace workspace, PieceChunk chunk) {
        return withPiecesPreservingMetadata(workspace, chunk.pieces()).toTag();
    }

    static PieceChunk firstPieceChunk(MKStructureWorkspace workspace) {
        List<MKWorkspacePieceDefinition> pieces = physicalAuthoringPieces(workspace);
        return pieceChunk(workspace, pieces, 0);
    }

    static PieceChunk pieceChunk(MKStructureWorkspace workspace, int offset) {
        List<MKWorkspacePieceDefinition> pieces = physicalAuthoringPieces(workspace);
        return pieceChunk(workspace, pieces, offset);
    }

    static CompoundTag pieceListTag(List<MKWorkspacePieceDefinition> pieces) {
        Tag pieceList = MKWorkspacePieceDefinition.CODEC.listOf()
                .encodeStart(NbtOps.INSTANCE, pieces)
                .resultOrPartial(error -> MKWorkspace.LOGGER.error("Failed to encode workspace piece chunk: {}", error))
                .orElseThrow(() -> new IllegalStateException("Failed to encode workspace piece chunk"));
        CompoundTag tag = new CompoundTag();
        tag.put(PIECES_KEY, pieceList);
        return tag;
    }

    static List<MKWorkspacePieceDefinition> parsePieceListTag(CompoundTag tag) {
        Tag pieceList = tag.get(PIECES_KEY);
        if (pieceList == null) {
            return List.of();
        }
        return MKWorkspacePieceDefinition.CODEC.listOf()
                .parse(NbtOps.INSTANCE, pieceList)
                .resultOrPartial(error -> MKWorkspace.LOGGER.error("Failed to parse workspace piece chunk: {}", error))
                .orElseThrow(() -> new IllegalStateException("Failed to parse workspace piece chunk"));
    }

    static MKWorkspaceMutationPreflight compactPreflight(MKWorkspaceMutationPreflight preflight) {
        return new MKWorkspaceMutationPreflight(
                preflight.report(),
                withPiecesPreservingMetadata(preflight.workspaceWithDirtyLayers(), List.of())
        );
    }

    static void warnIfLarge(String packetName, int encodedBytes) {
        if (encodedBytes >= LARGE_PAYLOAD_WARNING_BYTES) {
            MKWorkspace.LOGGER.warn("Workspace packet {} encoded {} bytes; this is near the custom payload limit.",
                    packetName, encodedBytes);
        }
    }

    static List<MKWorkspacePieceDefinition> physicalAuthoringPieces(MKStructureWorkspace workspace) {
        return workspace.pieces().stream()
                .filter(piece -> !MKWorkspaceTemplateReuseTags.isDerived(piece.tags()))
                .toList();
    }

    static int encodedNbtBytes(CompoundTag tag) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeNbt(tag);
            return buffer.writerIndex();
        } finally {
            buffer.release();
        }
    }

    static MKStructureWorkspace withPiecesPreservingMetadata(MKStructureWorkspace workspace,
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

    private static PieceChunk pieceChunk(MKStructureWorkspace workspace, List<MKWorkspacePieceDefinition> pieces,
                                         int offset) {
        int safeOffset = Math.max(0, Math.min(offset, pieces.size()));
        ArrayList<MKWorkspacePieceDefinition> chunkPieces = new ArrayList<>();
        int nextOffset = safeOffset;
        for (int index = safeOffset; index < pieces.size(); index++) {
            chunkPieces.add(pieces.get(index));
            int encodedBytes = encodedNbtBytes(pieceListTag(chunkPieces));
            if (encodedBytes > WORKSPACE_PIECE_CHUNK_TARGET_BYTES && chunkPieces.size() > 1) {
                chunkPieces.removeLast();
                break;
            }
            nextOffset = index + 1;
            if (encodedBytes > WORKSPACE_PIECE_CHUNK_TARGET_BYTES) {
                MKWorkspace.LOGGER.warn("Workspace piece {} at {} encoded {} bytes alone, exceeding the {} byte chunk " +
                                "target.",
                        pieces.get(index).pieceName(), workspace.anchor(), encodedBytes,
                        WORKSPACE_PIECE_CHUNK_TARGET_BYTES);
                break;
            }
        }
        return new PieceChunk(List.copyOf(chunkPieces), nextOffset, pieces.size(), pieceRevision(workspace, pieces));
    }

    private static long pieceRevision(MKStructureWorkspace workspace, List<MKWorkspacePieceDefinition> pieces) {
        long revision = workspace.id().hashCode();
        revision = (revision * 31L) + workspace.updatedAt();
        revision = (revision * 31L) + pieces.size();
        for (MKWorkspacePieceDefinition piece : pieces) {
            revision = (revision * 31L) + piece.pieceId().hashCode();
            revision = (revision * 31L) + piece.plannerId().value().hashCode();
            revision = (revision * 31L) + piece.variantIndex();
        }
        return revision;
    }
}
