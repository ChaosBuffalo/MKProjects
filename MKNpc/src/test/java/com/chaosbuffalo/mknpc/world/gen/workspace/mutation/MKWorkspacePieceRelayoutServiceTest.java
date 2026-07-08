package com.chaosbuffalo.mknpc.world.gen.workspace.mutation;

import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKStructureWorkspace;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mknpc.world.gen.workspace.model.MKWorkspacePlannerId;
import com.chaosbuffalo.mknpc.world.gen.workspace.planner.MKPlannedPiece;
import com.chaosbuffalo.mknpc.world.gen.workspace.scaffold.MKWorkspaceGridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspacePieceRelayoutServiceTest {
    private final MKWorkspacePieceRelayoutService service = new MKWorkspacePieceRelayoutService();

    @Test
    void catalogSummaryCountsPreservedNewAndRemovedPhysicalPieces() {
        MKWorkspacePieceDefinition preserved = piece("room_a_template", "room_a", 5, 5, 5);
        MKWorkspacePieceDefinition removed = piece("room_old_template", "room_old", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(preserved, removed));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", preserved.plannerId(), 5, 5, 5),
                planned("room_b_template", "room_b", plannerId("room_b"), 5, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(1, summary.newCount());
        assertEquals(1, summary.removedCount());
        assertEquals(0, summary.rebuildRequiredCount());
        assertTrue(summary.warnings().stream().anyMatch(warning -> warning.contains("preserved")));
    }

    @Test
    void catalogSummaryTreatsExpansionAsPreservedWork() {
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", existingPiece.plannerId(), 7, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(1, summary.preservedCount());
        assertEquals(1, summary.expandedCount());
        assertEquals(0, summary.rebuildRequiredCount());
    }

    @Test
    void catalogSummaryTreatsShrinkAsAffectedRebuild() {
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", 7, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));
        List<MKPlannedPiece> targetPieces = List.of(
                planned("room_a_template", "room_a", existingPiece.plannerId(), 5, 5, 5)
        );

        MKWorkspacePieceRelayoutService.CatalogRelayoutSummary summary = service
                .summarizeCatalogRelayout(existing, existing, targetPieces, targetPieces)
                .orElseThrow();

        assertEquals(0, summary.preservedCount());
        assertEquals(1, summary.rebuildRequiredCount());
    }

    @Test
    void catalogSummarySupportsRemovingAllPhysicalPieces() {
        MKWorkspacePieceDefinition existingPiece = piece("room_a_template", "room_a", 5, 5, 5);
        MKStructureWorkspace existing = MKStructureWorkspace.createDraft(BlockPos.ZERO)
                .withPieces(List.of(existingPiece));

        Optional<MKWorkspacePieceRelayoutService.CatalogRelayoutSummary> summary = service
                .summarizeCatalogRelayout(existing, existing, List.of(), List.of());

        assertTrue(summary.isPresent());
        assertEquals(1, summary.get().removedCount());
    }

    private static MKPlannedPiece planned(String pieceName, String baseName, MKWorkspacePlannerId plannerId,
                                          int width, int length, int height) {
        return new MKPlannedPiece(
                "floor.plan.room",
                pieceName,
                width,
                length,
                height,
                List.of(),
                tags(baseName),
                plannerId
        );
    }

    private static MKWorkspacePieceDefinition piece(String pieceName, String baseName,
                                                   int width, int length, int height) {
        return new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                pieceName,
                "floor.plan.room",
                plannerId(baseName),
                0,
                new MKWorkspaceDimensions(width, length, height, height, height, 3, 3, 3),
                1,
                List.of(),
                BlockPos.ZERO,
                new BoundingBox(0, 0, 0, width - 1, height - 1, length - 1),
                new BoundingBox(0, 0, 0, width + 3, height - 1, length + 3),
                BlockPos.ZERO,
                BlockPos.ZERO,
                List.of(),
                List.of(),
                tags(baseName)
        );
    }

    private static MKWorkspacePlannerId plannerId(String baseName) {
        return MKWorkspacePlannerId.of("floor").child("plan").child("room").child(baseName);
    }

    private static Map<String, String> tags(String baseName) {
        return Map.of(
                MKWorkspaceGridLayout.TAG_BASE_NAME, baseName,
                MKWorkspaceGridLayout.TAG_VARIANT_INDEX, "0",
                "workspace_piece_kind", "template"
        );
    }
}
