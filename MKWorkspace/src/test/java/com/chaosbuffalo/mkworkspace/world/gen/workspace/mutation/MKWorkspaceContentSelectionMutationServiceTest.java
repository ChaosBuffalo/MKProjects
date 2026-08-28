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
        MKWorkspacePieceDefinition gazebo = piece(draft, "fire_shrine_gazebo", "platform_contents",
                "platform_family", MKWorkspaceTemplatePurpose.FAMILY_VARIANT, 1);
        MKStructureWorkspace workspace = draft.withPieces(List.of(gazebo));
        var change = new MKWorkspaceContentSelectionChangePayload(
                MKWorkspaceContentSelectionChangePayload.Kind.PROMOTE_VARIANT, gazebo.pieceId(),
                "gazebo_family", 3, true, MKWorkspaceTemplatePurpose.FAMILY_VARIANT);

        MKStructureWorkspace updated;
        try (var ignored = MKWorkspaceBackupManifestWriter.enterTransaction(null)) {
            updated = service.apply(workspace, change);
        }

        MKWorkspacePieceDefinition promoted = updated.pieces().getFirst();
        assertEquals(gazebo.pieceId(), promoted.pieceId());
        assertEquals(gazebo.worldOrigin(), promoted.worldOrigin());
        assertEquals(gazebo.exportBounds(), promoted.exportBounds());
        assertEquals("gazebo_family", MKWorkspaceContentSelectionTags.familyId(promoted));
        assertEquals("platform_contents", MKWorkspaceContentSelectionTags.topologySlotId(promoted));
        assertEquals(3, MKWorkspaceContentSelectionTags.familyWeight(promoted.tags()));
        assertEquals(MKWorkspaceTemplatePurpose.FAMILY_CANONICAL,
                MKWorkspaceContentSelectionTags.purpose(promoted));
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
