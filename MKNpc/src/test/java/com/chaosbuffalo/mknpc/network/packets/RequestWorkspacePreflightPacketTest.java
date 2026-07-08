package com.chaosbuffalo.mknpc.network.packets;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayer;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceGeneratedLayerState;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceInvalidationReport;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationPreflight;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceMutationSafety;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceTemplateRemapSuggestion;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestWorkspacePreflightPacketTest {
    @Test
    void packetRoundTripsAcceptedRemaps() {
        List<MKWorkspaceTemplateRemapSuggestion> acceptedRemaps = List.of(
                new MKWorkspaceTemplateRemapSuggestion(
                        MKWorkspacePlannerId.of("keep.main.floor_plan.room.old_room"),
                        MKWorkspacePlannerId.of("keep.main.floor_plan.room.new_room"),
                        100,
                        "same floor piece kind, dimensions, and connector signature")
        );
        RequestWorkspacePreflightPacket packet = new RequestWorkspacePreflightPacket(
                MKStructureWorkspace.createDraft(BlockPos.ZERO), acceptedRemaps);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            packet.toBytes(buffer);

            RequestWorkspacePreflightPacket decoded = new RequestWorkspacePreflightPacket(buffer);

            assertEquals(acceptedRemaps, decoded.acceptedRemaps());
        } finally {
            buffer.release();
        }
    }

    @Test
    void editableWorkspacePayloadDropsPiecesButPreservesMetadata() {
        MKStructureWorkspace workspace = workspaceWithPieces(piece("authored", Map.of()));

        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(
                MKWorkspacePacketPayloads.editableWorkspaceTag(workspace));

        assertEquals(List.of(), decoded.pieces());
        assertEquals(workspace.id(), decoded.id());
        assertEquals(workspace.createdAt(), decoded.createdAt());
        assertEquals(workspace.updatedAt(), decoded.updatedAt());
        assertEquals(workspace.layerStates(), decoded.layerStates());
    }

    @Test
    void screenWorkspacePayloadKeepsOnlyPhysicalAuthoringPieces() {
        MKWorkspacePieceDefinition authored = piece("authored", Map.of(
                MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG, MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT,
                MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, "true",
                MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG, "source"));
        MKWorkspacePieceDefinition ordinary = piece("ordinary", Map.of());
        MKWorkspacePieceDefinition derived = piece("derived", Map.of(
                MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG, MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT,
                MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, "false",
                MKWorkspaceTemplateReuseTags.SOURCE_ID_TAG, "source"));
        MKStructureWorkspace workspace = workspaceWithPieces(authored, ordinary, derived);

        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(
                MKWorkspacePacketPayloads.screenWorkspaceTag(workspace));

        assertEquals(List.of("authored", "ordinary"),
                decoded.pieces().stream().map(MKWorkspacePieceDefinition::pieceName).toList());
    }

    @Test
    void screenWorkspacePayloadFallsBackToMetadataOnlyWhenPhysicalPiecesAreTooLarge() {
        String largeTagValue = "x".repeat(5000);
        MKWorkspacePieceDefinition[] pieces = IntStream.range(0, 250)
                .mapToObj(index -> piece("authored_" + index, Map.of("large", largeTagValue)))
                .toArray(MKWorkspacePieceDefinition[]::new);
        MKStructureWorkspace workspace = workspaceWithPieces(pieces);

        MKStructureWorkspace decoded = MKStructureWorkspace.fromTag(
                MKWorkspacePacketPayloads.screenWorkspaceTag(workspace));

        assertEquals(List.of(), decoded.pieces());
        assertEquals(workspace.id(), decoded.id());
        assertEquals(workspace.updatedAt(), decoded.updatedAt());
    }

    @Test
    void preflightPayloadDropsDirtyWorkspacePiecesButKeepsReportAndLayerState() {
        MKStructureWorkspace workspace = workspaceWithPieces(piece("authored", Map.of()));
        MKWorkspaceInvalidationReport report = new MKWorkspaceInvalidationReport(
                List.of(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS),
                List.of(MKWorkspacePlannerId.of("keep.main.floor_plan.room.new_room")),
                List.of(),
                List.of(),
                MKWorkspaceMutationSafety.SAFE_RELAYOUT,
                "safe relayout",
                "apply",
                List.of()
        );
        MKWorkspaceMutationPreflight preflight = new MKWorkspaceMutationPreflight(report, workspace);

        MKWorkspaceMutationPreflight compact = MKWorkspacePacketPayloads.compactPreflight(preflight);

        assertEquals(report, compact.report());
        assertEquals(List.of(), compact.workspaceWithDirtyLayers().pieces());
        assertEquals(workspace.layerStates(), compact.workspaceWithDirtyLayers().layerStates());
        assertEquals(workspace.updatedAt(), compact.workspaceWithDirtyLayers().updatedAt());
    }

    private static MKStructureWorkspace workspaceWithPieces(MKWorkspacePieceDefinition... pieces) {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        return new MKStructureWorkspace(
                draft.id(),
                draft.anchor(),
                draft.namespace(),
                draft.structureName(),
                draft.topologyProfile(),
                draft.dimensions(),
                draft.palette(),
                draft.stairConfig(),
                draft.verticalAccessPlacement(),
                draft.shellMargin(),
                draft.exteriorAirMargin(),
                draft.previewMargin(),
                draft.verticalAccessSpec(),
                draft.familyDefinitions(),
                draft.openingProfiles(),
                draft.linearRunFamilies(),
                draft.insertFamilies(),
                100L,
                200L,
                List.of(pieces),
                List.of(MKWorkspaceGeneratedLayerState.unlocked(MKWorkspaceGeneratedLayer.TEMPLATE_BINDINGS,
                        123L, 456L).markDirty())
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, Map<String, String> tags) {
        LinkedHashMap<String, String> resolvedTags = new LinkedHashMap<>(tags);
        resolvedTags.put("workspace_piece_kind", "template");
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan.room",
                MKWorkspacePlannerId.of("keep.main.floor_plan.room." + pieceName),
                0,
                MKWorkspaceDimensions.defaultDimensions(),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, 1, 1, 1),
                new BoundingBox(0, 0, 0, 1, 1, 1),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                resolvedTags
        );
    }
}
