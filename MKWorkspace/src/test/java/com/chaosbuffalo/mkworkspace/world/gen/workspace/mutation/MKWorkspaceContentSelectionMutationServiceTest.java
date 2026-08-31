package com.chaosbuffalo.mkworkspace.world.gen.workspace.mutation;

import com.chaosbuffalo.mkworkspace.world.gen.workspace.change.operations.MKWorkspaceContentSelectionChangePayload;
import com.chaosbuffalo.mkworkspace.world.gen.workspace.export.MKWorkspaceBackupManifestWriter;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MKWorkspaceContentSelectionMutationServiceTest {
    private final MKWorkspaceContentSelectionMutationService service =
            new MKWorkspaceContentSelectionMutationService();

    @Test
    void promotionPreservesPhysicalIdentityAndMakesVariantANewCanonical() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition blank = piece(draft, "platform_blank", "platform_contents",
                "platform_family", MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0);
        MKWorkspacePieceDefinition gazebo = piece(draft, "fire_shrine_gazebo", "platform_contents",
                "platform_family", MKWorkspaceTemplatePurpose.FAMILY_VARIANT, 1);
        MKStructureWorkspace workspace = draft.withPieces(List.of(blank, gazebo));
        var change = new MKWorkspaceContentSelectionChangePayload(
                MKWorkspaceContentSelectionChangePayload.Kind.PROMOTE_VARIANT, gazebo.pieceId(),
                "gazebo_family", 3, true, MKWorkspaceTemplatePurpose.FAMILY_VARIANT);

        MKStructureWorkspace updated;
        try (var ignored = MKWorkspaceBackupManifestWriter.enterTransaction(null)) {
            updated = service.apply(workspace, change);
        }

        MKWorkspacePieceDefinition promoted = updated.pieces().stream()
                .filter(piece -> piece.pieceId().equals(gazebo.pieceId())).findFirst().orElseThrow();
        assertEquals(gazebo.pieceId(), promoted.pieceId());
        assertEquals(gazebo.worldOrigin(), promoted.worldOrigin());
        assertEquals(gazebo.exportBounds(), promoted.exportBounds());
        assertEquals("gazebo_family", MKWorkspaceContentSelectionTags.familyId(promoted));
        assertEquals("platform_contents", MKWorkspaceContentSelectionTags.topologySlotId(promoted));
        assertEquals(3, MKWorkspaceContentSelectionTags.familyWeight(promoted.tags()));
        assertEquals(0, promoted.variantIndex());
        assertEquals("0", promoted.tags().get("workspace_variant_index"));
        assertEquals("gazebo_family", promoted.tags().get("workspace_base_name"));
        assertEquals("gazebo_family:canonical",
                promoted.tags().get(MKWorkspaceContentSelectionTags.CATALOG_MEMBER_ID));
        assertEquals(MKWorkspaceTemplatePurpose.FAMILY_CANONICAL,
                MKWorkspaceContentSelectionTags.purpose(promoted));

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary relayout =
                new MKWorkspacePieceRelayoutService().summarizeWorkspaceCatalogRelayout(workspace, updated)
                        .orElseThrow();
        assertEquals(2, relayout.preservedCount());
        assertEquals(0, relayout.newCount());
        assertEquals(0, relayout.removedCount());
        assertTrue(relayout.impacts().stream().anyMatch(impact ->
                "moved".equals(impact.outcome()) || "expanded".equals(impact.outcome())));
    }

    @Test
    void familySelectionChangesArePersistedOnEveryFamilyPiece() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition canonical = piece(draft, "gazebo", "platform_contents", "gazebo_family",
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0);
        MKWorkspacePieceDefinition variant = piece(draft, "gazebo_occupied", "platform_contents", "gazebo_family",
                MKWorkspaceTemplatePurpose.FAMILY_VARIANT, 1);
        MKStructureWorkspace workspace = draft.withPieces(List.of(canonical, variant));
        var change = new MKWorkspaceContentSelectionChangePayload(
                MKWorkspaceContentSelectionChangePayload.Kind.SET_FAMILY_WEIGHT, variant.pieceId(), "", 5,
                true, MKWorkspaceTemplatePurpose.FAMILY_VARIANT);

        MKStructureWorkspace updated;
        try (var ignored = MKWorkspaceBackupManifestWriter.enterTransaction(null)) {
            updated = service.apply(workspace, change);
        }

        assertTrue(updated.pieces().stream().allMatch(piece ->
                MKWorkspaceContentSelectionTags.familyWeight(piece.tags()) == 5));
    }

    @Test
    void moveRequiresCanonicalInSameTopologySlot() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        MKWorkspacePieceDefinition variant = piece(draft, "gazebo_occupied", "platform_contents", "gazebo_family",
                MKWorkspaceTemplatePurpose.FAMILY_VARIANT, 1);
        MKWorkspacePieceDefinition wrongSlotCanonical = piece(draft, "tower_room", "tower_slot", "tower_family",
                MKWorkspaceTemplatePurpose.FAMILY_CANONICAL, 0);
        MKStructureWorkspace workspace = draft.withPieces(List.of(variant, wrongSlotCanonical));
        var change = new MKWorkspaceContentSelectionChangePayload(
                MKWorkspaceContentSelectionChangePayload.Kind.MOVE_VARIANT, variant.pieceId(), "tower_family", 1,
                true, MKWorkspaceTemplatePurpose.FAMILY_VARIANT);

        assertTrue(service.validate(workspace, change).stream().anyMatch(error -> error.contains("same") ||
                error.contains("topology slot")));
    }

    @Test
    void normalizationSeparatesLegacyInsertScaffoldFromContentFamily() {
        MKStructureWorkspace draft = MKStructureWorkspace.createDraft(BlockPos.ZERO);
        Map<String, String> legacyTags = new java.util.LinkedHashMap<>();
        legacyTags.put("workspace_base_name", "platform_contents");
        legacyTags.put("workspace_piece_kind", "template");
        legacyTags.put("workspace_insert_family_id", "platform_contents");
        legacyTags.put("workspace_topology_slot_id", "workspace.insert_family.insert_socket");
        legacyTags.put(MKWorkspaceContentSelectionTags.TOPOLOGY_SLOT_ID,
                "workspace.insert_family.insert_socket");
        legacyTags.put(MKWorkspaceContentSelectionTags.FAMILY_ID, "platform_contents");
        BlockPos origin = new BlockPos(10, 64, 10);
        BoundingBox bounds = new BoundingBox(10, 64, 10, 14, 67, 14);
        MKWorkspacePieceDefinition scaffold = new MKWorkspacePieceDefinition(UUID.randomUUID(), draft.id(),
                "platform_contents", "workspace.insert_family.insert_socket", 0,
                new MKWorkspaceDimensions(5, 4, 5, 4, 5, 4, 3, 3), List.of(), origin, bounds, bounds,
                origin.above(), origin.above(2), List.of(), List.of(), legacyTags);
        MKStructureWorkspace workspace = draft.withPieces(List.of(scaffold));

        MKStructureWorkspace updated;
        try (var ignored = MKWorkspaceBackupManifestWriter.enterTransaction(null)) {
            updated = service.normalizeInsertSlotIdentities(workspace);
        }

        MKWorkspacePieceDefinition normalized = updated.pieces().getFirst();
        assertEquals(scaffold.pieceId(), normalized.pieceId());
        assertEquals(scaffold.worldOrigin(), normalized.worldOrigin());
        assertEquals("platform_contents", MKWorkspaceContentSelectionTags.topologySlotId(normalized));
        assertEquals("platform_contents", normalized.tags().get("workspace_insert_slot_id"));
        assertFalse(normalized.tags().containsKey(MKWorkspaceContentSelectionTags.FAMILY_ID));
        assertEquals(MKWorkspaceTemplatePurpose.SLOT_SCAFFOLD,
                MKWorkspaceContentSelectionTags.purpose(normalized));
    }

    private MKWorkspacePieceDefinition piece(MKStructureWorkspace workspace, String name, String slot,
                                               String family, MKWorkspaceTemplatePurpose purpose, int variantIndex) {
        Map<String, String> tags = MKWorkspaceContentSelectionTags.applyTemplate(
                MKWorkspaceContentSelectionTags.applyFamily(Map.of("workspace_base_name", name), slot, family, 1,
                        true), purpose, purpose.variant() ? name : "", 1, true);
        BlockPos origin = new BlockPos(10 + variantIndex * 10, 64, 10);
        BoundingBox bounds = new BoundingBox(origin.getX(), origin.getY(), origin.getZ(), origin.getX() + 4,
                origin.getY() + 3, origin.getZ() + 4);
        return new MKWorkspacePieceDefinition(UUID.randomUUID(), workspace.id(), name, slot, variantIndex,
                new MKWorkspaceDimensions(5, 4, 5, 4, 5, 4, 3, 3), List.of(), origin, bounds, bounds,
                origin.above(), origin.above(2), List.of(), List.of(), tags);
    }
}
