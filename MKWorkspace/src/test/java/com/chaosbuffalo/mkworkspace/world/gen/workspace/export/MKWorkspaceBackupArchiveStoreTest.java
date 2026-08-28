package com.chaosbuffalo.mkworkspace.world.gen.workspace.export;

import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceDimensions;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspacePieceDefinition;
import com.chaosbuffalo.mkworkspaceruntime.world.gen.workspace.model.MKWorkspaceTemplateReuseTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MKWorkspaceBackupArchiveStoreTest {
    @Test
    void backupBoundsIncludeTemplateControlsOutsideTheExportBounds() {
        MKWorkspacePieceDefinition piece = new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hub_spoke_corner_north_west_template",
                "hub_spoke_corner_north_west",
                0,
                new MKWorkspaceDimensions(5, 5, 4, 4, 4, 3, 3, 3),
                1,
                List.of(),
                new BlockPos(10, 64, 10),
                new BoundingBox(10, 64, 10, 14, 67, 14),
                new BoundingBox(8, 63, 8, 16, 68, 16),
                new BlockPos(7, 65, 12),
                new BlockPos(6, 65, 12),
                List.of(new BlockPos(15, 66, 17)),
                List.of(new BlockPos(11, 63, 11)),
                Map.of("workspace_base_name", "hub_spoke_corner_north_west")
        );

        BoundingBox bounds = MKWorkspaceBackupArchiveStore.pieceBackupBounds(piece);

        assertEquals(6, bounds.minX());
        assertEquals(17, bounds.maxZ());
        assertTrue(bounds.isInside(piece.structureBlockPos()));
        assertTrue(bounds.isInside(piece.signPos()));
        assertTrue(bounds.isInside(piece.markerPositions().getFirst()));
        assertTrue(bounds.isInside(piece.generatedStairPositions().getFirst()));
        assertTrue(bounds.isInside(new BlockPos(piece.exportBounds().maxX(), piece.exportBounds().maxY(),
                piece.exportBounds().maxZ())));
    }

    @Test
    void backupBoundsDoNotTreatDerivedLogicalControlsAsPhysicalBlocks() {
        MKWorkspacePieceDefinition piece = new MKWorkspacePieceDefinition(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "hub_spoke_corner_north_west_derived",
                "hub_spoke_corner_north_west",
                0,
                new MKWorkspaceDimensions(5, 5, 4, 4, 4, 3, 3, 3),
                1,
                List.of(),
                new BlockPos(10, 64, 10),
                new BoundingBox(10, 64, 10, 14, 67, 14),
                new BoundingBox(8, 63, 8, 16, 68, 16),
                new BlockPos(7, 65, 12),
                new BlockPos(6, 65, 12),
                List.of(new BlockPos(15, 66, 17)),
                List.of(),
                Map.of(
                        MKWorkspaceTemplateReuseTags.REUSE_MODE_TAG,
                        MKWorkspaceTemplateReuseTags.REUSE_MODE_ROTATE_EXPORT,
                        MKWorkspaceTemplateReuseTags.AUTHORING_PIECE_TAG, "false"
                )
        );

        BoundingBox bounds = MKWorkspaceBackupArchiveStore.pieceBackupBounds(piece);

        assertEquals(8, bounds.minX());
        assertEquals(16, bounds.maxZ());
    }
}
